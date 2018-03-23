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

            Log.d(FirestorePlugin.TAG, "Transactional document set");

            SetOptions setOptions = DocSetOptions.getSetOptions(options);

            TransactionWrapper transactionWrapper = firestorePlugin.getTransaction(transactionId);

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);

                Log.d(FirestorePlugin.TAG, "Transactional set for document " + collectionPath + "/" + doc);

                if (setOptions == null) {
                    transactionWrapper.transaction.set(documentRef, JSONHelper.toSettableMap(data));
                } else {
                    transactionWrapper.transaction.set(documentRef, JSONHelper.toSettableMap(data), setOptions);
                }
                callbackContext.success();

                Log.d(FirestorePlugin.TAG, "Successfully set document transactionally");

            } catch (Exception ex) {
                Log.e(FirestorePlugin.TAG, "Error processing transactional document set in thread", ex);
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing transactional document set", e);
        }

        return true;
    }
}