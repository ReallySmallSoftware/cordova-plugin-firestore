package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PluginResultHelper {

     private static final Map<FirebaseFirestoreException.Code, String> errorCodeMap = initMap();

     private static Map<FirebaseFirestoreException.Code, String> initMap() {
         Map<FirebaseFirestoreException.Code, String> map = new HashMap<>();
         map.put(FirebaseFirestoreException.Code.ABORTED,"aborted");
         map.put(FirebaseFirestoreException.Code.ALREADY_EXISTS,"already-exists");
         map.put(FirebaseFirestoreException.Code.CANCELLED,"cancelled");
         map.put(FirebaseFirestoreException.Code.DATA_LOSS,"data-loss");
         map.put(FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,"deadline-exceeded");
         map.put(FirebaseFirestoreException.Code.FAILED_PRECONDITION,"failed-precondition");
         map.put(FirebaseFirestoreException.Code.INTERNAL,"internal");
         map.put(FirebaseFirestoreException.Code.INVALID_ARGUMENT,"invalid-argument");
         map.put(FirebaseFirestoreException.Code.NOT_FOUND,"not-found");
         map.put(FirebaseFirestoreException.Code.OK,"ok");
         map.put(FirebaseFirestoreException.Code.OUT_OF_RANGE,"out-of-range");
         map.put(FirebaseFirestoreException.Code.PERMISSION_DENIED,"permission-denied");
         map.put(FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,"resource-exhausted");
         map.put(FirebaseFirestoreException.Code.UNAUTHENTICATED,"unauthenticated");
         map.put(FirebaseFirestoreException.Code.UNAVAILABLE,"unavailable");
         map.put(FirebaseFirestoreException.Code.UNIMPLEMENTED,"unimplemented");
         map.put(FirebaseFirestoreException.Code.UNKNOWN,"unknown");

         return Collections.unmodifiableMap(map);
     }

    static PluginResult createPluginErrorResult(FirebaseFirestoreException e, boolean reusable) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
                createError(e.getCode().toString(),e.getMessage()));
        pluginResult.setKeepCallback(reusable);
        return pluginResult;
    }

    static PluginResult createPluginResult(DocumentSnapshot doc, boolean reusable) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, createDocumentSnapshot(doc));
        pluginResult.setKeepCallback(reusable);
        return pluginResult;
    }

    static PluginResult createPluginResult(DocumentReference doc, boolean reusable) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, createDocumentReference(doc));
        pluginResult.setKeepCallback(reusable);
        return pluginResult;
    }

    static PluginResult createPluginResult(QuerySnapshot value, boolean reusable) {
        JSONObject querySnapshot = new JSONObject();
        JSONArray array = new JSONArray();
        JSONArray changesArray = new JSONArray();

        FirestoreLog.d(FirestorePlugin.TAG, "Creating query snapshot result");

        for (QueryDocumentSnapshot doc : value) {
            JSONObject document = createDocumentSnapshot(doc);
            array.put(document);
        }

        for (DocumentChange change : value.getDocumentChanges()) {
            JSONObject changeObj = new JSONObject();
            try {
                changeObj.put("type", change.getType().toString());
                changeObj.put("doc", createDocumentSnapshot(change.getDocument()));
            } catch (JSONException e) {
                FirestoreLog.e(FirestorePlugin.TAG, "Error creating document change result", e);
                throw new RuntimeException(e);
            }
            changesArray.put(changeObj);
        }

        try {
            querySnapshot.put("docs", array);
            querySnapshot.put("docChanges", changesArray);  // add this line
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, querySnapshot);
        pluginResult.setKeepCallback(reusable);
        return pluginResult;
    }

    public static JSONObject createError(String code, String message) {
        JSONObject error = new JSONObject();

        FirestoreLog.d(FirestorePlugin.TAG, "Creating error result");

        try {
            error.put("code", errorCodeMap.get(code));
            error.put("message", message);

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error creating error result", e);
            throw new RuntimeException(e);
        }

        return error;
    }

    private static JSONObject createDocumentSnapshot(DocumentSnapshot doc) {
        JSONObject documentSnapshot = new JSONObject();

        FirestoreLog.d(FirestorePlugin.TAG, "Creating document snapshot result");

        try {
            documentSnapshot.put("id", doc.getId());
            documentSnapshot.put("exists", doc.exists());
            documentSnapshot.put("ref", doc.getReference().getId());

            if (doc.exists()) {
                documentSnapshot.put("_data", JSONHelper.toJSON(doc.getData()));
            }

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error creating document snapshot result", e);
            throw new RuntimeException(e);
        }

        return documentSnapshot;
    }

    private static JSONObject createDocumentReference(DocumentReference doc) {
        JSONObject documentReference = new JSONObject();

        FirestoreLog.e(FirestorePlugin.TAG, "Creating document snapshot result");

        try {
            documentReference.put("id", doc.getId());

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error creating document reference result", e);
            throw new RuntimeException(e);
        }

        return documentReference;
    }
}
