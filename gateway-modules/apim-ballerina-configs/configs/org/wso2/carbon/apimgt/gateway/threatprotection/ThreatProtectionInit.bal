package org.wso2.carbon.apimgt.gateway.threatprotection;

import ballerina.lang.errors;
import ballerina.lang.jsons;
import ballerina.lang.system;

import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.utils;

import org.wso2.carbon.apimgt.ballerina.threatprotection;

function initThreatProtection() (boolean) {
    try {
        json threatProtectionPolicies = utils:getThreatProtectionPolicies();
        storePolicies(threatProtectionPolicies);
    } catch (errors:Error error) {
        system:println("Error occured while setting per-API threat protection policies. " +  error.msg);
    }
    return true;
}

function storePolicies(json policies) {
    int numPolicies = jsons:getInt(policies.list, "$.length()");
    int i = 0;
    json policyList = policies.list;
    while (i < numPolicies) {
        json policy = policyList[i];
        dto:XMLThreatProtectionInfoDTO xmlThreatProtectionConf = {};
        dto:JSONThreatProtectionInfoDTO jsonThreatProtectionConf = {};

        string policyType;
        policyType, _ = (string)policy["type"];
        if (policyType == "XML") {
            xmlThreatProtectionConf = utils:fromJSONToXMLThreatProtectionInfoDTO(policy);
            threatprotection:configureXmlAnalyzer(xmlThreatProtectionConf, "THREAT_PROTECTION_POLICY_ADD");
        } else if (policyType == "JSON") {
            jsonThreatProtectionConf = utils:fromJSONToJSONThreatProtectionInfoDTO(policy);
            threatprotection:configureJsonAnalyzer(jsonThreatProtectionConf, "THREAT_PROTECTION_POLICY_ADD");
        }
        i = i + 1;
    }

}
