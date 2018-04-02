package uk.co.reallysmall.cordova.plugin.firestore;


import android.util.Log;

import com.google.firebase.firestore.DocumentReference;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransactionDocDeleteHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public TransactionDocDeleteHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String transactionId = args.getString(0);
            final String doc = args.getString(1);
            final String collectionPath = args.getString(2);

            Log.d(FirestorePlugin.TAG, String.format("Transactional document delete for %s", transactionId));

            TransactionWrapper transactionWrapper = firestorePlugin.getTransaction();

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);

                Log.d(FirestorePlugin.TAG, String.format("Transactional %s delete for document %s", transactionId, collectionPath + "/" + doc));

                transactionWrapper.transaction.delete(documentRef);
                callbackContext.success();

            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing transactional document delete in thread", e);
                callbackContext.error(e.getMessage());
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing transactional document delete", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
