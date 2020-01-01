package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.FieldValue;

import org.json.JSONArray;
import org.json.JSONObject;

public class FieldValueHelper {

    private static String fieldValueDelete = "__DELETE";
    private static String fieldValueServerTimestamp = "__SERVERTIMESTAMP";
    private static String fieldValueIncrement = "__INCREMENT";
    private static String fieldValueArrayRemove = "__ARRAYREMOVE";
    private static String fieldValueArrayUnion = "__ARRAYUNION";

    public static void setDeletePrefix(String fieldValueDelete) {
        FieldValueHelper.fieldValueDelete = fieldValueDelete;
    }

    public static void setServerTimestampPrefix(String fieldValueServerTimestamp) {
        FieldValueHelper.fieldValueServerTimestamp = fieldValueServerTimestamp;
    }

    public static void setIncrementPrefix(String fieldValueIncrement) {
        FieldValueHelper.fieldValueIncrement = fieldValueIncrement;
    }

    public static void setArrayRemovePrefix(String fieldValueArrayRemove) {
        FieldValueHelper.fieldValueArrayRemove = fieldValueArrayRemove;
    }

    public static void setArrayUnionPrefix(String fieldValueArrayUnion) {
        FieldValueHelper.fieldValueArrayUnion = fieldValueArrayUnion;
    }

    public static Object unwrapFieldValue(Object value) {
        String valueString = (String) value;

        if (fieldValueDelete.equals(valueString)) {
            return FieldValue.delete();
        }

        if (fieldValueServerTimestamp.equals(valueString)) {
            return FieldValue.serverTimestamp();
        }

        if (valueString.startsWith(fieldValueIncrement)) {
            return FieldValue.increment(Long.parseLong(FieldValueHelper.unwrap(valueString, fieldValueIncrement)));
        }

        if (valueString.startsWith(fieldValueArrayRemove)) {
            String unwrapped = FieldValueHelper.unwrap(valueString, fieldValueArrayRemove);

            return FieldValue.arrayRemove(JSONArrayToArray(unwrapped));
        }

        if (valueString.startsWith(fieldValueArrayUnion)) {
            String unwrapped = FieldValueHelper.unwrap(valueString, fieldValueArrayUnion);

            return FieldValue.arrayUnion(JSONArrayToArray(unwrapped));
        }

        return value;
    }

    private static Object[] JSONArrayToArray(String unwrapped) {

        try {
            JSONArray jsonArray = new JSONArray(unwrapped);

            if (jsonArray == null)
                return null;

            Object[] array = new Object[jsonArray.length()];
            for (int i = 0; i < array.length; i++) {
                array[i] = jsonArray.opt(i);
            }
            return array;
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean isWrappedFieldValue(Object value) {
        if (value instanceof String) {
            String valueString = (String) value;

            if (fieldValueDelete.equals(valueString)) {
                return true;
            }

            if (fieldValueServerTimestamp.equals(valueString)) {
                return true;
            }

            if (valueString.startsWith(fieldValueIncrement)) {
                return true;
            }

            if (valueString.startsWith(fieldValueArrayRemove)) {
                return true;
            }

            if (valueString.startsWith(fieldValueArrayUnion)) {
                return true;
            }
        }

        return false;
    }

    public static String unwrap(String valueString, String prefix) {
        int prefixLength = prefix.length();
        String ret = valueString.substring(prefixLength + 1);
        return ret;
    }
}
