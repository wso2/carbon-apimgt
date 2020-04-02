package org.wso2.carbon.apimgt.impl.containermgt;

import java.util.List;
// ServiceListDTO

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
