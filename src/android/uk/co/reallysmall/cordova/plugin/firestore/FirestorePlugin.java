/**
 */
package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Hashtable;
import java.util.Map;

public class FirestorePlugin extends CordovaPlugin {
    static final String TAG = "FirestorePlugin";
    private static FirebaseFirestore database;
    private Map<String, ActionHandler> handlers = new Hashtable<String, ActionHandler>();
    private Map<String, ListenerRegistration> registrations = new Hashtable<String, ListenerRegistration>();
    private Map<String, TransactionQueue> transactions = new Hashtable<String, TransactionQueue>();

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        FirestoreLog.d(TAG, "Initializing FirestorePlugin");
        super.initialize(cordova, webView);

        handlers.put("collectionOnSnapshot", new CollectionOnSnapshotHandler(FirestorePlugin.this));
        handlers.put("collectionUnsubscribe", new CollectionUnsubscribeHandler(FirestorePlugin.this));
        handlers.put("collectionAdd", new CollectionAddHandler(FirestorePlugin.this));
        handlers.put("collectionGet", new CollectionGetHandler(FirestorePlugin.this));
        handlers.put("initialise", new InitialiseHandler(webView.getContext().getApplicationContext(), FirestorePlugin.this));
        handlers.put("docSet", new DocSetHandler(FirestorePlugin.this));
        handlers.put("docUpdate", new DocUpdateHandler(FirestorePlugin.this));
        handlers.put("docOnSnapshot", new DocOnSnapshotHandler(FirestorePlugin.this));
        handlers.put("docUnsubscribe", new DocUnsubscribeHandler(FirestorePlugin.this));
        handlers.put("docGet", new DocGetHandler(FirestorePlugin.this));
        handlers.put("docDelete", new DocDeleteHandler(FirestorePlugin.this));
        handlers.put("runTransaction", new RunTransactionHandler(FirestorePlugin.this));
        handlers.put("transactionDocGet", new TransactionDocGetHandler(FirestorePlugin.this));
        handlers.put("transactionDocUpdate", new TransactionDocUpdateHandler(FirestorePlugin.this));
        handlers.put("transactionDocSet", new TransactionDocSetHandler(FirestorePlugin.this));
        handlers.put("transactionDocDelete", new TransactionDocDeleteHandler(FirestorePlugin.this));
        handlers.put("transactionResolve", new TransactionResolveHandler(FirestorePlugin.this));
        handlers.put("setLogLevel", new setLogLevel());

        FirestoreLog.d(TAG, "Done Initializing FirestorePlugin");
    }

    public FirebaseFirestore getDatabase() {
        return database;
    }

    public void setDatabase(FirebaseFirestore database) {
        this.database = database;
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        FirestoreLog.d(TAG, action);

        if (handlers.containsKey(action)) {
            return handlers.get(action).handle(args, callbackContext);
        }

        return false;
    }

    public void addRegistration(String callbackId, ListenerRegistration listenerRegistration) {
        registrations.put(callbackId, listenerRegistration);
        FirestoreLog.d(TAG, "Registered subscriber " + callbackId);

    }

    public void unregister(String callbackId) {
        if (registrations.containsKey(callbackId)) {
            registrations.get(callbackId).remove();
            registrations.remove(callbackId);
            FirestoreLog.d(TAG, "Unregistered subscriber " + callbackId);
        }
    }

    public void storeTransaction(String transactionId, Transaction transaction) {
        TransactionQueue transactionQueue = new TransactionQueue();
        transactionQueue.transaction = transaction;
        transactions.put(transactionId, transactionQueue);
    }

    public TransactionQueue getTransaction(String transactionId) {
        return transactions.get(transactionId);
    }

    public void removeTransaction(String transactionId) {
        transactions.remove(transactionId);
    }

    private class setLogLevel implements ActionHandler {

        @Override
        public boolean handle(JSONArray args, CallbackContext callbackContext) throws JSONException {
            FirestoreLog.setLogLevel(args.getString(0));
            callbackContext.success();
            return true;
        }
    }

}
