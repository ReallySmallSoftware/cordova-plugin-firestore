package uk.co.reallysmall.cordova.plugin.firestore;

import org.json.JSONObject;

public class TransactionDetails {
    public String collectionPath;
    public String docId;
    public JSONObject data;
    public JSONObject options;
    public String results;
    public TransactionOperationType transactionOperationType;
}
