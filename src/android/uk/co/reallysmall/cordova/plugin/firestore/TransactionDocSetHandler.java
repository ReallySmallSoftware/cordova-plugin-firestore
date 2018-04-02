package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by richard on 23/03/18.
 */

public class TransactionDocSetHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public TransactionDocSetHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String transactionId = args.getString(0);
            final String doc = args.getString(1);
            final String collectionPath = args.getString(2);
            final JSONObject data = args.getJSONObject(3);

            final JSONObject options;

            if (!args.isNull(4)) {
                options = args.getJSONObject(4);
            } else {
                options = null;
            }

            Log.d(FirestorePlugin.TAG, String.format("Transactional document set for %s", transactionId));

            SetOptions setOptions = DocSetOptions.getSetOptions(options);

            TransactionWrapper transactionWrapper = firestorePlugin.getTransaction();

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);

                Log.d(FirestorePlugin.TAG, String.format("Transactional %s set for document %s", transactionId, collectionPath + "/" + doc));

                if (setOptions == null) {
                    transactionWrapper.transaction.set(documentRef, JSONHelper.toSettableMap(data));
                } else {
                    transactionWrapper.transaction.set(documentRef, JSONHelper.toSettableMap(data), setOptions);
                }
                callbackContext.success();

            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing transactional document set in thread", e);
                callbackContext.error(e.getMessage());
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing transactional document set", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}