package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Strings;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class StartPage extends Page {
    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("page", this);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.startPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "start.vm";
    }

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);
    }

    public String getConNameLong() {
        return Strings.conf.conventionLongname;
    }

    private java.util.Date getRegstartDate() throws ParseException {
        java.text.DateFormat df = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

        return df.parse(Strings.conf.goLiveTime);
    }

    public String getRegstartDateFor(String timezone) {
        java.text.DateFormat dfOut = new java.text.SimpleDateFormat(Strings.conf.goLiveDateFormat, Locale.ENGLISH);
        dfOut.setTimeZone(TimeZone.getTimeZone(timezone));

        try {
            java.util.Date reg = getRegstartDate();
            return Strings.conf.goLiveDatePrefix + dfOut.format(reg) + " " + timezone;
        } catch (ParseException e) {
            return Strings.startPage.unexpectedError;
        }
    }

    public String getCountdown() {
        java.util.Date now = new java.util.Date();
        java.util.Date reg;

        try {
            reg = getRegstartDate();

            long diff = reg.getTime() - now.getTime();

            if (diff >= 0) {
                long total = diff / 1000L;
                long secs = total % 60L;
                total /= 60L;
                long mins = total % 60L;
                total /= 60L;
                long hours = total % 24L;
                total /= 24L;
                long days = total;

                return Strings.startPage.timeUntilStart(days, hours, mins, secs);
            } else {
                return Strings.startPage.shouldHaveStarted;
            }
        } catch (ParseException e) {
            return Strings.startPage.unexpectedError;
        }
    }

}
