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

            final JSONObject options;

            if (!args.isNull(3)) {
                options = args.getJSONObject(3);
            } else {
                options = null;
            }

            try {

                SetOptions setOptions = DocSetOptions.getSetOptions(options);

                Log.d(FirestorePlugin.TAG, "Setting document");

                DocumentReference documentReference = firestorePlugin.getDatabase().collection(collection).document(docId);

                OnSuccessListener onSuccessListener = new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callbackContext.success();
                        Log.d(FirestorePlugin.TAG, "Successfully written document");
                    }
                };

                OnFailureListener onFailureListener = new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(FirestorePlugin.TAG, "Error writing document", e);
                        callbackContext.error(e.getMessage());
                    }
                };

                if (setOptions == null) {
                    documentReference.set(JSONHelper.toSettableMap(data)).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                } else {
                    documentReference.set(JSONHelper.toSettableMap(data), setOptions).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                }
            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing document set in thread", e);
            }


        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing document set", e);
        }

        return true;
    }
}
