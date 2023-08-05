package org.eurofurence.regsys.web.servlets.error;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eurofurence.regsys.backend.Logging;
import org.eurofurence.regsys.web.servlets.HttpMethod;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Fallback error servlet. Since we catch all exceptions, this should never actually get used.
 */
public class ErrorServlet extends HttpServlet {
    protected void doAnyMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod method) {
        try {
            Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
            String logInfo = "error servlet " + method.toString() + " " + request.getRequestURI() + " " + Thread.currentThread().getId() + " "
                    + (throwable != null ? throwable.getMessage() : "(null)");
            Logging.error("[00000000] " + logInfo);
            if (throwable != null)
                Logging.exception(throwable);

            handleUnexpectedException(throwable, response);
        } catch (Exception unhandled) {
            try {
                Logging.exception(unhandled);
            } catch (Throwable ignored) {
                // cannot do anything here, have already tried logging
            }
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

    public static void handleUnexpectedException(Throwable e, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = new PrintWriter(response.getWriter());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        out.println("<p>Internal error while processing your request.</p>");
    }
}
