package org.wso2.carbon.apimgt.gateway.dto;

struct BlockConditionDto{
    string key;
    boolean enabled;
    string conditionType;
    string conditionValue;
    string uuid;
    int fixedIp;
    int startingIP;
    int endingIP;
}