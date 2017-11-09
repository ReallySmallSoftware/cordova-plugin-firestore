package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;


public class InitialiseHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public InitialiseHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    synchronized public boolean handle(JSONArray args, CallbackContext callbackContext) {

        try {
            if (firestorePlugin.getDatabase() == null) {
                Log.d(FirestorePlugin.TAG, "Initialising Firestore...");

                final boolean persist = args.getBoolean(0);

                FirebaseFirestore.setLoggingEnabled(true);
                firestorePlugin.setDatabase(FirebaseFirestore.getInstance());

                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(persist)
                        .build();
                firestorePlugin.getDatabase().setFirestoreSettings(settings);
            }
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error initialising Forestore", e);
        }

        return true;
    }
}
