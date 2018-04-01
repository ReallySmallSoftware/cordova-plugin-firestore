package uk.co.reallysmall.cordova.plugin.firestore;


import android.util.Log;

import com.google.firebase.firestore.DocumentReference;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static uk.co.reallysmall.cordova.plugin.firestore.PluginResultHelper.createPluginResult;

public class TransactionDocUpdateHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public TransactionDocUpdateHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String transactionId = args.getString(0);
            final String doc = args.getString(1);
            final String collectionPath = args.getString(2);
            final JSONObject data = args.getJSONObject(3);

            Log.d(FirestorePlugin.TAG, "Transactional document update");

            TransactionWrapper transactionWrapper = firestorePlugin.getTransaction(transactionId);

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);

                Log.d(FirestorePlugin.TAG, "Transactional update for document " + collectionPath + "/" + doc);

                transactionWrapper.transaction.update(documentRef, JSONHelper.toSettableMap(data));
                callbackContext.success();

                Log.d(FirestorePlugin.TAG, "Successfully updated document transactionally");

            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing transactional document update in thread", e);
                callbackContext.error(e.getMessage());
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing transactional document update", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
