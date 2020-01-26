package org.wso2.carbon.apimgt.api.model.graphqlQueryAnalysis;

public class GraphqlDepthComplexityStatus {

    private Boolean depthEnabled = null;
    private Boolean complexityEnabled = null;

    public Boolean getDepthEnabled() {
        return depthEnabled;
    }

    public void setDepthEnabled(Boolean depthEnabled) {
        this.depthEnabled = depthEnabled;
    }

    public Boolean getComplexityEnabled() {
        return complexityEnabled;
    }

    public void setComplexityEnabled(Boolean complexityEnabled) {
        this.complexityEnabled = complexityEnabled;
    }
}
