package org.wso2.carbon.apimgt.core.models;

import org.wso2.carbon.apimgt.core.models.policy.Policy;


import java.util.HashMap;


/**
 * file representation of an Application
 */
public class FileApplication {
    private String name;
    private String description;
    private Policy policy;
    private String status;
    private String createdUser;
    private String permissionString;
    private HashMap permissionMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public void setPermissionString(String permissionString) {
        this.permissionString = permissionString;
    }

    public HashMap getPermissionMap() {
        return permissionMap;
    }

    public void setPermissionMap(HashMap permissionMap) {
        this.permissionMap = permissionMap;
    }

    public FileApplication() {
        super();
    }

    public FileApplication(Application application) {
        name = application.getName();
        description = application.getDescription();
        policy = application.getPolicy();
        status = application.getStatus();
        createdUser = application.getCreatedUser();
        permissionString = application.getPermissionString();
        permissionMap = application.getPermissionMap();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileApplication that = (FileApplication) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (policy != null ? !policy.equals(that.policy) : that.policy != null) {
            return false;
        }
        if (status != null ? !status.equals(that.status) : that.status != null) {
            return false;
        }
        if (createdUser != null ? !createdUser.equals(that.createdUser) : that.createdUser != null) {
            return false;
        }
        if (permissionString != null ? !permissionString.equals(that.permissionString) :
                that.permissionString != null) {
            return false;
        }
        return permissionMap != null ? permissionMap.equals(that.permissionMap) : that.permissionMap == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (policy != null ? policy.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (createdUser != null ? createdUser.hashCode() : 0);
        result = 31 * result + (permissionString != null ? permissionString.hashCode() : 0);
        result = 31 * result + (permissionMap != null ? permissionMap.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FileApplication{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", policy=" + policy +
                ", status='" + status + '\'' +
                ", createdUser='" + createdUser + '\'' +
                ", permissionString='" + permissionString + '\'' +
                ", permissionMap=" + permissionMap +
                '}';
    }
}




