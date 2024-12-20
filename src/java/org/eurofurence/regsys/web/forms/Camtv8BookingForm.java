package org.eurofurence.regsys.web.forms;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.persistence.DbException;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.payments.PaymentService;
import org.eurofurence.regsys.repositories.payments.Transaction;
import org.eurofurence.regsys.service.TransactionCalculator;
import org.eurofurence.regsys.service.camtv8.Entry;
import org.eurofurence.regsys.web.pages.Camtv8ImportPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  Represents the form used to book payments based on a parsed Camtv8 entry list.
 */
public class Camtv8BookingForm extends Form {

    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String BOOKINGFORM = "bookingform";
    public static final String PERFORM = "perform"; // followed by _ and line number
    public static final String BADGENO = "badgeno"; // followed by _ and line number

    // ------------ attributes -----------------------

    private List<Entry> entries = new ArrayList<>();
    private List<AttendeeSearchResultList.AttendeeSearchResult> attendeeList = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private Transaction transaction = new Transaction();

    private final PaymentService paymentService = new PaymentService();
    private final TransactionCalculator calculator = new TransactionCalculator();

    // ------------ constructors -----------------------

    public Camtv8BookingForm() {
    }

    public void initialize() {
        entries = new ArrayList<>();
        attendeeList = new ArrayList<>();
        transactions = new ArrayList<>();
        transaction = new Transaction();
    }

    // ---------- proxy methods for entity access -------

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    // ---------- business methods ---------

    public void loadUnpaidAttendees() throws DbException {
        AttendeeSearchCriteria criteria = new AttendeeSearchCriteria();
        AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
        criterion.status = Arrays.asList(Constants.MemberStatus.APPROVED.newRegsysValue(), Constants.MemberStatus.PARTIALLY_PAID.newRegsysValue());
        criteria.matchAny.add(criterion);
        criteria.fillFields = Arrays.asList("nickname", "first_name", "last_name", "total_dues", "payment_balance", "current_dues", "due_date");
        AttendeeSearchResultList attendeeResult = getPage().getAttendeeService().performFindAttendees(criteria, getPage().getTokenFromRequest(), getPage().getRequestId());

        if (attendeeResult.attendees == null || attendeeResult.attendees.isEmpty()) {
            throw new DbException(Strings.camtv8ImportPage.noAttendeesReceived);
        }

        attendeeList = attendeeResult.attendees;
    }

    private boolean informationBadgeNo(List<String> information, long badgeNo) {
        if (information == null || information.isEmpty()) {
            return false;
        }
        String info = String.join(" ", information);

        String[] words = info.split("\\W+");
        return Arrays.stream(words).anyMatch(w -> w.matches("^0*" + badgeNo + "$"));
    }

    private double informationNicknameScore(List<String> information, String nickname) {
        if (information == null || information.isEmpty() || nickname == null || nickname.isEmpty()) {
            return 0d;
        }
        String info = String.join(" ", information);

        final double[] bestScore = {0d};
        int nicknameLength = nickname.length();
        String[] words = info.split("\\W+");
        Arrays.stream(words)
                .forEach(w -> {
                    int maxLength = w.length();
                    if (maxLength < nicknameLength) {
                        maxLength = nicknameLength;
                    }
                    double score = (double) (maxLength - LevenshteinDistance.getDefaultInstance().apply(w, nickname)) / maxLength;
                    if (score > bestScore[0]) {
                        bestScore[0] = score;
                    }
                });
        return bestScore[0];
    }

    public void matchup() {
        entries.forEach(entry -> {
            int bestMatch = -1;
            double bestScore = 0d;

            for (int i = 0; i < attendeeList.size(); i++) {
                AttendeeSearchResultList.AttendeeSearchResult a = attendeeList.get(i);
                if (a.currentDues == entry.amount) {
                    double score = 0d;
                    if (informationBadgeNo(entry.information, a.id))
                        score += 110d;
                    score += 50 * informationNicknameScore(entry.information, a.nickname);
                    if (entry.debitorName.contains(a.lastName))
                        score += 75d;
                    if (entry.debitorName.contains(a.firstName))
                        score += 30d;

                    if (score > bestScore && score > 100d) {
                        bestMatch = i;
                        bestScore = score;
                    }
                }
            }

            if (bestMatch != -1) {
                entry.matchPosition = bestMatch;
            }
        });
    }

    public void loadTransactionsForAttendee(long the_id) throws DbException {
        if (the_id <= 0) throw new DbException(Strings.camtv8ImportPage.internalNeedId);

        // load info from payment service rather than relying on attendee service cache
        calculator.resetCache();
        calculator.loadTransactionsFor(the_id, getPage().getTokenFromRequest(), getPage().getRequestId());

        transactions = calculator.getTransactions();
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

    private String shorten(String full, int maxLen) {
        if (full == null) return "";
        if (full.length() > maxLen) return full.substring(0, maxLen) + "...";
        return full;
    }

    public List<String[]> getEntriesWebListing() {
        List<String[]> result = new ArrayList<>();
        for (int i=0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            String[] r = new String[14];
            r[0] = FormHelper.toCurrencyDecimals(entry.amount);
            r[1] = escape(entry.currency);
            r[2] = escape(entry.valuationDate);

            r[3] = escape(shorten(entry.debitorName, 30));
            r[4] = escape(entry.debitorName);

            String information = String.join(" ", entry.information);
            r[5] = escape(shorten(information, 50));
            r[6] = escape(information);

            if (entry.matchPosition >= 0) {
                r[7] = "yes";
                AttendeeSearchResultList.AttendeeSearchResult a = attendeeList.get(entry.matchPosition);
                r[8] = Long.toString(a.id);

                String name = a.firstName + " " + a.lastName;
                r[9] = escape(shorten(name, 30));
                r[10] = escape(name);
                r[11] = escape(shorten(a.nickname, 30));
                r[12] = escape(a.nickname);
                r[13] = Form.checkbox(true, PERFORM + "_" + i, "book", "", "check")
                      + Form.hiddenField(BADGENO + "_" + i, Long.toString(a.id));
            } else {
                r[7] = "no";
                r[8] = "";
                r[9] = "";
                r[10] = "";
                r[11] = "";
                r[12] = "";
                r[13] = "";
            }
            result.add(r);
        }
        return result;
    }

    public String getFormFooter() {
        return "</FORM>";
    }
}
