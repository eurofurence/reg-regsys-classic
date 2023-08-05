package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.persistence.DbDataException;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.config.Option;
import org.eurofurence.regsys.repositories.config.OptionList;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  Represents the form used on the search page.
 *
 *  See constants below for the parameter names.
 */
public class SearchForm extends Form {

    // ------------ parameter constants -----------------------

    public static final String SUBMIT_DIRECT_EDIT = "direct_edit";
    public static final String SUBMIT_SEARCH      = "search";

    public static final String ID            = "search_id";
    public static final String MIN_ID        = "search_min_id";
    public static final String MAX_ID        = "search_max_id";
    public static final String ORDER_BY      = "search_order";
    public static final String NICK          = "search_nick";
    public static final String STATUS        = "search_status";
    public static final String NAME          = "search_name";
    public static final String ADDRESS       = "search_address";
    public static final String COUNTRY       = "search_country";
    public static final String COMMENTS      = "search_comments";
    public static final String OVERDUE       = "search_overdue";
    public static final String X_NEW         = "search_status_Xnew";
    public static final String X_WAITING     = "search_status_Xwait";
    public static final String X_CANCELLED   = "search_status_Xcanc";
    public static final String SPONSOR_ITEMS = "search_sponsor_items";
    public static final String FLAG_BASE     = "search_flag_"; // includes options
    public static final String PACKAGE_BASE  = "search_package_";

    // ------------ attributes -----------------------

    // already supported criteria

    private AttendeeSearchCriteria.AttendeeSearchSingleCriterion attendeeFinder; // holds the current search parameters

    private String sortBy;
    private String sortOrder;
    private long   minId;
    private long   maxId;

    // result

    private AttendeeSearchResultList attendeeResult = new AttendeeSearchResultList();

    protected int        count = -1; // -1: init 0: find 1+: loaded (1-based!!!!!!)

    // ------------ constructors and initialization -----------------------

    public SearchForm() {
        attendeeFinder = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
    }

    public void initialize() {
        attendeeFinder = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
        minId = 0L;
        maxId = 0L;
    }

    // ---------- proxy methods for entity access -------

    protected String getSearchId() {
        if (attendeeFinder.ids == null || attendeeFinder.ids.isEmpty())
            return "";
        else
            return Long.toString(attendeeFinder.ids.get(0));
    }

    protected String getSearchMinId() {
        if (minId <= 0)
            return "";
        else
            return Long.toString(minId);
    }

    protected String getSearchMaxId() {
        if (maxId <= 0)
            return "";
        else
            return Long.toString(maxId);
    }

    protected String getSearchStatus() {
        if (attendeeFinder.status == null || attendeeFinder.status.size() != 1) {
            return "";
        } else {
            return Integer.toString(Constants.MemberStatus.byNewRegsysValue(attendeeFinder.status.get(0)).dbValue());
        }
    }

    protected String getSearchOverdue() {
        String key = "overdue";
        if (attendeeFinder.addInfo != null && attendeeFinder.addInfo.containsKey(key)) {
            return Integer.toString(attendeeFinder.addInfo.get(key));
        } else {
            return "-1";
        }
    }

    protected String getSearchSponsorItems() {
        String key = "sponsor_items";
        if (attendeeFinder.addInfo != null && attendeeFinder.addInfo.containsKey(key)) {
            return Integer.toString(attendeeFinder.addInfo.get(key));
        } else {
            return "-1";
        }
    }

    // --------- Business methods ----------------------

    public static final String SEARCH_FORM_SESSION_KEY = "SearchForm_Session_Attendee";

    public AttendeeSearchCriteria constructSearchCriteria() {
        AttendeeSearchCriteria criteria = new AttendeeSearchCriteria();
        criteria.matchAny.add(attendeeFinder);
        criteria.sortBy = sortBy;
        criteria.sortOrder = sortOrder;
        criteria.minId = minId;
        criteria.maxId = maxId;
        return criteria;
    }

    public void storeCriteriaInSession(HttpSession session) {
        session.setAttribute(SEARCH_FORM_SESSION_KEY, constructSearchCriteria());
    }

    /**
     *  getCriteriaFromSession is used by ListPage to access the stored session parameters
     *  when the parameters aren't immediately submitted (e.g. re-running a search).
     */
    public void getCriteriaFromSession(HttpSession session) {
        AttendeeSearchCriteria s =
                (AttendeeSearchCriteria) session.getAttribute(SEARCH_FORM_SESSION_KEY);
        setCriteria(s);
    }

    /**
     * setCriteria allows specifying all criteria directly. Used by e.g. csv exports.
     */
    public void setCriteria(AttendeeSearchCriteria s) {
        if (s != null && s.matchAny != null && s.matchAny.size() == 1) {
            attendeeFinder = s.matchAny.get(0);
            sortBy = s.sortBy;
            sortOrder = s.sortOrder;
            minId = s.minId;
            maxId = s.maxId;
        } else {
            // this should never happen, because SearchForm just saved the attendee to the session
            // rather than fail badly, just use a blank attendee - the "list all" function as a fallback
            attendeeFinder = new AttendeeSearchCriteria.AttendeeSearchSingleCriterion();
            sortBy = "id";
            sortOrder = "ascending";
            minId = 0L;
            maxId = 0L;
        }
    }

    /**
     * used by ListForm via ListPage to run the actual search
     */
    public boolean find() {
        boolean result = false;
        try {
            AttendeeSearchCriteria criteria = constructSearchCriteria();
            attendeeResult = getPage().getAttendeeService().performFindAttendees(criteria, getPage().getTokenFromRequest(), getPage().getRequestId());
            result = true;
            count = 0; // first loadNext() advances to first
        } catch (DownstreamWebErrorException e) {
            resetErrors(Strings.searchForm.nobodyFound);
            addWebErrors(e.getErr());
        } catch (DownstreamException e) {
            resetErrors(e.getMessage());
        }

        return result;
    }

    /**
     * used by ListForm via ListPage to advance the actual search
     */
    public boolean loadNext() {
        count++;
        return count <= attendeeResult.attendees.size();
    }

    public AttendeeSearchResultList.AttendeeSearchResult getAttendee() {
        if (count >= 1 && count <= attendeeResult.attendees.size()) {
            return attendeeResult.attendees.get(count-1);
        } else {
            return new AttendeeSearchResultList.AttendeeSearchResult();
        }
    }

    public int getCount() {
        return count;
    }

    protected List<String> getAllowedOrders() {
        return Arrays.asList(
             new String[] {
            "id", "id DESC",
            "status", "status DESC",
            "nickname", "nickname DESC",
            "birthday", "birthday DESC",
            "type", "type DESC",
            "amount_due", "amount_due DESC",
            "email", "email DESC",
            "name", "name DESC",
            "zip", "zip DESC",
            "city", "city DESC",
            "country", "country DESC",
        });
    }

    protected List<String> getAllowedOrderNames() {
        return Arrays.asList(
               new String[] {
            "Id", "Id reverse",
            "Status", "Status reverse",
            "Nickname", "Nickname reverse",
            "Date of Birth", "Date of Birth reverse",
            "Membership Type", "Membership Type reverse",
            "Membership Fee", "Membership Fee reverse",
            "Email", "Email reverse",
            "Last Name, First Name", "Last Name, First Name reverse",
            "Zip Code", "Zip Code reverse",
            "City, Country", "City, Country reverse",
            "Country", "Country reverse",
        });
    }

    protected List<String> getStatusValues() {
        return Arrays.asList(
                Constants.MemberStatus.getDbValueArray(Constants.MemberStatus.getDropDownList_Search())
                );
    }

    protected List<String> getStatusDescriptions() {
        return Arrays.asList(
                Constants.MemberStatus.getStringArray(Constants.MemberStatus.getDropDownList_Search())
                );
    }

    private OptionList flagsList() {
        OptionList list = new OptionList(Option.OptionTypes.Flag, getPage().getConfiguration().choices.flags, o -> true);
        for (Option o: list) {
            if (attendeeFinder != null && attendeeFinder.flags != null && attendeeFinder.flags.containsKey(o.code)) {
                o.searchSetting = attendeeFinder.flags.get(o.code);
            } else {
                o.searchSetting = -1;
            }
        }
        return list;
    }

    private OptionList optionsList() {
        OptionList list = new OptionList(Option.OptionTypes.InfoOption, getPage().getConfiguration().choices.options, o -> true);
        for (Option o: list) {
            if (attendeeFinder != null && attendeeFinder.options != null && attendeeFinder.options.containsKey(o.code)) {
                o.searchSetting = attendeeFinder.options.get(o.code);
            } else {
                o.searchSetting = -1;
            }
        }
        return list;
    }

    private OptionList packagesList() {
        OptionList list =  new OptionList(Option.OptionTypes.Package, getPage().getConfiguration().choices.packages, o -> true);
        for (Option o: list) {
            if (attendeeFinder != null && attendeeFinder.packages != null && attendeeFinder.packages.containsKey(o.code)) {
                o.searchSetting = attendeeFinder.packages.get(o.code);
            } else {
                o.searchSetting = -1;
            }
        }
        return list;
    }

    // --------------------- parameter parsers --------------------------------------------------

    /**
     * This inner class encapsules all functions that are provided by this form for parsing request parameters
     * - keeps some structure to the main class, separating this concern out from the regular business methods
     *   until they can eventually be moved into activities
     * - folds away nicely if not needed
     */
    public class ParameterParser {
        private void setSearchId(String t) {
            if (t == null || t.equals("")) t = "-1";
            long v = FormHelper.parseLong(getPage(), t, ID, -1);
            if (v > 0) {
                attendeeFinder.ids = Collections.singletonList(v);
            } else {
                attendeeFinder.ids = null;
            }
        }

        private void setSearchMinId(String t) {
            if (t == null || t.equals("")) t = "-1";
            long v = FormHelper.parseLong(getPage(), t, MIN_ID, -1);
            if (v > 0) {
                minId = v;
            } else {
                minId = 0L;
            }
        }

        private void setSearchMaxId(String t) {
            if (t == null || t.equals("")) t = "-1";
            long v = FormHelper.parseLong(getPage(), t, MAX_ID, -1);
            if (v > 0) {
                maxId = v;
            } else {
                maxId = 0L;
            }
        }

        private void setSearchOrder(String t) {
            if (t == null || t.equals("")) t = "id";
            for (String allowed: getAllowedOrders()) {
                if (allowed.equals(t)) {
                    sortOrder = "ascending";
                    if (t.endsWith(" DESC")) {
                        sortOrder = "descending";
                        t = t.substring(0, t.length() - " DESC".length());
                    }
                    sortBy = t;
                    return;
                }
            }
            addError(Strings.searchForm.invalidSearchOrder);
        }

        private void setSearchStatus(String t) {
            if (t == null || t.equals("")) t = "-1";
            int currentSearchStatus = -1;
            if (attendeeFinder.status != null && attendeeFinder.status.size() == 1) {
                currentSearchStatus = Constants.MemberStatus.byNewRegsysValue(attendeeFinder.status.get(0)).dbValue();
            }
            try {
                Constants.MemberStatus newSearchStatus = Constants.MemberStatus.getMemberStatus(FormHelper.parseInt(getPage(), t, STATUS, currentSearchStatus));
                if (newSearchStatus == Constants.MemberStatus.SEARCH_IGNORE) {
                    attendeeFinder.status = null;
                } else {
                    attendeeFinder.status = Collections.singletonList(newSearchStatus.newRegsysValue());
                }
            } catch (DbDataException e) {
                addError(e.getMessage());
            }
        }

        private void setSearchOverdue(String t) {
            if (t == null || t.equals("")) t = "-1";
            if (attendeeFinder.addInfo == null)
                attendeeFinder.addInfo = new HashMap<>();
            String key = "overdue";
            int current = -1;
            if (attendeeFinder.addInfo.containsKey(key)) {
                current = attendeeFinder.addInfo.get(key);
            }
            int searchOverdue = FormHelper.parseInt(getPage(), t, OVERDUE, current);
            if (searchOverdue == -1) {
                attendeeFinder.addInfo.remove(key);
            } else {
                attendeeFinder.addInfo.put(key, searchOverdue);
            }
        }

        private void setSearchStatusXsomeValue(String t, Constants.MemberStatus someValue, String param, String desc) {
            try {
                int current = 1;
                if (attendeeFinder.status != null && attendeeFinder.status.contains(someValue.newRegsysValue())) {
                    current = 0;
                }
                int v = FormHelper.parseInt(getPage(), t, param, current);
                if (v == 1) {
                    // remove someValue
                    if (attendeeFinder.status == null) {
                        attendeeFinder.status = Stream.of(
                                        Constants.MemberStatus.NEW.newRegsysValue(),
                                        Constants.MemberStatus.APPROVED.newRegsysValue(),
                                        Constants.MemberStatus.PARTIALLY_PAID.newRegsysValue(),
                                        Constants.MemberStatus.PAID.newRegsysValue(),
                                        Constants.MemberStatus.CHECKED_IN.newRegsysValue(),
                                        Constants.MemberStatus.WAITING.newRegsysValue(),
                                        Constants.MemberStatus.CANCELLED.newRegsysValue(),
                                        Constants.MemberStatus.DELETED.newRegsysValue()
                                )
                                .filter(s -> !someValue.newRegsysValue().equals(s))
                                .collect(Collectors.toList());
                    } else {
                        if (attendeeFinder.status.contains(someValue.newRegsysValue())) {
                            attendeeFinder.status = attendeeFinder.status.stream()
                                    .filter(s -> !someValue.newRegsysValue().equals(s))
                                    .collect(Collectors.toList());
                        }
                    }
                } else if (v == 0) {
                    if (attendeeFinder.status != null && attendeeFinder.status.size() > 1 && attendeeFinder.status.contains(someValue.newRegsysValue())) {
                        // add someValue
                        attendeeFinder.status.add(someValue.newRegsysValue());
                    }
                } else {
                    throw new DbDataException("Internal error - " + desc + " out of range [0,1]");
                }
            } catch (DbDataException e) {
                addError(e.getMessage());
            }
        }

        private void setSearchStatusXnew(String t) {
            setSearchStatusXsomeValue(t, Constants.MemberStatus.NEW, X_NEW,"searchStatusXnew");
        }

        private void setSearchStatusXwait(String t) {
            setSearchStatusXsomeValue(t, Constants.MemberStatus.WAITING, X_WAITING,"searchStatusXwait");
        }

        private void setSearchStatusXcanc(String t) {
            setSearchStatusXsomeValue(t, Constants.MemberStatus.CANCELLED, X_CANCELLED,"searchStatusXcanc");
        }

        private void setSearchSponsorItems(String t) {
            if (t == null || t.equals("")) t = "-1";
            if (attendeeFinder.addInfo == null)
                attendeeFinder.addInfo = new HashMap<>();
            String key = "sponsor_items";
            int current = -1;
            if (attendeeFinder.addInfo.containsKey(key)) {
                current = attendeeFinder.addInfo.get(key);
            }
            int searchSponsorItems = FormHelper.parseInt(getPage(), t, SPONSOR_ITEMS, current);
            if (searchSponsorItems == -1) {
                attendeeFinder.addInfo.remove(key);
            } else {
                attendeeFinder.addInfo.put(key, searchSponsorItems);
            }
        }

        private void genericOptionSetter(HttpServletRequest request, OptionList list, Map<String, Integer> paramMap, String paramBaseName, String errorMessageFormat) {
            for (Option o: list) {
                String parname = paramBaseName + o.code;
                String value = request.getParameter(parname); // may be null when NOT selected
                if (value == null) value = "-1";
                if ("-1".equals(value)) {
                    paramMap.remove(o.code);
                } else if ("0".equals(value)) {
                    paramMap.put(o.code, 0);
                } else if ("1".equals(value)) {
                    paramMap.put(o.code, 1);
                } else {
                    addError(String.format(errorMessageFormat, o.code));
                }
            }
        }

        private void setSearchFlagsByCode(HttpServletRequest request) {
            genericOptionSetter(request, flagsList(), attendeeFinder.flags, FLAG_BASE, Strings.searchForm.invalidFlagValue);
        }

        private void setSearchOptionsByCode(HttpServletRequest request) {
            genericOptionSetter(request, optionsList(), attendeeFinder.options, FLAG_BASE, Strings.searchForm.invalidFlagValue);
        }

        private void setSearchPackagesByCode(HttpServletRequest request) {
            genericOptionSetter(request, packagesList(), attendeeFinder.packages, PACKAGE_BASE, Strings.searchForm.invalidPackageValue);
        }

        private String nvl(String s) {
            return s == null ? "" : s;
        }

        public void parse(HttpServletRequest request) {
            setSearchId(request.getParameter(ID));
            setSearchMinId(request.getParameter(MIN_ID));
            setSearchMaxId(request.getParameter(MAX_ID));
            setSearchOrder(nvl(request.getParameter(ORDER_BY)));
            attendeeFinder.nickname = request.getParameter(NICK);
            setSearchStatus(request.getParameter(STATUS));
            attendeeFinder.name = request.getParameter(NAME);
            attendeeFinder.address = request.getParameter(ADDRESS);
            attendeeFinder.country = request.getParameter(COUNTRY);
            attendeeFinder.userComments = request.getParameter(COMMENTS);
            setSearchOverdue(request.getParameter(OVERDUE));
            setSearchStatusXnew(nvl(request.getParameter(X_NEW)));
            setSearchStatusXwait(nvl(request.getParameter(X_WAITING)));
            setSearchStatusXcanc(nvl(request.getParameter(X_CANCELLED)));
            setSearchSponsorItems(request.getParameter(SPONSOR_ITEMS));
            setSearchFlagsByCode(request);
            setSearchOptionsByCode(request);
            setSearchPackagesByCode(request);
        }
    }

    public ParameterParser getParameterParser() {
        return new ParameterParser();
    }

    // --------------------- form permission methods ------------------------------------------------

    // completely handled on the page level

    // --------------------- form output methods ------------------------------------------------

    /**
     * This inner class encapsules all functions that are provided by this form for velocity templates
     * - avoids calling any methods not listed here from Velocity
     * - folds away nicely if not needed
     */
    public class VelocityRepresentation {
        // properties

        // form fields

        public String getFormHeader() {
            return "<FORM ACTION=\"list\" METHOD=\"post\" accept-charset=\"UTF-8\">";
        }

        public String getFormFooter() {
            return "</FORM>";
        }

        public String submitDirectEdit(String caption, String style) {
            return "<INPUT TYPE=\"SUBMIT\" NAME=\"" + SUBMIT_DIRECT_EDIT + "\" VALUE=\"" + escape(caption) + "\" class=\"" + escape(style) + "\"/>";
        }

        public String submitSearch(String caption, String style) {
            return "<INPUT TYPE=\"SUBMIT\" NAME=\"" + SUBMIT_SEARCH + "\" VALUE=\"" + escape(caption) + "\" class=\"" + escape(style) + "\"/>";
        }

        public String fieldId(int displaySize, String style) {
            return Form.textField(true, ID, getSearchId(), displaySize, 10, style);
        }

        public String fieldMinId(int displaySize, String style) {
            return Form.textField(true, MIN_ID, getSearchMinId(), displaySize, 10, style);
        }

        public String fieldMaxId(int displaySize, String style) {
            return Form.textField(true, MAX_ID, getSearchMaxId(), displaySize, 10, style);
        }

        public String fieldOrderBy(String style) {
            String currentOrder = sortBy;
            if ("descending".equals(sortOrder))
                currentOrder += " DESC";
            return Form.selector(true, ORDER_BY, getAllowedOrders(), getAllowedOrderNames(), currentOrder, 1, style, null);
        }

        public String fieldNick(int displaySize) {
            return Form.textField(true, NICK, attendeeFinder.nickname, displaySize, 80);
        }

        public String fieldStatus(String style) {
            return Form.selector(true, STATUS, getStatusValues(), getStatusDescriptions(), getSearchStatus(), 1, style, null);
        }

        public String fieldName(int displaySize) {
            return Form.textField(true, NAME, attendeeFinder.name, displaySize, 80);
        }

        public String fieldAddress(int displaySize) {
            return Form.textField(true, ADDRESS, attendeeFinder.address, displaySize, 80);
        }

        public String fieldCountry(int displaySize) {
            return Form.textField(true, COUNTRY, attendeeFinder.country, displaySize, 2);
        }

        public String fieldComments(int displaySize) {
            return Form.textField(true, COMMENTS, attendeeFinder.userComments, displaySize, 80);
        }

        private String anyNoYesSelector(String param, String currentValue, String style) {
            return Form.selector(true, param,
                    Arrays.asList(new String[] {"-1", "0", "1"}),
                    Arrays.asList(new String[] {"Any", "No", "Yes"}),
                    currentValue, 1, style, null);
        }

        public String fieldOverdue(String style) {
            return anyNoYesSelector(OVERDUE, getSearchOverdue(), style);
        }

        private String getSearchStatusXsomeValue(Constants.MemberStatus someValue) {
            if (attendeeFinder.status != null) {
                if (attendeeFinder.status.size() == 1) {
                    return "0";
                } else if (attendeeFinder.status.contains(someValue.newRegsysValue())) {
                    return "0";
                } else {
                    return "1";
                }
            } else {
                return "0";
            }
        }

        public String fieldXnew(String style) {
            return Form.checkbox(true, X_NEW, "1", getSearchStatusXsomeValue(Constants.MemberStatus.NEW), style);
        }

        public String fieldXwaiting(String style) {
            return Form.checkbox(true, X_WAITING, "1", getSearchStatusXsomeValue(Constants.MemberStatus.WAITING), style);
        }

        public String fieldXcancelled(String style) {
            return Form.checkbox(true, X_CANCELLED, "1", getSearchStatusXsomeValue(Constants.MemberStatus.CANCELLED), style);
        }

        public String fieldSponsorItems(String style) {
            return anyNoYesSelector(SPONSOR_ITEMS, getSearchSponsorItems(), style);
        }

        public String getResetFormUrl() {
            return "search?resetform=yes";
        }

        public List<Integer> getFlagAndPackageIndexArray() {
            int limit1 = flagsList().size() + optionsList().size();
            int limit2 = packagesList().size();
            int max = Math.max(limit1, limit2);

            Integer[] result = new Integer[max];
            for (int i = 0; i < max; i++)
                result[i] = i;

            return Arrays.asList(result);
        }

        public Option getFlag(Integer index) {
            OptionList flags = flagsList();
            OptionList options = optionsList();

            if (index < flags.size()) {
                Option f = flags.get(index);
                f.searchSetting = -1;
                if (attendeeFinder.flags.containsKey(f.code)) {
                    f.searchSetting = attendeeFinder.flags.get(f.code);
                }
                return f;
            } else if (index < flags.size() + options.size()) {
                Option o = options.get(index - flags.size());
                o.searchSetting = -1;
                if (attendeeFinder.options.containsKey(o.code)) {
                    o.searchSetting = attendeeFinder.options.get(o.code);
                }
                return o;
            } else {
                return null;
            }
        }

        public Option getPackage(Integer index) {
            OptionList packages = packagesList();
            if (index < packages.size()) {
                Option p = packages.get(index);
                p.searchSetting = -1;
                if (attendeeFinder.packages.containsKey(p.code)) {
                    p.searchSetting = attendeeFinder.packages.get(p.code);
                }
                return p;
            } else {
                return null;
            }
        }

        public String getOptionName(Option o) {
            return o.name;
        }

        private String anyNoYesOptionSelector(String paramBase, Option o, String style) {
            return anyNoYesSelector(paramBase + o.code, Integer.toString(o.searchSetting), style);
        }

        public String fieldFlag(Option flag, String style) {
            return anyNoYesOptionSelector(FLAG_BASE, flag, style);
        }

        public String fieldPackage(Option pkg, String style) {
            return anyNoYesOptionSelector(PACKAGE_BASE, pkg, style);
        }
    }

    public VelocityRepresentation getVelocityRepresentation() {
        return new VelocityRepresentation();
    }
}
