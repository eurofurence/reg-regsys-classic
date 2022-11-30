package org.eurofurence.regsys.web.servlets.page;

import org.eurofurence.regsys.backend.Logging;
import org.eurofurence.regsys.web.pages.Page;
import org.eurofurence.regsys.web.servlets.HttpMethod;
import org.eurofurence.regsys.web.servlets.error.ErrorServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class PageServlet extends HttpServlet {

    protected Page buildHandler(HttpServletRequest request, HttpServletResponse response, HttpMethod method) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals(""))
            pathInfo = "/";

        Page page = PageRequestHandlerFactory.createByPathInfo(pathInfo);
        page.setMethod(method);
        page.initialize(getServletContext(), request, response, request.getSession());
        return page;
    }

    protected void doAnyMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod method) {
        String requestId = Page.createRequestId();
        try {
            long started = System.currentTimeMillis();
            String logInfo = method.toString() + " " + request.getRequestURI() + " " + Thread.currentThread().getId();
            Logging.info("[" + requestId + "] request starting " + logInfo);
            try {
                Page page = buildHandler(request, response, method);
                page.setRequestId(requestId);
                page.handle();
                long done = System.currentTimeMillis();
                Logging.info("[" + requestId + "] request finished " + logInfo + " -> " + response.getStatus() + " (" + (done - started) + " ms)");
            } catch (Exception unexpectedException) {
                try {
                    long done = System.currentTimeMillis();
                    Logging.error("[" + requestId + "] request unexpected exception " + logInfo + " (" + (done - started) + " ms), stacktrace follows");
                    Logging.exception(unexpectedException);
                } catch (Exception ignored) { }
                ErrorServlet.handleUnexpectedException(unexpectedException, response);
            }
        } catch (Exception ignored) {
            // cannot do anything here, have already tried logging
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        doAnyMethod(request, response, HttpMethod.GET);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doAnyMethod(request, response, HttpMethod.POST);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        doAnyMethod(request, response, HttpMethod.PUT);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        doAnyMethod(request, response, HttpMethod.DELETE);
    }

    @Override
    public void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doOptions(request, response);
    }
}
