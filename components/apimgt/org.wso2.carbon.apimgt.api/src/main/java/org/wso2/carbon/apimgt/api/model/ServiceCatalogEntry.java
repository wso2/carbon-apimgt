package org.wso2.carbon.apimgt.api.model;

public class ServiceCatalogEntry {
    private String key;
    private ServiceCatalogInfo serviceCatalogInfo;
    private EndPointInfo endPointInfo;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ServiceCatalogInfo getServiceCatalogInfo() {
        return serviceCatalogInfo;
    }

    public void setServiceCatalogInfo(ServiceCatalogInfo serviceCatalogInfo) {
        this.serviceCatalogInfo = serviceCatalogInfo;
    }

    public EndPointInfo getEndPointInfo() {
        return endPointInfo;
    }

    public void setEndPointInfo(EndPointInfo endPointInfo) {
        this.endPointInfo = endPointInfo;
    }
}
