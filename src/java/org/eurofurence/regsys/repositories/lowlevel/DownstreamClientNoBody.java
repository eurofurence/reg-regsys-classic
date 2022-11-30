package org.eurofurence.regsys.repositories.lowlevel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.config.ConfigService;

public class DownstreamClientNoBody<T> {
    private final Class<T> clazz;
    private final LowlevelClient lowlevelClient;
    private final ConfigService configService = new ConfigService(HardcodedConfig.CONFIG_URL);

    public DownstreamClientNoBody(Class<T> clazz) {
        this.clazz = clazz;
        this.lowlevelClient = new LowlevelClient();
    }

    public ResponseWithDto<T> performGet(String requestId, String url, String requestName, String token) {
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
