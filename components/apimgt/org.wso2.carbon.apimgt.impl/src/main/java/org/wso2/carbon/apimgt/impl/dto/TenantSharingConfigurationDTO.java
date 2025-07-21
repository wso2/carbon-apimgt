package org.wso2.carbon.apimgt.impl.dto;

public class TenantSharingConfigurationDTO {
    private boolean isTenantSyncEnabled;
    private boolean autoConfigureKeyManagerOfCurrentType;
    private String reservedUserName;
    private String reservedUserPassword;
    private String identityServerBaseUrl;

    public boolean getIsTenantSyncEnabled() {
        return isTenantSyncEnabled;
    }

    public void setIsTenantSyncEnabled(boolean isTenantSyncEnabled) {
        this.isTenantSyncEnabled = isTenantSyncEnabled;
    }

    public boolean getIsAutoConfigureKeyManagerOfCurrentType() {
        return autoConfigureKeyManagerOfCurrentType;
    }

    public void setAutoConfigureKeyManagerOfCurrentType(boolean autoConfigureKeyManagerOfCurrentType) {
        this.autoConfigureKeyManagerOfCurrentType = autoConfigureKeyManagerOfCurrentType;
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
