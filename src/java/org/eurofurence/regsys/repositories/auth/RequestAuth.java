package org.eurofurence.regsys.repositories.auth;

public class RequestAuth {
    public String idToken;
    public String accessToken;
    public String apiToken; // wins if set

    public boolean providedIdToken() {
        return idToken != null && !"".equals(idToken);
    }

    public boolean providedAccessToken() {
        return accessToken != null && !"".equals(accessToken);
    }

    public boolean providedApiToken() {
        return apiToken != null && !"".equals(apiToken);
    }
}
