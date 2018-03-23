package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Transaction;

public class TransactionWrapper {
    public Transaction transaction;
    public StringBuilder sync = new StringBuilder();

    public TransactionWrapper(Transaction transaction) {
        this.transaction = transaction;
    }
}
