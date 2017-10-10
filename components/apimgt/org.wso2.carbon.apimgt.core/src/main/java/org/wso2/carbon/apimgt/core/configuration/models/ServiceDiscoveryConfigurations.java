package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold Service Discovery configurations and generate yaml file
 */
@Configuration(namespace = "wso2.carbon.serviceDiscovery", description = "Service Discovery configurations")
public class ServiceDiscoveryConfigurations {

    @Element(description = "enable service discovery")
    private Boolean enabled = true;
    @Element(description = "service discovery implementation configurations")
    private List<ServiceDiscoveryImplConfig> implementationConfigs = new ArrayList<>();


    public ServiceDiscoveryConfigurations() {
        implementationConfigs.add(new ServiceDiscoveryImplConfig());
    }

    public Boolean isServiceDiscoveryEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<ServiceDiscoveryImplConfig> getImplementationConfigs() {
        return implementationConfigs;
    }
}
