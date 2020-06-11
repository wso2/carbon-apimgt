package org.wso2.carbon.apimgt.api.model;

import java.util.List;

/**
 * This class is to set and get properties of  list of services in the cluster.
 *
 */

public class ServiceDiscoveryEndpoints {
    private String type;
    List<Services> services;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Services> getServices() {
        return services;
    }

    public void setServices(List<Services> services) {
        this.services = services;
    }
}
