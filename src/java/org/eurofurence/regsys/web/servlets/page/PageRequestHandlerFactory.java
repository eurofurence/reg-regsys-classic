package org.eurofurence.regsys.web.servlets.page;

import org.eurofurence.regsys.web.pages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PageRequestHandlerFactory {
    protected static Map<String, Supplier<Page>> suppliersByPathInfo = new HashMap<>();

    protected static Supplier<Page> defaultSupplier = NotFoundPage::new;

    static {
        suppliersByPathInfo.put("/start", StartPage::new);
    }

    public static Page createByPathInfo(String pathInfo) {
        if (pathInfo == null)
            pathInfo = "";
        Supplier<Page> supp = suppliersByPathInfo.get(pathInfo);
        if (supp == null)
            supp = defaultSupplier;
        return supp.get();
    }
}
