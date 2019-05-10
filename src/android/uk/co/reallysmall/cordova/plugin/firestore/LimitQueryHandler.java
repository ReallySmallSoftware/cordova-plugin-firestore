package uk.co.reallysmall.cordova.plugin.firestore;


import com.google.firebase.firestore.Query;

public class LimitQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object limit) {
        System.out.println("Type of limit " + limit.getClass().getName());
        Long longLimit;
        if (limit instanceof String) {
            longLimit = Long.parseLong((String) limit);
        } else if(limit instanceof Number) {
            longLimit = ((Number) limit).longValue();
        } else {
            throw new IllegalArgumentException("Limit should be instanceof String or Number, got : " + limit.getClass().getName());
        }

        return query.limit(longLimit);
    }
}
