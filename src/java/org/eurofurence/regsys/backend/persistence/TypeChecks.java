package org.eurofurence.regsys.backend.persistence;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eurofurence.regsys.web.forms.Util;
import org.eurofurence.regsys.backend.types.IsoDate;

public class TypeChecks {
    // data checking helpers - throw error messages as DbDataException

    /*
     * Auxiliary code. Instead of simply writing
     *
     *    [return] Util.testMandatory(msg -> { throw new DbDataException(msg); }, value, field);
     *
     * you need to write
     *
     *    [return (ReturnType)] ErrorHandlingHelper.performTest(h -> Util.testMandatory(h, value, field));
     *
     * because java insists on a throws clause around the invoked method if a passed in lambda
     * throws a certain exception...
     *
     * But we really do not want to duplicate the validation functions in Util...
     */
    private static class ErrorHandlingHelper implements Consumer<String> {
        String message = null;

        @Override
        public void accept(String t) {
            message = t;
        }

        public void raiseIfError() throws DbDataException {
            if (message != null) throw new DbDataException(message);
        }

        public static Object performTest(Function<ErrorHandlingHelper, Object> theTest) throws DbDataException {
            ErrorHandlingHelper errHandler = new ErrorHandlingHelper();
            Object result = theTest.apply(errHandler);
            errHandler.raiseIfError();
            return result;
        }
    }

    public static void testMandatory(String value, final String field) throws DbDataException {
        ErrorHandlingHelper.performTest(h -> Util.testMandatory(h, value, field));
    }

    public static void testInt(int value, final String field, int min, int max) throws DbDataException {
        ErrorHandlingHelper.performTest(h -> Util.testInt(h, value, field, min, max));
    }

    public static void testFloat(float value, final String field, float min, float max) throws DbDataException {
        ErrorHandlingHelper.performTest(h -> Util.testFloat(h, value, field, min, max));
    }

    /** Parses the string to a date, returns a date object for NOW on failure. */
    public static java.util.Date parseStaticDate(final String value) {
        return Util.parseStaticDate(value);
    }

    public static String formatDate(java.util.Date thedate) {
        return thedate != null ? Util.formatDate(thedate) : null;
    }

    public static String formatDateNillable(java.util.Date thedate) {
        return thedate != null ? Util.formatDate(thedate) : "";
    }

    public static String formatDate(IsoDate thedate) {
        return thedate != null ? Util.formatDate(thedate) : null;
    }

    public static String formatDateNillable(IsoDate thedate) {
        return thedate != null ? Util.formatDate(thedate) : "";
    }


    public static void testDate(java.util.Date thedate, final String field,
                           String minDate, String minMsg,
                           String maxDate, String maxMsg) throws DbDataException {
        ErrorHandlingHelper.performTest(h -> Util.testDate(h, thedate, field, minDate, minMsg, maxDate, maxMsg));
    }

    /** Parses the string to a date, returns null on failure. */
    public static java.util.Date parseDate(final String value) {
        return Util.parseDate(value);
    }

    public static java.util.Date parseDate(
                           String value, final String field,
                           String minDate, String minMsg,
                           String maxDate, String maxMsg) throws DbDataException {
        return (java.util.Date) ErrorHandlingHelper.performTest(h -> Util.parseDate(h, value, field, null, minDate, minMsg, maxDate, maxMsg));
    }

    public static void testEnum(String input, final String field, String[] values) throws DbDataException {
        ErrorHandlingHelper.performTest(h -> Util.testEnum(h, input, field, values));
    }

    public static void testEmail(String input, final String field) throws DbDataException {
        ErrorHandlingHelper.performTest(h -> Util.testEmail(h, input, field));
    }
}
