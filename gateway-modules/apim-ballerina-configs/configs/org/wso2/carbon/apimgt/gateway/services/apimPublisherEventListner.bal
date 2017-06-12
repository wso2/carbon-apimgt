package org.wso2.carbon.apimgt.gateway.services;

import ballerina.lang.messages;
import ballerina.net.jms;
import ballerina.lang.system;
import ballerina.lang.errors;
import ballerina.lang.strings;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import org.wso2.carbon.apimgt.ballerina.util as apimgtUtil;

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
            json event = messages:getJsonPayload(m);
            errors:TypeCastError err;
            string eventType;
            eventType, err = (string)event[Constants:EVENT_TYPE];
            system:println("eventType " + eventType);
            if (strings:equalsIgnoreCase(eventType, Constants:API_CREATE)) {
                json apiSummary = event.apiSummary;
                if (apiSummary != null) {

                    dto:APIDTO api = gatewayUtil:fromJSONToAPIDTO(apiSummary);
                    //Retrieve API configuration
                    string apiConfig;
                    int status;
                    status, apiConfig = gatewayUtil:getAPIServiceConfig(api.id);
                    int maxRetries = 5;
                    int i = 0;
                    while (status == Constants:NOT_FOUND) {
                        apimgtUtil:wait(10000);
                        status, apiConfig = gatewayUtil:getAPIServiceConfig(api.id);
                        i = i + 1;
                        if (i > maxRetries) {
                            break;
                        }
                    }
                    //Deploy API service
                    gatewayUtil:deployService(api, apiConfig);
                    //Update API cache
                    holder:putIntoAPICache(api);
                    gatewayUtil:retrieveResources(api.context, api.version);
                } else {
                    system:println("Invalid json received");
                }


            } else if (strings:equalsIgnoreCase(eventType, Constants:API_UPDATE)) {
                json apiSummary = event.apiSummary;
                if (apiSummary != null) {

                    dto:APIDTO api = gatewayUtil:fromJSONToAPIDTO(apiSummary);
                    //Retrieve API configuration
                    string apiConfig;
                    int status;
                    status, apiConfig = gatewayUtil:getAPIServiceConfig(api.id);
                    int maxRetries = 10;
                    int i = 0;
                    while (status == Constants:NOT_FOUND) {
                        apimgtUtil:wait(10000);
                        status, apiConfig = gatewayUtil:getAPIServiceConfig(api.id);
                        i = i + 1;
                        if (i > maxRetries) {
                            break;
                        }
                    }            //Update API service
                    gatewayUtil:deployService(api, apiConfig);
                    //Update API cache
                    holder:removeFromAPICache(api);
                    holder:putIntoAPICache(api);
                } else {
                    system:println("Invalid json received");
                }

            } else if (strings:equalsIgnoreCase(eventType, Constants:API_DELETE)) {
                json apiSummary = event.apiSummary;
                if (apiSummary != null) {
                    dto:APIDTO api = gatewayUtil:fromJSONToAPIDTO(apiSummary);
                    //Undeploy API service
                    gatewayUtil:undeployService(api);
                    //Remove from API cache
                    holder:removeFromAPICache(api);
                } else {
                    system:println("Invalid json received");
                }

            } else if (strings:equalsIgnoreCase(eventType, Constants:API_STATE_CHANGE)) {
                json apiSummary = event.apiSummary;
                if (apiSummary != null) {

                    dto:APIDTO api = gatewayUtil:fromJSONToAPIDTO(apiSummary);
                    holder:removeFromAPICache(api);
                    holder:putIntoAPICache(api);
                } else {
                    system:println("Invalid json received");
                }
            } else if (strings:equalsIgnoreCase(eventType, Constants:ENDPOINT_CREATE)) {
                json endpoint = event.endpoint;
                if (endpoint != null) {
                    dto:EndpointDto endpointDto = gatewayUtil:fromJsonToEndpointDto(endpoint);
                    holder:putIntoEndpointCache(endpointDto);
                } else {
                    system:println("Invalid json received");
                }
            } else if (strings:equalsIgnoreCase(eventType, Constants:ENDPOINT_UPDATE)) {
                json endpoint = event.endpoint;
                if (endpoint != null) {
                    dto:EndpointDto endpointDto = gatewayUtil:fromJsonToEndpointDto(endpoint);
                    holder:updateEndpointCache(endpointDto);
                } else {
                    system:println("Invalid json received");
                }
            } else if (strings:equalsIgnoreCase(eventType, Constants:ENDPOINT_DELETE)) {
                json endpoint = event.endpoint;
                if (endpoint != null) {
                    string endpointId;
                    endpointId, err = (string)endpoint.id;
                    holder:removeFromEndpointCache(endpointId);
                } else {
                    system:println("Invalid json received");
                }
            } else {
                system:println("Invalid event received");
            }


    }

}

