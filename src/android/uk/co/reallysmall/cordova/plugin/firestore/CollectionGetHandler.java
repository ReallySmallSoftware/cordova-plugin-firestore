package uk.co.reallysmall.cordova.plugin.firestore;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import static uk.co.reallysmall.cordova.plugin.firestore.PluginResultHelper.createPluginResult;

public class CollectionGetHandler implements ActionHandler {

    private FirestorePlugin firestorePlugin;

    public CollectionGetHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String collectionPath = args.getString(0);
            final JSONArray queries = args.getJSONArray(1);


            Log.d(FirestorePlugin.TAG, "Getting document from collection");

            try {
                CollectionReference collectionRef = firestorePlugin.getDatabase().collection(collectionPath);
                Query query = QueryHelper.processQueries(queries, collectionRef);

                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        callbackContext.sendPluginResult(createPluginResult(querySnapshot, false));
                        Log.d(FirestorePlugin.TAG, "Successfully got collection");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(FirestorePlugin.TAG, "Error getting collection", e);
                        callbackContext.error(e.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error processing collection get in thread", e);
                callbackContext.error(e.getMessage());
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing collection get", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }
}
