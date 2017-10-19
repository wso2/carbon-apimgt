package org.wso2.carbon.apimgt.gateway.services;

import ballerina.net.jms;
import ballerina.lang.system;

@jms:config {
    initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61616",
    connectionFactoryType:"topic",
    connectionFactoryName:"TopicConnectionFactory",
    destination:"PublisherTopic",
    acknowledgmentMode:"AUTO_ACKNOWLEDGE"
}
service<jms> apimPublisherEventListner {

    resource onMessage (message m) {
        system:println("event received ");
    }

}

