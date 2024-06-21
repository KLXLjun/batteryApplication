package moe.huyaoi.batteryapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import moe.huyaoi.batteryapplication.adapter.CommonAdapter;
import moe.huyaoi.batteryapplication.adapter.CommonViewHolder;
import moe.huyaoi.batteryapplication.configs.configs;
import moe.huyaoi.batteryapplication.data.device;
import moe.huyaoi.batteryapplication.dataTypes.DeviceItem;
import moe.huyaoi.batteryapplication.dataTypes.DeviceItemJson;
import moe.huyaoi.batteryapplication.network.tcp;
import moe.huyaoi.batteryapplication.network.udp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String SharedName = "BatteryApplicationPrefs";

    private DeviceItemJson LastInfo = null;

    private TextView deviceName;
    private TextView deviceUid;
    private TextView batteryInfoTextView;
    private TextView connectionStatusTextView;

    private BroadcastReceiver batteryLevelReceiver;

    private CommonAdapter commandAdapter;
    private ListView listView;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configs.Init(getSharedPreferences(SharedName, MODE_PRIVATE));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", getPackageName());
                intent.putExtra("app_uid", getApplicationInfo().uid);
            } else {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
            }
            startActivity(intent);
        }

        commandAdapter = CreateList();

        batteryInfoTextView = findViewById(R.id.batteryInfoTextView);
        deviceName = findViewById(R.id.deviceName);
        deviceUid = findViewById(R.id.uid);
        connectionStatusTextView = findViewById(R.id.connectionStatus);

        connectionStatusTextView.setText("未连接至服务端");

        Context ctx = this;
        ImageButton button = (ImageButton) findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx,SettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        deviceName.setText(getDeviceName());
        deviceUid.setText(configs.GetUID());

        // 初始化广播接收器
        batteryLevelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            BatteryManager batteryManager = (BatteryManager) context.getSystemService(android.content.Context.BATTERY_SERVICE);
            int batteryCurrentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            int batteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            String pluggedTxt;
            String pluggedDesc="";
            switch (plugged){
                case BatteryManager.BATTERY_PLUGGED_AC:{
                    pluggedDesc="AC";
                    pluggedTxt = "交流电";
                }break;
                case BatteryManager.BATTERY_PLUGGED_USB:{
                    pluggedDesc="USB";
                    pluggedTxt = "USB";
                }break;
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:{
                    pluggedDesc="Wireless";
                    pluggedTxt = "无线充电";
                }break;
                default:{
                    pluggedDesc="No";
                    pluggedTxt = "";
                }
            }

            float batteryPct = level * 100 / (float) scale;

            String batteryText = "电量: " + batteryPct + "%";
            if (isCharging) {
                batteryText += " ("+pluggedTxt+"充电中)";
            }
            batteryText += " 电流:" + (int)Math.floor((double) batteryCurrentNow / 1000) + "mA";
            DecimalFormat df = new DecimalFormat("#.0");
            String formattedNumber = df.format(batteryTemperature / 10); // 输出 "3.1"
            batteryText += " 温度:" + formattedNumber + "°C";

            batteryInfoTextView.setText(batteryText);

            String finalPluggedDesc = pluggedDesc;

            DeviceItemJson dp = new DeviceItemJson();
            dp.setCommand("batteryChange");
            dp.setDeviceName(getDeviceName());
            dp.setId(configs.GetUID());
            dp.setLevel(((int) batteryPct));
            dp.setIsCharging(isCharging);
            dp.setPlugged(finalPluggedDesc);
            dp.setCurrent(batteryCurrentNow);
            dp.setTemperature(batteryTemperature);
            LastInfo = dp;
            byte[] sendBytes = dp.toBytes();

            new Thread(() -> {
                udp.sendUDPMessage(sendBytes,ctx);
                tcp.sendTCPMessage(sendBytes);
            }).start();
            }
        };

        // 注册广播接收器
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(device::publicRemoveExpiredItems, 0, 3, TimeUnit.MINUTES);

        tcp.Message.addListener(event -> runOnUiThread(() -> {
            connectionStatusTextView.setText(event.getText());
        }));

        device.eventSource.addListener(event -> runOnUiThread(() -> {
            readWriteLock.writeLock().lock();
            datal.clear();
            ArrayList<DeviceItem> t = device.CloneArray();
            datal.addAll(t);
            readWriteLock.writeLock().unlock();
            readWriteLock.readLock().lock();
            commandAdapter.notifyDataSetChanged();
            readWriteLock.readLock().unlock();
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Activity进入前台时执行的操作

        updateBatteryInfo();
        if(LastInfo != null){
            byte[] sendBytes = LastInfo.toBytes();

            new Thread(() -> {
                udp.sendUDPMessage(sendBytes,getApplicationContext());
                tcp.sendTCPMessage(sendBytes);
            }).start();
        }
    }

    private void updateBatteryInfo(){
        if(LastInfo == null){
            return;
        }
        BatteryManager batteryManager = (BatteryManager) this.getSystemService(android.content.Context.BATTERY_SERVICE);
        LastInfo.setCurrent(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW));
        LastInfo.setLevel(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        unregisterReceiver(batteryLevelReceiver);
        // 关闭UDP套接字
        tcp.destroy();
        udp.destroy();
    }

    private static List<DeviceItem> datal = new ArrayList<>();//存储界面

    /**
     * 一般的更新界面
     */
    public CommonAdapter CreateList() {
        listView = (ListView) findViewById(R.id.deviceList);
        final CommonAdapter commonAdapter = new CommonAdapter<DeviceItem>(this, datal, R.layout.device_item) {
            @Override
            protected void convertView(View item, DeviceItem s) {
                TextView textView = CommonViewHolder.get(item, R.id.DeviceName);
                textView.setText(s.DeviceName);

                TextView UUIDtextView = CommonViewHolder.get(item, R.id.UUID);
                UUIDtextView.setText(s.ID);

                TextView textView1 = CommonViewHolder.get(item, R.id.MacAddress);
                String Info = s.Level + "%" + (s.IsCharging ? " - ("+ s.Plugged +"充电中)" : "") + " 电流:" + (int)Math.floor((double) s.Current / 1000) + "mA";
                DecimalFormat df = new DecimalFormat("#.0");
                String formattedNumber = df.format(s.Temperature / 10);
                Info += " 温度:" + formattedNumber + "°C";
                textView1.setText(Info);
            }
        };
        listView.setAdapter(commonAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                readWriteLock.readLock().lock(); // 获取读锁
                try {
                    Log.v(TAG, datal.get(position).DeviceName + "/" + datal.get(position).ID);
                } finally {
                    readWriteLock.readLock().unlock(); // 释放读锁
                }
            }
        });
        return commonAdapter;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String brand = Build.BRAND;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(brand) + " " + model;
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        char firstChar = str.charAt(0);
        if (Character.isUpperCase(firstChar)) {
            return str;
        } else {
            return Character.toUpperCase(firstChar) + str.substring(1);
        }
    }

    public void displayNotification(String title, String text) {
        String CHANNEL_ID = "battery_info";  // 通道ID，需要唯一
        String CHANNEL_NAME = "电池信息";  // 通道名称，用户可见

        Context context = this;

        // 创建一个NotificationManager实例
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Android Oreo (8.0, API 26) 以上版本需要一个通知通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null); // 禁用声音
            channel.enableVibration(false); // 禁用振动
            notificationManager.createNotificationChannel(channel);
        }

        // 使用NotificationCompat.Builder构建通知内容
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // 设置通知小图标
                .setContentTitle(title)  // 设置通知标题
                .setContentText(text)  // 设置通知内容
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)// 设置通知优先级
                .setSound(null) // 禁用声音
                .setVibrate(new long[]{0L}) // 禁用振动
                .setSilent(true); // 设置为静默通知，确保不会有声音或振动;

        // 通过NotificationManager发送通知
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());  // 1是通知的ID
    }
}

