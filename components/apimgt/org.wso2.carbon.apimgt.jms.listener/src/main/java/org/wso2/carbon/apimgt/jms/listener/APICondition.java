package org.wso2.carbon.apimgt.jms.listener;

public class APICondition {
    private String resourceKey;
    private String name;

    public APICondition(String resourceKey, String name) {
        this.resourceKey = resourceKey;
        this.name = name;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public String getName() {
        return name;
    }
}
