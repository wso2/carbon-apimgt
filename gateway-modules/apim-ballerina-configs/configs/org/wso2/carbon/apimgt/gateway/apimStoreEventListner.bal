package org.wso2.carbon.apimgt.gateway;

import ballerina.lang.messages;
import ballerina.net.jms;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.errors;
import ballerina.lang.jsons;
import ballerina.lang.strings;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;


@jms:JMSSource {
factoryInitial : "org.apache.activemq.jndi.ActiveMQInitialContextFactory",
providerUrl : "tcp://localhost:61616"}
@jms:ConnectionProperty {key:"connectionFactoryType", value:"topic"}
@jms:ConnectionProperty {key:"destination", value:"StoreTopic"}
@jms:ConnectionProperty {key:"connectionFactoryJNDIName", value:"TopicConnectionFactory"}
@jms:ConnectionProperty {key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service apimStoreEventListner {

    @http:GET {}
    resource onMessage (message m) {
        try {
            json event = messages:getJsonPayload(m);
            string eventType = jsons:getString(event, Constants:EVENT_TYPE);

            if (strings:equalsIgnoreCase(eventType, Constants:SUBSCRIPTION_CREATE)) {
                json subscriptionsList = jsons:getJson(event, "subscriptionsList");
                gatewayUtil:updateSubscriptionCache(subscriptionsList);
            } else {
                system:println("Invalid event received");
            }

        }catch(errors:Error e){
            system:println(e.msg);
            system:println("Error occurred while processing gateway event ");
        }
    }

}

