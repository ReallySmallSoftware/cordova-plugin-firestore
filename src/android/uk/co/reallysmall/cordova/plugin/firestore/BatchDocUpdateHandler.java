package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class BatchDocUpdateHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public BatchDocUpdateHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String batchId = args.getString(0);
            final String docId = args.getString(1);
            final String collectionPath = args.getString(2);
            final JSONObject data = args.getJSONObject(3);

            FirestoreLog.d(FirestorePlugin.TAG, String.format("Batch document set for %s", batchId));

            DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(docId);

            WriteBatch batch = this.firestorePlugin.getBatch(batchId);

            batch.update(documentRef, (Map<String,Object>)JSONHelper.fromJSON(data));

            callbackContext.success();

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing batch document update", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
