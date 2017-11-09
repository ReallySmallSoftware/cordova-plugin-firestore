package uk.co.reallysmall.cordova.plugin.firestore;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryListenOptions;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class CollectionOnSnapshotHandler implements ActionHandler {
    private FirestorePlugin firestorePlugin;

    public CollectionOnSnapshotHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collection = args.getString(0);
            final JSONArray whereArray = args.getJSONArray(1);
            final JSONArray orderArray = args.getJSONArray(2);
            final long limit = args.getLong(3);
            final JSONObject options = args.optJSONObject(4);

            firestorePlugin.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {

                    Log.d(FirestorePlugin.TAG, "Listening to collection");

                    try {
                        CollectionReference collectionRef = firestorePlugin.getDatabase().collection(collection);
                        QueryListenOptions queryListenOptions= getQueryListenOptions(options);

                        processWhere(collectionRef,whereArray);
                        processOrder(collectionRef,orderArray);

                        if (limit >= 0) {
                            collectionRef.limit(limit);
                        }

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
                            collectionRef.addSnapshotListener(eventListener);
                        } else {
                            collectionRef.addSnapshotListener(queryListenOptions, eventListener);
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

    private void processWhere(CollectionReference collectionRef, JSONArray whereArray) {
        for (int i = 0, n = whereArray.length(); i < n; ++i) {
            try {
                JSONObject where = whereArray.getJSONObject(i);
                String fieldPath = where.getString("fieldPath");
                String opStr = where.getString("opStr");
                Object value = parseWhereValue(where);

                if ("==".equals(opStr)) {
                collectionRef.whereEqualTo(fieldPath, value);
                } else if (">".equals(opStr)) {
                    collectionRef.whereGreaterThan(fieldPath, value);
                } else if (">=".equals(opStr)) {
                    collectionRef.whereGreaterThanOrEqualTo(fieldPath, value);
                } else if ("<".equals(opStr)) {
                    collectionRef.whereLessThan(fieldPath, value);
                } else if ("<=".equals(opStr)) {
                    collectionRef.whereLessThanOrEqualTo(fieldPath, value);
                } else {
                    throw new RuntimeException("Unknown operator type " + opStr);
                }

            } catch (JSONException e) {
                Log.e(FirestorePlugin.TAG, "Error processing collection snapshot where", e);
            }
        }
    }

    private Object parseWhereValue(JSONObject where) throws JSONException {
        Object value = where.get("value");

        if (value instanceof String && ((String) value).startsWith("__DATE(")) {
            String stringValue = (String)value;
            String timestamp = stringValue.substring(7).substring(0, stringValue.length() - 8);

            return new Date(Integer.parseInt(timestamp));
        }

        return value;
    }

    private void processOrder(CollectionReference collectionRef, JSONArray orderArray) {
        for (int i = 0, n = orderArray.length(); i < n; ++i) {
            JSONObject order = null;
            try {
                order = orderArray.getJSONObject(i);
                Query.Direction direction = Query.Direction.valueOf(order.getString("direction"));
                collectionRef.orderBy(order.getString("field"), direction);

                Log.d(FirestorePlugin.TAG, "Order by " + order.getString("field") + " (" + direction.toString() + ")");

            } catch (JSONException e) {
                Log.e(FirestorePlugin.TAG, "Error processing collection snapshot ordering", e);
            }

        }
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
