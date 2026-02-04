package org.eurofurence.regsys.web.pages;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.web.forms.AccountingSearchForm;

import java.util.HashMap;

public class CreditCardStatusCheckPage extends Page {
    // ------------ constants -----------------------

    // constants for the parameter names

    // ------------ attributes -----------------------

    private boolean showlisting = true; // handle cancel

    // ------------ business methods -----------------------

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!hasPermission(Constants.Permission.ADMIN)) {
            return forward("page/start");
        }

        getAccountingSearchForm().getParameterParser().setupForStatusCheck(getRequest());
        if (showlisting) {
            if (!getAccountingSearchForm().find()) {
                addError("no matching payments found");
                showlisting = false;
            }
        }

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("page", this);
        veloContext.put("form", getAccountingSearchForm().getVelocityRepresentation());

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.creditCardStatusCheckPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "creditcardstatuscheck.vm";
    }

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createAccountingSearchForm();
    }

    // ------------ velocity representation -----------------------

    public boolean showListing() {
        return showlisting;
    }

    // ------- accounting search form ------------------

    private AccountingSearchForm accountingSearchForm;

    public void createAccountingSearchForm() {
        accountingSearchForm = new AccountingSearchForm();
        accountingSearchForm.givePage(this);
        accountingSearchForm.initialize();
    }

    public AccountingSearchForm getAccountingSearchForm() {
        return accountingSearchForm;
    }
}
