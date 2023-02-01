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

public class DownstreamClientWithBody<B, R> {
    private final Class<B> clazzBody;
    private final Class<R> clazzResponse;
    private final LowlevelClient lowlevelClient;
    private final ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    public DownstreamClientWithBody(Class<B> clazzBody, Class<R> clazzResponse) {
        this.clazzBody = clazzBody;
        this.clazzResponse = clazzResponse;
        this.lowlevelClient = new LowlevelClient();
    }

    private ResponseWithDto<R> performOnRequest(String requestId, String requestName, B body, HttpEntityEnclosingRequest request, RequestAuth auth) {
        lowlevelClient.requestHeaderManipulator(request, requestId, auth, configService.getConfig());

        request.setHeader("Content-Type", "application/json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            byte[] bodyContent = mapper.writerFor(clazzBody).writeValueAsBytes(body);
            request.setEntity(new ByteArrayEntity(bodyContent));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ResponseWithDto<R> response = new ResponseWithDto<>();
        lowlevelClient.performGivenRequest(requestId, requestName, (HttpUriRequest) request, (contentStream, status, loc) -> {
            ObjectMapper mapper = new ObjectMapper();
            response.status = status;
            response.location = loc;
            response.dto = mapper.readerFor(clazzResponse).readValue(contentStream);
        });

        return response;
    }

    public ResponseWithDto<R> performPost(String requestId, String url, String requestName, B body, RequestAuth auth) {
        HttpEntityEnclosingRequest request = new HttpPost(url);
        return performOnRequest(requestId, requestName, body, request, auth);
    }

    public ResponseWithDto<R> performPut(String requestId, String url, String requestName, B body, RequestAuth auth) {
        HttpEntityEnclosingRequest request = new HttpPut(url);
        return performOnRequest(requestId, requestName, body, request, auth);
    }
}