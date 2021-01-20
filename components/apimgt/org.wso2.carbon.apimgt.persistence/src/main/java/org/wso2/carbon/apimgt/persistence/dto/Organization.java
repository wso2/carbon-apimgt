package org.wso2.carbon.apimgt.persistence.dto;

public class Organization {
    String id;
    String name; // tenant domain or organization name
    String type; // Tenant or Org

    public Organization(String name, String id, String type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public Organization(String name) {
        this.name = name;
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
}
