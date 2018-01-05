package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Collections;
import java.util.List;

/**
 * Class to hold Environment configurations
 */
@Configuration(description = "Environment Configurations")
public class EnvironmentConfigurations {
    //Unique name for environment to set cookies by backend
    @Element(description = "current environment's label from the list of environments")
    private String environmentLabel = "Default";

    @Element(description = "list of web clients (eg: 127.0.0.1:9292) to allow make requests" +
            " to current environment\n(use '" + APIMgtConstants.CORSAllowOriginConstants.ALLOW_ALL_ORIGINS +
            "' to allow any web client)\nthe first host is used as UI-Service")
    //If the first host is an empty string, use "wso2.carbon.apimgt.application: apimBaseUrl" as UI-Service
    private List<String> allowedHosts = Collections.singletonList("");

    public String getEnvironmentLabel() {
        return environmentLabel;
    }

    public void setEnvironmentLabel(String environmentLabel) {
        this.environmentLabel = environmentLabel;
    }

    public List<String> getAllowedHosts() {
        return allowedHosts;
    }

    public void setAllowedHosts(List<String> allowedHosts) {
        this.allowedHosts = allowedHosts;
    }
}
