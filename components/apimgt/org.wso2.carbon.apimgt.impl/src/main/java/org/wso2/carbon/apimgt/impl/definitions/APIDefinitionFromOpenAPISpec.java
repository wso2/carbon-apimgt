/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class APIDefinitionFromOpenAPISpec extends APIDefinition {

    private static final Log log = LogFactory.getLog(APIDefinitionFromOpenAPISpec.class);

    /**
     * This method returns URI templates according to the given swagger file
     *
     * @param api                 API
     * @param resourceConfigsJSON swaggerJSON
     * @return URI Templates
     * @throws APIManagementException
     */
    @Override
    public Set<URITemplate> getURITemplates(API api, String resourceConfigsJSON) throws APIManagementException {
        JSONParser parser = new JSONParser();
        JSONObject swagger;
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        Set<Scope> scopes = getScopes(resourceConfigsJSON);
        try {
            swagger = (JSONObject) parser.parse(resourceConfigsJSON);
            if (swagger.get("paths") != null) {
                JSONObject paths = (JSONObject) swagger.get("paths");
                for (Object o : paths.keySet()) {
                    String uriTempVal = (String) o;
                    //if url template is a custom attribute "^x-" ignore.
                    if (uriTempVal.startsWith("x-") || uriTempVal.startsWith("X-")) {
                        continue;
                    }
                    JSONObject path = (JSONObject) paths.get(uriTempVal);
                    // Following code check is done to handle $ref objects supported by swagger spec
                    // See field types supported by "Path Item Object" in swagger spec.
                    if (path.containsKey("$ref")) {
                        log.info("Reference " + uriTempVal + " path object was ignored when generating URL template " +
                                "for api \"" + api.getId().getApiName() + '\"');
                        continue;
                    }

                    boolean isHttpVerbDefined = false;

                    for (Object o1 : path.keySet()) {
                        String httpVerb = (String) o1;

                        if (APIConstants.SWAGGER_SUMMARY.equals(httpVerb.toLowerCase())
                                || APIConstants.SWAGGER_DESCRIPTION.equals(httpVerb.toLowerCase())
                                || APIConstants.SWAGGER_SERVERS.equals(httpVerb.toLowerCase())
                                || APIConstants.PARAMETERS.equals(httpVerb.toLowerCase())
                                || httpVerb.startsWith("x-")
                                || httpVerb.startsWith("X-")) {
                            // openapi 3.x allow 'summary', 'description' and extensions in PathItem Object.
                            // which we are not interested at this point
                            continue;
                        }
                        //Only continue for supported operations
                        else if (APIConstants.SUPPORTED_METHODS.contains(httpVerb.toLowerCase()) ||
                                APIConstants.GRAPHQL_SUPPORTED_METHOD_LIST.contains(httpVerb.toUpperCase())) {
                            isHttpVerbDefined = true;
                            JSONObject operation = (JSONObject) path.get(httpVerb);
                            URITemplate template = new URITemplate();
                            String scopeName = (String) operation.get(APIConstants.SWAGGER_X_SCOPE);
                            Scope scope = APIUtil.findScopeByKey(scopes, scopeName);
                            if (scopeName != null && scope == null) {
                                throw new APIManagementException("Scope '" + scopeName + "' not found.");
                            }
                            String authType = (String) operation.get(APIConstants.SWAGGER_X_AUTH_TYPE);
                            if ("Application & Application User".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                            } else if ("Application User".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN;
                            } else if ("None".equals(authType)) {
                                authType = APIConstants.AUTH_NO_AUTHENTICATION;
                            } else if ("Application".equals(authType)) {
                                authType = APIConstants.AUTH_APPLICATION_LEVEL_TOKEN;
                            } else {
                                authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                            }
                            template.setThrottlingTier((String) operation.get(APIConstants.SWAGGER_X_THROTTLING_TIER));
                            template.setThrottlingTiers((String) operation.get(APIConstants.SWAGGER_X_THROTTLING_TIER));
                            template.setMediationScript((String) operation.get(APIConstants
                                    .SWAGGER_X_MEDIATION_SCRIPT));
                            template.setMediationScripts(httpVerb.toUpperCase(), (String) operation.get(
                                    APIConstants.SWAGGER_X_MEDIATION_SCRIPT));
                            template.setUriTemplate(uriTempVal);
                            template.setHTTPVerb(httpVerb.toUpperCase());
                            template.setHttpVerbs(httpVerb.toUpperCase());
                            template.setAuthType(authType);
                            template.setAuthTypes(authType);
                            if(scope != null) {
                                template.setScope(scope);
                                template.setScopes(scope);
                            }
                            uriTemplates.add(template);
                        } else {
                            handleException("The HTTP method '" + httpVerb + "' provided for resource '" + uriTempVal
                                    + "' is invalid");
                        }
                    }

                    if (!isHttpVerbDefined) {
                        handleException("Resource '" + uriTempVal + "' has global parameters without " +
                                "HTTP methods");
                    }
                }
            }
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
        }
        return uriTemplates;
    }

    /**
     * This method returns the oauth scopes according to the given swagger
     *
     * @param resourceConfigsJSON resource json
     * @return scope set
     * @throws APIManagementException
     */
    @Override
    public Set<Scope> getScopes(String resourceConfigsJSON) throws APIManagementException {
        Set<Scope> scopeList = new LinkedHashSet<Scope>();
        JSONObject swaggerObject;
        JSONParser parser = new JSONParser();
        try {
            swaggerObject = (JSONObject) parser.parse(resourceConfigsJSON);

            //Check whether security definitions are defined or not
            if (swaggerObject.get(APIConstants.SWAGGER_X_WSO2_SECURITY) != null) {
                JSONObject securityDefinitionsObjects = (JSONObject) swaggerObject.get(APIConstants.SWAGGER_X_WSO2_SECURITY);

                for (JSONObject securityDefinition : (Iterable<JSONObject>) securityDefinitionsObjects.values()) {
                    //Read scopes from custom wso2 scopes

                    if (securityDefinition.get(APIConstants.SWAGGER_X_WSO2_SCOPES) != null) {
                        JSONArray oauthScope = (JSONArray) securityDefinition.get(APIConstants.SWAGGER_X_WSO2_SCOPES);
                        for (Object anOauthScope : oauthScope) {
                            Scope scope = new Scope();
                            JSONObject scopeObj = (JSONObject) anOauthScope;
                            scope.setKey((String) scopeObj.get(APIConstants.SWAGGER_SCOPE_KEY));
                            scope.setName((String) scopeObj.get(APIConstants.SWAGGER_NAME));
                            scope.setDescription((String) scopeObj.get(APIConstants.SWAGGER_DESCRIPTION));
                            scope.setRoles(scopeObj.get(APIConstants.SWAGGER_ROLES).toString());

                            scopeList.add(scope);
                        }
                    }
                }
            }
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
        }
        return scopeList;
    }

    /**
     * This method saves api definition json in the registry
     *
     * @param api               API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     * @param registry          user registry
     * @throws APIManagementException
     */
    @Override
    public void saveAPIDefinition(API api, String apiDefinitionJSON, Registry registry) throws APIManagementException {
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
    @Override
    public String getAPIDefinition(APIIdentifier apiIdentifier, Registry registry) throws APIManagementException {
        String resourcePath = APIUtil.getOpenAPIDefinitionFilePath(apiIdentifier.getApiName(),
                apiIdentifier.getVersion(), apiIdentifier.getProviderName());

        JSONParser parser = new JSONParser();
        String apiDocContent = null;
        try {
            if (registry.resourceExists(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME);
                apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                parser.parse(apiDocContent);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Resource " + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME + " not found at " + resourcePath);
                }
            }
        } catch (RegistryException e) {
            handleException(
                    "Error while retrieving OpenAPI v2.0 or v3.0.0 Definition for " + apiIdentifier.getApiName() + '-'
                            + apiIdentifier.getVersion(), e);
        } catch (ParseException e) {
            handleException(
                    "Error while parsing OpenAPI v2.0 or v3.0.0 Definition for " + apiIdentifier.getApiName() + '-'
                            + apiIdentifier.getVersion() + " in " + resourcePath, e);
        }
        return apiDocContent;
    }

    /**
     * This method generates swagger 2.0 definition to the given api
     *
     * @param api api
     * @return swagger v2.0 doc as string
     * @throws APIManagementException
     */
    @Override
    @SuppressWarnings("unchecked")
    public String generateAPIDefinition(API api) throws APIManagementException {
        APIIdentifier identifier = api.getId();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        Environment environment = (Environment) config.getApiGatewayEnvironments().values().toArray()[0];
        String endpoints = environment.getApiGatewayEndpoint();
        String[] endpointsSet = endpoints.split(",");
        Set<URITemplate> uriTemplates = api.getUriTemplates();

        if (endpointsSet.length < 1) {
            throw new APIManagementException(
                    "Error in creating JSON representation of the API" + identifier.getApiName());
        }

        JSONObject swaggerObject = new JSONObject();

        //Create info object
        JSONObject infoObject = new JSONObject();
        infoObject.put(APIConstants.SWAGGER_TITLE, identifier.getApiName());
        if (api.getDescription() != null) {
            infoObject.put(APIConstants.SWAGGER_DESCRIPTION, api.getDescription());
        }

        //Create contact object and map business owner info
        JSONObject contactObject = new JSONObject();
        if (api.getBusinessOwner() != null) {
            contactObject.put(APIConstants.SWAGGER_NAME, api.getBusinessOwner());
        }
        if (api.getBusinessOwnerEmail() != null) {
            contactObject.put(APIConstants.SWAGGER_EMAIL, api.getBusinessOwnerEmail());
        }
        if (api.getBusinessOwner() != null || api.getBusinessOwnerEmail() != null) {
            //put contact object to info object
            infoObject.put(APIConstants.SWAGGER_CONTACT, contactObject);
        }

        //Create licence object # no need for this since this is not mandatory
        //JSONObject licenceObject = new JSONObject();
        //infoObject.put("license", licenceObject);

        infoObject.put(APIConstants.SWAGGER_VER, identifier.getVersion());

        //add info object to swaggerObject
        swaggerObject.put(APIConstants.SWAGGER_INFO, infoObject);

        JSONObject pathsObject = new JSONObject();

        if (APIConstants.GRAPHQL_API.equals(api.getType())) {
            List<String> verbList = new ArrayList<>();
            verbList.add("GET");
            verbList.add("POST");
            URITemplate swaggerTemplate = new URITemplate();
            for (String verb : verbList) {
                swaggerTemplate.setAuthType("Any");
                swaggerTemplate.setHTTPVerb(verb);
                swaggerTemplate.setHttpVerbs(verb);
                swaggerTemplate.setUriTemplate("/*");
                addOrUpdatePathsFromURITemplate(pathsObject, swaggerTemplate);
            }
            GraphQLSchemaDefinition schemaDefinition = new GraphQLSchemaDefinition();
            schemaDefinition.addQueryParams(pathsObject);
        } else {
            for (URITemplate uriTemplate : uriTemplates) {
                addOrUpdatePathsFromURITemplate(pathsObject, uriTemplate);
            }
        }

        swaggerObject.put(APIConstants.SWAGGER_PATHS, pathsObject);
        swaggerObject.put(APIConstants.SWAGGER, APIConstants.SWAGGER_V2);
        populateSwaggerScopeInfo(swaggerObject, api.getScopes());
        return swaggerObject.toJSONString();
    }

    @Override
    public String generateAPIDefinition(API api, String swagger, boolean syncOperations)
            throws APIManagementException {
        JSONParser parser = new JSONParser();
        try {
            JSONObject swaggerObj = (JSONObject) parser.parse(swagger);

            if (!(APIConstants.GRAPHQL_API).equals(api.getType())) {
                //Generates below model using the API's URI template
                // path -> [verb1 -> template1, verb2 -> template2, ..]
                Map<String, Map<String, URITemplate>> uriTemplateMap = getURITemplateMap(api);

                if (syncOperations) {
                    syncAPIDefinitionWithURITemplates(swaggerObj, uriTemplateMap);
                } else {
                    setDefaultManagedInfoToAPIDefinition(api, swaggerObj);
                }
            }
            // add scope in the API object to swagger
            populateSwaggerScopeInfo(swaggerObj, api.getScopes());
            return swaggerObj.toJSONString();
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing swagger definition", e);
        }
    }

    /**
     * gets the createdTime and updatedTime for the swagger definition
     *
     * @param apiIdentifier
     * @param registry
     * @return
     * @throws APIManagementException
     */
    @Override
    public Map<String, String> getAPIOpenAPIDefinitionTimeStamps(APIIdentifier apiIdentifier, Registry registry)
            throws APIManagementException {
        Map<String, String> timeStampMap = new HashMap<String, String>();
        String resourcePath = APIUtil.getOpenAPIDefinitionFilePath(apiIdentifier.getApiName(),
                apiIdentifier.getVersion(), apiIdentifier.getProviderName());
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
                    log.debug("Resource " + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME + " not found at " + resourcePath);
                }
            }
        } catch (RegistryException e) {
            handleException("Error while retrieving OpenAPI v2.0 or v3.0.0 updated time for " + apiIdentifier.getApiName
                    () + '-' +
                    apiIdentifier.getVersion(), e);
        }
        return timeStampMap;
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent) {
        return null;
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinitionByURL(String url, boolean returnJsonContent) {
        return null;
    }

    /**
     * Called using the jaggery api. Checks if the swagger contains valid api scopes.
     *
     * @param swagger Swagger definition
     * @return true if the scope definition is valid
     * @throws APIManagementException
     */
    public Boolean validateScopesFromSwagger(String swagger) throws APIManagementException {

        try {
            Set<Scope> scopes = getScopes(swagger);
            JSONParser parser = new JSONParser();
            JSONObject swaggerJson;
            swaggerJson = (JSONObject) parser.parse(swagger);
            if (swaggerJson.get("paths") != null) {
                JSONObject paths = (JSONObject) swaggerJson.get("paths");
                for (Object uriTempKey : paths.keySet()) {
                    String uriTemp = (String) uriTempKey;
                    //if url template is a custom attribute "^x-" ignore.
                    if (uriTemp.startsWith("x-") || uriTemp.startsWith("X-")) {
                        continue;
                    }
                    JSONObject path = (JSONObject) paths.get(uriTemp);
                    // Following code check is done to handle $ref objects supported by swagger spec
                    // See field types supported by "Path Item Object" in swagger spec.
                    if (path.containsKey("$ref")) {
                        continue;
                    }

                    for (Object httpVerbKey : path.keySet()) {
                        String httpVerb = (String) httpVerbKey;
                        JSONObject operation = (JSONObject) path.get(httpVerb);
                        String operationScope = (String) operation.get(APIConstants.SWAGGER_X_SCOPE);

                        Scope scope = APIUtil.findScopeByKey(scopes, operationScope);

                        if (scope == null && operationScope != null) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (APIManagementException e) {
            handleException("Error when validating scopes", e);
            return false;
        } catch (ParseException e) {
            handleException("Error when validating scopes", e);
            return false;
        }
    }

    /**
     * Add a new path based on the provided URI template to swagger if it does not exists. If it exists,
     * adds the respective operation to the existing path
     *
     * @param pathsObject swagger paths json object
     * @param uriTemplate URI template
     */
    private void addOrUpdatePathsFromURITemplate(JSONObject pathsObject, URITemplate uriTemplate) {
        String pathName = uriTemplate.getUriTemplate();
        if (pathsObject.get(pathName) == null) {
            pathsObject.put(pathName, new JSONObject());
        }

        JSONObject pathItemObject = (JSONObject)pathsObject.get(pathName);
        String httpVerb = uriTemplate.getHTTPVerb();
        JSONObject operationObject = createOperationFromTemplate(uriTemplate);

        pathItemObject.put(httpVerb.toLowerCase(), operationObject);
        pathsObject.put(pathName, pathItemObject);
    }

    /**
     * Creates a new operation object using the URI template object
     *
     * @param uriTemplate URI template
     * @return a new operation object using the URI template object
     */
    private JSONObject createOperationFromTemplate(URITemplate uriTemplate) {
        JSONObject operationObject = new JSONObject();
        String pathName = uriTemplate.getUriTemplate();
        updateOperationManagedInfo(uriTemplate, operationObject);

        JSONObject responseObject = new JSONObject();
        //add default response
        JSONObject status200 = new JSONObject();
        status200.put(APIConstants.SWAGGER_DESCRIPTION, "OK");
        responseObject.put(APIConstants.SWAGGER_RESPONSE_200, status200);
        operationObject.put(APIConstants.SWAGGER_RESPONSES, responseObject);

        List<String> pathParams = getPathParamNames(pathName);
        if (pathParams.size() > 0) {
            JSONArray parametersObj = new JSONArray();
            for (String pathParam : pathParams) {
                JSONObject pathParamObj = new JSONObject();
                pathParamObj.put("name", pathParam);
                pathParamObj.put("required", true);
                pathParamObj.put("in", "path");
                pathParamObj.put("type", "string");

                parametersObj.add(pathParamObj);
            }

            operationObject.put("parameters", parametersObj);
        }
        return operationObject;
    }

    /**
     *  Updates managed info of a provided operation such as auth type and throttling
     *
     * @param uriTemplate URI template
     * @param operationObject swagger operation json object
     */
    private void updateOperationManagedInfo(URITemplate uriTemplate, JSONObject operationObject) {
        String authType = uriTemplate.getAuthType();
        if (APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN.equals(authType)) {
            authType = "Application & Application User";
        }
        if (APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authType)) {
            authType = "Application User";
        }
        if (APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authType)) {
            authType = "Application";
        }
        operationObject.put(APIConstants.SWAGGER_X_AUTH_TYPE, authType);
        operationObject.put(APIConstants.SWAGGER_X_THROTTLING_TIER, uriTemplate.getThrottlingTier());
        if (uriTemplate.getScope() != null) {
            operationObject.put(APIConstants.SWAGGER_X_SCOPE, uriTemplate.getScope().getKey());
        } else {
            if (operationObject.containsKey(APIConstants.SWAGGER_X_SCOPE)) {
                operationObject.remove(APIConstants.SWAGGER_X_SCOPE);
            }
        }
    }

    /**
     * Set default managed info to API Definitions path objects
     *
     * @param api API object
     * @param swaggerObj Swagger (API Definition) object
     */
    private void setDefaultManagedInfoToAPIDefinition(API api, JSONObject swaggerObj) {
        if ("oauth2".contains(api.getApiSecurity())) {
            JSONObject pathsJsonObj = (JSONObject) swaggerObj.get(APIConstants.SWAGGER_PATHS);
            for (Object pathObj : pathsJsonObj.entrySet()) {
                Map.Entry pathEntry = (Map.Entry) pathObj;
                JSONObject pathJsonObj = (JSONObject) pathEntry.getValue();
                for (Object operationObj : pathJsonObj.entrySet()) {
                    Map.Entry operationEntry = (Map.Entry) operationObj;
                    String key = (String)operationEntry.getKey();
                    if (APIConstants.SUPPORTED_METHODS.contains(key)) {
                        JSONObject operationJsonObj = (JSONObject) operationEntry.getValue();
                        setOperationDefaultManagedInfo(api, operationJsonObj);
                    }
                }
            }
        }
    }

    /**
     * Sync API Definition with API's operations (URI Templates)
     *
     * @param swaggerObj API Definition object
     * @param uriTemplateMap URL Templates
     */
    private void syncAPIDefinitionWithURITemplates(JSONObject swaggerObj, Map<String, Map<String, URITemplate>>
            uriTemplateMap) {
        JSONObject pathsJsonObj = (JSONObject) swaggerObj.get(APIConstants.SWAGGER_PATHS);
        Iterator pathEntriesIterator = pathsJsonObj.entrySet().iterator();
        while (pathEntriesIterator.hasNext()) {
            Object pathObj = pathEntriesIterator.next();
            Map.Entry pathEntry = (Map.Entry) pathObj;
            String pathName = (String) pathEntry.getKey();
            JSONObject pathJsonObj = (JSONObject) pathEntry.getValue();

            Map<String, URITemplate> uriTemplatesForPath = uriTemplateMap.get(pathName);
            if (uriTemplatesForPath == null) {
                //remove paths that are not in URI Templates
                pathEntriesIterator.remove();
            } else {
                Iterator operationEntriesIterator = pathJsonObj.entrySet().iterator();
                while (operationEntriesIterator.hasNext()) {
                    Object operationObj = operationEntriesIterator.next();
                    Map.Entry operationEntry = (Map.Entry) operationObj;
                    String verb = (String) operationEntry.getKey();
                    if (APIConstants.SUPPORTED_METHODS.contains(verb)) {
                        JSONObject operationJsonObj = (JSONObject) operationEntry.getValue();

                        URITemplate template = uriTemplatesForPath.get(verb.toUpperCase());
                        if (template == null) {
                            // if particular operation is not available in URI templates, then remove it from swagger
                            operationEntriesIterator.remove();
                        } else {
                            // if operation is available in URI templates, update swagger operation
                            // with auth type, scope etc
                            updateOperationManagedInfo(template, operationJsonObj);
                        }
                    }
                }

                // if there are any verbs (operations) exists in uri template not defined in current path item
                // (pathJsonObj) in swagger then add them
                for (Map.Entry<String, URITemplate> uriTemplatesForPathEntry : uriTemplatesForPath.entrySet()) {
                    String verb = uriTemplatesForPathEntry.getKey();
                    URITemplate uriTemplate = uriTemplatesForPathEntry.getValue();
                    JSONObject operationJsonObj = (JSONObject) pathJsonObj.get(verb.toLowerCase());
                    if (operationJsonObj == null) {
                        operationJsonObj = createOperationFromTemplate(uriTemplate);
                        pathJsonObj.put(verb.toLowerCase(), operationJsonObj);
                    }
                }
            }
        }

        // add to swagger if there are any new path templates
        for (Map.Entry<String, Map<String, URITemplate>> uriTemplateMapEntry : uriTemplateMap.entrySet()) {
            String path = uriTemplateMapEntry.getKey();
            Map<String, URITemplate> verbMap = uriTemplateMapEntry.getValue();
            if (pathsJsonObj.get(path) == null) {
                for (Map.Entry<String, URITemplate> verbMapEntry : verbMap.entrySet()) {
                    URITemplate uriTemplate = verbMapEntry.getValue();
                    addOrUpdatePathsFromURITemplate(pathsJsonObj, uriTemplate);
                }
            }
        }
    }

    /**
     *  Updates managed info of a provided operation such as auth type and throttling with default values
     *
     * @param api API object
     * @param operationObject swagger operation json object
     */
    private void setOperationDefaultManagedInfo(API api, JSONObject operationObject) {
        if (operationObject.get(APIConstants.SWAGGER_X_AUTH_TYPE) == null) {
            operationObject.put(APIConstants.SWAGGER_X_AUTH_TYPE, "Application & Application User");
        }

        if (operationObject.get(APIConstants.SWAGGER_X_THROTTLING_TIER) == null) {
            operationObject.put(APIConstants.SWAGGER_X_THROTTLING_TIER, api.getApiLevelPolicy() == null ?
                    APIConstants.UNLIMITED_TIER : api.getApiLevelPolicy());
        }
    }

    /**
     * Populate scope details in swagger
     *
     * @param swaggerObject existing swagger json object
     * @param scopes        scopes information to include
     * @return swagger object with updated scopes
     */
    private JSONObject populateSwaggerScopeInfo(JSONObject swaggerObject, Set<Scope> scopes) {

        JSONObject securityDefinitionObject = new JSONObject();
        JSONObject scopesObject = new JSONObject();
        JSONArray xWso2ScopesArray = new JSONArray();
        JSONObject xWso2ScopesObject;
        JSONObject xScopesArrayObj = new JSONObject();
        JSONObject securityDefinitionJsonObject = new JSONObject();
        JSONObject securityDefinitionAttr = new JSONObject();

        if (scopes != null) {
            for (Scope scope : scopes) {
                xWso2ScopesObject = new JSONObject();
                xWso2ScopesObject.put(APIConstants.SWAGGER_SCOPE_KEY, scope.getKey());
                xWso2ScopesObject.put(APIConstants.SWAGGER_NAME, scope.getName());
                xWso2ScopesObject.put(APIConstants.SWAGGER_ROLES, scope.getRoles());
                xWso2ScopesObject.put(APIConstants.SWAGGER_DESCRIPTION, scope.getDescription());
                xWso2ScopesArray.add(xWso2ScopesObject);
                xScopesArrayObj.put(scope.getKey(), scope.getDescription());
            }
        }

        securityDefinitionJsonObject.put(APIConstants.SWAGGER_SECURITY_TYPE, APIConstants.SWAGGER_SECURITY_OAUTH2);
        securityDefinitionJsonObject.put(APIConstants.SWAGGER_SECURITY_OAUTH2_TOKEN_URL, "https://test.com");
        securityDefinitionJsonObject
                .put(APIConstants.SWAGGER_SECURITY_OAUTH2_FLOW, APIConstants.SWAGGER_SECURITY_OAUTH2_PASSWORD);
        if (!xScopesArrayObj.isEmpty()) {
            securityDefinitionJsonObject.put(APIConstants.SWAGGER_SCOPES, xScopesArrayObj);
        }
        securityDefinitionAttr.put(APIConstants.SWAGGER_APIM_DEFAULT_SECURITY, securityDefinitionJsonObject);

        scopesObject.put(APIConstants.SWAGGER_X_WSO2_SCOPES, xWso2ScopesArray);
        securityDefinitionObject.put(APIConstants.SWAGGER_OBJECT_NAME_APIM, scopesObject);

        swaggerObject.put(APIConstants.SWAGGER_X_WSO2_SECURITY, securityDefinitionObject);
        swaggerObject.put(APIConstants.SWAGGER_SECURITY_DEFINITIONS, securityDefinitionAttr);
        return swaggerObject;
    }
}
