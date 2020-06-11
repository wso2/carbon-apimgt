package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIManagementException;

/**
 * This class is to set and get properties of a single service in the cluster.
 *
 */
public class Services {
    private String type;
    private String serviceName;
    private String serviceURL;
    private String properties;
    private String creatingTimeStamp ;

    public String getCreatingTimeStamp() {
        return creatingTimeStamp;
    }

    public void setCreatingTimeStamp(String creatingTimeStamp) {
        this.creatingTimeStamp = creatingTimeStamp;
    }

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
        return "Service{" +
                "type='" + type + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceURL='" + serviceURL + '\'' +
                ", properties='" + properties + '\'' +
                '}';
    }
}
