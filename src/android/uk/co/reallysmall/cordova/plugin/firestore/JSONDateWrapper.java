package uk.co.reallysmall.cordova.plugin.firestore;

import java.util.Date;

public class JSONDateWrapper extends Date {

    private static String datePrefix = "__DATE:";

    public JSONDateWrapper(Date date) {
        super(date.getTime());
    }

    public static void setDatePrefix(String datePrefix) {
        JSONDateWrapper.datePrefix = datePrefix;
    }

    @Override
    public String toString() {
        return this.datePrefix + this.getTime();
    }
}
