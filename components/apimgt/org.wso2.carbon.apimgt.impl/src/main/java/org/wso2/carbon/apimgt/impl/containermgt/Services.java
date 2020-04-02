package org.wso2.carbon.apimgt.impl.containermgt;

import org.wso2.carbon.apimgt.api.model.Endpoint;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConf;

import java.util.List;

public class Services {
    private String type;
    private String serviceName;
    private String serviceURL;
    private String properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }



    @Override
    public String toString() {
        return "Services{" +
                "type='" + type + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceURL='" + serviceURL + '\'' +
                ", properties='" + properties + '\'' +
                '}';
    }
}
