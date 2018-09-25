package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

public class FirestoreLog {

  private static int _logLevel = Log.DEBUG;

  public static void setLogLevel(String logLevel) {
    _logLevel = ("debug".equals(logLevel)) ? Log.DEBUG :
        ("error".equals(logLevel)) ? Log.WARN : Log.ERROR;
  }

  public static void d(String TAG, String message) {
    if (_logLevel > Log.DEBUG) {
      Log.d(TAG, message);
    }
  }
  public static void w(String TAG, String message) {
    if (_logLevel > Log.WARN) {
      Log.w(TAG, message);
    }
  }
  public static void w(String TAG, String message, Exception e) {
    if (_logLevel > Log.WARN) {
      Log.w(TAG, message, e);
    }
  }
  public static void e(String TAG, String message) {
    if (_logLevel > Log.ERROR) {
      Log.e(TAG, message);
    }
  }
  public static void e(String TAG, String message, Exception e) {
    if (_logLevel > Log.ERROR) {
      Log.e(TAG, message, e);
    }
  }
}
