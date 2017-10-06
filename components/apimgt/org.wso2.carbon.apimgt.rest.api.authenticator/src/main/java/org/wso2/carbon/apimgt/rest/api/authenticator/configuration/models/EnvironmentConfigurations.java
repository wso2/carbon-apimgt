package org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold environment configurations
 */

@Configuration(description = "Key Management Configurations")
    public class EnvironmentConfigurations {

        @Element(description = "Default environment IS Host name")
        private String envIsHost = "localhost";

        @Element(description = "Default environment IS port")
        private int envIsPort = 0;

        @Element(description = "Default environment IS Token endpoint URL")
        private String envIsEndPoint = "https://localhost:9443/oauth2/token";

        @Element(description = "Default environment IS Token endpoint URL")
        private String envIsrevokeEndPoint = "";

        @Element(description = "Default environment IS Token endpoint URL")
        private String env = "development";

        public String getEnvIsHost() {
            return envIsHost;
        }

        public int getEnvIsPort() {
            return envIsPort;
        }

        public String getEnvIsEndPoint() {
            return envIsEndPoint;
        }

        public String getEnvIsrevokeEndPoint() {
            return envIsrevokeEndPoint;
        }

        public String getEnv() {
            return env;
        }


}
