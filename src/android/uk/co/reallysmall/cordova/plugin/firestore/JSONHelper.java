package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import android.util.Log;

public class JSONHelper {
    static JSONObject toJSON(Map<String, Object> values) throws JSONException {
        JSONObject result = new JSONObject();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = toJSON((Map<String, Object>)value);;
            } else if (value instanceof List) {
                value = toJSONArray((List) value);
            } else if (value instanceof Date) {
                value = new JSONDateWrapper((Date) value);
            }
            result.put(entry.getKey(), value);
        }
        return result;
    }

    static JSONArray toJSONArray(List values) throws JSONException {
        JSONArray result = new JSONArray();

        for (Object value : values) {
            if (value instanceof Map) {
                value = toJSON((Map)value);;
            } else if (value instanceof List) {
                value = toJSONArray((List) value);
            } else if (value instanceof Date) {
                value = new JSONDateWrapper((Date) value);
            }
            result.put(value);
        }
        return result;
    }

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

        for (Map.Entry<String,Object> entry : value.entrySet()) {

            if (JSONDateWrapper.isWrappedDate(entry.getValue())) {
                entry.setValue(JSONDateWrapper.unwrapDate(entry.getValue()));
            } else  if (entry.getValue() instanceof Map) {
                entry.setValue(toSettableMapInternal((Map<Object, Object>)entry.getValue()));
            }
        }
        return value;
    }

    static Map<Object, Object> toSettableMapInternal(Map<Object, Object> value) {

        for (Map.Entry<Object,Object> entry : value.entrySet()) {

            if (JSONDateWrapper.isWrappedDate(entry.getValue())) {
                entry.setValue(JSONDateWrapper.unwrapDate(entry.getValue()));
            } else  if (entry.getValue() instanceof Map) {
                entry.setValue(toSettableMapInternal((Map<Object, Object>)entry.getValue()));
            }
        }
        return value;
    }
}
