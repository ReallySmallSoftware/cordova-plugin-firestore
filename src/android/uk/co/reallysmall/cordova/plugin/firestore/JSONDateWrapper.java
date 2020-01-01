package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class JSONDateWrapper extends Date {

    private static String datePrefix = "__DATE:";

    public JSONDateWrapper(Date date) {
        super(date.getTime());
    }

    public static void setDatePrefix(String datePrefix) {
        JSONDateWrapper.datePrefix = datePrefix;
    }

    public static boolean isWrappedDate(Object value) {


        if (value instanceof String && ((String) value).startsWith(datePrefix)) {
            return true;
        }

        return false;
    }

    public static Date unwrapDate(Object value) {
        String stringValue = (String) value;
        int prefixLength = datePrefix.length();
        String timestamp = stringValue.substring(prefixLength + 1);

        return new Date(Long.parseLong(timestamp));
    }

    @Override
    public String toString() {
        return this.datePrefix + this.getTime();
    }
}
