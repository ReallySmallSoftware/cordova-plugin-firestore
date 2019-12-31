package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Query;

public class StartAfterQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object startAfter) {
        return query.startAfter(JSONHelper.fromJSON(startAfter));
    }
}
