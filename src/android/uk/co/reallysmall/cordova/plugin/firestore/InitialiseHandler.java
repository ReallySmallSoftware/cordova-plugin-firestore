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
    public static final String TIMESTAMP_PREFIX = "timestampPrefix";
    public static final String GEOPOINT_PREFIX = "geopointPrefix";
    public static final String REFERENCE_PREFIX = "referencePrefix";
    public static final String FIELDVALUE_DELETE = "fieldValueDelete";
    public static final String FIELDVALUE_SERVERTIMESTAMP = "fieldValueServerTimestamp";
    public static final String FIELDVALUE_ARRAYREMOVE = "fieldValueArrayRemove";
    public static final String FIELDVALUE_ARRAYUNION = "fieldValueArrayUnion";
    public static final String FIELDVALUE_INCREMENT = "fieldValueIncrement";
    public static final String TIMESTAMPSINSNAPSHOTS = "timestampsInSnapshots";
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

            FirestoreLog.d(FirestorePlugin.TAG, "Initialising Firestore...");

            final JSONObject options = args.getJSONObject(0);
            FirestoreLog.d(FirestorePlugin.TAG, "Options : " + options.toString());

            FirebaseFirestore.setLoggingEnabled(true);
            if (options.has(CONFIG)) {
                JSONObject config = options.getJSONObject(CONFIG);
                FirebaseOptions.Builder configBuilder = new FirebaseOptions.Builder();
                if (options.has("applicationId")) {
                    configBuilder.setApplicationId(options.getString("applicationId"));
                }
                if (options.has("gcmSenderId")) {
                    configBuilder.setGcmSenderId(options.getString("gcmSenderId"));
                }
                if (options.has("apiKey")) {
                    configBuilder.setApiKey(options.getString("apiKey"));
                }
                if (options.has("projectId")) {
                    configBuilder.setProjectId(options.getString("projectId"));
                }
                if (options.has("databaseUrl")) {
                    configBuilder.setDatabaseUrl(options.getString("databaseUrl"));
                }
                if (options.has("storageBucket")) {
                    configBuilder.setStorageBucket(options.getString("storageBucket"));
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

            if (options.has(TIMESTAMP_PREFIX)) {
                JSONTimestampWrapper.setTimestampPrefix(options.getString(TIMESTAMP_PREFIX));
            }

            if (options.has(GEOPOINT_PREFIX)) {
                JSONGeopointWrapper.setGeopointPrefix(options.getString(GEOPOINT_PREFIX));
            }

            if (options.has(REFERENCE_PREFIX)) {
                JSONReferenceWrapper.setReferencePrefix(options.getString(REFERENCE_PREFIX));
            }

            if (options.has(FIELDVALUE_DELETE)) {
                FieldValueHelper.setDeletePrefix(options.getString(FIELDVALUE_DELETE));
            }

            if (options.has(FIELDVALUE_SERVERTIMESTAMP)) {
                FieldValueHelper.setServerTimestampPrefix(options.getString(FIELDVALUE_SERVERTIMESTAMP));
            }

            if (options.has(FIELDVALUE_ARRAYREMOVE)) {
                FieldValueHelper.setArrayRemovePrefix(options.getString(FIELDVALUE_ARRAYREMOVE));
            }

            if (options.has(FIELDVALUE_ARRAYUNION)) {
                FieldValueHelper.setArrayUnionPrefix(options.getString(FIELDVALUE_ARRAYUNION));
            }

            if (options.has(FIELDVALUE_INCREMENT)) {
                FieldValueHelper.setIncrementPrefix(options.getString(FIELDVALUE_INCREMENT));
            }

            JSONHelper.setPlugin(this.firestorePlugin);

            FirestoreLog.d(FirestorePlugin.TAG, "Setting Firestore persistance to " + persist);

            boolean timestampsInSnapshots = false;

            if (options.has(TIMESTAMPSINSNAPSHOTS)) {
                timestampsInSnapshots = options.getBoolean(TIMESTAMPSINSNAPSHOTS);
            }

            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(persist)
                    .build();
            firestorePlugin.getDatabase().setFirestoreSettings(settings);

            callbackContext.success();
        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error initialising Firestore", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
