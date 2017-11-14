package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JSONHelper {
    static JSONObject toJSON(Map<String, Object> values) throws JSONException {
        JSONObject result = new JSONObject();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = new JSONObject((Map) value);
            } else if (value instanceof List) {
                value = new JSONArray((List) value);
            }

            if (value instanceof Date) {
                value = new JSONDateWrapper((Date) value);
            }
            result.put(entry.getKey(), value);
        }
        return result;
    }

//    static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
//        Map<String, Object> retMap = new HashMap<String, Object>();
//
//        if (json != JSONObject.NULL) {
//            retMap = toMap(json);
//        }
//        return retMap;
//    }

//    static Map<String, Object> toMap(JSONObject object) throws JSONException {
//        Map<String, Object> map = new HashMap<String, Object>();
//
//        Iterator<String> keysItr = object.keys();
//        while (keysItr.hasNext()) {
//            String key = keysItr.next();
//            Object value = object.get(key);
//
//            if (value instanceof JSONArray) {
//                value = toList((JSONArray) value);
//            } else if (value instanceof JSONObject) {
//                value = toMap((JSONObject) value);
//            }
//            map.put(key, value);
//        }
//        return map;
//    }
//
//
//    static List<Object> toList(JSONArray array) throws JSONException {
//        List<Object> list = new ArrayList<Object>();
//        for (int i = 0; i < array.length(); i++) {
//            Object value = array.get(i);
//            if (value instanceof JSONArray) {
//                value = toList((JSONArray) value);
//            } else if (value instanceof JSONObject) {
//                value = toMap((JSONObject) value);
//            }
//            list.add(value);
//        }
//        return list;
//    }

    static Object toSettable(Object value) {

        Object result = value;
        if (value instanceof JSONObject) {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            result = new Gson().fromJson(value.toString(), type);
        }

        return result;
    }

    static Map<String, Object> toSettableMap(JSONObject rawValue) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> value = new Gson().fromJson(rawValue.toString(), type);
        return value;
    }
}
