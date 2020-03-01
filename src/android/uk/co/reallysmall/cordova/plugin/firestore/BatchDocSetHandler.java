package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BatchDocSetHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public BatchDocSetHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String batchId = args.getString(0);
            final String docId = args.getString(1);
            final String collectionPath = args.getString(2);
            final JSONObject data = args.getJSONObject(3);

            final JSONObject options;

            if (!args.isNull(4)) {
                options = args.getJSONObject(4);
            } else {
                options = null;
            }

            FirestoreLog.d(FirestorePlugin.TAG, String.format("Batch document set for %s", batchId));

            SetOptions setOptions = DocSetOptions.getSetOptions(options);

            DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(docId);

            WriteBatch batch = this.firestorePlugin.getBatch(batchId);

            if (setOptions == null) {
                batch.set(documentRef, JSONHelper.fromJSON(data));
            } else {
                batch.set(documentRef, JSONHelper.fromJSON(data), setOptions);
            }

            callbackContext.success();

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing batch document set", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
