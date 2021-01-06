package org.wso2.carbon.apimgt.persistence.dto;

public class Organization {
    private String id;
    private String name; // tenant domain or organization name
    private String type; // Tenant or Org
    private String organizationId; // Tenant or Org

    public Organization(String name, String id, String type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public Organization(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
