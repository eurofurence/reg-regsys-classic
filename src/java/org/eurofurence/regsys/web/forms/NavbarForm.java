package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Logging;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.enums.RoomManagementOption;
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
