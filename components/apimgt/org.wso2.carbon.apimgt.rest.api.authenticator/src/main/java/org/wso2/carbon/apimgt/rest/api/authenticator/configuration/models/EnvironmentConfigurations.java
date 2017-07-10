package org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold environment configurations
 */

@Configuration(description = "Key Management Configurations")
public class EnvironmentConfigurations {

    @Element(description = "Default environment IS Host name")
    private String defaultEnvIsHost = "localhost";

    @Element(description = "Default environment IS port")
    private int defaultEnvIsPort = 0;

    @Element(description = "Default environment IS Token endpoint URL")
    private String defaultEnvIsEndPoint = "https://localhost:9443/oauth2/token";

    @Element(description = "Default environment IS Token endpoint URL")
    private String defaultEnvIsrevokeEndPoint = "";

    @Element(description = "Default environment IS Token endpoint URL")
    private String defaultEnv = "development";

    public String getDefaultEnvIsHost() {
        return defaultEnvIsHost;
    }

    public int getDefaultEnvIsPort() {
        return defaultEnvIsPort;
    }

    public String getDefaultEnvIsEndPoint() {
        return defaultEnvIsEndPoint;
    }

    public String getDefaultEnvIsrevokeEndPoint() {
        return defaultEnvIsrevokeEndPoint;
    }

    public String getDefaultEnv() {
        return defaultEnv;
    }
}
