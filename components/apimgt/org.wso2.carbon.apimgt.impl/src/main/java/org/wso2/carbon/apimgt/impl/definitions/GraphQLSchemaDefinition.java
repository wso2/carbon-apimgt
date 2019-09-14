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

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class GraphQLSchemaDefinition {

    protected Log log = LogFactory.getLog(getClass());

    /**
     * build schema with scopes and roles
     *
     * @param api api object
     * @return schemaDefinition
     */
    public String buildSchemaWithScopesAndRoles(API api) {
        Swagger swagger = null;
        Map<String, String> scopeRoleMap = new HashMap<>();
        Map<String, String> operationScopeMap = new HashMap<>();
        Map<String, String> operationAuthSchemeMap = new HashMap<>();
        Map<String, String> operationThrottlingMap = new HashMap<>();

        String operationScopeType;
        StringBuilder schemaDefinitionBuilder = new StringBuilder(api.getGraphQLSchema());
        StringBuilder operationScopeMappingBuilder = new StringBuilder();
        StringBuilder scopeRoleMappingBuilder = new StringBuilder();
        StringBuilder operationAuthSchemeMappingBuilder = new StringBuilder();
        StringBuilder operationThrottlingMappingBuilder = new StringBuilder();

        SwaggerParser parser = new SwaggerParser();
        String swaggerDef = api.getSwaggerDefinition();

        if (swaggerDef != null) {
            swagger = parser.parse(swaggerDef);
        }

        if (swagger != null) {
            Map<String, Object> vendorExtensions = swagger.getVendorExtensions();
            if (vendorExtensions != null) {
                LinkedHashMap swaggerWSO2Security = (LinkedHashMap) swagger.getVendorExtensions()
                        .get(APIConstants.SWAGGER_X_WSO2_SECURITY);
                if (swaggerWSO2Security != null) {
                    LinkedHashMap swaggerObjectAPIM = (LinkedHashMap) swaggerWSO2Security
                            .get(APIConstants.SWAGGER_OBJECT_NAME_APIM);
                    if (swaggerObjectAPIM != null) {
                        @SuppressWarnings("unchecked")
                        ArrayList<LinkedHashMap> scopes = (ArrayList<LinkedHashMap>) swaggerObjectAPIM
                                .get(APIConstants.SWAGGER_X_WSO2_SCOPES);
                        for (LinkedHashMap scope : scopes) {
                            for (URITemplate template : api.getUriTemplates()) {
                                String scopeInURITemplate = template.getScope() != null ?
                                        template.getScope().getName() : null;
                                operationThrottlingMap.put(template.getUriTemplate(), template.getThrottlingTier());
                                operationAuthSchemeMap.put(template.getUriTemplate(), template.getAuthType());
                                if (scopeInURITemplate != null && scopeInURITemplate.
                                        equals(scope.get(APIConstants.SWAGGER_SCOPE_KEY))) {
                                    operationScopeMap.put(template.getUriTemplate(), scopeInURITemplate);
                                    if (!scopeRoleMap.containsKey(scopeInURITemplate)) {
                                        scopeRoleMap.put(scopeInURITemplate,
                                                scope.get(APIConstants.SWAGGER_ROLES).toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (operationScopeMap.size() > 0) {
                String base64EncodedURLOperationKey;
                String base64EncodedURLScope;
                for (Map.Entry<String, String> entry : operationScopeMap.entrySet()) {
                    base64EncodedURLOperationKey = Base64.getUrlEncoder().withoutPadding().
                            encodeToString(entry.getKey().getBytes(Charset.defaultCharset()));
                    base64EncodedURLScope = Base64.getUrlEncoder().withoutPadding().
                            encodeToString(entry.getValue().getBytes(Charset.defaultCharset()));
                    operationScopeType = "type ScopeOperationMapping_" +
                            base64EncodedURLOperationKey + "{\n" + base64EncodedURLScope + ": String\n}\n";
                    operationScopeMappingBuilder.append(operationScopeType);
                }
                schemaDefinitionBuilder.append(operationScopeMappingBuilder.toString());
            }

            if (scopeRoleMap.size() > 0) {
                List<String> scopeRoles = new ArrayList<>();
                String[] roleList;
                String scopeType;
                String base64EncodedURLScopeKey;
                String scopeRoleMappingType;
                String base64EncodedURLRole;
                String roleField;

                for (Map.Entry<String, String> entry : scopeRoleMap.entrySet()) {
                    base64EncodedURLScopeKey = Base64.getUrlEncoder().withoutPadding().
                            encodeToString(entry.getKey().getBytes(Charset.defaultCharset()));
                    scopeType = "type ScopeRoleMapping_" + base64EncodedURLScopeKey + "{\n";
                    StringBuilder scopeRoleBuilder = new StringBuilder(scopeType);
                    roleList = entry.getValue().split(",");

                    for (String role : roleList) {
                        if (!role.equals("") && !scopeRoles.contains(role)) {
                            base64EncodedURLRole = Base64.getUrlEncoder().withoutPadding().
                                    encodeToString(role.getBytes(Charset.defaultCharset()));
                            roleField = base64EncodedURLRole + ": String\n";
                            scopeRoleBuilder.append(roleField);
                            scopeRoles.add(role);
                        }
                    }

                    if (scopeRoles.size() > 0 && !StringUtils.isEmpty(scopeRoleBuilder.toString())) {
                        scopeRoleMappingType = scopeRoleBuilder.toString() + "}\n";
                        scopeRoleMappingBuilder.append(scopeRoleMappingType);
                    }
                }
                schemaDefinitionBuilder.append(scopeRoleMappingBuilder.toString());
            }
            if (operationThrottlingMap.size() > 0) {
                String operationThrottlingType;
                for (Map.Entry<String, String> entry : operationThrottlingMap.entrySet()) {
                    operationThrottlingType = "type OperationThrottlingMapping_" +
                            entry.getKey() + "{\n" + entry.getValue() + ": String\n}\n";
                    operationThrottlingMappingBuilder.append(operationThrottlingType);
                }
                schemaDefinitionBuilder.append(operationThrottlingMappingBuilder.toString());
            }

            if (operationAuthSchemeMap.size() > 0) {
                String operationAuthSchemeType;
                String isSecurityEnabled;
                for (Map.Entry<String, String> entry : operationAuthSchemeMap.entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(APIConstants.AUTH_NO_AUTHENTICATION)) {
                        isSecurityEnabled = "securityEnabled";
                    } else {
                        isSecurityEnabled = "securityDisabled";
                    }
                    operationAuthSchemeType = "type OperationAuthSchemeMapping_" +
                            entry.getKey() + "{\n" + isSecurityEnabled + ": String\n}\n";
                    operationAuthSchemeMappingBuilder.append(operationAuthSchemeType);
                }
                schemaDefinitionBuilder.append(operationAuthSchemeMappingBuilder.toString());
            }
        }
        return schemaDefinitionBuilder.toString();
    }

    /** This method add paths from URI Template For GraphQLAPI
     *
     * @param pathsObject
     */
    protected void addQueryParams(JSONObject pathsObject) {
        String parameter = "";
        String type = "";
        String inValue = "";
        String description = "";
        Map.Entry resourceObject;
        JSONObject pathsObjectValues;
        JSONObject pathItemObject = new JSONObject();

        for (Object pathObject : pathsObject.entrySet()) {
            Map.Entry resourcePath = (Map.Entry) pathObject;
            pathsObjectValues = (JSONObject) resourcePath.getValue();
            for (Object resource : pathsObjectValues.entrySet()) {
                JSONArray parametersObj = new JSONArray();
                JSONObject queryParamObj = new JSONObject();
                resourceObject = (Map.Entry) resource;
                JSONObject resourceParams = (JSONObject) resourceObject.getValue();

                if (resourceObject.getKey() != null) {
                    if (resourceObject.getKey().toString().equals("get")) {
                        parameter = "query";
                        inValue = "query";
                        type = "string";
                        description = "Query to be passed to graphQL API";
                    } else if (resourceObject.getKey().toString().equals("post")) {
                        JSONObject schema = new JSONObject();
                        JSONObject payload = new JSONObject();
                        JSONObject typeOfPayload = new JSONObject();
                        schema.put("type", "object");
                        typeOfPayload.put("type", "string");
                        payload.put("payload", typeOfPayload);
                        schema.put("properties", payload);
                        queryParamObj.put("schema", schema);
                        parameter = "payload";
                        inValue = "body";
                        description = "Query or mutation to be passed to graphQL API";
                    }
                }

                queryParamObj.put("name", parameter);
                queryParamObj.put("in", inValue);
                queryParamObj.put("required", true);
                queryParamObj.put("type", type);
                queryParamObj.put("description", description);

                parametersObj.add(queryParamObj);
                resourceParams.put("parameters", parametersObj);
                pathItemObject.put(resourceObject.getKey().toString(), resourceParams);
                pathsObject.put(resourcePath.getKey().toString(), pathItemObject);
            }
        }
    }


    /**
     * This method saves schema definition of GraphQL APIs in the registry
     *
     * @param api               API to be saved
     * @param schemaDefinition  Graphql API definition as String
     * @param registry          user registry
     * @throws APIManagementException
     */
    public void saveGraphQLSchemaDefinition(API api, String schemaDefinition, Registry registry)
            throws APIManagementException {
        String apiName = api.getId().getApiName();
        String apiVersion = api.getId().getVersion();
        String apiProviderName = api.getId().getProviderName();
        String resourcePath = APIUtil.getGraphqlDefinitionFilePath(apiName, apiVersion, apiProviderName);
        try {
            String saveResourcePath = resourcePath + apiProviderName + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR +
                    apiName + apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
            Resource resource;
            if (!registry.resourceExists(saveResourcePath)) {
                resource = registry.newResource();
            } else {
                resource = registry.get(saveResourcePath);
            }

            resource.setContent(schemaDefinition);
            resource.setMediaType(String.valueOf(ContentType.TEXT_PLAIN));
            registry.put(saveResourcePath, resource);
            if (log.isDebugEnabled()) {
                log.debug("Successfully imported the schema: " + schemaDefinition );
            }

            String[] visibleRoles = null;
            if (api.getVisibleRoles() != null) {
                visibleRoles = api.getVisibleRoles().split(",");
            }

            //Need to set anonymous if the visibility is public
            APIUtil.clearResourcePermissions(resourcePath, api.getId(), ((UserRegistry) registry).getTenantId());
            APIUtil.setResourcePermissions(apiProviderName, api.getVisibility(), visibleRoles, resourcePath);

        } catch (RegistryException e) {
            String errorMessage = "Error while adding Graphql Definition for " + apiName + '-' + apiVersion;
            log.error(errorMessage, e);
            handleException(errorMessage, e);
        }
    }

    /**
     * Returns the graphQL content in registry specified by the wsdl name
     *
     * @param apiId Api Identifier
     * @return graphQL content matching name if exist else null
     */
    public String getGraphqlSchemaDefinition(APIIdentifier apiId, Registry registry) throws APIManagementException {
        String apiName = apiId.getApiName();
        String apiVersion = apiId.getVersion();
        String apiProviderName = apiId.getProviderName();
        String resourcePath = APIUtil.getGraphqlDefinitionFilePath(apiName, apiVersion, apiProviderName);

        String schemaDoc = null;
        String schemaName = apiId.getProviderName() + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR +
                apiId.getApiName() + apiId.getVersion() + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        String schemaResourePath = resourcePath + schemaName;
        try {
            if (registry.resourceExists(schemaResourePath)) {
                Resource schemaResource = registry.get(schemaResourePath);
                schemaDoc = IOUtils.toString(schemaResource.getContentStream(),
                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
            }
        } catch (RegistryException e) {
            String msg = "Error while getting schema file from the registry " + schemaResourePath;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (IOException e) {
            String error = "Error occurred while getting the content of schema: " + schemaName;
            log.error(error);
            throw new APIManagementException(error, e);
        }
        return schemaDoc;
    }
}

