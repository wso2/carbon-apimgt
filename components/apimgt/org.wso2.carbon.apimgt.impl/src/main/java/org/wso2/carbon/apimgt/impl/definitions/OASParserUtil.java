/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.definitions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.RefModel;
import io.swagger.models.RefPath;
import io.swagger.models.RefResponse;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.RefProperty;
import io.swagger.v3.parser.ObjectMapperFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * Provide common functions related to OAS
 */
public class OASParserUtil {
    private static final Log log = LogFactory.getLog(OASParserUtil.class);
    private static APIDefinition oas2Parser = new OAS2Parser();
    private static APIDefinition oas3Parser = new OAS3Parser();
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Return correct OAS parser by validating give definition with OAS 2/3 parsers.
     *
     * @param apiDefinition OAS definition
     * @return APIDefinition APIDefinition parser
     * @throws APIManagementException If error occurred while parsing definition.
     */
    public static APIDefinition getOASParser(String apiDefinition) throws APIManagementException {
        ObjectMapper mapper;
        if (apiDefinition.trim().startsWith("{")) {
            mapper = ObjectMapperFactory.createJson();
        } else {
            mapper = ObjectMapperFactory.createYaml();
        }
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(apiDefinition.getBytes());
        } catch (IOException e) {
            throw new APIManagementException("Error occurred while parsing OAS definition", e);
        }
        ObjectNode node = (ObjectNode) rootNode;
        JsonNode openapi = node.get("openapi");
        if (openapi != null && openapi.asText().startsWith("3.")) {
            return oas3Parser;
        }
        JsonNode swagger = node.get("swagger");
        if (swagger != null) {
            return oas2Parser;
        }

        throw new APIManagementException("Invalid OAS definition provided.");
    }

    /**
     * Try to validate a give openAPI definition using OpenAPI 3 parser
     *
     * @param apiDefinition     definition
     * @param returnJsonContent whether to return definition as a json content
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException if error occurred while parsing definition
     */
    public static APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse = oas3Parser
                .validateAPIDefinition(apiDefinition, returnJsonContent);
        if (!validationResponse.isValid()) {
            for (ErrorHandler handler : validationResponse.getErrorItems()) {
                if (ExceptionCodes.INVALID_OAS3_FOUND.getErrorCode() == handler.getErrorCode()) {
                    return tryOAS2Validation(apiDefinition, returnJsonContent);
                }
            }
        }
        return validationResponse;
    }

    /**
     * Try to validate a give openAPI definition using swagger parser
     *
     * @param apiDefinition     definition
     * @param returnJsonContent whether to return definition as a json content
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException if error occurred while parsing definition
     */
    private static APIDefinitionValidationResponse tryOAS2Validation(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse = oas2Parser
                .validateAPIDefinition(apiDefinition, returnJsonContent);
        if (!validationResponse.isValid()) {
            for (ErrorHandler handler : validationResponse.getErrorItems()) {
                if (ExceptionCodes.INVALID_OAS2_FOUND.getErrorCode() == handler.getErrorCode()) {
                    addErrorToValidationResponse(validationResponse, "attribute swagger or openapi should present");
                    return validationResponse;
                }
            }
        }
        return validationResponse;
    }

    /**
     * Update the APIDefinitionValidationResponse object with success state using the values given
     *
     * @param validationResponse    APIDefinitionValidationResponse object to be updated
     * @param originalAPIDefinition original API Definition
     * @param openAPIVersion        version of OpenAPI Spec (2.0 or 3.0.0)
     * @param title                 title of the OpenAPI Definition
     * @param version               version of the OpenAPI Definition
     * @param context               base path of the OpenAPI Definition
     * @param description           description of the OpenAPI Definition
     */
    public static void updateValidationResponseAsSuccess(APIDefinitionValidationResponse validationResponse,
            String originalAPIDefinition, String openAPIVersion, String title, String version, String context,
            String description) {
        validationResponse.setValid(true);
        validationResponse.setContent(originalAPIDefinition);
        APIDefinitionValidationResponse.Info info = new APIDefinitionValidationResponse.Info();
        info.setOpenAPIVersion(openAPIVersion);
        info.setName(title);
        info.setVersion(version);
        info.setContext(context);
        info.setDescription(description);
        validationResponse.setInfo(info);
    }

    /**
     * Add error item with the provided message to the provided validation response object
     *
     * @param validationResponse APIDefinitionValidationResponse object
     * @param errMessage         error message
     * @return added ErrorItem object
     */
    public static ErrorItem addErrorToValidationResponse(APIDefinitionValidationResponse validationResponse,
            String errMessage) {
        ErrorItem errorItem = new ErrorItem();
        errorItem.setErrorCode(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode());
        errorItem.setMessage(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorMessage());
        errorItem.setDescription(errMessage);
        validationResponse.getErrorItems().add(errorItem);
        return errorItem;
    }

    /**
     * Creates a json string using the swagger object.
     *
     * @param swaggerObj swagger object
     * @return json string using the swagger object
     * @throws APIManagementException error while creating swagger json
     */
    public static String getSwaggerJsonString(Swagger swaggerObj) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        //this is to ignore "originalRef" in schema objects
        mapper.addMixIn(RefModel.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefProperty.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefPath.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefParameter.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefResponse.class, IgnoreOriginalRefMixin.class);

        //this is to ignore "responseSchema" in response schema objects
        mapper.addMixIn(Response.class, ResponseSchemaMixin.class);
        try {
            return new String(mapper.writeValueAsBytes(swaggerObj));
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error while generating Swagger json from model", e);
        }
    }

    /**
     * This method validates the given OpenAPI definition by URL
     *
     * @param url               URL of the API definition
     * @param returnJsonContent whether to return the converted json form of the
     * @return APIDefinitionValidationResponse object with validation information
     */
    public static APIDefinitionValidationResponse validateAPIDefinitionByURL(String url, boolean returnJsonContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        try {
            URL urlObj = new URL(url);
            HttpClient httpClient = APIUtil.getHttpClient(urlObj.getPort(), urlObj.getProtocol());
            HttpGet httpGet = new HttpGet(url);

            HttpResponse response = httpClient.execute(httpGet);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                String responseStr = EntityUtils.toString(response.getEntity());
                validationResponse = validateAPIDefinition(responseStr, returnJsonContent);
            } else {
                validationResponse.setValid(false);
                validationResponse.getErrorItems().add(ExceptionCodes.OPENAPI_URL_NO_200);
            }
        } catch (IOException e) {
            ErrorHandler errorHandler = ExceptionCodes.OPENAPI_URL_MALFORMED;
            //Log the error and continue since this method is only intended to validate a definition
            log.error(errorHandler.getErrorDescription(), e);

            validationResponse.setValid(false);
            validationResponse.getErrorItems().add(errorHandler);
        }
        return validationResponse;
    }

    /**
     * This method returns the timestamps for a given API
     *
     * @param apiIdentifier
     * @param registry
     * @return
     * @throws APIManagementException
     */
    public static Map<String, String> getAPIOpenAPIDefinitionTimeStamps(APIIdentifier apiIdentifier, Registry registry)
            throws APIManagementException {
        Map<String, String> timeStampMap = new HashMap<String, String>();
        String resourcePath = APIUtil
                .getOpenAPIDefinitionFilePath(apiIdentifier.getApiName(), apiIdentifier.getVersion(),
                        apiIdentifier.getProviderName());
        try {
            if (registry.resourceExists(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME);
                Date lastModified = apiDocResource.getLastModified();
                Date createdTime = apiDocResource.getCreatedTime();
                if (lastModified != null) {
                    timeStampMap.put("UPDATED_TIME", String.valueOf(lastModified.getTime()));
                } else {
                    timeStampMap.put("CREATED_TIME", String.valueOf(createdTime.getTime()));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Resource " + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME + " not found at "
                            + resourcePath);
                }
            }
        } catch (RegistryException e) {
            handleException(
                    "Error while retrieving OpenAPI v2.0 or v3.0.0 updated time for " + apiIdentifier.getApiName() + '-'
                            + apiIdentifier.getVersion(), e);
        }
        return timeStampMap;
    }

    /**
     * This method saves api definition json in the registry
     *
     * @param api               API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     * @param registry          user registry
     * @throws APIManagementException
     */
    public static void saveAPIDefinition(API api, String apiDefinitionJSON, Registry registry)
            throws APIManagementException {
        String apiName = api.getId().getApiName();
        String apiVersion = api.getId().getVersion();
        String apiProviderName = api.getId().getProviderName();

        try {
            String resourcePath = APIUtil.getOpenAPIDefinitionFilePath(apiName, apiVersion, apiProviderName);
            resourcePath = resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
            Resource resource;
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();
            } else {
                resource = registry.get(resourcePath);
            }
            resource.setContent(apiDefinitionJSON);
            resource.setMediaType("application/json");
            registry.put(resourcePath, resource);

            String[] visibleRoles = null;
            if (api.getVisibleRoles() != null) {
                visibleRoles = api.getVisibleRoles().split(",");
            }

            //Need to set anonymous if the visibility is public
            APIUtil.clearResourcePermissions(resourcePath, api.getId(), ((UserRegistry) registry).getTenantId());
            APIUtil.setResourcePermissions(apiProviderName, api.getVisibility(), visibleRoles, resourcePath);

        } catch (RegistryException e) {
            handleException("Error while adding Swagger Definition for " + apiName + '-' + apiVersion, e);
        }
    }

    /**
     * This method returns api definition json for given api
     *
     * @param apiIdentifier api identifier
     * @param registry      user registry
     * @return api definition json as json string
     * @throws APIManagementException
     */
    public static String getAPIDefinition(Identifier apiIdentifier, Registry registry)
            throws APIManagementException {
        String resourcePath = "";

        if (apiIdentifier instanceof APIIdentifier) {
            resourcePath = APIUtil
                    .getOpenAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                            apiIdentifier.getProviderName());
        } else if (apiIdentifier instanceof APIProductIdentifier) {
            resourcePath = APIUtil
                    .getAPIProductOpenAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                            apiIdentifier.getProviderName());
        }

        JSONParser parser = new JSONParser();
        String apiDocContent = null;
        try {
            if (registry.resourceExists(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME);
                apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                parser.parse(apiDocContent);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Resource " + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME + " not found at "
                            + resourcePath);
                }
            }
        } catch (RegistryException e) {
            handleException(
                    "Error while retrieving OpenAPI v2.0 or v3.0.0 Definition for " + apiIdentifier.getName() + '-'
                            + apiIdentifier.getVersion(), e);
        } catch (ParseException e) {
            handleException(
                    "Error while parsing OpenAPI v2.0 or v3.0.0 Definition for " + apiIdentifier.getName() + '-'
                            + apiIdentifier.getVersion() + " in " + resourcePath, e);
        }
        return apiDocContent;
    }

    /**
     * Sets the scopes to the URL template object using the given list of scopes
     *
     * @param template URL template
     * @param scopes   list of scopes
     * @return URL template after setting the scopes
     */
    public static URITemplate setScopesToTemplate(URITemplate template, List<String> scopes) {
        for (String scope : scopes) {
            Scope scopeObj = new Scope();
            scopeObj.setKey(scope);
            scopeObj.setName(scope);

            template.setScopes(scopeObj);
        }
        return template;
    }

    /**
     * generate endpoint information for OAS definition
     *
     * @param api          API
     * @param isProduction is production endpoints
     * @return JsonNode
     */
    public static JsonNode generateOASConfigForEndpoints(API api, boolean isProduction) {
        if (api.getEndpointConfig() == null || api.getEndpointConfig().trim().isEmpty()) {
            return null;
        }
        JSONObject endpointConfig = new JSONObject(api.getEndpointConfig());
        if (endpointConfig.has(APIConstants.IMPLEMENTATION_STATUS)) {
            // no need to populate if it is prototype API
            return null;
        }
        ObjectNode endpointResult = objectMapper.createObjectNode();
        String type = endpointConfig.getString(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE);
        if (APIConstants.ENDPOINT_TYPE_DEFAULT.equalsIgnoreCase(type)) {
            endpointResult.put(APIConstants.X_WSO2_ENDPOINT_TYPE, APIConstants.ENDPOINT_TYPE_DEFAULT);
            return endpointResult;
        } else if (APIConstants.ENDPOINT_TYPE_FAILOVER.equalsIgnoreCase(type)) {
            populateFailoverConfig(endpointResult, endpointConfig, isProduction);
        } else if (APIConstants.ENDPOINT_TYPE_LOADBALANCE.equalsIgnoreCase(type)) {
            populateLoadBalanceConfig(endpointResult, endpointConfig, isProduction);
        } else if (APIConstants.ENDPOINT_TYPE_HTTP.equalsIgnoreCase(type)) {
            setPrimaryConfig(endpointResult, endpointConfig, isProduction, APIConstants.ENDPOINT_TYPE_HTTP);
        } else if (APIConstants.ENDPOINT_TYPE_ADDRESS.equalsIgnoreCase(type)) {
            setPrimaryConfig(endpointResult, endpointConfig, isProduction, APIConstants.ENDPOINT_TYPE_ADDRESS);
        } else {
            return null;
        }
        return endpointResult;
    }

    /**
     * Set failover configuration
     *
     * @param endpointResult result object
     * @param endpointConfig endpoint configuration json string
     * @param isProd         endpoint type
     */
    private static void populateFailoverConfig(ObjectNode endpointResult, JSONObject endpointConfig, boolean isProd) {
        JSONArray endpointsURLs;
        JSONObject primaryEndpoints;
        if (isProd) {
            endpointsURLs = endpointConfig.getJSONArray(APIConstants.ENDPOINT_PRODUCTION_FAILOVERS);
            primaryEndpoints = endpointConfig.getJSONObject(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
        } else {
            endpointsURLs = endpointConfig.getJSONArray(APIConstants.ENDPOINT_SANDBOX_FAILOVERS);
            primaryEndpoints = endpointConfig.getJSONObject(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS);
        }

        ArrayNode endpointsArray = objectMapper.createArrayNode();
        if (endpointsURLs != null) {
            for (int i = 0; i < endpointsURLs.length(); i++) {
                JSONObject obj = endpointsURLs.getJSONObject(i);
                endpointsArray.add(obj.getString(APIConstants.ENDPOINT_URL));
            }
        }
        if (primaryEndpoints != null && primaryEndpoints.has(APIConstants.ENDPOINT_URL)) {
            endpointsArray.add(primaryEndpoints.getString(APIConstants.ENDPOINT_URL));
        }
        endpointResult.set(APIConstants.ENDPOINT_URLS, endpointsArray);
        endpointResult.put(APIConstants.X_WSO2_ENDPOINT_TYPE, APIConstants.ENDPOINT_TYPE_FAILOVER);
    }

    /**
     * Set load balance configuration
     *
     * @param endpointResult result object
     * @param endpointConfig endpoint configuration json string
     * @param isProd         endpoint type
     */
    private static void populateLoadBalanceConfig(ObjectNode endpointResult, JSONObject endpointConfig,
            boolean isProd) {
        JSONArray primaryProdEndpoints = new JSONArray();
        if (isProd && endpointConfig.has(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS) &&
                endpointConfig.get(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS) instanceof JSONArray) {
            primaryProdEndpoints = endpointConfig.getJSONArray(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
        } else if (endpointConfig.has(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS) &&
                endpointConfig.get(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS) instanceof JSONArray) {
            primaryProdEndpoints = endpointConfig.getJSONArray(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS);
        }

        ArrayNode endpointsArray = objectMapper.createArrayNode();
        if (primaryProdEndpoints != null) {
            for (int i = 0; i < primaryProdEndpoints.length(); i++) {
                JSONObject obj = primaryProdEndpoints.getJSONObject(i);
                endpointsArray.add(obj.getString(APIConstants.ENDPOINT_URL));
                if (obj.has(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE)) {
                }
            }
        }
        endpointResult.set(APIConstants.ENDPOINT_URLS, endpointsArray);
        endpointResult.put(APIConstants.X_WSO2_ENDPOINT_TYPE, APIConstants.ENDPOINT_TYPE_LOADBALANCE);
    }

    /**
     * Set baisc configuration
     *
     * @param endpointResult result object
     * @param endpointConfig endpoint configuration json string
     * @param isProd         endpoint type
     * @param type           endpoint type
     */
    private static void setPrimaryConfig(ObjectNode endpointResult, JSONObject endpointConfig, boolean isProd,
            String type) {
        JSONObject primaryEndpoints = new JSONObject();
        if (isProd && endpointConfig.has(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
            primaryEndpoints = endpointConfig.getJSONObject(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
        } else if (endpointConfig.has(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
            primaryEndpoints = endpointConfig.getJSONObject(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS);
        }
        if (primaryEndpoints != null && primaryEndpoints.has(APIConstants.ENDPOINT_URL)) {
            ArrayNode endpointsArray = objectMapper.createArrayNode();
            endpointsArray.add(primaryEndpoints.getString(APIConstants.ENDPOINT_URL));
            endpointResult.set(APIConstants.ENDPOINT_URLS, endpointsArray);
            endpointResult.put(APIConstants.X_WSO2_ENDPOINT_TYPE, type);
        }
    }

    /**
     * remove publisher/MG related extension from OAS
     *
     * @param extensions extensions
     */
    public static void removePublisherSpecificInfo(Map<String, Object> extensions) {
        if (extensions == null) {
            return;
        }
        if (extensions.containsKey(APIConstants.X_WSO2_AUTH_HEADER)) {
            extensions.remove(APIConstants.X_WSO2_AUTH_HEADER);
        }
        if (extensions.containsKey(APIConstants.X_THROTTLING_TIER)) {
            extensions.remove(APIConstants.X_THROTTLING_TIER);
        }
        if (extensions.containsKey(APIConstants.X_WSO2_CORS)) {
            extensions.remove(APIConstants.X_WSO2_CORS);
        }
        if (extensions.containsKey(APIConstants.X_WSO2_PRODUCTION_ENDPOINTS)) {
            extensions.remove(APIConstants.X_WSO2_PRODUCTION_ENDPOINTS);
        }
        if (extensions.containsKey(APIConstants.X_WSO2_SANDBOX_ENDPOINTS)) {
            extensions.remove(APIConstants.X_WSO2_SANDBOX_ENDPOINTS);
        }
        if (extensions.containsKey(APIConstants.X_WSO2_BASEPATH)) {
            extensions.remove(APIConstants.X_WSO2_BASEPATH);
        }
        if (extensions.containsKey(APIConstants.X_WSO2_TRANSPORTS)) {
            extensions.remove(APIConstants.X_WSO2_TRANSPORTS);
        }
    }
}
