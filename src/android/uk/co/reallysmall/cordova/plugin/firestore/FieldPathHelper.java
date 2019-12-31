package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.FieldPath;

public class FieldPathHelper {

    private static String fieldPathDocumentId = "__DOCUMENTID";

    public static void setDocumentIdePrefix(String fieldPathDocumentId) {
        FieldPathHelper.fieldPathDocumentId = fieldPathDocumentId;
    }

    public static boolean isWrapped(String value) {
        if (fieldPathDocumentId.equals(value)) {
            return true;
        }
        return false;
    }

    public static FieldPath unwrapFieldPath(Object value) {
            return FieldPath.documentId();
    }
}
