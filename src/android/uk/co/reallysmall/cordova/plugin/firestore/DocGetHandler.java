package uk.co.reallysmall.cordova.plugin.firestore;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import static uk.co.reallysmall.cordova.plugin.firestore.PluginResultHelper.createPluginResult;

public class DocGetHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public DocGetHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collectionPath = args.getString(0);
            final String doc = args.getString(1);


            Log.d(FirestorePlugin.TAG, "Listening to document");

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);

                documentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        callbackContext.sendPluginResult(createPluginResult(documentSnapshot, false));
                        Log.d(FirestorePlugin.TAG, "Successfully got document");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(FirestorePlugin.TAG, "Error getting document", e);
                        callbackContext.error(e.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing document get in thread", e);
                callbackContext.error(e.getMessage());
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing document snapshot", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
