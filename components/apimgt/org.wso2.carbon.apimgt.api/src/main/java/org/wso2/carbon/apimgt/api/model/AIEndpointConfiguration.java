package org.wso2.carbon.apimgt.api.model;

public class AIEndpointConfiguration {

    private String authKey;
    private String sandboxAuthValue;
    private String productionAuthValue;
    private String authType;

    public String getAuthKey() {

        return authKey;
    }

    public void setAuthKey(String authKey) {

        this.authKey = authKey;
    }

    public String getSandboxAuthValue() {

        return sandboxAuthValue;
    }

    public void setSandboxAuthValue(String sandboxAuthValue) {

        this.sandboxAuthValue = sandboxAuthValue;
    }

    public String getProductionAuthValue() {

        return productionAuthValue;
    }

    public void setProductionAuthValue(String productionAuthValue) {

        this.productionAuthValue = productionAuthValue;
    }

    public String getAuthType() {

        return authType;
    }

    public void setAuthType(String authType) {

        this.authType = authType;
    }
}
