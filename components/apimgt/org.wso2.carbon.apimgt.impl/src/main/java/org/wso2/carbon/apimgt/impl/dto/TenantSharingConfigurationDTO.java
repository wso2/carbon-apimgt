package org.wso2.carbon.apimgt.impl.dto;

public class TenantSharingConfigurationDTO {
    private boolean isEnabled;
    private String reservedUserName;
    private String reservedUserPassword;
    private String identityServerBaseUrl;

    public boolean getIsEnabled() {
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

    public String getIdentityServerBaseUrl() {
        return identityServerBaseUrl;
    }

    public void setIdentityServerBaseUrl(String identityServerBaseUrl) {
        this.identityServerBaseUrl = identityServerBaseUrl;
    }
}
