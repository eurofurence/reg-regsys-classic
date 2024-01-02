package org.eurofurence.regsys.web.servlets.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.web.servlets.HttpMethod;
import org.eurofurence.regsys.web.servlets.RequestHandler;
import org.eurofurence.regsys.web.servlets.base.BaseServlet;
import org.eurofurence.regsys.web.servlets.error.ErrorServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;

public class ServiceServlet extends BaseServlet {
    Logger logger = LoggerFactory.getLogger(this.getClass());

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

    @Override
    protected void doAnyMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod method) {
        String requestId = RequestHandler.createRequestId();
        try {
            MDC.put("requestid", requestId);
            String logInfo = method.toString() + " " + request.getRequestURI() + " " + Thread.currentThread().getId();
            logger.info("service starting " + logInfo);
            try {
                RequestHandler handler = buildHandler(request, response, method);
                handler.setRequestId(requestId);
                handler.handle();
            } catch (Exception unexpectedException) {
                try {
                    logger.error("service unexpected exception " + logInfo, unexpectedException);
                } catch (Exception ignored) { }
                ErrorServlet.handleUnexpectedException(unexpectedException, response);
            }
            logger.info("service finished " + logInfo);
        } catch (Exception ignored) {
            // cannot do anything here, have already tried logging
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        addCorsHeadersForDevelopment(response);
        doAnyMethod(request, response, HttpMethod.GET);
    }

    @Override
    public void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        addCorsHeadersForDevelopment(response);
        super.doOptions(request, response);
    }

    protected ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    protected void addCorsHeadersForDevelopment(HttpServletResponse response) {
        if ("true".equals(configService.getConfig().web.enableDevCorsHeader)) {
            logger.warn("Sending CORS headers to allow all origins. This setting is not suitable for production!");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
            response.addHeader("Access-Control-Allow-Headers", "content-type");
            response.addHeader("Access-Control-Expose-Headers", "Location");
        }
    }
}
