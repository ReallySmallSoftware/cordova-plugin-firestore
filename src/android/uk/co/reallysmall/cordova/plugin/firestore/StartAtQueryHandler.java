package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Query;

public class StartAtQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object startAt) {
        return query.startAt(JSONHelper.fromJSON(startAt));
    }
}
