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

            Log.d(FirestorePlugin.TAG, "Writing document to collection");

            try {
                firestorePlugin.getDatabase().collection(collectionPath).add(JSONHelper.toSettableMap(data)).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        callbackContext.sendPluginResult(PluginResultHelper.createPluginResult(documentReference, false));

                        Log.d(FirestorePlugin.TAG, "Successfully written document to collection");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(FirestorePlugin.TAG, "Error writing document to collection", e);
                        callbackContext.error(e.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing collection add in thread", e);
                callbackContext.error(e.getMessage());
            }
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing collection add", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
