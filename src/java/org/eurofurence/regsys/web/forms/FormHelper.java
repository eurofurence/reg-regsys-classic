package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.web.pages.Page;

public class FormHelper {
    // data checking helpers - error messages are placed in the error list of the page

    public static boolean testMandatory(Page page, String value, final String field) {
        return Util.testMandatory(page::addError, value, field);
    }

    public static int parseInt(Page page, String value, final String field, int oldvalue) {
        return Util.parseInt(page::addError, value, field, oldvalue);
    }

    public static long parseLong(Page page, String value, final String field, long oldvalue) {
        return Util.parseLong(page::addError, value, field, oldvalue);
    }

    public static boolean testInt(Page page, int value, final String field, int min, int max) {
        return Util.testInt(page::addError, value, field, min, max);
    }

    public static int parseInt(Page page, String value, final String field, int oldvalue, int min, int max) {
        return Util.parseInt(page::addError, value, field, oldvalue, min, max);
    }

    public static long parseLong(Page page, String value, final String field, long oldvalue, long min, long max) {
        return Util.parseLong(page::addError, value, field, oldvalue, min, max);
    }

    public static float parseFloat(Page page, String value, final String field, float oldvalue) {
        return Util.parseFloat(page::addError, value, field, oldvalue);
    }

    public static long parseCurrencyDecimals(Page page, String value, final String field, long oldvalue) {
        return Util.parseCurrencyDecimals(page::addError, value, field, oldvalue);
    }

    public static float parseFloat(Page page, String value, final String field, float oldvalue, float min, float max) {
        return Util.parseFloat(page::addError, value, field, oldvalue, min, max);
    }

    public static String formatDate(java.util.Date thedate) {
        return Util.formatDate(thedate);
    }

    public static java.util.Date parseStaticDate(final String value) {
        return Util.parseStaticDate(value);
    }

    public static boolean testDate(Page page,
                           java.util.Date thedate, final String field,
                           String minDate, String minMsg,
                           String maxDate, String maxMsg) {
        return Util.testDate(page::addError, thedate, field, minDate, minMsg, maxDate, maxMsg);
    }

    public static java.util.Date parseDate(Page page,
                           String value, final String field, java.util.Date oldvalue,
                           String minDate, String minMsg,
                           String maxDate, String maxMsg) {
        return Util.parseDate(page::addError, value, field, oldvalue, minDate, minMsg, maxDate, maxMsg);
    }

    public static boolean testEnum(Page page,
                   String input, final String field,
                   String[] values) {
        return Util.testEnum(page::addError, input, field, values);
    }

    public static boolean testEmail(Page page, String input, final String field) {
        return Util.testEmail(page::addError, input, field);
    }

    public static String toDecimals(double d) {
        return Util.toDecimals(d);
    }

    public static String toCurrencyDecimals(Long cents) {
        return Util.toCurrencyDecimals(cents);
    }

    public static float toFloat(String s) {
        return Util.toFloat(s);
    }
}
