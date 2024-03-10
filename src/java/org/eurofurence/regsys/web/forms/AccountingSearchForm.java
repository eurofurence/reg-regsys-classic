package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;
import org.eurofurence.regsys.repositories.payments.PaymentService;
import org.eurofurence.regsys.repositories.payments.Transaction;
import org.eurofurence.regsys.repositories.payments.TransactionResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountingSearchForm extends Form {

    // ------------ parameter constants -----------------------

    public static final String PARAM_SEARCH_FROM     = "searchFrom";
    public static final String PARAM_SEARCH_TO       = "searchTo";
    public static final String PARAM_SEARCH_COMMENTS = "searchComments";
    public static final String PARAM_SEARCH_STATUS   = "searchPaymentStatus";
    public static final String PARAM_SEARCH_TYPE     = "searchPaymentType";
    public static final String PARAM_SEARCH_METHOD   = "searchPaymentMethod";

    // ------------ attributes -----------------------

    private String searchFrom; // kept as iso - from
    private String searchTo; // kept as iso - until before
    private String searchComments;
    private Set<String> searchStatus;
    private Set<String> searchTypes;
    private Set<String> searchMethods;

    private int count = -1; // before find()

    private Map<String,Map<Long,Long>> breakdownCountByAmount;
    private Map<String,Long> totalAmounts;

    public static class AttendeeInfos {
        public String nickname;
        public String flags;
        public String options;
        public String packages;
        public String status;
        public long totalDues;
        public long paymentBalance;
        public long currentDues;
        public String dueDate;
    }

    private Map<Long, AttendeeInfos> attendeeInfos = new HashMap<>();

    private List<Transaction> found = new ArrayList<>();

    // ------------ constructors and initialization -----------------------

    private final PaymentService paymentService = new PaymentService();
    private final AttendeeService attendeeService = new AttendeeService();

    public AccountingSearchForm() {
        breakdownCountByAmount = new TreeMap<>();
        breakdownCountByAmount.put(Transaction.TransactionType.DUE.getValue(), new TreeMap<>());
        breakdownCountByAmount.put(Transaction.TransactionType.PAYMENT.getValue(), new TreeMap<>());
        totalAmounts = new TreeMap<>();
        totalAmounts.put(Transaction.TransactionType.DUE.getValue(), 0L);
        totalAmounts.put(Transaction.TransactionType.PAYMENT.getValue(), 0L);
    }

    private void registerInBreakdownAndTotals(Transaction t) {
        Long amount = 0L;
        if (t.amount != null) {
            amount = t.amount.grossCent;
        }

        try {
            Long currentCount = 0L;
            Map<Long,Long> br = breakdownCountByAmount.get(t.transactionType);
            if (br.containsKey(amount)) {
                currentCount = br.get(amount);
            }
            currentCount++;
            br.put(amount, currentCount);

            Long currentTotal = totalAmounts.get(t.transactionType);
            currentTotal += amount;
            totalAmounts.put(t.transactionType, currentTotal);
        } catch (Exception e) {
            addError("internal error, skipped transaction with type " + t.transactionType);
        }
    }

    public void initialize() {
        // toDate default = 1st of this month
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int mon = cal.get(Calendar.MONTH);
        cal.set(year, mon, 1);

        Date toDate = cal.getTime();

        // fromDate default = 1st of previous month
        mon--;
        if (mon < 0) {
            mon += 12;
            year--;
        }
        cal.set(year, mon, 1);

        Date fromDate = cal.getTime();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        searchFrom = format.format(fromDate);
        searchTo = format.format(toDate);
        searchComments = "";
        searchStatus = Stream.of(Transaction.Status.VALID.getValue())
                .collect(Collectors.toSet());
        searchTypes = Arrays.stream(Transaction.TransactionType.values())
                .map(Transaction.TransactionType::getValue)
                .collect(Collectors.toSet());
        searchMethods = Arrays.stream(Transaction.Method.values())
                .map(Transaction.Method::getValue)
                .collect(Collectors.toSet());
        count = -1;
    }

    // ---------- proxy methods for entity access -------

    // --------- Business methods ----------------------

    public boolean find() {
        boolean result = false;
        found = new ArrayList<>();
        try {
            populateAttendeeInfos();
            loadTransactions();
            result = true;
            count = -1; // first loadNextTransaction() advances to first
        } catch (DownstreamWebErrorException e) {
            resetErrors(e.getMessage());
            addWebErrors(e.getErr());
        } catch (DownstreamException e) {
            resetErrors(e.getMessage());
        }
        return result;
    }

    private void populateAttendeeInfos() {
        AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
        AttendeeSearchCriteria criteria = new AttendeeSearchCriteria();
        criteria.fillFields = Arrays.asList("nickname", "configuration", "balances", "status");
        criteria.matchAny.add(criterion); // note: will filter deleted attendees

        AttendeeSearchResultList resultList = attendeeService.performFindAttendees(criteria, getPage().getTokenFromRequest(), getPage().getRequestId());
        if (resultList != null && resultList.attendees != null) {
            resultList.attendees.forEach(sr -> {
                AttendeeInfos inf = new AttendeeInfos();
                inf.nickname = sr.nickname;
                inf.flags = sr.flags;
                inf.options = sr.options;
                inf.packages = sr.packages;
                inf.status = sr.status;
                inf.totalDues = valueOrZero(sr.totalDues);
                inf.paymentBalance = valueOrZero(sr.paymentBalance);
                inf.currentDues = valueOrZero(sr.currentDues);
                inf.dueDate = sr.dueDate;
                attendeeInfos.put(sr.id, inf);
            });
        }
    }

    private void loadTransactions() {
        TransactionResponse response = paymentService.performFindTransactions(null, null, searchFrom, searchTo, getPage().getTokenFromRequest(), getPage().getRequestId());
        if (response.payload != null) {
            for (Transaction t : response.payload) {
                if (searchStatus.contains(t.status) &&
                        searchTypes.contains(t.transactionType) &&
                        searchMethods.contains(t.method)) {
                    if (searchComments.equals("") || t.comment != null && t.comment.contains(searchComments)) {
                        found.add(t);
                    }
                }
            }
        }
    }

    private long valueOrZero(Long value) {
        if (value == null) {
            return 0L;
        }
        return value.longValue();
    }

    public boolean loadNextTransaction() {
        count++;
        if (count < found.size()) {
            registerInBreakdownAndTotals(found.get(count));
            return true;
        } else {
            return false;
        }
    }

    // --------------------- parameter parsers --------------------------------------------------

    private String nvl(String v) {
        return v == null ? "" : v;
    }

    private Transaction.Status byStatusValue(String value) {
        for (Transaction.Status option: Transaction.Status.values()) {
            if (option.getValue().equals(value)) {
                return option;
            }
        }
        return Transaction.Status.VALID;
    }

    private Transaction.TransactionType byTypeValue(String value) {
        for (Transaction.TransactionType option: Transaction.TransactionType.values()) {
            if (option.getValue().equals(value)) {
                return option;
            }
        }
        return null;
    }

    private Transaction.Method byMethodValue(String value) {
        for (Transaction.Method option: Transaction.Method.values()) {
            if (option.getValue().equals(value)) {
                return option;
            }
        }
        return null;
    }

    private String tryParseOrNull(SimpleDateFormat inFormat, SimpleDateFormat outFormat, String value) {
        try {
            return outFormat.format(inFormat.parse(value));
        } catch (ParseException e) {
            return null;
        }
    }

    private String isoFromHuman(String input, String defaultValue) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        String attempt = tryParseOrNull(isoFormat, isoFormat, input);
        if (attempt != null)
            return attempt;

        SimpleDateFormat deFormat = new SimpleDateFormat("dd.MM.yyyy");
        attempt = tryParseOrNull(deFormat, isoFormat, input);
        if (attempt != null)
            return attempt;

        SimpleDateFormat usFormat = new SimpleDateFormat("MM/dd/yyyy");
        attempt = tryParseOrNull(usFormat, isoFormat, input);
        if (attempt != null)
            return attempt;

        return defaultValue;
    }

    private String humanFromIso(String iso) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat deFormat = new SimpleDateFormat("dd.MM.yyyy");
        String attempt = tryParseOrNull(isoFormat, deFormat, iso);
        if (attempt != null)
            return attempt;

        return "";
    }

    /**
     * This inner class encapsules all functions that are provided by this form for parsing request parameters
     * - keeps some structure to the main class, separating this concern out from the regular business methods
     *   until they can eventually be moved into activities
     * - folds away nicely if not needed
     */
    public class ParameterParser {
        public boolean parseFormParams(HttpServletRequest request) {
            if (request.getParameter(PARAM_SEARCH_FROM) != null) {
                searchFrom = isoFromHuman(nvl(request.getParameter(PARAM_SEARCH_FROM)), searchFrom);
                searchTo = isoFromHuman(nvl(request.getParameter(PARAM_SEARCH_TO)), searchTo);
                searchComments = nvl(request.getParameter(PARAM_SEARCH_COMMENTS));
                searchStatus = Arrays.stream(request.getParameterValues(PARAM_SEARCH_STATUS)).collect(Collectors.toSet());
                searchTypes = Arrays.stream(request.getParameterValues(PARAM_SEARCH_TYPE)).collect(Collectors.toSet());
                searchMethods = Arrays.stream(request.getParameterValues(PARAM_SEARCH_METHOD)).collect(Collectors.toSet());
                return !hasErrors();
            }
            return false;
        }
    }

    public ParameterParser getParameterParser() {
        return new ParameterParser();
    }

    // --------------------- form permission methods ------------------------------------------------

    // --------------------- form output methods ------------------------------------------------

    /**
     * This inner class encapsules all functions that are provided by this form for velocity templates
     * - avoids calling any methods not listed here from Velocity
     * - folds away nicely if not needed
     */
    public class VelocityRepresentation {
        // urls

        // properties

        // exposed navigation

        public boolean loadNext() {
            return loadNextTransaction();
        }

        // attributes for the current payment

        public String getCount() {
            return Integer.toString(count+1);
        }

        public String getAmount() {
            if (Transaction.TransactionType.PAYMENT.getValue().equals(found.get(count).transactionType)) {
                Transaction.Amount amount = found.get(count).amount;
                if (amount != null) {
                    return Util.toDecimals(amount.grossCent / 100.0f);
                } else {
                    return "???";
                }
            } else {
                return "&nbsp;";
            }
        }

        public String getAmountDue() {
            if (Transaction.TransactionType.DUE.getValue().equals(found.get(count).transactionType)) {
                Transaction.Amount amount = found.get(count).amount;
                if (amount != null) {
                    return Util.toDecimals(amount.grossCent / 100.0f);
                } else {
                    return "???";
                }
            } else {
                return "&nbsp;";
            }
        }

        public String getReceived() {
            return escape(found.get(count).effectiveDate);
        }

        public String getDebitorId() {
            Long debitorId = found.get(count).debitorId;
            if (debitorId != null) {
                return Long.toString(debitorId);
            } else {
                return "null";
            }
        }

        public String getStatus() {
            return escape(found.get(count).status);
        }

        public String getType() {
            return escape(found.get(count).transactionType);
        }

        public String getMethod() {
            return escape(found.get(count).method);
        }

        public String getComment() {
            return escape(found.get(count).comment);
        }

        // lookups from attendeeInfos

        private AttendeeInfos attInf() {
            Long debitorId = found.get(count).debitorId;
            if (debitorId != null) {
                AttendeeInfos inf = attendeeInfos.get(debitorId);
                if (inf == null) {
                    inf = new AttendeeInfos();
                    inf.nickname = "(unknown)";
                }
                return inf;
            } else {
                AttendeeInfos inf = new AttendeeInfos();
                inf.nickname = "(no id)";
                return inf;
            }
        }

        public String getAttendeeNickname() {
            return escape(attInf().nickname);
        }

        public boolean attendeeHasFlag(String flag) {
            AttendeeInfos inf = attInf();
            String flags = "," + inf.flags + ","; // may result in ,null,
            return flags.contains("," + flag + ",");
        }

        // attributes for the amount breakdown (available after all loadNexts only)

        public List<Map<String,String>> getDueAmountsBreakdown() {
            return breakdownCountByAmount.get(Transaction.TransactionType.DUE.getValue()).entrySet().stream()
                    .map(e -> {
                        Map<String,String> constructed = new HashMap<>();
                        constructed.put("amount", Util.toCurrencyDecimals(e.getKey()));
                        constructed.put("count", Long.toString(e.getValue()));
                        return constructed;
                    }).collect(Collectors.toList());
        }

        public List<Map<String,String>> getPayAmountsBreakdown() {
            return breakdownCountByAmount.get(Transaction.TransactionType.PAYMENT.getValue()).entrySet().stream()
                    .map(e -> {
                        Map<String,String> constructed = new HashMap<>();
                        constructed.put("amount", Util.toCurrencyDecimals(e.getKey()));
                        constructed.put("count", Long.toString(e.getValue()));
                        return constructed;
                    }).collect(Collectors.toList());
        }

        public String getDueGrandTotal() {
            return Util.toCurrencyDecimals(totalAmounts.get(Transaction.TransactionType.DUE.getValue()));
        }

        public String getPayGrandTotal() {
            return Util.toCurrencyDecimals(totalAmounts.get(Transaction.TransactionType.PAYMENT.getValue()));
        }

        // form fields

        public String fieldFrom(String style) {
            return textField(true, PARAM_SEARCH_FROM, humanFromIso(searchFrom), 10, 10, style);
        }

        public String fieldTo(String style) {
            return textField(true, PARAM_SEARCH_TO, humanFromIso(searchTo), 10, 10, style);
        }

        public String fieldComments(String style) {
            return textField(true, PARAM_SEARCH_COMMENTS, searchComments, 30, 30, style);
        }

        public String fieldStatus(String style) {
            List<String> valuesList = Arrays.stream(Transaction.Status.values()).
                    map(Transaction.Status::getValue).
                    collect(Collectors.toList());
            return selector( true, PARAM_SEARCH_STATUS,
                    valuesList,
                    valuesList,
                    searchStatus, 4, true, style, "");
        }

        public String fieldTypes(String style) {
            List<String> valuesList = Arrays.stream(Transaction.TransactionType.values()).
                    map(Transaction.TransactionType::getValue).
                    collect(Collectors.toList());
            return selector( true, PARAM_SEARCH_TYPE,
                    valuesList,
                    valuesList,
                    searchTypes, 2, true, style, "");
        }

        public String fieldMethods(String style) {
            List<String> valuesList = Arrays.stream(Transaction.Method.values()).
                    map(Transaction.Method::getValue).
                    collect(Collectors.toList());
            return selector( true, PARAM_SEARCH_METHOD,
                    valuesList,
                    valuesList,
                    searchMethods, 5, true, style, "");
        }
    }

    public VelocityRepresentation getVelocityRepresentation() {
        return new VelocityRepresentation();
    }
}
