package uk.co.reallysmall.cordova.plugin.firestore;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class JSONGeopointWrapper extends GeoPoint {

    private static String geopointPrefix = "__GEOPOINT:";

    public JSONGeopointWrapper(GeoPoint geoPoint) {
        super(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    public static void setGeopointPrefix(String geopointPrefix) {
        JSONGeopointWrapper.geopointPrefix = geopointPrefix;
    }

    public static boolean isWrappedGeoPoint(Object value) {


        if (value instanceof String && ((String) value).startsWith(geopointPrefix)) {
            return true;
        }

        return false;
    }

    public static GeoPoint unwrapGeoPoint(Object value) {
        String stringValue = (String) value;
        int prefixLength = geopointPrefix.length();
        String latLng = stringValue.substring(prefixLength).substring(0, stringValue.length() - prefixLength);
        String[] tmp = latLng.split(",");
        return new GeoPoint(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1]));
    }

    @Override
    public String toString() {
        return this.geopointPrefix + this.getLatitude() + "-" + this.getLongitude();
    }
}
