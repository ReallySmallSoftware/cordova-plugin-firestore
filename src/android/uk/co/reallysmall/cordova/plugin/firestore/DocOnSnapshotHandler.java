package uk.co.reallysmall.cordova.plugin.firestore;


import android.support.annotation.Nullable;
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
            final String doc = args.getString(1);
            final String callbackId = args.getString(2);

            final JSONObject options;

            if (args.length() > 3) {
                options = args.getJSONObject(3);
            } else {
                options = null;
            }

            Log.d(FirestorePlugin.TAG, "Listening to document");

            DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);
            MetadataChanges metadataChanges = getMetadataChanges(options);
            Log.d(FirestorePlugin.TAG, "SS for document " + collectionPath + "/" + doc);

            EventListener eventListener = new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(FirestorePlugin.TAG, "Document snapshot listener error", e);
                        return;
                    }

                    Log.d(FirestorePlugin.TAG, "Got document snapshot data");
                    callbackContext.sendPluginResult(PluginResultHelper.createPluginResult(documentSnapshot, true));
                }
            };

            firestorePlugin.addRegistration(callbackId, documentRef.addSnapshotListener(metadataChanges, eventListener));

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing document snapshot", e);
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
                Log.e(FirestorePlugin.TAG, "Error getting document option includeMetadataChanges", e);
                throw new RuntimeException(e);
            }

            Log.d(FirestorePlugin.TAG, "Set document options");
        }

        return metadataChanges;
    }
}
