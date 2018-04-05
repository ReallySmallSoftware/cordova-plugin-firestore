package uk.co.reallysmall.cordova.plugin.firestore;


import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

public class RunTransactionHandler implements ActionHandler {

    public static final int TRANSACTION_TIMEOUT = 30000;
    private FirestorePlugin firestorePlugin;

    public RunTransactionHandler(FirestorePlugin firestorePlugin) {
        this.firestorePlugin = firestorePlugin;
    }

    @Override
    public boolean handle(JSONArray args, final CallbackContext callbackContext) {
        try {
            final String transactionId = args.getString(0);

            Log.d(FirestorePlugin.TAG, "Running transaction");

            try {
                firestorePlugin.getDatabase().runTransaction(new Transaction.Function<String>() {
                    @Override
                    public String apply(Transaction transaction) throws FirebaseFirestoreException {

                        Log.d(FirestorePlugin.TAG, String.format("Applying transaction %s", transactionId));

                        firestorePlugin.storeTransaction(transactionId, transaction);
                        TransactionQueue transactionQueue = firestorePlugin.getTransaction(transactionId);

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {

                                WebView wv = (WebView) firestorePlugin.webView.getView();
                                wv.evaluateJavascript(String.format("Firestore.__executeTransaction('%s');", transactionId), new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                    }
                                });
                            }
                        };

                        firestorePlugin.cordova.getActivity().runOnUiThread(runnable);

                        Long started = System.currentTimeMillis();

                        boolean timedOut = false;

                        TransactionOperationType transactionOperationType = TransactionOperationType.NONE;

                        while (transactionOperationType != TransactionOperationType.RESOLVE && !timedOut) {

                            timedOut = timedOut(started);

                            while (transactionQueue.queue.size() < 1 && !timedOut) {
                            }

                            TransactionDetails transactionDetails = transactionQueue.queue.get(0);
                            transactionOperationType = transactionDetails.transactionOperationType;

                            switch (transactionOperationType) {
                                case SET:
                                    performSet(transaction, transactionDetails, transactionId);
                                    break;
                                case DELETE:
                                    performDelete(transaction, transactionDetails, transactionId);
                                    break;
                                case UPDATE:
                                    performUpdate(transaction, transactionDetails, transactionId);
                                    break;
                                default:
                                    break;
                            }

                            transactionQueue.queue.remove(0);
                        }

                        firestorePlugin.removeTransaction(transactionId);

                        if (timedOut) {
                            throw new RuntimeException("Transaction timed out");
                        } else {
                            Log.d(FirestorePlugin.TAG, String.format("Sync result complete for transaction %s", transactionId));
                        }

                        Log.d(FirestorePlugin.TAG, String.format("Returning transaction %s result %s", transactionId, transactionQueue.results.toString()));
                        return transactionQueue.results.toString();
                    }

                    private boolean timedOut(Long started) {
                        Long current = System.currentTimeMillis();

                        if (current - started > TRANSACTION_TIMEOUT) {
                            return true;
                        }

                        return false;
                    }
                }).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String result) {
                        callbackContext.success(result);
                        Log.d(FirestorePlugin.TAG, "Transaction success");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(FirestorePlugin.TAG, "Transaction failure", e);
                        callbackContext.error(e.getMessage());
                    }
                });

            } catch (Exception e) {
                Log.e(FirestorePlugin.TAG, "Error running transaction", e);
                callbackContext.error(e.getMessage());
            }

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error running transaction", e);
            callbackContext.error(e.getMessage());
        }

        return true;
    }

    public void performDelete(Transaction transaction, TransactionDetails transactionDetails, String transactionId) {

        Log.d(FirestorePlugin.TAG, String.format("Perform transactional document delete for %s", transactionId));

        try {
            DocumentReference documentRef = firestorePlugin.getDatabase().collection(transactionDetails.collectionPath).document(transactionDetails.docId);
            transaction.delete(documentRef);

        } catch (Exception e) {
            Log.e(FirestorePlugin.TAG, "Error performing transactional document delete in thread", e);
            throw new RuntimeException(e);
        }
    }

    public void performSet(Transaction transaction, TransactionDetails transactionDetails, String transactionId) {

        Log.d(FirestorePlugin.TAG, String.format("Perform transactional document set for %s", transactionId));

        SetOptions setOptions = DocSetOptions.getSetOptions(transactionDetails.options);

        try {
            DocumentReference documentRef = firestorePlugin.getDatabase().collection(transactionDetails.collectionPath).document(transactionDetails.docId);

            if (setOptions == null) {
                transaction.set(documentRef, JSONHelper.toSettableMap(transactionDetails.data));
            } else {
                transaction.set(documentRef, JSONHelper.toSettableMap(transactionDetails.data), setOptions);
            }

        } catch (Exception e) {
            Log.e(FirestorePlugin.TAG, "Error performing transactional document set in thread", e);
            throw new RuntimeException(e);
        }
    }

    public void performUpdate(Transaction transaction, TransactionDetails transactionDetails, String transactionId) {

        Log.d(FirestorePlugin.TAG, String.format("Perform transactional document update for %s", transactionId));

        try {
            DocumentReference documentRef = firestorePlugin.getDatabase().collection(transactionDetails.collectionPath).document(transactionDetails.docId);
            transaction.update(documentRef, JSONHelper.toSettableMap(transactionDetails.data));

        } catch (Exception e) {
            Log.e(FirestorePlugin.TAG, "Error performing transactional document update in thread", e);
            throw new RuntimeException(e);
        }
    }
}
