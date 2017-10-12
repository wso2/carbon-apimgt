package org.wso2.carbon.apimgt.gateway.dto;

struct GatewayConfDTO {
    KeyManagerInfoDTO keyManagerInfo;
    JWTInfoDTO jwtInfo;
    AnalyticsInfoDTO analyticsInfo;
    ThrottlingInfoDTO throttlingInfo;
    GAnalyticsTrackingInfoDTO gAnalyticsTrackingInfo;
}

struct KeyManagerInfoDTO {
    string dcrEndpoint;
    string tokenEndpoint;
    string revokeEndpoint;
    string introspectEndpoint;
    CredentialsDTO credentials;
}

struct JWTInfoDTO {
    boolean enableJWTGeneration;
    string jwtHeader;
}

struct AnalyticsInfoDTO {
    boolean enabled;
    string type;
    string serverURL;
    string authServerURL;
    CredentialsDTO credentials;
}

struct ThrottlingInfoDTO {
    boolean enabled;
    string type;
    string serverURL;
    string authServerURL;
    CredentialsDTO credentials;
}

struct GAnalyticsTrackingInfoDTO {
    boolean enabled;
    string trackingID;
}

struct CredentialsDTO {
    string username;
    string password;
}