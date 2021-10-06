/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.solace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.apicurio.datamodels.asyncapi.models.AaiChannelItem;
import io.apicurio.datamodels.asyncapi.models.AaiOperationBindings;
import io.apicurio.datamodels.asyncapi.models.AaiParameter;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashMap;


public class SolaceAdminApis {

    String baseUrl;
    String password;
    String userName;
    String developerUserName;

    public SolaceAdminApis(String baseUrl, String userName, String password, String developerUserName) {
        this.baseUrl = baseUrl;
        this.userName = userName;
        this.password = password;
        this.developerUserName = developerUserName;
    }

    private String getEncoding(){
        String toEncode = userName + ":" + password;
        return Base64.getEncoder().encodeToString((toEncode).getBytes());
    }

//    private static final String baseUrl = "http://api.solace-apim.net:3000/v1/";
//    private static final String encoding = Base64.getEncoder().encodeToString(("wso2:hzxVWwFQs2EEK5kK").getBytes());
//    private static final String developerUserName = "dev-1";


    /**
     * Check whether the environment is available
     *
     * @param organization name of the Organization
     * @param environment  name of the Environment
     * @return HttpResponse of the GET call
     */
    public HttpResponse environmentGET(String organization, String environment) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseUrl + "/" + organization + "/" + "environments" + "/" + environment);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Register API in Solace using AsyncAPI Definition
     *
     * @param organization  name of the Organization
     * @param title         name of the Solace API
     * @param apiDefinition Async definition of the Solace API
     * @return HttpResponse of the PUT call
     */
    public HttpResponse registerAPI(String organization, String title, String apiDefinition) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPut request = new HttpPut(baseUrl + "/" + organization + "/apis/" + title);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
        JsonNode jsonNodeTree;
        String jsonAsYaml = null;
        // convert json to yaml
        try {
            jsonNodeTree = new ObjectMapper().readTree(apiDefinition);
            jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //add definition to request body
        if (jsonAsYaml != null) {
            StringEntity params = null;
            try {
                params = new StringEntity(jsonAsYaml);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            request.setEntity(params);
            try {
                return httpClient.execute(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Create API Product in Solace using AsyncAPI Definition
     *
     * @param organization           name of the Organization
     * @param environment            name of the Environment
     * @param apiProductName         name of the API product
     * @param apiNameForRegistration name of the Solace API product
     * @param aai20Document          Async definition of the Solace API
     * @return HttpResponse of the POST call
     */
    public HttpResponse createAPIProduct(String organization, String environment, Aai20Document aai20Document,
                                         String apiProductName, String apiNameForRegistration) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost(baseUrl + "/" + organization + "/apiProducts");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        //setRequestBody
        org.json.JSONObject requestBody = buildAPIProductRequestBody(aai20Document, environment, apiProductName, apiNameForRegistration);
        try {
            StringEntity params2;
            params2 = new StringEntity(requestBody.toString());
            request.setEntity(params2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check existence of API in Solace
     *
     * @param organization name of the Organization
     * @param apiTitle     name of the API
     * @return HttpResponse of the GET call
     */
    public HttpResponse registeredAPIGet(String organization, String apiTitle) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseUrl + "/" + organization + "/apis/" + apiTitle);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check existence of API Product in Solace
     *
     * @param organization   name of the Organization
     * @param apiProductName name of the API Product
     * @return HttpResponse of the GET call
     */
    public HttpResponse apiProductGet(String organization, String apiProductName) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseUrl + "/" + organization + "/apiProducts/" + apiProductName);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check existence of the developer in Solace
     *
     * @param organization name of the Organization
     * @return HttpResponse of the GET call
     */
    public HttpResponse developerGet(String organization) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseUrl + "/" + organization + "/developers/" + developerUserName);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check existence of application in Solace
     *
     * @param organization name of the Organization
     * @param uuid  Application UUID to be checked in solace
     * @param syntax       protocol type
     * @return HttpResponse of the GET call
     */
    public HttpResponse applicationGet(String organization, String uuid, String syntax) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request;
        if ("MQTT".equalsIgnoreCase(syntax)) {
            request = new HttpGet(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/" + uuid + "?topicSyntax=mqtt");
        } else {
            request = new HttpGet(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/" + uuid);
        }
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Add subscriptions to application in Solace and update the application
     *
     * @param organization name of the Organization
     * @param application  Application to be checked in solace
     * @param apiProducts  API products to add as subscriptions
     * @return HttpResponse of the PATCH call
     */
    public HttpResponse applicationPatchAddSubscription(String organization, Application application, ArrayList<String>
            apiProducts) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPatch request = new HttpPatch(baseUrl + "/" + organization + "/developers/" + developerUserName +
                "/apps/" + application.getUUID());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        // retrieve existing API products in the app
        try {
            apiProducts = retrieveApiProductsInAnApplication(applicationGet(organization, application.getUUID(), "default"
            ), apiProducts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        org.json.JSONObject requestBody = buildRequestBodyForApplicationPatchSubscriptions(apiProducts);
        StringEntity params = null;
        try {
            params = new StringEntity(requestBody.toString());
            request.setEntity(params);
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Remove subscriptions to application in Solace and update the application
     *
     * @param organization        name of the Organization
     * @param application         Application to be checked in solace
     * @param apiProductsToRemove List of API products to remove from subscriptions
     * @return HttpResponse of the PATCH call
     */
    public HttpResponse applicationPatchRemoveSubscription(String organization, Application application,
                                                           List<String> apiProductsToRemove) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPatch request = new HttpPatch(baseUrl + "/" + organization + "/developers/" + developerUserName +
                "/apps/" + application.getUUID());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        // retrieve existing API products in the app
        ArrayList<String> apiProducts = new ArrayList<>();
        try {
            apiProducts = retrieveApiProductsInAnApplication(applicationGet(organization, application.getUUID(), "default"
            ), apiProducts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // remove API product from arrayList
        apiProducts.removeAll(apiProductsToRemove);

        org.json.JSONObject requestBody = buildRequestBodyForApplicationPatchSubscriptions(apiProducts);
        StringEntity params = null;
        try {
            params = new StringEntity(requestBody.toString());
            request.setEntity(params);
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create new application in Solace
     *
     * @param organization name of the Organization
     * @param application  Application to be created in Solace
     * @param apiProducts  List of API products to add as subscriptions
     * @return HttpResponse of the POST call
     */
    public HttpResponse createApplication(String organization, Application application, ArrayList<String> apiProducts) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        org.json.JSONObject requestBody = buildRequestBodyForCreatingApp(application, apiProducts);
        StringEntity params = null;
        try {
            params = new StringEntity(requestBody.toString());
            request.setEntity(params);
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete API Product from Solace Broker
     *
     * @param organization   name of the Organization
     * @param apiProductName name of the API product
     * @return HttpResponse of the DELETE call
     */
    public HttpResponse deleteApiProduct(String organization, String apiProductName) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(baseUrl + "/" + organization + "/apiProducts/" + apiProductName);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete registered API from Solace
     *
     * @param organization name of the Organization
     * @param title        name of the API
     * @return HttpResponse of the DELETE call
     */
    public HttpResponse deleteRegisteredAPI(String organization, String title) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(baseUrl + "/" + organization + "/apis/" + title);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // delete application from Solace

    /**
     * Delete application from Solace Broker
     *
     * @param organization name of the Organization
     * @param uuid         UUID of Application object to be deleted
     * @return HttpResponse of the DELETE call
     */
    public HttpResponse deleteApplication(String organization, String uuid) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(baseUrl + "/" + organization + "/developers/" + developerUserName +
                "/apps/" + uuid);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Rename application in Solace Broker
     *
     * @param organization name of the Organization
     * @param application  Application object to be renamed
     * @return HttpResponse of the DELETE call
     */
    public HttpResponse renameApplication(String organization, Application application) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPatch request = new HttpPatch(baseUrl + "/" + organization + "/developers/" + developerUserName +
                "/apps/" + application.getUUID());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        org.json.JSONObject requestBody = buildRequestBodyForRenamingApp(application);
        StringEntity params = null;
        try {
            params = new StringEntity(requestBody.toString());
            request.setEntity(params);
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Patch client ID for Solace application
     *
     * @param organization name of the Organization
     * @param application  Application object to be renamed
     * @param consumerKey  Consumer key to be used when patching
     * @param consumerSecret Consumer secret to be used when patching
     * @return HttpResponse of the PATCH call
     */
    public HttpResponse patchClientIdForApplication(String organization, Application application, String consumerKey,
                                                    String consumerSecret) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPatch request = new HttpPatch(baseUrl + "/" + organization + "/developers/" + developerUserName +
                "/apps/" + application.getUUID());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getEncoding());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        org.json.JSONObject requestBody = buildRequestBodyForClientIdPatch(application, consumerKey, consumerSecret);
        StringEntity params = null;
        try {
            params = new StringEntity(requestBody.toString());
            request.setEntity(params);
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get list of API products subscribed to an application
     *
     * @param applicationGetResponse Application response of GET call
     * @param apiProducts            List of API products
     * @return ArrayList<String> List of API products in application
     * @throws IOException if error occurs when retrieving API products
     */
    public ArrayList<String> retrieveApiProductsInAnApplication(HttpResponse applicationGetResponse,
                                                                ArrayList<String> apiProducts) throws IOException {
        String appGetResponse = EntityUtils.toString(applicationGetResponse.getEntity());
        JSONObject responseJson = new JSONObject(appGetResponse);
        JSONArray apiProductsArray = responseJson.getJSONArray("apiProducts");
        for (int i = 0; i < apiProductsArray.length(); i++) {
            apiProducts.add(apiProductsArray.getString(i));
        }
        return apiProducts;
    }

    /**
     * Build the request body for API product creation request
     *
     * @param aai20Document      Async definition of the Solace API
     * @param environment        name of the Environment in Solace
     * @param apiNameWithContext name of the API with context
     * @param registeredApiName  name of the API in Solace
     * @return org.json.JSON Object of request body
     */
    private org.json.JSONObject buildAPIProductRequestBody(Aai20Document aai20Document, String environment,
                                                           String apiNameWithContext, String registeredApiName) {
        org.json.JSONObject requestBody = new org.json.JSONObject();

        org.json.JSONArray apiName = new org.json.JSONArray();
        apiName.put(registeredApiName);
        requestBody.put("apis", apiName);

        requestBody.put("approvalType", "auto");
        if (aai20Document.info.description != null) {
            requestBody.put("description", aai20Document.info.description);
        } else {
            requestBody.put("description", apiNameWithContext);
        }
        requestBody.put("displayName", apiNameWithContext);
        requestBody.put("pubResources", new org.json.JSONArray());
        requestBody.put("subResources", new org.json.JSONArray());
        requestBody.put("name", apiNameWithContext);

        org.json.JSONArray environments = new org.json.JSONArray();
        environments.put(environment);
        requestBody.put("environments", environments);

        HashSet<String> parameters1 = new HashSet<>();
        org.json.JSONArray attributes1 = new org.json.JSONArray();
        for (AaiChannelItem channel : aai20Document.getChannels()) {
            Set<String> parameterKeySet = channel.parameters.keySet();
            for (String parameterName : parameterKeySet) {
                if (!parameters1.contains(parameterName)) {
                    AaiParameter parameterObject = channel.parameters.get(parameterName);
                    if (parameterObject.schema != null) {
                        org.json.JSONObject attributeObj = getAttributesFromParameterSchema(parameterName, parameterObject);
                        if (attributeObj != null) {
                            attributes1.put(attributeObj);
                            parameters1.add(parameterName);
                        }
                    } else if (parameterObject.$ref != null) {
                        if (aai20Document.components.parameters != null) {
                            AaiParameter parameterObject1 = aai20Document.components.parameters.get(parameterName);
                            if (parameterObject1.schema != null) {
                                org.json.JSONObject attributeObj = getAttributesFromParameterSchema(parameterName,
                                        parameterObject1);
                                if (attributeObj != null) {
                                    attributes1.put(attributeObj);
                                    parameters1.add(parameterName);
                                }
                            }
                        }
                    }
                }
            }
        }
        requestBody.put("attributes", attributes1);

        HashSet<String> protocolsHashSet = new HashSet<>();
        for (AaiChannelItem channel : aai20Document.getChannels()) {
            protocolsHashSet.addAll(getProtocols(channel));
        }
        org.json.JSONArray protocols = new org.json.JSONArray();
        for (String protocol : protocolsHashSet) {
            org.json.JSONObject protocolObject = new org.json.JSONObject();
            protocolObject.put("name", protocol);
            protocolObject.put("version", getProtocolVersion(protocol));
            protocols.put(protocolObject);
        }
        requestBody.put("protocols", protocols);

        return requestBody;
    }

    /**
     * Get the attributes from parameter schema
     *
     * @param parameterObj  Parameter object
     * @param parameterName name of the parameter
     * @return org.json.JSON Object of request body
     */
    private org.json.JSONObject getAttributesFromParameterSchema(String parameterName, AaiParameter parameterObj) {
        ObjectNode schemaNode = (ObjectNode) parameterObj.schema;
        org.json.JSONObject schemaJson = new org.json.JSONObject(schemaNode.toString());
        String enumString = "";
        if (schemaJson.has("enum")) {
            org.json.JSONArray enumArray = schemaJson.getJSONArray("enum");
            List<String> enumList = new ArrayList<>();
            for (int i = 0; i < enumArray.length(); i++) {
                enumList.add(enumArray.get(i).toString());
            }
            StringBuilder enumStringBuilder = new StringBuilder();
            for (String value : enumList) {
                enumStringBuilder.append(value).append(", ");
            }
            enumString = enumStringBuilder.substring(0, enumStringBuilder.length() - 2);

        }
        org.json.JSONObject attributeObject = new org.json.JSONObject();
        if (!enumString.isEmpty()) {
            attributeObject.put("name", parameterName);
            attributeObject.put("value", enumString);
            return attributeObject;
        }
        return null;
    }

    /**
     * Get the transport protocols
     *
     * @param channel AaiChannelItem to get protocol
     * @return HashSet<String> set of transport protocols
     */
    public HashSet<String> getProtocols(AaiChannelItem channel) {

        HashSet<String> protocols = new HashSet<>();

        if (channel.subscribe != null) {
            if (channel.subscribe.bindings != null) {
                protocols.addAll(getProtocolsFromBindings(channel.subscribe.bindings));
            }
        }
        if (channel.publish != null) {
            if (channel.publish.bindings != null) {
                protocols.addAll(getProtocolsFromBindings(channel.publish.bindings));
            }
        }

        return protocols;
    }

    /**
     * Get the transport protocols the bindings
     *
     * @param bindings AaiOperationBindings to get protocols
     * @return HashSet<String> set of transport protocols
     */
    private HashSet<String> getProtocolsFromBindings(AaiOperationBindings bindings) {

        HashSet<String> protocolsFromBindings = new HashSet<>();

        if (bindings.http != null) {
            protocolsFromBindings.add(APIConstants.HTTP_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.ws != null) {
            protocolsFromBindings.add(APIConstants.WS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.kafka != null) {
            protocolsFromBindings.add(APIConstants.KAFKA_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.amqp != null) {
            protocolsFromBindings.add(APIConstants.AMQP_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.amqp1 != null) {
            protocolsFromBindings.add(APIConstants.AMQP1_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.mqtt != null) {
            protocolsFromBindings.add(APIConstants.MQTT_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.mqtt5 != null) {
            protocolsFromBindings.add(APIConstants.MQTT5_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.nats != null) {
            protocolsFromBindings.add(APIConstants.NATS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.jms != null) {
            protocolsFromBindings.add(APIConstants.JMS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.sns != null) {
            protocolsFromBindings.add(APIConstants.SNS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.sqs != null) {
            protocolsFromBindings.add(APIConstants.SQS_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.stomp != null) {
            protocolsFromBindings.add(APIConstants.STOMP_TRANSPORT_PROTOCOL_NAME);
        }
        if (bindings.redis != null) {
            protocolsFromBindings.add(APIConstants.REDIS_TRANSPORT_PROTOCOL_NAME);
        }

        if (bindings.hasExtraProperties()) {
            protocolsFromBindings.addAll(bindings.getExtraPropertyNames());
        }

        return protocolsFromBindings;
    }

    /**
     * Get the transport protocol versions
     *
     * @param protocol Transport protocol
     * @return String version of protocol
     */
    private String getProtocolVersion(String protocol) {
        HashMap<String, String> protocolsWithVersions = new HashMap<>();
        protocolsWithVersions.put(APIConstants.HTTP_TRANSPORT_PROTOCOL_NAME, APIConstants.HTTP_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.MQTT_TRANSPORT_PROTOCOL_NAME, APIConstants.MQTT_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.AMQP_TRANSPORT_PROTOCOL_NAME, APIConstants.AMQP_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.AMQPS_TRANSPORT_PROTOCOL_NAME, APIConstants.AMQPS_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.SECURE_MQTT_TRANSPORT_PROTOCOL_NAME, APIConstants.SECURE_MQTT_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.WS_MQTT_TRANSPORT_PROTOCOL_NAME, APIConstants.WS_MQTT_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.WSS_MQTT_TRANSPORT_PROTOCOL_NAME, APIConstants.WSS_MQTT_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.JMS_TRANSPORT_PROTOCOL_NAME, APIConstants.JMS_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.HTTPS_TRANSPORT_PROTOCOL_NAME, APIConstants.HTTPS_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.SMF_TRANSPORT_PROTOCOL_NAME, APIConstants.SMF_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(APIConstants.SMFS_TRANSPORT_PROTOCOL_NAME, APIConstants.SMFS_TRANSPORT_PROTOCOL_VERSION);
        if (protocolsWithVersions.get(protocol) != null) {
            return protocolsWithVersions.get(protocol);
        }
        return "";
    }

    /**
     * Build the request body for application creation request
     *
     * @param application Application to be created in Solace
     * @param apiProducts List of API products to add as subscriptions
     * @return org.json.JSON Object of request body
     */
    private org.json.JSONObject buildRequestBodyForCreatingApp(Application application, ArrayList<String> apiProducts) {
        org.json.JSONObject requestBody = new org.json.JSONObject();
        String appName = application.getName();

        requestBody.put("name", application.getUUID());
        requestBody.put("expiresIn", -1);
        requestBody.put("displayName", appName);

        //add api products
        org.json.JSONArray apiProductsArray = new org.json.JSONArray();
        for (String x : apiProducts) {
            apiProductsArray.put(x);
        }
        requestBody.put("apiProducts", apiProductsArray);

        //add credentials
        org.json.JSONObject credentialsBody = new org.json.JSONObject();
        credentialsBody.put("expiresAt", -1);
        credentialsBody.put("issuedAt", 0);
        org.json.JSONObject credentialsSecret = new org.json.JSONObject();
        credentialsSecret.put("consumerKey", appName + "-application-key");
        credentialsSecret.put("consumerSecret", appName + "-application-secret");
        credentialsBody.put("secret", credentialsSecret);
        requestBody.put("credentials", credentialsBody);

        return requestBody;
    }

    /**
     * Build the request body for application patch subscription request
     *
     * @param apiProducts List of API products to add as subscriptions
     * @return org.json.JSON Object of request body
     */
    private org.json.JSONObject buildRequestBodyForApplicationPatchSubscriptions(ArrayList<String> apiProducts) {
        org.json.JSONObject requestBody = new org.json.JSONObject();
        org.json.JSONArray apiProductsArray = new org.json.JSONArray();
        for (String x : apiProducts) {
            apiProductsArray.put(x);
        }
        requestBody.put("apiProducts", apiProductsArray);
        return requestBody;
    }

    /**
     * Build the request body for application rename  request
     *
     * @param application Application to be renamed in Solace
     * @return org.json.JSON Object of request body
     */
    private org.json.JSONObject buildRequestBodyForRenamingApp(Application application) {
        org.json.JSONObject requestBody = new org.json.JSONObject();
        requestBody.put("displayName", application.getName());
        return requestBody;
    }

    /**
     * Build the request body for patch client ID for Solace application
     *
     * @param application Application object to be renamed
     * @param consumerKey Consumer key to be used when patching
     * @param consumerSecret Consumer secret to be used when patching
     * @return org.json.JSON Object of request body
     */
    private org.json.JSONObject buildRequestBodyForClientIdPatch(Application application, String consumerKey,
                                                                 String consumerSecret) {
        org.json.JSONObject requestBody = new org.json.JSONObject();
        org.json.JSONObject credentialsBody = new org.json.JSONObject();
        credentialsBody.put("expiresAt", -1);
        credentialsBody.put("issuedAt", 0);
        org.json.JSONObject credentialsSecret = new org.json.JSONObject();
        credentialsSecret.put("consumerKey", consumerKey);
        credentialsSecret.put("consumerSecret", consumerSecret);
        credentialsBody.put("secret", credentialsSecret);
        requestBody.put("credentials", credentialsBody);
        return requestBody;
    }

}