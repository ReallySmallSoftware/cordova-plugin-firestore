package uk.co.reallysmall.cordova.plugin.firestore;

import java.util.Date;

public class JSONDateWrapper extends Date {
    public JSONDateWrapper(Date date) {
        super(date.getTime());
    }

    @Override
    public String toString() {
        return "__DATE(" + this.getTime() + ")";
    }
}
