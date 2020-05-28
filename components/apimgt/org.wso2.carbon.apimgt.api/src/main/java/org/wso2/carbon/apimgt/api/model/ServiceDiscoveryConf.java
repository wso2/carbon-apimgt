package org.wso2.carbon.apimgt.api.model;

public class ServiceDiscoveryConf {

    private String masterURL;
    private String saToken;

    public String getMasterURL() {
        return masterURL;
    }

    public void setMasterURL(String masterURL) {
        this.masterURL = masterURL;
    }

    public String getSaToken() {
        return saToken;
    }

    public void setSaToken(String saToken) {
        this.saToken = saToken;
    }
}
