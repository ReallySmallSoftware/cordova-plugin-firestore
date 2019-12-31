package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Query;

public class EndBeforeQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object endBefore) {
        return query.endBefore(JSONHelper.fromJSON(endBefore));
    }
}
