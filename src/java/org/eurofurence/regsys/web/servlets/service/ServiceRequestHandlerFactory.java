package org.eurofurence.regsys.web.servlets.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eurofurence.regsys.web.services.DealersDenApi;
import org.eurofurence.regsys.web.services.NosecounterApi;
import org.eurofurence.regsys.web.services.NotFound;
import org.eurofurence.regsys.web.services.PackageApi;
import org.eurofurence.regsys.web.services.SecuritySystemApi;
import org.eurofurence.regsys.web.servlets.RequestHandler;

public class ServiceRequestHandlerFactory {
    protected static Map<String, Supplier<RequestHandler>> suppliersByPathInfo = new HashMap<>();

    protected static Supplier<RequestHandler> defaultSupplier = NotFound::new;

    static {
        suppliersByPathInfo.put("/nosecounter-api", NosecounterApi::new);
        suppliersByPathInfo.put("/dealers-den-api", DealersDenApi::new);
        suppliersByPathInfo.put("/security-system-api", SecuritySystemApi::new);
        suppliersByPathInfo.put("/package-api", PackageApi::new);
    }

    public static RequestHandler createByPathInfo(String pathInfo) {
        if (pathInfo == null)
            pathInfo = "";
        Supplier<RequestHandler> supp = suppliersByPathInfo.get(pathInfo);
        if (supp == null)
            supp = defaultSupplier;
        return supp.get();
    }
}
