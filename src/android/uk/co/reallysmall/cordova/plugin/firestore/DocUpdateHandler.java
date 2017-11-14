package uk.co.reallysmall.cordova.plugin.firestore;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DocUpdateHandler implements ActionHandler {
    private FirestorePlugin firestorePlugin;

    public DocUpdateHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collection = args.getString(0);
            final String docId = args.getString(1);
            final JSONObject data = args.getJSONObject(2);


            Log.d(FirestorePlugin.TAG, "Updating document");

            try {
                firestorePlugin.getDatabase().collection(collection).document(docId).update(JSONHelper.toSettableMap(data)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callbackContext.success();
                        Log.d(FirestorePlugin.TAG, "Successfully updated document");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callbackContext.error(e.getMessage());
                        Log.w(FirestorePlugin.TAG, "Error updating document", e);
                    }
                });
            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing document update in thread", e);
            }
            ;
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing document update", e);
        }

        return true;
    }
}
