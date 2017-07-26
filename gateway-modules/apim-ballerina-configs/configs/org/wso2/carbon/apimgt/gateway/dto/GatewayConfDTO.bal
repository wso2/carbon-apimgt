package org.wso2.carbon.apimgt.gateway.dto;

struct GatewayConfDTO {
    KeyManagerInfoDTO keyManagerInfo;
    JWTInfoDTO jwtInfo;
    AnalyticsInfoDTO analyticsInfo;
    ThrottlingInfoDTO throttlingInfo;
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
    string serverURL;
    CredentialsDTO credentials;
}

struct ThrottlingInfoDTO {
    string serverURL;
    CredentialsDTO credentials;
}

struct CredentialsDTO {
    string username;
    string password;
}