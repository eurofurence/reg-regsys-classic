package org.eurofurence.regsys.repositories.auth;

import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientNoBody;
import org.eurofurence.regsys.repositories.lowlevel.ResponseWithDto;

public class AuthService {
    private final String serviceBaseUrl;

    public AuthService() {
        Configuration config = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();
        serviceBaseUrl = config.downstream.authService;
    }

    // --- userinfo ---

    public UserInfo performGetFrontendUserinfo(RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/v1/frontend-userinfo";
        DownstreamClientNoBody<UserInfo> client = new DownstreamClientNoBody<>(UserInfo.class);
        ResponseWithDto<UserInfo> result = client.performGet(requestId, url,"auth/frontendUserinfo", auth);
        return result.dto;
    }
}
