package org.wso2.carbon.apimgt.core.util;

import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;

import java.util.List;

/**
 * Class for Environment related utility methods
 */
public class EnvironmentUtils {
    /**
     * Check for allowed origin list from configurations and return origin if exists
     *
     * @param origin Origin to check with the allowed origin list
     * @return origin or null
     */
    public static String getAllowedOrigin(String origin) {
        if (origin == null) {
            return null;
        }

        String host = origin.split(APIMgtConstants.WEB_PROTOCOL_SUFFIX)[1];
        List<String> allowedOrigins = APIMConfigurationService.getInstance().getEnvironmentConfigurations()
                .getAllowedHosts();
        if (allowedOrigins.contains(APIMgtConstants.CORSAllowOriginConstants.ALLOW_ALL_ORIGINS) ||
                allowedOrigins.contains(host)) {
            return origin;
        }

        //origin is not within the allowed origin list
        return null;
    }
}
