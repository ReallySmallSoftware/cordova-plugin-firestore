package uk.co.reallysmall.cordova.plugin.firestore;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Transaction;

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

            Log.d(FirestorePlugin.TAG, "Transactional document get");

            TransactionWrapper transactionWrapper = firestorePlugin.getTransaction(transactionId);

            try {
                DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);

                Log.d(FirestorePlugin.TAG, "Transactional get for document " + collectionPath + "/" + doc);

                callbackContext.sendPluginResult(createPluginResult(transactionWrapper.transaction.get(documentRef), false));
                Log.d(FirestorePlugin.TAG, "Successfully got document transactionally");

            } catch (Exception ex) {
                Log.e(FirestorePlugin.TAG, "Error processing transactional document get in thread", ex);
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing transactional document snapshot", e);
        }

        return true;
    }
}
