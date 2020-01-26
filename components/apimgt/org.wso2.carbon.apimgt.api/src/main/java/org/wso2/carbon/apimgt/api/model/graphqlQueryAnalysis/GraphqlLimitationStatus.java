package org.wso2.carbon.apimgt.api.model.graphqlQueryAnalysis;

public class GraphqlLimitationStatus {

    private String limitationType = null;
    private Boolean enabled = null;

    public String getLimitationType() {
        return limitationType;
    }

    public void setLimitationType(String limitationType) {
        this.limitationType = limitationType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
