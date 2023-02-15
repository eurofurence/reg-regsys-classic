package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Logging;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.enums.RoomManagementOption;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.errors.UnauthorizedException;

import java.util.function.Predicate;

/**
 *  Represents the navigation elements used in the Navbar menu.
 *
 *  This Form understands these request parameters:
 *      (none)
 *
 *  Moves logic to control visibility of menu options out of the page templates.
 */
public class NavbarForm extends Form {
    // web url construction methods, visibility methods

    public boolean showLogin() {
        return getPage().getConfiguration().testing != null && !getPage().isLoggedIn();
    }
    public boolean showLogout() {
        return getPage().getConfiguration().testing != null && getPage().isLoggedIn();
    }

    public boolean isLoggedIn() {
        return getPage().isLoggedIn();
    }

    // configured strings

    public String getConventionLongname() {
        return Strings.conf.conventionLongname;
    }

    public String getConventionHumanReadableDate() {
        return Strings.conf.conventionHumanReadableDate;
    }

    // start page (always shown)

    public String getStartPageURL() {
        return "start";
    }

    // accounting

    public boolean showAccounting() {
        return getPage().hasPermission(Constants.Permission.ADMIN);
    }

    public String getAccountingURL() {
        return "accounting";
    }

    // statistics

    public boolean showStatistics() {
        return getPage().hasPermission(Constants.Permission.STATS);
    }

    public String getStatisticsURL() {
        return "stats";
    }

    // list new / list all / list rooms / search / show comments

    public boolean showSearchAndLists() {
        return getPage().hasPermission(Constants.Permission.VIEW);
    }

    public String getListNewURL() {
        return "list?search_status=0";
    }

    public String getListAllURL() {
        return "list";
    }

    public String getListUrgentCCRefundsURL() {
        return "list?search_status_Xnew=1&search_status_Xwait=1&search_type_Xguest=1&search_status_Xcanc=0&search_refundable_cc=1&search_refundable_other=-1&" +
                "search_flag_rchoice=1&search_flag_rdonate=0&search_flag_rrefund=0&search_flag_rurgent=1&search_flag_refunded=0";
    }

    public String getListCCRefundsURL() {
        return "list?search_status_Xnew=1&search_status_Xwait=1&search_type_Xguest=1&search_status_Xcanc=0&search_refundable_cc=1&search_refundable_other=-1&" +
                "search_flag_rchoice=1&search_flag_rdonate=0&search_flag_rrefund=1&search_flag_rurgent=0&search_flag_refunded=0";
    }

    public String getListUrgentBankRefundsURL() {
        return "list?search_status_Xnew=1&search_status_Xwait=1&search_type_Xguest=1&search_status_Xcanc=0&search_refundable_cc=-1&search_refundable_other=1&" +
                "search_flag_rchoice=1&search_flag_rdonate=0&search_flag_rrefund=0&search_flag_rurgent=1&search_flag_refunded=0";
    }

    public String getListBankRefundsURL() {
        return "list?search_status_Xnew=1&search_status_Xwait=1&search_type_Xguest=1&search_status_Xcanc=0&search_refundable_cc=-1&search_refundable_other=1&" +
                "search_flag_rchoice=1&search_flag_rdonate=0&search_flag_rrefund=1&search_flag_rurgent=0&search_flag_refunded=0";
    }

    public boolean showRoomRequestList() {
        return getPage().hasPermission(Constants.Permission.VIEW) && (HardcodedConfig.ROOM_CONFIGURATION == RoomManagementOption.ROOMS_HOUSE_GROUPS);
    }

    public String getListRoomRequestsURL() {
        return "roomrequest?action=list";
    }

    public boolean showRoomList() {
        return getPage().hasPermission(Constants.Permission.VIEW) && (HardcodedConfig.ROOM_CONFIGURATION == RoomManagementOption.ROOMS_HOUSE_GROUPS);
    }

    public String getListRoomsURL() {
        return "room?action=list";
    }

    public boolean showRoomAssignment() {
        return getPage().hasPermission(Constants.Permission.ADMIN) && (HardcodedConfig.ROOM_CONFIGURATION == RoomManagementOption.ROOMS_HOUSE_GROUPS);
    }

    public String getAssignRoomsURL() {
        return "roomassign";
    }

    public String getSearchURL() {
        return "search";
    }

    public String getViewCommentsURL() {
        return "comments";
    }

    // export

    public boolean showExport() {
        return getPage().hasPermission(Constants.Permission.EXPORT_CONBOOK);
    }

    public String getExportURL() {
        return "export";
    }

    // bans

    public boolean showManageBans() {
        return getPage().hasPermission(Constants.Permission.ADMIN);
    }

    public String getManageBansURL() {
        return "bans";
    }

    // bulkmail announcements

    public boolean showAnnouncementsEditAndSend() {
        return getPage().hasPermission(Constants.Permission.ANNOUNCE);
    }

    public String getEditAnnouncementsURL() {
        return "announcements";
    }

    public String getSendAnnouncementsURL() {
        return "bulkmail";
    }

    // edit own registration

    private boolean loggedInRegisteredAttendeeStatusCondition(Predicate<Constants.MemberStatus> condition) {
        if (getPage().isLoggedIn()) {
            Attendee attendee;
            try {
                attendee = getPage().getLoggedInAttendee();
            } catch (UnauthorizedException e) {
                Logging.warn("authorization expired - during navbar render, getLoggedInAttendee failed but isLoggedIn is true");
                return false;
            }
            if (attendee.id != null && attendee.id > 0) {
                Constants.MemberStatus status = getPage().getLoggedInAttendeeStatus();
                return condition.test(status);
            }
        }
        return false;
    }

    public boolean showEditOwnRegistration() {
//        if (getPage().isRegistrationEnabled()) {
//            return loggedInRegisteredAttendeeStatusCondition(status ->
//                    (status != Constants.MemberStatus.DELETED)
//                            && (status != Constants.MemberStatus.CANCELLED)
//            );
//        }
        return false;
    }

    public String getEditOwnRegistrationURL() {
        return "input";
    }

    // view own payments

    public boolean showViewOwnPayments() {
//        if (getPage().isRegistrationEnabled()) {
//            return loggedInRegisteredAttendeeStatusCondition(status ->
//                    (status != Constants.MemberStatus.DELETED)
//                            && (status != Constants.MemberStatus.CANCELLED)
//                            && (status != Constants.MemberStatus.NEW)
//            );
//        }
        return false;
    }

    public String getViewOwnPaymentsURL() {
        return "payment";
    }

    // room group functions

    public boolean showMyRoomGroup() {
        if (HardcodedConfig.ROOM_CONFIGURATION != RoomManagementOption.ROOMS_HOUSE_GROUPS)
            return false;

        return false;
    }

    public String getMyRoomGroupURL() {
        return "roomrequest";
    }

    // new registration

    public boolean showNewRegistration() {
//        if (getPage().isRegistrationEnabled()) {
//            if (getPage().isLoggedIn()) {
//                if (!getPage().readonlyExceptAdmin()) {
//                    Attendee attendee;
//                    try {
//                        attendee = getPage().getLoggedInAttendee();
//                    } catch (UnauthorizedException e) {
//                        Logging.warn("authorization expired - during navbar render, getLoggedInAttendee failed but isLoggedIn is true");
//                        return false;
//                    }
//                    if (attendee.id == null || attendee.id <= 0) {
//                        return true;
//                    }
//                }
//            }
//        }
        return false;
    }

    public String getNewRegistrationURL() {
        return "input?param_id=0";
    }

    // login / logout

    public String getLoginURL() {
        return "login";
    }

    // imprint and privacy statement

    public String getPrivacyStatementURL() {
        return Strings.conf.privacyStatementURL;
    }

    public String getPrivacyStatementText() {
        return Strings.conf.privacyStatementText;
    }

    public String getPrivacyStatementHTML() {
        return Strings.conf.privacyStatementHTML;
    }

    public String getImprintURL() {
        return Strings.conf.imprintURL;
    }

    public String getImprintText() {
        return Strings.conf.imprintText;
    }

    public String getImprintHTML() {
        return Strings.conf.imprintHTML;
    }

    // design and logo

    public String getNavbarBgcolor() {
        return Strings.conf.navbarBgcolor;
    }

    public String getNavbarWidth() {
        return Strings.conf.navbarWidth;
    }

    public String getNavbarLogoWidth() {
        return Strings.conf.navbarLogoWidth;
    }

    public String getNavbarLogoHeight() {
        return Strings.conf.navbarLogoHeight;
    }

}
