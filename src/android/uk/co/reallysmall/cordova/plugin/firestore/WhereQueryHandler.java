package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class WhereQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object whereObject) {

        try {
            JSONObject where = (JSONObject) whereObject;

            String fieldPath = where.getString("fieldPath");
            String opStr = where.getString("opStr");
            Object value = parseWhereValue(where);

            if ("==".equals(opStr)) {
                query = query.whereEqualTo(fieldPath, value);
            } else if (">".equals(opStr)) {
                query = query.whereGreaterThan(fieldPath, value);
            } else if (">=".equals(opStr)) {
                query = query.whereGreaterThanOrEqualTo(fieldPath, value);
            } else if ("<".equals(opStr)) {
                query = query.whereLessThan(fieldPath, value);
            } else if ("<=".equals(opStr)) {
                query = query.whereLessThanOrEqualTo(fieldPath, value);
            } else {
                throw new RuntimeException("Unknown operator type " + opStr);
            }
        } catch (JSONException e) {
            Log.e(FirestorePlugin.TAG, "Error processing where", e);
            throw  new RuntimeException(e);
        }

        return query;
    }

    private Object parseWhereValue(JSONObject where) throws JSONException {
        Object value = where.get("value");

        if (JSONDateWrapper.isWrappedDate(value)) {
            return JSONDateWrapper.unwrapDate(value);
        }

        return value;
    }
}
