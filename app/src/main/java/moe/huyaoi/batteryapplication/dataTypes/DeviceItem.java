package moe.huyaoi.batteryapplication.dataTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import moe.huyaoi.batteryapplication.json.json;

public class DeviceItem  {
    public String DeviceName;
    public String ID;
    public int Level;
    public Boolean IsCharging;
    public String Plugged;
    public int Current;
    public int Temperature;
    public Long LastUpdateTime;
    public boolean IsTcpConnect;
    public boolean IsUdpConnect;
}

