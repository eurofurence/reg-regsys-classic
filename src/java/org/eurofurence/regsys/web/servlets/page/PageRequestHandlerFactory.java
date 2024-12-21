package org.eurofurence.regsys.web.servlets.page;

import org.eurofurence.regsys.web.pages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PageRequestHandlerFactory {
    protected static Map<String, Supplier<Page>> suppliersByPathInfo = new HashMap<>();

    protected static Supplier<Page> defaultSupplier = NotFoundPage::new;

    static {
        suppliersByPathInfo.put("/accounting", AccountingPage::new);
        suppliersByPathInfo.put("/announcements", AnnouncementsPage::new);
        suppliersByPathInfo.put("/bans", BansPage::new);
        suppliersByPathInfo.put("/bulkmail", BulkmailPage::new);
        suppliersByPathInfo.put("/camtv8", Camtv8ImportPage::new);
        suppliersByPathInfo.put("/comments", CommentsPage::new);
        suppliersByPathInfo.put("/email", ResendStatusEmailPage::new);
        suppliersByPathInfo.put("/export", ExportPage::new);
        suppliersByPathInfo.put("/groups", GroupsPage::new);
        suppliersByPathInfo.put("/input", InputPage::new);
        suppliersByPathInfo.put("/list", ListPage::new);
        suppliersByPathInfo.put("/mass-approve", MassApprovePage::new);
        suppliersByPathInfo.put("/payment", PaymentPage::new);
        suppliersByPathInfo.put("/rooms", RoomsPage::new);
        suppliersByPathInfo.put("/search", SearchPage::new);
        suppliersByPathInfo.put("/sepa", SepaPage::new);
        suppliersByPathInfo.put("/start", StartPage::new);
        suppliersByPathInfo.put("/stats", StatsPage::new);

        suppliersByPathInfo.put("/login", LoginPage::new);
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
