package org.wso2.carbon.apimgt.impl.containermgt;

import org.wso2.carbon.apimgt.api.model.Endpoint;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConf;

import java.util.List;

public class Services {
    private String type;
    private List<List<Object>> servicesLists;
    private List<Object> innerService;
    private String serviceName;
    private String serviceURL;
    private String properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<List<Object>> getServicesLists() {
        return servicesLists;
    }

    public void setServicesLists(List<List<Object>> servicesLists) {
        this.servicesLists = servicesLists;
    }

    public List<Object> getInnerService() {
        return innerService;
    }

    public void setInnerService(List<Object> innerService) {
        this.innerService = innerService;
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

    public List<List<Object>>  addServiceDetailsToList(){
        this.innerService.add(this.serviceName);
        this.innerService.add(this.serviceURL);
        this.innerService.add(this.properties);
        this.servicesLists.add(this.innerService);

        return servicesLists;

    }



    @Override
    public String toString() {
        return "Services{" +
                "type='" + type + '\'' +
                ", servicesLists=" + servicesLists +
                ", innerService=" + innerService +
                ", serviceName='" + serviceName + '\'' +
                ", serviceURL='" + serviceURL + '\'' +
                ", properties='" + properties + '\'' +
                '}';
    }
}
