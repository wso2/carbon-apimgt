package org.wso2.carbon.apimgt.gateway;

import ballerina.lang.messages;
import ballerina.net.jms;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.errors;
import ballerina.lang.strings;
import ballerina.lang.jsons;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;

service<jms> apimStoreEventListner {
@jms:JMSSource {
    factoryInitial:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61616"}
@jms:ConnectionProperty {key:"connectionFactoryType", value:"topic"}
@jms:ConnectionProperty {key:"destination", value:"StoreTopic"}
@jms:ConnectionProperty {key:"connectionFactoryJNDIName", value:"TopicConnectionFactory"}
@jms:ConnectionProperty {key:"subscriptionDurable", value:"true"}
@jms:ConnectionProperty {key:"durableSubscriberClientID", value:"apimStoreEventListner"}
@jms:ConnectionProperty {key:"durableSubscriberName", value:"apimStoreEventListner"}
@jms:ConnectionProperty {key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service apimStoreEventListner {


    @http:GET {}
    resource onMessage (message m) {
        try {
            errors:TypeCastError err;
            json event = messages:getJsonPayload(m);
            string eventType;
            eventType, err = (string)event[Constants:EVENT_TYPE];
            if (strings:equalsIgnoreCase(eventType, Constants:SUBSCRIPTION_CREATE)) {
                system:println("creating subscription....");
                json subscriptionsList = event.subscriptionsList;
                system:println(jsons:toString(subscriptionsList));
                gatewayUtil:putIntoSubscriptionCache(subscriptionsList);
            } else if (strings:equalsIgnoreCase(eventType, Constants:SUBSCRIPTION_DELETE)) {
                system:println("deleting subscription...");
                json subscriptionsList = event.subscriptionsList;
                system:println(jsons:toString(subscriptionsList));
                gatewayUtil:removeFromSubscriptionCache(subscriptionsList);
            } else if (strings:equalsIgnoreCase(eventType, Constants:APPLICATION_CREATE)) {
                gatewayUtil:putIntoApplicationCache(event);
            } else if (strings:equalsIgnoreCase(eventType, Constants:APPLICATION_UPDATE)) {
                gatewayUtil:putIntoApplicationCache(event);
            } else if (strings:equalsIgnoreCase(eventType, Constants:APPLICATION_DELETE)) {
                gatewayUtil:removeFromApplicationCache(event);
            } else if (strings:equalsIgnoreCase(eventType, Constants:SUBSCRIPTION_STATUS_CHANGE)) {
                json subscriptionsList = event.subscriptionsList;
                gatewayUtil:removeFromSubscriptionCache(subscriptionsList);
                gatewayUtil:putIntoSubscriptionCache(subscriptionsList);
            } else {
                system:println("Invalid event received");
            }

        } catch (errors:Error e) {
            system:println(e.msg);
            system:println("Error occurred while processing gateway event ");
        }
    }

}
