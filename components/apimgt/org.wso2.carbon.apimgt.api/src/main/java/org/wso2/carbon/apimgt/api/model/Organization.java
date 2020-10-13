package org.wso2.carbon.apimgt.api.model;

public class Organization {
    String name; // tenant domain
    int id;
    String type; // Tenant or Org

    public Organization(String name, int id, String type) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
