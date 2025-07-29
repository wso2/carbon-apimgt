package org.wso2.carbon.apimgt.gateway.mcp;

public class Param {
    private String name;
    private boolean isRequired;

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
