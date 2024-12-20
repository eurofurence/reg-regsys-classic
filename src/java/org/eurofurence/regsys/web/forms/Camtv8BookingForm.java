package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.service.camtv8.Entry;
import org.eurofurence.regsys.web.pages.Camtv8ImportPage;

import java.util.ArrayList;
import java.util.List;

/**
 *  Represents the form used to book payments based on a parsed Camtv8 entry list.
 */
public class Camtv8BookingForm extends Form {

    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String BOOKINGFORM = "bookingform";

    // ------------ attributes -----------------------

    private List<Entry> entries = new ArrayList<>();

    // ------------ constructors -----------------------

    public Camtv8BookingForm() {
    }

    public void initialize() {
        entries = new ArrayList<>();
    }

    // ---------- proxy methods for entity access -------

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    // ---------- business methods ---------

    public void matchup() {
        // TODO
    }

    public void processBooking() {
        // TODO
    }

    // --------------------- form output methods ------------------------------------------------

    public String getFormHeader() {
        String result = "";

        result += "<FORM id='bookingform' name='" + BOOKINGFORM + "' action='camtv8' method='POST' accept-charset='UTF-8'>\n";
        result += "    " + Form.hiddenField(Camtv8ImportPage.MODE, Camtv8ImportPage.MODE_BOOK) + "\n";

        return result;
    }

    public String getFormSubmitButton(String caption, String style) {
        return "<INPUT ID='bookingsubmit' TYPE='SUBMIT' VALUE='" + escape(caption) + "' CLASS='" + escape(style) + "'/>";
    }

    public List<String[]> getEntriesWebListing() {
        List<String[]> result = new ArrayList<>();
        for (Entry entry : entries) {
            String[] r = new String[6];
            r[0] = FormHelper.toCurrencyDecimals(entry.amount);
            r[1] = escape(entry.currency);
            r[2] = escape(entry.bookingDate);
            r[3] = escape(entry.valuationDate);
            r[4] = escape(entry.debitorName);
            r[5] = escape(String.join(" ", entry.information));
            result.add(r);
        }
        return result;
    }

    public String getFormFooter() {
        return "</FORM>";
    }
}
