package uk.co.reallysmall.cordova.plugin.firestore;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryListenOptions;
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

            firestorePlugin.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {

                    Log.d(FirestorePlugin.TAG, "Listening to collection");

                    try {
                        CollectionReference collectionRef = firestorePlugin.getDatabase().collection(collection);
                        QueryListenOptions queryListenOptions = getQueryListenOptions(options);

                        Query query = QueryHelper.processQueries(queries, collectionRef);

                        EventListener eventListener = new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value,
                                                @Nullable FirebaseFirestoreException e) {

                                if (e != null) {
                                    Log.w(FirestorePlugin.TAG, "Collection snapshot listener error", e);
                                    return;
                                }

                                Log.d(FirestorePlugin.TAG, "Got collection snapshot data");
                                callbackContext.sendPluginResult(PluginResultHelper.createPluginResult(value, true));
                            }
                        };

                        if (queryListenOptions == null) {
                            query.addSnapshotListener(eventListener);
                        } else {
                            query.addSnapshotListener(queryListenOptions, eventListener);
                        }

                    } catch (Exception e) {
                        Log.e(FirestorePlugin.TAG, "Error processing collection snapshot in thread", e);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing collection snapshot", e);
        }

        return true;
    }

    private QueryListenOptions getQueryListenOptions(JSONObject options) {
        QueryListenOptions queryListenOptions = null;

        if (options != null) {
            queryListenOptions = new QueryListenOptions();

            try {
                if (options.getBoolean("includeDocumentMetadataChanges")) {
                    queryListenOptions.includeDocumentMetadataChanges();
                }
                if (options.getBoolean("includeQueryMetadataChanges")) {
                    queryListenOptions.includeQueryMetadataChanges();
                }
            } catch (JSONException e) {
                Log.e(FirestorePlugin.TAG, "Error getting query options", e);
            }

            Log.d(FirestorePlugin.TAG, "Set document options");
        }

        return queryListenOptions;
    }
}
