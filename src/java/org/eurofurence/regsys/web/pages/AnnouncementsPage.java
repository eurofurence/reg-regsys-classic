package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.mails.MailSendRequest;
import org.eurofurence.regsys.repositories.mails.MailService;
import org.eurofurence.regsys.repositories.mails.TemplateList;
import org.eurofurence.regsys.web.forms.AnnouncementForm;
import org.eurofurence.regsys.web.forms.Form;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  Represents the announcement template editing page
 */
public class AnnouncementsPage extends Page {
    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_SURE   = "sure";
    public static final String PARAM_TEST = "test";

    // constants for enumerated parameter values
    public static final String ACTION_STORE  = "store";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_EDIT  = "edit";
    public static final String ACTION_COPY  = "copy";
    public static final String ACTION_LIST  = "list"; // the default

    // ------------ attributes -----------------------

    private String action = "";
    private String pageTitle = "";
    private boolean sendTestMail = false;

    private TemplateList list = new TemplateList();

    // ------------ business methods -----------------------

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!hasPermission(Constants.Permission.ADMIN) && !hasPermission(Constants.Permission.ANNOUNCE)) {
            return forward("page/start");
        }

        parsePageParameters();
        getAnnouncementForm().getParameterParser().parseIdParam(getRequest());
        performFunction();

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("form", getAnnouncementForm().getVelocityRepresentation());
        veloContext.put("page", this);

        veloContext.put("function", getPageTitle());
        veloContext.put("url", "announcements");
        veloContext.put("urlNew", "announcements?action=edit");

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "announcements.vm";
    }

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createAnnouncementForm();
    }

    private final MailService mailService = new MailService();
    public MailService getMailService() {
        return mailService;
    }

    private void parsePageParameters() {
        action = getRequest().getParameter(PARAM_ACTION);
        if (action == null) {
            action = "";
        }
        if (getRequest().getParameter(PARAM_TEST) != null) {
            sendTestMail = true;
        }
    }

    private void loadList() {
        getAnnouncementForm().withErrorHandling(() -> {
            list = getMailService().performFindTemplates("", "", getTokenFromRequest(), getRequestId());
        }, Strings.announcementPage.dbErrorLoadList);
    }

    public static class SendTestMailResult {
        boolean result;
    }

    private synchronized boolean sendTestMail() {
        final SendTestMailResult result = new SendTestMailResult();
        MailSendRequest sendRequest = new MailSendRequest();
        sendRequest.to = Collections.singletonList(getLoggedInAttendee().email);
        sendRequest.cid = getAnnouncementForm().getCid();
        sendRequest.lang = getAnnouncementForm().getLang();
        sendRequest.variables = new HashMap<>();
        sendRequest.variables.put("nickname", "testnick");
        // TODO - more vars
        getAnnouncementForm().withErrorHandling(() -> {
            getMailService().performSendMail(sendRequest, getTokenFromRequest(), getRequestId());
            result.result = true;
        }, Strings.announcementPage.testSendFailure);
        return result.result;
    }

    private void performFunction() {
        if (action.equals(ACTION_STORE)) {
            if (getAnnouncementForm().isNew()) {
                pageTitle = Strings.announcementPage.functionSaveNew;
                getAnnouncementForm().makeNew();
            } else {
                pageTitle = Strings.announcementPage.functionSave;
                getAnnouncementForm().loadAnnouncement();
            }

            getAnnouncementForm().getParameterParser().parseFormParams(getRequest());

            if (sendTestMail) {
                if (sendTestMail()) {
                    getAnnouncementForm().storeAnnouncement(); // may change id
                }
            } else {
                getAnnouncementForm().storeAnnouncement(); // may change id
            }

            if (hasErrors()) {
                // edit again (with error msg on top)
                action = ACTION_EDIT;

                if (getAnnouncementForm().isNew()) {
                    pageTitle = Strings.announcementPage.functionEditNew;
                } else {
                    pageTitle = Strings.announcementPage.functionEdit;
                }
            } else {
                pageTitle = Strings.announcementPage.functionList;
                loadList();
            }
        } else if (action.equals(ACTION_DELETE)) {
            if (getAnnouncementForm().isNew()) {
                // nonsense - do nothing with no complaints and go back to list
                action = ACTION_LIST;
                pageTitle = Strings.announcementPage.functionList;
            } else {
                if ( getRequest().getParameter(PARAM_SURE) != null ) {
                    getAnnouncementForm().loadAnnouncement();
                    if (!hasErrors())
                        getAnnouncementForm().deleteAnnouncement();

                    action = ACTION_LIST;
                    pageTitle = Strings.announcementPage.functionList;

                    loadList();
                } else {
                    pageTitle = Strings.announcementPage.functionDeleteConfirm;
                    getAnnouncementForm().loadAnnouncement();
                    // wait for user to confirm - nothing to do yet
                }
            }
        } else if ( action.equals(ACTION_EDIT) ) {
            if (getAnnouncementForm().isNew()) {
                pageTitle = Strings.announcementPage.functionEditNew;
                getAnnouncementForm().makeNew();
            } else {
                pageTitle = Strings.announcementPage.functionEdit;
                getAnnouncementForm().loadAnnouncement();
            }
        } else if ( action.equals(ACTION_COPY) ) {
            pageTitle = Strings.announcementPage.functionEditNew;
            if (getAnnouncementForm().isNew()) {
                getAnnouncementForm().makeNew();
            } else {
                getAnnouncementForm().loadAnnouncement();
                getAnnouncementForm().makeCopy();
                action = ACTION_EDIT;
            }
        } else {
            action = ACTION_LIST;
            pageTitle = Strings.announcementPage.functionList;
            loadList();
        }
    }

    // ------------ velocity representation -----------------------

    public boolean wasStored() {
        return ACTION_STORE.equals(action);
    }

    public boolean showDeleteConfirm() {
        return ACTION_DELETE.equals(action);
    }

    public boolean showList() {
        return ACTION_LIST.equals(action) || ACTION_STORE.equals(action);
    }

    public boolean showEditForm() {
        return ACTION_EDIT.equals(action);
    }

    public List<Map<String,String>> getLoadedEscapedList() {
        return list.templates.stream().map(
                tpl -> {
                    Map<String,String> escaped = new HashMap<String,String>();
                    escaped.put("cid", Form.escape(tpl.cid));
                    escaped.put("lang", Form.escape(tpl.lang));
                    escaped.put("subject", Form.escape(tpl.subject));
                    escaped.put("editUrl", "announcements?action=edit&template_id="+tpl.uuid);
                    escaped.put("copyUrl", "announcements?action=copy&template_id="+tpl.uuid);
                    escaped.put("deleteUrl", "announcements?action=delete&template_id="+tpl.uuid);
                    return escaped;
                }
        ).collect(Collectors.toList());
    }

    // ------- announcement form ------------------

    private AnnouncementForm announcementForm;

    public void createAnnouncementForm() {
        announcementForm = new AnnouncementForm();
        announcementForm.givePage(this);
        announcementForm.initialize();
    }

    public AnnouncementForm getAnnouncementForm() {
        return announcementForm;
    }
}
