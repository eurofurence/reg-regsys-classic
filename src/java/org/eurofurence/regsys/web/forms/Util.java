package org.eurofurence.regsys.web.forms;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;

import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.types.IsoDate;
import org.eurofurence.regsys.validators.EmailValidator;

/**
 * Static utility functions
 */
public class Util {
    private static final ThreadLocal<SimpleDateFormat> PUBLIC_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat(Strings.conf.dateFormat));

    /**
     * Returns <code>str</code> unchanged if it is non-null, otherwise returns
     * an empty string.
     */
    public static String notNull(String str) {
        return str != null ? str : "";
    }

    /**
     * Returns <code>obj</code> unchanged if it is non-null, otherwise returns
     * the <code>default_value</code>.
     */
    public static <E> E notNull(E obj, E default_value) {
        return obj != null ? obj : default_value;
    }

    /**
     * returns true if the String <code>str</code> is null or an empty string.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.equals("");
    }

    // data checking helpers

    public static boolean testMandatory(Consumer<String> errors, String value, final String field) {
        if ((value != null) && !value.equals("")) {
            // nothing
            return true;
        } else {
            errors.accept(String.format(Strings.util.fieldMandatory, field));
            return false;
        }
    }

    public static int parseInt(Consumer<String> errors, String value, final String field, int oldvalue) {
        int v = oldvalue;
        try {
            if (value.trim().equals("")) value = "0";
            v = Integer.parseInt(value);
        } catch (Exception e) {
            v = oldvalue;
            errors.accept(String.format(Strings.util.invalidInt, field, e.getMessage()));
        }
        return v;
    }

    public static boolean testInt(Consumer<String> errors, int value, final String field, int min, int max) {
        if (value < min) {
            errors.accept(String.format(Strings.util.intTooSmall, field));
            return false;
        }
        if (value > max) {
            errors.accept(String.format(Strings.util.intTooLarge, field));
            return false;
        }
        return true;
    }

    public static long parseLong(Consumer<String> errors, String value, final String field, long oldvalue) {
        long v = oldvalue;
        try {
            if (value.trim().equals("")) value = "0";
            v = Integer.parseInt(value);
        } catch (Exception e) {
            v = oldvalue;
            errors.accept(String.format(Strings.util.invalidInt, field, e.getMessage()));
        }
        return v;
    }

    public static boolean testLong(Consumer<String> errors, long value, final String field, long min, long max) {
        if (value < min) {
            errors.accept(String.format(Strings.util.intTooSmall, field));
            return false;
        }
        if (value > max) {
            errors.accept(String.format(Strings.util.intTooLarge, field));
            return false;
        }
        return true;
    }

    public static int parseInt(Consumer<String> errors, String value, final String field, int oldvalue, int min, int max) {
        int v = parseInt(errors, value, field, oldvalue);
        if (v != oldvalue && !testInt(errors, v, field, min, max)) {
            v = oldvalue;
        }
        return v;
    }

    public static long parseLong(Consumer<String> errors, String value, final String field, long oldvalue, long min, long max) {
        long v = parseLong(errors, value, field, oldvalue);
        if (v != oldvalue && !testLong(errors, v, field, min, max)) {
            v = oldvalue;
        }
        return v;
    }

    public static long parseCurrencyDecimals(Consumer<String> errors, String value, final String field, long oldvalue) {
        if (value == null) {
            errors.accept(String.format(Strings.util.invalidCurrencyAmount, field, "value is null"));
        } else {
            if (!value.matches("^-?[0-9]*[.,][0-9]{2}$")) {
                errors.accept(String.format(Strings.util.invalidCurrencyAmount, field, "must match -?[0-9]*[.,][0-9]{2}, valid examples: -0.99 or 222,16"));
            } else {
                String centValueStr = value.replaceAll("[^0-9-]", "");
                long centValue = Long.parseLong(centValueStr);
                return centValue;
            }
        }
        return oldvalue;
    }

    public static float parseFloat(Consumer<String> errors, String value, final String field, float oldvalue) {
        float v = oldvalue;
        try {
            if (value.trim().equals("")) value = "0";
            v = Float.parseFloat(value);
        } catch (Exception e) {
            errors.accept(String.format(Strings.util.invalidFloat, field, e.getMessage()));
        }
        return v;
    }

    public static boolean testFloat(Consumer<String> errors, float value, final String field, float min, float max) {
        if ((value < min) || (value > max)) {
            errors.accept(String.format(Strings.util.floatBounds, field, min, max));
            return false;
        }
        return true;
    }

    public static float parseFloat(Consumer<String> errors, String value, final String field, float oldvalue, float min, float max) {
        float val = parseFloat(errors, value, field, oldvalue);

        if (!testFloat(errors, val, field, min, max)) {
            val = oldvalue;
        }
        return val;
    }

    public static String formatDate(java.util.Date thedate) {
        return PUBLIC_DATE_FORMAT.get().format(thedate);
    }

    public static String formatDate(IsoDate thedate) {
        return thedate.getPublicFormat();
    }

    private static java.util.Date parseDate(final String value, java.util.Date nullDefault) {
        java.util.Date d = null;
        try {
            d = PUBLIC_DATE_FORMAT.get().parse(value);
        } catch (Exception e) {
            d = nullDefault;
        }
        return d;
    }

    /** Parses the string to a date, returns null on failure. */
    public static java.util.Date parseDate(final String value) {
        return parseDate(value, null);
    }

    /** Parses the string to a date, returns a date object for NOW on failure. */
    public static java.util.Date parseStaticDate(final String value) {
        return parseDate(value, new java.util.Date());
    }

    public static boolean testDate(Consumer<String> errors,
                           java.util.Date thedate, final String field,
                           String minDate, String minMsg,
                           String maxDate, String maxMsg) {

        java.util.Date min = new java.util.Date();
        java.util.Date max = new java.util.Date();
        boolean result = true;
        try {
            min = PUBLIC_DATE_FORMAT.get().parse(minDate);
            max = PUBLIC_DATE_FORMAT.get().parse(maxDate);
        } catch (ParseException e) {
            throw new Error("Dates given to method call are invalid!" + e.getMessage());
        }

        try {
            if (thedate.before(min))
                throw new Exception(minMsg);

            if (thedate.after(max))
                throw new Exception(maxMsg);

        } catch (Exception e) {
            errors.accept(e.getMessage());
            result = false;
        }

        return result;
    }

    public static java.util.Date parseDate(Consumer<String> errors,
                           String value, final String field, java.util.Date oldvalue,
                           String minDate, String minMsg,
                           String maxDate, String maxMsg) {

        java.util.Date thedate = oldvalue;
        String output = "";
        try {
            thedate = PUBLIC_DATE_FORMAT.get().parse(value);

            if (!PUBLIC_DATE_FORMAT.get().format(thedate).equals(value))
                throw new Exception(Strings.util.nonexistantDate);

            if (!testDate(errors, thedate, field, minDate, minMsg, maxDate, maxMsg)) {
                thedate = oldvalue;
            }
        } catch (Exception e) {
            output = String.format(Strings.util.invalidDate, field, value)
                    + Strings.util.dateFormatMessage + ": "
                    + e.getMessage();

            errors.accept(output);

            thedate = oldvalue;
        }

        return thedate;
    }

    public static boolean testEnum(Consumer<String> errors, String input, final String field, String[] values) {
        String allowed = "";
        int i;
        for (i = 0; i < values.length; i++) {
            if (input.equals(values[i])) return true;
            allowed = allowed + ", " + values[i];
        }

        errors.accept(String.format(Strings.util.invalidEnum, field, input, allowed));
        return false;
    }

    public static boolean testEmail(Consumer<String> errors, String input, final String field) {
        boolean result = EmailValidator.isValidEmailAddress(input);
        if (!result) {
            errors.accept(String.format(Strings.util.invalidEmailAddress, field, input));
        }
        return result;
    }

    // Other data conversion helpers

    public static String toDecimals(float f) {
        DecimalFormat format = new DecimalFormat("0.00");
        String res = format.format(f);
        if (res != null) {
          res = res.replace(',', '.'); // damn i8n
        }
        return res;
    }

    public static String toCurrencyDecimals(Long cents) {
        if (cents == null)
            return "UNKNOWN";
        return toDecimals(cents/100f);
    }

    public static float toFloat(String s) {
        DecimalFormat format = new DecimalFormat("#.#");
        float r = 0.00f;

        try {
            r = format.parse(s).floatValue();
        } catch (Exception e) {
            // ignore ...
        }

        return r;
    }

    public static String scriptescape(String input) {
        input = input.replace("\\", "\\u005c");
        input = input.replace("<", "\\u003c");
        input = input.replace(">", "\\u003e");
        input = input.replace("\"", "\\u0022");
        input = input.replace("\'", "\\u0027");
        input = input.replace("&", "\\u0026");
        return input;
    }
}
