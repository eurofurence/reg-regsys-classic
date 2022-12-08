package org.eurofurence.regsys.web.forms;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Constants.Permission;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.web.pages.ListPage;

/**
 *  Represents the form used on the search results page.
 *
 *  See constants below for the parameter names.
 */
public class ListForm extends Form {

    // ------------ parameter constants -----------------------

    public static final String LONG_VERSION = "long";

    public static final String SUBMIT_RESEND_STATUS_EMAIL     = "resend-status-email";
    public static final String SUBMIT_EXPORT_EMAIL            = "export-email";
    public static final String SUBMIT_EXPORT_WAIVER           = "export-waiver";
    public static final String SUBMIT_BULKMAIL                = "bulkmail";
    public static final String SUBMIT_EXPORT_CSV              = "export-csv";
    public static final String SUBMIT_EXPORT_BADGE            = "export-badge";
    public static final String SUBMIT_MASS_APPROVE            = "mass-approve";

    // ------------ attributes -----------------------

    protected boolean longVersion;
    protected StringBuilder allIds = new StringBuilder(",");

    // ------------ constructors and initialization -----------------------

    public ListForm() {
    }

    // ---------- proxy methods for entity access -------

    private AttendeeSearchResultList.AttendeeSearchResult cachedCurrentAttendeeReference = null;

    protected boolean loadNextAndCache() {
        SearchForm searchForm = ((ListPage) getPage()).getSearchForm();
        boolean result = searchForm.loadNext();
        cachedCurrentAttendeeReference = searchForm.getAttendee();
        if (result)
            allIds.append(cachedCurrentAttendeeReference.id).append(",");
        return result;
    }

    protected int getLoadCount() {
        SearchForm searchForm = ((ListPage) getPage()).getSearchForm();
        return searchForm.getCount();
    }

    // --------------------- parameter parsers --------------------------------------------------

    /**
     * static utility method to extract the selected attendee ids into a sorted set again,
     * used by all the functionalities that the various submit buttons lead to.
     */
    public static SortedSet<Long> extractIdsFromParams(HttpServletRequest request) {
        boolean invertSelection = "1".equals(request.getParameter("allexcept"));
        String all = request.getParameter("all");
        if (all == null) all = "";

        SortedSet<Long> result = new TreeSet<>();
        for (String pname : all.split(",")) {
            if (pname.matches("[0-9]+")) {
                Long id = Long.parseLong(pname);
                boolean selected = "1".equals(request.getParameter(pname));
                if (selected ^ invertSelection) result.add(id);
            }
        }
        return result;
    }

    /**
     * This inner class encapsules all functions that are provided by this form for parsing request parameters
     * - keeps some structure to the main class, separating this concern out from the regular business methods
     *   until they can eventually be moved into activities
     * - folds away nicely if not needed
     */
    public class ParameterParser {
        public void parse(HttpServletRequest request) {
            longVersion = (request.getParameter(LONG_VERSION) != null);
        }
    }

    public ParameterParser getParameterParser() {
        return new ParameterParser();
    }

    // --------------------- form permission methods ------------------------------------------------

    private boolean auth(Permission permission) {
        return getPage().hasPermission(permission);
    }

    // --------------------- form output methods ------------------------------------------------

    /**
     * This inner class encapsules all functions that are provided by this form for velocity templates
     * - avoids calling any methods not listed here from Velocity
     * - folds away nicely if not needed
     */
    @SuppressWarnings("unused")
    public class VelocityRepresentation {
        // properties

        public boolean nextAttendeeAvailable() {
            return loadNextAndCache();
        }

        // can be used like this #if($form.longVersion)
        public boolean isLongVersion() {
            return longVersion;
        }

        public String getShortAgainUrl() {
            return "list?again=yes";
        }

        public String getLongAgainUrl() {
            return "list?again=yes&long=yes";
        }

        // form
        public String getFormHeader() {
            return "<FORM ACTION=\"export\" METHOD=\"post\" accept-charset=\"UTF-8\">";
        }

        public String getFormFooter() {
            return "</FORM>";
        }

        // submit buttons
        private String submitButton(String name, String caption, String style) {
            return "<INPUT TYPE=\"SUBMIT\" NAME=\"" + name + "\" VALUE=\"" + escape(caption) + "\" class=\"" + escape(style) + "\"/>";
        }

        public String submitResendStatusEmail(String caption, String style) {
            if (auth(Constants.Permission.ADMIN))
                return submitButton(SUBMIT_RESEND_STATUS_EMAIL, caption, style);
            else
                return "";
        }

        public String submitExportEmail(String caption, String style) {
            if (auth(Constants.Permission.ADMIN))
                return submitButton(SUBMIT_EXPORT_EMAIL, caption, style);
            else
                return "";
        }

        public String submitExportCsv(String caption, String style) {
            if (auth(Constants.Permission.ADMIN))
                return submitButton(SUBMIT_EXPORT_CSV, caption, style);
            else
                return "";
        }

        public String submitExportBadge(String caption, String style) {
            if (auth(Constants.Permission.ADMIN))
                return submitButton(SUBMIT_EXPORT_BADGE, caption, style);
            else
                return "";
        }

        public String submitExportWaiver(String caption, String style) {
            if (auth(Constants.Permission.ADMIN))
                return submitButton(SUBMIT_EXPORT_WAIVER, caption, style);
            else
                return "";
        }

        public String submitBulkmail(String caption, String style) {
            if (auth(Constants.Permission.ADMIN) || auth(Constants.Permission.ANNOUNCE))
                return submitButton(SUBMIT_BULKMAIL, caption, style);
            else
                return "";
        }

        public boolean mayMassAccept() {
            SearchForm searchForm = ((ListPage) getPage()).getSearchForm();
            return auth(Constants.Permission.ADMIN)
                    && searchForm.getSearchStatus().equals(Integer.toString(Constants.MemberStatus.NEW.dbValue()));
        }

        public String submitMassAccept(String caption, String style) {
            return submitButton(SUBMIT_MASS_APPROVE, caption, style);
        }

        // form fields
        public String allExceptCheckbox(String style) {
            return checkbox(true, "allexcept", "1", "0", style);
        }

        public String editAttendeeUrl() {
            return "input?param_id=" + getId();
        }

        public String editAttendeeAdminPageUrl() {
            return "input?param_id=" + getId() + "#admin";
        }

        public String viewPaymentsUrl() {
            return "payment?param_id=" + getId();
        }

        // list data
        public String getCount() {
            return Integer.toString(getLoadCount());
        }

        public String getId() {
            return Long.toString(cachedCurrentAttendeeReference.id);
        }

        public String getNick() {
            return escape(cachedCurrentAttendeeReference.nickname);
        }

        public String getFirstName() {
            return escape(cachedCurrentAttendeeReference.firstName);
        }

        public String getLastName() {
            return escape(cachedCurrentAttendeeReference.lastName);
        }

        public String getEmail() {
            return escape(cachedCurrentAttendeeReference.email);
        }

        public String getStatus() {
            return escape(cachedCurrentAttendeeReference.status);
        }

        public String getType() {
            // TODO missing
            return "MISSING";
            // return escape(cachedCurrentAttendeeReference.<missing admin info fields>);
        }

        public String getAmountDue() {
            return escape(FormHelper.toCurrencyDecimals(cachedCurrentAttendeeReference.totalDues));
        }

        public String getRemainingDues() {
            return escape(FormHelper.toCurrencyDecimals(cachedCurrentAttendeeReference.currentDues));
        }

        public boolean isOverdue() {
            // TODO
            return false;
            // return cachedCurrentAttendeeReference.dueDate;
        }

        public String getReportedPaydate() {
            // TODO missing - get from payment update cache in add info
            return "MISSING";
            // return cachedCurrentAttendeeReference.ReportPaydate();
        }

        public String getRegistrationDate() {
            return cachedCurrentAttendeeReference.registered;
        }

        public String currentCheckbox(String style) {
            return checkbox(true, getId(), "1", "", style);
        }

        public String allIdsHiddenField() {
            return "<INPUT TYPE=\"hidden\" NAME=\"all\" VALUE=\"" + allIds.toString() + "\"/>";
        }
    }

    public VelocityRepresentation getVelocityRepresentation() {
        return new VelocityRepresentation();
    }
}
