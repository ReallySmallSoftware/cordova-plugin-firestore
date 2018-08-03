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

public class DocOfSubCollectionSetHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public DocOfSubCollectionSetHandler(FirestorePlugin firestorePlugin) {
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

            final JSONObject options;

            if (!args.isNull(5)) {
                options = args.getJSONObject(5);
            } else {
                options = null;
            }

            try {

                SetOptions setOptions = DocSetOptions.getSetOptions(options);

                Log.d(FirestorePlugin.TAG, "Setting document of sub collection");

                DocumentReference documentReference = firestorePlugin.getDatabase().collection(collection).document(docId).collection(subCollection).document(docOfSubCollectionId);

                OnSuccessListener onSuccessListener = new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callbackContext.success();
                        Log.d(FirestorePlugin.TAG, "Successfully written document of sub collection");
                    }
                };

                OnFailureListener onFailureListener = new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(FirestorePlugin.TAG, "Error writing document of sub collection", e);
                        callbackContext.error(e.getMessage());
                    }
                };

                if (setOptions == null) {
                    documentReference.set(JSONHelper.toSettableMap(data)).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                } else {
                    documentReference.set(JSONHelper.toSettableMap(data), setOptions).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                }
            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing document of sub collection set in thread", e);
                callbackContext.error(e.getMessage());
            }
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing document of sub collection set", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
