package org.wso2.carbon.apimgt.gateway;

import ballerina.lang.messages;
import ballerina.net.jms;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.errors;
import ballerina.lang.jsons;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.holders as throttle;


@jms:JMSSource {
factoryInitial:"org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
providerUrl:"/Users/Sam/Documents/Ballerina_GIT/tools-distribution/modules/ballerina-tools/target/ballerina-tools-0.88-SNAPSHOT/bin/jndi.properties"}
@jms:ConnectionProperty {key:"connectionFactoryType", value:"topic"}
@jms:ConnectionProperty {key:"destination", value:"throttleData"}
@jms:ConnectionProperty {key:"connectionFactoryJNDIName", value:"TopicConnectionFactory"}
@jms:ConnectionProperty {key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service ThrottleJmsService {

    @http:GET {}
    resource onMessage (message m) {
        try {
            system:println("OnMessage fired ............");

            json event = messages:getJsonPayload(m);

            system:println("Throttling Message received : " + (string)event);
            string value = jsons:getString(event, Constants:BLOCKING_CONDITION_KEY) ;
            if ("" != jsons:getString(event, Constants:THROTTLE_KEY)) {
               
                 // This message contains throttle data in map which contains Keys
                 // throttleKey - Key of particular throttling level
                 // isThrottled - Whether message has throttled or not
                 // expiryTimeStamp - When the throttling time window will expires
                 
                handleThrottleUpdateMessage(event);
            } else if ("" != jsons:getString(event, Constants:BLOCKING_CONDITION_KEY)) {
                
                 // This message contains blocking condition data
                 // blockingCondition - Blocking condition type
                 // conditionValue - blocking condition value
                 // state - State whether blocking condition is enabled or not
                handleBlockingMessage(event);
            } else if ("" != jsons:getString(event, Constants:POLICY_TEMPLATE_KEY)) {
                
                 // This message contains key template data
                 // keyTemplateValue - Value of key template
                 // keyTemplateState - whether key template active or not
                handleKeyTemplateMessage(event);
            }
            
        }catch(errors:Error e){
        	system:println("Error occured... ");
        }
    }

}

function handleThrottleUpdateMessage(json event){

    string throttleKey = jsons:getString(event, Constants:THROTTLE_KEY);
    string throttleState = jsons:getString(event, Constants:IS_THROTTLED);
    string timeStamp = jsons:getString(event, Constants:EXPIRY_TIMESTAMP);

    system:println("Received Key -  throttleKey : " + throttleKey + " , " + "isThrottled :" + throttleState + " , expiryTime : " + timeStamp);
    
    if (throttleState == Constants:TRUE) {
        throttle:addThrottleData(throttleKey, timeStamp);
    } else {
        throttle:removeThrottleData(throttleKey);
    }
}


function handleBlockingMessage(json event) {
    system:println("Received Key -  blockingCondition : " + jsons:getString(event, Constants:BLOCKING_CONDITION_KEY) + " , " +
              "conditionValue :" + jsons:getString(event, Constants:BLOCKING_CONDITION_VALUE));
    
    string condition = (string)jsons:getString(event, Constants:BLOCKING_CONDITION_KEY);
    string conditionValue = (string)jsons:getString(event, Constants:BLOCKING_CONDITION_VALUE);
    string conditionState = (string)jsons:getString(event, Constants:BLOCKING_CONDITION_STATE);

    if (Constants:BLOCKING_CONDITIONS_APPLICATION == condition) {
        if (Constants:TRUE == conditionState) {
            throttle:addApplicationBlockingCondition(conditionValue, conditionValue);
        } else {
            throttle:removeApplicationBlockingCondition(conditionValue);
        }
    } else if (Constants:BLOCKING_CONDITIONS_API == condition) {
        if (Constants:TRUE == conditionState) {
            throttle:addAPIBlockingCondition(conditionValue, conditionValue);
        } else {
            throttle:removeAPIBlockingCondition(conditionValue);
        }
    } else if (Constants:BLOCKING_CONDITIONS_USER == condition) {
        if (Constants:TRUE == conditionState) {
            throttle:addUserBlockingCondition(conditionValue, conditionValue);
        } else {
            throttle:removeUserBlockingCondition(conditionValue);
        }
    } else if (Constants:BLOCKING_CONDITIONS_IP == condition) {
        if (Constants:TRUE == conditionState) {
            throttle:addIpBlockingCondition(conditionValue, conditionValue);
        } else {
            throttle:removeIpBlockingCondition(conditionValue);
        }
    }
}

function handleKeyTemplateMessage(json event) {
    
    system:println("Received Key -  KeyTemplate : " + jsons:getString(event, Constants:KEY_TEMPLATE_KEY));
    
    string keyTemplateValue = jsons:getString(event, Constants:KEY_TEMPLATE_KEY);
    string keyTemplateState = jsons:getString(event, Constants:KEY_TEMPLATE_KEY_STATE);
    if (Constants:ADD == keyTemplateState) {
        throttle:addKeyTemplate(keyTemplateValue, keyTemplateValue);
    } else {
        throttle:removeKeyTemplate(keyTemplateValue);
    }
}

