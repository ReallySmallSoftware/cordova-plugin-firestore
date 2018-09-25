package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.Query;

public class EndBeforeQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object endBefore) {

        if (JSONDateWrapper.isWrappedDate(endBefore)) {
            endBefore = JSONDateWrapper.unwrapDate(endBefore);
        } else if (JSONGeopointWrapper.isWrappedGeoPoint(endBefore)) {
            endBefore = JSONGeopointWrapper.unwrapGeoPoint(endBefore);
        }

        return query.endBefore(endBefore);
    }
}
