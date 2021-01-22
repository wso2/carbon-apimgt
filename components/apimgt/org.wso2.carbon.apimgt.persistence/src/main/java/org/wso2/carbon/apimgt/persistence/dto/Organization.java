package org.wso2.carbon.apimgt.persistence.dto;

public class Organization {
    private String name; // tenant domain or organization name
    private String type; // Tenant or Org
    private String orgId; // Tenant or Org

    public Organization(String name, String orgId, String type) {
        this.name = name;
        this.orgId = orgId;
        this.type = type;
    }

    public Organization(String orgId) {
        this.orgId = orgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}