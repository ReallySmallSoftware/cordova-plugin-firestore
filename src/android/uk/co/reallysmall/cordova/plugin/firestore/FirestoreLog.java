package uk.co.reallysmall.cordova.plugin.firestore;

import android.util.Log;

public class FirestoreLog {

  private static int _logLevel = Log.DEBUG;

  public static void setLogLevel(String logLevel) {
    FirestoreLog.w(FirestorePlugin.TAG, "Setting log level to : " + logLevel);
    switch(logLevel.toLowerCase()) {
      case "debug":
        _logLevel = Log.DEBUG;
        break;
      case "error":
        _logLevel = Log.ERROR;
        break;
      case "warn":
      case "warning":
        _logLevel = Log.WARN;
        break;
      default:
        FirestoreLog.e(FirestorePlugin.TAG, "New logLevel unknown, leaving previous setting : " + _logLevel);
        break;
    }

    FirestoreLog.d(FirestorePlugin.TAG, "New logLevel : " + _logLevel);
  }

  public static void d(String TAG, String message) {
    if (_logLevel <= Log.DEBUG) {
      Log.d(TAG, message);
    }
  }
  public static void w(String TAG, String message) {
    if (_logLevel <= Log.WARN) {
      Log.w(TAG, message);
    }
  }
  public static void w(String TAG, String message, Exception e) {
    if (_logLevel <= Log.WARN) {
      Log.w(TAG, message, e);
    }
  }
  public static void e(String TAG, String message) {
    if (_logLevel <= Log.ERROR) {
      Log.e(TAG, message);
    }
  }
  public static void e(String TAG, String message, Exception e) {
    if (_logLevel <= Log.ERROR) {
      Log.e(TAG, message, e);
    }
  }
}
