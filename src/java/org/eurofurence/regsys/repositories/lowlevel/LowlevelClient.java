package org.eurofurence.regsys.repositories.lowlevel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class LowlevelClient {
    // here, we need the logger in static methods
    private static final Logger LOGGER = LoggerFactory.getLogger("org.eurofurence.regsys.repositories.lowlevel.LowlevelClient");

    // TODO read from config
    private static final int REMOTE_REQUEST_TIMEOUT_SECONDS = 10;

    // CloseableHttpClient is annotated thread safe
    private static volatile CloseableHttpClient cachedClient = null;

    private RuntimeException httpError(int status, HttpResponse response, Throwable cause) {
        ErrorDto err;
        try {
            ObjectMapper mapper = new ObjectMapper();
            err = mapper.readerFor(ErrorDto.class).readValue(response.getEntity().getContent());
        } catch (Exception e) {
            err = new ErrorDto();
            err.message = "unknown";
        }
        switch (status) {
            case 400:
                return new BadRequestException(err, cause);
            case 401:
                return new UnauthorizedException(err, cause);
            case 403:
                return new ForbiddenException(err, cause);
            case 404:
                return new NotFoundException(err, cause);
            case 409:
                return new ConflictException(err, cause);
            case 500:
                return new InternalServerErrorException(err, cause);
            case 502:
                return new BadGatewayException(err, cause);
            default:
                return new DownstreamException(err.message, cause);
        }
    }

    public void performGivenRequest(String requestId, String requestName, HttpUriRequest request, IOExceptionThrowingResponseConsumer<InputStream> successResponseReceiver) {
        long started = System.currentTimeMillis();

        // Logging.info("[" + requestId + "] downstream " + requestName + " starting... ");

        HttpClient httpClient = getClient();

        HttpResponse response = null;
        try {
            response = httpClient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            if (status >= 300) {
                RuntimeException e = httpError(status, response, null);
                long done = System.currentTimeMillis();
                if (status == 400 || status == 401 || status == 403 || status == 404) {
                    LOGGER.info("downstream " + requestName + " -> " + status + " failed (" + (done - started) + " ms)");
                } else {
                    LOGGER.warn("downstream " + requestName + " -> " + status + " failed (" + (done - started) + " ms)");
                }
                throw e;
            } else {
                String location = null;
                Header locationHeader = response.getLastHeader("Location");
                if (locationHeader != null) {
                    location = locationHeader.getValue();
                }
                if (location == null) {
                    location = "";
                }

                InputStream content = null;
                if (response.getEntity() != null) {
                    content = response.getEntity().getContent();
                }
                successResponseReceiver.accept(content, status, location);
                long done = System.currentTimeMillis();
                LOGGER.info("downstream " + requestName + " -> " + status + " (" + (done - started) + " ms)");
            }
        } catch (IOException e) {
            LOGGER.error("downstream " + requestName + " failed: " + e.getMessage());
            DownstreamException wrap = new DownstreamException("http.request.failure", e);
            throw wrap;
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    public void requestHeaderManipulator(HttpMessage request, String requestId, RequestAuth auth, Configuration config) {
        if (auth.providedApiToken()) {
            request.addHeader("X-Api-Key", auth.apiToken);
        } else {
            String cookie = "";
            if (auth.providedIdToken()) {
                cookie += config.downstream.idTokenCookieName + "=" + auth.idToken;
            }
            if (auth.providedAccessToken()) {
                if (!"".equals(cookie)) {
                    cookie += "; ";
                }
                cookie += config.downstream.accessTokenCookieName + "=" + auth.accessToken;
            }
            request.addHeader("Cookie", cookie);

            // TODO this is a workaround until we have 2FA
            request.addHeader("X-Admin-Request", "available");
        }
        if (requestId != null && !"".equals(requestId)) {
            request.addHeader("X-Request-Id", requestId);
        }
    }

    public HttpClient getClient() {
        if (cachedClient == null)
            createClient();
        return cachedClient;
    }

    private static synchronized void createClient() {
        if (cachedClient == null) {
            long started = System.currentTimeMillis();

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(REMOTE_REQUEST_TIMEOUT_SECONDS * 1000)
                    .setSocketTimeout(REMOTE_REQUEST_TIMEOUT_SECONDS * 1000)
                    .build();
            cachedClient = HttpClientBuilder.create().disableCookieManagement().setDefaultRequestConfig(requestConfig).build();

            long done = System.currentTimeMillis();
            LOGGER.info("downstream http client successfully created ("+ (done - started) + " ms)");
        }
    }

    public static synchronized void destroyClient() {
        HttpClientUtils.closeQuietly(cachedClient);
    }
}
