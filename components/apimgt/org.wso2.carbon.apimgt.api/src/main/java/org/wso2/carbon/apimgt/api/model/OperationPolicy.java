package org.wso2.carbon.apimgt.api.model;

import java.util.Map;
import java.util.Objects;

public class OperationPolicy {

    public enum PolicyType {
        SET_HEADER,
        REMOVE_HEADER,
        REWRITE_HTTP_METHOD,
        CALL_INTERCEPTOR_SERVICE,
        REWRITE_RESOURCE_PATH,
        ADD_QUERY_PARAM,
        REMOVE_QUERY_PARAM,
    };

    private PolicyType policyType = null;
    private String direction;
    private Map<String, Object> parameters;
    private int id;

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
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

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OperationPolicy that = (OperationPolicy) o;
        return id == that.id && policyType == that.policyType && direction.equals(that.direction) && parameters
                .equals(that.parameters);
    }

    @Override public int hashCode() {
        return Objects.hash(policyType, direction, parameters, id);
    }
}
