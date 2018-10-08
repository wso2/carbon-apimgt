namespace java org.wso2.carbon.apimgt.impl.generated.thrift

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
    15: optional set<string> scopes;
    16: optional i64 validityPeriod;
    17: optional i64 issuedTime;
    18: optional bool isContentAware;
    19: optional string apiTier;
    20: optional list<string> throttlingDataList;
    21: optional i32 spikeArrestLimit;
    22: optional string subscriberTenantDomain;
    23: optional string spikeArrestUnit;
    24: optional bool stopOnQuotaReach;
}


struct ConditionDTO {
    1: optional string conditionType;
    2: optional string conditionName;
    3: optional string conditionValue;
    4: optional bool isInverted;
}

struct ConditionGroupDTO {
    1: optional string conditionGroupId;
    2: optional list<ConditionDTO> conditions;
}

struct URITemplate {
    1: optional string uriTemplate;
    2: optional string resourceURI;
    3: optional string resourceSandboxURI;
    4: optional string httpVerb;
    5: optional string authType;
    6: optional string throttlingTier;
    7: optional list<string>  throttlingConditions;
    8: optional string applicableLevel;
    9: optional list<ConditionGroupDTO> conditionGroups;
}

service APIKeyValidationService {
APIKeyValidationInfoDTO validateKey(1:required string context, 2:required string version, 3:required string accessToken, 4:required string sessionId, 5:required string requiredAuthenticationLevel, 6:optional string clientDomain, 7:required string matchingResource, 8:required string httpVerb) throws (1:APIKeyMgtException apiKeyMgtException, 2:APIManagementException apiMgtException)
list<URITemplate> getAllURITemplates(1:required string context, 2:required string apiVersion, 3:required string sessionId) throws (1:APIKeyMgtException apiKeyMgtException, 2:APIManagementException apiMgtException)
APIKeyValidationInfoDTO validateKeyforHandshake(1:required string context, 2:required string version, 3:required string accessToken, 4:required string sessionId) throws (1:APIKeyMgtException apiKeyMgtException, 2:APIManagementException apiMgtException)
}
