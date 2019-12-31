package uk.co.reallysmall.cordova.plugin.firestore;

import androidx.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CollectionOnSnapshotHandler implements ActionHandler {
    private FirestorePlugin firestorePlugin;

    public CollectionOnSnapshotHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collection = args.getString(0);
            final JSONArray queries = args.getJSONArray(1);
            final JSONObject options = args.optJSONObject(2);
            final String callbackId = args.getString(3);


            FirestoreLog.d(FirestorePlugin.TAG, "Listening to collection " + collection);

            try {
                CollectionReference collectionRef = firestorePlugin.getDatabase().collection(collection);
                MetadataChanges metadataChanges = getMetadataChanges(options);

                Query query = QueryHelper.processQueries(queries, collectionRef, this.firestorePlugin);

                EventListener eventListener = new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            FirestoreLog.w(FirestorePlugin.TAG, "Collection snapshot listener error " + collection, e);
                            return;
                        }

                        FirestoreLog.d(FirestorePlugin.TAG, "Got collection snapshot data " + collection);
                        callbackContext.sendPluginResult(PluginResultHelper.createPluginResult(value, true));
                    }
                };

                firestorePlugin.addRegistration(callbackId, query.addSnapshotListener(metadataChanges, eventListener));

            } catch (Exception e) {
                FirestoreLog.e(FirestorePlugin.TAG, "Error processing collection snapshot in thread " + collection, e);
                callbackContext.error(e.getMessage());
            }

        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing collection snapshot", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }

    private MetadataChanges getMetadataChanges(JSONObject options) {
        MetadataChanges metadataChanges = MetadataChanges.EXCLUDE;

        if (options != null) {

            try {
                if (options.getBoolean("includeDocumentMetadataChanges")) {
                    metadataChanges = MetadataChanges.INCLUDE;
                }
                if (options.getBoolean("includeQueryMetadataChanges")) {
                    metadataChanges = MetadataChanges.INCLUDE;
                }
                if (options.getBoolean("includeMetadataChanges")) {
                    metadataChanges = MetadataChanges.INCLUDE;
                }
            } catch (JSONException e) {
                FirestoreLog.e(FirestorePlugin.TAG, "Error getting query options", e);
                throw new RuntimeException(e);
            }

            FirestoreLog.d(FirestorePlugin.TAG, "Set document options");
        }

        return metadataChanges;
    }
}
