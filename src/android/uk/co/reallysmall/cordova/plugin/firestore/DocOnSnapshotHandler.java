package uk.co.reallysmall.cordova.plugin.firestore;


import androidx.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DocOnSnapshotHandler implements ActionHandler {
    private FirestorePlugin firestorePlugin;

    public DocOnSnapshotHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collectionPath = args.getString(0);
            final String docId = args.getString(1);
            final String docPath = collectionPath + "/" + docId;
            final String callbackId = args.getString(2);

            final JSONObject options;

            if (args.length() > 3) {
                options = args.getJSONObject(3);
            } else {
                options = null;
            }


            DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(docId);
            MetadataChanges metadataChanges = getMetadataChanges(options);

            FirestoreLog.d(FirestorePlugin.TAG, "Launching onSnapshot handler for document " + docPath);

            EventListener eventListener = new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        FirestoreLog.w(FirestorePlugin.TAG, "Document snapshot listener error", e);
                        return;
                    }

                    FirestoreLog.d(FirestorePlugin.TAG, "Got document snapshot data");
                    callbackContext.sendPluginResult(PluginResultHelper.createPluginResult(documentSnapshot, true));
                }
            };

            firestorePlugin.addRegistration(callbackId, documentRef.addSnapshotListener(metadataChanges, eventListener));

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing document snapshot", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }

    private MetadataChanges getMetadataChanges(JSONObject options) {
        MetadataChanges metadataChanges = MetadataChanges.EXCLUDE;

        if (options != null) {

            try {
                if (options.getBoolean("includeMetadataChanges")) {
                    metadataChanges = MetadataChanges.INCLUDE;
                }
            } catch (JSONException e) {
                FirestoreLog.e(FirestorePlugin.TAG, "Error getting document option includeMetadataChanges", e);
                throw new RuntimeException(e);
            }

            FirestoreLog.d(FirestorePlugin.TAG, "Set document options");
        }

        return metadataChanges;
    }
}
