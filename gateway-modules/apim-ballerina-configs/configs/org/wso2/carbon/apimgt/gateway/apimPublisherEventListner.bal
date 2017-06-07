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
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holder;


@jms:JMSSource {
factoryInitial:"org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
providerUrl:"bre/conf/jndi.properties"}
@jms:ConnectionProperty {key:"connectionFactoryType", value:"topic"}
@jms:ConnectionProperty {key:"destination", value:"PublisherTopic"}
@jms:ConnectionProperty {key:"connectionFactoryJNDIName", value:"TopicConnectionFactory"}
@jms:ConnectionProperty {key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service jmsService {

    @http:GET {}
    resource onMessage (message m) {
        try {
            json event = messages:getJsonPayload(m);
            string eventType = jsons:getString(event, Constants:EVENT_TYPE);

            if (strings:equalsIgnoreCase(eventType, Constants:API_CREATE)) {
                json apiSummary = jsons:getJson(event, "apiSummary");
                if(apiSummary != null){

                    dto:APIDTO api = gatewayUtil:fromJSONToAPIDTO(apiSummary);
                    //Retrieve API configuration
                    string apiConfig = gatewayUtil:getAPIServiceConfig(api.id);
                    //Deploy API service
                    gatewayUtil:deployService(api, apiConfig);
                    //Update API cache
                    holder:putIntoAPICache(api);
                } else {
                    system:println("Invalid json received");
                }


            } else if (strings:equalsIgnoreCase(eventType, Constants:API_UPDATE)) {
                json apiSummary = jsons:getJson(event, "apiSummary");
                if(apiSummary != null){

                    dto:APIDTO api = gatewayUtil:fromJSONToAPIDTO(apiSummary);
                    //Retrieve API configuration
                    string apiConfig = gatewayUtil:getAPIServiceConfig(api.id);
                    //Update API service
                    gatewayUtil:updateService(api, apiConfig);
                    //Update API cache
                    holder:putIntoAPICache(api);
                } else {
                    system:println("Invalid json received");
                }

            } else if (strings:equalsIgnoreCase(eventType, Constants:API_DELETE)){
                json apiSummary = jsons:getJson(event, "apiSummary");
                if(apiSummary != null){

                    dto:APIDTO api = gatewayUtil:fromJSONToAPIDTO(apiSummary);
                    //Retrieve API configuration
                    string apiConfig = gatewayUtil:getAPIServiceConfig(api.id);
                    //Undeploy API service
                    gatewayUtil:undeployService(api);
                    //Remove from API cache
                    holder:removeFromAPICache(api);
                } else {
                    system:println("Invalid json received");
                }

            } else if(strings:equalsIgnoreCase(eventType, Constants:API_STATE_CHANGE)){
                json apiSummary = jsons:getJson(event, "apiSummary");
                if(apiSummary != null){
                    //Update API cache
                    dto:APIDTO api = gatewayUtil:fromJSONToAPIDTO(apiSummary);
                    holder:putIntoAPICache(api);
                } else {
                    system:println("Invalid json received");
                }
            } else {
                system:println("Invalid event received");
            }

        }catch(errors:Error e){
            system:println("[Error] : Error occurred while processing gateway event ");
        }
    }

}

