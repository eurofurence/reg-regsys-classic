package org.eurofurence.regsys.repositories.lowlevel;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.ConfigService;

public class DownstreamClientNoBodyNoResponse {
    private final LowlevelClient lowlevelClient;
    private final ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    public DownstreamClientNoBodyNoResponse() {
        this.lowlevelClient = new LowlevelClient();
    }

    public ResponseWithDto<String> performGet(String requestId, String url, String requestName, RequestAuth auth) {
        HttpUriRequest request = new HttpGet(url);
        lowlevelClient.requestHeaderManipulator(request, requestId, auth, configService.getConfig());

        ResponseWithDto<String> response = new ResponseWithDto<>();
        lowlevelClient.performGivenRequest(requestId, requestName, request, (contentStream, status, loc) -> {
            response.status = status;
            response.location = loc;
            response.dto = "";
        });

        return response;
    }

    public ResponseWithDto<String> performPost(String requestId, String url, String requestName, RequestAuth auth) {
        HttpUriRequest request = new HttpPost(url);
        lowlevelClient.requestHeaderManipulator(request, requestId, auth, configService.getConfig());

        ResponseWithDto<String> response = new ResponseWithDto<>();
        lowlevelClient.performGivenRequest(requestId, requestName, request, (contentStream, status, loc) -> {
            response.status = status;
            response.location = loc;
            response.dto = "";
        });

        return response;
    }

    public ResponseWithDto<String> performDelete(String requestId, String url, String requestName, RequestAuth auth) {
        HttpUriRequest request = new HttpDelete(url);
        lowlevelClient.requestHeaderManipulator(request, requestId, auth, configService.getConfig());

        ResponseWithDto<String> response = new ResponseWithDto<>();
        lowlevelClient.performGivenRequest(requestId, requestName, request, (contentStream, status, loc) -> {
            response.status = status;
            response.location = loc;
            response.dto = "";
        });

        return response;
    }
}
