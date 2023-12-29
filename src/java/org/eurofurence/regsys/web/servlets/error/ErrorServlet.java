package org.eurofurence.regsys.web.servlets.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eurofurence.regsys.web.servlets.HttpMethod;
import org.eurofurence.regsys.web.servlets.base.BaseServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Fallback error servlet. Since we catch all exceptions, this should never actually get used.
 */
public class ErrorServlet extends BaseServlet {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    protected void doAnyMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod method) {
        try {
            Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
            String logInfo = "error servlet " + method.toString() + " " + request.getRequestURI() + " " + Thread.currentThread().getId() + " "
                    + (throwable != null ? throwable.getMessage() : "(null)");
            if (throwable != null) {
                logger.error(logInfo, throwable);
            } else {
                logger.error(logInfo);
            }

            handleUnexpectedException(throwable, response);
        } catch (Exception unhandled) {
            try {
                logger.error("encountered another exception during top level exception logging", unhandled);
            } catch (Throwable ignored) {
                // cannot do anything here, have already tried logging
            }
        }
    }

    public static void handleUnexpectedException(Throwable e, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = new PrintWriter(response.getWriter());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        out.println("<p>Internal error while processing your request.</p>");
    }
}
