package org.eurofurence.regsys.web.pages;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Logging;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;
import org.eurofurence.regsys.repositories.errors.ErrorDto;
import org.eurofurence.regsys.repositories.errors.ForbiddenException;
import org.eurofurence.regsys.repositories.errors.UnauthorizedException;
import org.eurofurence.regsys.web.forms.Form;
import org.eurofurence.regsys.web.forms.NavbarForm;
import org.eurofurence.regsys.web.servlets.RequestHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 *  Semi-abstract base class that represents a page in a web application.
 *
 *  Subclass to implement an actual page.
 *
 *  Offers some static convenience methods to render Velocity templates into a String.
 */
public abstract class Page extends RequestHandler {
    public static boolean velocityinitialized = false; 

    public static void initializeVelocity(ServletContext servletContext) {
        if (!velocityinitialized) {
            Velocity.setProperty("runtime.log", "/tmp/velocity.log");
            Velocity.setProperty("resource.loader.file.path", servletContext.getRealPath("WEB-INF/templates/pages"));
            Velocity.setProperty("parser.pool.size", "1000");

            Velocity.init();

            velocityinitialized = true;
        }
    }

    /**
     * static convenience method that takes a template and renders it into a String.
     *
     * @param servletContext call getServletContext() on your servlet
     * @param templateFilename String with path/filename for the template file relative to velocity templates directory inside WEB-INF
     * @param templateContext Map of known identifiers in your template, reconstruct before each call. Recommended is a java.util.HashMap<String,Object>
     * @return
     */
    public static String renderTemplate(ServletContext servletContext, String templateFilename, Map<String, Object> templateContext) {
          if (!velocityinitialized) {
              initializeVelocity(servletContext);
          }

          VelocityContext ctx = new VelocityContext(templateContext);

          java.io.Writer wri = new java.io.StringWriter();

          org.apache.velocity.app.Velocity.mergeTemplate(templateFilename, "UTF-8", ctx, wri);

          return wri.toString();
    }

    // setup

    protected ArrayList<String> errors = new ArrayList<String>();
    protected ArrayList<String> debugmessages = new ArrayList<String>();

    // page customization

    /**
     * Override this to customize the contents of the html TITLE tag
     */
    protected abstract String getPageTitle();

    public String getTitle() {
        return Strings.conf.conventionLongname + " - " + getPageTitle();
    }

    /**
     * Override this to return the filename of the main page template
     */
    public abstract String getPageTemplateFile();

    /**
     * Override this to include any javascript code files in the header
     */
    public ArrayList<String> getHeaderJsFileList() {
        return null;
    }

    // error handling

    /**
     * Clear the list of errors.
     */
    public synchronized void resetErrors() {
        errors = new ArrayList<String>();
    }

    /**
     * Add an error to the list.
     */
    public synchronized void addError(String message) {
        errors.add(message);
    }

    /**
     * Add errors coming from an exception to the list. If it wraps additional error info, that is added, too.
     */
    public synchronized void addException(Throwable t) {
        errors.add(t.getMessage());
        if (t instanceof DownstreamWebErrorException) {
            addWebErrors(((DownstreamWebErrorException) t).getErr());
        }
    }

    public void addWebErrors(ErrorDto err) {
        if (err != null && err.details != null) {
            err.details.forEach(
                    (k, v) -> {
                        if (v != null) {
                            v.forEach(
                                    msg -> addError(k + ": " + msg)
                            );
                        }
                    }
            );
        }
    }

    /**
     * Obtain the accumulated list of errors since instantiation or since the last reset.
     *
     * We actually produce a copy of the errors list, so we can html-escape each one
     */
    public ArrayList<String> getErrors() {
        if (errors == null || errors.isEmpty()) {
            return new ArrayList<String>();
        }

        ArrayList<String> escapedList = new ArrayList<String>(errors.size());
        for (int i = 0; i < errors.size(); i++) {
            escapedList.add(Form.escape(errors.get(i)));
        }

        return escapedList;
    }

    public boolean hasErrors() {
        return (errors != null) && (errors.size() > 0);
    }

    public int getErrorCount() {
        return (errors != null) ? errors.size() : 0;
    }

    // often used for showing / hiding things

    public boolean isRegistrationEnabled() {
        return getConfiguration().web.enableRegistration;
    }

    // used by messagePreparedForConvention.vm

    public boolean showBeforeConventionAnnouncement() {
        return getConfiguration().web.beforeConventionShowAnnouncement;
    }

    public boolean readonlyExceptAdmin() {
        return getConfiguration().web.readonlyExceptAdmin;
    }

    public boolean showBeforeConventionAnnouncementOffline() {
        return !isRegistrationEnabled();
    }

    public boolean showBeforeConventionAnnouncementReadonly() {
        return isRegistrationEnabled() && readonlyExceptAdmin();
    }

    // navbarForm - used by pretty much every page

    private NavbarForm navbarForm;

    /**
     * create and initialize the navigation bar form
     */
    public void createNavbarForm() {
        navbarForm = new NavbarForm();
        navbarForm.givePage(this); // it will get the auth from here
    }

    public NavbarForm getNavbarForm() {
        return navbarForm;
    }

    // ----------------- main request handling code --------------------

    public void refreshSessionTimeout() {
        getSession().setMaxInactiveInterval(getConfiguration().web.sessionTimeout);
    }

    /**
     * You should probably override this method.
     *
     * Minimally set up any forms and passed-in parameters in one convenient place.
     *
     * do NOT forget to call this version of initialize() from your implementation if you do.
     */
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        setServletContext(servletContext);
        setRequest(request);
        setResponse(response);
        setSession(session);

        createNavbarForm();
    }

    /**
     * main entry point into the page
     */
    public abstract String handleRequest() throws ServletException;

    /**
     * main entry point will become this when we are done migrating to a full servlet, right now it's unused
     */
    public void handle() {
        getResponse().setContentType("text/html; charset=utf-8");
        try {
            String page = handleRequest();
            getResponse().getWriter().print(page);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void handleException(Exception e) {
        try {
            Logging.exception(e);

            getSession().invalidate();

            String page = errorPage();
            getResponse().getWriter().print(page);
        } catch (Exception ignore) {
            // cannot do anything anymore - just ignore this
        }
    }

    public String errorPage() {
        String timeStamp = new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());

        return "<HTML>\n" +
                "  <HEAD>\n" +
                "    <TITLE>Registration - Internal Error Page</TITLE>\n" +
                "    <meta name=\"robots\" content=\"noindex\"/>\n" +
                "    <meta http-equiv=\"expires\" content=\"0\"/>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n" +
                "    <LINK REL=\"STYLESHEET\" TYPE=\"text/css\" HREF=\"../style.css\"/>\n" +
                "  </HEAD>\n" +
                "  <body bgcolor=\"#FFFFFF\">\n" +
                "    <table class=\"contentbox\">\n" +
                "      <tr>\n" +
                "        <td class=\"contentbox\">\n" +
                "\n" +
                "    <h3>An unhandled error was caught.</h3>\n" +
                "\n" +
                "    <p>This may just mean that we're conducting maintenance on the\n" +
                "    registration software. Please try again in a few minutes.</p>\n" +
                "\n" +
                "    <p>If the problem persists, please contact us via our \n" +
                "    <A href=\"https://help.eurofurence.org/\">contact page</A> \n" +
                "   to open a ticket.</p>\n" +
                "\n" +
                "   <p>Please explain what exactly you were doing when the\n" +
                "   error occurred and provide us with this timestamp.</p>\n" +
                "\n" +
                "   <p>Timestamp: " + timeStamp + "</p>\n" +
                "\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </body>\n\n" +
                "</HTML>\n";
    }

    /**
     * perform a jsp:forward
     *
     * IMPORTANT: use return like this!!!!!!!
     *
     * return forward ("page/start");
     */
    public String forward(String forwardTo) throws ServletException {
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/" + forwardTo);
        try {
            rd.forward(getRequest(), getResponse());
            return "";
        } catch (IOException e) {
            throw new ServletException("IOException while forwarding to " + forwardTo, e);
        }
    }

    /**
     * perform a jsp:forward with parameters, but coerce to a GET request
     *
     * IMPORTANT: use return like this!!!!!!!
     *
     * return forward ("page/start", map);
     */
    @SuppressWarnings("deprecation")
    public String forward(String forwardTo, Map<String, String> params) throws ServletException {
        StringBuilder paramStr = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> par: params.entrySet()) {
            paramStr.append(first ? "?" : "&");
            paramStr.append(par.getKey());
            paramStr.append("=");
            paramStr.append(URLEncoder.encode(par.getValue()));
            first = false;
        }
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/" + forwardTo + paramStr.toString());
        try {
            HttpServletRequest wrappedRequestAsGet = new HttpServletRequestWrapper(getRequest()) {
                @Override
                public String getMethod() {
                    return "GET";
                }
            };
            rd.forward(wrappedRequestAsGet, getResponse());
            return "";
        } catch (IOException e) {
            throw new ServletException("IOException while forwarding to " + forwardTo, e);
        }
    }

    /**
     * perform a redirect.
     *
     * IMPORTANT: use return like this!!!!!!!
     *
     * return redirect ("page/start");
     */
    public String redirect(String redirectTo) throws ServletException {
        String baseUrl = getConfiguration().web.serverUrl + getConfiguration().web.contextPath + "/";
        return redirectExternal(baseUrl + redirectTo);
    }

    /**
     * perform an external redirect.
     *
     * IMPORTANT: use return like this!!!!!!!
     *
     * return redirectExternal("https://something/");
     */
    public String redirectExternal(String fullUrl) throws ServletException {
        try {
            getResponse().sendRedirect(fullUrl);
            return "";
        } catch (IOException e) {
            throw new ServletException("IOException while redirecting to " + fullUrl, e);
        }
    }
}
