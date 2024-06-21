package moe.huyaoi.batteryapplication.dataTypes;

import java.util.HashMap;
import java.util.Map;

import moe.huyaoi.batteryapplication.json.json;

public class HeartbeatPacket {
    public byte[] getBytes(){
        Map<String, Object> result = new HashMap<>();
        result.put("Command", "heartBeat");
        return json.Map2Json(result).getBytes();
    }
}
