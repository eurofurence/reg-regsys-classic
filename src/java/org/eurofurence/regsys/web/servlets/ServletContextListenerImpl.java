package org.eurofurence.regsys.web.servlets;

import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.backend.Logging;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.web.pages.Page;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextListenerImpl implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Logging.info("loading new configuration...");
        Configuration configuration = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();

        Logging.info("initializing velocity template engine...");
        Page.initializeVelocity(sce.getServletContext());

        Logging.info("context successfully initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Logging.info("context successfully destroyed");
    }
}
