package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JSONHelper {

    private static FirestorePlugin firestorePlugin;

    static void setPlugin(FirestorePlugin firestorePlugin) {
        JSONHelper.firestorePlugin = firestorePlugin;
    }

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
            } else if (value instanceof Timestamp) {
                value = new JSONTimestampWrapper((Timestamp) value);
            } else if (value instanceof GeoPoint) {
                value = new JSONGeopointWrapper((GeoPoint) value);
            } else if (value instanceof DocumentReference) {
                value = new JSONReferenceWrapper((DocumentReference) value);
            }
            result.put(entry.getKey(), value);
        }
        return result;
    }

    private static JSONArray toJSONArray(List values) throws JSONException {
        JSONArray result = new JSONArray();

        for (Object value : values) {
            if (value instanceof Map) {
                value = toJSON((Map) value);
                ;
            } else if (value instanceof List) {
                value = toJSONArray((List) value);
            } else if (value instanceof Date) {
                value = new JSONDateWrapper((Date) value);
            } else if (value instanceof Timestamp) {
                value = new JSONTimestampWrapper((Timestamp) value);
            } else if (value instanceof GeoPoint) {
                value = new JSONGeopointWrapper((GeoPoint) value);
            } else if (value instanceof DocumentReference) {
                value = new JSONReferenceWrapper((DocumentReference) value);
            }
            result.put(value);
        }
        return result;
    }


    public static Object fromJSON(Object value) {
        Object newValue;

        if (value instanceof String) {
            if (JSONGeopointWrapper.isWrappedGeoPoint(value)) {
                newValue = JSONGeopointWrapper.unwrapGeoPoint(value);
            } else if (JSONDateWrapper.isWrappedDate(value)) {
                newValue = JSONDateWrapper.unwrapDate(value);
            } else if (JSONTimestampWrapper.isWrappedTimestamp(value)) {
                newValue = JSONTimestampWrapper.unwrapTimestamp(value);
            } else if (JSONReferenceWrapper.isWrappedReference(value)) {
                newValue = JSONReferenceWrapper.unwrapReference(JSONHelper.firestorePlugin, value);
            } else if (FieldValueHelper.isWrappedFieldValue(value)) {
                newValue = FieldValueHelper.unwrapFieldValue(value);
            } else {
                newValue = value;
            }
        } else if (value instanceof Map) {
            newValue = toSettableMapInternal((Map<Object, Object>) value);
        } else if (value instanceof ArrayList) {
            newValue = toSettableArrayInternal((ArrayList) value);
        }  else if (value instanceof JSONObject) {
            newValue = toSettableJSONInternal((JSONObject) value);
        }
        else {
            newValue = value;
        }

        return newValue;
    }

    private static Map<Object, Object> toSettableJSONInternal(JSONObject map) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<Object, Object> value = new Gson().fromJson(map.toString(), type);

        return toSettableMapInternal(value);
    }

    private static Map<Object, Object> toSettableMapInternal(Map<Object, Object> value) {

        for (Map.Entry<Object, Object> entry : value.entrySet()) {
            entry.setValue(fromJSON(entry.getValue()));
        }
        return value;
    }

    private static ArrayList toSettableArrayInternal(ArrayList array) {

        int i = 0;

        for (Object entryValue : array) {
            array.set(i,fromJSON(entryValue));

            i++;
        }
        return array;
    }
}
