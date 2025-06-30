package org.wso2.carbon.apimgt.impl.dto;

public class TenantSharingConfigurationDTO {
    private boolean isEnabled;
    private String reservedUserName;
    private String reservedUserPassword;

    public boolean isIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getReservedUserName() {
        return reservedUserName;
    }

    public void setReservedUserName(String reservedUserName) {
        this.reservedUserName = reservedUserName;
    }

    public String getReservedUserPassword() {
        return reservedUserPassword;
    }

    public void setReservedUserPassword(String reservedUserPassword) {
        this.reservedUserPassword = reservedUserPassword;
    }

}
