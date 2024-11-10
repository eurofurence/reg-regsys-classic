package org.eurofurence.regsys.web.pages;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;

import java.util.ArrayList;
import java.util.HashMap;

public class RoomsPage extends Page {
    public boolean mayView() {
        return     hasPermission(Constants.Permission.ADMIN)
                || hasPermission(Constants.Permission.VIEW);
    }

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!mayView())
            return redirect("page/start");

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("page", this);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.roomsPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "rooms.vm";
    }

    @Override
    public ArrayList<String> getHeaderJsFileList() {
        ArrayList<String> lst = new ArrayList<>();
        lst.add("js/rooms_js.vm");
        return lst;
    }

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);
    }
}
