package org.eurofurence.regsys.repositories.payments;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientNoBody;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientWithBodyNoResponse;
import org.eurofurence.regsys.repositories.lowlevel.ResponseWithDto;
import org.eurofurence.regsys.repositories.lowlevel.Utils;

import java.util.ArrayList;
import java.util.List;

public class PaymentService {
    private final String serviceBaseUrl;

    public PaymentService() {
        Configuration config = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();
        serviceBaseUrl = config.downstream.paymentService;
    }

    public TransactionResponse performFindTransactions(Long debitorId, String transactionIdentifier, String effectiveFromISO, String effectiveBeforeISO, String token, String requestId) {
        List<NameValuePair> queryParams = new ArrayList<>();
        if (debitorId != null)
            queryParams.add(new BasicNameValuePair("debitor_id", debitorId.toString()));
        if (transactionIdentifier != null)
            queryParams.add(new BasicNameValuePair("transaction_identifier", transactionIdentifier));
        if (effectiveFromISO != null)
            queryParams.add(new BasicNameValuePair("effective_from", effectiveFromISO));
        if (effectiveBeforeISO != null)
            queryParams.add(new BasicNameValuePair("effective_before", effectiveBeforeISO));
        String url = serviceBaseUrl + "/api/rest/v1/transactions?" + URLEncodedUtils.format(queryParams, "UTF-8");
        DownstreamClientNoBody<TransactionResponse> client = new DownstreamClientNoBody<>(TransactionResponse.class);
        ResponseWithDto<TransactionResponse> result = client.performGet(requestId, url, "payment/findTransactions", token);
        return result.dto;
    }

    public long performCreateTransaction(Transaction transaction, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/transactions";
        DownstreamClientWithBodyNoResponse<Transaction> client = new DownstreamClientWithBodyNoResponse<>(Transaction.class);
        ResponseWithDto<String> result = client.performPost(requestId, url, "payment/createTransaction", transaction, token);
        return Utils.idFromLocationHeader(result.location);
    }

    public void performUpdateTransaction(long id, Transaction transaction, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/transactions/" + id;
        DownstreamClientWithBodyNoResponse<Transaction> client = new DownstreamClientWithBodyNoResponse<>(Transaction.class);
        ResponseWithDto<String> result = client.performPut(requestId, url, "payment/updateTransaction", transaction, token);
    }
}
