package org.wso2.carbon.apimgt.gateway;

import ballerina.lang.messages;
import ballerina.net.jms;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.errors;
import ballerina.lang.strings;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.holders as throttle;
import org.wso2.carbon.apimgt.gateway.utils as util;


@jms:JMSSource {
factoryInitial : "org.apache.activemq.jndi.ActiveMQInitialContextFactory",
providerUrl : "tcp://localhost:61616"}
@jms:ConnectionProperty{key:"connectionFactoryType", value:"topic"}
@jms:ConnectionProperty{key:"destination", value:"TEST.FOO"}
@jms:ConnectionProperty{key:"connectionFactoryJNDIName", value:"TopicConnectionFactory"}
@jms:ConnectionProperty{key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service ThrottleJmsService {

    @http:GET {}
    resource onMessage (message m) {
        try {
            
            system:println("OnMessage fired ............");

            json event = {};
            
            string receivedMessage = messages:getStringPayload(m);
            
            // Should be removed after getting a proper json message from TM
            if(strings:contains(receivedMessage,"siddhiEventId")){
                string[] tempArray = strings:split(receivedMessage, "\n");
                receivedMessage = tempArray[1]; // removing siddhi ID
                string[] attributes = strings:split(receivedMessage, ",");
                event.throttleKey = attributes[0];
                event.isThrottled = attributes[1];
                event.expiryTimeStamp = attributes[2];
            }else{
                event = messages:getJsonPayload(m);
            }

            system:println("Throttling Message received : " + (string)event);
            
            if ("" != util:getJsonString(event, Constants:THROTTLE_KEY)) {
               
                 // This message contains throttle data in map which contains Keys
                 // throttleKey - Key of particular throttling level
                 // isThrottled - Whether message has throttled or not
                 // expiryTimeStamp - When the throttling time window will expires
                 
                handleThrottleUpdateMessage(event);
            } else if ("" != util:getJsonString(event, Constants:POLICY_TEMPLATE_KEY)) {
                
                 // This message contains key template data
                 // keyTemplateValue - Value of key template
                 // keyTemplateState - whether key template active or not
                handleKeyTemplateMessage(event);
            }
            
        }catch(errors:Error e){
        	system:println("Error occured... " + e.msg);
        }
    }

}

function handleThrottleUpdateMessage(json event){

    string throttleKey = util:getJsonString(event, Constants:THROTTLE_KEY);
    string throttleState = util:getJsonString(event, Constants:IS_THROTTLED);
    string timeStamp = util:getJsonString(event, Constants:EXPIRY_TIMESTAMP);

    system:println("Received Key -  throttleKey : " + throttleKey + " , " + "isThrottled :" + throttleState + " , expiryTime : " + timeStamp);
    
    if (throttleState == Constants:TRUE) {
        throttle:addThrottleData(throttleKey, timeStamp);
    } else {
        throttle:removeThrottleData(throttleKey);
    }
}


function handleKeyTemplateMessage(json event) {
    
    system:println("Received Key -  KeyTemplate : " + (string )event[Constants:KEY_TEMPLATE_KEY]);
    
    string keyTemplateValue = util:getJsonString(event, Constants:KEY_TEMPLATE_KEY);
    string keyTemplateState = util:getJsonString(event, Constants:KEY_TEMPLATE_KEY_STATE);
    if (Constants:ADD == keyTemplateState) {
        throttle:addKeyTemplate(keyTemplateValue, keyTemplateValue);
    } else {
        throttle:removeKeyTemplate(keyTemplateValue);
    }
}
