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
import io.apicurio.datamodels.asyncapi.models.AaiParameter;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class consists the internal Admin REST API requests to communicate with Solace broker
 */
public class SolaceAdminApis {
    
    private static final Log log = LogFactory.getLog(SolaceAdminApis.class);

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

    private String getBase64EncodedCredentials() {
        String toEncode = userName + ":" + password;
        return Base64.getEncoder().encodeToString((toEncode).getBytes());
    }

    /**
     * Check whether the environment is available
     *
     * @param organization name of the Organization
     * @param environment  name of the Environment
     * @return CloseableHttpResponse of the GET call
     */
    public CloseableHttpResponse environmentGET(String organization, String environment) {

        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpGet httpGet = new HttpGet(baseUrl + "/" + organization + "/" + "environments" + "/" + environment);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        try {
            return APIUtil.executeHTTPRequest(httpGet, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Register API in Solace using AsyncAPI Definition
     *
     * @param organization  name of the Organization
     * @param title         name of the Solace API
     * @param apiDefinition Async definition of the Solace API
     * @return CloseableHttpResponse of the PUT call
     */
    public CloseableHttpResponse registerAPI(String organization, String title, String apiDefinition) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpPut httpPut = new HttpPut(baseUrl + "/" + organization + "/apis/" + title);
        httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        httpPut.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
        JsonNode jsonNodeTree;
        String jsonAsYaml = null;
        // convert json to yaml
        try {
            jsonNodeTree = new ObjectMapper().readTree(apiDefinition);
            jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        //add definition to request body
        if (jsonAsYaml != null) {
            StringEntity params = null;
            try {
                params = new StringEntity(jsonAsYaml);
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage());
            }
            httpPut.setEntity(params);
            try {
                return APIUtil.executeHTTPRequest(httpPut, httpClient);
            } catch (IOException | APIManagementException e) {
                log.error(e.getMessage());
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
     * @return CloseableHttpResponse of the POST call
     */
    public CloseableHttpResponse createAPIProduct(String organization, String environment, Aai20Document aai20Document,
                                         String apiProductName, String apiNameForRegistration) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpPost httpPost = new HttpPost(baseUrl + "/" + organization + "/apiProducts");
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        //setRequestBody
        org.json.JSONObject requestBody = buildAPIProductRequestBody(aai20Document, environment, apiProductName,
                apiNameForRegistration);
        try {
            StringEntity params2;
            params2 = new StringEntity(requestBody.toString());
            httpPost.setEntity(params2);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        try {
            return APIUtil.executeHTTPRequest(httpPost, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Check existence of API in Solace
     *
     * @param organization name of the Organization
     * @param apiTitle     name of the API
     * @return CloseableHttpResponse of the GET call
     */
    public CloseableHttpResponse registeredAPIGet(String organization, String apiTitle) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpGet httpGet = new HttpGet(baseUrl + "/" + organization + "/apis/" + apiTitle);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        try {
            return APIUtil.executeHTTPRequest(httpGet, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Check existence of API Product in Solace
     *
     * @param organization   name of the Organization
     * @param apiProductName name of the API Product
     * @return CloseableHttpResponse of the GET call
     */
    public CloseableHttpResponse apiProductGet(String organization, String apiProductName) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpGet httpGet = new HttpGet(baseUrl + "/" + organization + "/apiProducts/" + apiProductName);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        try {
            return APIUtil.executeHTTPRequest(httpGet, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Check existence of the developer in Solace
     *
     * @param organization name of the Organization
     * @return CloseableHttpResponse of the GET call
     */
    public CloseableHttpResponse developerGet(String organization) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpGet httpGet = new HttpGet(baseUrl + "/" + organization + "/developers/" + developerUserName);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        try {
            return APIUtil.executeHTTPRequest(httpGet, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Check existence of application in Solace
     *
     * @param organization name of the Organization
     * @param uuid  Application UUID to be checked in solace
     * @param syntax       protocol type
     * @return CloseableHttpResponse of the GET call
     */
    public CloseableHttpResponse applicationGet(String organization, String uuid, String syntax) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpGet httpGet;
        if ("MQTT".equalsIgnoreCase(syntax)) {
            httpGet = new HttpGet(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/"
                    + uuid + "?topicSyntax=mqtt");
        } else {
            httpGet = new HttpGet(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/"
                    + uuid);
        }
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        try {
            return APIUtil.executeHTTPRequest(httpGet, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Add subscriptions to application in Solace and update the application
     *
     * @param organization name of the Organization
     * @param application  Application to be checked in solace
     * @param apiProducts  API products to add as subscriptions
     * @return CloseableHttpResponse of the PATCH call
     */
    public CloseableHttpResponse applicationPatchAddSubscription(String organization, Application application,
                                                                 ArrayList<String> apiProducts) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpPatch httpPatch = createHTTPPatchRequestBase(organization, application.getUUID());
        // retrieve existing API products in the app
        try {
            apiProducts = retrieveApiProductsInAnApplication(applicationGet(organization, application.getUUID(),
                    "default"), apiProducts);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        org.json.JSONObject requestBody = buildRequestBodyForApplicationPatchSubscriptions(apiProducts);
        StringEntity params = null;
        try {
            params = new StringEntity(requestBody.toString());
            httpPatch.setEntity(params);
            return APIUtil.executeHTTPRequest(httpPatch, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Remove subscriptions to application in Solace and update the application
     *
     * @param organization        name of the Organization
     * @param application         Application to be checked in solace
     * @param apiProductsToRemove List of API products to remove from subscriptions
     * @return CloseableHttpResponse of the PATCH call
     */
    public CloseableHttpResponse applicationPatchRemoveSubscription(String organization, Application application,
                                                           List<String> apiProductsToRemove) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpPatch httpPatch = createHTTPPatchRequestBase(organization, application.getUUID());
        // retrieve existing API products in the app
        ArrayList<String> apiProducts = new ArrayList<>();
        try {
            apiProducts = retrieveApiProductsInAnApplication(applicationGet(organization, application.getUUID(),
                    "default"), apiProducts);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        // remove API product from arrayList
        apiProducts.removeAll(apiProductsToRemove);

        org.json.JSONObject requestBody = buildRequestBodyForApplicationPatchSubscriptions(apiProducts);
        StringEntity params;
        try {
            params = new StringEntity(requestBody.toString());
            httpPatch.setEntity(params);
            return APIUtil.executeHTTPRequest(httpPatch, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Create new application in Solace
     *
     * @param organization name of the Organization
     * @param application  Application to be created in Solace
     * @param apiProducts  List of API products to add as subscriptions
     * @return CloseableHttpResponse of the POST call
     */
    public CloseableHttpResponse createApplication(String organization, Application application,
                                                   ArrayList<String> apiProducts) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpPost httpPost = new HttpPost(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps");
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        StringEntity params;
        try {
            org.json.JSONObject requestBody = buildRequestBodyForCreatingApp(application, apiProducts);
            params = new StringEntity(requestBody.toString());
            httpPost.setEntity(params);
            return APIUtil.executeHTTPRequest(httpPost, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Delete API Product from Solace Broker
     *
     * @param organization   name of the Organization
     * @param apiProductName name of the API product
     * @return CloseableHttpResponse of the DELETE call
     */
    public CloseableHttpResponse deleteApiProduct(String organization, String apiProductName) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/" + organization + "/apiProducts/" + apiProductName);
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        try {
            return APIUtil.executeHTTPRequest(httpDelete, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Delete registered API from Solace
     *
     * @param organization name of the Organization
     * @param title        name of the API
     * @return CloseableHttpResponse of the DELETE call
     */
    public CloseableHttpResponse deleteRegisteredAPI(String organization, String title) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/" + organization + "/apis/" + title);
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        try {
            return APIUtil.executeHTTPRequest(httpDelete, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Delete application from Solace Broker
     *
     * @param organization name of the Organization
     * @param uuid         UUID of Application object to be deleted
     * @return CloseableHttpResponse of the DELETE call
     */
    public CloseableHttpResponse deleteApplication(String organization, String uuid) {

        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/" + organization + "/developers/" + developerUserName +
                "/apps/" + uuid);
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        try {
            return APIUtil.executeHTTPRequest(httpDelete, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Rename application in Solace Broker
     *
     * @param organization name of the Organization
     * @param application  Application object to be renamed
     * @return CloseableHttpResponse of the DELETE call
     */
    public CloseableHttpResponse renameApplication(String organization, Application application) {
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpPatch httpPatch = createHTTPPatchRequestBase(organization, application.getUUID());
        org.json.JSONObject requestBody = buildRequestBodyForRenamingApp(application);
        StringEntity params;
        try {
            params = new StringEntity(requestBody.toString());
            httpPatch.setEntity(params);
            return APIUtil.executeHTTPRequest(httpPatch, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
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
     * @return CloseableHttpResponse of the PATCH call
     */
    public CloseableHttpResponse patchClientIdForApplication(String organization, Application application,
                                                             String consumerKey, String consumerSecret) {
        HttpPatch httpPatch = createHTTPPatchRequestBase(organization, application.getUUID());
        URL serviceEndpointURL = new URL(baseUrl);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        org.json.JSONObject requestBody = buildRequestBodyForClientIdPatch(application, consumerKey, consumerSecret);
        StringEntity params;
        try {
            params = new StringEntity(requestBody.toString());
            httpPatch.setEntity(params);
            return APIUtil.executeHTTPRequest(httpPatch, httpClient);
        } catch (IOException | APIManagementException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public HttpPatch createHTTPPatchRequestBase(String organization, String  applicationUUID) {
        HttpPatch httpPatch = new HttpPatch(baseUrl + "/" + organization + "/developers/" + developerUserName +
                "/apps/" + applicationUUID);
        httpPatch.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64EncodedCredentials());
        httpPatch.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        return httpPatch;
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
            if (channel.parameters != null) {
                Set<String> parameterKeySet = channel.parameters.keySet();
                for (String parameterName : parameterKeySet) {
                    if (!parameters1.contains(parameterName)) {
                        AaiParameter parameterObject = channel.parameters.get(parameterName);
                        if (parameterObject.schema != null) {
                            org.json.JSONObject attributeObj = getAttributesFromParameterSchema(parameterName,
                                    parameterObject);
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
        }
        requestBody.put("attributes", attributes1);

        HashSet<String> protocolsHashSet = new HashSet<>();
        for (AaiChannelItem channel : aai20Document.getChannels()) {
            protocolsHashSet.addAll(AsyncApiParser.getProtocols(channel));
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
     * Get the transport protocol versions
     *
     * @param protocol Transport protocol
     * @return String version of protocol
     */
    private String getProtocolVersion(String protocol) {
        HashMap<String, String> protocolsWithVersions = new HashMap<>();
        protocolsWithVersions.put(SolaceConstants.HTTP_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.HTTP_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.MQTT_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.MQTT_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.AMQP_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.AMQP_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.AMQPS_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.AMQPS_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.SECURE_MQTT_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.SECURE_MQTT_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.WS_MQTT_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.WS_MQTT_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.WSS_MQTT_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.WSS_MQTT_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.JMS_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.JMS_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.HTTPS_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.HTTPS_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.SMF_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.SMF_TRANSPORT_PROTOCOL_VERSION);
        protocolsWithVersions.put(SolaceConstants.SMFS_TRANSPORT_PROTOCOL_NAME,
                SolaceConstants.SMFS_TRANSPORT_PROTOCOL_VERSION);
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
    private org.json.JSONObject buildRequestBodyForCreatingApp(Application application, ArrayList<String> apiProducts)
            throws APIManagementException {
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
        // Set consumer key and secret for new application
        if (application.getKeys().isEmpty()) {
            throw new APIManagementException("Application keys are not generated for " + appName);
        } else {
            credentialsSecret.put("consumerKey", application.getKeys().get(0).getConsumerKey());
            credentialsSecret.put("consumerSecret", application.getKeys().get(0).getConsumerSecret());
        }
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
