package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransactionDocSetHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public TransactionDocSetHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String transactionId = args.getString(0);
            final String docId = args.getString(1);
            final String collectionPath = args.getString(2);
            final JSONObject data = args.getJSONObject(3);

            final JSONObject options;

            if (!args.isNull(4)) {
                options = args.getJSONObject(4);
            } else {
                options = null;
            }

            Log.d(FirestorePlugin.TAG, String.format("Transactional document set for %s", transactionId));

            TransactionQueue transactionQueue = firestorePlugin.getTransaction(transactionId);

            TransactionDetails transactionDetails = new TransactionDetails();
            transactionDetails.collectionPath = collectionPath;
            transactionDetails.docId = docId;
            transactionDetails.data = data;
            transactionDetails.options = options;
            transactionDetails.transactionOperationType = TransactionOperationType.SET;

            transactionQueue.queue.add(transactionDetails);
            callbackContext.success();

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing transactional document set", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
