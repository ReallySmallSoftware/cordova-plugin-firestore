package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class DocUnsubscribeHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public DocUnsubscribeHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, CallbackContext callbackContext) {
        try {
            String callbackId = args.getString(0);
            firestorePlugin.unregister(callbackId);
            callbackContext.success();
        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error unsubscribing from document", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
