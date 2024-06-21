package moe.huyaoi.batteryapplication.network;

import android.util.Log;

import moe.huyaoi.batteryapplication.configs.configs;
import moe.huyaoi.batteryapplication.data.device;
import moe.huyaoi.batteryapplication.dataTypes.DeviceItem;
import moe.huyaoi.batteryapplication.dataTypes.DeviceItemJson;
import moe.huyaoi.batteryapplication.dataTypes.HeartbeatPacket;
import moe.huyaoi.batteryapplication.event.TextEvent;
import moe.huyaoi.batteryapplication.event.TextEventSource;
import moe.huyaoi.batteryapplication.json.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class tcp {
    private static final String TAG = "Network.TCP";
    private static Socket tcpSocket;
    private static int serverPort = 12345; // 选择一个端口号进行UDP广播
    private static String serverAddress = "127.0.0.1"; // 广播地址
    private static final ReentrantReadWriteLock tcpLock = new ReentrantReadWriteLock();

    private static BufferedReader reader;

    public static TextEventSource Message = new TextEventSource();

    public static void sendTCPMessage(byte[] message){
        if(!configs.GetTcpSwitch()){
            return;
        }
        tcpLock.writeLock().lock();
        try {
            if (tcpSocket == null){
                Message.triggerEvent(new TextEvent("尝试连接"));
                try {
                    serverAddress = configs.GetTcpServerAddress();
                    serverPort = configs.GetTcpServerPort();

                    tcpSocket = new Socket(serverAddress, serverPort);
                    tcpSocket.setSoTimeout(5000);
                    tcpSocket.setTcpNoDelay(false);
                    tcpSocket.setKeepAlive(true);
                    reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                } catch (Exception e){
                    if(e instanceof SocketTimeoutException){
                        Message.triggerEvent(new TextEvent("与TCP服务器的连接超时"));
                    }else{
                        if (Objects.requireNonNull(e.getMessage()).contains("Connection reset")) {
                            Message.triggerEvent(new TextEvent("与TCP服务器的连接被服务器重置"));
                        }else if(Objects.requireNonNull(e.getMessage()).contains("Connection refused")){
                            Message.triggerEvent(new TextEvent("与TCP服务器的连接被拒绝"));
                        }else{
                            Message.triggerEvent(new TextEvent("与TCP服务器的连接发生错误"));
                        }
                    }

                    Log.w(TAG, Objects.requireNonNull(e.getMessage()));
                    tcpSocket = null;
                    return;
                }

                if(tcpSocket == null){
                    Message.triggerEvent(new TextEvent("已断开与服务器的连接"));
                    return;
                }

                new Thread(() -> {
                    try {
                        String msg;
                        Message.triggerEvent(new TextEvent("已连接"));
                        while ((msg = reader.readLine()) != null) {
                            Log.v(TAG,"Test Reader " + msg);

                            Map<String, Object> map = json.Json2Map(Arrays.toString(msg.getBytes()));

                            if (map.get("Command") != "batteryChange"){
                                continue;
                            }

                            DeviceItemJson dp = new DeviceItemJson();
                            try{
                                dp.mapTo(map);
                            } catch (Exception e){
                                Log.w(TAG,"数据包错误 " + e.getMessage());
                                continue;
                            }

                            DeviceItem s = new DeviceItem();
                            s.ID = dp.getId();
                            s.DeviceName = dp.getDeviceName();
                            s.Level = dp.getLevel();
                            s.IsCharging = dp.getIsCharging();
                            s.LastUpdateTime = System.currentTimeMillis();
                            s.Plugged = dp.getPlugged();
                            s.Current = dp.getCurrent();
                            s.Temperature = dp.getTemperature();

                            if(!Objects.equals(s.ID, configs.GetUID())){
                                device.SetOne(s);
                            }
                        }
                    } catch (IOException e) {
                        Message.triggerEvent(new TextEvent("已断开与服务器的连接"));
                        Log.w(TAG, Objects.requireNonNull(e.getMessage()));
                    }
                }).start();

                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.scheduleAtFixedRate(()->{
                    HeartbeatPacket t = new HeartbeatPacket();
                    sendTCPMessage(t.getBytes());
                }, 0, 7, TimeUnit.SECONDS);
            }
        } finally {
            tcpLock.writeLock().unlock(); // 释放写锁
        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(tcpSocket.getOutputStream())), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        out.println(new String(message));
        out.flush();
        Log.v(TAG,"Send " + new String(message));
    }

    public static void destroy(){
        if (tcpSocket != null){
            try {
                tcpSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
