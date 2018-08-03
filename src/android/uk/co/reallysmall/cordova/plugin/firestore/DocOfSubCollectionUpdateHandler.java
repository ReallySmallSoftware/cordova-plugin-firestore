package uk.co.reallysmall.cordova.plugin.firestore;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DocOfSubCollectionUpdateHandler implements ActionHandler {
    private FirestorePlugin firestorePlugin;

    public DocOfSubCollectionUpdateHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collection = args.getString(0);
            final String docId = args.getString(1);
            final String subCollection = args.getString(2);
            final String docOfSubCollectionId = args.getString(3);
            final JSONObject data = args.getJSONObject(4);

            Log.d(FirestorePlugin.TAG, "Updating document of sub collection");

            try {
                firestorePlugin.getDatabase().collection(collection).document(docId).collection(subCollection).document(docOfSubCollectionId).update(JSONHelper.toSettableMap(data)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callbackContext.success();
                        Log.d(FirestorePlugin.TAG, "Successfully updated document of sub collection");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callbackContext.error(e.getMessage());
                        Log.w(FirestorePlugin.TAG, "Error updating document of sub collection", e);
                    }
                });
            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing document of sub collection update in thread", e);
                callbackContext.error(e.getMessage());
            }
            ;
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing document of sub collection update", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
