package uk.co.reallysmall.cordova.plugin.firestore;


import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import static uk.co.reallysmall.cordova.plugin.firestore.PluginResultHelper.createPluginResult;

public class TransactionDocGetHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public TransactionDocGetHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String transactionId = args.getString(0);
            final String doc = args.getString(1);
            final String collectionPath = args.getString(2);

            FirestoreLog.d(FirestorePlugin.TAG, String.format("Transactional document get for %s", transactionId));

            TransactionQueue transactionQueue = firestorePlugin.getTransaction(transactionId);

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);

                callbackContext.sendPluginResult(createPluginResult(transactionQueue.transaction.get(documentRef), false));

            } catch (Exception e) {
                FirestoreLog.e(FirestorePlugin.TAG, "Error processing transactional document get in thread", e);
                String errorCode = ((FirebaseFirestoreException) e).getCode().name();
                callbackContext.error(PluginResultHelper.createError(errorCode, e.getMessage()));
            }

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing transactional document snapshot", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
