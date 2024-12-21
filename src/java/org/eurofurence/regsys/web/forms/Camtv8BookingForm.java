package org.eurofurence.regsys.web.forms;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.persistence.DbException;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;
import org.eurofurence.regsys.repositories.payments.PaymentService;
import org.eurofurence.regsys.repositories.payments.Transaction;
import org.eurofurence.regsys.service.TransactionCalculator;
import org.eurofurence.regsys.service.camtv8.Entry;
import org.eurofurence.regsys.web.pages.Camtv8ImportPage;

import java.util.*;

/**
 *  Represents the form used to book payments based on a parsed Camtv8 entry list.
 */
public class Camtv8BookingForm extends Form {

    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String BOOKINGFORM = "bookingform";
    public static final String PERFORM = "perform"; // followed by _ and line number
    public static final String BADGENO = "badgeno"; // followed by _ and line number

    public static final String PERFORM_VALUE = "book";

    // ------------ attributes -----------------------

    private List<Entry> entries = new ArrayList<>();
    private List<AttendeeSearchResultList.AttendeeSearchResult> attendeeList = new ArrayList<>();

    private List<Transaction> transactions = new ArrayList<>();
    private Transaction transaction = new Transaction();
    private Attendee attendee = new Attendee();
    private List<String[]> bookingLog = new ArrayList<>();

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
        bookingLog = new ArrayList<>();
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

    private boolean containsIgnoringCase(String haystack, String needle) {
        if (haystack == null || needle == null) {
            return false;
        }
        return haystack.toLowerCase().contains(needle.toLowerCase());
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
                        score += 90d;
                    score += 50 * informationNicknameScore(entry.information, a.nickname);
                    if (containsIgnoringCase(entry.debitorName, a.lastName))
                        score += 65d;
                    if (containsIgnoringCase(entry.debitorName, a.firstName))
                        score += 20d;

                    if (score > bestScore && score > 100d) {
                        bestMatch = i;
                        bestScore = score;
                    }
                }
            }

            // we need to overwrite, attendee list may have changed since getting entries from session
            entry.matchPosition = bestMatch;
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
        Map<Integer,Long> badgeNumberToBookFromEntries = new TreeMap<>(); // entry position in entries -> badge number

        // extract badgeNumberToBookFromEntries from request parameters
        Map<String,String[]> parameters = getPage().getRequest().getParameterMap();
        parameters.entrySet().stream()
                .filter(e -> e.getKey().startsWith(PERFORM + "_"))
                .filter(e -> e.getValue() != null && Arrays.asList(e.getValue()).contains(PERFORM_VALUE))
                .forEach(e -> {
                    String keyNumber = e.getKey().replace(PERFORM + "_", "");
                    try {
                        Integer key = Integer.parseInt(keyNumber);

                        String[] badgeRaw = parameters.get(BADGENO + "_" + keyNumber);
                        if (badgeRaw != null && badgeRaw.length > 0) {
                            long value = Long.parseLong(badgeRaw[0]);

                            if (value > 0) {
                                badgeNumberToBookFromEntries.put(key, value);
                            } else {
                                addError("invalid perform or badgeno parameter '" + e.getKey() + "': invalid badge number");
                            }
                        }
                    } catch (NumberFormatException ex) {
                        addError("invalid perform or badgeno parameter '" + e.getKey() + "': " + ex.getMessage());
                    }
                });


        badgeNumberToBookFromEntries.forEach((key, value) -> {
            String[] protocol = new String[7];
            try {
                loadTransactionsForAttendee(value);
                protocol[0] = Integer.toString(key + 1); // row number

                Entry entry = entries.get(key);
                protocol[1] = FormHelper.toCurrencyDecimals(entry.amount);

                protocol[2] = Long.toString(value); // badge number

                attendee = getPage().getAttendeeService().performGetAttendee(value, getPage().getTokenFromRequest(), getPage().getRequestId());
                protocol[3] = escape(shorten(attendee.nickname, 30));
                protocol[4] = escape(attendee.nickname);

                transaction = new Transaction(); // new transaction as fallback
                transaction.transactionType = Transaction.TransactionType.PAYMENT.getValue();
                transaction.method = Transaction.Method.TRANSFER.getValue();
                protocol[5] = "new tx";

                // first check if we have a matching _tentative_ payment, if so, we can use its id (will also keep the paylink intact)
                transactions.forEach(t -> {
                    if (Transaction.TransactionType.PAYMENT.getValue().equals(t.transactionType)
                            && Transaction.Method.TRANSFER.getValue().equals(t.method)
                            && Transaction.Status.TENTATIVE.getValue().equals(t.status)) {
                        transaction = t;
                        protocol[5] = "tentative " + t.transactionIdentifier;
                    }
                });
                // now check if we have a matching _pending_ payment, if so, use its id (prefer over tentative)
                transactions.forEach(t -> {
                    if (Transaction.TransactionType.PAYMENT.getValue().equals(t.transactionType)
                        && Transaction.Method.TRANSFER.getValue().equals(t.method)
                        && Transaction.Status.PENDING.getValue().equals(t.status)) {
                        transaction = t;
                        protocol[5] = "pending " + t.transactionIdentifier;
                    }
                });

                // if existing, update to valid
                transaction.status = Transaction.Status.VALID.getValue();
                transaction.amount.grossCent = entry.amount;
                transaction.amount.currency = "EUR";
                transaction.comment = entry.debitorAccount + " " + String.join(" ", entry.information);
                transaction.effectiveDate = entry.valuationDate;

                if (transaction.transactionIdentifier == null || "".equals(transaction.transactionIdentifier)) {
                    paymentService.performCreateTransaction(transaction, getPage().getTokenFromRequest(), getPage().getRequestId());
                } else {
                    paymentService.performUpdateTransaction(transaction.transactionIdentifier, transaction, getPage().getTokenFromRequest(), getPage().getRequestId());
                }

                protocol[6] = "success";

                bookingLog.add(protocol);
            } catch (Exception e) {
                addError("FAILED to process transactions for badge number "+value+": " + e.getMessage());
            }
        });
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

    public String getFormFooter() {
        return "</FORM>";
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
                r[13] = Form.checkbox(true, PERFORM + "_" + i, PERFORM_VALUE, "", "check")
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

    public List<String[]> getBookingLog() {
        return bookingLog;
    }
}
