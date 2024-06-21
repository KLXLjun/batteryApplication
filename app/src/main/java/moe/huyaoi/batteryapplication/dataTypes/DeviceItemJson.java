package moe.huyaoi.batteryapplication.dataTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import moe.huyaoi.batteryapplication.json.json;

public class DeviceItemJson {
    private String Command;
    private String DeviceName;
    private String ID;
    private int Level;
    private Boolean IsCharging;
    private String Plugged;
    private int Current;
    private int Temperature;

    public String getCommand() { return Command; }

    public void setCommand(String str){ Command = str; }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getId() {
        return ID;
    }

    public void setId(String id) {
        this.ID = id;
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        Level = level;
    }

    public Boolean getIsCharging() {
        return IsCharging;
    }

    public void setIsCharging(Boolean charging) {
        this.IsCharging = charging;
    }

    public String getPlugged() {
        return Plugged;
    }

    public void setPlugged(String plugged) {
        Plugged = plugged;
    }

    public int getCurrent() {
        return Current;
    }

    public void setCurrent(int current) {
        Current = current;
    }

    public int getTemperature() {
        return Temperature;
    }

    public void setTemperature(int temperature) {
        Temperature = temperature;
    }

    public void input(DeviceItem item){
        DeviceName = item.DeviceName;
        ID = item.ID;
        Level = item.Level;
        IsCharging = item.IsCharging;
        Plugged = item.Plugged;
        Current = item.Current;
        Temperature = item.Temperature;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> result = new HashMap<>();
        result.put("Command", Command);
        result.put("DeviceName", DeviceName);
        result.put("ID", ID);
        result.put("Level",Level);
        result.put("IsCharging",IsCharging);
        result.put("Plugged", Plugged);
        result.put("Current", Current);
        result.put("Temperature",Temperature);
        return result;
    }

    public void mapTo(Map<String, Object> objectMap){
        DeviceName = (String) objectMap.get("DeviceName");
        ID = (String) objectMap.get("ID");
        Level = (Integer) objectMap.get("Level");
        IsCharging = (Boolean) objectMap.get("IsCharging");
        Plugged = (String) objectMap.get("Plugged");
        Current = (Integer) objectMap.get("Current");
        Temperature = (Integer) objectMap.get("Temperature");
    }

    public byte[] toBytes(){
        return json.Map2Json(toMap()).getBytes();
    }
}
