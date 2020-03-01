package uk.co.reallysmall.cordova.plugin.firestore;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.WriteBatch;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class BatchCommitHandler implements ActionHandler {
    private final FirestorePlugin firestorePlugin;

    public BatchCommitHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String batchId = args.getString(0);

            FirestoreLog.d(FirestorePlugin.TAG, String.format("Batch commit for %s", batchId));

            WriteBatch batch = this.firestorePlugin.getBatch(batchId);

            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    firestorePlugin.removeBatch((batchId));

                    if (task.isSuccessful()) {
                        callbackContext.success();
                    } else {
                        callbackContext.error(task.getException().getMessage());
                    }
                }
            });

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing batch commit", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
