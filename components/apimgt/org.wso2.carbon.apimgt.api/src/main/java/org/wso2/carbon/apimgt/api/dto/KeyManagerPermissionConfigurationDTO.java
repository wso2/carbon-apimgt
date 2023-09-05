package org.wso2.carbon.apimgt.api.dto;


import java.io.Serializable;

public class KeyManagerPermissionConfigurationDTO implements Serializable {

    private Integer keyManagerPermissionID = null;
    private String keyManagerUUID = null;
    private String permissionType = null;
    private String role = null;

    public KeyManagerPermissionConfigurationDTO () {
    }

    public KeyManagerPermissionConfigurationDTO (Integer keyManagerPermissionID, String keyManagerUUID, String permissionType, String role) {
        this.keyManagerPermissionID = keyManagerPermissionID;
        this.keyManagerUUID = keyManagerUUID;
        this.permissionType = permissionType;
        this.role = role;
    }

    public Integer getKeyManagerPermissionID () {
        return keyManagerPermissionID;
    }

    public void setKeyManagerPermissionID (Integer keyManagerPermissionID) {
        this.keyManagerPermissionID = keyManagerPermissionID;
    }

    public String getKeyManagerUUID () {
        return keyManagerUUID;
    }

    public void setKeyManagerUUID (String keyManagerUUID) {
        this.keyManagerUUID = keyManagerUUID;
    }

    public String getPermissionType () {
        return permissionType;
    }

    public void setPermissionType (String permissionType) {
        this.permissionType = permissionType;
    }

    public String getRole () {
        return role;
    }

    public void setRole (String role) {
        this.role = role;
    }
}
