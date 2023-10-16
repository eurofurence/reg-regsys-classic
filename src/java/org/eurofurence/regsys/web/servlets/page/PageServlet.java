package org.eurofurence.regsys.web.servlets.page;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eurofurence.regsys.web.pages.Page;
import org.eurofurence.regsys.web.servlets.HttpMethod;
import org.eurofurence.regsys.web.servlets.error.ErrorServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;

public class PageServlet extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(this.getClass());

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
            MDC.put("requestid", requestId);
            long started = System.currentTimeMillis();
            String logInfo = method.toString() + " " + request.getRequestURI() + " " + Thread.currentThread().getId();
            logger.info("request starting " + logInfo);
            try {
                Page page = buildHandler(request, response, method);
                page.setRequestId(requestId);
                page.handle();
                long done = System.currentTimeMillis();
                logger.info("request finished " + logInfo + " -> " + response.getStatus() + " (" + (done - started) + " ms)");
            } catch (Exception unexpectedException) {
                try {
                    long done = System.currentTimeMillis();
                    logger.error("request unexpected exception " + logInfo + " (" + (done - started) + " ms)", unexpectedException);
                } catch (Exception ignored) { }
                ErrorServlet.handleUnexpectedException(unexpectedException, response);
            }
        } catch (Exception ignored) {
            // cannot do anything here, have already tried logging
        } finally {
            MDC.clear();
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
