package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Query;

public interface QueryHandler {
    Query handle(Query query, Object value);
}
