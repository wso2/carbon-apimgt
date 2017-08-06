package org.wso2.carbon.apimgt.rest.api.authenticator.utils.bean;

import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.EnvironmentConfigurations;

import java.util.ArrayList;
import java.util.List;

/**
 * This class retrive the environment information.
 *
 */
public class EnvironmentConfigBean {

    private List<EnvironmentConfigurations> environments = new ArrayList<EnvironmentConfigurations>();

    public List<EnvironmentConfigurations> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<EnvironmentConfigurations> environments) {
        this.environments = environments;
    }
}
