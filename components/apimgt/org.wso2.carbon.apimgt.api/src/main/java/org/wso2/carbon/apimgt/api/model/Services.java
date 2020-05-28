package org.wso2.carbon.apimgt.api.model;

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
        return "Service{" +
                "type='" + type + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceURL='" + serviceURL + '\'' +
                ", properties='" + properties + '\'' +
                '}';
    }
}
