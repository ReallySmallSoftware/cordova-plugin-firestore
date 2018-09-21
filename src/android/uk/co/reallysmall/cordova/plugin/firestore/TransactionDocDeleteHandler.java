package uk.co.reallysmall.cordova.plugin.firestore;


import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class TransactionDocDeleteHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public TransactionDocDeleteHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String transactionId = args.getString(0);
            final String docId = args.getString(1);
            final String collectionPath = args.getString(2);

            FirestoreLog.d(FirestorePlugin.TAG, String.format("Transactional document delete for %s", transactionId));

            TransactionQueue transactionQueue = firestorePlugin.getTransaction(transactionId);

            TransactionDetails transactionDetails = new TransactionDetails();
            transactionDetails.collectionPath = collectionPath;
            transactionDetails.docId = docId;
            transactionDetails.transactionOperationType = TransactionOperationType.DELETE;

            transactionQueue.queue.add(transactionDetails);
            callbackContext.success();

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing transactional document delete", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
