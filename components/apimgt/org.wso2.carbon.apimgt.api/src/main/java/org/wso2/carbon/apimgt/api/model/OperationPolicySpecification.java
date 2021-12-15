package org.wso2.carbon.apimgt.api.model;

import java.util.List;

public class OperationPolicySpecification {

    private String policyName;
    private String displayName;
    private String policyDescription;
    private List<String> flow;
    private List<String> supportedGatewayTypes;
    private List<String> apiTypes;
    private List<OperationPolicySpecAttribute> policyAttributes;

    public String getPolicyName() {

        return policyName;
    }

    public void setPolicyName(String policyName) {

        this.policyName = policyName;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public String getPolicyDescription() {

        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {

        this.policyDescription = policyDescription;
    }

    public List<String> getFlow() {

        return flow;
    }

    public void setFlow(List<String> flow) {

        this.flow = flow;
    }

    public List<String> getSupportedGatewayTypes() {

        return supportedGatewayTypes;
    }

    public void setSupportedGatewayTypes(List<String> supportedGatewayTypes) {

        this.supportedGatewayTypes = supportedGatewayTypes;
    }

    public List<String> getApiTypes() {

        return apiTypes;
    }

    public void setApiTypes(List<String> apiTypes) {

        this.apiTypes = apiTypes;
    }

    public List<OperationPolicySpecAttribute> getPolicyAttributes() {

        return policyAttributes;
    }

    public void setPolicyAttributes(
            List<OperationPolicySpecAttribute> policyAttributes) {

        this.policyAttributes = policyAttributes;
    }
}
