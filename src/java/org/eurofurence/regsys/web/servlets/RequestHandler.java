package org.eurofurence.regsys.web.servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;

import java.util.Random;

/**
 *  Abstract base class that represents anything that can service a web request.
 */
public abstract class RequestHandler {
    private ServletContext servletContext;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private HttpMethod method;
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * This is called by the servlet before handle(), so request logging also has it.
     */
    public static String createRequestId() {
        Random r = new Random();
        return String.format("%08x", r.nextInt());
    }

    // new configuration

    protected final Configuration configuration = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Pass in the servlet context (for determining paths etc.)
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Pass in the request object
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Pass in the response object
     */
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Pass in the session object
     */
    public void setSession(HttpSession session) {
        this.session = session;
    }

    public HttpSession getSession() {
        return session;
    }

    /**
     * Pass in the http request method, such as GET
     */
    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public HttpMethod getMethod() {
        return method;
    }

    /**
     * main entry point into the request handler
     */
    public abstract void handle();
}
