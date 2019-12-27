package uk.co.reallysmall.cordova.plugin.firestore;


//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;

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
            final String docId = args.getString(1);
            final String docPath = collectionPath + "/" + docId;
            FirestoreLog.d(FirestorePlugin.TAG, "Getting document : " + docPath);

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(docId);

                documentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        callbackContext.sendPluginResult(createPluginResult(documentSnapshot, false));
                        FirestoreLog.d(FirestorePlugin.TAG, "Successfully got document " + docPath);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirestoreLog.w(FirestorePlugin.TAG, "Error getting document " + docPath, e);
                        callbackContext.error(e.getMessage());
                    }
                });
            } catch (Exception e) {
                FirestoreLog.e(FirestorePlugin.TAG, "Error processing document get " + docPath, e);
                callbackContext.error(e.getMessage());
            }

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing document snapshot ", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
