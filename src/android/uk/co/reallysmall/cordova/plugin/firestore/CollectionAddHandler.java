package uk.co.reallysmall.cordova.plugin.firestore;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CollectionAddHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public CollectionAddHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collectionPath = args.getString(0);
            final JSONObject data = args.getJSONObject(1);

            firestorePlugin.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {

                    Log.d(FirestorePlugin.TAG, "Writing document to collection");

                    try {
                        firestorePlugin.getDatabase().collection(collectionPath).add(JSONHelper.jsonToMap(data)).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                callbackContext.success(documentReference.getId());
                                Log.d(FirestorePlugin.TAG, "Successfully written document to collection");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(FirestorePlugin.TAG, "Error writing document to collection", e);
                                callbackContext.error(e.getMessage());
                            }
                        });
                    } catch (Exception ex) {
                        Log.e(FirestorePlugin.TAG, "Error processing collection add in thread", ex);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing collection add", e);
        }

        return true;
    }
}
