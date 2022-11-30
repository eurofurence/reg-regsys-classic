package org.eurofurence.regsys.backend;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Temporary logging until we can plug in a proper logging framework
 */
public class Logging {
    private static final boolean DEBUG = false;

    private static String currentDatetime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");
        Date now = new Date();
        return sdfDate.format(now);
    }

    private static void log(String message) {
        System.out.println(currentDatetime() + message);
    }

    public static void debug(String message) {
        if (DEBUG)
            log("DEBUG " + message);
    }

    public static void info(String message) {
        log("INFO " + message);
    }

    public static void warn(String message) {
        log("WARN " + message);
    }

    public static void error(String message) {
        log("ERROR " + message);
    }

    public static void exception(Throwable e) {
        log("ERROR " + e.getClass().getSimpleName() + ": " + e.getMessage());
        e.printStackTrace();
    }
}
