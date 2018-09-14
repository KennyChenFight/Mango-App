package com.csim.scu.aibox.log;

import android.util.Log;

public class Logger {

    private static final String DEFAULT_TAG = "Kenny";
    private static final boolean LOG_ENABLE = true;
    private static final boolean DETAIL_ENABLE = true;

    private Logger() {

    }

    private static String buildMsg(String msg) {
        StringBuilder buffer = new StringBuilder();

        if (DETAIL_ENABLE) {
            final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];

            buffer.append("[");
            buffer.append(Thread.currentThread().getName());
            buffer.append(":");
            buffer.append(stackTraceElement.getFileName());
            buffer.append(":");
            buffer.append(stackTraceElement.getLineNumber());
            buffer.append(":");
            buffer.append(stackTraceElement.getMethodName());
        }
        buffer.append("()] ");
        buffer.append(msg);
        return buffer.toString();
    }

    // Log.VERBOSE
    public static void v() {
        if (LOG_ENABLE) {
            Log.v(DEFAULT_TAG, buildMsg(""));
        }
    }

    public static void v(String msg) {
        if (LOG_ENABLE) {
            Log.v(DEFAULT_TAG, buildMsg(msg));
        }
    }

    public static void v(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.v(tag, buildMsg(msg));
        }
    }

    // Log.DEBUG
    public static void d() {
        if (LOG_ENABLE) {
            Log.d(DEFAULT_TAG, buildMsg(""));
        }
    }
    public static void d(String msg) {
        if (LOG_ENABLE) {
            Log.d(DEFAULT_TAG, buildMsg(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.d(tag, buildMsg(msg));
        }
    }

    // Log.INFO
    public static void i() {
        if (LOG_ENABLE && Log.isLoggable(DEFAULT_TAG, Log.INFO)) {
            Log.i(DEFAULT_TAG, buildMsg(""));
        }
    }

    public static void i(String msg) {
        if (LOG_ENABLE && Log.isLoggable(DEFAULT_TAG, Log.INFO)) {
            Log.i(DEFAULT_TAG, buildMsg(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (LOG_ENABLE && Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, buildMsg(msg));
        }
    }

    // Log.WARN
    public static void w() {
        if (LOG_ENABLE && Log.isLoggable(DEFAULT_TAG, Log.WARN)) {
            Log.w(DEFAULT_TAG, buildMsg(""));
        }
    }
    public static void w(String msg) {
        if (LOG_ENABLE && Log.isLoggable(DEFAULT_TAG, Log.WARN)) {
            Log.w(DEFAULT_TAG, buildMsg(msg));
        }
    }

    public static void w(String tag, String msg) {
        if (LOG_ENABLE && Log.isLoggable(tag, Log.WARN)) {
            Log.w(tag, buildMsg(msg));
        }
    }

    public static void w(String msg, Exception e) {
        if (LOG_ENABLE && Log.isLoggable(DEFAULT_TAG, Log.WARN)) {
            Log.w(DEFAULT_TAG, buildMsg(msg), e);
        }
    }

    // Log.ERROR
    public static void e() {
        if (LOG_ENABLE && Log.isLoggable(DEFAULT_TAG, Log.ERROR)) {
            Log.e(DEFAULT_TAG, buildMsg(""));
        }
    }
    public static void e(String msg) {
        if (LOG_ENABLE && Log.isLoggable(DEFAULT_TAG, Log.ERROR)) {
            Log.e(DEFAULT_TAG, buildMsg(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (LOG_ENABLE & Log.isLoggable(tag, Log.INFO)) {
            Log.e(tag, buildMsg(msg));
        }
    }

    public static void e(String msg, Exception e) {
        if (LOG_ENABLE && Log.isLoggable(DEFAULT_TAG, Log.ERROR)) {
            Log.e(DEFAULT_TAG, buildMsg(msg), e);
        }
    }

}
