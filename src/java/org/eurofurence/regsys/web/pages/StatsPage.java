package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.types.IsoDate;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.PackageInfo;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.NotFoundException;
import org.eurofurence.regsys.repositories.payments.PaymentService;
import org.eurofurence.regsys.repositories.payments.Transaction;
import org.eurofurence.regsys.repositories.payments.TransactionResponse;
import org.eurofurence.regsys.web.forms.FormHelper;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.*;

public class StatsPage extends Page {
    // ---- parameters ----

    // ---- page ---

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!hasPermission(Constants.Permission.ADMIN)) {
            return forward("page/start");
        }

        initializeFields();
        computeFullStats();

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("page", this);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.statsPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "stats.vm";
    }

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);
    }

    // ---- accessors ----

    public long getByStatus(String key) {
        return by_status.getOrDefault(key, 0L);
    }

    public long[] getByType() {
        return by_type;
    }

    public static class PackageCount {
        public String category;
        public String description;
        public Long count;

        public PackageCount(String category, String description, Long count) {
            this.category = category;
            this.description = description;
            this.count = count;
        }

        public String getDescription() {
            return description;
        }

        public Long getCount() {
            return count;
        }
    }

    public List<PackageCount> getByPackage() {
        List<PackageCount> result = new ArrayList<>();
        getConfiguration().choices.packages.entrySet().stream().toList().stream()
                .sorted(Map.Entry.comparingByKey()) // stable sort retains suborders, so this is the lowest priority
                .sorted(Comparator.comparingInt(e -> e.getValue().sorting))
                .sorted(Comparator.comparing(e -> e.getValue().category))
                .forEach(e -> {
                    result.add(new PackageCount(e.getValue().category, e.getValue().description, by_package.getOrDefault(e.getKey(), 0L)));
                });

        return result;
    }

    public Map<String, Long> getByFlag() {
        return by_flag;
    }

    public Map<String, String> getFinances() {
        Map<String, String> result = new HashMap<>();
        result.put("total_dues", toCurrencyDecimals(finances.total_dues));
        result.put("remain_dues", toCurrencyDecimals(finances.remain_dues));
        result.put("overpaid_amount", toCurrencyDecimals(finances.overpaid_amount));
        result.put("received", toCurrencyDecimals(finances.total_dues - finances.remain_dues));
        result.put("total", toCurrencyDecimals(finances.remain_dues - finances.overpaid_amount));
        return result;
    }

    private Map<String, String> formatPaymentStats(PaymentStats s) {
        Map<String, String> result = new HashMap<>();
        result.put("incoming", toCurrencyDecimals(s.incoming));
        result.put("outgoing", toCurrencyDecimals(s.outgoing));
        result.put("total", toCurrencyDecimals(s.incoming + s.outgoing));
        result.put("incoming_count", Integer.toString(s.incoming_count));
        result.put("outgoing_count", Integer.toString(s.outgoing_count));
        return result;
    }

    public Set<String> getByPayMethodKeys() {
        return payments_by_method.keySet();
    }

    public Map<String,String> getByPayMethod(String method) {
        if (payments_by_method.containsKey(method)) {
            return formatPaymentStats(payments_by_method.get(method));
        } else {
            return formatPaymentStats(new PaymentStats());
        }
    }

    public Map<String,String> getSums() {
        return formatPaymentStats(sums);
    }

    public long minorsCount() {
        return minors;
    }

    public long overpaidCount() {
        return overpaid_count;
    }

    public long overdueCount() {
        return getByStatus("overdue_count");
    }

    // ---- formatting helpers ----

    public String toCurrencyDecimals(long value) {
        return FormHelper.toCurrencyDecimals(value);
    }

    // ---- statistics collection ----

    private AttendeeSearchResultList attendees;
    private TransactionResponse transactions;

    public Map<String, Long> by_status = new TreeMap<>();
    public long[] by_type = new long[] {0L, 0L, 0L};  // standard, guest, day
    public Map<String, Long> by_package = new TreeMap<>();
    public Map<String, Long> by_flag = new TreeMap<>();

    public static class Finances {
        long total_dues = 0L;
        long remain_dues = 0L;
        long overpaid_amount = 0L;
    }
    public Finances finances = new Finances();

    public long minors = 0L;
    public long overpaid_count = 0L;

    public static class PaymentStats {
        public long incoming;
        public long outgoing; // negative
        public int incoming_count;
        public int outgoing_count;
    }
    public Map<String, PaymentStats> payments_by_method = new TreeMap<>();
    public PaymentStats sums = new PaymentStats();

    public void initializeFields() {
        for (Constants.MemberStatus status: Constants.MemberStatus.values()) {
            if (!status.newRegsysValue().contains("(")) {
                by_status.put(status.newRegsysValue(), 0L);
            }
        }
        by_status.put("attending_count", 0L);
        by_status.put("total_count", 0L);
        by_status.put("overdue_count", 0L);

        for (Transaction.Method method: Transaction.Method.values()) {
            payments_by_method.put(method.getValue(), new PaymentStats());
        }

        for (Map.Entry<String, Configuration.ChoiceConfig> e: getConfiguration().choices.packages.entrySet()) {
            by_package.put(e.getKey(), 0L);
        }
        for (Map.Entry<String, Configuration.ChoiceConfig> e: getConfiguration().choices.flags.entrySet()) {
            by_flag.put(e.getKey(), 0L);
        }
        for (Map.Entry<String, Configuration.ChoiceConfig> e: getConfiguration().choices.options.entrySet()) {
            by_flag.put(e.getKey(), 0L);
        }
    }

    private void loadAttendeeData() {
        AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
        AttendeeSearchCriteria criteria = new AttendeeSearchCriteria();
        criteria.matchAny.add(criterion);

        try {
            attendees = getAttendeeService().performFindAttendees(criteria, getTokenFromRequest(), getRequestId());
        } catch (NotFoundException ignore) {
            // valid case
            attendees = new AttendeeSearchResultList();
        }
    }

    private final PaymentService paymentService = new PaymentService();

    private void loadPaymentData() {
        try {
            transactions = paymentService.performFindTransactions(null, null, null, null, getTokenFromRequest(), getRequestId());
        } catch (NotFoundException ignore) {
            // valid case
            transactions = new TransactionResponse();
        }
    }

    private void inc(Map<String,Long> map, String key, long count) {
        if (map == null || key == null || "".equals(key))
            return;
        if (map.containsKey(key)) {
            long old = map.get(key);
            map.put(key, old + count);
        } else {
            map.put(key, count);
        }
    }

    private void inc(Map<String,Long> map, String key) {
        inc(map, key, 1L);
    }

    private boolean contains(String commasepvalues, String v) {
        if (commasepvalues == null || "".equals(commasepvalues) || v == null)
            return false;
        for (String it: commasepvalues.split(",")) {
            if (v.equals(it)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSubkey(List<PackageInfo> pkg, String v) {
        if (pkg == null || v == null)
            return false;
        for (PackageInfo entry: pkg) {
            if (entry != null && entry.name != null) {
                if (entry.name.contains(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void incAfterSplit(Map<String,Long> map, String commasepvalues) {
        if (map == null || commasepvalues == null || "".equals(commasepvalues))
            return;
        for (String it: commasepvalues.split(",")) {
            inc(map, it);
        }
    }

    private void incWithCounts(Map<String,Long> map, List<PackageInfo> pkgList) {
        if (map == null || pkgList == null)
            return;
        for (PackageInfo entry: pkgList) {
            inc(map, entry.name, entry.count);
        }
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private void recordPayAmount(String method, Long amount) {
        if (method == null || amount == null)
            return;
        if (!payments_by_method.containsKey(method))
            return;
        PaymentStats r = payments_by_method.get(method);
        if (amount < 0) {
            r.outgoing_count++;
            r.outgoing += amount;
            sums.outgoing_count++;
            sums.outgoing += amount;
        } else if (amount > 0) {
            r.incoming_count++;
            r.incoming += amount;
            sums.incoming_count++;
            sums.incoming += amount;
        }
    }

    public void computeFullStats() {
        try {
            loadAttendeeData();
            loadPaymentData();
        } catch (DownstreamException e) {
            resetErrors();
            addError(e.getMessage());
            return;
        }

        String today = new IsoDate().getIsoFormat();

        for(AttendeeSearchResultList.AttendeeSearchResult a: attendees.attendees) {
            boolean isAttending = false;
            if ("approved".equals(a.status) || "partially paid".equals(a.status) || "paid".equals(a.status) || "checked in".equals(a.status)) {
                isAttending = true;
            }

            inc(by_status, "total_count");
            inc(by_status, a.status);
            if (isAttending) {
                inc(by_status, "attending_count");
                if (a.currentDues > 0 && a.dueDate != null && !"".equals(a.dueDate) && today.compareTo(a.dueDate) > 0) {
                    inc(by_status, "overdue_count");
                }

                incWithCounts(by_package, a.packagesList);
                incAfterSplit(by_flag, a.flags);
                incAfterSplit(by_flag, a.options);

                if (contains(a.flags, "guest")) {
                    by_type[1]++;
                } else if (containsSubkey(a.packagesList, "day_")) {
                    by_type[2]++;
                } else {
                    by_type[0]++;
                }

                finances.total_dues += nvl(a.totalDues);
                finances.remain_dues += nvl(a.currentDues);
                if (nvl(a.currentDues) < 0) {
                    finances.overpaid_amount -= a.currentDues;
                    overpaid_count++;
                }
            }
        }

        for (Transaction t: transactions.payload) {
            if ("valid".equals(t.status) && "payment".equals(t.transactionType)) {
                if (t.amount != null)
                    recordPayAmount(t.method, t.amount.grossCent);
            }
        }
    }
}
