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

            Log.d(FirestorePlugin.TAG, "Transactional document delete");

            TransactionWrapper transactionWrapper = firestorePlugin.getTransaction(transactionId);

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);

                Log.d(FirestorePlugin.TAG, "Transactional delete for document " + collectionPath + "/" + doc);

                transactionWrapper.transaction.delete(documentRef);
                callbackContext.success();

                Log.d(FirestorePlugin.TAG, "Successfully deleted document transactionally");

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
