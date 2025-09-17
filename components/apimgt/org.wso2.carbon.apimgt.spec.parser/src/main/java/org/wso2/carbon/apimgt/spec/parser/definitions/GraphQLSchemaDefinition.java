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

package org.wso2.carbon.apimgt.spec.parser.definitions;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.OperationTypeDefinition;
import graphql.language.SchemaDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.CustomComplexityDetails;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlSchemaType;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GraphQLSchemaDefinition {

    protected Log log = LogFactory.getLog(getClass());

    /**
     * Extract GraphQL Operations from given schema.
     *
     * @param typeRegistry graphQL Schema Type Registry
     * @param type         operation type string
     * @return the arrayList of APIOperationsDTO
     */
    public List<URITemplate> extractGraphQLOperationList(TypeDefinitionRegistry typeRegistry, String type) {
        if (log.isDebugEnabled()) {
            log.debug("Extracting GraphQL operations for type: " + (type != null ? type : "all types"));
        }
        
        List<URITemplate> operationArray = new ArrayList<>();
        Map<java.lang.String, TypeDefinition> operationList = typeRegistry.types();
        
        for (Map.Entry<String, TypeDefinition> entry : operationList.entrySet()) {
            Optional<SchemaDefinition> schemaDefinition = typeRegistry.schemaDefinition();
            if (schemaDefinition.isPresent()) {
                List<OperationTypeDefinition> operationTypeList = schemaDefinition.get().getOperationTypeDefinitions();
                for (OperationTypeDefinition operationTypeDefinition : operationTypeList) {
                    boolean canAddOperation = entry.getValue().getName()
                            .equalsIgnoreCase(operationTypeDefinition.getTypeName().getName()) &&
                            (type == null || type.equals(operationTypeDefinition.getName().toUpperCase()));
                    if (canAddOperation) {
                        if (log.isDebugEnabled()) {
                            log.debug("Adding GraphQL operations for type: " + operationTypeDefinition.getName());
                        }
                        addOperations(entry, operationTypeDefinition.getName().toUpperCase(), operationArray);
                    }
                }
            } else {
                boolean canAddOperation = (entry.getValue().getName().equalsIgnoreCase(APISpecParserConstants.GRAPHQL_QUERY) ||
                        entry.getValue().getName().equalsIgnoreCase(APISpecParserConstants.GRAPHQL_MUTATION)
                        || entry.getValue().getName().equalsIgnoreCase(APISpecParserConstants.GRAPHQL_SUBSCRIPTION)) &&
                        (type == null || type.equals(entry.getValue().getName().toUpperCase()));
                if (canAddOperation) {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding GraphQL operations for default type: " + entry.getKey());
                    }
                    addOperations(entry, entry.getKey(), operationArray);
                }
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Successfully extracted " + operationArray.size() + " GraphQL operations");
        }
        return operationArray;
    }

    /**
     * Extract GraphQL Operations from given schema.
     *
     * @param schema graphQL Schema
     * @return the arrayList of APIOperationsDTO
     */
    public List<URITemplate> extractGraphQLOperationList(String schema) {
        if (log.isDebugEnabled()) {
            log.debug("Parsing GraphQL schema to extract operations");
        }
        
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
        
        if (log.isDebugEnabled()) {
            log.debug("Successfully parsed GraphQL schema, extracting operations");
        }
        
        return extractGraphQLOperationList(typeRegistry, null);
    }

    /**
     * @param entry          Entry
     * @param operationArray operationArray
     */
    private void addOperations(Map.Entry<String, TypeDefinition> entry, String graphQLType, List<URITemplate> operationArray) {
        for (FieldDefinition fieldDef : ((ObjectTypeDefinition) entry.getValue()).getFieldDefinitions()) {
            URITemplate operation = new URITemplate();
            operation.setHTTPVerb(graphQLType);
            operation.setUriTemplate(fieldDef.getName());
            operationArray.add(operation);
        }
    }

    /**
     * Extract GraphQL Types and Fields from given schema
     *
     * @param schema GraphQL Schema
     * @return list of all types and fields
     */
    public List<GraphqlSchemaType> extractGraphQLTypeList(String schema) {
        List<GraphqlSchemaType> typeList = new ArrayList<>();
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
        Map<java.lang.String, TypeDefinition> list = typeRegistry.types();
        for (Map.Entry<String, TypeDefinition> entry : list.entrySet()) {
            if (entry.getValue() instanceof ObjectTypeDefinition) {
                GraphqlSchemaType graphqlSchemaType = new GraphqlSchemaType();
                List<String> fieldList = new ArrayList<>();
                graphqlSchemaType.setType(entry.getValue().getName());
                for (FieldDefinition fieldDef : ((ObjectTypeDefinition) entry.getValue()).getFieldDefinitions()) {
                    fieldList.add(fieldDef.getName());
                }
                graphqlSchemaType.setFieldList(fieldList);
                typeList.add(graphqlSchemaType);
            }
        }
        return typeList;
    }

    /**
     * build schema with additional info
     *
     * @param api                   api object
     * @param graphqlComplexityInfo
     * @return schemaDefinition
     */
    public String buildSchemaWithAdditionalInfo(API api, GraphqlComplexityInfo graphqlComplexityInfo) {
        Map<String, String> scopeRoleMap = new HashMap<>();
        Map<String, String> operationScopeMap = new HashMap<>();
        Map<String, String> operationAuthSchemeMap = new HashMap<>();
        Map<String, String> operationThrottlingMap = new HashMap<>();

        String operationScopeType;
        StringBuilder schemaDefinitionBuilder = new StringBuilder(api.getGraphQLSchema());
        schemaDefinitionBuilder.append("\n");
        StringBuilder operationScopeMappingBuilder = new StringBuilder();
        StringBuilder scopeRoleMappingBuilder = new StringBuilder();
        StringBuilder operationAuthSchemeMappingBuilder = new StringBuilder();
        StringBuilder operationThrottlingMappingBuilder = new StringBuilder();
        StringBuilder policyBuilder = new StringBuilder();

        String swaggerDef = api.getSwaggerDefinition();
        OpenAPI openAPI = null;
        LinkedHashMap<String, Object> scopeBindings = null;

        if (swaggerDef != null) {
            OpenAPIParser parser = new OpenAPIParser();
            openAPI = parser.readContents(swaggerDef, null, null).getOpenAPI();
        }

        Map<String, Object> extensions = null;
        if (openAPI != null) {
            extensions = openAPI.getComponents().getSecuritySchemes().get(APISpecParserConstants.SWAGGER_APIM_DEFAULT_SECURITY).
                    getFlows().getImplicit().getExtensions();
        }
        if (extensions != null) {
            scopeBindings = (LinkedHashMap<String, Object>) openAPI.getComponents().getSecuritySchemes().
                    get(APISpecParserConstants.SWAGGER_APIM_DEFAULT_SECURITY).getFlows().getImplicit().getExtensions().
                    get(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS);
        }

        if (swaggerDef != null) {
            for (URITemplate template : api.getUriTemplates()) {
                String scopeInURITemplate = template.getScope() != null ? template.getScope().getKey() : null;
                if (scopeInURITemplate != null) {
                    operationScopeMap.put(template.getUriTemplate(), scopeInURITemplate);
                    if (!scopeRoleMap.containsKey(scopeInURITemplate)) {
                        if (scopeBindings != null) {
                            scopeRoleMap.put(scopeInURITemplate, scopeBindings.get(scopeInURITemplate).toString());
                        }
                    }
                }
            }

            for (URITemplate template : api.getUriTemplates()) {
                operationThrottlingMap.put(template.getUriTemplate(), template.getThrottlingTier());
                operationAuthSchemeMap.put(template.getUriTemplate(), template.getAuthType());
            }

            if (operationScopeMap.size() > 0) {
                String base64EncodedURLOperationKey;
                String base64EncodedURLScope;
                for (Map.Entry<String, String> entry : operationScopeMap.entrySet()) {
                    base64EncodedURLOperationKey = Base64.getUrlEncoder().withoutPadding().
                            encodeToString(entry.getKey().getBytes(Charset.defaultCharset()));
                    base64EncodedURLScope = Base64.getUrlEncoder().withoutPadding().
                            encodeToString(entry.getValue().getBytes(Charset.defaultCharset()));
                    operationScopeType = "type " + APISpecParserConstants.SCOPE_OPERATION_MAPPING + "_" +
                            base64EncodedURLOperationKey + "{\n" + base64EncodedURLScope + ": String\n}\n";
                    operationScopeMappingBuilder.append(operationScopeType);
                }
                schemaDefinitionBuilder.append(operationScopeMappingBuilder.toString());
            }

            if (scopeRoleMap.size() > 0) {
                String[] roleList;
                String scopeType;
                String base64EncodedURLScopeKey;
                String scopeRoleMappingType;
                String base64EncodedURLRole;
                String roleField;
                for (Map.Entry<String, String> entry : scopeRoleMap.entrySet()) {
                    List<String> scopeRoles = new ArrayList<>();
                    base64EncodedURLScopeKey = Base64.getUrlEncoder().withoutPadding().
                            encodeToString(entry.getKey().getBytes(Charset.defaultCharset()));
                    scopeType = "type " + APISpecParserConstants.SCOPE_ROLE_MAPPING + "_" + base64EncodedURLScopeKey + "{\n";
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
                    String base64EncodedURLOperationKey = Base64.getUrlEncoder().withoutPadding().
                            encodeToString(entry.getKey().getBytes(Charset.defaultCharset()));
                    String base64EncodedURLThrottilingTier = Base64.getUrlEncoder().withoutPadding().
                            encodeToString(entry.getValue().getBytes(Charset.defaultCharset()));
                    operationThrottlingType = "type " + APISpecParserConstants.OPERATION_THROTTLING_MAPPING + "_" +
                            base64EncodedURLOperationKey + "{\n" + base64EncodedURLThrottilingTier + ": String\n}\n";
                    operationThrottlingMappingBuilder.append(operationThrottlingType);
                }
                schemaDefinitionBuilder.append(operationThrottlingMappingBuilder.toString());
            }

            if (operationAuthSchemeMap.size() > 0) {
                String operationAuthSchemeType;
                String isSecurityEnabled;
                for (Map.Entry<String, String> entry : operationAuthSchemeMap.entrySet()) {
                    String base64EncodedURLOperationKey = Base64.getUrlEncoder().withoutPadding().
                            encodeToString(entry.getKey().getBytes(Charset.defaultCharset()));
                    if (entry.getValue().equalsIgnoreCase(APISpecParserConstants.AUTH_NO_AUTHENTICATION)) {
                        isSecurityEnabled = APISpecParserConstants.OPERATION_SECURITY_DISABLED;
                    } else {
                        isSecurityEnabled = APISpecParserConstants.OPERATION_SECURITY_ENABLED;
                    }
                    operationAuthSchemeType = "type " + APISpecParserConstants.OPERATION_AUTH_SCHEME_MAPPING + "_" +
                            base64EncodedURLOperationKey + "{\n" + isSecurityEnabled + ": String\n}\n";
                    operationAuthSchemeMappingBuilder.append(operationAuthSchemeType);
                }
                schemaDefinitionBuilder.append(operationAuthSchemeMappingBuilder.toString());
            }

            if (operationAuthSchemeMap.size() > 0) {
                // Constructing the policy definition
                JSONObject jsonPolicyDefinition = policyDefinitionToJson(graphqlComplexityInfo);
                String base64EncodedPolicyDefinition = Base64.getUrlEncoder().withoutPadding().
                        encodeToString(jsonPolicyDefinition.toJSONString().getBytes(Charset.defaultCharset()));
                String policyDefinition = "type " + APISpecParserConstants.GRAPHQL_ACCESS_CONTROL_POLICY + " {\n" +
                        base64EncodedPolicyDefinition + ": String\n}\n";
                policyBuilder.append(policyDefinition);
                schemaDefinitionBuilder.append(policyBuilder.toString());
            }
        }
        return schemaDefinitionBuilder.toString();
    }

    /**
     * Method to convert GraphqlComplexityInfo object to a JSONObject
     *
     * @param graphqlComplexityInfo GraphqlComplexityInfo object
     * @return json object which contains the policy definition
     */
    public JSONObject policyDefinitionToJson(GraphqlComplexityInfo graphqlComplexityInfo) {
        JSONObject policyDefinition = new JSONObject();
        HashMap<String, HashMap<String, Integer>> customComplexityMap = new HashMap<>();
        List<CustomComplexityDetails> list = graphqlComplexityInfo.getList();
        for (CustomComplexityDetails customComplexityDetails : list) {
            String type = customComplexityDetails.getType();
            String field = customComplexityDetails.getField();
            int complexityValue = customComplexityDetails.getComplexityValue();
            if (customComplexityMap.containsKey(type)) {
                customComplexityMap.get(type).put(field, complexityValue);
            } else {
                HashMap<String, Integer> complexityValueMap = new HashMap<>();
                complexityValueMap.put(field, complexityValue);
                customComplexityMap.put(type, complexityValueMap);
            }
        }

        Map<String, Map<String, Object>> customComplexityObject = new LinkedHashMap<>(customComplexityMap.size());
        for (HashMap.Entry<String, HashMap<String, Integer>> entry : customComplexityMap.entrySet()) {
            HashMap<String, Integer> fieldValueMap = entry.getValue();
            String type = entry.getKey();
            Map<String, Object> fieldValueObject = new LinkedHashMap<>(fieldValueMap.size());
            for (HashMap.Entry<String, Integer> subEntry : fieldValueMap.entrySet()) {
                String field = subEntry.getKey();
                int complexityValue = subEntry.getValue();
                fieldValueObject.put(field, complexityValue);
            }
            customComplexityObject.put(type, fieldValueObject);
        }

        policyDefinition.put(APISpecParserConstants.QUERY_ANALYSIS_COMPLEXITY, customComplexityObject);
        return policyDefinition;
    }
}

