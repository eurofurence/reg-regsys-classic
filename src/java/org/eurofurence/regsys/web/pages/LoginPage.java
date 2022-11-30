package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Strings;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * A login simulator page.
 *
 * Only available if jwt tokens for testing are configured.
 */
public class LoginPage extends Page {
    // ------------ constants -----------------------

    public static final String PARAM_ROLE       = "role";

    // ------------ constructors/initializers -----------------------

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);
    }

    @Override
    protected String getPageTitle() {
        return Strings.loginPage.pageTitle;
    }

    // ------------ visibility permission checks -----------------------

    // ------------ business methods -----------------------

    private void addOrDeleteCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        if ("".equals(value)) {
            cookie.setMaxAge(0);
        }
        getResponse().addCookie(cookie);
    }

    @Override
    public String getPageTemplateFile() {
        return "login.vm";
    }

    @Override
    public String handleRequest() throws ServletException {
        String message = "This page no longer handles logins.";

        if (configuration.testing != null) {
            String role = getRequest().getParameter(PARAM_ROLE);
            if ("admin".equals(role)) {
                addOrDeleteCookie(configuration.downstream.tokenCookieName, configuration.testing.adminToken);
                return redirect("page/start");
            } else if ("staff".equals(role)) {
                addOrDeleteCookie(configuration.downstream.tokenCookieName, configuration.testing.staffToken);
                return redirect("page/start");
            } else if ("user".equals(role)) {
                addOrDeleteCookie(configuration.downstream.tokenCookieName, configuration.testing.userToken);
                return redirect("page/start");
            } else if (isLoggedIn()) {
                addOrDeleteCookie(configuration.downstream.tokenCookieName, "");
                return redirect("page/start");
            } else {
                message = "Log in as " +
                        "<a href=\"login?role=admin\">admin</a>, " +
                        "<a href=\"login?role=staff\">staff</a>, " +
                        "<a href=\"login?role=user\">user</a>. ";
            }
        }

        // ---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- ----------

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("page", this);
        veloContext.put("message", message);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }
}
