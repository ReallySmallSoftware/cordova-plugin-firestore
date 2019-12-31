package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FieldPath;

import org.json.JSONException;
import org.json.JSONObject;

public class WhereQueryHandler implements QueryHandler {
    @Override
    public Query handle(Query query, Object whereObject) {

        try {
            JSONObject where = (JSONObject) whereObject;

            String fieldPath =where.getString("fieldPath");
            String opStr = where.getString("opStr");
            Object value = parseWhereValue(where);

            if (FieldPathHelper.isWrapped(fieldPath)) {

                FieldPath fieldPathUnwrapped = FieldPathHelper.unwrapFieldPath(fieldPath);

                if ("==".equals(opStr)) {
                    query = query.whereEqualTo(fieldPathUnwrapped, value);
                } else if (">".equals(opStr)) {
                    query = query.whereGreaterThan(fieldPathUnwrapped, value);
                } else if (">=".equals(opStr)) {
                    query = query.whereGreaterThanOrEqualTo(fieldPathUnwrapped, value);
                } else if ("<".equals(opStr)) {
                    query = query.whereLessThan(fieldPathUnwrapped, value);
                } else if ("<=".equals(opStr)) {
                    query = query.whereLessThanOrEqualTo(fieldPathUnwrapped, value);
                } else if ("in".equals(opStr)) {
                  //  query = query.whereIn(fieldPathUnwrapped, value);
                } else if ("array-contains".equals(opStr)) {
                    query = query.whereArrayContains(fieldPathUnwrapped, value);
                } else if ("array-contains-any".equals(opStr)) {
                    query = query.whereArrayContains(fieldPathUnwrapped, value);
                } else {
                    throw new RuntimeException("Unknown operator type " + opStr);
                }
            } else {
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
                } else if ("in".equals(opStr)) {
                 //   query = query.whereIn(fieldPath, value);
                } else if ("array-contains".equals(opStr)) {
                    query = query.whereArrayContains(fieldPath, value);
                } else if ("array-contains-any".equals(opStr)) {
                    query = query.whereArrayContains(fieldPath, value);
                } else {
                    throw new RuntimeException("Unknown operator type " + opStr);
                }
            }
        } catch (JSONException e) {
            FirestoreLog.e(FirestorePlugin.TAG, "Error processing where", e);
            throw new RuntimeException(e);
        }

        return query;
    }

    private Object parseWhereValue(JSONObject where) throws JSONException {
        return JSONHelper.fromJSON(where.get("value"));
    }
}
