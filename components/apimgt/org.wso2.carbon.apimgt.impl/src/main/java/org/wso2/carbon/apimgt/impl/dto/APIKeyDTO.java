package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;

/**
 * This class represent the API Key DTO.
 */
public class APIKeyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String keyDisplayName;
    private String applicationId;
    private String keyType;

    private String apiKeyProperties;
    private String authUser;
    private long validityPeriod;
    private String lastUsedTime;
    private String permittedIP;
    private String permittedReferer;

    public String getKeyDisplayName() {
        return keyDisplayName;
    }

    public void setKeyDisplayName(String keyDisplayName) {
        this.keyDisplayName = keyDisplayName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getApiKeyProperties() {
        return apiKeyProperties;
    }

    public void setApiKeyProperties(String apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public long getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public String getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(String lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public String getPermittedIP() {
        return permittedIP;
    }

    public void setPermittedIP(String permittedIP) {
        this.permittedIP = permittedIP;
    }

    public String getPermittedReferer() {
        return permittedReferer;
    }

    public void setPermittedReferer(String permittedReferer) {
        this.permittedReferer = permittedReferer;
    }
}
