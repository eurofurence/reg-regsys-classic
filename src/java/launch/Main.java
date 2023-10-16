package launch;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.lowlevel.LowlevelClient;
import org.eurofurence.regsys.web.pages.Page;
import org.eurofurence.regsys.web.servlets.content.StaticContentServlet;
import org.eurofurence.regsys.web.servlets.error.ErrorServlet;
import org.eurofurence.regsys.web.servlets.page.PageServlet;
import org.eurofurence.regsys.web.servlets.service.ServiceServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.logging.LogManager;

public class Main {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void Run() throws Exception {
        Tomcat tomcat = new Tomcat();

        // load tomcat juli logging configuration - logback.xml is automatically loaded

        String loggingPropertiesPath = "/WEB-INF/config/logging-juli.properties";
        try (InputStream inputStream = getClass().getResourceAsStream(loggingPropertiesPath)) {
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (Exception e) {
            System.out.println ("Logging configuration failed to load, bailing out: " + e.getMessage());
            throw e;
        }

        // set up webapp and servlets

        String webappDirLocation = "webapps";

        tomcat.setPort(8080);
        tomcat.getConnector();

        // provide static file server under ./webapps
        StandardContext context = (StandardContext) tomcat.addContext("", new File(webappDirLocation).getAbsolutePath());

        // ctx.addMimeMapping("ext", "type");

        // so far unported portion of web.xml

        //  <display-name>Eurofurence Registration System</display-name>
        //  <description>
        //        The Eurofurence Registration System, (C) 1999-2022 Eurofurence e.V.
        //  </description>

        //     <filter>
        //        <filter-name>setCharacterEncodingFilter</filter-name>
        //        <filter-class>org.apache.catalina.filters.SetCharacterEncodingFilter</filter-class>
        //        <init-param>
        //            <param-name>encoding</param-name>
        //            <param-value>UTF-8</param-value>
        //        </init-param>
        //    </filter>
        //
        //    <filter-mapping>
        //        <filter-name>setCharacterEncodingFilter</filter-name>
        //        <url-pattern>/*</url-pattern>
        //    </filter-mapping>
        //
        //    <listener>
        //        <listener-class>
        //            org.eurofurence.regsys.web.servlets.ServletContextListenerImpl
        //        </listener-class>
        //    </listener>

        // <session-config>
        //      <cookie-config>
        //          <path>/</path>
        //      </cookie-config>
        //    </session-config>

        String serviceServletName = "ServiceServlet";
        String pageServletName = "PageServlet";
        String errorServletName = "ErrorServlet";
        String staticServletName = "StaticServlet";

        tomcat.addServlet("", serviceServletName, new ServiceServlet());
        context.addServletMappingDecoded("/service/*", serviceServletName);

        tomcat.addServlet("", pageServletName, new PageServlet());
        context.addServletMappingDecoded("/page/*", pageServletName);

        // ErrorPage ep = new ErrorPage();  ep.setErrorCode(500);  ep.setLocation("/error.html");  ctx.addErrorPage(ep);
        tomcat.addServlet("", errorServletName, new ErrorServlet());
        context.addServletMappingDecoded("/error", errorServletName);

        tomcat.addServlet("", staticServletName, new StaticContentServlet());
        context.addServletMappingDecoded("/*", staticServletName);

        // application level setup

        logger.info("loading configuration...");
        Configuration configuration = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();
        if (configuration.testing != null) {
            logger.warn("testing tokens are configured. NOT INTENDED FOR PRODUCTION!!!");
        }

        logger.info("creating downstream client...");
        new LowlevelClient().getClient();

        logger.info("initializing velocity template engine...");
        // TODO is there another way to get an existing application context?
        Page.initializeVelocity(new ApplicationContext(context));

        logger.info("context successfully initialized");

        // start tomcat

        tomcat.start();
        tomcat.getServer().await();
    }

    public static void main(String[] args) {
        try {
            new Main().Run();
        } catch (Throwable t) {
            System.out.println ("Application threw top level exception: " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }
}
