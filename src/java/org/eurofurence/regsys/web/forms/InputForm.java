package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Constants.Permission;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.persistence.DbDataException;
import org.eurofurence.regsys.backend.persistence.TypeChecks;
import org.eurofurence.regsys.backend.types.IsoDate;
import org.eurofurence.regsys.repositories.attendees.AdminInfo;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.attendees.StatusChange;
import org.eurofurence.regsys.repositories.attendees.StatusHistory;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.config.Option;
import org.eurofurence.regsys.repositories.config.OptionList;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;
import org.eurofurence.regsys.repositories.errors.NotFoundException;
import org.eurofurence.regsys.repositories.rooms.Group;
import org.eurofurence.regsys.repositories.rooms.Member;
import org.eurofurence.regsys.repositories.rooms.Room;
import org.eurofurence.regsys.repositories.rooms.RoomService;
import org.eurofurence.regsys.service.TransactionCalculator;
import org.eurofurence.regsys.web.pages.InputPage;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 *  Represents the single huge form used on the input page,
 *  used for creating and editing registrations.
 *
 *  See constants below for the parameter names.
 */
public class InputForm extends Form {

    // ------------ parameter constants -----------------------

    public static final String NICK          = "param_nick";
    public static final String FIRST_NAME    = "param_first_name";
    public static final String LAST_NAME     = "param_last_name";
    public static final String STREET        = "param_street";
    public static final String ZIP           = "param_zip";
    public static final String CITY          = "param_city";
    public static final String COUNTRY       = "param_country";
    public static final String STATE         = "param_state";
    public static final String EMAIL         = "param_email";
    public static final String EMAIL_REPEAT  = "param_email_repeat";
    public static final String PHONE         = "param_phone";
    public static final String BIRTHDAY      = "param_birthday";
    public static final String GENDER        = "param_gender";
    public static final String PRONOUNS      = "param_pronouns";
    public static final String ROOM          = "param_room";
    public static final String PARTNER       = "param_partner";
    public static final String TELEGRAM      = "param_telegram";

    public static final String LANG          = "param_lang";
    public static final String REG_LANG      = "param_reg_lang";
    public static final String FLAG          = "param_flag_"; // flag names taken from configuration
    public static final String PACKAGE       = "param_package_"; // package names taken from configuration
    public static final String OPTION        = "param_option_"; // option names taken from configuration

    public static final String TSHIRT_SIZE   = "param_tshirtsize";
    public static final String USER_COMMENTS = "param_user_comments";
    public static final String ROOMMATE      = "param_roommate";
    public static final String ROOMMATE1     = ROOMMATE + "_id";
    public static final String ROOMMATE2     = ROOMMATE + "2_id";
    public static final String ROOMMATE3     = ROOMMATE + "3_id";
    public static final String READ_TOS      = "readTOS";

    // admin only params
    public static final String MANUAL_DUES   = "param_manual_dues";
    public static final String MANUAL_DUE_DESC = "param_manual_due_desc";
    public static final String STATUS        = "param_status";
    public static final String CANCEL_REASON = "param_cancel_reason";
    public static final String ROOMASSIGN    = "param_roomassign";
    public static final String REGISTERED    = "param_registered";
    public static final String DUE_DATE      = "param_due_date";
    public static final String ADMIN_COMMENTS = "param_admin_comments";
    public static final String PERMISSIONS   = "param_permissions";
    public static final String KEY_DEPOSIT   = "param_key_deposit";
    public static final String REG_ID        = "param_reg_id"; // person registering this attendee

    // ------------ attributes -----------------------

    private Attendee attendee;
    private AdminInfo adminInfo;
    private Constants.MemberStatus attendeeStatus;
    private Constants.MemberStatus newAttendeeStatus;
    private List<StatusChange> statusHistory;
    private Group group;
    private Room room;
    private Room newRoom;
    private List<Room> eligibleRooms;
    private String dueDate;
    private boolean dueDateChanged = false;

    private String cancelReason = "";

    private boolean willDoMailingAdminUpdate = false;

    public List<String>     greenText       = new Vector<String>();

    // ------------ constructors and initialization -----------------------

    private final AttendeeService attendeeService = new AttendeeService();
    private final RoomService roomService = new RoomService();
    private final ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    public InputForm() {
        attendee = new Attendee();
        adminInfo = new AdminInfo();
        attendeeStatus = Constants.MemberStatus.NEW;
        newAttendeeStatus = Constants.MemberStatus.NEW;
        statusHistory = new ArrayList<>();
        group = new Group();
        room = new Room();
        newRoom = null;
        eligibleRooms = new ArrayList<>();
        dueDate = "";
    }

    public synchronized void initialize() {
        attendee = new Attendee();
        attendee.flags = getDefaultFlags().getDbString();
        attendee.packagesList = getDefaultPackages().getAsPackagesList();
        attendee.options = getDefaultOptions().getDbString();
        attendee.registrationLanguage = "en-US"; // have a default
        attendee.country = "DE"; // have a default
        statusHistory = new ArrayList<>();
        group = new Group();
        room = new Room();
        newRoom = null;
        eligibleRooms = new ArrayList<>();
        dueDate = "";
        dueDateChanged = false;
    }

    // ---------- proxy methods for entity access -------

    // used by services apis
    public Attendee getAttendee() {
        return attendee;
    }

    public AdminInfo getAdminInfo() {
        return adminInfo;
    }

    private long nvl(Long i) {
        if (i == null)
            return 0;
        else
            return i;
    }

    public boolean isNew() {
        return nvl(attendee.id) == 0;
    }

    public Constants.MemberStatus getStatus() {
        return attendeeStatus;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    public OptionList getDefaultFlags() {
        OptionList flags = new OptionList(Option.OptionTypes.Flag, configService.getConfig().choices.flags, o -> !o.adminOnly);
        return flags;
    }

    public OptionList getDefaultPackages() {
        OptionList packages = new OptionList(Option.OptionTypes.Package, configService.getConfig().choices.packages, o -> true);
        return packages;
    }

    public OptionList getDefaultOptions() {
        OptionList options = new OptionList(Option.OptionTypes.InfoOption, configService.getConfig().choices.options, o -> true);
        return options;
    }

    public OptionList getAttendeeFlags() {
        OptionList flags = new OptionList(Option.OptionTypes.Flag, configService.getConfig().choices.flags, o -> !o.adminOnly);
        flags.parseFromDbString(nvl(attendee.flags));
        return flags;
    }

    public OptionList getAttendeePackages() {
        OptionList packages = new OptionList(Option.OptionTypes.Package, configService.getConfig().choices.packages, o -> true);
        packages.parseFromPackagesList(attendee.packagesList);
        return packages;
    }

    public OptionList getAttendeeOptions() {
        OptionList options = new OptionList(Option.OptionTypes.InfoOption, configService.getConfig().choices.options, o -> true);
        options.parseFromDbString(nvl(attendee.options));
        return options;
    }

    public OptionList getAdminOnlyFlags() {
        OptionList flags = new OptionList(Option.OptionTypes.Flag, configService.getConfig().choices.flags, o -> o.adminOnly);
        flags.parseFromDbString(nvl(adminInfo.flags));
        return flags;
    }

    // --------- Business methods ----------------------

    private void loadStatusHistoryThrows(long dbId, RequestAuth auth, String requestId) {
        StatusHistory rawStatusHistory = attendeeService.performGetStatusHistory(dbId, auth, requestId);
        if (rawStatusHistory != null && rawStatusHistory.statusHistory != null) {
            statusHistory = rawStatusHistory.statusHistory;
        } else {
            statusHistory = new ArrayList<>();
        }
    }

    private void loadGroupThrows(long dbId, RequestAuth auth, String requestId) {
        group = new Group();

        Configuration config = getPage().getConfiguration();
        if (config.groups != null && config.groups.enable) {
            try {
                List<Group> groupList = roomService.performListGroups(dbId, false, auth, requestId);
                if (groupList != null && !groupList.isEmpty()) {
                    group = groupList.get(0);
                }
            } catch (NotFoundException ignore) {
                // perfectly valid
            }
        }
    }

    // we only call this if it is needed (attending status + not currently in a room)
    private void subLoadEligibleRoomsThrows(RequestAuth auth, String requestId) {
        try {
            List<Room> allRooms = roomService.performListRooms(0, auth, requestId);
            eligibleRooms = allRooms.stream()
                    .filter(r -> r.occupants == null || r.occupants.size() < r.size)
                    .toList();
        } catch (NotFoundException ignore) {
            // acceptable - no rooms set up yet
        }
    }

    private void loadRoomThrows(long dbId, RequestAuth auth, String requestId) {
        room = new Room();
        eligibleRooms = new ArrayList<>();

        Configuration config = getPage().getConfiguration();
        if (config.rooms != null && config.rooms.enable) {
            try {
                List<Room> roomList = roomService.performListRooms(dbId, auth, requestId);
                if (roomList != null && !roomList.isEmpty()) {
                    room = roomList.get(0);
                } else {
                    subLoadEligibleRoomsThrows(auth, requestId);
                }
            } catch (NotFoundException ignore) {
                // perfectly valid - now load available rooms
                subLoadEligibleRoomsThrows(auth, requestId);
            }
        }
    }

    public void getFromDBThrows(long dbId) {
        RequestAuth auth = getPage().getTokenFromRequest();
        String requestId = getPage().getRequestId();

        attendee = attendeeService.performGetAttendee(dbId, auth, requestId);
        attendeeStatus = Constants.MemberStatus.byNewRegsysValue(
                attendeeService.performGetCurrentStatus(dbId, auth, requestId)
        );
        newAttendeeStatus = attendeeStatus;
        dueDate = attendeeService.performGetDueDate(dbId, auth, requestId);
        if (getPage().hasPermission(Permission.ADMIN)) {
            adminInfo = attendeeService.performGetAdminInfo(dbId, auth, requestId);
            loadStatusHistoryThrows(dbId, auth, requestId);
            if (attendeeStatus.isParticipating()) {
                loadGroupThrows(dbId, auth, requestId);
                loadRoomThrows(dbId, auth, requestId);
            }
        }
    }

    // Web form data checking

    private boolean hasFundamentalDifference(Constants.MemberStatus s1, Constants.MemberStatus s2) {
        if (s1 == Constants.MemberStatus.APPROVED || s1 == Constants.MemberStatus.PARTIALLY_PAID || s1 == Constants.MemberStatus.PAID) {
            if (s2 == Constants.MemberStatus.APPROVED || s2 == Constants.MemberStatus.PARTIALLY_PAID || s2 == Constants.MemberStatus.PAID) {
                return false;
            }
        }
        return s1 != s2;
    }

    private void reloadAttendeeStatusThrows() {
        RequestAuth auth = getPage().getTokenFromRequest();
        String requestId = getPage().getRequestId();

        attendeeStatus = Constants.MemberStatus.byNewRegsysValue(
                attendeeService.performGetCurrentStatus(attendee.id, auth, requestId)
        );
        loadStatusHistoryThrows(attendee.id, auth, requestId);
    }

    private void reloadAttendeeDueDateThrows() {
        dueDate = attendeeService.performGetDueDate(attendee.id, getPage().getTokenFromRequest(), getPage().getRequestId());
    }

    private void reloadRoomThrows() {
        loadRoomThrows(attendee.id, getPage().getTokenFromRequest(), getPage().getRequestId());
    }

    /**
     * Processes a registration step form submission and decides whether to advance the current registration step and
     * the MaxStep. This depends on whether the current step is error-free and actually the next available step in the
     * initial registration.
     *
     * @return returns whether some errors occured. Redundant (but inverted) to hasErrors()
     *
     */
    public boolean processAccept(boolean wasNew) {
        RequestAuth auth = getPage().getTokenFromRequest();
        String requestId = getPage().getRequestId();

        try {
            if (wasNew) {
                attendee.id = attendeeService.performAddAttendee(attendee, auth, requestId);
            } else {
                if (dueDateChanged) {
                    attendeeService.performOverrideDueDate(attendee.id, dueDate, auth, requestId);
                }

                if (willDoMailingAdminUpdate) {
                    attendeeService.performUpdateAttendeeWithoutEmail(attendee, auth, requestId);
                } else {
                    attendeeService.performUpdateAttendee(attendee, auth, requestId);
                }

                if (getPage().hasPermission(Permission.ADMIN)) {
                    attendeeService.performSetAdminInfo(attendee.id, adminInfo, auth, requestId);
                }

                // reload current status after the changes
                reloadAttendeeStatusThrows();
                reloadAttendeeDueDateThrows();

                if (hasFundamentalDifference(newAttendeeStatus, attendeeStatus)) {
                    StatusChange change = new StatusChange();
                    change.status = newAttendeeStatus.newRegsysValue();
                    if (newAttendeeStatus == Constants.MemberStatus.CANCELLED) {
                        change.comment = cancelReason;
                    } else {
                        change.comment = "admin changed status";
                    }
                    attendeeService.performStatusChange(attendee.id, change, auth, requestId);

                    reloadAttendeeStatusThrows();
                    reloadAttendeeDueDateThrows();
                }

                boolean roomAssigned = newRoom != null
                        && newRoom.id != null
                        && !newRoom.id.isEmpty()
                        && !newRoom.id.equals(room.id);
                if (roomAssigned) {
                    roomService.performAddToRoom(newRoom.id, attendee.id, auth, requestId);

                    reloadRoomThrows();
                }
            }
        } catch (DownstreamWebErrorException e) {
            resetErrors(Strings.inputForm.dbError);
            addWebErrors(e.getErr());
        } catch (DownstreamException e) {
            resetErrors(e.getMessage());
        }

        return !hasErrors();
    }

    protected TransactionCalculator transactionCalculator = new TransactionCalculator();

    public void updateTransactionCache(long id) {
        try {
            transactionCalculator.loadTransactionsFor(id, getPage().getTokenFromRequest(), getPage().getRequestId());
        } catch (DownstreamException e) {
            getPage().addException(e);
        }
    }

    // --------------------- parameter parsers --------------------------------------------------

    /**
     * This inner class encapsules all functions that are provided by this form for parsing request parameters
     * - keeps some structure to the main class, separating this concern out from the regular business methods
     *   until they can eventually be moved into activities
     * - folds away nicely if not needed
     */
    public class ParameterParser {
        private void setLowlevelFromRequest(HttpServletRequest request, OptionList list, String paramPrefix) {
            boolean isAdmin = getPage().hasPermission(Permission.ADMIN);
            for (Option o: list) {
                String parname = paramPrefix + o.code;
                String value = request.getParameter(parname); // may be null when NOT selected
                if (value == null) value = "0";
                if (isAdmin || !o.readonly) {
                    o.count = "1".equals(value) ? 1 : 0;
                }
            }
        }

        private void setSpokenLanguagesFromRequest(HttpServletRequest request) {
            String[] raw = request.getParameterValues(LANG);
            if (raw == null) {
                raw = new String[]{};
            }
            List<String> selected = Arrays.asList(raw);
            attendee.spokenLanguages = String.join(",", selected);
        }

        private void setRegistrationLanguageFromRequest(HttpServletRequest request) {
            String selected = request.getParameter(REG_LANG);
            if (selected == null) {
                selected = configService.getConfig().registrationLanguages.get(0).key();
            }
            attendee.registrationLanguage = selected;
        }

        private void setFlagsFromRequest(HttpServletRequest request) {
            OptionList list = getAttendeeFlags();
            setLowlevelFromRequest(request, list, FLAG);
            attendee.flags = list.getDbString();
        }

        private void setPackagesFromRequest(HttpServletRequest request) {
            OptionList list = getAttendeePackages();

            setLowlevelFromRequest(request, list, PACKAGE);
            // silently deselect options which are hidden for the current selection
            deselectHiddenPackagesNotImplemented(list);
            // silently selects options which are mandatory (those should be shown set&readonly to the user anyways)
            handleMandatoryPackagesNotImplemented(list);

            attendee.packagesList = list.getAsPackagesList();
        }

        private void deselectHiddenPackagesNotImplemented(OptionList list) {
            // TODO downstream will reject invalid combinations, then we'll show the error
            for (Option o : list) {
                if (o.count == 0 || o.readonly) continue;

//                 if (o.isConstraintsHiding(attendee.getPackages())) {
//                     attendee.setPackageByCode(o.code, 0);
//                 }
            }
        }

        private void handleMandatoryPackagesNotImplemented(OptionList list) {
            // TODO downstream will reject invalid combinations, then we'll show the error
            for (Option o : list) {
                // TODO
                // if (o.isConstraintsMandatory(attendee.getPackages())) {
                //     attendee.setPackageByCode(o.code, 1);
                // }
            }
        }

        private void setOptionsFromRequest(HttpServletRequest request) {
            OptionList list = getAttendeeOptions();
            setLowlevelFromRequest(request, list, OPTION);
            attendee.options = list.getDbString();
        }

        /** Requires that this checkbox is checked on initial registration. Adds an error otherwise. */
        private void checkReadTOS(String checked) {
            if (isNew() && !"yes".equals(checked)) {
                addError(Strings.inputForm.needYourAgreement);
            }
        }

        private String nvl(String s) {
            return s == null ? "" : s;
        }

        public void setNick(String t) {
            attendee.nickname = nvl(t);
        }

        public void setEmail(String value, String repeated) {
            attendee.email = nvl(value);
            if (!nvl(value).equals(nvl(repeated))) {
                addError(Strings.inputForm.emailsDoNotMatch);
            }
        }

        public void setBirthday(String t) {
            attendee.birthday = nvl(t);
        }

        public void setGender(String t) {
            attendee.gender = nvl(t);
        }

        public void setPronouns(String t) {
            attendee.pronouns = nvl(t);
        }

        public void parseRegularParams(HttpServletRequest request) {
            attendee.nickname = nvl(request.getParameter(NICK));
            attendee.firstName = nvl(request.getParameter(FIRST_NAME));
            attendee.lastName = nvl(request.getParameter(LAST_NAME));
            attendee.street = nvl(request.getParameter(STREET));
            attendee.zip = nvl(request.getParameter(ZIP));
            attendee.city = nvl(request.getParameter(CITY));
            attendee.country = nvl(request.getParameter(COUNTRY));
            attendee.state = nvl(request.getParameter(STATE));
            setEmail(request.getParameter(EMAIL), request.getParameter(EMAIL_REPEAT));
            attendee.phone = nvl(request.getParameter(PHONE));
            setBirthday(request.getParameter(BIRTHDAY));
            setGender(request.getParameter(GENDER));
            setSpokenLanguagesFromRequest(request);
            setRegistrationLanguageFromRequest(request);
            setFlagsFromRequest(request);
            setPackagesFromRequest(request);
            setOptionsFromRequest(request);
            attendee.partner = nvl(request.getParameter(PARTNER));
            attendee.telegram = nvl(request.getParameter(TELEGRAM));
            attendee.pronouns = nvl(request.getParameter(PRONOUNS));
            attendee.tshirtSize = nvl(request.getParameter(TSHIRT_SIZE));
            attendee.userComments = nvl(request.getParameter(USER_COMMENTS));
            checkReadTOS(request.getParameter(READ_TOS)); // adds an error if not checked (on initial reg)
        }

        private void setManualDues(String t) {
            long oldValue = adminInfo.manualDues;
            if (t == null) t = "0";
            adminInfo.manualDues = FormHelper.parseCurrencyDecimals(getPage(), t, "manual_dues", adminInfo.manualDues);
            if (adminInfo.manualDues != oldValue) {
                willDoMailingAdminUpdate = true;
            }
        }

        private void setManualDueDesc(String t) {
            adminInfo.manualDuesDescription = nvl(t);
        }

        private void setAdminOnlyFlagsFromRequest(HttpServletRequest request) {
            OptionList list = getAdminOnlyFlags();
            setLowlevelFromRequest(request, list, FLAG);
            adminInfo.flags = list.getDbString();
        }

        private void setStatus(String t) {
            try {
                newAttendeeStatus = Constants.MemberStatus.byNewRegsysValue(nvl(t));
            } catch (DownstreamException e) {
                newAttendeeStatus = attendeeStatus;
                addError(e.getMessage());
            }
        }

        private void setCancelReason(String t) {
            cancelReason = nvl(t);
            if (newAttendeeStatus == Constants.MemberStatus.CANCELLED) {
                if ("".equals(cancelReason)) {
                    addError(Strings.inputForm.mustProvideCancelReason);
                }
            }
        }

        private void setAdminComments(String t) {
            adminInfo.adminComments = nvl(t);
        }

        private void setPermissions(String t) {
            adminInfo.permissions = nvl(t);
        }

        private void setDueDate(String t) {
            if (t == null || "".equals(t)) {
                return;
            }
            try {
                IsoDate tdate = new IsoDate().fromDate(TypeChecks.parseDate(t, DUE_DATE, Strings.conf.paymentStart, Strings.util.payDateMessage,
                        Strings.conf.paymentEnd, Strings.util.payDateMessage));

                String newValue = tdate.getIsoFormat();
                dueDateChanged = !newValue.equals(dueDate);
                dueDate = newValue;
            } catch (DbDataException e) {
                getPage().addError(e.getMessage());
            }
        }

        private void setRoomAssign(String t) {
            if (t == null || "".equals(t)) {
                return;
            }
            try {
                // load room assignment and validate
                newRoom = roomService.performGetRoomById(t, getPage().getTokenFromRequest(), getPage().getRequestId());
            } catch (DownstreamWebErrorException e) {
                addWebErrors(e.getErr());
                newRoom = null;
            } catch (DownstreamException e) {
                addError(e.getMessage());
                newRoom = null;
            }
        }

        public void parseAdminParams(HttpServletRequest request) {
            setManualDues(request.getParameter(MANUAL_DUES));
            setManualDueDesc(request.getParameter(MANUAL_DUE_DESC));
            setAdminOnlyFlagsFromRequest(request);
            setStatus(request.getParameter(STATUS));
            setCancelReason(request.getParameter(CANCEL_REASON));
            setAdminComments(request.getParameter(ADMIN_COMMENTS));
            setPermissions(request.getParameter(PERMISSIONS));
            setDueDate(request.getParameter(DUE_DATE));
            setRoomAssign(request.getParameter(ROOMASSIGN));
        }
    }

    public ParameterParser getParameterParser() {
        return new ParameterParser();
    }

    // --------------------- form permission methods ------------------------------------------------

    private boolean auth(Permission permission) {
        return getPage().hasPermission(permission);
    }

    private boolean mayEdit() {
        return ((InputPage) getPage()).mayEdit();
    }

    private boolean mayEditAdmin() {
        return ((InputPage) getPage()).mayEditAdmin();
    }

    /*
    private boolean mayChangeRoommates() {
        return Config.ENABLE_ROOMSELECTION || auth(Constants.Permission.ADMIN);
    }
     */

    // --------------------- form output methods ------------------------------------------------

    /**
     * This inner class encapsules all functions that are provided by this form for velocity templates
     * - avoids calling any methods not listed here from Velocity
     * - folds away nicely if not needed
     */
    public class VelocityRepresentation {
        // properties

        /**
         * Returns true if one of the manual dues fields (7%, 19% or Description) is set and thus all fields should be
         * displayed. ADMIN will always see these fields - in order to be able to edit them.
         */
        public boolean showManualDues() {
            return adminInfo.manualDues != 0 || !"".equals(adminInfo.manualDuesDescription) || auth(Permission.ADMIN);
        }

        // attribute forwarders

        private String str(long i) {
            return Long.toString(i);
        }

        public String getId() {
            return str(nvl(attendee.id));
        }

        public Constants.MemberStatus getStatus() {
            return attendeeStatus;
        }

        public boolean isNew() {
            return nvl(attendee.id) == 0;
        }

        public boolean isOverdue() {
            if (attendeeStatus == Constants.MemberStatus.APPROVED || attendeeStatus == Constants.MemberStatus.PARTIALLY_PAID) {
                if (!"".equals(dueDate)) {
                    String today = new IsoDate().getIsoFormat();
                    return today.compareTo(dueDate) > 0;
                }
            }
            return false;
        }

        public String getFullPrice() {
            try {
                return FormHelper.toCurrencyDecimals(transactionCalculator.getTotalDues());
            } catch (Exception e) {
                addError(Strings.inputForm.dbError + "payment service error");
                return "UNAVAILABLE";
            }
        }

        public String getAmountDue() {
            try {
                return FormHelper.toCurrencyDecimals(transactionCalculator.getTotalDues());
            } catch (Exception e) {
                addError(Strings.inputForm.dbError + "payment service error");
                return "UNAVAILABLE";
            }
        }

        public String getRemainingDues() {
            try {
                return FormHelper.toCurrencyDecimals(transactionCalculator.getRemainingDues());
            } catch (Exception e) {
                addError(Strings.inputForm.dbError + "payment service error");
                return "UNAVAILABLE";
            }
        }

        public String getDueDate() {
            if (dueDate != null && !"".equals(dueDate)) {
                try {
                    return Util.formatDate(new IsoDate().fromIsoFormat(dueDate));
                } catch (Exception e) {
                    return "";
                }
            } else {
                return "";
            }
        }

        public boolean getPaymentsPending() {
            try {
                return transactionCalculator.getOpenPayments() > 0;
            } catch (Exception e) {
                addError(Strings.inputForm.dbError + "payment service error");
                return false;
            }
        }

        public boolean hasRemainingDues() {
            try {
                return transactionCalculator.getRemainingDues() > 0;
            } catch (Exception e) {
                addError(Strings.inputForm.dbError + "payment service error");
                return false;
            }
        }

        // form fields

        public String getFormHeader() {
            String result = "";

            if (mayEdit()) {
                result += "<FORM id=\"inputform\" ACTION=\"input\" METHOD=\"POST\" accept-charset=\"UTF-8\">\n";
                if (!isNew()) {
                    result += "    " + hiddenField(InputPage.ATTENDEE_ID, str(nvl(attendee.id))) + "\n";
                }
                result += "    " + hiddenField(InputPage.SUBMIT_MARKER, "yes") + "\n";
            }

            return result;
        }

        public String getFormFooter() {
            if (mayEdit())
                return "</FORM>";
            else
                return "";
        }

        public String getSubmitButton(String caption, String style, String onClick) {
            if (mayEdit())
                return "<INPUT TYPE=\"SUBMIT\" VALUE=\"" + escape(caption) + "\" CLASS=\"" + escape(style) + "\" " +
                    (onClick != null ? "onClick=\"" + onClick + "\"" : "") + "/>";
            else
                return "";
        }

        public String fieldNick(int displaySize) {
            return textField(mayEdit(), NICK, attendee.nickname, displaySize, 80);
        }

        public String fieldFirstname(int displaySize) {
            return textField(mayEdit(), FIRST_NAME, attendee.firstName, displaySize, 80);
        }

        public String fieldLastname(int displaySize) {
            return textField(mayEdit(), LAST_NAME, attendee.lastName, displaySize, 80);
        }

        public String fieldStreet(int displaySize) {
            return textField(mayEdit(), STREET, attendee.street, displaySize, 120);
        }

        public String fieldZip(int displaySize, String style) {
            return textField(mayEdit(), ZIP, attendee.zip, displaySize, 20, style);
        }

        public String fieldCity(int displaySize, String style) {
            return textField(mayEdit(), CITY, attendee.city, displaySize, 80, style);
        }

        public String fieldCountry() {
            return selector(mayEdit(), COUNTRY,
                    configService.getConfig().countries.keyList(),
                    configService.getConfig().countries.valueList(configService.getConfig().web.systemLanguage),
                    attendee.country, 1);
        }

        public String fieldSpokenLanguages() {
            String value = attendee.spokenLanguages;
            if (value == null) value = "";
            Set<String> selected = new HashSet<>(Arrays.asList(value.split(",")));
            return selector(mayEdit(), LANG,
                    configService.getConfig().spokenLanguages.keyList(),
                    configService.getConfig().spokenLanguages.valueListWithKey(configService.getConfig().web.systemLanguage),
                    selected, 5, true);
        }

        public String fieldRegistrationLanguage() {
            return selector(mayEdit(), REG_LANG,
                    configService.getConfig().registrationLanguages.keyList(),
                    configService.getConfig().registrationLanguages.valueListWithKey(configService.getConfig().web.systemLanguage),
                    attendee.registrationLanguage, 1);
        }

        public String fieldState(int displaySize) {
            return textField(mayEdit(), STATE, attendee.state, displaySize, 80);
        }

        public String fieldEmail(int displaySize) {
            return textField(mayEdit(), EMAIL, attendee.email != null ? attendee.email : "", displaySize, 200);
        }

        public String fieldEmailRepeat(int displaySize) {
            return textField(mayEdit(), EMAIL_REPEAT, attendee.email != null ? attendee.email : "", displaySize, 200);
        }

        public String fieldPhone(int displaySize) {
            return textField(mayEdit(), PHONE, attendee.phone, displaySize, 200);
        }

        public String fieldBirthday(int displaySize, String style) {
            return textField(mayEdit(), BIRTHDAY, attendee.birthday, displaySize, 10, style);
        }

        public String radioMale(String style) {
            return radioButton(mayEdit(), GENDER, "male", attendee.gender, style);
        }

        public String radioFemale(String style) {
            return radioButton(mayEdit(), GENDER, "female", attendee.gender, style);
        }

        public String radioOther(String style) {
            return radioButton(mayEdit(), GENDER, "other", attendee.gender, style);
        }

        public String radioNotprovided(String style) {
            return radioButton(mayEdit(), GENDER, "notprovided", attendee.gender, style);
        }

        public List<Option> getFlags() {
            List<Option> oList = new ArrayList<Option>();
            OptionList flags = getAttendeeFlags();
            for (Option o: flags) {
                // omit unselected admin only options, and even if so, only show as readonly
                if (o.adminOnly && !auth(Permission.ADMIN)) {
                    o.readonly = true;
                    if (o.count == 0)
                        continue;
                }
                oList.add(o);
            }
           return oList;
        }

        public String fieldFlag(Option o) {
            return checkbox(mayEdit(), FLAG + o.code, "1", o.count > 0 ? "1" : "0", "check", false);
        }

        public String getFlagParam(Option o) {
            return FLAG + o.code;
        }

        public String getOptionName(Option o) {
            return escape(o.name);
        }

        // accommodation section

        public boolean showAccommodationPackageSection() {
            // not currently supported
            return false;
        }

        public String fieldAccomodationPackagesOff() {
            return hiddenField(ROOM, "room-none");
        }

        public String getAccomodationParam(Option o) {
            return ROOM + "_" + o.code;
        }

        public String fieldPartner(int displaySize) {
            return textField(mayEdit(), PARTNER, attendee.partner, displaySize, 80);
        }

        public String fieldTelegram(int displaySize) {
            return textField(mayEdit(), TELEGRAM, attendee.telegram, displaySize, 80);
        }

        public String fieldPronouns(int displaySize) {
            return textField(mayEdit(), PRONOUNS, attendee.pronouns, displaySize, 80);
        }

        public String getOptionPrice(Option o) {
            return FormHelper.toCurrencyDecimals(o.price);
        }

        public List<Option> getPackages() {
            List<Option> oList = new ArrayList<Option>();
            OptionList packages = getAttendeePackages();
            for (Option o: packages) {
                // omit unselected admin only options
                if (o.adminOnly && o.count == 0 && !auth(Permission.ADMIN)) {
                    continue;
                }
                oList.add(o);
            }
            return oList;
        }

        public String fieldPackage(Option o) {
            if (o.readonly && !auth(Permission.ADMIN)) {
                // show visual read-only 'X' and also add the parameter hidden so that input validation is easier
                return (o.count > 0 ? "[X]" : "[&nbsp;]") + hiddenField(PACKAGE + o.code, o.count > 0 ? "1" : "0");
            } else {
                return checkbox(mayEdit(), PACKAGE + o.code, "1", o.count > 0 ? "1" : "0", "check");
            }
        }

        public String getPackageParam(Option o) {
            return PACKAGE + o.code;
        }

        public String fieldManualDues(int displaySize, String style) {
            if (auth(Permission.ADMIN)) {
                return textField(mayEditAdmin(), MANUAL_DUES, FormHelper.toCurrencyDecimals(adminInfo.manualDues), displaySize, 20, style);
            } else {
                String manualDueDisplay = FormHelper.toCurrencyDecimals(adminInfo.manualDues);
                return manualDueDisplay.equals("") ? "0.00&nbsp;&euro;" : manualDueDisplay + "&nbsp;&euro;";
            }
        }

        public String fieldManualDuesDescription(int displaySize) {
            return textField(mayEditAdmin(), MANUAL_DUE_DESC, adminInfo.manualDuesDescription, displaySize, 80);
        }

        public String fieldTshirtSize(String style) {
            List<String> keys = new ArrayList<>();
            keys.add("");
            keys.addAll(configService.getConfig().tShirtSizes.keyList());

            List<String> values = new ArrayList<>();
            values.add(Strings.inputForm.tshirtSizeSelectPrompt);
            values.addAll(configService.getConfig().tShirtSizes.valueList(configService.getConfig().web.systemLanguage));

            return selector(mayEdit(), TSHIRT_SIZE,
                    keys,
                    values,
                    attendee.tshirtSize, 1, style, null);
        }

        public List<Option> getOptions() {
            List<Option> oList = new ArrayList<Option>();
            OptionList flags = getAttendeeOptions();
            for (Option o: flags) {
                // omit unselected admin only options
                if (o.adminOnly && !auth(Permission.ADMIN)) {
                    o.readonly = true;
                    if (o.count == 0) continue;
                }
                oList.add(o);
            }
            return oList;
        }

        public String fieldOption(Option o) {
            return checkbox(mayEdit(), OPTION + o.code, "1", o.count > 0 ? "1" : "0", "check", false);
        }

        public String getOptionParam(Option o) {
            return OPTION + o.code;
        }

        public String fieldUserComments() {
            return textArea(mayEdit(), USER_COMMENTS, attendee.userComments, 10, 40);
        }

        public List<Option> getAdminFlags() {
            List<Option> oList = new ArrayList<Option>();
            OptionList flags = getAdminOnlyFlags();
            for (Option o: flags) {
                oList.add(o);
            }
            return oList;
        }

        public List<Map<String,String>> getStatusHistoryHumanReadable() {
            List<Map<String,String>> result = new ArrayList<>();
            if (statusHistory != null) {
                statusHistory.forEach(c -> {
                    Map<String,String> entry = new HashMap<>();
                    entry.put("timestamp", formatStatusTimestamp(c));
                    entry.put("status", formatStatusValue(c));
                    entry.put("comment", formatStatusComment(c));
                    result.add(entry);
                });
            }
            return result;
        }

        private String formatStatusTimestamp(StatusChange entry) {
            try {
                IsoDate parsed = new IsoDate().fromIsoFormat(entry.timestamp.substring(0,10));
                return parsed.getPublicFormat() + "&nbsp;" + entry.timestamp.substring(11,16);
            } catch (Exception e) {
                return "parse error";
            }
        }

        private String formatStatusValue(StatusChange entry) {
            try {
                return Constants.MemberStatus.byNewRegsysValue(entry.status).toString();
            } catch (DownstreamException e) {
                return "error: unknown";
            }
        }

        private String formatStatusComment(StatusChange entry) {
            return escape(entry.comment);
        }

        public String fieldStatus(String style) {
            List<Constants.MemberStatus> available = Constants.MemberStatus.getDropDownList_Admin_AvailableOnly(attendeeStatus);
            return selector(mayEditAdmin(), STATUS,
                    Constants.MemberStatus.getNewRegsysValueArray(available),
                    Constants.MemberStatus.getDisplayNames(available),
                    attendeeStatus.newRegsysValue(), 1, false, style);
        }

        public String fieldCancelReason(int displaySize) {
            String value = "";
            if (statusHistory != null && !statusHistory.isEmpty()) {
                StatusChange lastEntry = statusHistory.get(statusHistory.size()-1);
                if (Constants.MemberStatus.CANCELLED.newRegsysValue().equals(lastEntry.status)) {
                    value = lastEntry.comment;
                }
            }
            return textField(mayEditAdmin(), CANCEL_REASON, value, displaySize, 80);
        }

        public String fieldAdminComments() {
            return textArea(mayEditAdmin(), ADMIN_COMMENTS, adminInfo.adminComments, 5, 40);
        }

        public String fieldPermissions(int displaySize) {
            return textField(mayEditAdmin(), PERMISSIONS, adminInfo.permissions, displaySize, 40);
        }

        public String fieldDueDate(int displaySize) {
            return textField(mayEditAdmin(), DUE_DATE, getDueDate(), displaySize, 10);
        }

        /*
        public String fieldKeyDeposit(String style) {
            return checkbox(mayEditAdmin(), KEY_DEPOSIT, "1", str(attendee.getKeyDeposit()), style);
        }
        */

        // group and room section

        public boolean showGroupConfig() {
            Configuration config = getPage().getConfiguration();
            return getPage().isLoggedIn() && config.groups != null && config.groups.enable;
        }

        public boolean showRoomConfig() {
            Configuration config = getPage().getConfiguration();
            return config.rooms != null && config.rooms.enable;
        }

        @SuppressWarnings("unused")
        public boolean showGroupAndRoomSection() {
            return attendeeStatus.isParticipating() && (showGroupConfig() || showRoomConfig());
        }

        private boolean groupContainsAttendee() {
            return (group != null) &&
                    (group.members != null) &&
                    (group.members.stream().anyMatch(m -> m.id == attendee.id));
        }

        @SuppressWarnings("unused")
        public boolean hasGroup() {
            return groupContainsAttendee();
        }

        @SuppressWarnings("unused")
        public String fieldGroupName() {
            if (groupContainsAttendee()) {
                return escape(group.name);
            } else {
                return "";
            }
        }

        @SuppressWarnings("unused")
        public String fieldGroupFlags() {
            if (groupContainsAttendee() && group.flags != null) {
                return String.join(",&nbsp;", group.flags);
            } else {
                return "";
            }
        }

        @SuppressWarnings("unused")
        public String fieldGroupComments() {
            if (groupContainsAttendee() && group.comments != null) {
                return escape(group.comments);
            } else {
                return "";
            }
        }

        @SuppressWarnings("unused")
        public String fieldGroupIsOwner(String yes, String no) {
            if (groupContainsAttendee()) {
                if (group.owner == attendee.id) {
                    return yes;
                } else {
                    return no;
                }
            } else {
                return "";
            }
        }

        private List<Map<String,String>> membersHumanReadable(List<Member> entries) {
            List<Map<String,String>> result = new ArrayList<>();
            if (entries != null) {
                entries.forEach(m -> {
                    Map<String,String> entry = new HashMap<>();
                    entry.put("id", Long.toString(m.id));
                    entry.put("nickname", m.nickname);
                    result.add(entry);
                });
            }
            return result;
        }

        @SuppressWarnings("unused")
        public List<Map<String,String>> getGroupMembersHumanReadable() {
            return membersHumanReadable(group.members);
        }

        @SuppressWarnings("unused")
        public List<Map<String,String>> getGroupInvitesHumanReadable() {
            return membersHumanReadable(group.invites);
        }

        private boolean roomContainsAttendee() {
            return (room != null) &&
                    (room.occupants != null) &&
                    (room.occupants.stream().anyMatch(m -> m.id == attendee.id));
        }

        @SuppressWarnings("unused")
        public boolean hasRoom() {
            return roomContainsAttendee();
        }

        @SuppressWarnings("unused")
        public String fieldRoomName() {
            if (roomContainsAttendee()) {
                return escape(room.name);
            } else {
                List<String> keys = new ArrayList<>();
                keys.add("");
                keys.addAll(eligibleRooms.stream().map(r -> r.id).toList());

                List<String> values = new ArrayList<>();
                values.add(Strings.inputForm.roomSelectPrompt);
                values.addAll(eligibleRooms.stream().map(r -> {
                    int occ = 0;
                    if (r.occupants != null)
                        occ = r.occupants.size();
                    return r.name + "&nbsp;(" + occ + "/" + r.size + ")";
                }).toList());

                return selector(mayEditAdmin(), ROOMASSIGN,
                        keys,
                        values,
                        "", 1);
            }
        }

        @SuppressWarnings("unused")
        public String fieldRoomSize() {
            if (roomContainsAttendee()) {
                return Long.toString(room.size);
            } else {
                return "";
            }
        }

        @SuppressWarnings("unused")
        public String fieldRoomFlags() {
            if (roomContainsAttendee() && room.flags != null) {
                return String.join(",&nbsp;", room.flags);
            } else {
                return "";
            }
        }

        @SuppressWarnings("unused")
        public String fieldRoomComments() {
            if (roomContainsAttendee() && room.comments != null) {
                return escape(room.comments);
            } else {
                return "";
            }
        }

        @SuppressWarnings("unused")
        public List<Map<String,String>> getRoomOccupantsHumanReadable() {
            return membersHumanReadable(room.occupants);
        }

        // roommates section

        public boolean showRoommatesSection() {
            // hotel rooms not currently supported
            return false;
        }

        public boolean isInitialReg() {
            return isNew();
        }

        /*
        public int bedsPerRoom() {
            return attendee.getRoomOption().bedsPerRoom;
        }

        public boolean maySelectRoommates() {
            return mayChangeRoommates();
        }

        public boolean maySeeRoommateAdminLinks() {
            return auth(Permission.ADMIN);
        }

        private int getRoommateId(int which) {
            switch (which) {
                case 3:
                    return attendee.getRoommate3Id();
                case 2:
                    return attendee.getRoommate2Id();
                default:
                    return attendee.getRoommateId();
            }
        }

        public String adminLinkToRoommate(int which) {
            int roommateId = getRoommateId(which);
            if (roommateId == 0)
                return Strings.inputForm.adminLinkRoommateNoneDescription;

            Attendee roommate = new Attendee(getDb().getDbConnection(), getPage().getAuth());
            try {
                roommate.getFromDB(roommateId);
            } catch (Exception e) {
                return Strings.inputForm.adminLinkRoommateInvalid + " '" + roommateId + "'";
            }
            return "<a href='input?param_id=" + roommateId + "#share'>" + roommate.getNick() + "</a>";
        }
         */

        /** Displays the status and identity of a roommate. If it is confirmed, the name is shown, otherwise only the ID. */
        /*
        private String viewRoommate(int which) {
            int roommateId = getRoommateId(which);
            if (roommateId <= 0)
                return Strings.inputForm.displayRoommateNoneDescription;

            Attendee roommate = fetchRoommate(roommateId);
            if (roommate.getId() <= 0)
                return Strings.inputForm.displayRoommateInvalid + " '" + roommateId + "'";

            // call check method on the roommate, asking them if they are ok with me as a roommate
            // either returns appropriate error message or empty string for ok
            String roommateStatus = roommate.checkOkayWithRoommate(attendee);

            boolean confirmed = roommateStatus.equals("");
            if (confirmed) {
                return "ID:&nbsp;(" + roommateId + " - " + escape(roommate.getNick()) + ")&nbsp;<b><font color=\"green\">CONFIRMED</font></b>";
            } else {
                return "ID:&nbsp;(" + roommateId + ")&nbsp;<b><font color=\"red\">NOT CONFIRMED: " + roommateStatus + "</font></b>";
            }
        }

        // either return appropriate error message or empty string for ok [void also is ok]
        private String displayRoommateStatus(int which) {
            int roommateId = getRoommateId(which);
            if (roommateId <= 0)
                return Strings.inputForm.displayRoommateNoneDescription;

            Attendee roommate = fetchRoommate(roommateId);
            if (roommate.getId() <= 0)
                return Strings.inputForm.displayRoommateInvalid + " '" + roommateId + "'";

            return roommate.checkOkayWithRoommate(attendee);
        }

        public class RoommateInfo {
            public int nr;
            public int outOf;
            public int id;
            public String caption;
            public String status;
            public boolean confirmed;
            public String statusViewonly;
        }

        public List<RoommateInfo> roomateInfos() {
            List<RoommateInfo> result = new ArrayList<>();
            int n = bedsPerRoom() - 1; // self is no roommate
            for (int i = 1; i <= n; i++) {
                RoommateInfo info = new RoommateInfo();
                info.nr = i;
                info.outOf = n;
                info.id = getRoommateId(i);
                info.caption = Strings.inputForm.roommateCaption(i, n);
                info.status = displayRoommateStatus(i);
                info.confirmed = "".equals(info.status);
                info.statusViewonly = viewRoommate(i);
            }

            return result;
        }

        public String fieldRoommate(int which, int displaySize, String style) {
            switch (which) {
                case 3:
                    return textField(mayChangeRoommates(), ROOMMATE3, Integer.toString(getRoommateId(which)), displaySize, 10, style);
                case 2:
                    return textField(mayChangeRoommates(), ROOMMATE2, Integer.toString(getRoommateId(which)), displaySize, 10, style);
                default:
                    return textField(mayChangeRoommates(), ROOMMATE1, Integer.toString(getRoommateId(which)), displaySize, 10, style);
            }
        }
         */
    }

    public VelocityRepresentation getVelocityRepresentation() {
        return new VelocityRepresentation();
    }
}
