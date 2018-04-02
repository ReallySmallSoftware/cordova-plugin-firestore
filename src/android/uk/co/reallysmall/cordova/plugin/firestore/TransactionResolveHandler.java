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

            Log.d(FirestorePlugin.TAG, String.format("Transactional resolve for %s", transactionId));

            TransactionWrapper transactionWrapper = firestorePlugin.getTransaction();
            transactionWrapper.sync.append(result);

            synchronized (transactionWrapper.sync) {
                transactionWrapper.sync.notify();
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error resolving transaction", e);
            callbackContext.error(e.getMessage());
        }

        return false;
    }
}
