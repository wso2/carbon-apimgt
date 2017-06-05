package org.wso2.carbon.apimgt.gateway.dto;
struct APIKeyValidationInfoDTO {
    string username;
    string applicationTier;
    string tier;
    string apiTier;
    boolean isContentAwareTierPresent;
    string apiKey;
    string keyType;
    string callerToken;
    string applicationId;
    string applicationName;
    string consumerKey;
    string subscriber;
    string[] throttlingDataList;
    int spikeArrestLimit;
    string spikeArrestUnit;
    boolean stopOnQuotaReach;
}
