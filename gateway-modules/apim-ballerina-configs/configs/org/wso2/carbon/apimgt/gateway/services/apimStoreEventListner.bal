package org.wso2.carbon.apimgt.gateway.services;

import ballerina.lang.messages;
import ballerina.net.jms;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.errors;
import ballerina.lang.strings;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;

@jms:config {
    initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61616",
    connectionFactoryType:"topic",
    connectionFactoryName:"TopicConnectionFactory",
    destination:"StoreTopic"
}

service<jms> apimStoreEventListner {

    @http:GET {}
    resource onMessage (message m) {
        try {
            errors:TypeCastError err;
            json event = messages:getJsonPayload(m);
            string eventType;
            eventType, err = (string)event[Constants:EVENT_TYPE];
            if (strings:equalsIgnoreCase(eventType, Constants:SUBSCRIPTION_CREATE)) {
                json subscriptionsList = event.subscriptionsList;
                gatewayUtil:putIntoSubscriptionCache(subscriptionsList);
            } else if (strings:equalsIgnoreCase(eventType, Constants:SUBSCRIPTION_DELETE)) {
                json subscriptionsList = event.subscriptionsList;
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
