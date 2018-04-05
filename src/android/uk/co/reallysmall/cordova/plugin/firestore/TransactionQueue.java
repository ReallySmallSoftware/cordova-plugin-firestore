package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionQueue {
    public Transaction transaction;
    public StringBuilder results = new StringBuilder();
    public List<TransactionDetails> queue = Collections.synchronizedList(new ArrayList<TransactionDetails>());
}
