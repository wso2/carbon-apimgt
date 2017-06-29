package org.wso2.carbon.apimgt.gateway;

import ballerina.lang.messages;
import ballerina.net.jms;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.errors;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtils;
import org.wso2.carbon.apimgt.gateway.holders;
import org.wso2.carbon.apimgt.gateway.dto;


@jms:JMSSource {
    factoryInitial:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61616"}
@jms:ConnectionProperty {key:"connectionFactoryType", value:"topic"}
@jms:ConnectionProperty {key:"destination", value:"ThrottleTopic"}
@jms:ConnectionProperty {key:"connectionFactoryJNDIName", value:"TopicConnectionFactory"}
@jms:ConnectionProperty {key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service ThrottleCoreJmsService {

    @http:GET {}
    resource onMessage (message m) {
        json event = messages:getJsonPayload(m);
        string eventType = (string)event[Constants:EVENT_TYPE];
        try {
            if(eventType == Constants:POLICY_CREATE){
                gatewayUtils:putIntoPolicyCache(event);
            }else if(eventType == Constants:POLICY_UPDATE){
                holders:removeFromPolicyCache((string )event.id);
                gatewayUtils:putIntoPolicyCache(event);
            }else if(eventType == Constants:POLICY_DELETE){
                holders:removeFromPolicyCache((string )event.id);
             }else if(eventType == Constants:BLOCK_CONDITION_ADD){
                dto:BlockConditionDto condition = gatewayUtils:fromJsonToBlockConditionDto(event);
                holders:addBlockConditions(condition);
            }else if(eventType == Constants:BLOCK_CONDITION_UPDATE){
                dto:BlockConditionDto condition = gatewayUtils:fromJsonToBlockConditionDto(event);
                holders:updateBlockCondition(condition);
            }else if(eventType == Constants:BLOCK_CONDITION_DELETE){
                dto:BlockConditionDto condition = gatewayUtils:fromJsonToBlockConditionDto(event);
                holders:removeBlockCondition(condition);
            }
        } catch (errors:Error e) {
        system:println( e.msg);
        system:println("Error occurred while processing gateway event ");
    }
    }
}

