package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Query;

public class StartAtQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object startAt) {

        if (JSONDateWrapper.isWrappedDate(startAt)) {
            startAt = JSONDateWrapper.unwrapDate(startAt);
        }

        return query.startAt(startAt);
    }
}
