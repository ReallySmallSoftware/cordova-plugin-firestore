package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.Timestamp;

import java.util.Date;

public class JSONTimestampWrapper {

    private static String timestampPrefix = "__TIMESTAMP:";
    private Timestamp timestamp;

    public JSONTimestampWrapper(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public static void setTimestampPrefix(String timestampPrefix) {
        JSONTimestampWrapper.timestampPrefix = timestampPrefix;
    }

    public static boolean isWrappedTimestamp(Object value) {


        if (value instanceof String && ((String) value).startsWith(timestampPrefix)) {
            return true;
        }

        return false;
    }

    public static Timestamp unwrapTimestamp(Object value) {
        String stringValue = (String) value;
        int prefixLength = timestampPrefix.length();
        String timestamp = stringValue.substring(prefixLength).substring(0, stringValue.length() - prefixLength);

        long seconds = 0L;
        int nanoseconds = 0;

        if (timestamp.contains("_")) {
            String[] timestampParts = timestamp.split("_");
            seconds = Long.parseLong(timestampParts[0]);
            nanoseconds = Integer.parseInt(timestampParts[1]);
        } else {
            seconds = Long.parseLong(timestamp);
        }

        return new Timestamp(seconds, nanoseconds);
    }

    @Override
    public String toString() {
        return this.timestampPrefix + timestamp.getSeconds() + "_" + timestamp.getNanoseconds();
    }
}
