package uk.co.reallysmall.cordova.plugin.firestore;


import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransactionDocUpdateHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public TransactionDocUpdateHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String transactionId = args.getString(0);
            final String docId = args.getString(1);
            final String collectionPath = args.getString(2);
            final JSONObject data = args.getJSONObject(3);

            FirestoreLog.d(FirestorePlugin.TAG, String.format("Transactional document update for %s", transactionId));

            TransactionQueue transactionQueue = firestorePlugin.getTransaction(transactionId);

            TransactionDetails transactionDetails = new TransactionDetails();
            transactionDetails.collectionPath = collectionPath;
            transactionDetails.docId = docId;
            transactionDetails.data = data;
            transactionDetails.transactionOperationType = TransactionOperationType.UPDATE;

            transactionQueue.queue.add(transactionDetails);
            callbackContext.success();

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing transactional document update", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
