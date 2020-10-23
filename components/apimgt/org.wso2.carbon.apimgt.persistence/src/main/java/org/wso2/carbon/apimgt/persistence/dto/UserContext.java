package org.wso2.carbon.apimgt.persistence.dto;

import java.util.Map;

public class UserContext {
    String userame;
    Organization organization;

    Map<String, Object> properties;

    public UserContext(String userame, Organization organization, Map<String, Object> properties) {
        this.userame = userame;
        this.organization = organization;
        this.properties = properties;
    }

    public String getUserame() {
        return userame;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
