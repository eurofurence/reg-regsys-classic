package org.eurofurence.regsys.backend;

/**
 * Externalize human-readable message strings from the backend.
 *
 * The frontends are expected to externalize strings using templates, but this is somewhat impractical for the backend, where
 * - error messages need to work even in low memory conditions or all other kinds of weirdnesses
 * - error messages may contain a fixed number of parameters or need the contents of an exception
 * - error messages need constants defined in Config, LocalConfig, Constants
 *
 * Yes, with a lot of work this could be moved to a full-blown template system. We might even get around
 * to that at some point. On the other hand, we do not wish for conventions to have to wade through all the
 * code to find and adapt messages.
 *
 * So the compromise is this class that has messages as variables in static subclasses. Ok to edit,
 * but we have some compiler checks, and access to all the java constants.
 *
 * Note you WILL still need to go through the following classes, they do have lots of configuration and string constants
 *   Config.java
 *   Constants.java
 *   LocalConfig.java
 *
 * To switch between implementations, just change the extends declaration, for example into "extends StringsMMC"
 */
public class Strings extends StringsEF {
    public static ConfigStrings conf = new ConfigStrings();

    public static UtilMsgs util = new UtilMsgs();

    public static AccountingPageMsgs accountingPage = new AccountingPageMsgs();
    public static AnnouncementPageMsgs announcementPage = new AnnouncementPageMsgs();
    public static BansPageMsgs bansPage = new BansPageMsgs();
    public static BulkmailPageMsgs bulkmailPage = new BulkmailPageMsgs();
    public static CommentsPageMsgs commentsPage = new CommentsPageMsgs();
    public static Camtv8ImportPageMsgs camtv8ImportPage = new Camtv8ImportPageMsgs();
    public static CreditCardStatusCheckPageMsgs creditCardStatusCheckPage = new CreditCardStatusCheckPageMsgs();
    public static EmailPageMsgs emailPage = new EmailPageMsgs();
    public static ExportPageMsgs exportPage = new ExportPageMsgs();
    public static InputFormMsgs inputForm = new InputFormMsgs();
    public static InputPageMsgs inputPage = new InputPageMsgs();
    public static ListPageMsgs listPage = new ListPageMsgs();
    public static LoginPageMsgs loginPage = new LoginPageMsgs();
    public static MassApproveFormMsgs massApproveForm = new MassApproveFormMsgs();
    public static MassApprovePageMsgs massApprovePage = new MassApprovePageMsgs();
    public static PaymentFormMsgs paymentForm = new PaymentFormMsgs();
    public static PaymentPageMsgs paymentPage = new PaymentPageMsgs();
    public static SearchFormMsgs searchForm = new SearchFormMsgs();
    public static SearchPageMsgs searchPage = new SearchPageMsgs();
    public static SepaPageMsgs sepaPage = new SepaPageMsgs();
    public static StartPageMsgs startPage = new StartPageMsgs();
    public static StatsPageMsgs statsPage = new StatsPageMsgs();
    public static GroupsPageMsgs groupsPage = new GroupsPageMsgs();
    public static RoomsPageMsgs roomsPage = new RoomsPageMsgs();
}
