package launch;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.eurofurence.regsys.web.servlets.content.StaticContentServlet;
import org.eurofurence.regsys.web.servlets.error.ErrorServlet;
import org.eurofurence.regsys.web.servlets.page.PageServlet;
import org.eurofurence.regsys.web.servlets.service.ServiceServlet;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {

        String webappDirLocation = "webapps";
        Tomcat tomcat = new Tomcat();

        tomcat.setPort(8080);
        tomcat.getConnector();

        // provide static file server under ./webapps
        Context context = tomcat.addContext("", new File(webappDirLocation).getAbsolutePath());

        // ctx.addMimeMapping("ext", "type");

        // <session-config>
        //      <cookie-config>
        //          <path>/</path>
        //      </cookie-config>
        //    </session-config>

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

        tomcat.start();
        tomcat.getServer().await();
    }}
