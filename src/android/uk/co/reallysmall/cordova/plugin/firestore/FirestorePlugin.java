/**
 */
package uk.co.reallysmall.cordova.plugin.firestore;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SnapshotMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FirestorePlugin extends CordovaPlugin {
    private static final String TAG = "FirestorePlugin";
    private FirebaseFirestore database;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Log.d(TAG, "Initializing FirestorePlugin");
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, action);

        if (action.equals("initialise")) {
            initialise(args, callbackContext);
        } else if (action.equals("collectionOnShapshot")) {
            collectionOnShapshot(args, callbackContext);
        } else if (action.equals("collectionAdd")) {
            collectionAdd(args, callbackContext);
        } else if (action.equals("collectionGet")) {
            collectionGet(args, callbackContext);
        } else if (action.equals("docSet")) {
            docSet(args, callbackContext);
        } else if (action.equals("docUpdate")) {
            docUpdate(args, callbackContext);
        } else if (action.equals("docOnShapshot")) {
            docOnShapshot(args, callbackContext);
        }
        return true;
    }

    private void initialise(JSONArray args, final CallbackContext callbackContext) {
        this.database = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        this.database.setFirestoreSettings(settings);
    }

    private void collectionOnShapshot(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collection = args.getString(0);
            final JSONArray whereArray = args.getJSONArray(1);
            final JSONArray orderArray = args.getJSONArray(2);

            Log.d(TAG, collection);
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        CollectionReference collectionRef = database.collection(collection);

                        for (int i = 0, n = whereArray.length(); i < n; ++i) {
                            try {
                                JSONObject where = whereArray.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        for (int i = 0, n = orderArray.length(); i < n; ++i) {
                            JSONObject order = null;
                            try {
                                order = orderArray.getJSONObject(i);
                                Query.Direction direction = Query.Direction.valueOf(order.getString("direction"));
                                collectionRef.orderBy(order.getString("field"), direction);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        Log.d(TAG, "listening");

                        collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value,
                                                @Nullable FirebaseFirestoreException e) {
                                Log.d(TAG, "got data");
                                callbackContext.sendPluginResult(createPluginResult(value));
                            }
                        });
                    } catch (Exception ex) {
                        Log.d(TAG, ex.getMessage());
                    }
                }
            });
        } catch (JSONException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    private void collectionAdd(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String collection = args.getString(0);
        final String docId = args.getString(1);
        final JSONObject data = args.getJSONObject(2);
        final JSONObject options = args.getJSONObject(3);

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    database.collection(collection).add(jsonToMap(data)).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            callbackContext.success(documentReference.getId());
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                            callbackContext.error(e.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }
        });
    }

    private void collectionGet(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String collection = args.getString(0);

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    database.collection(collection).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            callbackContext.sendPluginResult(createPluginResult(querySnapshot));
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                            callbackContext.error(e.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }
        });
    }

    private void docSet(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String collection = args.getString(0);
        final String docId = args.getString(1);
        final JSONObject data = args.getJSONObject(2);
        final JSONObject options = args.getJSONObject(3);

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    database.collection(collection).document(docId).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            callbackContext.success();
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                            callbackContext.error(e.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }
        });
    }

    private void docUpdate(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String collection = args.getString(0);
        final String docId = args.getString(1);
        final JSONObject data = args.getJSONObject(2);

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    database.collection(collection).document(docId).update(jsonToMap(data)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            callbackContext.success();
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callbackContext.error(e.getMessage());
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }
        });
    }

    private static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    private static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }


    private void docOnShapshot(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String collection = args.getString(0);
        final String doc = args.getString(1);
        final JSONObject options;

        if (args.length() > 2) {
            options = args.getJSONObject(2);
        } else {
            options = null;
        }

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                DocumentReference documentRef = database.collection(collection).document(doc);

                if (options != null) {
                    documentRef.set(options);
                }

                documentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        callbackContext.sendPluginResult(createPluginResult(value));
                    }
                });
            }
        });
    }

    private PluginResult createPluginResult(QuerySnapshot value) {
        JSONObject querySnapshot = new JSONObject();
        JSONArray array = new JSONArray();

        for (DocumentSnapshot doc : value) {
            try {
                JSONObject document = createDocumentSnapshot(doc);
                array.put(document);
                Log.d(TAG, toJSON(doc.getData()).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            querySnapshot.put("docs", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new PluginResult(PluginResult.Status.OK, querySnapshot);
    }

    private PluginResult createPluginResult(DocumentSnapshot doc) {
        return new PluginResult(PluginResult.Status.OK, createDocumentSnapshot(doc));
    }

    private JSONObject createDocumentSnapshot(DocumentSnapshot doc) {
        JSONObject documentSnapshot = new JSONObject();

        try {
            Log.d(TAG, "parse doc");
            documentSnapshot.put("id", doc.getId());
            documentSnapshot.put("exists", doc.exists());
            documentSnapshot.put("_data", toJSON(doc.getData()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return documentSnapshot;
    }

    private static JSONObject toJSON(Map<String, Object> values) throws JSONException {
        JSONObject result = new JSONObject();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = new JSONObject((Map) value);
            } else if (value instanceof List) {
                value = new JSONArray((List) value);
            }
            result.put(entry.getKey(), value);
        }
        return result;
    }
}
