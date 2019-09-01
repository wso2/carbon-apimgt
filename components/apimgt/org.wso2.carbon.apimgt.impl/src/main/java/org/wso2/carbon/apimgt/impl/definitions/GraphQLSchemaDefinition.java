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
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GraphQLSchemaDefinition {

    /**
     * build schema with scopes and roles
     *
     * @param api api object
     * @return schemaDefinition
     */
    public String buildSchemaWithScopesAndRoles(API api) {
        Swagger swagger = null;
        Map<String, String> scopeRoleMap = new HashMap<>();
        Map<String, String> scopeOperationMap = new HashMap<>();
        String operationScopeType;
        StringBuilder schemaDefinitionBuilder = new StringBuilder(api.getGraphQLSchema());
        StringBuilder operationScopeMappingBuilder = new StringBuilder();
        StringBuilder scopeRoleMappingBuilder = new StringBuilder();
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
                                if (scopeInURITemplate != null && scopeInURITemplate.
                                        equals(scope.get(APIConstants.SWAGGER_SCOPE_KEY))) {
                                    scopeOperationMap.put(template.getUriTemplate(), scopeInURITemplate);
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

            if (scopeOperationMap.size() > 0) {
                String base64EncodedURLOperationKey;
                String base64EncodedURLScope;
                for (Map.Entry<String, String> entry : scopeOperationMap.entrySet()) {
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

            if (scopeOperationMap.size() > 0) {
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
                        if (!scopeRoles.contains(role)) {
                            base64EncodedURLRole = Base64.getUrlEncoder().withoutPadding().
                                    encodeToString(role.getBytes(Charset.defaultCharset()));
                            roleField = base64EncodedURLRole + ": String\n";
                            scopeRoleBuilder.append(roleField);
                        }
                        scopeRoles.add(role);
                    }

                    if (!StringUtils.isEmpty(scopeRoleBuilder.toString())) {
                        scopeRoleMappingType = scopeRoleBuilder.toString() + "}\n";
                        scopeRoleMappingBuilder.append(scopeRoleMappingType);
                    }
                }
                schemaDefinitionBuilder.append(scopeRoleMappingBuilder.toString());
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
}
