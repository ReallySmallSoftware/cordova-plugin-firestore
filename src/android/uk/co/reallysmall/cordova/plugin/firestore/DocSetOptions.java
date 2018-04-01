package uk.co.reallysmall.cordova.plugin.firestore;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DocSetOptions  {

    public static SetOptions getSetOptions(JSONObject options) {
        SetOptions setOptions = null;

        try {
            if (options != null && options.getBoolean("merge")) {
                setOptions = SetOptions.merge();
            }
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error getting document option", e);
            throw new RuntimeException(e);
        }

        Log.d(FirestorePlugin.TAG, "Set document options");

        return setOptions;
    }
}
