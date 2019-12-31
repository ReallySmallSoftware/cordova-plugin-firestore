package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Query;

public class EndAtQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object endAt) {
        return query.endAt(JSONHelper.fromJSON(endAt));
    }
}
