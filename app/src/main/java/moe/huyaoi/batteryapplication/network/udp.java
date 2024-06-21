package moe.huyaoi.batteryapplication.network;

import android.content.Context;
import android.util.Log;

import moe.huyaoi.batteryapplication.configs.configs;
import moe.huyaoi.batteryapplication.data.device;
import moe.huyaoi.batteryapplication.dataTypes.DeviceItem;
import moe.huyaoi.batteryapplication.dataTypes.DeviceItemJson;
import moe.huyaoi.batteryapplication.event.TextEventSource;
import moe.huyaoi.batteryapplication.json.json;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class udp {
    private static String TAG = "Network.UDP";
    private static DatagramSocket udpSocket;
    private static int udpPort = 34555;
    private static String broadcastIP = "255.255.255.255"; // 广播地址
    private static final ReentrantReadWriteLock tcpLock = new ReentrantReadWriteLock();
    private static int bufLength = 1024;
    private static boolean running = true;
    private static TextEventSource Message = new TextEventSource();
    public static void sendUDPMessage(byte[] message, Context ctx) {
        if(!configs.GetUdpSwitch()){
            return;
        }
        tcpLock.writeLock().lock();
        udpPort = configs.GetUdpPort();
        try{
            if (udpSocket == null){
                try {
                    // 初始化UDP套接字
                    udpSocket = new DatagramSocket(udpPort);
                    udpSocket.setBroadcast(true);
                } catch (IOException e) {
                    Log.w(TAG, Objects.requireNonNull(e.getMessage()));
                }

                new Thread(() -> {
                    while(running){
                        try {
                            byte[] buf = new byte[bufLength];
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            udpSocket.receive(packet);

                            InetAddress address = packet.getAddress();
                            int port = packet.getPort();
                            packet = new DatagramPacket(buf, buf.length, address, port);
                            String received = new String(packet.getData(), 0, buf.length);
                            DeviceItemJson dp = new DeviceItemJson();
                            try{
                                dp.mapTo(json.Json2Map(received));
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

                            Log.w(TAG,"接入 " + s.ID);

                            if(!Objects.equals(s.ID, configs.GetUID())){
                                device.SetOne(s);
                            }
                        } catch (IOException e) {
                            Log.w(TAG, Objects.requireNonNull(e.getMessage()));
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            Log.w(TAG, Objects.requireNonNull(e.getMessage()));
        } finally {
            tcpLock.writeLock().unlock();
        }

        try {
            InetAddress address = InetAddress.getByName(broadcastIP);
            DatagramPacket packet = new DatagramPacket(message, message.length, address, udpPort);
            udpSocket.send(packet);
        } catch (IOException e) {
            Log.w(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    public static void destroy(){
        if (udpSocket != null) {
            udpSocket.close();
        }
    }
}
