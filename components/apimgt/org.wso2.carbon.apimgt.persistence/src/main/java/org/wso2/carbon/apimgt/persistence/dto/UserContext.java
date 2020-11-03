package org.wso2.carbon.apimgt.persistence.dto;

import java.util.Map;

public class UserContext {
    String username;
    Organization organization;
    // domain name, role

    Map<String, Object> properties;

    public UserContext(String userame, Organization organization, Map<String, Object> properties) {
        this.username = userame;
        this.organization = organization;
        this.properties = properties;
    }

    public String getUserame() {
        return username;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
