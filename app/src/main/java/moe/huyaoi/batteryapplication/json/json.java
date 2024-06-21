package moe.huyaoi.batteryapplication.json;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class json {
    public static Map<String, Object> Json2Map(String jsonString) {
        Map<String, Object> result = new HashMap<>();
        jsonString = jsonString.trim();
        if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
            jsonString = jsonString.substring(1, jsonString.length() - 1).trim();
            String[] keyValuePairs = jsonString.split(",");
            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split(":");
                String key = keyValue[0].trim().replaceAll("^\"|\"$", ""); // 去掉引号
                String value = keyValue[1].trim();
                if (value.startsWith("\"")) {
                    value = value.replaceAll("^\"|\"$", ""); // 去掉引号
                    result.put(key, value);
                } else if (value.equals("true") || value.equals("false")) {
                    result.put(key, Boolean.parseBoolean(value));
                } else if (value.matches("-?\\d+(\\.\\d+)?")) {
                    result.put(key, Integer.parseInt(value));
                }
            }
        }
        return result;
    }

    public static String Map2Json(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\": ");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            json.append(", ");
        }
        json.delete(json.length() - 2, json.length()); // 删除最后一个逗号和空格
        json.append("}");
        return json.toString();
    }
}
