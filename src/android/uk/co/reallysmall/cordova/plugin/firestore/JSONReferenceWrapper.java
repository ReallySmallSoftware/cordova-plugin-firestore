package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class JSONReferenceWrapper {

    private static String referencePrefix = "__REFERENCE:";
    private  DocumentReference documentReference;

    public JSONReferenceWrapper(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    public static void setReferencePrefix(String referencePrefix) {
        JSONReferenceWrapper.referencePrefix = referencePrefix;
    }

    public static boolean isWrappedReference(Object value) {


        if (value instanceof String && ((String) value).startsWith(referencePrefix)) {
            return true;
        }

        return false;
    }

    public static DocumentReference unwrapReference(FirestorePlugin firestorePlugin, Object value) {
        String stringValue = (String) value;
        int prefixLength = referencePrefix.length();
        String reference = stringValue.substring(prefixLength).substring(0, stringValue.length() - prefixLength);
        return firestorePlugin.getDatabase().document(reference);
    }

    @Override
    public String toString() {
        return this.referencePrefix + this.documentReference.getPath();
    }
}
