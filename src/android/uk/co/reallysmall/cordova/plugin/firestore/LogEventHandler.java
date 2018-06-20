package uk.co.reallysmall.cordova.plugin.firestore;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class LogEventHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;
    private Context context;
    private FirebaseAnalytics firebaseAnalytics;

    public LogEventHandler(Context context, FirestorePlugin firestorePlugin) {
        this.context = context;
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(final JSONArray args, CallbackContext callbackContext) {

        try {
            this.firebaseAnalytics = FirebaseAnalytics.getInstance(this.context);

            final String name = args.getString(0);
            final JSONObject params = args.getJSONObject(1);
            Bundle bundle = new Bundle();
            Iterator<String> it = params.keys();

            while (it.hasNext()) {
                String key = it.next();
                Object value = params.get(key);

                if (value instanceof String) {
                    bundle.putString(key, (String)value);
                } else if (value instanceof Integer) {
                    bundle.putInt(key, (Integer)value);
                } else if (value instanceof Double) {
                    bundle.putDouble(key, (Double)value);
                } else if (value instanceof Long) {
                    bundle.putLong(key, (Long)value);
                } else {
                    Log.w(this.firestorePlugin.TAG, "Value for key " + key + " not one of (String, Integer, Double, Long)");
                }
            }

            this.firebaseAnalytics.logEvent(name, bundle);

            callbackContext.success();
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error logEvent", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
