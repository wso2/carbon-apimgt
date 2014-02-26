namespace java org.wso2.carbon.apimgt.keymgt.service.thrift

exception APIKeyMgtException {
    1: required string message
}

exception APIManagementException {
    1: required string message
}

struct APIKeyValidationInfoDTO {
    1: optional bool authorized;
    2: optional string subscriber;
    3: optional string tier;
    4: optional string type;
    5: optional string endUserToken;
    6: optional string endUserName;
    7: optional string applicationName;
    8: optional i32 validationStatus;
    9: optional string applicationId;
    10: optional string applicationTier;
    11: optional string apiName;
    12: optional string consumerKey;
    13: optional string apiPublisher;
    14: optional list<string> authorizedDomains;
}

struct URITemplate {
    1: optional string uriTemplate;
    2: optional string resourceURI;
    3: optional string resourceSandboxURI;
    4: optional string httpVerb;
    5: optional string authType;
    6: optional string throttlingTier;
}


service APIKeyValidationService {
APIKeyValidationInfoDTO validateKey(1:required string context, 2:required string version, 3:required string accessToken, 4:required string sessionId, 5:required string requiredAuthenticationLevel, 6:optional string clientDomain) throws (1:APIKeyMgtException apiKeyMgtException, 2:APIManagementException apiMgtException)
list<URITemplate> getAllURITemplates(1:required string context, 2:required string apiVersion) throws (1:APIKeyMgtException apiKeyMgtException, 2:APIManagementException apiMgtException)
}
