package org.wso2.carbon.apimgt.api.model;

import java.util.Map;
import java.util.Objects;

public class OperationPolicy {

    private String policyName = "";
    private String templateName = "";
    private String direction;
    private Map<String, Object> parameters;
    private int policyId;
    private int order = 1;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    public int getPolicyId() {
        return policyId;
    }

    public int getOrder() {

        return order;
    }

    public void setOrder(int order) {

        this.order = order;
    }

    public String getTemplateName() {

        return templateName;
    }

    public void setTemplateName(String templateName) {

        this.templateName = templateName;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OperationPolicy that = (OperationPolicy) o;
        return policyId == that.policyId && policyName == that.policyName && direction.equals(that.direction) && parameters
                .equals(that.parameters);
    }

    @Override public int hashCode() {
        return Objects.hash(policyName, direction, parameters, policyId);
    }
}
