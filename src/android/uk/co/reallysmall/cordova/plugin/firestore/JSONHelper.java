package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.GeoPoint;
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
                value = toJSON((Map<String, Object>) value);
                ;
            } else if (value instanceof List) {
                value = toJSONArray((List) value);
            } else if (value instanceof Date) {
                value = new JSONDateWrapper((Date) value);
            } else if (value instanceof GeoPoint) {
                value = new JSONGeopointWrapper((GeoPoint) value);
            }
            result.put(entry.getKey(), value);
        }
        return result;
    }

    static JSONArray toJSONArray(List values) throws JSONException {
        JSONArray result = new JSONArray();

        for (Object value : values) {
            if (value instanceof Map) {
                value = toJSON((Map) value);
                ;
            } else if (value instanceof List) {
                value = toJSONArray((List) value);
            } else if (value instanceof Date) {
                value = new JSONDateWrapper((Date) value);
            } else if (value instanceof GeoPoint) {
                value = new JSONGeopointWrapper((GeoPoint) value);
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

        for (Map.Entry<String, Object> entry : value.entrySet()) {

            Object entryValue = entry.getValue();

            if (JSONGeopointWrapper.isWrappedGeoPoint(entryValue)) {
                entry.setValue(JSONGeopointWrapper.unwrapGeoPoint(entryValue));
            } else if (JSONDateWrapper.isWrappedDate(entryValue)) {
                entry.setValue(JSONDateWrapper.unwrapDate(entryValue));
            } else if (entryValue instanceof Map) {
                entry.setValue(toSettableMapInternal((Map<Object, Object>) entryValue));
            } else if (FieldValueHelper.isWrappedFieldValue(entryValue)) {
                entry.setValue(FieldValueHelper.unwrapFieldValue(entry.getValue()));
            }
        }
        return value;
    }

    static Map<Object, Object> toSettableMapInternal(Map<Object, Object> value) {

        for (Map.Entry<Object, Object> entry : value.entrySet()) {

            Object entryValue = entry.getValue();

            if (JSONDateWrapper.isWrappedDate(entryValue)) {
                entry.setValue(JSONDateWrapper.unwrapDate(entryValue));
            } else if (JSONGeopointWrapper.isWrappedGeoPoint(entryValue)) {
                entry.setValue(JSONGeopointWrapper.unwrapGeoPoint(entryValue));
            } else if (entryValue instanceof Map) {
                entry.setValue(toSettableMapInternal((Map<Object, Object>) entryValue));
            } else if (FieldValueHelper.isWrappedFieldValue(entryValue)) {
                entry.setValue(FieldValueHelper.unwrapFieldValue(entry.getValue()));
            }
        }
        return value;
    }
}
