package org.eurofurence.regsys.web.servlets.content;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class StaticContentServlet extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String requestedPath = StringUtils.removeStart(req.getPathInfo(), "/");
        String resourcePath = String.format("/WEB-INF/html/%s", requestedPath);

        URL resource = getClass().getResource(resourcePath);
        logger.info("requesting static resource "+requestedPath);

        String contentType = getContentType(requestedPath);

        if(resource == null || contentType == null) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try {
                resp.getWriter().write("Not Found");
                resp.getWriter().close();
            } catch (IOException ignore) {
                // cannot help this
            }
        } else {
            resp.setContentType(contentType);
            resp.setStatus(HttpServletResponse.SC_OK);
            OutputStream o = null;
            InputStream i = null;
            try {
                o = resp.getOutputStream();
                i = getClass().getResourceAsStream(resourcePath);

                byte[] buf = new byte[8192];
                int length;
                while ((length = i.read(buf)) != -1) {
                    o.write(buf, 0, length);
                }
            } catch (IOException ignore) {
                // cannot help this
            } finally {
                if (o != null) {
                    try {
                        o.close();
                    } catch (IOException ignore) {
                    }
                }
                if (i != null) {
                    try {
                        i.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

    protected String getContentType(String fileName) {
        if (fileName == null) {
            return null;
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".jpg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".ico")) {
            return "image/vnd.microsoft.icon";
        } else {
            return null;
        }
    }
}
