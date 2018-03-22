package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.FieldValue;

public class FieldValueHelper {

    private static String fieldValueDelete = "__DELETE";
    private static String fieldValueServerTimestamp = "__SERVERTIMESTAMP";

    public static void setDeletePrefix(String fieldValueDelete) {
        FieldValueHelper.fieldValueDelete = fieldValueDelete;
    }

    public static void setServerTimestampPrefix(String fieldValueServerTimestamp) {
        FieldValueHelper.fieldValueServerTimestamp = fieldValueServerTimestamp;
    }

    public static Object unwrapFieldValue(Object value) {
        String valueString = (String)value;

        if (fieldValueDelete.equals(valueString)) {
            return FieldValue.delete();
        }

        if (fieldValueServerTimestamp.equals(valueString)) {
            return FieldValue.serverTimestamp();
        }

        return value;
    }

    public static boolean isWrappedFieldValue(Object value) {
        if (value instanceof String) {
            String valueString = (String)value;

            if (fieldValueDelete.equals(valueString)) {
                return true;
            }

            if (fieldValueServerTimestamp.equals(valueString)) {
                return true;
            }
        }

        return false;
    }
}
