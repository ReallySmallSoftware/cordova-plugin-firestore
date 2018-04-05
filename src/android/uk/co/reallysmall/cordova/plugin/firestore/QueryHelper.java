package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QueryHelper {
    private static Map<String, QueryHandler> queryHandlers = new HashMap<String, QueryHandler>();

    static {
        queryHandlers.put("limit", new LimitQueryHandler());
        queryHandlers.put("where", new WhereQueryHandler());
        queryHandlers.put("orderBy", new OrderByQueryHandler());
        queryHandlers.put("startAfter", new StartAfterQueryHandler());
        queryHandlers.put("startAt", new StartAtQueryHandler());
        queryHandlers.put("endAt", new EndAtQueryHandler());
        queryHandlers.put("endBefore", new EndBeforeQueryHandler());
    }


    public static Query processQueries(JSONArray queries, Query query) throws JSONException {

        Log.d(FirestorePlugin.TAG, "Processing queries");

        int length = queries.length();
        for (int i = 0; i < length; i++) {
            JSONObject queryDefinition = queries.getJSONObject(i);

            String queryType = queryDefinition.getString("queryType");

            if (queryHandlers.containsKey(queryType)) {
                query = queryHandlers.get(queryType).handle(query, queryDefinition.get("value"));
            } else {
                Log.e(FirestorePlugin.TAG, String.format("Unknown query type %s", queryType));
            }
        }

        return query;
    }
}
