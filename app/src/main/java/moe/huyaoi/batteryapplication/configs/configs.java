package moe.huyaoi.batteryapplication.configs;

import android.content.SharedPreferences;

import java.util.UUID;

public class configs {
    static SharedPreferences sharedPreferences;

    public static void Init(SharedPreferences sp){
        sharedPreferences = sp;
    }

    public static String GetUID(){
        String UID = sharedPreferences.getString("DeviceIdentity", "");
        if (UID.isEmpty()) {
            String uniqueID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("DeviceIdentity", uniqueID);
            UID = uniqueID;
            editor.apply();
        }
        return UID;
    }

    public static Boolean GetTcpSwitch(){
        Boolean rsl = sharedPreferences.getBoolean("TcpConnect", false);
        SetTcpSwitch(rsl);
        return rsl;
    }

    public static void SetTcpSwitch(Boolean input){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("TcpConnect", input);
        editor.apply();
    }

    public static void SetTcpServerAddress(String input){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("TcpServerAddress", input);
        editor.apply();
    }

    public static String GetTcpServerAddress(){
        return sharedPreferences.getString("TcpServerAddress", "127.0.0.1");
    }

    public static void SetTcpServerPort(int input){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("TcpServerPort", input);
        editor.apply();
    }

    public static int GetTcpServerPort(){
        return sharedPreferences.getInt("TcpServerPort", 12345);
    }

    public static Boolean GetUdpSwitch(){
        Boolean rsl = sharedPreferences.getBoolean("UdpConnect", true);
        SetUdpSwitch(rsl);
        return rsl;
    }

    public static void SetUdpSwitch(Boolean input){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("UdpConnect", input);
        editor.apply();
    }

    public static void SetUdpPort(int input){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("SetUdpPort", input);
        editor.apply();
    }

    public static int GetUdpPort(){
        return sharedPreferences.getInt("GetUdpPort", 34555);
    }
}
