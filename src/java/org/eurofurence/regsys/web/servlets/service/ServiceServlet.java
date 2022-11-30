package org.eurofurence.regsys.web.servlets.service;

import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.backend.Logging;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.web.servlets.HttpMethod;
import org.eurofurence.regsys.web.servlets.RequestHandler;
import org.eurofurence.regsys.web.servlets.error.ErrorServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServiceServlet extends HttpServlet {
    private static final long serialVersionUID = 2358875836630902347L;

    protected RequestHandler buildHandler(HttpServletRequest request, HttpServletResponse response, HttpMethod method) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals(""))
            pathInfo = "/";

        RequestHandler rh = ServiceRequestHandlerFactory.createByPathInfo(pathInfo);

        rh.setRequest(request);
        rh.setResponse(response);
        rh.setSession(request.getSession());
        rh.setServletContext(getServletContext());
        rh.setMethod(method);
        return rh;
    }

    protected void doAnyMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod method) {
        String requestId = RequestHandler.createRequestId();
        try {
            String logInfo = method.toString() + " " + request.getRequestURI() + " " + Thread.currentThread().getId();
            Logging.info("[" + requestId + "] service starting " + logInfo);
            try {
                RequestHandler handler = buildHandler(request, response, method);
                handler.setRequestId(requestId);
                handler.handle();
            } catch (Exception unexpectedException) {
                try {
                    Logging.error("[" + requestId + "] service unexpected exception " + logInfo);
                    Logging.exception(unexpectedException);
                } catch (Exception ignored) { }
                ErrorServlet.handleUnexpectedException(unexpectedException, response);
            }
            Logging.info("[" + requestId + "] service finished " + logInfo);
        } catch (Exception ignored) {
            // cannot do anything here, have already tried logging
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        addCorsHeadersForDevelopment(response);
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
        addCorsHeadersForDevelopment(response);
        super.doOptions(request, response);
    }

    protected ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    protected void addCorsHeadersForDevelopment(HttpServletResponse response) {
        if ("true".equals(configService.getConfig().web.enableDevCorsHeader)) {
            Logging.warn("Sending CORS headers to allow all origins. This setting is not suitable for production!");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
            response.addHeader("Access-Control-Allow-Headers", "content-type");
            response.addHeader("Access-Control-Expose-Headers", "Location");
        }
    }
}
