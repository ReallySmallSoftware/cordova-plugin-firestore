package uk.co.reallysmall.cordova.plugin.firestore;


import com.google.firebase.firestore.Query;

public class LimitQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object limit) {
        return query.limit(Long.parseLong((String) limit));
    }
}
