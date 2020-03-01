package uk.co.reallysmall.cordova.plugin.firestore;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class BatchDocDeleteHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public BatchDocDeleteHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String batchId = args.getString(0);
            final String docId = args.getString(1);
            final String collectionPath = args.getString(2);

            FirestoreLog.d(FirestorePlugin.TAG, String.format("Batch document delete for %s", batchId));

            DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(docId);

            WriteBatch batch = this.firestorePlugin.getBatch(batchId);
            batch.delete(documentRef);

            callbackContext.success();

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing batch document delete", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
