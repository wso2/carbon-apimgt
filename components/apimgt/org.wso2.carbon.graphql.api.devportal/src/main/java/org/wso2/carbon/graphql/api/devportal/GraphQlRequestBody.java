package org.wso2.carbon.graphql.api.devportal;

import java.util.Map;

public class GraphQlRequestBody {

    private String query;
    private String operationName;
    private Map<String , Object> variable;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Map<String, Object> getVariable() {
        return variable;
    }

    public void setVariable(Map<String, Object> variable) {
        this.variable = variable;
    }
}
