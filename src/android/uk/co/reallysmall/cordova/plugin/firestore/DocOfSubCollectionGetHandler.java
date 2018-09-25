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

public class DocOfSubCollectionGetHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public DocOfSubCollectionGetHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collectionPath = args.getString(0);
            final String docId = args.getString(1);
            final String subCollection = args.getString(2);
            final String docOfSubCollectionId = args.getString(3);

            FirestoreLog.d(FirestorePlugin.TAG, "Listening to document of sub collection");

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(docId).collection(subCollection).document(docOfSubCollectionId);

                documentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        callbackContext.sendPluginResult(createPluginResult(documentSnapshot, false));
                        FirestoreLog.d(FirestorePlugin.TAG, "Successfully got document of sub collection");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirestoreLog.w(FirestorePlugin.TAG, "Error getting document of sub collection", e);
                        callbackContext.error(e.getMessage());
                    }
                });
            } catch (Exception e) {
                FirestoreLog.e(FirestorePlugin.TAG, "Error processing document of sub collection get in thread", e);
                callbackContext.error(e.getMessage());
            }

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing document of sub collection snapshot", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}