package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class CollectionUnsubscribeHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public CollectionUnsubscribeHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, CallbackContext callbackContext) {
        try {
            String callbackId = args.getString(0);
            firestorePlugin.unregister(callbackId);
            callbackContext.success();
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error unsubscribing from collection", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
