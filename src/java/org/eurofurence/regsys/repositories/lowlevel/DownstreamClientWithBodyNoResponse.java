package org.eurofurence.regsys.repositories.lowlevel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.ConfigService;

public class DownstreamClientWithBodyNoResponse<B> {
    private final Class<B> clazzBody;
    private final LowlevelClient lowlevelClient;
    private final ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    public DownstreamClientWithBodyNoResponse(Class<B> clazzBody) {
        this.clazzBody = clazzBody;
        this.lowlevelClient = new LowlevelClient();
    }

    private ResponseWithDto<String> performOnRequest(String requestId, String requestName, B body, HttpEntityEnclosingRequest request, RequestAuth auth) {
        lowlevelClient.requestHeaderManipulator(request, requestId, auth, configService.getConfig());

        request.setHeader("Content-Type", "application/json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            byte[] bodyContent = mapper.writerFor(clazzBody).writeValueAsBytes(body);
            request.setEntity(new ByteArrayEntity(bodyContent));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ResponseWithDto<String> response = new ResponseWithDto<>();
        lowlevelClient.performGivenRequest(requestId, requestName, (HttpUriRequest) request, (contentStream, status, loc) -> {
            response.status = status;
            response.location = loc;
            response.dto = "";
        });

        return response;
    }

    public ResponseWithDto<String> performPost(String requestId, String url, String requestName, B body, RequestAuth auth) {
        HttpEntityEnclosingRequest request = new HttpPost(url);
        return performOnRequest(requestId, requestName, body, request, auth);
    }

    public ResponseWithDto<String> performPut(String requestId, String url, String requestName, B body, RequestAuth auth) {
        HttpEntityEnclosingRequest request = new HttpPut(url);
        return performOnRequest(requestId, requestName, body, request, auth);
    }
}