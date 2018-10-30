package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Query;

public class EndAtQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object endAt) {

        if (JSONDateWrapper.isWrappedDate(endAt)) {
            endAt = JSONDateWrapper.unwrapDate(endAt);
        } else if (JSONTimestampWrapper.isWrappedTimestamp(endAt)) {
            endAt = JSONTimestampWrapper.unwrapTimestamp(endAt);
        } else if (JSONGeopointWrapper.isWrappedGeoPoint(endAt)) {
            endAt = JSONGeopointWrapper.unwrapGeoPoint(endAt);
        }

        return query.endAt(endAt);
    }
}
