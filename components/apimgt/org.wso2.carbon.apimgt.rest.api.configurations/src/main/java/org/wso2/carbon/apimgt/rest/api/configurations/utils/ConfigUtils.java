package org.wso2.carbon.apimgt.rest.api.configurations.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.configurations.ConfigurationService;
import org.wso2.carbon.apimgt.rest.api.configurations.constants.ConfigurationConstants;

import java.util.List;

public class ConfigUtils {

    /**
     * Check for allowed origin list from configurations and return origin if exists
     *
     * @param origin Origin to check with the allowed origin list
     * @return allowed origin
     */
    public static String getAllowedOrigin(String origin) {
        if (origin == null) {
            return "";
        }

        String host = origin.split(ConfigurationConstants.WEB_PROTOCOL_SUFFIX)[1];
        List<String> allowedOrigins = ConfigurationService.getClientHosts();
        if (allowedOrigins.contains(ConfigurationConstants.ALL_ORIGINS) || allowedOrigins.contains(host)) {
            return origin;
        }

        return ConfigurationConstants.NOT_ALLOWED_ORIGIN;
    }
}
