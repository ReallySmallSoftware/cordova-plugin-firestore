package uk.co.reallysmall.cordova.plugin.firestore;


import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.DocumentListenOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

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


            Log.d(FirestorePlugin.TAG, "Listening to document " + collectionPath + " " + doc);

            DocumentReference documentRef = firestorePlugin.getDatabase().collection(collectionPath).document(doc);
            DocumentListenOptions documentListenOptions = getDocumentListenOptions(options);
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

            if (documentListenOptions == null) {
                firestorePlugin.addRegistration(callbackId, documentRef.addSnapshotListener(eventListener));
            } else {
                firestorePlugin.addRegistration(callbackId, documentRef.addSnapshotListener(documentListenOptions, eventListener));
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing document snapshot", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }

    private DocumentListenOptions getDocumentListenOptions(JSONObject options) {
        DocumentListenOptions documentListenOptions = null;

        if (options != null) {
            documentListenOptions = new DocumentListenOptions();

            try {
                if (options.getBoolean("includeMetadataChanges")) {
                    documentListenOptions.includeMetadataChanges();
                }
            } catch (JSONException e) {
                Log.e(FirestorePlugin.TAG, "Error getting document option includeMetadataChanges", e);
                throw new RuntimeException(e);
            }

            Log.d(FirestorePlugin.TAG, "Set document options");
        }

        return documentListenOptions;
    }
}
