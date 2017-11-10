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
    initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61616",
    connectionFactoryType:"topic",
    connectionFactoryName:"TopicConnectionFactory",
    destination:"ThreatProtectionTopic"
}
service<jms> ThreatProtectionJmsService {

    @http:GET {}
    resource onMessage (message m) {
        json event = messages:getJsonPayload(m);
        string eventType;
        eventType, _ = (string)event.eventType;
        system:println(event);
        if (strings:contains(eventType, "JSON")) {
            json jsonPolicy = event.policy;
            dto:JSONThreatProtectionInfoDTO jsonInfo = utils:fromJSONToJSONThreatProtectionInfoDTO(jsonPolicy);
            threatprotection:configureJsonAnalyzer(jsonInfo, eventType);
        } else if (strings:contains(eventType, "XML")) {
            json xmlPolicy = event.policy;
            dto:XMLThreatProtectionInfoDTO xmlInfo = utils:fromJSONToXMLThreatProtectionInfoDTO(xmlPolicy);
            threatprotection:configureXmlAnalyzer(xmlInfo, eventType);
        } else {
            system:println("Threat Protection: Unknown event type for Threat Protection Policy. Event: " + eventType);
        }
    }
}

