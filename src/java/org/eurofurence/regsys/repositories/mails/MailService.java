package org.eurofurence.regsys.repositories.mails;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientNoBody;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientNoBodyNoResponse;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientWithBodyNoResponse;
import org.eurofurence.regsys.repositories.lowlevel.ResponseWithDto;
import org.eurofurence.regsys.repositories.lowlevel.Utils;

import java.util.ArrayList;
import java.util.List;

public class MailService {
    private final String serviceBaseUrl;

    public MailService() {
        Configuration config = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();
        serviceBaseUrl = config.downstream.mailService;
    }

    // --- mails ---

    public void performSendMail(MailSendRequest body, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/v1/mail";
        DownstreamClientWithBodyNoResponse<MailSendRequest> client = new DownstreamClientWithBodyNoResponse<>(MailSendRequest.class);
        client.performPost(requestId, url, "mail/send", body, auth);
    }

    public void performPreviewMail(MailPreviewRequest body, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/v1/mail/preview";
        DownstreamClientWithBodyNoResponse<MailPreviewRequest> client = new DownstreamClientWithBodyNoResponse<>(MailPreviewRequest.class);
        client.performPost(requestId, url, "mail/preview", body, auth);
    }

    // --- templates ---

    public TemplateList performFindTemplates(String cid, String lang, RequestAuth auth, String requestId) {
        List<NameValuePair> queryParams = new ArrayList<>();
        if (cid != null)
            queryParams.add(new BasicNameValuePair("cid", cid));
        if (lang != null)
            queryParams.add(new BasicNameValuePair("lang", lang));
        String url = serviceBaseUrl + "/api/v1/templates?" + URLEncodedUtils.format(queryParams, "UTF-8");
        DownstreamClientNoBody<TemplateList> client = new DownstreamClientNoBody<>(TemplateList.class);
        ResponseWithDto<TemplateList> result = client.performGet(requestId, url, "mail/findTemplates", auth);
        return result.dto;
    }

    public String performCreateTemplate(Template template, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/v1/templates";
        DownstreamClientWithBodyNoResponse<Template> client = new DownstreamClientWithBodyNoResponse<>(Template.class);
        ResponseWithDto<String> result = client.performPost(requestId, url, "mail/createTemplate", template, auth);
        return Utils.uuidFromLocationHeader(result.location);
    }

    public Template performGetTemplate(String uuid, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/v1/templates/" + uuid;
        DownstreamClientNoBody<Template> client = new DownstreamClientNoBody<>(Template.class);
        ResponseWithDto<Template> result = client.performGet(requestId, url, "mail/getTemplate", auth);
        return result.dto;
    }

    public void performUpdateTemplate(String uuid, Template template, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/v1/templates/" + uuid;
        DownstreamClientWithBodyNoResponse<Template> client = new DownstreamClientWithBodyNoResponse<>(Template.class);
        client.performPut(requestId, url, "mail/updateTemplate", template, auth);
    }

    public void performDeleteTemplate(String uuid, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/v1/templates/" + uuid;
        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performDelete(requestId, url, "mail/deleteTemplate", auth);
    }
}
