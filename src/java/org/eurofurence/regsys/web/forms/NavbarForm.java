package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.config.Configuration;
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
    // configured strings

    @SuppressWarnings("unused")
    public String getConventionLongname() {
        return Strings.conf.conventionLongname;
    }

    @SuppressWarnings("unused")
    public String getConventionHumanReadableDate() {
        return Strings.conf.conventionHumanReadableDate;
    }

    // start page (always shown)

    @SuppressWarnings("unused")
    public String getStartPageURL() {
        return "start";
    }

    // accounting

    @SuppressWarnings("unused")
    public boolean showAccounting() {
        return getPage().hasPermission(Constants.Permission.ADMIN);
    }

    @SuppressWarnings("unused")
    public String getAccountingURL() {
        return "accounting";
    }

    // statistics

    @SuppressWarnings("unused")
    public boolean showStatistics() {
        return getPage().hasPermission(Constants.Permission.STATS);
    }

    @SuppressWarnings("unused")
    public String getStatisticsURL() {
        return "stats";
    }

    // list new / list all / search / show comments

    @SuppressWarnings("unused")
    public boolean showSearchAndLists() {
        return getPage().hasPermission(Constants.Permission.VIEW);
    }

    @SuppressWarnings("unused")
    public String getListNewURL() {
        return "list?search_status=0";
    }

    @SuppressWarnings("unused")
    public String getListAllURL() {
        return "list";
    }

    @SuppressWarnings("unused")
    public String getSearchURL() {
        return "search";
    }

    @SuppressWarnings("unused")
    public String getViewCommentsURL() {
        return "comments";
    }

    // export

    @SuppressWarnings("unused")
    public boolean showExport() {
        return getPage().hasPermission(Constants.Permission.EXPORT_CONBOOK);
    }

    @SuppressWarnings("unused")
    public String getExportURL() {
        return "export";
    }

    // bans

    @SuppressWarnings("unused")
    public boolean showManageBans() {
        return getPage().hasPermission(Constants.Permission.ADMIN);
    }

    @SuppressWarnings("unused")
    public String getManageBansURL() {
        return "bans";
    }

    // bulkmail announcements

    @SuppressWarnings("unused")
    public boolean showAnnouncementsEditAndSend() {
        return getPage().hasPermission(Constants.Permission.ANNOUNCE);
    }

    @SuppressWarnings("unused")
    public String getEditAnnouncementsURL() {
        return "announcements";
    }

    // groups

    private boolean groupsEnabled() {
        Configuration conf = getPage().getConfiguration();
        return conf.groups != null && conf.groups.enable;
    }

    @SuppressWarnings("unused")
    public boolean showManageGroups() {
        return getPage().hasPermission(Constants.Permission.VIEW) && groupsEnabled();
    }

    @SuppressWarnings("unused")
    public String getManageGroupsURL() {
        return "groups";
    }

    // rooms

    private boolean roomsEnabled() {
        Configuration conf = getPage().getConfiguration();
        return conf.rooms != null && conf.rooms.enable;
    }

    @SuppressWarnings("unused")
    public boolean showManageRooms() {
        return getPage().hasPermission(Constants.Permission.ADMIN) && roomsEnabled();
    }

    @SuppressWarnings("unused")
    public String getManageRoomsURL() {
        return "rooms";
    }

    // edit own registration

    private boolean loggedInRegisteredAttendeeStatusCondition(Predicate<Constants.MemberStatus> condition) {
        if (getPage().isLoggedIn()) {
            Attendee attendee;
            try {
                attendee = getPage().getLoggedInAttendee();
            } catch (UnauthorizedException e) {
                logger.warn("authorization expired - during navbar render, getLoggedInAttendee failed but isLoggedIn is true");
                return false;
            }
            if (attendee.id != null && attendee.id > 0) {
                Constants.MemberStatus status = getPage().getLoggedInAttendeeStatus();
                return condition.test(status);
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public boolean showEditOwnRegistration() {
//        if (getPage().isRegistrationEnabled()) {
//            return loggedInRegisteredAttendeeStatusCondition(status ->
//                    (status != Constants.MemberStatus.DELETED)
//                            && (status != Constants.MemberStatus.CANCELLED)
//            );
//        }
        return false;
    }

    @SuppressWarnings("unused")
    public String getEditOwnRegistrationURL() {
        return "input";
    }

    // view own payments

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public String getViewOwnPaymentsURL() {
        return "payment";
    }

    // new registration

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public String getNewRegistrationURL() {
        return "input?param_id=0";
    }

    // login / logout

    // web url construction methods, visibility methods

    @SuppressWarnings("unused") // used in navbar.vm (same for all other suppressions)
    public boolean showLogin() {
        return getPage().getConfiguration().testing != null && !getPage().isLoggedIn();
    }

    @SuppressWarnings("unused")
    public boolean showLogout() {
        return getPage().getConfiguration().testing != null && getPage().isLoggedIn();
    }

    @SuppressWarnings("unused")
    public String getLoginURL() {
        return "login";
    }

    // global visibility switch for info pages

    private boolean showMenu = true;

    @SuppressWarnings("unused")
    public boolean showMenu() {
        return showMenu;
    }

    public void disableMenu() {
        showMenu = false;
    }

    // imprint and privacy statement

    @SuppressWarnings("unused")
    public String getPrivacyStatementURL() {
        return Strings.conf.privacyStatementURL;
    }

    @SuppressWarnings("unused")
    public String getPrivacyStatementText() {
        return Strings.conf.privacyStatementText;
    }

    @SuppressWarnings("unused")
    public String getPrivacyStatementHTML() {
        return Strings.conf.privacyStatementHTML;
    }

    @SuppressWarnings("unused")
    public String getImprintURL() {
        return Strings.conf.imprintURL;
    }

    @SuppressWarnings("unused")
    public String getImprintText() {
        return Strings.conf.imprintText;
    }

    @SuppressWarnings("unused")
    public String getImprintHTML() {
        return Strings.conf.imprintHTML;
    }

    // design and logo

    @SuppressWarnings("unused")
    public String getNavbarBgcolor() {
        return Strings.conf.navbarBgcolor;
    }

    @SuppressWarnings("unused")
    public String getNavbarWidth() {
        return Strings.conf.navbarWidth;
    }

    @SuppressWarnings("unused")
    public String getNavbarLogoWidth() {
        return Strings.conf.navbarLogoWidth;
    }

    @SuppressWarnings("unused")
    public String getNavbarLogoHeight() {
        return Strings.conf.navbarLogoHeight;
    }

}
