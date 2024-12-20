package org.eurofurence.regsys.web.pages;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.persistence.DbDataException;
import org.eurofurence.regsys.backend.persistence.DbException;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;
import org.eurofurence.regsys.web.forms.Camtv8BookingForm;
import org.eurofurence.regsys.web.forms.Camtv8UploadForm;
import org.eurofurence.regsys.web.forms.PaymentForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *  Represents the account statement XML upload page in the registration system.
 *
 *  Admin only.
 *
 *  Initially, this page presents a file selection box to select a CAMT v8 052 or 053 XML file for upload.
 *  When submitted with one, it parses it and displays best matches to incoming payments for booking.
 *
 *  This page understands these request parameters:
 *      mode             before-upload (default), upload, book
 *  (plus any parameters introduced by forms)
 *
 *  This page uses these forms:
 *      NavbarForm
 *      Camtv8UploadForm
 */
public class Camtv8ImportPage extends Page {
    // constants for parameter names
    public static final String MODE = "mode";

    public static final String MODE_BEFORE_UPLOAD = "before-upload";
    public static final String MODE_UPLOAD = "upload";
    public static final String MODE_BOOK = "book";

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createCamtv8UploadForm();
        createCamtv8BookingForm();
    }

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!isLoggedIn() || !hasPermission(Constants.Permission.ADMIN))
            return redirect("page/start");

        String mode = getRequest().getParameter(MODE);
        if (mode == null || mode.isEmpty()) {
            mode = MODE_BEFORE_UPLOAD;
        }

        List<String[]> entrylines = new ArrayList<>();

        try {
            if (mode.equals(MODE_BEFORE_UPLOAD)) {
                getCamtv8UploadForm().initialize();
                getCamtv8BookingForm().initialize();
            } else if (mode.equals(MODE_UPLOAD)) {
                getCamtv8UploadForm().processUpload();
                getCamtv8BookingForm().loadUnpaidAttendees();

                if (hasErrors()) {
                    getCamtv8UploadForm().initialize();
                    getCamtv8BookingForm().initialize();
                    mode = MODE_BEFORE_UPLOAD;
                } else {
                    getCamtv8UploadForm().storeEntriesInSession(getSession());
                    getCamtv8BookingForm().setEntries(getCamtv8UploadForm().getEntries());
                }

                getCamtv8BookingForm().matchup();
                entrylines = getCamtv8BookingForm().getEntriesWebListing();
            } else if (mode.equals(MODE_BOOK)) {
                getCamtv8UploadForm().getEntriesFromSession(getSession());

                if (hasErrors()) {
                    getCamtv8UploadForm().initialize();
                    getCamtv8BookingForm().initialize();
                    mode = MODE_BEFORE_UPLOAD;
                } else {
                    getCamtv8BookingForm().setEntries(getCamtv8UploadForm().getEntries());
                    getCamtv8BookingForm().processBooking();
                }
            } else {
                throw new DbException("invalid mode - this is a bug");
            }
        } catch (DbException e) {
            addError(e.getMessage());
        } catch (DownstreamWebErrorException e) {
            resetErrors();
            addError(Strings.searchForm.nobodyFound);
            getCamtv8UploadForm().addWebErrors(e.getErr());
        } catch (DownstreamException e) {
            resetErrors();
            addError(e.getMessage());
        }

        HashMap<String, Object> veloContext = new HashMap<>();
        veloContext.put("page", this);
        veloContext.put("mode", mode);
        veloContext.put("navbar", getNavbarForm());
        veloContext.put("uploadform", getCamtv8UploadForm());
        veloContext.put("bookingform", getCamtv8BookingForm());
        veloContext.put("entrylines", entrylines);
        veloContext.put("bookinglog", getCamtv8BookingForm().getBookingLog());

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.camtv8ImportPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "camtv8import.vm";
    }

    public ArrayList<String> getHeaderJsFileList() {
        if (hasPermission(Constants.Permission.ADMIN)) {
            ArrayList<String> jsFiles = new ArrayList<>();
            jsFiles.add("js/camtv8_js.vm");
            return jsFiles;
        }
        return null;
    }

    public boolean isAdmin() {
        return hasPermission(Constants.Permission.ADMIN);
    }

    // upload Form

    private Camtv8UploadForm uploadForm;

    public void createCamtv8UploadForm() {
        uploadForm = new Camtv8UploadForm();
        uploadForm.givePage(this); // it will get the auth from here
    }

    public Camtv8UploadForm getCamtv8UploadForm() {
        return uploadForm;
    }

    // booking form

    private Camtv8BookingForm bookingForm;

    public void createCamtv8BookingForm() {
        bookingForm = new Camtv8BookingForm();
        bookingForm.givePage(this);
    }

    public Camtv8BookingForm getCamtv8BookingForm() {
        return bookingForm;
    }
}
