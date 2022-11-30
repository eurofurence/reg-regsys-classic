package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.attendees.BanRuleList;
import org.eurofurence.regsys.web.forms.BanForm;
import org.eurofurence.regsys.web.forms.Form;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  Represents the ban editing page
 */
public class BansPage extends Page {
    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_SURE   = "sure";

    // constants for enumerated parameter values
    public static final String ACTION_STORE  = "store";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_EDIT  = "edit";
    public static final String ACTION_COPY  = "copy";
    public static final String ACTION_LIST  = "list"; // the default

    // ------------ attributes -----------------------

    private String action = "";
    private String pageTitle = "";

    private BanRuleList list = new BanRuleList();

    // ------------ business methods -----------------------

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!hasPermission(Constants.Permission.ADMIN)) {
            return forward("page/start");
        }

        parsePageParameters();
        getBanForm().getParameterParser().parseIdParam(getRequest());
        performFunction();

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("form", getBanForm().getVelocityRepresentation());
        veloContext.put("page", this);

        veloContext.put("function", getPageTitle());
        veloContext.put("url", "bans"); // base url for page
        veloContext.put("urlNew", "?action=edit&id=0");

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "bans.vm";
    }

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createBanForm();
    }

    private void parsePageParameters() {
        action = getRequest().getParameter(PARAM_ACTION);
        if (action == null) {
            action = "";
        }
    }

    private void loadBanList() {
        getBanForm().withErrorHandling(() -> {
            list = getAttendeeService().performListBans(getTokenFromRequest(), getRequestId());
        }, Strings.bansPage.dbErrorLoadList);
    }

    private void performFunction() {
        if (action.equals(ACTION_STORE)) {
            if (getBanForm().isNew()) {
                pageTitle = Strings.bansPage.functionSaveNew;
                getBanForm().makeNew();
            } else {
                pageTitle = Strings.bansPage.functionSave;
                getBanForm().loadBan();
            }

            getBanForm().getParameterParser().parseFormParams(getRequest());

            getBanForm().storeBan(); // may change id

            if (hasErrors()) {
                // edit ban again (with error msg on top)
                action = ACTION_EDIT;

                if (getBanForm().isNew()) {
                    pageTitle = Strings.bansPage.functionEditNew;
                } else {
                    pageTitle = Strings.bansPage.functionEdit;
                }
            } else {
                pageTitle = Strings.bansPage.functionList;
                loadBanList();
            }
        } else if (action.equals(ACTION_DELETE)) {
            if (getBanForm().isNew()) {
                // nonsense - do nothing with no complaints and go back to list
                action = ACTION_LIST;
                pageTitle = Strings.bansPage.functionList;
            } else {
                if ( getRequest().getParameter(PARAM_SURE) != null ) {
                    getBanForm().loadBan();
                    if (!hasErrors())
                        getBanForm().deleteBan();

                    action = ACTION_LIST;
                    pageTitle = Strings.bansPage.functionList;

                    loadBanList();
                } else {
                    pageTitle = Strings.bansPage.functionDeleteConfirm;
                    getBanForm().loadBan();
                    // wait for user to confirm - nothing to do yet
                }
            }
        } else if ( action.equals(ACTION_EDIT) ) {
            if (getBanForm().isNew()) {
                pageTitle = Strings.bansPage.functionEditNew;
                getBanForm().makeNew();
            } else {
                pageTitle = Strings.bansPage.functionEdit;
                getBanForm().loadBan();
            }
        } else if ( action.equals(ACTION_COPY) ) {
            pageTitle = Strings.bansPage.functionEditNew;
            if (getBanForm().isNew()) {
                getBanForm().makeNew();
            } else {
                getBanForm().loadBan();
                getBanForm().makeCopy();
                action = ACTION_EDIT;
            }
        } else {
            action = ACTION_LIST;
            pageTitle = Strings.bansPage.functionList;
            loadBanList();
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
        return list.bans.stream().map(
                rule -> {
                    Map<String,String> escaped = new HashMap<String,String>();
                    escaped.put("id", Long.toString(rule.id));
                    escaped.put("namePattern", Form.escape(rule.namePattern));
                    escaped.put("nicknamePattern", Form.escape(rule.nicknamePattern));
                    escaped.put("emailPattern", Form.escape(rule.emailPattern));
                    escaped.put("reason", Form.escape(rule.reason));
                    escaped.put("editUrl", "bans?action=edit&id="+rule.id);
                    escaped.put("copyUrl", "bans?action=copy&id="+rule.id);
                    escaped.put("deleteUrl", "bans?action=delete&id="+rule.id);
                    return escaped;
                }
        ).collect(Collectors.toList());
    }

    // ------- ban form ------------------

    private BanForm banForm;

    public void createBanForm() {
        banForm = new BanForm();
        banForm.givePage(this);
        banForm.initialize();
    }

    public BanForm getBanForm() {
        return banForm;
    }
}
