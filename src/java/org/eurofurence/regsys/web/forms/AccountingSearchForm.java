package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;
import org.eurofurence.regsys.repositories.payments.PaymentService;
import org.eurofurence.regsys.repositories.payments.Transaction;
import org.eurofurence.regsys.repositories.payments.TransactionResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
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

    public static final String PARAM_LONG_VERSION     = "long";

    // ------------ attributes -----------------------

    private String searchFrom;
    private String searchTo;
    private String searchComments;
    private Set<String> searchStatus;
    private Set<String> searchTypes;
    private Set<String> searchMethods;

    private int count = -1; // before find()

    float totalSum = 0.00f;
    float attendanceSum = 0.00f;
    float membershipSum = 0.00f;

    private List<Transaction> found = new ArrayList<>();

    // ------------ constructors and initialization -----------------------

    private final PaymentService paymentService = new PaymentService();

    public AccountingSearchForm() {
    }

    public void initialize() {
        searchFrom = "";
        searchTo = "";
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
            result = true;
            count = -1; // first loadNext() advances to first
        } catch (DownstreamWebErrorException e) {
            resetErrors(e.getMessage());
            addWebErrors(e.getErr());
        } catch (DownstreamException e) {
            resetErrors(e.getMessage());
        }
        return result;
    }

    public boolean loadNext() {
        count++;
        if (count < found.size()) {
            // TODO <% totalSum += payment.getamountFloat(); %>
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

    /**
     * This inner class encapsules all functions that are provided by this form for parsing request parameters
     * - keeps some structure to the main class, separating this concern out from the regular business methods
     *   until they can eventually be moved into activities
     * - folds away nicely if not needed
     */
    public class ParameterParser {
        public boolean parseFormParams(HttpServletRequest request) {
            if (request.getParameter(PARAM_SEARCH_FROM) != null) {
                searchFrom = nvl(request.getParameter(PARAM_SEARCH_FROM));
                searchTo = nvl(request.getParameter(PARAM_SEARCH_TO));
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

        // attributes for the current payment

        public String getCount() {
            return Integer.toString(count+1);
        }

        public String getAmount() {
            Transaction.Amount amount = found.get(count).amount;
            if (amount != null) {
                return Util.toDecimals(amount.grossCent/100.0f);
            } else {
                return "null";
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

        // attributes for the amount breakdown (available after all loadNexts only)

        public List<Map<String,String>> getAmountsBreakdown() {
            SortedMap<Long, Integer> countsByAmount = new TreeMap<>();
            for (Transaction t: found) {
                if (t.amount != null && t.amount.grossCent != null) {
                    if (countsByAmount.containsKey(t.amount.grossCent)) {
                        Integer current = countsByAmount.get(t.amount.grossCent);
                        countsByAmount.put(t.amount.grossCent, current+1);
                    } else {
                        countsByAmount.put(t.amount.grossCent, 1);
                    }
                }
            }

            return countsByAmount.entrySet().stream()
                    .map(e -> {
                        Map<String,String> constructed = new HashMap<>();
                        constructed.put("amount", Util.toDecimals(e.getKey()/100.0f));
                        constructed.put("count", Integer.toString(e.getValue()));
                        return constructed;
                    }).collect(Collectors.toList());
        }

        public String getGrandTotal() {
            return Util.toDecimals(totalSum);
        }

        // form fields

        public String fieldFrom(String style) {
            return textField(true, PARAM_SEARCH_FROM, searchFrom, 10, 10, style);
        }

        public String fieldTo(String style) {
            return textField(true, PARAM_SEARCH_TO, searchTo, 10, 10, style);
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
