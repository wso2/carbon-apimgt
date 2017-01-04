package org.wso2.carbon.apimgt.gateway.throttling.dto;

/**
 * Temporary files should be delted once the Authentication handler part is completed
 */
public class AuthenticationContextDTO {

    private String keyType;
    private String apiKey;
    private String applicationId;
    private String subscriber;
    private String applicationName;
    private String username;

    private String subscriberTenantDomain;
    private String tier;
    private String applicationTier;
    private String requestKey;
    private String apiTier;
    private String callerToken;
    private String spikeArrestUnit;
    private int spikeArrestLimit;
    private boolean stopOnQuotaReach;

    public AuthenticationContextDTO() {
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSubscriberTenantDomain() {
        return subscriberTenantDomain;
    }

    public void setSubscriberTenantDomain(String subscriberTenantDomain) {
        this.subscriberTenantDomain = subscriberTenantDomain;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getApplicationTier() {
        return applicationTier;
    }

    public void setApplicationTier(String applicationTier) {
        this.applicationTier = applicationTier;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public String getApiTier() {
        return apiTier;
    }

    public void setApiTier(String apiTier) {
        this.apiTier = apiTier;
    }

    public String getCallerToken() {
        return callerToken;
    }

    public void setCallerToken(String callerToken) {
        this.callerToken = callerToken;
    }

    public String getSpikeArrestUnit() {
        return spikeArrestUnit;
    }

    public void setSpikeArrestUnit(String spikeArrestUnit) {
        this.spikeArrestUnit = spikeArrestUnit;
    }

    public int getSpikeArrestLimit() {
        return spikeArrestLimit;
    }

    public void setSpikeArrestLimit(int spikeArrestLimit) {
        this.spikeArrestLimit = spikeArrestLimit;
    }

    public boolean isStopOnQuotaReach() {
        return stopOnQuotaReach;
    }

    public void setStopOnQuotaReach(boolean stopOnQuotaReach) {
        this.stopOnQuotaReach = stopOnQuotaReach;
    }

    // TODO: should remove this method once we fetch the cotext from carbon message
    public static AuthenticationContextDTO getInstance() {

        AuthenticationContextDTO authContext = new AuthenticationContextDTO();
        authContext.setKeyType("PRODUCTION");
        authContext.setApiKey("/api/1.0.0");
        authContext.setApplicationId("1");
        authContext.setSubscriber("admin");
        authContext.setApplicationName("defaultApplication");
        authContext.setUsername("admin");
        authContext.setApplicationName("carbon.super");
        authContext.setTier("UNLIMITED");
        authContext.setApplicationTier("UNLIMITED");
        authContext.setApiTier("UNLIMITED");
        authContext.setCallerToken("xxxxxx.Test_token.xxxxxxx");
        authContext.setSpikeArrestUnit("minutes");
        authContext.setSpikeArrestLimit(10);
        authContext.setStopOnQuotaReach(true);
        return authContext;
    }
}
