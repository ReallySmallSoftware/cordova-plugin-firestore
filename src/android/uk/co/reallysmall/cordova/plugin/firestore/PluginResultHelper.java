package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PluginResultHelper {
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

        FirestoreLog.d(FirestorePlugin.TAG, "Creating query snapshot result");

        for (QueryDocumentSnapshot doc : value) {
            JSONObject document = createDocumentSnapshot(doc);
            array.put(document);
        }

        try {
            querySnapshot.put("docs", array);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, querySnapshot);
        pluginResult.setKeepCallback(reusable);
        return pluginResult;
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
