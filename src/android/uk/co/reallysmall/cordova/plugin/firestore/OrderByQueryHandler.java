package uk.co.reallysmall.cordova.plugin.firestore;


import android.util.Log;

import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

public class OrderByQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object orderByObject) {

        JSONObject order = (JSONObject) orderByObject;

        try {
            Query.Direction direction = Query.Direction.valueOf(order.getString("direction"));
            query = query.orderBy(order.getString("field"), direction);

            Log.d(FirestorePlugin.TAG, "Order by " + order.getString("field") + " (" + direction.toString() + ")");

        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing ordering", e);
        }

        return query;
    }
}
