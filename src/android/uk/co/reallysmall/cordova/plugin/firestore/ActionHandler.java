package uk.co.reallysmall.cordova.plugin.firestore;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;

public interface ActionHandler {
    boolean handle(JSONArray args, final CallbackContext callbackContext);
}
