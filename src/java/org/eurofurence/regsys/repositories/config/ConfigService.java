package org.eurofurence.regsys.repositories.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class ConfigService {
    private static final String REG_SECRET_API_TOKEN = "REG_SECRET_API_TOKEN";
    private static final String REG_SECRET_NOSECOUNTER_TOKEN = "REG_SECRET_NOSECOUNTER_TOKEN";
    private static final String REG_SECRET_DD_TOKEN = "REG_SECRET_DD_TOKEN";
    private static final String REG_SECRET_ARTSHOW_TOKEN = "REG_SECRET_ARTSHOW_TOKEN";
    private static final String REG_SECRET_BOAT_TOKEN = "REG_SECRET_BOAT_TOKEN";
    private static final String REG_SECRET_SECU_TOKEN = "REG_SECRET_SECU_TOKEN";
    private static final String REG_SECRET_SECU_SECRET = "REG_SECRET_SECU_SECRET";

    private static Configuration cachedConfiguration = null;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String configUrl;

    public ConfigService(String configUrl) {
        this.configUrl = configUrl;
    }

    public Configuration getConfig() {
        if (cachedConfiguration == null)
            LoadConfig();
        return cachedConfiguration;
    }

    private synchronized void LoadConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            logger.info("loading configuration from " + configUrl);
            cachedConfiguration = mapper.readValue(new URL(configUrl), Configuration.class);

            logger.info("applying environment variable secret overrides");
            applyEnvOverrides(cachedConfiguration.downstream);
        } catch (IOException e) {
            logger.error("failed to load configuration from " + configUrl, e);
            throw new ConfigLoadException("failed to load configuration", e);
        }

        // TODO minimal validation
    }

    private void applyEnvOverrides(Configuration.DownstreamConfig downstream) {
        if (downstream != null) {
            downstream.apiToken = envOrDefault(REG_SECRET_API_TOKEN, downstream.apiToken);
            downstream.nosecounterToken = envOrDefault(REG_SECRET_NOSECOUNTER_TOKEN, downstream.nosecounterToken);
            downstream.ddToken = envOrDefault(REG_SECRET_DD_TOKEN, downstream.ddToken);
            downstream.artshowToken = envOrDefault(REG_SECRET_ARTSHOW_TOKEN, downstream.artshowToken);
            downstream.boatToken = envOrDefault(REG_SECRET_BOAT_TOKEN, downstream.boatToken);
            downstream.secuToken = envOrDefault(REG_SECRET_SECU_TOKEN, downstream.secuToken);
            downstream.secuSecret = envOrDefault(REG_SECRET_SECU_SECRET, downstream.secuSecret);
        }
    }

    private String envOrDefault(String env, String defaultValue) {
        String envValue = System.getenv(env);
        if (envValue == null || "".equals(envValue)) {
            return defaultValue;
        } else {
            return envValue;
        }
    }
}
