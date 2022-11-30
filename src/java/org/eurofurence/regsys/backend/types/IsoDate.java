package org.eurofurence.regsys.backend.types;

import org.eurofurence.regsys.backend.Strings;

import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 *  Yes this is our own date class.
 *
 *  We have had too many problems with java.util.Date and various mysql driver versions under high load. Apparently,
 *  something in the mysql driver isn't thread safe.
 *  Error messages included stuff like 'invalid date 1998-02-30', even though we were passing in java.sql.Date, which
 *  cannot even take this value.
 *
 *  Now we read and write dates from/to mysql as Strings in ISO format, yyyy-MM-dd, but we still want to be type safe.
 *  Hence this class.
 */
public class IsoDate {
    private String value;

    public IsoDate() {
        value = formatIsoDate(new java.util.Date());
    }

    public IsoDate fromPublicFormat(String value) {
        this.value = formatIsoDate(parsePublicDate(value, new java.util.Date()));
        return this;
    }

    public IsoDate fromIsoFormat(String value) {
        this.value = formatIsoDate(parseIsoDate(value, new java.util.Date()));
        return this;
    }

    public IsoDate fromDate(java.util.Date date) {
        value = formatIsoDate(date);
        return this;
    }

    public java.util.Date getAsDate() {
        return parseIsoDate(value, new java.util.Date());
    }

    public String getPublicFormat() {
        return formatPublicDate(parseIsoDate(value, new java.util.Date()));
    }

    public String getIsoFormat() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IsoDate isoDate = (IsoDate) o;
        return Objects.equals(value, isoDate.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    // static conversion thread locals

    private static final ThreadLocal<SimpleDateFormat> PUBLIC_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat(Strings.conf.dateFormat));

    private static final ThreadLocal<SimpleDateFormat> ISO_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    // helper functions

    private java.util.Date parsePublicDate(final String value, java.util.Date nullDefault) {
        return parseDateWithFormat(PUBLIC_DATE_FORMAT.get(), value, nullDefault);
    }

    private java.util.Date parseIsoDate(final String value, java.util.Date nullDefault) {
        return parseDateWithFormat(ISO_DATE_FORMAT.get(), value, nullDefault);
    }

    private java.util.Date parseDateWithFormat(final SimpleDateFormat dateFormatter,
                                               final String value,
                                               final java.util.Date nullDefault) {
        if (value == null) {
            // expected for things like "reported pay date"
            return nullDefault;
        }
        try {
            return dateFormatter.parse(value);
        } catch (Exception e) {
            System.out.println("Error in IsoDate.parseDateWithFormat(" + value + "), using default value");
            e.printStackTrace();
            return nullDefault;
        }
    }

    private String formatPublicDate(java.util.Date value) {
        return PUBLIC_DATE_FORMAT.get().format(value);
    }

    private String formatIsoDate(java.util.Date value) {
        return ISO_DATE_FORMAT.get().format(value);
    }
}
