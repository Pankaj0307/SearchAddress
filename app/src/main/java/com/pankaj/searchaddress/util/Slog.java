package com.pankaj.searchaddress.util;

import android.util.Log;

/**
 * Created by s.pankaj on 11-04-2018.
 */

public class Slog {

    public static final boolean isLogging = Util.debugMode;

    public static void e(String log_tag, String message) {
        if (isLogging)
            Log.e(log_tag, message);
    }

    public static void e(String log_tag, String message, Throwable tr) {
        if (isLogging)
            Log.e(log_tag, message, tr);
    }

    public static void d(String log_tag, String message) {
        if (isLogging)
            Log.d(log_tag, message);
    }

    public static void i(String log_tag, String message) {
        if (isLogging)
            Log.i(log_tag, message);
    }

    public static void v(String log_tag, String message) {
        if (isLogging)
            Log.v(log_tag, message);
    }

    public static void w(String log_tag, String message) {
        if (isLogging)
            Log.w(log_tag, message);
    }
}
