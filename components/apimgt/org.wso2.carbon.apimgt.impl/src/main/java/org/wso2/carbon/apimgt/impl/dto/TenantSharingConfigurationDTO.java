package org.wso2.carbon.apimgt.impl.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the Tenant Sharing Configuration DTO.
 */
public class TenantSharingConfigurationDTO {
    private String type;
    private Map<String, String> properties = new HashMap<>();

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public Map<String, String> getProperties() {

        return properties;
    }

    public void setProperties(Map<String, String> properties) {

        this.properties = properties;
    }
}
