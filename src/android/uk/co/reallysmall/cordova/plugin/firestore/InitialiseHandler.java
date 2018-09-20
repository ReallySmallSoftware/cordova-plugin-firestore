package uk.co.reallysmall.cordova.plugin.firestore;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class InitialiseHandler implements ActionHandler {

    public static final String PERSIST = "persist";
    public static final String DATE_PREFIX = "datePrefix";
    public static final String FIELDVALUE_DELETE = "fieldValueDelete";
    public static final String FIELDVALUE_SERVERTIMESTAMP = "fieldValueServerTimestamp";
    public static final String CONFIG = "config";
    private FirestorePlugin firestorePlugin;
    private Context context;

    public InitialiseHandler(Context context, FirestorePlugin firestorePlugin) {
        this.context = context;
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(final JSONArray args, CallbackContext callbackContext) {

        try {

            Log.d(FirestorePlugin.TAG, "Initialising Firestore...");

            final JSONObject options = args.getJSONObject(0);

            FirebaseFirestore.setLoggingEnabled(true);
            if (options.has(CONFIG)) {
                JSONObject config = options.getJSONObject(CONFIG);
                FirebaseOptions.Builder configBuilder = new FirebaseOptions.Builder();
                if (options.has("applicationId")) {
                    configBuilder.setApplicationId("applicationId");
                }
                if (options.has("gcmSenderID")) {
                    configBuilder.setGcmSenderId("gcmSenderID");
                }
                if (options.has("apiKey")) {
                    configBuilder.setApiKey("apiKey");
                }
                if (options.has("projectID")) {
                    configBuilder.setProjectId("projectID");
                }
                if (options.has("databaseURL")) {
                    configBuilder.setDatabaseUrl("databaseURL");
                }
                if (options.has("storageBucket")) {
                    configBuilder.setStorageBucket("storageBucket");
                }

                FirebaseOptions customOptions = configBuilder.build();

                FirebaseApp customApp;
                try {
                    customApp = FirebaseApp.getInstance(config.getString("apiKey"));
                } catch (Exception err) {
                    FirebaseApp.initializeApp(this.context, customOptions, config.getString("apiKey"));
                    customApp = FirebaseApp.getInstance(config.getString("apiKey"));
                    err.printStackTrace();
                }

                firestorePlugin.setDatabase(FirebaseFirestore.getInstance(customApp));
            } else{
                firestorePlugin.setDatabase(FirebaseFirestore.getInstance());
            }

            boolean persist = false;

            if (options.has(PERSIST) && options.getBoolean(PERSIST)) {
                persist = true;
            }

            if (options.has(DATE_PREFIX)) {
                JSONDateWrapper.setDatePrefix(options.getString(DATE_PREFIX));
            }

            if (options.has(FIELDVALUE_DELETE)) {
                FieldValueHelper.setDeletePrefix(options.getString(FIELDVALUE_DELETE));
            }

            if (options.has(FIELDVALUE_SERVERTIMESTAMP)) {
                FieldValueHelper.setServerTimestampPrefix(options.getString(FIELDVALUE_SERVERTIMESTAMP));
            }

            Log.d(FirestorePlugin.TAG, "Setting Firestore persistance to " + persist);

            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(persist)
                    .build();
            firestorePlugin.getDatabase().setFirestoreSettings(settings);

            callbackContext.success();
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error initialising Forestore", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
