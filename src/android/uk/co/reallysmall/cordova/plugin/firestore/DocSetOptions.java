package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class DocSetOptions {

    public static SetOptions getSetOptions(JSONObject options) {
        SetOptions setOptions = null;

        try {
            if (options != null && options.getBoolean("merge")) {
                setOptions = SetOptions.merge();
            }
        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error getting document option", e);
            throw new RuntimeException(e);
        }

        FirestoreLog.d(FirestorePlugin.TAG, "Set document options");

        return setOptions;
    }
}
