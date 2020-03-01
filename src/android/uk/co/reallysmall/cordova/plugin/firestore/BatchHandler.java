package uk.co.reallysmall.cordova.plugin.firestore;


import com.google.firebase.firestore.WriteBatch;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class BatchHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public BatchHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String batchId = args.getString(0);

            FirestoreLog.d(FirestorePlugin.TAG, String.format("Batch %s", batchId));

            WriteBatch batch = this.firestorePlugin.getDatabase().batch();
            this.firestorePlugin.storeBatch(batchId,batch);

            callbackContext.success();

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error creating batch", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
