package org.eurofurence.regsys.repositories.paygate;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientNoBody;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientWithBodyNoResponse;
import org.eurofurence.regsys.repositories.lowlevel.ResponseWithDto;
import org.eurofurence.regsys.repositories.lowlevel.Utils;
import org.eurofurence.regsys.repositories.payments.Transaction;
import org.eurofurence.regsys.repositories.payments.TransactionResponse;

import java.util.ArrayList;
import java.util.List;

public class PaygateAdapter {
    private final String serviceBaseUrl;

    public PaygateAdapter() {
        Configuration config = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();
        serviceBaseUrl = config.downstream.paygateAdapter;
    }

    public PaymentResponse performStatusCheck(String referenceId, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/paylinks/" + referenceId + "/status-check";
        DownstreamClientNoBody<PaymentResponse> client = new DownstreamClientNoBody<>(PaymentResponse.class);
        ResponseWithDto<PaymentResponse> result = client.performPost(requestId, url, "paygate/statusCheck", auth);
        return result.dto;
    }
}
