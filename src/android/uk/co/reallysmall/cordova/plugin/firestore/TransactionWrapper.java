package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Transaction;

public class TransactionWrapper {
    public String transactionId;
    public Transaction transaction;
    public StringBuilder sync = new StringBuilder();
}
