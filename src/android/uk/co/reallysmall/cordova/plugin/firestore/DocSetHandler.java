package uk.co.reallysmall.cordova.plugin.firestore;

//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;

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
            final String docPath = collection + "/" + docId;
            final JSONObject data = args.getJSONObject(2);

            final JSONObject options;

            if (!args.isNull(3)) {
                options = args.getJSONObject(3);
            } else {
                options = null;
            }

            try {

                SetOptions setOptions = DocSetOptions.getSetOptions(options);

                FirestoreLog.d(FirestorePlugin.TAG, "Setting document " + docPath);

                DocumentReference documentReference = firestorePlugin.getDatabase().collection(collection).document(docId);

                OnSuccessListener onSuccessListener = new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callbackContext.success();
                        FirestoreLog.d(FirestorePlugin.TAG, "Successfully written document " + docPath);
                    }
                };

                OnFailureListener onFailureListener = new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirestoreLog.w(FirestorePlugin.TAG, "Error writing document " + docPath, e);
                        callbackContext.error(e.getMessage());
                    }
                };

                if (setOptions == null) {
                    documentReference.set(JSONHelper.fromJSON(data)).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                } else {
                    documentReference.set(JSONHelper.fromJSON(data), setOptions).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                }
            } catch (Exception e) {
                FirestoreLog.e(FirestorePlugin.TAG, "Error processing document set " + docPath, e);
                callbackContext.error(e.getMessage());
            }
        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing document set", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
