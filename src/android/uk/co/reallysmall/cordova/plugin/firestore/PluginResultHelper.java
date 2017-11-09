package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    static PluginResult createPluginResult(QuerySnapshot value, boolean reusable) {
        JSONObject querySnapshot = new JSONObject();
        JSONArray array = new JSONArray();

        for (DocumentSnapshot doc : value) {
            JSONObject document = createDocumentSnapshot(doc);
            array.put(document);
        }

        try {
            querySnapshot.put("docs", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, querySnapshot);
        pluginResult.setKeepCallback(reusable);
        return pluginResult;
    }

    private static JSONObject createDocumentSnapshot(DocumentSnapshot doc) {
        JSONObject documentSnapshot = new JSONObject();

        try {
            documentSnapshot.put("id", doc.getId());
            documentSnapshot.put("exists", doc.exists());
            documentSnapshot.put("ref", doc.getReference().getId());

            documentSnapshot.put("_data", JSONHelper.toJSON(doc.getData()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return documentSnapshot;
    }
}
