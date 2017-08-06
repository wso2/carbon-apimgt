package org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold APIM configuration parameters and generate yaml file
 */
@Configuration(namespace = "wso2.carbon.apmigt.environments", description = "Environment Configaration Paramters")
public class APIMConfigurations {


    @Element(description = "Name for environment")
    private String environmentName = "Env";

    @Element(description = "Set of environments")
    private EnvironmentConfigurations environmentConfigs = new EnvironmentConfigurations();

    @Element(description = "Array of environments")
    private List<EnvironmentConfigurations> environments = new ArrayList<EnvironmentConfigurations>();


    public APIMConfigurations() {
        environments.add(environmentConfigs);
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public EnvironmentConfigurations getEnvironmentConfigs() {
        return environmentConfigs;
    }

    public List<EnvironmentConfigurations> getEnvironments() {
        return environments;
    }

}
