package org.eurofurence.regsys.repositories.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.eurofurence.regsys.backend.Logging;

import java.io.IOException;
import java.net.URL;

public class ConfigService {
    private static Configuration cachedConfiguration = null;
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
            Logging.info("loading configuration from " + configUrl);
            cachedConfiguration = mapper.readValue(new URL(configUrl), Configuration.class);
        } catch (IOException e) {
            Logging.error("failed to load configuration from " + configUrl);
            Logging.exception(e);
            throw new ConfigLoadException("failed to load configuration", e);
        }

        // TODO minimal validation
    }
}
