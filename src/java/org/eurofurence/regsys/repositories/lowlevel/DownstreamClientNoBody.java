package org.eurofurence.regsys.repositories.lowlevel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.ConfigService;

public class DownstreamClientNoBody<T> {
    private final Class<T> clazz;
    private final LowlevelClient lowlevelClient;
    private final ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    public DownstreamClientNoBody(Class<T> clazz) {
        this.clazz = clazz;
        this.lowlevelClient = new LowlevelClient();
    }

    public ResponseWithDto<T> performGet(String requestId, String url, String requestName, RequestAuth auth) {
        HttpUriRequest request = new HttpGet(url);
        lowlevelClient.requestHeaderManipulator(request, requestId, auth, configService.getConfig());

        ResponseWithDto<T> response = new ResponseWithDto<>();
        lowlevelClient.performGivenRequest(requestId, requestName, request, (contentStream, status, loc) -> {
            ObjectMapper mapper = new ObjectMapper();
            response.status = status;
            response.location = loc;
            response.dto = mapper.readerFor(clazz).readValue(contentStream);
        });

        return response;
    }

    public ResponseWithDto<T> performPost(String requestId, String url, String requestName, RequestAuth auth) {
        HttpUriRequest request = new HttpPost(url);
        lowlevelClient.requestHeaderManipulator(request, requestId, auth, configService.getConfig());

        ResponseWithDto<T> response = new ResponseWithDto<>();
        lowlevelClient.performGivenRequest(requestId, requestName, request, (contentStream, status, loc) -> {
            ObjectMapper mapper = new ObjectMapper();
            response.status = status;
            response.location = loc;
            response.dto = mapper.readerFor(clazz).readValue(contentStream);
        });

        return response;
    }

}
