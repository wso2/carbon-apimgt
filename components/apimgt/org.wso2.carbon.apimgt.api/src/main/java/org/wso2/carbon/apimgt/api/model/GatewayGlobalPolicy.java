package org.wso2.carbon.apimgt.api.model;

import java.util.Map;
import java.util.Objects;

public class GatewayGlobalPolicy {
    private String policyName = "";
    private String policyVersion = "v1";
    private String direction = null;
    private Map<String, Object> parameters = null;
    private String policyId = null;
    private int order = 1;
    private String gatewayLabel;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getGatewayLabel() {
        return gatewayLabel;
    }

    public void setGatewayLabel(String gatewayLabel) {
        this.gatewayLabel = gatewayLabel;
    }

    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GatewayGlobalPolicy policyObj = (GatewayGlobalPolicy) o;
        return policyId == policyObj.policyId && policyName == policyObj.policyName
                && policyVersion == policyObj.policyVersion && gatewayLabel.equals(policyObj.gatewayLabel)
                && direction.equals(policyObj.direction) && parameters.equals(policyObj.parameters);
    }

    @Override
    public int hashCode() {

        return Objects.hash(policyName, policyVersion, direction, parameters, policyId, gatewayLabel);
    }
}
