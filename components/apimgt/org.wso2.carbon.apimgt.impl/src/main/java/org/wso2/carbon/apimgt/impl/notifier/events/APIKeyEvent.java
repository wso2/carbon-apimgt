package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.Map;
import java.util.UUID;

/**
 * An Event Object which can holds the data related to API Key which are required
 * for the validation purpose in a gateway.
 */
public class APIKeyEvent extends Event {
    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public long getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBound() {
        return bound;
    }

    public void setBound(String bound) {
        this.bound = bound;
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

    public String getApplicationUUId() {
        return applicationUUId;
    }

    public void setApplicationUUId(String applicationUUId) {
        this.applicationUUId = applicationUUId;
    }

    public String getApiUUId() {
        return apiUUId;
    }

    public void setApiUUId(String apiUUId) {
        this.apiUUId = apiUUId;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    private String apiKeyHash;
    private String uuid;
    private String name;
    private String keyType;
    private String user;
    private Map properties;
    private long timeCreated;
    private long validityPeriod;
    private String status;
    private String permittedIP;
    private String permittedReferer;
    private String applicationUUId;
    private String apiUUId;
    private int apiId;
    private int applicationId;
    private String bound;

    public APIKeyEvent(String type, int tenantId, String tenantDomain,
                       String apiKeyHash, String uuid, String name, String keyType, String user,
                       Map<String, String> properties,
                       long timeCreated, long validityPeriod, String permittedIP, String permittedReferer,
                       String status,
                       String bound) {

        super(UUID.randomUUID().toString(), System.currentTimeMillis(), type, tenantId, tenantDomain);
        this.apiKeyHash = apiKeyHash;
        this.uuid = uuid;
        this.name = name;
        this.keyType = keyType;
        this.user = user;
        this.properties = properties;
        this.timeCreated = timeCreated;
        this.validityPeriod = validityPeriod;
        this.status = status;
        this.bound = bound;
        this.permittedIP = permittedIP;
        this.permittedReferer = permittedReferer;
    }

    public APIKeyEvent(String type, int tenantId, String tenantDomain,
                       String apiKeyHash, String uuid, String name, String keyType) {

        super(UUID.randomUUID().toString(), System.currentTimeMillis(), type, tenantId, tenantDomain);
        this.apiKeyHash = apiKeyHash;
        this.uuid = uuid;
        this.name = name;
        this.keyType = keyType;
    }
}
