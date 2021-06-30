package org.wso2.carbon.apimgt.impl.solace;

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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tools.ant.taskdefs.condition.Http;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.Application;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class SolaceAdminApis {

    private static final String baseUrl = "http://api.solace-apim.net:3000/v1/";
    private static final String encoding = Base64.getEncoder().encodeToString(("wso2:hzxVWwFQs2EEK5kK").getBytes());
    private static final String developerUserName = "dev-1";

    // check whether the environment is available
    public HttpResponse environmentGET(String organization, String environment) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseUrl + "/" + organization + "/" + "environments" + "/" + environment);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // register API in Solace using AsyncAPI Definition
    public HttpResponse registerAPI(String organization, String title, String apiDefinition) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPut request = new HttpPut(baseUrl + "/" + organization + "/apis/" + title);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
        JsonNode jsonNodeTree;
        String jsonAsYaml = null;
        //convert json to yaml
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

    // create API product in Solace
    public HttpResponse createAPIProduct(String organization, String environment,
                                         Aai20Document aai20Document, String apiProductName, String apiNameForRegistration) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost(baseUrl + "/" + organization + "/apiProducts");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
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

    // check existence of API in solace
    public HttpResponse registeredAPIGet(String organization, String apiTitle) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseUrl + "/" + organization + "/apis/" + apiTitle);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // check API product in Solace
    public HttpResponse apiProductGet(String organization, String apiProductName) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseUrl + "/" + organization + "/apiProducts/" + apiProductName);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // check developer existence in Solace
    public HttpResponse developerGet(String organization) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(baseUrl + "/" + organization + "/developers/" + developerUserName);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // check application existence in solace
    public HttpResponse applicationGet(String organization, Application application, String syntax) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request;
        if ("MQTT".equalsIgnoreCase(syntax)) {
            request = new HttpGet(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/" + application.getUUID() + "?topicSyntax=mqtt");
        } else {
            request = new HttpGet(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/" + application.getUUID());
        }
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // update application in Solace
    public HttpResponse applicationPatchAddSubscription(String organization, Application application, ArrayList<String> apiProducts) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPatch request = new HttpPatch(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/" + application.getUUID());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        // retrieve existing API products in the app
        try {
            apiProducts = retrieveApiProductsInAnApplication(applicationGet(organization, application, "default"), apiProducts);
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

    public HttpResponse applicationPatchRemoveSubscription(String organization, Application application, List<String> apiProductsToRemove) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPatch request = new HttpPatch(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/" + application.getUUID());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        // retrieve existing API products in the app
        ArrayList<String> apiProducts = new ArrayList<>();
        try {
            apiProducts = retrieveApiProductsInAnApplication(applicationGet(organization, application, "default"), apiProducts);
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

    // create new application in Solace
    public HttpResponse createApplication(String organization, Application application, ArrayList<String> apiProducts) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
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

    // delete API product from Solace Broker
    public HttpResponse deleteApiProduct(String organization, String apiProductName) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(baseUrl + "/" + organization + "/apiProducts/" + apiProductName);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // delete registered API from Solace
    public HttpResponse deleteRegisteredAPI(String organization, String title) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(baseUrl + "/" + organization + "/apis/" + title);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // delete application from Solace
    public HttpResponse deleteApplication(String organization, Application application) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/" + application.getUUID());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // rename application
    public HttpResponse renameApplication(String organization, Application application) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPatch request = new HttpPatch(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/" + application.getUUID());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
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

    //patch client ID for Solace application
    public HttpResponse patchClientIdForApplication(String organization, Application application, String consumerKey) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPatch request = new HttpPatch(baseUrl + "/" + organization + "/developers/" + developerUserName + "/apps/" + application.getUUID());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        org.json.JSONObject requestBody = buildRequestBodyForClientIdPatch(application, consumerKey);
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

    public ArrayList<String> retrieveApiProductsInAnApplication(HttpResponse applicationGetResponse, ArrayList<String> apiProducts) throws IOException {
        String appGetResponse = EntityUtils.toString(applicationGetResponse.getEntity());
        JSONObject responseJson = new JSONObject(appGetResponse);
        JSONArray apiProductsArray = responseJson.getJSONArray("apiProducts");
        for (int i = 0; i < apiProductsArray.length(); i++) {
            apiProducts.add(apiProductsArray.getString(i));
        }
        return apiProducts;
    }

    private org.json.JSONObject buildAPIProductRequestBody(Aai20Document aai20Document,
                                                       String environment, String apiNameWithContext, String registeredApiName) {
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

        /*HashSet<String> parameters = new HashSet<>();
        for (AaiChannelItem channel : aai20Document.getChannels()) {
            parameters.addAll(channel.parameters.keySet());
        }
        org.json.JSONArray attributes = new org.json.JSONArray();
        for (String parameter : parameters) {
            AaiParameter parameterObj = aai20Document.components.parameters.get(parameter);
            if (parameterObj.schema != null) {
                ObjectNode schemaNode = (ObjectNode) parameterObj.schema;
                org.json.JSONObject schemaJson = new org.json.JSONObject(schemaNode.toString());
                if (schemaJson.has("enum")) {
                    org.json.JSONArray enumArray = schemaJson.getJSONArray("enum");
                    List<String> enumList = new ArrayList<>();
                    for (int i = 0; i < enumArray.length(); i++) {
                        enumList.add(enumArray.get(i).toString());
                    }
                    //List<Object> enumList = enumArray.toList();
                    StringBuilder enumStringBuilder = new StringBuilder();
                    for (String value : enumList) {
                        enumStringBuilder.append(value).append(", ");
                    }
                    String enumString = enumStringBuilder.substring(0, enumStringBuilder.length()-2);

                    org.json.JSONObject attributeObject = new org.json.JSONObject();
                    attributeObject.put("name", parameter);
                    attributeObject.put("value", enumString);
                    attributes.put(attributeObject);
                }
            }
        }
        requestBody.put("attributes", attributes);*/

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
                                org.json.JSONObject attributeObj = getAttributesFromParameterSchema(parameterName, parameterObject1);
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

    private org.json.JSONObject getAttributesFromParameterSchema (String parameterName, AaiParameter parameterObj) {
        ObjectNode schemaNode = (ObjectNode) parameterObj.schema;
        org.json.JSONObject schemaJson = new org.json.JSONObject(schemaNode.toString());
        String enumString = "";
        if (schemaJson.has("enum")) {
            org.json.JSONArray enumArray = schemaJson.getJSONArray("enum");
            List<String> enumList = new ArrayList<>();
            for (int i = 0; i < enumArray.length(); i++) {
                enumList.add(enumArray.get(i).toString());
            }
            //List<Object> enumList = enumArray.toList();
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

    public HashSet<String> getProtocols (AaiChannelItem channel) {

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

    private HashSet<String> getProtocolsFromBindings(AaiOperationBindings bindings) {

        HashSet<String> protocolsFromBindings = new HashSet<>();

        if (bindings.http != null) { protocolsFromBindings.add("http"); }
        if (bindings.ws != null) { protocolsFromBindings.add("ws"); }
        if (bindings.kafka != null) { protocolsFromBindings.add("kafka"); }
        if (bindings.amqp != null) { protocolsFromBindings.add("amqp"); }
        if (bindings.amqp1 != null) { protocolsFromBindings.add("amqp1"); }
        if (bindings.mqtt != null) { protocolsFromBindings.add("mqtt"); }
        if (bindings.mqtt5 != null) { protocolsFromBindings.add("mqtt5"); }
        if (bindings.nats != null) { protocolsFromBindings.add("nats"); }
        if (bindings.jms != null) { protocolsFromBindings.add("jms"); }
        if (bindings.sns != null) { protocolsFromBindings.add("sns"); }
        if (bindings.sqs != null) { protocolsFromBindings.add("sqs"); }
        if (bindings.stomp != null) { protocolsFromBindings.add("stomp"); }
        if (bindings.redis != null) { protocolsFromBindings.add("redis"); }

        if (bindings.hasExtraProperties()) {
            protocolsFromBindings.addAll(bindings.getExtraPropertyNames());
        }

        return protocolsFromBindings;
    }

    private String getProtocolVersion(String protocol) {
        HashMap<String, String> protocolsWithVersions = new HashMap<>();
        protocolsWithVersions.put("http", "1.1");
        protocolsWithVersions.put("mqtt", "3.1.1");
        //protocolsWithVersions.put("mqtt5", "5.0");
        protocolsWithVersions.put("amqp", "1.0.0");
        protocolsWithVersions.put("amqps", "1.0.0");
        //protocolsWithVersions.put("amqp1", "1.0");
        protocolsWithVersions.put("secure-mqtt", "3.1.1");
        protocolsWithVersions.put("ws-mqtt", "3.1.1");
        protocolsWithVersions.put("wss-mqtt", "3.1.1");
        protocolsWithVersions.put("jms", "1.1");
        protocolsWithVersions.put("https", "1.1");
        protocolsWithVersions.put("smf", "smf");
        protocolsWithVersions.put("smfs", "smfs");
        if (protocolsWithVersions.get(protocol) != null) {
            return protocolsWithVersions.get(protocol);
        }
        return "";
    }

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
        credentialsSecret.put("consumerKey", appName+"-application-key");
        credentialsSecret.put("consumerSecret", appName+"-application-secret");
        credentialsBody.put("secret", credentialsSecret);
        requestBody.put("credentials", credentialsBody);

        return requestBody;
    }

    private org.json.JSONObject buildRequestBodyForApplicationPatchSubscriptions(ArrayList<String> apiProducts) {
        org.json.JSONObject requestBody = new org.json.JSONObject();
        org.json.JSONArray apiProductsArray = new org.json.JSONArray();
        for (String x : apiProducts) {
            apiProductsArray.put(x);
        }
        requestBody.put("apiProducts", apiProductsArray);
        return requestBody;
    }

    private org.json.JSONObject buildRequestBodyForRenamingApp(Application application) {
        org.json.JSONObject requestBody = new org.json.JSONObject();
        requestBody.put("displayName", application.getName());
        return requestBody;
    }

    private org.json.JSONObject buildRequestBodyForClientIdPatch(Application application, String consumerKey) {
        org.json.JSONObject requestBody = new org.json.JSONObject();
        org.json.JSONObject credentialsBody = new org.json.JSONObject();
        credentialsBody.put("expiresAt", -1);
        credentialsBody.put("issuedAt", 0);
        org.json.JSONObject credentialsSecret = new org.json.JSONObject();
        credentialsSecret.put("consumerKey", consumerKey);
        credentialsSecret.put("consumerSecret", application.getName()+"-application-secret");
        credentialsBody.put("secret", credentialsSecret);
        requestBody.put("credentials", credentialsBody);
        return requestBody;
    }

}