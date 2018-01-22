package org.wso2.carbon.apimgt.gateway.services;

import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.net.jms;
import ballerina.lang.system;
import ballerina.lang.strings;

import org.wso2.carbon.apimgt.gateway.dto;
import org.wso2.carbon.apimgt.gateway.utils;
import org.wso2.carbon.apimgt.ballerina.threatprotection;

@jms:config {
    initialContextFactory:"org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
    connectionFactoryType:"topic",
    connectionFactoryName:"TopicConnectionFactory",
    destination:"ThreatProtectionTopic",
    properties: ["connectionfactory.TopicConnectionFactory=amqp://admin:admin@clientID/carbon?brokerlist='tcp://localhost:5672'"]
}

service<jms> ThreatProtectionJmsService {

    @http:GET {}
    resource onMessage (message m) {
        json event = messages:getJsonPayload(m);
        string eventType;
        string policyType;
        policyType, _ = (string)event.policy["type"];
        eventType, _ = (string)event.eventType;
        system:println("Threat Protection Policy Event: " + eventType);
        if (strings:contains(policyType, "JSON")) {
            json jsonPolicy = event.policy;
            dto:JSONThreatProtectionInfoDTO jsonInfo = utils:fromJSONToJSONThreatProtectionInfoDTO(jsonPolicy);
            threatprotection:configureJsonAnalyzer(jsonInfo, eventType);
        } else if (strings:contains(policyType, "XML")) {
            json xmlPolicy = event.policy;
            dto:XMLThreatProtectionInfoDTO xmlInfo = utils:fromJSONToXMLThreatProtectionInfoDTO(xmlPolicy);
            threatprotection:configureXmlAnalyzer(xmlInfo, eventType);
        } else {
            system:println("Threat Protection: Unknown event type for Threat Protection Policy. Event: " + eventType);
        }
    }
}

