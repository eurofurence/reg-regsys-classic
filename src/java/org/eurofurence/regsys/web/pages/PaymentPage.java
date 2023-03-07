package org.eurofurence.regsys.web.pages;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.persistence.DbDataException;
import org.eurofurence.regsys.backend.persistence.DbException;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;
import org.eurofurence.regsys.web.forms.PaymentForm;

/**
 *  Represents the payment page in the registration system.
 *
 *  Regular attendees may view their own payments, but make no changes. Admins
 *  can manage everyone's payments.
 *
 *  This page understands these request parameters:
 *      param_id             id of the attendee
 *  (plus any parameters introduced by forms)
 *
 *  This page uses these forms:
 *      NavbarForm
 *      PaymentForm
 */
public class PaymentPage extends Page {
    // constants for parameter names
    public static final String ATTENDEE_ID = "param_id"; // id of attendee to work with

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createPaymentForm();
    }

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!hasPermission(Constants.Permission.ADMIN))
            return redirect("page/start"); // use new regsys now!

        if (!isLoggedIn()) {
            return redirect("page/start");
        }

        if (!isRegistrationEnabled()) {
            return forward("page/start");
        }

        long the_id = 0;
        ArrayList<String[]> paymentlines = new ArrayList<String[]>();

        try {
            if (hasPermission(Constants.Permission.ADMIN) || hasPermission(Constants.Permission.VIEW)) {
                try {
                    the_id = Long.parseLong(getRequest().getParameter(ATTENDEE_ID));
                    if (the_id < 0) the_id = 0;
                } catch (Exception ef) {
                    try {
                        Attendee me = getLoggedInAttendee();
                        the_id = me.id;
                    } catch (Exception eg) {
                        the_id = 0; // fallback
                    }
                }
            } else {
                // simply ignore parameter
                Attendee me = getLoggedInAttendee();
                the_id = me.id;
            }

            if (the_id == 0)
                throw new DbDataException(Strings.paymentPage.needsAttendeeId);

            getPaymentForm().initializeForAttendee(the_id);

            if (!hasPermission(Constants.Permission.ADMIN)) {
                if (!hasPermission(Constants.Permission.VIEW) && !isMyBadgeNumber(the_id)) {
                    return forward("page/start");
                }
                // USER MODE: we are authenticated, show payments
                // VIEW MODE: show payments as for that user, no functionality
            } else {
                // ADMIN MODE: Allow full payment management functionality
                try {
                    getPaymentForm().processAdminRequest(getRequest());
                } catch (Exception eg) {
                    addError(eg.getMessage());
                }
            }

            paymentlines = getPaymentForm().getPaymentWebListing(the_id);

        } catch (DbException e) {
            addError(e.getMessage());
        } catch (DownstreamWebErrorException e) {
            resetErrors();
            addError(Strings.searchForm.nobodyFound);
            getPaymentForm().addWebErrors(e.getErr());
        } catch (DownstreamException e) {
            resetErrors();
            addError(e.getMessage());
        }

        HashMap<String, Object> veloContext = new HashMap<>();
        veloContext.put("page", this);
        veloContext.put("navbar", getNavbarForm());
        veloContext.put("form", getPaymentForm());
        veloContext.put("paymentlines", paymentlines);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.paymentPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "payment.vm";
    }

    public ArrayList<String> getHeaderJsFileList() {
        if (hasPermission(Constants.Permission.ADMIN)) {
            ArrayList<String> jsFiles = new ArrayList<String>();
            jsFiles.add("js/payment_js.vm");
            return jsFiles;
        }
        return null;
    }

    public boolean isAdmin() {
        return hasPermission(Constants.Permission.ADMIN);
    }

    // payment Form

    private PaymentForm paymentForm;

    public void createPaymentForm() {
        paymentForm = new PaymentForm();
        paymentForm.givePage(this); // it will get the auth from here
    }

    public PaymentForm getPaymentForm() {
        return paymentForm;
    }
}
