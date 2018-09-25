package uk.co.reallysmall.cordova.plugin.firestore;


import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class TransactionResolveHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public TransactionResolveHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, CallbackContext callbackContext) {

        try {
            String transactionId = args.getString(0);
            String result = args.getString(1);

            FirestoreLog.d(FirestorePlugin.TAG, String.format("Transactional resolve for %s", transactionId));

            TransactionQueue transactionQueue = firestorePlugin.getTransaction(transactionId);
            transactionQueue.results.append(result);

            TransactionDetails transactionDetails = new TransactionDetails();
            transactionDetails.transactionOperationType = TransactionOperationType.RESOLVE;

            transactionQueue.queue.add(transactionDetails);

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error resolving transaction", e);
            callbackContext.error(e.getMessage());
        }

        return false;
    }
}
