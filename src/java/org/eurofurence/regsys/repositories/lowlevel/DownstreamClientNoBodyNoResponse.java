package org.eurofurence.regsys.repositories.lowlevel;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.config.ConfigService;

public class DownstreamClientNoBodyNoResponse {
    private final LowlevelClient lowlevelClient;
    private final ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    public DownstreamClientNoBodyNoResponse() {
        this.lowlevelClient = new LowlevelClient();
    }

    public ResponseWithDto<String> performGet(String requestId, String url, String requestName, String token) {
        HttpUriRequest request = new HttpGet(url);
        if (token != null && !"".equals(token)) {
            if (token.equals(configService.getConfig().downstream.apiToken)) {
                request.addHeader("X-Api-Key", token);
            } else {
                request.addHeader("Authorization", "Bearer " + token);
            }
        }
        if (requestId != null && !"".equals(requestId)) {
            request.addHeader("X-Request-Id", requestId);
        }

        ResponseWithDto<String> response = new ResponseWithDto<>();
        lowlevelClient.performGivenRequest(requestId, requestName, request, (contentStream, status, loc) -> {
            response.status = status;
            response.location = loc;
            response.dto = "";
        });

        return response;
    }

    public ResponseWithDto<String> performDelete(String requestId, String url, String requestName, String token) {
        HttpUriRequest request = new HttpDelete(url);
        if (token != null && !"".equals(token)) {
            request.addHeader("Authorization", "Bearer " + token);
        }
        if (requestId != null && !"".equals(requestId)) {
            request.addHeader("X-Request-Id", requestId);
        }

        ResponseWithDto<String> response = new ResponseWithDto<>();
        lowlevelClient.performGivenRequest(requestId, requestName, request, (contentStream, status, loc) -> {
            response.status = status;
            response.location = loc;
            response.dto = "";
        });

        return response;
    }
}
