package uk.co.reallysmall.cordova.plugin.firestore;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.SetOptions;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DocSetHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public DocSetHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collection = args.getString(0);
            final String docId = args.getString(1);
            final JSONObject data = args.getJSONObject(2);
            final JSONObject options = args.getJSONObject(3);

            firestorePlugin.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        SetOptions setOptions = getSetOptions(options);

                        Log.d(FirestorePlugin.TAG, "Setting document");

                        firestorePlugin.getDatabase().collection(collection).document(docId).set(data, setOptions).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                callbackContext.success();
                                Log.d(FirestorePlugin.TAG, "Successfully written document");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(FirestorePlugin.TAG, "Error writing document", e);
                                callbackContext.error(e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        Log.e(FirestorePlugin.TAG, "Error processing document set in thread", e);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing document set", e);
        }

        return true;
    }

    private SetOptions getSetOptions(JSONObject options) {
        SetOptions setOptions = null;

        if (options != null) {

            try {
                if (options.getBoolean("merge")) {
                    setOptions.merge();
                }
            } catch (JSONException e) {
                Log.e(FirestorePlugin.TAG, "Error getting document option", e);
            }

            Log.d(FirestorePlugin.TAG, "Set document options");
        }

        return setOptions;
    }
}
