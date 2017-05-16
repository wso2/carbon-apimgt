package org.wso2.carbon.apimgt.gateway.dto;
struct APIKeyValidationInfoDTO {
    boolean authorized;
    string subscriber;
    boolean contentAware;
    string apiTier;
    string userType;
    string endUserToken;
    string endUserName;
    string applicationId;
    string applicationName;
    string applicationTier;
    string validationStatus;
    string validityPeriod;
    string issuedTime;
    datatable throttlingDataList;
    int spikeArrestLimit;
    string spikeArrestUnit;
    boolean stopOnQuotaReach;
}
