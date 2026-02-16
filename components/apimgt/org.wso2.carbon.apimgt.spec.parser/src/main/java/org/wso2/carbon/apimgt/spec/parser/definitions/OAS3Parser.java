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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.XmlExampleSerializer;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.util.DeserializationUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIResourceMediationPolicy;
import org.wso2.carbon.apimgt.api.model.BackendOperation;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.APIOperationMapping;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.carbon.apimgt.spec.parser.definitions.APISpecParserConstants.APPLICATION_JSON_MEDIA_TYPE;
import static org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil.isValidWithPathsWithTrailingSlashes;

/**
 * Models API definition using OAS (OpenAPI 3.0) parser
 */
public class OAS3Parser extends APIDefinition {
    private static final Log log = LogFactory.getLog(OAS3Parser.class);
    static final String OPENAPI_SECURITY_SCHEMA_KEY = "default";
    static final String OPENAPI_DEFAULT_AUTHORIZATION_URL = "https://test.com";
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private List<String> otherSchemes;
    private List<String> getOtherSchemes() {
        return otherSchemes;
    }
    private String specVersion;
    private void setOtherSchemes(List<String> otherSchemes) {
        this.otherSchemes = otherSchemes;
    }
    public OAS3Parser() {}
    public OAS3Parser(String specVersion) {
        this.specVersion = specVersion;
    }

    /**
     * This method  generates Sample/Mock payloads for Open API Specification (3.0) definitions
     *
     * @param apiDefinition API Definition
     * @return swagger Json
     */
    @Override
    public Map<String, Object> generateExample(String apiDefinition) throws APIManagementException {
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(apiDefinition, null, null);
        if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
            log.debug("Errors found when parsing OAS definition");
        }
        OpenAPI swagger = parseAttemptForV3.getOpenAPI();
        //return map
        Map<String, Object> returnMap = new HashMap<>();
        //List for APIResMedPolicyList
        List<APIResourceMediationPolicy> apiResourceMediationPolicyList = new ArrayList<>();
        for (Map.Entry<String, PathItem> entry : swagger.getPaths().entrySet()) {
            int minResponseCode = 0;
            int responseCode = 0;
            String path = entry.getKey();
            Map<String, Schema> definitions = swagger.getComponents().getSchemas();
            //operation map to get verb
            Map<PathItem.HttpMethod, Operation> operationMap = entry.getValue().readOperationsMap();
            List<Operation> operations = swagger.getPaths().get(path).readOperations();
            for (int i = 0, operationsSize = operations.size(); i < operationsSize; i++) {
                Operation op = operations.get(i);
                //initializing apiResourceMediationPolicyObject
                APIResourceMediationPolicy apiResourceMediationPolicyObject = new APIResourceMediationPolicy();
                //setting path for apiResourceMediationPolicyObject
                apiResourceMediationPolicyObject.setPath(path);
                ArrayList<Integer> responseCodes = new ArrayList<>();
                //for each HTTP method get the verb
                StringBuilder genCode = new StringBuilder();
                boolean hasJsonPayload = false;
                boolean hasXmlPayload = false;
                //for setting only one initializing if condition per response code
                boolean respCodeInitialized = false;
                Object[] operationsArray = operationMap.entrySet().toArray();
                if (operationsArray.length > i) {
                    Map.Entry<PathItem.HttpMethod, Operation> operationEntry =
                            (Map.Entry<PathItem.HttpMethod, Operation>) operationsArray[i];
                    apiResourceMediationPolicyObject.setVerb(String.valueOf(operationEntry.getKey()));
                } else {
                    throw new
                            APIManagementException("Cannot find the HTTP method for the API Resource Mediation Policy");
                }
                for (String responseEntry : op.getResponses().keySet()) {
                    if (!responseEntry.equals("default")) {
                        int minimumResponseCode;
                        int maximumResponseCode;
                        if (!responseEntry.contains("X")) {
                            minimumResponseCode = Integer.parseInt(responseEntry);
                            maximumResponseCode = Integer.parseInt(responseEntry);
                        } else {
                            minimumResponseCode = Integer.parseInt(responseEntry.replace("X","0"));
                            maximumResponseCode = Integer.parseInt(responseEntry.replace("X","9"));
                        }

                        for (responseCode = minimumResponseCode; responseCode <= maximumResponseCode; responseCode++ ) {
                            if ((op.getResponses().keySet().contains(Integer.toString(responseCode))) && (minimumResponseCode != maximumResponseCode)) {
                                continue;
                            }
                            responseCodes.add(responseCode);
                            minResponseCode = Collections.min(responseCodes);

                            Content content = op.getResponses().get(responseEntry).getContent();
                            if (content != null) {
                                MediaType applicationJson = content.get(APISpecParserConstants.APPLICATION_JSON_MEDIA_TYPE);
                                MediaType applicationXml = content.get(APISpecParserConstants.APPLICATION_XML_MEDIA_TYPE);
                                if (applicationJson != null) {
                                    Schema jsonSchema = applicationJson.getSchema();
                                    if (jsonSchema != null) {
                                        String jsonExample = getJsonExample(jsonSchema, definitions);
                                        genCode.append(getGeneratedResponsePayloads(Integer.toString(responseCode), jsonExample, "json", false));
                                        respCodeInitialized = true;
                                        hasJsonPayload = true;
                                    }
                                }
                                if (applicationXml != null) {
                                    Schema xmlSchema = applicationXml.getSchema();
                                    if (xmlSchema != null) {
                                        String xmlExample = getXmlExample(xmlSchema, definitions);
                                        genCode.append(getGeneratedResponsePayloads(Integer.toString(responseCode), xmlExample, "xml", respCodeInitialized));
                                        hasXmlPayload = true;
                                    }
                                }
                            } else {
                                setDefaultGeneratedResponse(genCode, Integer.toString(responseCode));
                                hasJsonPayload = true;
                                hasXmlPayload = true;
                            }
                        }
                    } else {
                        Content content = op.getResponses().get(responseEntry).getContent();
                        if (content != null) {
                            MediaType applicationJson = content.get(APISpecParserConstants.APPLICATION_JSON_MEDIA_TYPE);
                            MediaType applicationXml = content.get(APISpecParserConstants.APPLICATION_XML_MEDIA_TYPE);
                            if (applicationJson != null) {
                                Schema jsonSchema = applicationJson.getSchema();
                                if (jsonSchema != null) {
                                    String jsonExample = getJsonExample(jsonSchema, definitions);
                                    genCode.append(getGeneratedResponsePayloads(responseEntry, jsonExample, "json", false));
                                    respCodeInitialized = true;
                                    hasJsonPayload = true;
                                }
                            }
                            if (applicationXml != null) {
                                Schema xmlSchema = applicationXml.getSchema();
                                if (xmlSchema != null) {
                                    String xmlExample = getXmlExample(xmlSchema, definitions);
                                    genCode.append(getGeneratedResponsePayloads(responseEntry, xmlExample, "xml", respCodeInitialized));
                                    hasXmlPayload = true;
                                }
                            }
                        } else {
                            setDefaultGeneratedResponse(genCode, responseEntry);
                            hasJsonPayload = true;
                            hasXmlPayload = true;
                        }
                    }
                }
                //inserts minimum response code and mock payload variables to static script
                String finalGenCode = getMandatoryScriptSection(minResponseCode, genCode);
                //gets response section string depending on availability of json/xml payloads
                String responseConditions = getResponseConditionsSection(hasJsonPayload, hasXmlPayload);
                String finalScript = finalGenCode + responseConditions;
                apiResourceMediationPolicyObject.setContent(finalScript);
                //sets script to each resource in the swagger
                op.addExtension(APISpecParserConstants.SWAGGER_X_MEDIATION_SCRIPT, finalScript);
                apiResourceMediationPolicyList.add(apiResourceMediationPolicyObject);
            }
            checkAndSetEmptyScope(swagger);
            returnMap.put(APISpecParserConstants.SWAGGER, prettifyOAS3ToJson(swagger));
            returnMap.put(APISpecParserConstants.MOCK_GEN_POLICY_LIST, apiResourceMediationPolicyList);
        }
        return returnMap;
    }

    /**
     * This is to avoid removing the `scopes` field of default security scheme when there are no scopes present. This
     * will set an empty scope object there.
     *
     *   securitySchemes:
     *     default:
     *       type: oauth2
     *       flows:
     *         implicit:
     *           authorizationUrl: 'https://test.com'
     *           scopes: {}
     *           x-scopes-bindings: {}
     *
     * @param swagger OpenAPI object
     */
    private void checkAndSetEmptyScope(OpenAPI swagger) {
        Components comp = swagger.getComponents();
        Map<String, SecurityScheme> securitySchemeMap;
        SecurityScheme securityScheme;
        OAuthFlows oAuthFlows;
        OAuthFlow implicitFlow;
        if (comp != null && (securitySchemeMap = comp.getSecuritySchemes()) != null &&
                (securityScheme = securitySchemeMap.get(OPENAPI_SECURITY_SCHEMA_KEY)) != null &&
                (oAuthFlows = securityScheme.getFlows()) != null &&
                (implicitFlow = oAuthFlows.getImplicit()) != null && implicitFlow.getScopes() == null) {
            implicitFlow.setScopes(new Scopes());
        }
    }

    /**
     * This method  generates Sample/Mock payloads of Json Examples for operations in the swagger definition
     *
     * @param model model
     * @param definitions definition
     * @return JsonExample
     */
    private String getJsonExample(Schema model, Map<String, Schema> definitions) {
        Example example = ExampleBuilder.fromSchema(model, definitions);
        if (example == null) {
            return "";
        }
        SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
        Json.mapper().registerModule(simpleModule);
        return Json.pretty(example);
    }

    /**
     * This method  generates Sample/Mock payloads of XML Examples for operations in the swagger definition
     *
     * @param model model
     * @param definitions definition
     * @return XmlExample
     */
    private String getXmlExample(Schema model, Map<String, Schema> definitions) {
        Example example = ExampleBuilder.fromSchema(model, definitions);
        if (example == null) {
            return "";
        }
        String rawXmlExample = new XmlExampleSerializer().serialize(example);
        return rawXmlExample.replace("<?xml version='1.1' encoding='UTF-8'?>", "");
    }

    /**
     *Sets default script for response codes without defined payloads
     *
     * @param genCode String builder
     */
    private void setDefaultGeneratedResponse(StringBuilder genCode, String responseCode) {
        if (responseCode.equals("default")) {
            responseCode = "\"" + responseCode + "\"";
        }
        genCode.append("if (!responses[").append(responseCode).append("]) {\n" +
                "  responses[").append(responseCode).append("] = [];\n" +
                "}\n" +
                "responses[").append(responseCode).append("][\"application/json\"] = \"\";\n" +
                "responses[").append(responseCode).append("][\"application/xml\"] = \"\";\n\n");
    }

    /**
     * Generates string for initializing response code arrays and payload variables
     *
     * @param responseCode response Entry Code
     * @param example generated Example Json/Xml
     * @param type  mediaType (Json/Xml)
     * @param initialized response code array
     * @return generatedString
     */
    private String getGeneratedResponsePayloads(String responseCode, String example, String type, boolean initialized) {
        StringBuilder genRespPayload = new StringBuilder();
        if (responseCode.equals("default")) {
            responseCode = "\"" + responseCode + "\"";
        }
        if (StringUtils.isBlank(example) || example.equals("null")) {
            example = "\"\"";
        }
        if (!initialized) {
            genRespPayload.append("\nif (!responses[").append(responseCode).append("]) {").append("\n responses [").append(responseCode).append("] = [];").append("\n}");
        }
        genRespPayload.append("\nresponses[").append(responseCode).append("][\"application/").append(type).append("\"] = ").append(example).append(";\n");
        return genRespPayload.toString();
    }

    /**
     * Generates variables for setting accept-header type and response code specified by user
     * and sets generated payloads and minimum response code in case specified response code is null
     *
     * @param minResponseCode minimum response code
     * @param payloadVariables generated payloads
     * @return script with mock payloads and conditions to handle not implemented
     */
    private String getMandatoryScriptSection(int minResponseCode, StringBuilder payloadVariables) {
        return "var accept = mc.getProperty('AcceptHeader');" +
                "\nvar responseCode = mc.getProperty('query.param.responseCode');" +
                "\nvar responseCodeSC;" +
                "\nvar responses = [];\n" +
                payloadVariables +
                "\nresponses[501] = [];" +
                "\nresponses[501][\"application/json\"] = {" +
                "\n\"code\" : 501," +
                "\n\"description\" : \"Not Implemented\"" +
                "}\n" +
                "responses[501][\"application/xml\"] = <response><code>501</code><description>Not Implemented</description></response>;\n\n" +
                "if (responseCode == null) {\n" +
                " responseCode = " + minResponseCode + ";\n" +   //assign lowest response code
                "}\n\n" +
                "if (!responses[responseCode]) {\n" +
                "  if (responses[\"default\"]) {\n" +
                "    responseCode = \"default\"\n" +
                "  } else {\n" +
                "    responseCode = 501;\n" +
                "  }\n" +
                "}\n" +
                "if (responseCode === \"default\") {\n" +
                "  responseCodeSC = mc.getProperty('query.param.responseCode');\n" +
                "} else {\n" +
                "  responseCodeSC = responseCode;\n" +
                "}\n" +
                "if (accept == null || !responses[responseCode][accept]) {\n";
    }

    /**
     * Conditions for setting responses at end of inline script of each resource
     *
     * @param hasJsonPayload contains JSON payload
     * @param hasXmlPayload contains XML payload
     * @return response section that sets response code and type
     */
    private String getResponseConditionsSection(boolean hasJsonPayload, boolean hasXmlPayload) {
        String responseSection = "";
        if (hasJsonPayload && hasXmlPayload) {
            responseSection = " accept = \"application/json\";\n" +
                    "}\n\n" +
                    "if (accept == \"application/json\") {\n" +
                    " mc.setProperty('CONTENT_TYPE', 'application/json');\n" +
                    " mc.setProperty('HTTP_SC', responseCodeSC + \"\");\n" +
                    " mc.setPayloadJSON(responses[responseCode][\"application/json\"]);\n" +
                    "} else if (accept == \"application/xml\") {\n" +
                    " mc.setProperty('CONTENT_TYPE', 'application/xml');\n" +
                    " mc.setProperty('HTTP_SC', responseCodeSC + \"\");\n" +
                    " mc.setPayloadXML(responses[responseCode][\"application/xml\"]);\n" +
                    "}";
        } else if (hasJsonPayload) {
            responseSection = " accept = \"application/json\"; // assign whatever available\n" +
                    "}\n\n" +
                    "if (accept == \"application/json\") {\n" +
                    " mc.setProperty('CONTENT_TYPE', 'application/json');\n" +
                    " mc.setProperty('HTTP_SC', responseCodeSC + \"\");\n" +
                    " mc.setPayloadJSON(responses[responseCode][\"application/json\"]);\n" +
                    "}";
        } else if (hasXmlPayload) {
            responseSection = " accept = \"application/xml\"; // assign whatever available\n" +
                    "}\n\n" +
                    "if (accept == \"application/xml\") {\n" +
                    " mc.setProperty('CONTENT_TYPE', 'application/xml');\n" +
                    " mc.setProperty('HTTP_SC', responseCodeSC + \"\");\n" +
                    " mc.setPayloadXML(responses[responseCode][\"application/xml\"]);\n" +
                    "}";
        }
        return responseSection;
    }

    /**
     * This method returns URI templates according to the given swagger file
     *
     * @param resourceConfigsJSON swaggerJSON
     * @return URI Templates
     * @throws APIManagementException
     */
    @Override
    public Set<URITemplate> getURITemplates(String resourceConfigsJSON) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(resourceConfigsJSON);
        Set<URITemplate> urlTemplates = new LinkedHashSet<>();
        Set<Scope> scopes = getScopes(resourceConfigsJSON);

        for (String pathKey : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(pathKey);
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                Operation operation = entry.getValue();
                URITemplate template = new URITemplate();
                if (APISpecParserConstants.SUPPORTED_METHODS.contains(entry.getKey().name().toLowerCase())) {
                    template.setHTTPVerb(entry.getKey().name().toUpperCase());
                    template.setHttpVerbs(entry.getKey().name().toUpperCase());
                    template.setUriTemplate(pathKey);
                    List<String> opScopes = getScopeOfOperations(OPENAPI_SECURITY_SCHEMA_KEY, operation);
                    if (!opScopes.isEmpty()) {
                        if (opScopes.size() == 1) {
                            String firstScope = opScopes.get(0);
                            if (StringUtils.isNoneBlank(firstScope)) {
                                Scope scope = APISpecParserUtil.findScopeByKey(scopes, firstScope);
                                if (scope == null) {
                                    throw new APIManagementException("Scope '" + firstScope + "' not found.");
                                }
                                template.setScope(scope);
                                template.setScopes(scope);
                            }
                        } else {
                            template = OASParserUtil.setScopesToTemplate(template, opScopes, scopes);
                        }
                    } else if (!getScopeOfOperations("OAuth2Security", operation).isEmpty()) {
                        opScopes = getScopeOfOperations("OAuth2Security", operation);
                        if (opScopes.size() == 1) {
                            String firstScope = opScopes.get(0);
                            if (StringUtils.isNoneBlank(firstScope)) {
                                Scope scope = APISpecParserUtil.findScopeByKey(scopes, firstScope);
                                if (scope == null) {
                                    throw new APIManagementException("Scope '" + firstScope + "' not found.");
                                }
                                template.setScope(scope);
                                template.setScopes(scope);
                            }
                        } else {
                            template = OASParserUtil.setScopesToTemplate(template, opScopes, scopes);
                        }
                    }
                    Map<String, Object> extensions = operation.getExtensions();
                    if (extensions != null) {
                        if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_AUTH_TYPE)) {
                            String scopeKey = (String) extensions.get(APISpecParserConstants.SWAGGER_X_AUTH_TYPE);
                            template.setAuthType(scopeKey);
                            template.setAuthTypes(scopeKey);
                        } else {
                            template.setAuthType("Any");
                            template.setAuthTypes("Any");
                        }
                        if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_THROTTLING_TIER)) {
                            String throttlingTier = (String) extensions.get(APISpecParserConstants.SWAGGER_X_THROTTLING_TIER);
                            template.setThrottlingTier(throttlingTier);
                            template.setThrottlingTiers(throttlingTier);
                        }
                        if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_MEDIATION_SCRIPT)) {
                            String mediationScript = (String) extensions.get(APISpecParserConstants.SWAGGER_X_MEDIATION_SCRIPT);
                            template.setMediationScript(mediationScript);
                            template.setMediationScripts(template.getHTTPVerb(), mediationScript);
                        }
                        if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_AMZN_RESOURCE_NAME)) {
                            template.setAmznResourceName((String)
                                    extensions.get(APISpecParserConstants.SWAGGER_X_AMZN_RESOURCE_NAME));
                        }
                        if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)) {
                            template.setAmznResourceTimeout(((Number)
                                    extensions.get(APISpecParserConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)).intValue());
                        }
                        if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_AMZN_RESOURCE_CONTENT_ENCODED)) {
                            template.setAmznResourceContentEncoded((Boolean)
                                    extensions.get(APISpecParserConstants.SWAGGER_X_AMZN_RESOURCE_CONTENT_ENCODED));
                        }
                    }
                    urlTemplates.add(template);
                }
            }
        }
        return urlTemplates;
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
        OpenAPI openAPI = getOpenAPI(resourceConfigsJSON);
        return getScopesFromOpenAPI(openAPI);
    }

    /**
     * This method returns the scopes from the OpenAPI object.
     *
     * @param openAPI OpenAPI object
     * @return Set of scopes
     * @throws APIManagementException if an error occurs while retrieving scopes
     */
    private Set<Scope> getScopesFromOpenAPI(OpenAPI openAPI) throws APIManagementException {

        Map<String, SecurityScheme> securitySchemes;
        SecurityScheme securityScheme;
        OAuthFlows oAuthFlows;
        OAuthFlow oAuthFlow;
        Scopes scopes;
        if (openAPI.getComponents() != null && (securitySchemes = openAPI.getComponents().getSecuritySchemes())
                != null) {
            Set<Scope> scopeSet = new HashSet<>();
            if ((securityScheme = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY)) != null &&
                    (oAuthFlows = securityScheme.getFlows()) != null && (oAuthFlow = oAuthFlows.getImplicit()) != null
                    && (scopes = oAuthFlow.getScopes()) != null) {
                for (Map.Entry<String, String> entry : scopes.entrySet()) {
                    Scope scope = new Scope();
                    scope.setKey(entry.getKey());
                    scope.setName(entry.getKey());
                    scope.setDescription(entry.getValue());
                    Map<String, String> scopeBindings;
                    if (oAuthFlow.getExtensions() != null && (scopeBindings =
                            (Map<String, String>) oAuthFlow.getExtensions()
                                    .get(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS))
                            != null) {
                        if (scopeBindings.get(scope.getKey()) != null) {
                            scope.setRoles(scopeBindings.get(scope.getKey()));
                        }
                    }
                    scopeSet.add(scope);
                }
                if (scopes.isEmpty() && openAPI.getExtensions() != null
                        && openAPI.getExtensions().containsKey(APISpecParserConstants.SWAGGER_X_WSO2_SECURITY)) {
                    return OASParserUtil.sortScopes(getScopesFromExtensions(openAPI));
                }
            } else if ((securityScheme = securitySchemes.get("OAuth2Security")) != null &&
                    (oAuthFlows = securityScheme.getFlows()) != null && (oAuthFlow = oAuthFlows.getPassword()) != null
                    && (scopes = oAuthFlow.getScopes()) != null) {
                for (Map.Entry<String, String> entry : scopes.entrySet()) {
                    Scope scope = new Scope();
                    scope.setKey(entry.getKey());
                    scope.setName(entry.getKey());
                    scope.setDescription(entry.getValue());
                    Map<String, String> scopeBindings;
                    scopeSet.add(scope);
                }
            } else if (openAPI.getExtensions() != null
                    && openAPI.getExtensions().containsKey(APISpecParserConstants.SWAGGER_X_WSO2_SECURITY)) {
                return OASParserUtil.sortScopes(getScopesFromExtensions(openAPI));
            }
            return OASParserUtil.sortScopes(scopeSet);
        } else {
            return OASParserUtil.sortScopes(getScopesFromExtensions(openAPI));
        }
    }

    /**
     * This method generates API definition to the given api
     *
     * @param swaggerData api
     * @return API definition in string format
     * @throws APIManagementException
     */
    @Override
    public String generateAPIDefinition(SwaggerData swaggerData) throws APIManagementException {

        OpenAPI openAPI = new OpenAPI();
        //Set the openAPI 3.1.0 version
        if (APISpecParserConstants.OAS_V31.equalsIgnoreCase(specVersion)) {
            openAPI.setOpenapi(APISpecParserConstants.OPEN_API_V31_VERSION);
        }

        // create path if null
        if (openAPI.getPaths() == null) {
            openAPI.setPaths(new Paths());
        }

        //Create info object
        Info info = new Info();
        info.setTitle(swaggerData.getTitle());
        if (swaggerData.getDescription() != null) {
            info.setDescription(swaggerData.getDescription());
        }

        Contact contact = new Contact();
        //Create contact object and map business owner info
        if (swaggerData.getContactName() != null) {
            contact.setName(swaggerData.getContactName());
        }
        if (swaggerData.getContactEmail() != null) {
            contact.setEmail(swaggerData.getContactEmail());
        }
        if (swaggerData.getContactName() != null || swaggerData.getContactEmail() != null) {
            //put contact object to info object
            info.setContact(contact);
        }

        info.setVersion(swaggerData.getVersion());
        openAPI.setInfo(info);
        updateSwaggerSecurityDefinition(openAPI, swaggerData, OPENAPI_DEFAULT_AUTHORIZATION_URL,
                new KeyManagerConfigurationDTO());
        updateLegacyScopesFromSwagger(openAPI, swaggerData);
        if (APISpecParserConstants.GRAPHQL_API.equals(swaggerData.getTransportType())) {
            modifyGraphQLSwagger(openAPI);
        } else if (APISpecParserConstants.MCP_API.equals(swaggerData.getTransportType())) {
            addDefaultPostPathToSwagger(openAPI, APISpecParserConstants.MCP_RESOURCES_MCP);
            addDefaultGetPathToSwagger(openAPI, APISpecParserConstants.MCP_RESOURCES_MCP);
            addAuthServerMetaEndpointPathToSwagger(openAPI, APISpecParserConstants.MCP_RESOURCES_WELL_KNOWN);
        } else {
            for (SwaggerData.Resource resource : swaggerData.getResources()) {
                addOrUpdatePathToSwagger(openAPI, resource);
            }
        }
        return prettifyOAS3ToJson(openAPI);
    }

    /**
     * This method generates API definition using the given api's URI templates and the swagger.
     * It will alter the provided swagger definition based on the URI templates. For example: if there is a new
     * URI template which is not included in the swagger, it will be added to the swagger as a basic resource. Any
     * additional resources inside the swagger will be removed from the swagger. Changes to scopes, throtting policies,
     * on the resource will be updated on the swagger
     *
     * @param swaggerData api
     * @param swagger     swagger definition
     * @return API definition in string format
     * @throws APIManagementException if error occurred when generating API Definition
     */
    @Override
    @Deprecated
    public String generateAPIDefinition(SwaggerData swaggerData, String swagger) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swagger);
        return generateAPIDefinition(swaggerData, openAPI);
    }

    /**
     * This method generates API definition using the given api's URI templates and the swagger with parser options.
     *
     * @param swaggerData api
     * @param swagger     swagger definition
     * @param options OASParserOptions
     * @return API definition in string format
     * @throws APIManagementException if error occurred when generating API Definition
     */
    @Override
    public String generateAPIDefinition(SwaggerData swaggerData, String swagger, OASParserOptions options)
            throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swagger, options);
        return generateAPIDefinition(swaggerData, openAPI);
    }

    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent) throws APIManagementException {
        return validateAPIDefinition(apiDefinition, "", returnJsonContent);
    }

    /**
     * This method generates API definition using the given api's URI templates and the swagger.
     * It will alter the provided swagger definition based on the URI templates. For example: if there is a new
     * URI template which is not included in the swagger, it will be added to the swagger as a basic resource. Any
     * additional resources inside the swagger will be removed from the swagger. Changes to scopes, throtting policies,
     * on the resource will be updated on the swagger
     *
     * @param swaggerData api
     * @param openAPI     OpenAPI
     * @return API definition in string format
     * @throws APIManagementException if error occurred when generating API Definition
     */
    private String generateAPIDefinition(SwaggerData swaggerData, OpenAPI openAPI) throws APIManagementException {

        Set<SwaggerData.Resource> copy = new HashSet<>(swaggerData.getResources());

        Iterator<Map.Entry<String, PathItem>> itr = openAPI.getPaths().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, PathItem> pathEntry = itr.next();
            String pathKey = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                Operation operation = entry.getValue();
                boolean operationFound = false;
                for (SwaggerData.Resource resource : swaggerData.getResources()) {
                    if ((pathKey.equalsIgnoreCase(resource.getPath())
                            && entry.getKey().name().equalsIgnoreCase(resource.getVerb()))) {
                        //update operations in definition
                        operationFound = true;
                        copy.remove(resource);
                        updateOperationManagedInfo(resource, operation);
                        break;
                    }
                }
                // remove operation from definition
                if (!operationFound) {
                    pathItem.operation(entry.getKey(), null);
                }
            }
            if (pathItem.readOperations().isEmpty()) {
                itr.remove();
            }
        }
        if (APISpecParserConstants.GRAPHQL_API.equals(swaggerData.getTransportType())) {
            modifyGraphQLSwagger(openAPI);
        } else {
            //adding new operations to the definition
            for (SwaggerData.Resource resource : copy) {
                addOrUpdatePathToSwagger(openAPI, resource);
            }
        }
        updateSwaggerSecurityDefinition(openAPI, swaggerData, OPENAPI_DEFAULT_AUTHORIZATION_URL,
                new KeyManagerConfigurationDTO());
        updateLegacyScopesFromSwagger(openAPI, swaggerData);

        if (openAPI.getInfo() != null) {
            openAPI.getInfo().setTitle(swaggerData.getTitle());
            openAPI.getInfo().setVersion(swaggerData.getVersion());
        } else {
            Info info = new Info();
            info.setTitle(swaggerData.getTitle());
            info.setVersion(swaggerData.getVersion());
            openAPI.setInfo(info);
        }

        if (!APISpecParserConstants.GRAPHQL_API.equals(swaggerData.getTransportType())) {
            preserveResourcePathOrderFromAPI(swaggerData, openAPI);
        }
        return prettifyOAS3ToJson(openAPI);
    }

    @Override
    public String generateAPIDefinitionForBackendAPI(SwaggerData swaggerData, String oasDefinition) {

        OpenAPI openAPI = getOpenAPI(oasDefinition);
        removePublisherSpecificInfo(openAPI);
        cleanUpPathItems(openAPI, swaggerData.getResources());

        updateOpenAPIMetadata(openAPI, swaggerData);
        updateSwaggerSecurityDefinition(openAPI, swaggerData, OPENAPI_DEFAULT_AUTHORIZATION_URL,
                new KeyManagerConfigurationDTO());
        updateLegacyScopesFromSwagger(openAPI, swaggerData);

        return prettifyOAS3ToJson(openAPI);
    }

    /**
     * Clean up path items in the OpenAPI definition by matching them with the resources from the Swagger data.
     * If a resource matches a path item, it updates the operation with the resource's managed info.
     * If a path item has no operations left, it removes that path item.
     *
     * @param openAPI            OpenAPI object to be cleaned up
     * @param unmatchedResources Set of unmatched SwaggerData.Resource objects
     */
    private void cleanUpPathItems(OpenAPI openAPI, Set<SwaggerData.Resource> unmatchedResources) {

        Iterator<Map.Entry<String, PathItem>> pathIterator = openAPI.getPaths().entrySet().iterator();

        while (pathIterator.hasNext()) {
            Map.Entry<String, PathItem> pathEntry = pathIterator.next();
            String path = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();

            for (Map.Entry<PathItem.HttpMethod, Operation> methodEntry : pathItem.readOperationsMap().entrySet()) {
                PathItem.HttpMethod httpMethod = methodEntry.getKey();
                Operation operation = methodEntry.getValue();

                SwaggerData.Resource matchedResource =
                        findMatchingResource(unmatchedResources, path, httpMethod.name());
                if (matchedResource != null) {
                    unmatchedResources.remove(matchedResource);
                    updateOperationManagedInfo(matchedResource, operation);
                }
            }

            if (pathItem.readOperations().isEmpty()) {
                pathIterator.remove();
            }
        }
    }

    /**
     * Find a matching resource in the set of resources based on the path and method.
     *
     * @param resources Set of SwaggerData.Resource objects
     * @param path      Path to match
     * @param method    HTTP method to match
     * @return Matching SwaggerData.Resource object or null if not found
     */
    private SwaggerData.Resource findMatchingResource(Set<SwaggerData.Resource> resources, String path, String method) {

        for (SwaggerData.Resource resource : resources) {
            if (APISpecParserConstants.HTTP_VERB_TOOL.equalsIgnoreCase(resource.getVerb())
                    && resource.getBackendOperationMapping() != null) {
                APIConstants.SupportedHTTPVerbs mappedMethod =
                        resource.getBackendOperationMapping().getBackendOperation().getVerb();
                String mappedTarget =
                        resource.getBackendOperationMapping().getBackendOperation().getTarget();
                if (method.equalsIgnoreCase(mappedMethod.toString()) && path.equalsIgnoreCase(mappedTarget)) {
                    return resource;
                }
            }
        }
        return null;
    }

    /**
     * Update OpenAPI metadata such as title and version.
     *
     * @param openAPI     OpenAPI object to be updated
     * @param swaggerData Swagger data containing the title and version
     */
    private void updateOpenAPIMetadata(OpenAPI openAPI, SwaggerData swaggerData) {

        Info info = openAPI.getInfo();
        if (info == null) {
            info = new Info();
            openAPI.setInfo(info);
        }
        info.setTitle(swaggerData.getTitle());
        info.setVersion(swaggerData.getVersion());
    }

    /**
     * Preserve and rearrange the OpenAPI definition according to the resource path order of the updating API payload.
     *
     * @param swaggerData Updating API swagger data
     * @param openAPI     Updated OpenAPI definition
     */
    private void preserveResourcePathOrderFromAPI(SwaggerData swaggerData, OpenAPI openAPI) {

        Set<String> orderedResourcePaths = new LinkedHashSet<>();
        Paths orderedOpenAPIPaths = new Paths();
        // Iterate the URI template order given in the updating API payload (Swagger Data) and rearrange resource paths
        // order in OpenAPI with relevance to the first matching resource path item from the swagger data path list.
        for (SwaggerData.Resource resource : swaggerData.getResources()) {
            String path = resource.getPath();
            if (!orderedResourcePaths.contains(path)) {
                orderedResourcePaths.add(path);
                // Get the resource path item for the path from existing OpenAPI
                PathItem resourcePathItem = openAPI.getPaths().get(path);
                orderedOpenAPIPaths.addPathItem(path, resourcePathItem);
            }
        }
        openAPI.setPaths(orderedOpenAPIPaths);
    }

    /**
     * Construct path parameters to the Operation
     *
     * @param operation OpenAPI operation
     * @param pathName  pathname
     */
    private void populatePathParameters(Operation operation, String pathName) {
        List<String> pathParams = getPathParamNames(pathName);
        Parameter parameter;
        if (pathParams.size() > 0) {
            for (String pathParam : pathParams) {
                parameter = new Parameter();
                parameter.setName(pathParam);
                parameter.setRequired(true);
                parameter.setIn("path");
                Schema schema = new Schema();
                schema.setType("string");
                parameter.setSchema(schema);
                operation.addParametersItem(parameter);
            }
        }
    }

    /**
     * This method validates the given OpenAPI definition by content
     *
     * @param apiDefinition     OpenAPI Definition content
     * @param host OpenAPI Definition url
     * @param returnJsonContent whether to return the converted json form of the OpenAPI definition
     * @return APIDefinitionValidationResponse object with validation information
     */
    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, String host, boolean returnJsonContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(apiDefinition, null, options);
        if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
            validationResponse.setValid(false);
            for (String message : parseAttemptForV3.getMessages()) {
                OASParserUtil.addErrorToValidationResponse(validationResponse, message);
                if (message.contains(APISpecParserConstants.OPENAPI_IS_MISSING_MSG)) {
                    ErrorItem errorItem = new ErrorItem();
                    errorItem.setErrorCode(ExceptionCodes.INVALID_OAS3_FOUND.getErrorCode());
                    errorItem.setMessage(ExceptionCodes.INVALID_OAS3_FOUND.getErrorMessage());
                    errorItem.setDescription(ExceptionCodes.INVALID_OAS3_FOUND.getErrorMessage());
                    validationResponse.getErrorItems().add(errorItem);
                }
            }
            if (System.getProperty(APISpecParserConstants.SWAGGER_RELAXED_VALIDATION) != null &&
                    parseAttemptForV3.getOpenAPI() != null) {
                validationResponse.setValid(true);
            } else {
                validationResponse.setValid(false);
            }
        } else {
            validationResponse.setValid(true);

            // Check for multiple resource paths with and without trailing slashes.
            // If there are two resource paths with the same name, one with and one without trailing slashes,
            // it will be considered an error since those are considered as one resource in the API deployment.
            if (parseAttemptForV3.getOpenAPI() != null) {
                if (!isValidWithPathsWithTrailingSlashes(parseAttemptForV3.getOpenAPI(), null, validationResponse)) {
                    validationResponse.setValid(false);
                };
            }
        }
        if (validationResponse.isValid()){
            OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
            io.swagger.v3.oas.models.info.Info info = openAPI.getInfo();
            List<String> endpoints;
            String endpointWithHost = "";
            if (openAPI.getServers() == null || openAPI.getServers().isEmpty()) {
                endpoints = null;
            } else {
                endpoints = openAPI.getServers().stream().map(url -> url.getUrl()).collect(Collectors.toList());
                for (String endpoint : endpoints) {
                    if (endpoint.startsWith("/")) {
                        if (StringUtils.isEmpty(host)) {
                            endpointWithHost = "http://api.yourdomain.com" + endpoint;
                        } else {
                            endpointWithHost = host + endpoint;
                        }
                       endpoints.set(endpoints.indexOf(endpoint), endpointWithHost);
                    }
                }
            }
            List<URITemplate> uriTemplates;
            if (openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
                uriTemplates = null;
            } else {
                uriTemplates = new ArrayList<>();
                for (String pathKey : openAPI.getPaths().keySet()) {
                    PathItem pathItem = openAPI.getPaths().get(pathKey);
                    for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                        URITemplate template = new URITemplate();
                        if (APISpecParserConstants.SUPPORTED_METHODS.contains(entry.getKey().name().toLowerCase())) {
                            template.setHTTPVerb(entry.getKey().name().toUpperCase());
                            template.setHttpVerbs(entry.getKey().name().toUpperCase());
                            template.setUriTemplate(pathKey);
                            uriTemplates.add(template);
                        }
                    }
                }
            }

            String title = null;
            String context = null;
            String version = null;
            String description = null;

            // If info is null, random values for metadata will be generated only if SWAGGER_RELAXED_VALIDATION is set.
            if (info != null) {
                if (!StringUtils.isBlank(info.getTitle())) {
                    title = info.getTitle();
                    context = info.getTitle().replaceAll("\\s", "").toLowerCase();
                }
                version = info.getVersion();
                description = info.getDescription();
            } else {
                // Generate random placeholder values to prevent null assignments and ensure downstream components receive valid response attributes.
                title = "API-Title-" + UUID.randomUUID().toString();
                context = title.toLowerCase();
                version = "v1-" + UUID.randomUUID().toString().substring(0, 3);
                description = "API-description-" + UUID.randomUUID().toString();
            }
            OASParserUtil.updateValidationResponseAsSuccess(
                    validationResponse, apiDefinition, openAPI.getOpenapi(),
                    title, version, context,
                    description, endpoints, uriTemplates
            );
            validationResponse.setParser(this);
            if (returnJsonContent) {
                if (!apiDefinition.trim().startsWith("{")) { // not a json (it is yaml)
                    JsonNode jsonNode = DeserializationUtils.readYamlTree(apiDefinition);
                    validationResponse.setJsonContent(jsonNode.toString());
                } else {
                    validationResponse.setJsonContent(apiDefinition);
                }
            }
        }
        return validationResponse;
    }

    /**
     * Populate definition with wso2 APIM specific information
     *
     * @param oasDefinition OAS definition
     * @param swaggerData   API related Swagger data
     * @return Generated OAS definition
     * @throws APIManagementException If an error occurred
     */
    @Override
    @Deprecated
    public String populateCustomManagementInfo(String oasDefinition, SwaggerData swaggerData)
            throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(oasDefinition);
        removePublisherSpecificInfo(openAPI);
        return generateAPIDefinition(swaggerData, openAPI);
    }

    /**
     * Populate definition with wso2 APIM specific information
     *
     * @param oasDefinition OAS definition
     * @param swaggerData   API related Swagger data
     * @return Generated OAS definition
     * @throws APIManagementException If an error occurred
     */
    @Override
    public String populateCustomManagementInfo(String oasDefinition, SwaggerData swaggerData, OASParserOptions options)
            throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(oasDefinition, options);
        removePublisherSpecificInfo(openAPI);
        return generateAPIDefinition(swaggerData, openAPI);
    }

    /**
     * Remove MG related information
     *
     * @param openAPI OpenAPI
     */
    private void removePublisherSpecificInfo(OpenAPI openAPI) {
        Map<String, Object> extensions = openAPI.getExtensions();
        OASParserUtil.removePublisherSpecificInfo(extensions);
        for (String pathKey : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(pathKey);
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                Operation operation = entry.getValue();
                OASParserUtil.removePublisherSpecificInfofromOperation(operation.getExtensions());
            }
        }
    }

    /**
     * Update OAS definition for store
     *
     * @param api                        API
     * @param oasDefinition              OAS definition
     * @param hostsWithSchemes           host addresses with protocol mapping
     * @param keyManagerConfigurationDTO configuration details of the Key Manager
     * @return OAS definition
     */
    @Override
    @Deprecated
    public String getOASDefinitionForStore(API api, String oasDefinition, Map<String, String> hostsWithSchemes,
                                           KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {

        OpenAPI openAPI = getOpenAPI(oasDefinition);
        updateOperations(openAPI);
        updateEndpoints(api, hostsWithSchemes, openAPI);
        return updateSwaggerSecurityDefinitionForStore(openAPI, new SwaggerData(api), hostsWithSchemes,
                keyManagerConfigurationDTO);
    }

    /**
     * Update OAS definition for store with parser options
     *
     * @param api                        API
     * @param oasDefinition              OAS definition
     * @param hostsWithSchemes           host addresses with protocol mapping
     * @param keyManagerConfigurationDTO configuration details of the Key Manager
     * @param options                    OASParserOptions
     * @return OAS definition
     */
    @Override
    public String getOASDefinitionForStore(API api, String oasDefinition, Map<String, String> hostsWithSchemes,
            KeyManagerConfigurationDTO keyManagerConfigurationDTO, OASParserOptions options)
            throws APIManagementException {

        OpenAPI openAPI = getOpenAPI(oasDefinition, options);
        updateOperations(openAPI);
        updateEndpoints(api, hostsWithSchemes, openAPI);
        return updateSwaggerSecurityDefinitionForStore(openAPI, new SwaggerData(api), hostsWithSchemes,
                keyManagerConfigurationDTO);
    }

    /**
     * Update OAS definition for store
     *
     * @param product                    APIProduct
     * @param oasDefinition              OAS definition
     * @param hostsWithSchemes           host addresses with protocol mapping
     * @param keyManagerConfigurationDTO configuration details of the Key Manager
     * @return OAS definition
     */
    @Override
    @Deprecated
    public String getOASDefinitionForStore(APIProduct product, String oasDefinition,
                                           Map<String, String> hostsWithSchemes,
                                           KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {

        OpenAPI openAPI = getOpenAPI(oasDefinition);
        updateOperations(openAPI);
        updateEndpoints(product, hostsWithSchemes, openAPI);
        return updateSwaggerSecurityDefinitionForStore(openAPI, new SwaggerData(product), hostsWithSchemes,
                keyManagerConfigurationDTO);
    }

    /**
     * Update OAS definition for store with parser options
     *
     * @param product                    APIProduct
     * @param oasDefinition              OAS definition
     * @param hostsWithSchemes           host addresses with protocol mapping
     * @param keyManagerConfigurationDTO configuration details of the Key Manager
     * @param options                    OASParserOptions
     * @return OAS definition
     */
    @Override
    public String getOASDefinitionForStore(APIProduct product, String oasDefinition,
            Map<String, String> hostsWithSchemes,
            KeyManagerConfigurationDTO keyManagerConfigurationDTO, OASParserOptions options)
            throws APIManagementException {

        OpenAPI openAPI = getOpenAPI(oasDefinition, options);
        updateOperations(openAPI);
        updateEndpoints(product, hostsWithSchemes, openAPI);
        return updateSwaggerSecurityDefinitionForStore(openAPI, new SwaggerData(product), hostsWithSchemes,
                keyManagerConfigurationDTO);
    }

    /**
     * Update OAS definition for API Publisher
     *
     * @param api           API
     * @param oasDefinition
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    @Override
    @Deprecated
    public String getOASDefinitionForPublisher(API api, String oasDefinition) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(oasDefinition);
        return getOASDefinitionForPublisherCore(api, openAPI);
    }

    /**
     * Update OAS definition for API Publisher with parser options
     *
     * @param api           API
     * @param oasDefinition
     * @param options       OASParserOptions
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    @Override
    public String getOASDefinitionForPublisher(API api, String oasDefinition, OASParserOptions options) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(oasDefinition, options);
        return getOASDefinitionForPublisherCore(api, openAPI);
    }

    private String getOASDefinitionForPublisherCore(API api, OpenAPI openAPI) throws APIManagementException {
        if (openAPI.getComponents() == null) {
            openAPI.setComponents(new Components());
        }
        Map<String, SecurityScheme> securitySchemes = openAPI.getComponents().getSecuritySchemes();
        if (securitySchemes == null) {
            securitySchemes = new HashMap<>();
            openAPI.getComponents().setSecuritySchemes(securitySchemes);
        }
        SecurityScheme securityScheme = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY);
        if (securityScheme == null) {
            securityScheme = new SecurityScheme();
            securityScheme.setType(SecurityScheme.Type.OAUTH2);
            securitySchemes.put(OPENAPI_SECURITY_SCHEMA_KEY, securityScheme);
            List<SecurityRequirement> security = new ArrayList<SecurityRequirement>();
            SecurityRequirement secReq = new SecurityRequirement();
            secReq.addList(OPENAPI_SECURITY_SCHEMA_KEY, new ArrayList<String>());
            security.add(secReq);
            openAPI.setSecurity(security);
        }
        if (securityScheme.getFlows() == null) {
            securityScheme.setFlows(new OAuthFlows());
        }
        // setting scopes id if it is null
        // https://github.com/swagger-api/swagger-parser/issues/1202
        OAuthFlow oAuthFlow = securityScheme.getFlows().getImplicit();
        if (oAuthFlow == null) {
            oAuthFlow = new OAuthFlow();
            securityScheme.getFlows().setImplicit(oAuthFlow);
        }
        if (oAuthFlow.getScopes() == null) {
            oAuthFlow.setScopes(new Scopes());
        }
        if (oAuthFlow.getAuthorizationUrl() == null) {
            oAuthFlow.setAuthorizationUrl(OPENAPI_DEFAULT_AUTHORIZATION_URL);
        }
        if (api.getAuthorizationHeader() != null) {
            openAPI.addExtension(APISpecParserConstants.X_WSO2_AUTH_HEADER, api.getAuthorizationHeader());
        }
        if (api.getApiKeyHeader() != null) {
            openAPI.addExtension(APISpecParserConstants.X_WSO2_API_KEY_HEADER, api.getApiKeyHeader());
        }
        if (api.getApiLevelPolicy() != null) {
            openAPI.addExtension(APISpecParserConstants.X_THROTTLING_TIER, api.getApiLevelPolicy());
        }
        openAPI.addExtension(APISpecParserConstants.X_WSO2_CORS, api.getCorsConfiguration());
        Object prodEndpointObj = OASParserUtil.generateOASConfigForEndpoints(api, true);
        if (prodEndpointObj != null) {
            openAPI.addExtension(APISpecParserConstants.X_WSO2_PRODUCTION_ENDPOINTS, prodEndpointObj);
        }
        Object sandEndpointObj = OASParserUtil.generateOASConfigForEndpoints(api, false);
        if (sandEndpointObj != null) {
            openAPI.addExtension(APISpecParserConstants.X_WSO2_SANDBOX_ENDPOINTS, sandEndpointObj);
        }
        openAPI.addExtension(APISpecParserConstants.X_WSO2_BASEPATH, api.getContext());
        if (api.getTransports() != null) {
            openAPI.addExtension(APISpecParserConstants.X_WSO2_TRANSPORTS, api.getTransports().split(","));
        }
        String apiSecurity = api.getApiSecurity();
        // set mutual ssl extension if enabled
        if (apiSecurity != null) {
            List<String> securityList = Arrays.asList(apiSecurity.split(","));
            if (securityList.contains(APISpecParserConstants.API_SECURITY_MUTUAL_SSL)) {
                String mutualSSLOptional = !securityList.contains(APISpecParserConstants.API_SECURITY_MUTUAL_SSL_MANDATORY) ?
                        APISpecParserConstants.OPTIONAL : APISpecParserConstants.MANDATORY;
                openAPI.addExtension(APISpecParserConstants.X_WSO2_MUTUAL_SSL, mutualSSLOptional);
            }
        }
        // This app security should be given in both root level and resource level,
        // otherwise the default oauth2 scheme defined at each resource level will override application securities
        JsonNode appSecurityExtension = OASParserUtil.getAppSecurity(apiSecurity);
        if (openAPI.getExtensions() != null && !(openAPI.getExtensions()
                .containsKey(APISpecParserConstants.X_WSO2_APP_SECURITY))) {
            openAPI.addExtension(APISpecParserConstants.X_WSO2_APP_SECURITY, appSecurityExtension);
        }
        for (String pathKey : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(pathKey);
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                Operation operation = entry.getValue();
                operation.addExtension(APISpecParserConstants.X_WSO2_APP_SECURITY, appSecurityExtension);
            }
        }
        openAPI.addExtension(APISpecParserConstants.X_WSO2_RESPONSE_CACHE,
                OASParserUtil.getResponseCacheConfig(api.getResponseCache(), api.getCacheTimeout()));
        return prettifyOAS3ToJson(openAPI);
    }

    @Override
    public String getOASVersion(String oasDefinition) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(oasDefinition);
        return openAPI.getInfo().getVersion();
    }

    /**
     * Gets a list of scopes using the security requirements
     *
     * @param oauth2SchemeKey OAuth2 security element key
     * @param operation       Swagger path operation
     * @return list of scopes using the security requirements
     */
    private List<String> getScopeOfOperations(String oauth2SchemeKey, Operation operation) {
        List<SecurityRequirement> security = operation.getSecurity();
        if (security != null) {
            for (Map<String, List<String>> requirement : security) {
                if (requirement.get(oauth2SchemeKey) != null) {
                    return requirement.get(oauth2SchemeKey);
                }
            }
        }
        return getScopeOfOperationsFromExtensions(operation);
    }

    /**
     * Get scope of operation
     *
     * @param operation
     * @return
     */
    private List<String> getScopeOfOperationsFromExtensions(Operation operation) {

        Map<String, Object> extensions = operation.getExtensions();
        if (extensions != null && extensions.containsKey(APISpecParserConstants.SWAGGER_X_SCOPE)) {
            String scopeKey = (String) extensions.get(APISpecParserConstants.SWAGGER_X_SCOPE);
            return Stream.of(scopeKey.split(",")).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Get scope information from the extensions
     *
     * @param openAPI openAPI object
     * @return Scope set
     * @throws APIManagementException if an error occurred
     */
    private Set<Scope> getScopesFromExtensions(OpenAPI openAPI) throws APIManagementException {
        Set<Scope> scopeList = new LinkedHashSet<>();
        Map<String, Object> extensions = openAPI.getExtensions();
        if (extensions != null && extensions.containsKey(APISpecParserConstants.SWAGGER_X_WSO2_SECURITY)) {
            Map<String, Object> securityDefinitions =
                    (Map<String, Object>) extensions.get(APISpecParserConstants.SWAGGER_X_WSO2_SECURITY);
            for (Map.Entry<String, Object> entry : securityDefinitions.entrySet()) {
                Map<String, Object> securityDefinition = (Map<String, Object>) entry.getValue();
                if (securityDefinition.containsKey(APISpecParserConstants.SWAGGER_X_WSO2_SCOPES)) {
                    List<Map<String, String>> oauthScope =
                            (List<Map<String, String>>) securityDefinition.get(APISpecParserConstants.SWAGGER_X_WSO2_SCOPES);
                    for (Map<String, String> anOauthScope : oauthScope) {
                        Scope scope = new Scope();
                        scope.setKey(anOauthScope.get(APISpecParserConstants.SWAGGER_SCOPE_KEY));
                        scope.setName(anOauthScope.get(APISpecParserConstants.SWAGGER_NAME));
                        scope.setDescription(anOauthScope.get(APISpecParserConstants.SWAGGER_DESCRIPTION));
                        scope.setRoles(anOauthScope.get(APISpecParserConstants.SWAGGER_ROLES));
                        scopeList.add(scope);
                    }
                }
            }
        }
        return scopeList;
    }

    /**
     * Include Scope details to the definition
     *
     * @param openAPI     openapi definition
     * @param swaggerData Swagger related API data
     */
    private void updateSwaggerSecurityDefinition(OpenAPI openAPI, SwaggerData swaggerData, String authUrl,
            KeyManagerConfigurationDTO keyManagerConfigurationDTO) {

        if (keyManagerConfigurationDTO == null || StringUtils.isEmpty(keyManagerConfigurationDTO.getUuid())) {
            if (openAPI.getComponents() == null) {
                openAPI.setComponents(new Components());
            }
            Map<String, SecurityScheme> securitySchemes = openAPI.getComponents().getSecuritySchemes();
            if (securitySchemes == null) {
                securitySchemes = new HashMap<>();
                openAPI.getComponents().setSecuritySchemes(securitySchemes);
            }
            SecurityScheme securityScheme = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY);
            if (securityScheme == null) {
                securityScheme = new SecurityScheme();
                securityScheme.setType(SecurityScheme.Type.OAUTH2);
                securitySchemes.put(OPENAPI_SECURITY_SCHEMA_KEY, securityScheme);
                List<SecurityRequirement> security = new ArrayList<SecurityRequirement>();
                SecurityRequirement secReq = new SecurityRequirement();
                secReq.addList(OPENAPI_SECURITY_SCHEMA_KEY, new ArrayList<String>());
                security.add(secReq);
                openAPI.setSecurity(security);
            }
            if (securityScheme.getFlows() == null) {
                securityScheme.setFlows(new OAuthFlows());
            }
            OAuthFlow oAuthFlow = securityScheme.getFlows().getImplicit();
            if (oAuthFlow == null) {
                oAuthFlow = new OAuthFlow();
                securityScheme.getFlows().setImplicit(oAuthFlow);
            }
            if (oAuthFlow.getAuthorizationUrl() == null) {
                oAuthFlow.setAuthorizationUrl(authUrl);
            }
            setScopesToOAuthFlow(oAuthFlow, swaggerData);
        } else {
            addSecuritySchemeToOpenAPI(openAPI, keyManagerConfigurationDTO, authUrl, swaggerData);
        }
    }

    /**
     * Add scopes for OAuth flow
     *
     * @param oAuthFlow     existing oauthFlow object
     * @param swaggerData   Swagger related API data
     */
    private void setScopesToOAuthFlow(OAuthFlow oAuthFlow, SwaggerData swaggerData){

        Scopes oas3Scopes = new Scopes();
        Set<Scope> scopes = swaggerData.getScopes();
        if (scopes != null && !scopes.isEmpty()) {
            Map<String, String> scopeBindings = new HashMap<>();
            for (Scope scope : scopes) {
                String description = scope.getDescription() != null ? scope.getDescription() : "";
                oas3Scopes.put(scope.getKey(), description);
                String roles = (StringUtils.isNotBlank(scope.getRoles())
                        && scope.getRoles().trim().split(",").length > 0) ? scope.getRoles() : StringUtils.EMPTY;
                scopeBindings.put(scope.getKey(), roles);
            }
            oAuthFlow.addExtension(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
        }
        oAuthFlow.setScopes(oas3Scopes);
    }

    /**
     * Set security schema with the information from key manger configurations
     *
     * @param openAPI           OpenAPI spec
     * @param keyManagerConfig  Key manager information
     * @param authUrl           Default authorization url for the value not existing cases
     */
    private void addSecuritySchemeToOpenAPI(OpenAPI openAPI, KeyManagerConfigurationDTO keyManagerConfig,
            String authUrl, SwaggerData swaggerData) {

        if (openAPI.getComponents() == null) {
            openAPI.setComponents(new Components());
        }

        Map<String, SecurityScheme> securitySchemes = openAPI.getComponents().getSecuritySchemes();
        if (securitySchemes != null && securitySchemes.containsKey(OPENAPI_SECURITY_SCHEMA_KEY)) {
            // Remove the existing default security scheme
            securitySchemes.remove(OPENAPI_SECURITY_SCHEMA_KEY);
        }

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(generateOAuthFlows(keyManagerConfig, authUrl, swaggerData));

        // Add the security scheme to components using key manager type as the key
        openAPI.getComponents().addSecuritySchemes(keyManagerConfig.getType(), securityScheme);
    }

    /**
     * Add the key manager provided flows supported by OAS3
     *
     * @param keyManagerConfig  Key manager information
     * @param authUrl           Default authorization url for the value not existing cases
     * @return OAuthFlows object with generated flows
     */
    private OAuthFlows generateOAuthFlows(KeyManagerConfigurationDTO keyManagerConfig, String authUrl,
            SwaggerData swaggerData) {
        OAuthFlows oAuthFlows = new OAuthFlows();
        List<String> grantTypes = (List<String>) keyManagerConfig.getAdditionalProperties().get("grant_types");

        if (Objects.nonNull(grantTypes)) {
            String tokenEP = null;
            String authorizeEP = null;
            if (keyManagerConfig.getAdditionalProperties() != null) {
                // To keep tokenEP and authorizeEP remains null if the values get null when retrieving
                tokenEP = Objects.toString(
                        keyManagerConfig.getAdditionalProperties().get(APISpecParserConstants.KeyManager.TOKEN_ENDPOINT), "");
                authorizeEP = Objects.toString(
                        keyManagerConfig.getAdditionalProperties().get(APISpecParserConstants.KeyManager.AUTHORIZE_ENDPOINT), "");
            }
            // This will generate only supported flows by OAS3
            for (String grantType : grantTypes) {
                OAuthFlow flow = new OAuthFlow();
                if (APISpecParserConstants.KeyManager.AUTHORIZATION_CODE_GRANT_TYPE.equals(grantType) && !StringUtils.isEmpty(
                        tokenEP)) {
                    configureAuthorizationCodeFlow(flow, authUrl, authorizeEP, tokenEP);
                    setScopesToOAuthFlow(flow, swaggerData);
                    oAuthFlows.authorizationCode(flow);
                } else if (APISpecParserConstants.KeyManager.IMPLICIT_GRANT_TYPE.equals(grantType)) {
                    configureImplicitFlow(flow, authUrl, authorizeEP);
                    setScopesToOAuthFlow(flow, swaggerData);
                    oAuthFlows.implicit(flow);
                } else if (APISpecParserConstants.KeyManager.PASSWORD_GRANT_TYPE.equals(grantType) && !StringUtils.isEmpty(
                        tokenEP)) {
                    configurePasswordFlow(flow, tokenEP);
                    setScopesToOAuthFlow(flow, swaggerData);
                    oAuthFlows.password(flow);
                } else if (APISpecParserConstants.KeyManager.CLIENT_CREDENTIALS_GRANT_TYPE.equals(grantType)
                        && !StringUtils.isEmpty(tokenEP)) {
                    configureClientCredentialsFlow(flow, tokenEP);
                    setScopesToOAuthFlow(flow, swaggerData);
                    oAuthFlows.clientCredentials(flow);
                }
            }
        }
        return oAuthFlows;
    }

    /**
     * set authorization code flow information to the flow
     *
     * @param flow              flow of adding the information
     * @param authUrl           Default authorization url for the value not existing cases
     * @param authorizeEP       authorization endpoint url
     * @param tokenEP           token endpoint url
     */
    private void configureAuthorizationCodeFlow(OAuthFlow flow, String authUrl, String authorizeEP, String tokenEP) {
        if (!StringUtils.isEmpty(authorizeEP)) {
            flow.setAuthorizationUrl(authorizeEP);
        } else {
            flow.setAuthorizationUrl(authUrl);
        }
        flow.setTokenUrl(tokenEP);
    }

    /**
     * set implicit flow information to the flow
     *
     * @param flow              flow of adding the information
     * @param authUrl           Default authorization url for the value not existing cases
     * @param authorizeEP       authorization endpoint url
     */
    private void configureImplicitFlow(OAuthFlow flow, String authUrl, String authorizeEP) {
        if (!StringUtils.isEmpty(authorizeEP)) {
            flow.setAuthorizationUrl(authorizeEP);
        } else {
            flow.setAuthorizationUrl(authUrl);
        }
    }

    /**
     * set password flow information to the flow
     *
     * @param flow              flow of adding the information
     * @param tokenEP           token endpoint url
     */
    private void configurePasswordFlow(OAuthFlow flow, String tokenEP) {
        flow.setTokenUrl(tokenEP);
    }

    /**
     * set client credentials flow information to the flow
     *
     * @param flow              flow of adding the information
     * @param tokenEP           token endpoint url
     */
    private void configureClientCredentialsFlow(OAuthFlow flow, String tokenEP) {
        flow.setTokenUrl(tokenEP);
    }

    /**
     * Remove legacy scope from swagger
     *
     * @param openAPI
     */
    private void updateLegacyScopesFromSwagger(OpenAPI openAPI, SwaggerData swaggerData) {

        Map<String, Object> extensions = openAPI.getExtensions();
        if (extensions != null && extensions.containsKey(APISpecParserConstants.SWAGGER_X_WSO2_SECURITY)) {
            extensions.remove(APISpecParserConstants.SWAGGER_X_WSO2_SECURITY);
        }
    }

    /**
     * Add a new path based on the provided URI template to swagger if it does not exists. If it exists,
     * adds the respective operation to the existing path
     *
     * @param openAPI  swagger object
     * @param resource API resource data
     */
    private void addOrUpdatePathToSwagger(OpenAPI openAPI, SwaggerData.Resource resource) {
        PathItem path;
        if (openAPI.getPaths() == null) {
            openAPI.setPaths(new Paths());
        }
        if (openAPI.getPaths().get(resource.getPath()) != null) {
            path = openAPI.getPaths().get(resource.getPath());
        } else {
            path = new PathItem();
        }

        Operation operation = createOperation(resource);
        PathItem.HttpMethod httpMethod = PathItem.HttpMethod.valueOf(resource.getVerb());
        path.operation(httpMethod, operation);
        openAPI.getPaths().addPathItem(resource.getPath(), path);
    }

    /**
     * Creates a new operation object using the URI template object
     *
     * @param resource API resource data
     * @return a new operation object using the URI template object
     */
    private Operation createOperation(SwaggerData.Resource resource) {
        Operation operation = new Operation();
        populatePathParameters(operation, resource.getPath());
        updateOperationManagedInfo(resource, operation);

        ApiResponses apiResponses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.description("OK");
        apiResponses.addApiResponse(APISpecParserConstants.SWAGGER_RESPONSE_200, apiResponse);
        operation.setResponses(apiResponses);
        return operation;
    }

    /**
     * Updates managed info of a provided operation such as auth type and throttling
     *
     * @param resource  API resource data
     * @param operation swagger operation
     */
    private void updateOperationManagedInfo(SwaggerData.Resource resource, Operation operation) {
        String authType = resource.getAuthType();
        if (APISpecParserConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN.equals(authType) || authType == null) {
            authType = "Application & Application User";
        }
        if (APISpecParserConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authType)) {
            authType = "Application User";
        }
        if (APISpecParserConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authType)) {
            authType = "Application";
        }
        operation.addExtension(APISpecParserConstants.SWAGGER_X_AUTH_TYPE, authType);
        if (resource.getPolicy() != null) {
            operation.addExtension(APISpecParserConstants.SWAGGER_X_THROTTLING_TIER, resource.getPolicy());
        } else {
            operation.addExtension(APISpecParserConstants.SWAGGER_X_THROTTLING_TIER, APISpecParserConstants.DEFAULT_API_POLICY_UNLIMITED);
        }
        // AWS Lambda: set arn & timeout to swagger
        if (resource.getAmznResourceName() != null) {
            operation.addExtension(APISpecParserConstants.SWAGGER_X_AMZN_RESOURCE_NAME, resource.getAmznResourceName());
            if (resource.isAmznResourceContentEncoded()) {
                operation.addExtension(APISpecParserConstants.SWAGGER_X_AMZN_RESOURCE_CONTENT_ENCODED,
                        resource.isAmznResourceContentEncoded());
            }
        }
        if (resource.getAmznResourceTimeout() != 0) {
            operation.addExtension(APISpecParserConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT, resource.getAmznResourceTimeout());
        }
        updateLegacyScopesFromOperation(resource, operation);
        List<SecurityRequirement> security = operation.getSecurity();
        if (security == null) {
            security = new ArrayList<>();
            operation.setSecurity(security);
        }
        for (Map<String, List<String>> requirement : security) {
            if (requirement.get(OPENAPI_SECURITY_SCHEMA_KEY) != null) {

                if (resource.getScopes().isEmpty()) {
                    requirement.put(OPENAPI_SECURITY_SCHEMA_KEY, Collections.EMPTY_LIST);
                } else {
                    requirement.put(OPENAPI_SECURITY_SCHEMA_KEY, resource.getScopes().stream().map(Scope::getKey)
                            .collect(Collectors.toList()));
                }
                return;
            }
        }
        // if oauth2SchemeKey not present, add a new
        SecurityRequirement defaultRequirement = new SecurityRequirement();
        if (resource.getScopes().isEmpty()) {
            defaultRequirement.put(OPENAPI_SECURITY_SCHEMA_KEY, Collections.EMPTY_LIST);
        } else {
            defaultRequirement.put(OPENAPI_SECURITY_SCHEMA_KEY, resource.getScopes().stream().map(Scope::getKey)
                    .collect(Collectors.toList()));
        }
        security.add(defaultRequirement);
    }

    /**
     * Remove legacy scope information from swagger operation.
     *
     * @param resource  Given Resource in the input
     * @param operation Operation in APIDefinition
     */
    private void updateLegacyScopesFromOperation(SwaggerData.Resource resource, Operation operation) {

        Map<String, Object> extensions = operation.getExtensions();
        if (extensions != null && extensions.containsKey(APISpecParserConstants.SWAGGER_X_SCOPE)) {
            extensions.remove(APISpecParserConstants.SWAGGER_X_SCOPE);
        }
    }

    /**
     * Update OAS definition with authorization endpoints.
     *
     * @param openAPI                    OpenAPI
     * @param swaggerData                SwaggerData
     * @param hostsWithSchemes           GW hosts with protocols
     * @param keyManagerConfigurationDTO configuration details of the Key Manager
     * @return updated OAS definition
     */
    private String updateSwaggerSecurityDefinitionForStore(OpenAPI openAPI, SwaggerData swaggerData,
                                                           Map<String, String> hostsWithSchemes,
                                                           KeyManagerConfigurationDTO keyManagerConfigurationDTO) {
        String authUrl;
        // By Default, add the GW host with HTTPS protocol if present.
        if (hostsWithSchemes.containsKey(APISpecParserConstants.HTTPS_PROTOCOL)) {
            authUrl = (hostsWithSchemes.get(APISpecParserConstants.HTTPS_PROTOCOL)).concat("/authorize");
        } else {
            authUrl = (hostsWithSchemes.get(APISpecParserConstants.HTTP_PROTOCOL)).concat("/authorize");
        }
        updateSwaggerSecurityDefinitionForStore(openAPI, swaggerData, authUrl, keyManagerConfigurationDTO);
        return prettifyOAS3ToJson(openAPI);
    }

    /**
     * Update Swagger security definition for dev portal only.
     *
     * @param openAPI     OpenAPI
     * @param swaggerData SwaggerData
     * @param authUrl     Authorization URL
     */
    private void updateSwaggerSecurityDefinitionForStore(OpenAPI openAPI, SwaggerData swaggerData, String authUrl,
            KeyManagerConfigurationDTO keyManagerConfigurationDTO) {

        if (openAPI.getComponents() == null)
            openAPI.setComponents(new Components());
        // Get the security defined for the current API.
        List<String> secList = swaggerData.getSecurity() != null ?
                Arrays.asList(swaggerData.getSecurity().split(",")) :
                new ArrayList<>();
        // Get the security schemes defined in the OAS definition.
        Map<String, SecurityScheme> securitySchemes = openAPI.getComponents().getSecuritySchemes();
        if (keyManagerConfigurationDTO == null || StringUtils.isEmpty(keyManagerConfigurationDTO.getUuid())) {
            if (securitySchemes == null) {
                // If no security schemes defined, create a new map.
                securitySchemes = new HashMap<>();
                openAPI.getComponents().setSecuritySchemes(securitySchemes);
            }
            List<SecurityRequirement> security = new ArrayList<>(); // Override with new global security requirements.
            openAPI.setSecurity(security);
            // If the security in API is empty or default oauth, add oauth2 security to the OAS definition.
            if (secList.isEmpty() || secList.contains(APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2)) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Updating the OAS definition with default oauth2 security of API: " + swaggerData.getTitle()
                                    + " Version: " + swaggerData.getVersion());
                }
                // Add oauth to global security requirement to the OAS definition.
                OASParserUtil.addSecurityRequirementToSwagger(openAPI, OPENAPI_SECURITY_SCHEMA_KEY);
                // If default oauth type security scheme in the OAS definition, add it.
                SecurityScheme securityScheme = securitySchemes.computeIfAbsent(OPENAPI_SECURITY_SCHEMA_KEY, key -> {
                    SecurityScheme newOAuthScheme = new SecurityScheme();
                    newOAuthScheme.setType(SecurityScheme.Type.OAUTH2);
                    return newOAuthScheme;
                });
                if (securityScheme.getFlows() == null) { // If no flows defined, create a new one.
                    securityScheme.setFlows(new OAuthFlows());
                }
                OAuthFlow oAuthFlow = securityScheme.getFlows().getImplicit();
                if (oAuthFlow == null) {    // If no implicit flow defined, create a new one.
                    oAuthFlow = new OAuthFlow();
                    securityScheme.getFlows().setImplicit(oAuthFlow);
                }
                // rewrite the authorization url if the authorization url is not empty.
                oAuthFlow.setAuthorizationUrl(authUrl);
                // Set the scopes defined in the API to the OAS definition.
                OASParserUtil.setScopesFromAPIToSecurityScheme(swaggerData, securityScheme);
            }
        } else {
            addSecuritySchemeToOpenAPI(openAPI, keyManagerConfigurationDTO, authUrl, swaggerData);
        }
        // If the Basic Auth security is in API, add basic security to the OAS definition.
        if (secList.contains(APISpecParserConstants.API_SECURITY_BASIC_AUTH)) {
            if (log.isDebugEnabled()) {
                log.debug("Updating the OAS definition with basic_auth security of API: " + swaggerData.getTitle()
                        + " Version: " + swaggerData.getVersion());
            }
            SecurityScheme securityScheme = securitySchemes.computeIfAbsent(APISpecParserConstants.API_SECURITY_BASIC_AUTH,
                    key -> {
                        SecurityScheme scheme = new SecurityScheme();
                        scheme.setType(SecurityScheme.Type.HTTP);
                        scheme.setScheme(APISpecParserConstants.SWAGGER_API_SECURITY_BASIC_AUTH_TYPE);
                        return scheme;
                    });
            // Set the scopes defined in the API to the OAS definition.
            OASParserUtil.setScopesFromAPIToSecurityScheme(swaggerData, securityScheme);
            // Add global basic security requirement to the OAS definition.
            OASParserUtil.addSecurityRequirementToSwagger(openAPI, APISpecParserConstants.API_SECURITY_BASIC_AUTH);
        }
        if (secList.contains(APISpecParserConstants.API_SECURITY_API_KEY)) {
            if (log.isDebugEnabled()) {
                log.debug("Updating the OAS definition with api_key security of API: " + swaggerData.getTitle()
                        + " Version: " + swaggerData.getVersion());
            }
            securitySchemes.computeIfAbsent(APISpecParserConstants.API_SECURITY_API_KEY,
                    key -> {
                        SecurityScheme scheme = new SecurityScheme();
                        scheme.setType(SecurityScheme.Type.APIKEY);
                        scheme.setIn(SecurityScheme.In.HEADER);
                        scheme.setName(APISpecParserConstants.API_KEY_HEADER_QUERY_PARAM);
                        return scheme;
                    });
            // Add global api key security requirement to the OAS definition.
            OASParserUtil.addSecurityRequirementToSwagger(openAPI, APISpecParserConstants.API_SECURITY_API_KEY);
        }
        // Add requirement with scopes to the operations in OAS definition.
        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            for (Operation operation : pathEntry.getValue().readOperations()) {
                List<SecurityRequirement> oldSecList = operation.getSecurity();
                if (oldSecList == null) {
                    oldSecList = new ArrayList<>();
                }
                List<String> operationScopes = oldSecList.stream()
                        .filter(securityRequirement -> securityRequirement.containsKey(OPENAPI_SECURITY_SCHEMA_KEY))
                        .findFirst()
                        .map(securityRequirement -> securityRequirement.get(OPENAPI_SECURITY_SCHEMA_KEY))
                        .orElse(new ArrayList<>());
                // Add operation level security for basic_auth and api_key.
                OASParserUtil.addOASBasicAuthResourceScopesFromAPI(operationScopes, secList, operation);
                OASParserUtil.addOASOperationSecurityReqFromAPI(oldSecList, secList,
                        APISpecParserConstants.API_SECURITY_BASIC_AUTH, new ArrayList<>());
                OASParserUtil.addOASOperationSecurityReqFromAPI(oldSecList, secList, APISpecParserConstants.API_SECURITY_API_KEY,
                        new ArrayList<>());
                if (!secList.isEmpty() && !secList.contains(APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2)
                        && operation.getSecurity() != null) {
                    // If oauth2 is not set for the API, remove oauth security scheme from resource level if exists.
                    operation.setSecurity(operation.getSecurity().stream()
                            .filter(securityRequirement -> !securityRequirement
                                    .containsKey(OPENAPI_SECURITY_SCHEMA_KEY))
                            .collect(Collectors.toList()));
                }
            }
        }
        if (!secList.isEmpty() && !secList.contains(APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2)) {
            if (log.isDebugEnabled()) {
                log.debug("Removing default oauth2 security of API: " + swaggerData.getTitle()
                        + " Version: " + swaggerData.getVersion() + " from OAS definition");
            }
            // Remove oauth security scheme from global level and resource level if exists
            securitySchemes.remove(OPENAPI_SECURITY_SCHEMA_KEY);
        }
    }

    /**
     * Update OAS definition with GW endpoints
     *
     * @param product           APIProduct
     * @param hostsWithSchemes  GW hosts with protocol mapping
     * @param openAPI           OpenAPI
     */
    private void updateEndpoints(APIProduct product, Map<String, String> hostsWithSchemes, OpenAPI openAPI) {

        String basePath = product.getContext();
        String transports = product.getTransports();
        updateEndpoints(openAPI, basePath, transports, hostsWithSchemes, null);
    }

    /**
     * Update OAS definition with GW endpoints
     *
     * @param api               API
     * @param hostsWithSchemes  GW hosts with protocol mapping
     * @param openAPI           OpenAPI
     */
    private void updateEndpoints(API api, Map<String, String> hostsWithSchemes, OpenAPI openAPI) {

        String basePath = api.getContext();
        String transports = api.getTransports();
        updateEndpoints(openAPI, basePath, transports, hostsWithSchemes, api);
    }

    /**
     * Update OAS definition with GW endpoints and API information
     *
     * @param openAPI          OpenAPI
     * @param basePath         API context
     * @param transports       transports types
     * @param hostsWithSchemes GW hosts with protocol mapping
     * @param api              API
     */
    private void updateEndpoints(OpenAPI openAPI, String basePath, String transports,
                                 Map<String, String> hostsWithSchemes, API api) {
        List<Server> servers = new ArrayList<>();
        if (api != null && api.isAdvertiseOnly()) {
            String externalProductionEndpoint = api.getApiExternalProductionEndpoint();
            String externalSandboxEndpoint = api.getApiExternalSandboxEndpoint();
            if (externalProductionEndpoint != null) {
                Server externalProductionServer = new Server();
                externalProductionServer.setUrl(externalProductionEndpoint);
                servers.add(externalProductionServer);
            }
            if (externalSandboxEndpoint != null) {
                Server externalSandboxServer = new Server();
                externalSandboxServer.setUrl(externalSandboxEndpoint);
                servers.add(externalSandboxServer);
            }
            openAPI.setServers(servers);
        } else {
            String[] apiTransports = transports.split(",");
            if (ArrayUtils.contains(apiTransports, APISpecParserConstants.HTTPS_PROTOCOL) && hostsWithSchemes
                    .containsKey(APISpecParserConstants.HTTPS_PROTOCOL)) {
                String host = hostsWithSchemes.get(APISpecParserConstants.HTTPS_PROTOCOL).trim()
                        .replace(APISpecParserConstants.HTTPS_PROTOCOL_URL_PREFIX, "");
                String httpsURL = APISpecParserConstants.HTTPS_PROTOCOL + "://" + host + basePath;
                Server httpsServer = new Server();
                httpsServer.setUrl(httpsURL);
                servers.add(httpsServer);
            }
            if (ArrayUtils.contains(apiTransports, APISpecParserConstants.HTTP_PROTOCOL) && hostsWithSchemes
                    .containsKey(APISpecParserConstants.HTTP_PROTOCOL)) {
                String host = hostsWithSchemes.get(APISpecParserConstants.HTTP_PROTOCOL).trim()
                        .replace(APISpecParserConstants.HTTP_PROTOCOL_URL_PREFIX, "");
                String httpURL = APISpecParserConstants.HTTP_PROTOCOL + "://" + host + basePath;
                Server httpsServer = new Server();
                httpsServer.setUrl(httpURL);
                servers.add(httpsServer);
            }
            openAPI.setServers(servers);
        }
    }

    /**
     * Update OAS operations for Store
     *
     * @param openAPI OpenAPI to be updated
     */
    private void updateOperations(OpenAPI openAPI) {
        for (String pathKey : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(pathKey);
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                Operation operation = entry.getValue();
                Map<String, Object> extensions = operation.getExtensions();
                if (extensions != null) {
                    // remove mediation extension
                    if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_MEDIATION_SCRIPT)) {
                        extensions.remove(APISpecParserConstants.SWAGGER_X_MEDIATION_SCRIPT);
                    }
                    // set x-scope value to security definition if it not there.
                    if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_WSO2_SCOPES)) {
                        String scope = (String) extensions.get(APISpecParserConstants.SWAGGER_X_WSO2_SCOPES);
                        List<SecurityRequirement> security = operation.getSecurity();
                        if (security == null) {
                            security = new ArrayList<>();
                            operation.setSecurity(security);
                        }
                        for (Map<String, List<String>> requirement : security) {
                            if (requirement.get(OPENAPI_SECURITY_SCHEMA_KEY) == null || !requirement
                                    .get(OPENAPI_SECURITY_SCHEMA_KEY).contains(scope)) {
                                requirement.put(OPENAPI_SECURITY_SCHEMA_KEY, Collections.singletonList(scope));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get parsed OpenAPI object
     *
     * @param oasDefinition OAS definition
     * @return OpenAPI
     */
    OpenAPI getOpenAPI(String oasDefinition) {
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(oasDefinition, null, null);
        if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
            log.debug("Errors found when parsing OAS definition");
        }
        return parseAttemptForV3.getOpenAPI();
    }

    /**
     * Get parsed OpenAPI object with options
     *
     * @param oasDefinition OAS definition
     * @param options       OAS parser options
     * @return OpenAPI
     */
    OpenAPI getOpenAPI(String oasDefinition, OASParserOptions options) {
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        ParseOptions parserOptions = options != null ? convertOptionsToParseOptions(options) : new ParseOptions();
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(oasDefinition, null, parserOptions);
        if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
            log.debug("Errors found when parsing OAS definition");
        }
        return parseAttemptForV3.getOpenAPI();
    }

    private ParseOptions convertOptionsToParseOptions(OASParserOptions options) {
        ParseOptions parserOptions = new ParseOptions();
        parserOptions.setExplicitStyleAndExplode(options.isExplicitStyleAndExplode());
        return parserOptions;
    }

    /**
     * Construct openAPI definition for graphQL. Add get and post operations
     *
     * @param openAPI OpenAPI
     * @return modified openAPI for GraphQL
     */
    private void modifyGraphQLSwagger(OpenAPI openAPI) {
        SwaggerData.Resource resource = new SwaggerData.Resource();
        resource.setAuthType(APISpecParserConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
        resource.setPolicy(APISpecParserConstants.DEFAULT_SUB_POLICY_UNLIMITED);
        resource.setPath("/");
        resource.setVerb(APISpecParserConstants.HTTP_POST);
        Operation postOperation = createOperation(resource);

        //post operation
        RequestBody requestBody = new RequestBody();
        requestBody.setDescription("Query or mutation to be passed to graphQL API");
        requestBody.setRequired(true);

        JSONObject typeOfPayload = new JSONObject();
        JSONObject payload = new JSONObject();
        typeOfPayload.put(APISpecParserConstants.TYPE, APISpecParserConstants.STRING);
        payload.put(APISpecParserConstants.OperationParameter.PAYLOAD_PARAM_NAME, typeOfPayload);

        Schema postSchema = new Schema();
        postSchema.setType(APISpecParserConstants.OBJECT);
        postSchema.setProperties(payload);

        MediaType mediaType = new MediaType();
        mediaType.setSchema(postSchema);

        Content content = new Content();
        content.addMediaType(APPLICATION_JSON_MEDIA_TYPE, mediaType);
        requestBody.setContent(content);
        postOperation.setRequestBody(requestBody);

        //add post and get operations to path /*
        PathItem pathItem = new PathItem();
        pathItem.setPost(postOperation);
        Paths paths = new Paths();
        paths.put("/", pathItem);

        openAPI.setPaths(paths);
    }

    @Override
    public String getOASDefinitionWithTierContentAwareProperty(String oasDefinition, List<String> contentAwareTiersList,
            String apiLevelTier) throws APIManagementException {
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(oasDefinition, null, null);
        if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
            log.debug("Errors found when parsing OAS definition");
        }
        OpenAPI swagger = parseAttemptForV3.getOpenAPI();
        // check if API Level tier is content aware. if so, we set a extension as a global property
        if (contentAwareTiersList.contains(apiLevelTier)) {
            swagger.addExtension(APISpecParserConstants.SWAGGER_X_THROTTLING_BANDWIDTH, true);
            // no need to check resource levels since both cannot exist at the same time.
            log.debug("API Level policy is content aware..");
            return prettifyOAS3ToJson(swagger);
        }
        // if api level tier exists, skip checking for resource level tiers since both cannot exist at the same time.
        if (apiLevelTier != null) {
            log.debug("API Level policy is not content aware..");
            return oasDefinition;
        } else {
            log.debug("API Level policy does not exist. Checking for resource level");
            for (Map.Entry<String, PathItem> entry : swagger.getPaths().entrySet()) {
                String path = entry.getKey();
                List<Operation> operations = swagger.getPaths().get(path).readOperations();
                for (Operation op : operations) {
                    if (contentAwareTiersList
                            .contains(op.getExtensions().get(APISpecParserConstants.SWAGGER_X_THROTTLING_TIER))) {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    "API resource Level policy is content aware for operation " + op.getOperationId());
                        }
                        op.addExtension(APISpecParserConstants.SWAGGER_X_THROTTLING_BANDWIDTH, true);
                    }
                }
            }
            return prettifyOAS3ToJson(swagger);
        }
    }

    /**
     * This method returns the boolean value which checks whether the swagger is included default security scheme or not
     *
     * @param swaggerContent resource json
     * @return boolean
     * @throws APIManagementException
     */
    private boolean isDefaultGiven(String swaggerContent) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent);

        Components components = openAPI.getComponents();
        if (components == null) {
            return false;
        }
        Map<String, SecurityScheme> securitySchemes = components.getSecuritySchemes();
        if (securitySchemes == null) {
            return false;
        }
        SecurityScheme checkDefault = openAPI.getComponents().getSecuritySchemes().get(OPENAPI_SECURITY_SCHEMA_KEY);
        if (checkDefault == null) {
            return false;
        }
        return true;
    }

    /**
     * This method will inject scopes of other schemes to the swagger definition
     *
     * @param swaggerContent resource json
     * @return String
     * @throws APIManagementException
     */
    @Override
    @Deprecated
    public String processOtherSchemeScopes(String swaggerContent) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent);
        return processOtherSchemeScopesCore(swaggerContent, openAPI);
    }

    /**
     * This method will inject scopes of other schemes to the swagger definition with options
     *
     * @param swaggerContent resource json
     * @param options        OAS parser options
     * @return String
     * @throws APIManagementException
     */
    @Override
    public String processOtherSchemeScopes(String swaggerContent, OASParserOptions options) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent, options);
        return processOtherSchemeScopesCore(swaggerContent, openAPI);
    }

    private String processOtherSchemeScopesCore(String swaggerContent, OpenAPI openAPI) throws APIManagementException {
        Set<Scope> legacyScopes = getScopesFromExtensions(openAPI);

        //In case default scheme already exists we check whether the legacy x-wso2-scopes are there in the default scheme
        //If not we proceed to process legacy scopes to make sure old local scopes work in migrated pack too.
        //This is to fix https://github.com/wso2/product-apim/issues/8724
        if (isDefaultGiven(swaggerContent) && !legacyScopes.isEmpty()) {
            SecurityScheme defaultScheme = openAPI.getComponents().getSecuritySchemes()
                    .get(OPENAPI_SECURITY_SCHEMA_KEY);
            if (defaultScheme != null && defaultScheme.getFlows() != null) {
                OAuthFlows flows = defaultScheme.getFlows();
                OAuthFlow oAuthFlow = null;
                if (flows.getImplicit() != null || flows.getAuthorizationCode() != null) {
                    oAuthFlow =
                            flows.getImplicit() != null ? flows.getImplicit() : flows.getAuthorizationCode();
                    String authUrl = oAuthFlow.getAuthorizationUrl();
                    if (StringUtils.isBlank(authUrl)) {
                        oAuthFlow.setAuthorizationUrl(OPENAPI_DEFAULT_AUTHORIZATION_URL);
                    }
                } else if (flows.getClientCredentials() != null) {
                    oAuthFlow = flows.getClientCredentials();
                } else if (flows.getPassword() != null) {
                    oAuthFlow = flows.getPassword();
                }
                if (oAuthFlow != null) {
                    Scopes defaultScopes = oAuthFlow.getScopes();
                    if (defaultScopes != null) {
                        for (Scope legacyScope : legacyScopes) {
                            if (!defaultScopes.containsKey(legacyScope.getKey())) {
                                openAPI = processLegacyScopes(openAPI);
                                return prettifyOAS3ToJson(openAPI);
                            }
                        }
                    }
                }
            }
        }

        if (!isDefaultGiven(swaggerContent)) {
            openAPI = processLegacyScopes(openAPI);
            openAPI = injectOtherScopesToDefaultScheme(openAPI);
            openAPI = injectOtherResourceScopesToDefaultScheme(openAPI);
            return prettifyOAS3ToJson(openAPI);
        }
        return swaggerContent;
    }

    /**
     * This method returns openAPI definition which replaced X-WSO2-throttling-tier extension comes from
     * mgw with X-throttling-tier extensions in openAPI file(openAPI version 3)
     *
     * @param swaggerContent String
     * @return String
     * @throws APIManagementException
     */
    @Override
    @Deprecated
    public String injectMgwThrottlingExtensionsToDefault(String swaggerContent) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent);
        return injectMgwThrottlingExtensionsToDefaultCore(openAPI);
    }

    /**
     * This method returns openAPI definition which replaced X-WSO2-throttling-tier extension comes from
     * mgw with X-throttling-tier extensions in openAPI file(openAPI version 3)
     *
     * @param swaggerContent String
     * @return String
     * @throws APIManagementException
     */
    @Override
    public String injectMgwThrottlingExtensionsToDefault(String swaggerContent, OASParserOptions options) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent, options);
        return injectMgwThrottlingExtensionsToDefaultCore(openAPI);
    }

    private String injectMgwThrottlingExtensionsToDefaultCore(OpenAPI openAPI){
        Paths paths = openAPI.getPaths();
        for (String pathKey : paths.keySet()) {
            Map<PathItem.HttpMethod, Operation> operationsMap = paths.get(pathKey).readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : operationsMap.entrySet()) {
                Operation operation = entry.getValue();
                Map<String, Object> extensions = operation.getExtensions();
                if (extensions != null && extensions.containsKey(APISpecParserConstants.X_WSO2_THROTTLING_TIER)) {
                    Object tier = extensions.get(APISpecParserConstants.X_WSO2_THROTTLING_TIER);
                    extensions.remove(APISpecParserConstants.X_WSO2_THROTTLING_TIER);
                    extensions.put(APISpecParserConstants.SWAGGER_X_THROTTLING_TIER, tier);
                }
            }
        }
        return prettifyOAS3ToJson(openAPI);
    }

    /**
     * This method will copy vendor extensions from existing OAS to updated OAS
     *
     * @param existingOASContent existing OAS content
     * @param updatedOASContent  updated OAS content
     * @return updated OAS content with vendor extensions copied from existing OAS
     */
    @Override
    @Deprecated
    public String copyVendorExtensions(String existingOASContent, String updatedOASContent) {

        OpenAPI existingOpenAPI = getOpenAPI(existingOASContent);
        OpenAPI updatedOpenAPI = getOpenAPI(updatedOASContent);
        return copyVendorExtensionsCore(existingOpenAPI, updatedOpenAPI);
    }

    /**
     * This method will copy vendor extensions from existing OAS to updated OAS, with parser options
     *
     * @param existingOASContent existing OAS content
     * @param updatedOASContent  updated OAS content
     * @param options            OAS parser options
     * @return updated OAS content with vendor extensions copied from existing OAS
     */
    @Override
    public String copyVendorExtensions(String existingOASContent, String updatedOASContent, OASParserOptions options) {

        OpenAPI existingOpenAPI = getOpenAPI(existingOASContent, options);
        OpenAPI updatedOpenAPI = getOpenAPI(updatedOASContent, options);
        return copyVendorExtensionsCore(existingOpenAPI, updatedOpenAPI);
    }

    private String copyVendorExtensionsCore(OpenAPI existingOpenAPI, OpenAPI updatedOpenAPI) {
        Paths updatedPaths = updatedOpenAPI.getPaths();
        Paths existingPaths = existingOpenAPI.getPaths();

        // Merge Security Schemes
        if (existingOpenAPI.getComponents().getSecuritySchemes() != null) {
            if (updatedOpenAPI.getComponents() != null) {
                updatedOpenAPI.getComponents().setSecuritySchemes(existingOpenAPI.getComponents().getSecuritySchemes());
            } else {
                Components components = new Components();
                components.setSecuritySchemes(existingOpenAPI.getComponents().getSecuritySchemes());
                updatedOpenAPI.setComponents(components);
            }
        }

        // Merge Operation specific vendor extensions
        for (String pathKey : updatedPaths.keySet()) {
            Map<PathItem.HttpMethod, Operation> operationsMap = updatedPaths.get(pathKey).readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> updatedEntry : operationsMap.entrySet()) {
                if (existingPaths.keySet().contains(pathKey)) {
                    for (Map.Entry<PathItem.HttpMethod, Operation> existingEntry : existingPaths.get(pathKey)
                            .readOperationsMap().entrySet()) {
                        if (updatedEntry.getKey().equals(existingEntry.getKey())) {
                            Map<String, Object> vendorExtensions = updatedEntry.getValue().getExtensions();
                            Map<String, Object> existingExtensions = existingEntry.getValue().getExtensions();
                            boolean extensionsAreEmpty = false;
                            if (vendorExtensions == null) {
                                vendorExtensions = new HashMap<>();
                                extensionsAreEmpty = true;
                            }
                            OASParserUtil.copyOperationVendorExtensions(existingExtensions, vendorExtensions);
                            if (extensionsAreEmpty) {
                                updatedEntry.getValue().setExtensions(existingExtensions);
                            }
                            List<SecurityRequirement> securityRequirements = existingEntry.getValue().getSecurity();
                            List<SecurityRequirement> updatedRequirements = new ArrayList<>();
                            if (securityRequirements != null) {
                                for (SecurityRequirement requirement : securityRequirements) {
                                    List<String> scopes = requirement.get(OAS3Parser.OPENAPI_SECURITY_SCHEMA_KEY);
                                    if (scopes != null) {
                                        updatedRequirements.add(requirement);
                                    }
                                }
                                updatedEntry.getValue().setSecurity(updatedRequirements);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return prettifyOAS3ToJson(updatedOpenAPI);
    }

    /**
     * This method will extract scopes from legacy x-wso2-security and add them to default scheme
     * @param openAPI openAPI definition
     * @return
     * @throws APIManagementException
     */
    private OpenAPI processLegacyScopes(OpenAPI openAPI) throws APIManagementException {
        Set<Scope> scopes = getScopesFromExtensions(openAPI);

        if (!scopes.isEmpty()) {
            if (openAPI.getComponents() == null) {
                openAPI.setComponents(new Components());
            }
            Map<String, SecurityScheme> securitySchemes = openAPI.getComponents().getSecuritySchemes();
            if (securitySchemes == null) {
                securitySchemes = new HashMap<>();
                openAPI.getComponents().setSecuritySchemes(securitySchemes);
            }
            SecurityScheme securityScheme = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY);
            if (securityScheme == null) {
                securityScheme = new SecurityScheme();
                securityScheme.setType(SecurityScheme.Type.OAUTH2);
                securitySchemes.put(OPENAPI_SECURITY_SCHEMA_KEY, securityScheme);
                List<SecurityRequirement> security = new ArrayList<SecurityRequirement>();
                SecurityRequirement secReq = new SecurityRequirement();
                secReq.addList(OPENAPI_SECURITY_SCHEMA_KEY, new ArrayList<String>());
                security.add(secReq);
                openAPI.setSecurity(security);
            }
            if (securityScheme.getFlows() == null) {
                securityScheme.setFlows(new OAuthFlows());
            }
            OAuthFlow oAuthFlow = securityScheme.getFlows().getImplicit();
            if (oAuthFlow == null) {
                oAuthFlow = new OAuthFlow();
                securityScheme.getFlows().setImplicit(oAuthFlow);
            }
            if (oAuthFlow.getAuthorizationUrl() == null) {
                oAuthFlow.setAuthorizationUrl(OPENAPI_DEFAULT_AUTHORIZATION_URL);
            }
            Scopes oas3Scopes = oAuthFlow.getScopes() != null ? oAuthFlow.getScopes() : new Scopes();

            if (scopes != null && !scopes.isEmpty()) {
                Map<String, String> scopeBindings = new HashMap<>();
                if (oAuthFlow.getExtensions() != null) {
                    scopeBindings =
                            (Map<String, String>) oAuthFlow.getExtensions().get(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS)
                                    != null ?
                                    (Map<String, String>) oAuthFlow.getExtensions()
                                            .get(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS) :
                                    new HashMap<>();

                }
                for (Scope scope : scopes) {
                    oas3Scopes.put(scope.getKey(), scope.getDescription());
                    String roles = (StringUtils.isNotBlank(scope.getRoles())
                            && scope.getRoles().trim().split(",").length > 0)
                            ? scope.getRoles() : StringUtils.EMPTY;
                    scopeBindings.put(scope.getKey(), roles);
                }
                oAuthFlow.addExtension(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
            }
            oAuthFlow.setScopes(oas3Scopes);
        }
        return openAPI;
    }

    /**
     * This method returns the oauth scopes according to the given swagger(version 3)
     *
     * @param openAPI - OpenApi object
     * @return OpenAPI
     * @throws APIManagementException
     */
    private OpenAPI injectOtherScopesToDefaultScheme(OpenAPI openAPI) throws APIManagementException {
        Map<String, SecurityScheme> securitySchemes;
        Components component = openAPI.getComponents();
        List<String> otherSetOfSchemes = new ArrayList<>();

        if (openAPI.getComponents() != null && (securitySchemes = openAPI.getComponents().getSecuritySchemes()) != null) {
            //If there is no default type schemes set a one
            SecurityScheme defaultScheme = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY);
            if (defaultScheme == null) {
                SecurityScheme newDefault = new SecurityScheme();
                newDefault.setType(SecurityScheme.Type.OAUTH2);
                //Populating the default security scheme with default values
                OAuthFlows newDefaultFlows = new OAuthFlows();
                OAuthFlow newDefaultFlow = new OAuthFlow();
                newDefaultFlow.setAuthorizationUrl(OPENAPI_DEFAULT_AUTHORIZATION_URL);
                Scopes newDefaultScopes = new Scopes();
                newDefaultFlow.setScopes(newDefaultScopes);
                newDefaultFlows.setImplicit(newDefaultFlow);
                newDefault.setFlows(newDefaultFlows);

                securitySchemes.put(OPENAPI_SECURITY_SCHEMA_KEY, newDefault);
            }
            for (Map.Entry<String, SecurityScheme> entry : securitySchemes.entrySet()) {
                if (!OPENAPI_SECURITY_SCHEMA_KEY.equals(entry.getKey()) && "oauth2".equals(entry.getValue().getType().toString())) {
                    otherSetOfSchemes.add(entry.getKey());
                    //Check for default one
                    SecurityScheme defaultType = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY);
                    OAuthFlows defaultTypeFlows = defaultType.getFlows();
                    if (defaultTypeFlows == null) {
                        defaultTypeFlows = new OAuthFlows();
                    }
                    OAuthFlow defaultTypeFlow = defaultTypeFlows.getImplicit();
                    if (defaultTypeFlow == null) {
                        defaultTypeFlow = new OAuthFlow();
                    }

                    SecurityScheme noneDefaultType = entry.getValue();
                    OAuthFlows noneDefaultTypeFlows = noneDefaultType.getFlows();
                    //Get Implicit Flows
                    OAuthFlow noneDefaultTypeFlowImplicit = noneDefaultTypeFlows.getImplicit();
                    if (noneDefaultTypeFlowImplicit != null) {
                        defaultTypeFlow = extractAndInjectScopesFromFlow(noneDefaultTypeFlowImplicit, defaultTypeFlow);
                        defaultTypeFlows.setImplicit(defaultTypeFlow);
                    }
                    //Get AuthorizationCode Flow
                    OAuthFlow noneDefaultTypeFlowAuthorizationCode = noneDefaultTypeFlows.getAuthorizationCode();
                    if (noneDefaultTypeFlowAuthorizationCode != null) {
                        defaultTypeFlow = extractAndInjectScopesFromFlow(noneDefaultTypeFlowAuthorizationCode, defaultTypeFlow);
                        defaultTypeFlows.setImplicit(defaultTypeFlow);
                    }
                    //Get ClientCredentials Flow
                    OAuthFlow noneDefaultTypeFlowClientCredentials = noneDefaultTypeFlows.getClientCredentials();
                    if (noneDefaultTypeFlowClientCredentials != null) {
                        defaultTypeFlow = extractAndInjectScopesFromFlow(noneDefaultTypeFlowClientCredentials, defaultTypeFlow);
                        defaultTypeFlows.setImplicit(defaultTypeFlow);
                    }
                    //Get Password Flow
                    OAuthFlow noneDefaultTypeFlowPassword = noneDefaultTypeFlows.getPassword();
                    if (noneDefaultTypeFlowPassword != null) {
                        defaultTypeFlow = extractAndInjectScopesFromFlow(noneDefaultTypeFlowPassword, defaultTypeFlow);
                        defaultTypeFlows.setImplicit(defaultTypeFlow);
                    }

                    defaultType.setFlows(defaultTypeFlows);
                }
            }
            component.setSecuritySchemes(securitySchemes);
            openAPI.setComponents(component);
        }
        setOtherSchemes(otherSetOfSchemes);
        return openAPI;
    }

    /**
     * This method returns the oauth scopes of Oauthflows according to the given swagger(version 3)
     *
     * @param noneDefaultTypeFlow , OAuthflow
     * @param defaultTypeFlow,    OAuthflow
     * @return OAuthFlow
     */
    private OAuthFlow extractAndInjectScopesFromFlow(OAuthFlow noneDefaultTypeFlow, OAuthFlow defaultTypeFlow) {
        Scopes noneDefaultFlowScopes = noneDefaultTypeFlow.getScopes();
        Scopes defaultFlowScopes = defaultTypeFlow.getScopes();
        Map<String, String> defaultScopeBindings = null;
        if (defaultFlowScopes == null) {
            defaultFlowScopes = new Scopes();
        }
        if (noneDefaultFlowScopes != null) {
            for (Map.Entry<String, String> input : noneDefaultFlowScopes.entrySet()) {
                //Inject scopes set into default scheme
                defaultFlowScopes.addString(input.getKey(), input.getValue());
            }
        }
        defaultTypeFlow.setScopes(defaultFlowScopes);
        //Check X-Scope Bindings
        Map<String, String> noneDefaultScopeBindings = null;
        Map<String, Object> defaultTypeExtension = defaultTypeFlow.getExtensions();
        if (defaultTypeExtension == null) {
            defaultTypeExtension = new HashMap<>();
        }
        if (noneDefaultTypeFlow.getExtensions() != null && (noneDefaultScopeBindings =
                (Map<String, String>) noneDefaultTypeFlow.getExtensions().get(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS))
                != null) {
            defaultScopeBindings = (Map<String, String>) defaultTypeExtension.get(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS);
            if (defaultScopeBindings == null) {
                defaultScopeBindings = new HashMap<>();
            }
            for (Map.Entry<String, String> roleInUse : noneDefaultScopeBindings.entrySet()) {
                defaultScopeBindings.put(roleInUse.getKey(), roleInUse.getValue());
            }
        }
        defaultTypeExtension.put(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS, defaultScopeBindings);
        defaultTypeFlow.setExtensions(defaultTypeExtension);
        return defaultTypeFlow;
    }

    /**
     * This method returns URI templates according to the given swagger file(Swagger version 3)
     *
     * @param openAPI OpenAPI
     * @return OpenAPI
     * @throws APIManagementException
     */
    private OpenAPI injectOtherResourceScopesToDefaultScheme(OpenAPI openAPI) throws APIManagementException {
        List<String> schemes = getOtherSchemes();

        Paths paths = openAPI.getPaths();
        for (String pathKey : paths.keySet()) {
            PathItem pathItem = paths.get(pathKey);
            Map<PathItem.HttpMethod, Operation> operationsMap = pathItem.readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : operationsMap.entrySet()) {
                SecurityRequirement updatedDefaultSecurityRequirement = new SecurityRequirement();
                PathItem.HttpMethod httpMethod = entry.getKey();
                Operation operation = entry.getValue();
                List<SecurityRequirement> securityRequirements = operation.getSecurity();
                if (securityRequirements == null) {
                    securityRequirements = new ArrayList<>();
                }
                if (APISpecParserConstants.SUPPORTED_METHODS.contains(httpMethod.name().toLowerCase())) {
                    List<String> opScopesDefault = new ArrayList<>();
                    List<String> opScopesDefaultInstance = getScopeOfOperations(OPENAPI_SECURITY_SCHEMA_KEY, operation);
                    if (opScopesDefaultInstance != null) {
                        opScopesDefault.addAll(opScopesDefaultInstance);
                    }
                    updatedDefaultSecurityRequirement.put(OPENAPI_SECURITY_SCHEMA_KEY, opScopesDefault);
                    for (Map<String, List<String>> input : securityRequirements) {
                        for (String scheme : schemes) {
                            if (!OPENAPI_SECURITY_SCHEMA_KEY.equals(scheme)) {
                                List<String> opScopesOthers = getScopeOfOperations(scheme, operation);
                                if (opScopesOthers != null) {
                                    for (String scope : opScopesOthers) {
                                        if (!opScopesDefault.contains(scope)) {
                                            opScopesDefault.add(scope);
                                        }
                                    }
                                }
                            }
                            updatedDefaultSecurityRequirement.put(OPENAPI_SECURITY_SCHEMA_KEY, opScopesDefault);
                        }
                    }
                    securityRequirements.add(updatedDefaultSecurityRequirement);
                }
                operation.setSecurity(securityRequirements);
                entry.setValue(operation);
                operationsMap.put(httpMethod, operation);
            }
            paths.put(pathKey, pathItem);
        }
        openAPI.setPaths(paths);
        return openAPI;
    }

    /**
     * This method returns api that is attached with api extensions related to micro-gw
     *
     * @param apiDefinition String
     * @param api           API
     * @return API
     */
    @Override
    public API setExtensionsToAPI(String apiDefinition, API api) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(apiDefinition);
        Map<String, Object> extensions = openAPI.getExtensions();
        if (extensions == null) {
            return api;
        }

        //Setup Custom auth header for API
        String authHeader = OASParserUtil.getAuthorizationHeaderFromSwagger(extensions);
        if (StringUtils.isNotBlank(authHeader)) {
            api.setAuthorizationHeader(authHeader);
        }
        //Setup custom api key header for API
        String apiKeyHeader = OASParserUtil.getApiKeyHeaderFromSwagger(extensions);
        if (StringUtils.isNotBlank(apiKeyHeader)) {
            api.setApiKeyHeader(apiKeyHeader);
        }

        //Setup application Security
        List<String> applicationSecurity = OASParserUtil.getApplicationSecurityTypes(extensions);
        Boolean isOptional = OASParserUtil.getAppSecurityStateFromSwagger(extensions);
        if (!applicationSecurity.isEmpty()) {
            String securityList = api.getApiSecurity();
            securityList = securityList == null ? "" : securityList;
            for (String securityType : applicationSecurity) {
                if (APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2.equals(securityType) &&
                        !securityList.contains(APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2)) {
                    securityList = securityList + "," + APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2;
                }
                if (APISpecParserConstants.API_SECURITY_BASIC_AUTH.equals(securityType) &&
                        !securityList.contains(APISpecParserConstants.API_SECURITY_BASIC_AUTH)) {
                    securityList = securityList + "," + APISpecParserConstants.API_SECURITY_BASIC_AUTH;
                }
                if (APISpecParserConstants.API_SECURITY_API_KEY.equals(securityType) &&
                        !securityList.contains(APISpecParserConstants.API_SECURITY_API_KEY)) {
                    securityList = securityList + "," + APISpecParserConstants.API_SECURITY_API_KEY;
                }
            }
            if (!(isOptional || securityList.contains(APISpecParserConstants.MANDATORY))) {
                securityList = securityList + "," + APISpecParserConstants.MANDATORY;
            }
            api.setApiSecurity(securityList);
        }
        //Setup mutualSSL configuration
        String mutualSSL = OASParserUtil.getMutualSSLEnabledFromSwagger(extensions);
        if (StringUtils.isNotBlank(mutualSSL)) {
            String securityList = api.getApiSecurity();
            if (StringUtils.isBlank(securityList)) {
                securityList = APISpecParserConstants.DEFAULT_API_SECURITY_OAUTH2;
            }
            if (APISpecParserConstants.OPTIONAL.equals(mutualSSL) &&
                    !securityList.contains(APISpecParserConstants.API_SECURITY_MUTUAL_SSL)) {
                securityList = securityList + "," + APISpecParserConstants.API_SECURITY_MUTUAL_SSL;
            } else if (APISpecParserConstants.MANDATORY.equals(mutualSSL) &&
                    !securityList.contains(APISpecParserConstants.API_SECURITY_MUTUAL_SSL_MANDATORY)){
                securityList = securityList + "," + APISpecParserConstants.API_SECURITY_MUTUAL_SSL + "," +
                        APISpecParserConstants.API_SECURITY_MUTUAL_SSL_MANDATORY;
            }
            api.setApiSecurity(securityList);
        }
        //Setup CORSConfigurations
        CORSConfiguration corsConfiguration = OASParserUtil.getCorsConfigFromSwagger(extensions);
        if (corsConfiguration != null && !corsConfiguration.isEmpty()) {
            api.setCorsConfiguration(corsConfiguration);
            if (log.isDebugEnabled()) {
                log.debug("Adding CORS Configuration to the API");
            }
        }
        //Setup Response cache enabling
        boolean responseCacheEnable = OASParserUtil.getResponseCacheFromSwagger(extensions);
        if (responseCacheEnable) {
            api.setResponseCache(APISpecParserConstants.ENABLED);
        }
        //Setup cache timeOut
        int cacheTimeOut = OASParserUtil.getCacheTimeOutFromSwagger(extensions);
        if (cacheTimeOut != 0) {
            api.setCacheTimeout(cacheTimeOut);
        }
        //Setup Transports
        String transports = OASParserUtil.getTransportsFromSwagger(extensions);
        if (StringUtils.isNotBlank(transports)) {
            api.setTransports(transports);
        }
        //Setup Throttlingtiers
        String throttleTier = OASParserUtil.getThrottleTierFromSwagger(extensions);
        if (StringUtils.isNotBlank(throttleTier)) {
            api.setApiLevelPolicy(throttleTier);
        }
        return api;
    }

    /**
     * Remove x-examples from all the paths from the OpenAPI definition.
     *
     * @param apiDefinition OpenAPI definition as String
     */
    public static String removeExamplesFromOpenAPI(String apiDefinition) throws APIManagementException {
        try {
            OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
            SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(apiDefinition, null, null);
            if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
                log.debug("Errors found when parsing OAS definition");
            }
            OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
            for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
                String path = entry.getKey();
                List<Operation> operations = openAPI.getPaths().get(path).readOperations();
                for (Operation operation : operations) {
                    if (operation.getExtensions() != null && operation.getExtensions().keySet()
                            .contains(APISpecParserConstants.SWAGGER_X_EXAMPLES)) {
                        operation.getExtensions().remove(APISpecParserConstants.SWAGGER_X_EXAMPLES);
                    }
                }
            }
            return Yaml.pretty().writeValueAsString(openAPI);
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error while removing examples from OpenAPI definition", e,
                    ExceptionCodes.ERROR_REMOVING_EXAMPLES);
        }
    }

    /**
     * This method will extractX-WSO2-disable-security extension provided in API level
     * by mgw and inject that extension to all resources in OAS file
     *
     * @param swaggerContent String
     * @return String
     * @throws APIManagementException
     */
    @Override
    @Deprecated
    public String processDisableSecurityExtension(String swaggerContent) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent);
        return processDisableSecurityExtensionsCore(openAPI, swaggerContent);
    }

    /**
     * This method will extract X-WSO2-disable-security extension provided in API level
     * by mgw and inject that extension to all resources in OAS file with options
     *
     * @param swaggerContent String
     * @param options        OAS parser options
     * @return String
     * @throws APIManagementException
     */
    @Override
    public String processDisableSecurityExtension(String swaggerContent, OASParserOptions options)
            throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent, options);
        return processDisableSecurityExtensionsCore(openAPI, swaggerContent);
    }

    private String processDisableSecurityExtensionsCore(OpenAPI openAPI, String swaggerContent)
            throws APIManagementException {
        Map<String, Object> apiExtensions = openAPI.getExtensions();
        if (apiExtensions == null) {
            return swaggerContent;
        }
        //Check Disable Security is enabled in API level
        boolean apiLevelDisableSecurity = OASParserUtil.getDisableSecurity(apiExtensions);
        Paths paths = openAPI.getPaths();
        for (String pathKey : paths.keySet()) {
            Map<PathItem.HttpMethod, Operation> operationsMap = paths.get(pathKey).readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : operationsMap.entrySet()) {
                Operation operation = entry.getValue();
                Map<String, Object> resourceExtensions = operation.getExtensions();
                boolean extensionsAreEmpty = false;
                if (apiLevelDisableSecurity) {
                    if (resourceExtensions == null) {
                        resourceExtensions = new HashMap<>();
                        extensionsAreEmpty = true;
                    }
                    resourceExtensions.put(APISpecParserConstants.SWAGGER_X_AUTH_TYPE, "None");
                    if (extensionsAreEmpty) {
                        operation.setExtensions(resourceExtensions);
                    }
                } else if (resourceExtensions != null && resourceExtensions.containsKey(APISpecParserConstants.X_WSO2_DISABLE_SECURITY)) {
                    //Check Disable Security is enabled in resource level
                    boolean resourceLevelDisableSecurity = Boolean.parseBoolean(String.valueOf(resourceExtensions.get(APISpecParserConstants.X_WSO2_DISABLE_SECURITY)));
                    if (resourceLevelDisableSecurity) {
                        resourceExtensions.put(APISpecParserConstants.SWAGGER_X_AUTH_TYPE, "None");
                    }
                }
            }
        }
        return prettifyOAS3ToJson(openAPI);
    }

    /**
     * This method prettify the OA3 definition to a JSON object
     * @param openAPI
     * @return
     */
    public String prettifyOAS3ToJson(OpenAPI openAPI) {
        return OASParserUtil.convertOAStoJSON(openAPI);
    }

    @Override
    public String getVendorFromExtension(String swaggerContent) {
        return null;
    }

    @Override
    public String getVendorFromExtensionWithError(String swaggerContent) throws APIManagementException {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    public String getSpecVersion() {
        return specVersion;
    }
    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    @Override
    public Set<URITemplate> generateMCPTools(String backendApiDefinition, APIIdentifier refApiId, String backendId,
                                             String mcpSubtype, Set<URITemplate> uriTemplates)
            throws APIManagementException {

        OpenAPI backendDefinition = getOpenAPI(backendApiDefinition);
        mergePathParametersIntoOperations(backendDefinition);
        if (backendDefinition.getPaths() == null || backendDefinition.getPaths().isEmpty()) {
            log.warn("Backend API definition has no paths defined");
            return new HashSet<>();
        }
        Set<String> tools = new LinkedHashSet<>();
        Set<URITemplate> generatedTools = new HashSet<>();
        for (URITemplate template : uriTemplates) {
            BackendOperation backendOperation = null;
            if (APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND.equals(mcpSubtype)) {
                BackendOperationMapping mapping = template.getBackendOperationMapping();
                if (mapping != null && mapping.getBackendOperation() != null) {
                    backendOperation = mapping.getBackendOperation();
                }
            } else if (APISpecParserConstants.API_SUBTYPE_EXISTING_API.equals(mcpSubtype)) {
                APIOperationMapping mapping = template.getAPIOperationMapping();
                if (mapping != null && mapping.getBackendOperation() != null) {
                    backendOperation = mapping.getBackendOperation();
                }
            }

            if (backendOperation == null) {
                log.warn("URITemplate does not have valid backend or API operation mapping: " + template);
                continue;
            }

            OperationMatch match =
                    findMatchingOperation(backendDefinition, backendOperation.getTarget(), backendOperation.getVerb());
            if (match != null) {
                URITemplate toolTemplate = populateURITemplate(template, match, backendDefinition, backendId,
                        refApiId, true);
                if (!tools.add(toolTemplate.getUriTemplate())) {
                    log.error("Duplicate MCP tool detected: " + toolTemplate.getUriTemplate());
                    throw new APIManagementException("Tool " + toolTemplate.getUriTemplate() + " is repeated",
                            ExceptionCodes.DUPLICATE_MCP_TOOLS);
                }
                generatedTools.add(toolTemplate);
            }
        }

        return generatedTools;
    }

    @Override
    public Set<URITemplate> updateMCPTools(String backendApiDefinition, APIIdentifier refApiId, String backendId,
                                           String mcpSubtype, Set<URITemplate> uriTemplates)
            throws APIManagementException {

        OpenAPI backendDefinition = getOpenAPI(backendApiDefinition);
        mergePathParametersIntoOperations(backendDefinition);
        if (backendDefinition.getPaths() == null || backendDefinition.getPaths().isEmpty()) {
            log.warn("Backend API definition has no paths defined");
            return new HashSet<>();
        }
        Set<String> tools = new LinkedHashSet<>();
        Set<URITemplate> updatedTools = new HashSet<>();
        for (URITemplate template : uriTemplates) {

            BackendOperation backendOperation = null;

            if (APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND.equals(mcpSubtype)) {
                BackendOperationMapping mapping = template.getBackendOperationMapping();
                if (mapping != null && mapping.getBackendOperation() != null) {
                    backendOperation = mapping.getBackendOperation();
                }
            } else if (APISpecParserConstants.API_SUBTYPE_EXISTING_API.equals(mcpSubtype)) {
                APIOperationMapping mapping = template.getAPIOperationMapping();
                if (mapping != null && mapping.getBackendOperation() != null) {
                    backendOperation = mapping.getBackendOperation();
                }
            }

            if (backendOperation == null) {
                log.warn("URITemplate does not have valid backend or API operation mapping: " + template);
                continue;
            }

            OperationMatch match = findMatchingOperation(backendDefinition, backendOperation.getTarget(),
                    backendOperation.getVerb());

            if (match != null) {
                URITemplate populated = populateURITemplate(template, match, backendDefinition, backendId, refApiId,
                        false);
                if (!tools.add(populated.getUriTemplate())) {
                    log.error("Duplicate MCP tool detected: " + populated.getUriTemplate());
                    throw new APIManagementException("Tool " + populated.getUriTemplate() + " is repeated",
                            ExceptionCodes.DUPLICATE_MCP_TOOLS);
                }
                updatedTools.add(populated);
                continue;
            }
            if (!tools.add(template.getUriTemplate())) {
                log.error("Duplicate MCP tool detected: " + template.getUriTemplate());
                throw new APIManagementException("Tool " + template.getUriTemplate() + " is repeated",
                        ExceptionCodes.DUPLICATE_MCP_TOOLS);
            }
            updatedTools.add(template);
        }
        return updatedTools;
    }

    /**
     * Merges path-level parameters into each operation under that path.
     * Path parameters are added to operations unless they are already defined at the operation level.
     *
     * @param openAPI OpenAPI definition to process
     */
    private static void mergePathParametersIntoOperations(OpenAPI openAPI) {

        if (openAPI == null || openAPI.getPaths() == null) {
            return;
        }
        for (PathItem pathItem : openAPI.getPaths().values()) {
            if (pathItem == null || pathItem.getParameters() == null || pathItem.getParameters().isEmpty()) continue;
            List<Parameter> pathParams = new ArrayList<>();
            for (Parameter parameter : pathItem.getParameters()) {
                Parameter reference = resolveParameterRef(parameter, openAPI);
                if (reference == null) continue;
                Parameter copy = deepCopyParameter(reference);
                if (APISpecParserConstants.PATH.equalsIgnoreCase(copy.getIn())) copy.setRequired(Boolean.TRUE);
                pathParams.add(copy);
            }
            if (pathParams.isEmpty()) continue;
            for (Operation operation : Arrays.asList(pathItem.getGet(), pathItem.getPost(), pathItem.getPut(),
                    pathItem.getDelete(), pathItem.getPatch(), pathItem.getHead(),
                    pathItem.getOptions(), pathItem.getTrace())) {
                if (operation == null) continue;

                if (operation.getParameters() == null) {
                    operation.setParameters(new ArrayList<>());
                }
                Map<String, Integer> parameterMap = new LinkedHashMap<>();
                for (int i = 0; i < operation.getParameters().size(); i++) {
                    Parameter parameter = operation.getParameters().get(i);
                    parameterMap.put(paramKey(parameter), i);
                }
                for (Parameter parameter : pathParams) {
                    String key = paramKey(parameter);
                    if (!parameterMap.containsKey(key)) {
                        operation.getParameters().add(parameter);
                    }
                }
            }
        }
    }

    /**
     * Generates a unique key for a parameter based on its location and name.
     * This is used to identify parameters uniquely across different operations.
     *
     * @param parameter Parameter to generate the key for
     * @return Unique key for the parameter
     */
    private static String paramKey(Parameter parameter) {

        return (parameter.getIn() == null ? StringUtils.EMPTY : parameter.getIn()) + ":" +
                (parameter.getName() == null ? StringUtils.EMPTY : parameter.getName());
    }

    /**
     * Resolves a parameter reference to its actual definition in the OpenAPI components.
     * If the parameter is a reference, it retrieves the corresponding parameter from the components.
     *
     * @param parameter Parameter to resolve
     * @param openAPI   OpenAPI definition containing components
     * @return Resolved Parameter or original if not a reference
     */
    private static Parameter resolveParameterRef(Parameter parameter, OpenAPI openAPI) {

        if (parameter == null || parameter.get$ref() == null) return parameter;
        if (openAPI == null || openAPI.getComponents() == null || openAPI.getComponents().getParameters() == null)
            return parameter;
        String ref = parameter.get$ref();
        String name = ref.substring(ref.lastIndexOf('/') + 1);
        Parameter target = openAPI.getComponents().getParameters().get(name);
        return target != null ? target : parameter;
    }

    /**
     * Creates a deep copy of a Parameter object.
     * This is necessary to avoid modifying the original parameter when making changes.
     *
     * @param parameter Parameter to copy
     * @return Deep copied Parameter
     */
    private static Parameter deepCopyParameter(Parameter parameter) {

        return Json.mapper().convertValue(Json.mapper().valueToTree(parameter), Parameter.class);
    }

    /**
     * Populates the URITemplate with details from the OpenAPI definition and the matched operation.
     *
     * @param uriTemplate            URITemplate to populate
     * @param match                  OperationMatch containing path, method, and operation details
     * @param backendAPIDefinition   OpenAPI definition of the backend API
     * @param backendId              Backend ID for the operation mapping
     * @param refApiId               APIIdentifier for the reference API
     * @param setPropsFromDefinition Whether to set properties from the OpenAPI definition
     * @return Populated URITemplate
     * @throws APIManagementException If an error occurs while populating the URITemplate
     */
    private URITemplate populateURITemplate(URITemplate uriTemplate, OperationMatch match, OpenAPI backendAPIDefinition,
                                            String backendId, APIIdentifier refApiId, boolean setPropsFromDefinition)
            throws APIManagementException {

        if (uriTemplate.getUriTemplate() == null || uriTemplate.getUriTemplate().isEmpty()) {
            String operationId = Optional.ofNullable(match.operation.getOperationId())
                    .orElseGet(() -> match.method.toString().toLowerCase() +
                            match.path.replaceAll("/+$", "")
                                    .replaceAll("\\{([^/}]+)\\}", "by_$1")
                                    .replace("/", "_"));
            uriTemplate.setUriTemplate(operationId);
        }

        if (uriTemplate.getDescription() == null || uriTemplate.getDescription().isEmpty()) {
            String description = Optional.ofNullable(match.operation.getDescription())
                    .filter(desc -> !desc.isEmpty())
                    .orElse(Optional.ofNullable(match.operation.getSummary())
                            .filter(sum -> !sum.isEmpty())
                            .orElse(StringUtils.EMPTY));
            uriTemplate.setDescription(description);
        }

        if (uriTemplate.getSchemaDefinition() == null || uriTemplate.getSchemaDefinition().isEmpty()) {
            try {
                String jsonSchema = getObjectMapper()
                        .writeValueAsString(buildUnifiedInputSchema(
                                match.operation.getParameters(),
                                match.operation.getRequestBody(),
                                backendAPIDefinition));
                uriTemplate.setSchemaDefinition(jsonSchema);
            } catch (JsonProcessingException e) {
                log.error("Error generating JSON schema for operation: " + uriTemplate.getUriTemplate(), e);
                throw new APIManagementException(
                        "Error generating JSON schema for operation: " + uriTemplate.getUriTemplate(), e);
            }
        }

        BackendOperation backendOperation = new BackendOperation();
        String methodStr = match.method.toString();
        try {
            APIConstants.SupportedHTTPVerbs verb = APIConstants.SupportedHTTPVerbs.fromValue(methodStr);
            backendOperation.setVerb(verb);
        } catch (IllegalArgumentException e) {
            throw new APIManagementException("Unsupported HTTP verb: " + methodStr, e);
        }
        backendOperation.setTarget(match.path);

        if (uriTemplate.getBackendOperationMapping() != null) {
            BackendOperationMapping backendOperationMap = new BackendOperationMapping();
            backendOperationMap.setBackendId(backendId);
            backendOperationMap.setBackendOperation(backendOperation);
            uriTemplate.setBackendOperationMapping(backendOperationMap);
        } else if (uriTemplate.getAPIOperationMapping() != null) {
            APIOperationMapping apiOperationMap = new APIOperationMapping();
            apiOperationMap.setApiUuid(refApiId.getUUID());
            apiOperationMap.setApiName(refApiId.getApiName());
            apiOperationMap.setApiVersion(refApiId.getVersion());
            apiOperationMap.setBackendOperation(backendOperation);
            uriTemplate.setAPIOperationMapping(apiOperationMap);
        }
        if (setPropsFromDefinition) {
            Map<String, Object> extensions = match.operation.getExtensions();
            if (extensions != null) {
                if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_AUTH_TYPE)) {
                    String scopeKey = (String) extensions.get(APISpecParserConstants.SWAGGER_X_AUTH_TYPE);
                    uriTemplate.setAuthType(scopeKey);
                    uriTemplate.setAuthTypes(scopeKey);
                } else {
                    uriTemplate.setAuthType(APISpecParserConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                    uriTemplate.setAuthTypes(APISpecParserConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                }
                if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_THROTTLING_TIER)) {
                    String throttlingTier = (String) extensions.get(APISpecParserConstants.SWAGGER_X_THROTTLING_TIER);
                    uriTemplate.setThrottlingTier(throttlingTier);
                    uriTemplate.setThrottlingTiers(throttlingTier);
                }
                if (extensions.containsKey(APISpecParserConstants.SWAGGER_X_MEDIATION_SCRIPT)) {
                    String mediationScript = (String) extensions.get(APISpecParserConstants.SWAGGER_X_MEDIATION_SCRIPT);
                    uriTemplate.setMediationScript(mediationScript);
                    uriTemplate.setMediationScripts(uriTemplate.getHTTPVerb(), mediationScript);
                }
            }
        }
        return uriTemplate;
    }

    /**
     * Returns the ObjectMapper instance used for JSON processing.
     * This is a singleton instance to avoid creating multiple ObjectMapper instances.
     *
     * @return ObjectMapper instance
     */
    private ObjectMapper getObjectMapper() {

        return OBJECT_MAPPER;
    }

    /**
     * Finds a matching operation in the OpenAPI definition based on the target path and HTTP verb.
     * Returns an OperationMatch object containing the path, method, and operation details if found.
     *
     * @param openAPI OpenAPI definition
     * @param target  Target path to match
     * @param verb    HTTP verb to match
     * @return OperationMatch if found, null otherwise
     */
    private OperationMatch findMatchingOperation(OpenAPI openAPI, String target, APIConstants.SupportedHTTPVerbs verb) {

        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            for (Map.Entry<PathItem.HttpMethod, Operation> opEntry :
                    pathEntry.getValue().readOperationsMap().entrySet()) {
                if (pathEntry.getKey().equals(target) &&
                        opEntry.getKey().toString().equalsIgnoreCase(verb.toString())) {
                    return new OperationMatch(pathEntry.getKey(), opEntry.getKey(), opEntry.getValue());
                }
            }
        }
        return null;
    }

    /**
     * Builds a unified input schema for the operation, combining parameters and request body.
     * Returns a Map representing the schema structure.
     *
     * @param parameters   List of parameters for the operation
     * @param requestBody  Request body schema if available
     * @param openAPI      OpenAPI definition to resolve schemas
     * @return Map representing the unified input schema
     */
    private Map<String, Object> buildUnifiedInputSchema(List<Parameter> parameters, RequestBody requestBody,
                                                        OpenAPI openAPI) {

        Map<String, Object> root = new LinkedHashMap<>();
        root.put(APISpecParserConstants.TYPE, APISpecParserConstants.OBJECT);

        Map<String, Object> props = new LinkedHashMap<>();
        List<String> requiredFields = new ArrayList<>();

        if (parameters != null) {
            for (Parameter param : parameters) {
                if (param.get$ref() != null) {
                    param = resolveComponentRef(param.get$ref(), openAPI, new HashSet<>(), Parameter.class);
                }

                if (param == null) {
                    continue;
                }

                String name = param.getIn() + "_" + param.getName();
                Map<String, Object> paramSchema = new LinkedHashMap<>();
                Schema<?> schema = resolveSchema(param.getSchema(), openAPI);

                if (schema != null) {
                    paramSchema.put(APISpecParserConstants.TYPE, schema.getType());
                    if (schema.getFormat() != null) {
                        paramSchema.put(APISpecParserConstants.FORMAT, schema.getFormat());
                    }
                    if (schema.getEnum() != null) {
                        paramSchema.put(APISpecParserConstants.ENUM, schema.getEnum());
                    }
                    if (schema.getDefault() != null) {
                        paramSchema.put(APISpecParserConstants.DEFAULT, schema.getDefault());
                    }
                    if (param.getDescription() != null) {
                        paramSchema.put(APISpecParserConstants.DESCRIPTION, param.getDescription());
                    }
                }
                props.put(name, paramSchema);
                if (Boolean.TRUE.equals(param.getRequired())) {
                    requiredFields.add(name);
                }
            }
        }

        if (requestBody != null) {
            if (requestBody.get$ref() != null) {
                requestBody = resolveComponentRef(requestBody.get$ref(), openAPI, new HashSet<>(), RequestBody.class);
            }
            if (requestBody != null &&
                    requestBody.getContent() != null &&
                    requestBody.getContent().get(APISpecParserConstants.APPLICATION_JSON_MEDIA_TYPE) != null) {

                Schema<?> rawSchema =
                        requestBody.getContent().get(APISpecParserConstants.APPLICATION_JSON_MEDIA_TYPE).getSchema();

                Schema<?> bodySchema = resolveSchema(rawSchema, openAPI);

                Map<String, Object> requestBodyNode = new LinkedHashMap<>();
                requestBodyNode.put(APISpecParserConstants.TYPE, APISpecParserConstants.OBJECT);
                requestBodyNode.put(APISpecParserConstants.CONTENT_TYPE, APPLICATION_JSON_MEDIA_TYPE);

                if (bodySchema != null) {
                    if (bodySchema.getProperties() != null) {
                        requestBodyNode.put(APISpecParserConstants.PROPERTIES, bodySchema.getProperties());
                    }
                    if (bodySchema.getRequired() != null) {
                        requestBodyNode.put(APISpecParserConstants.REQUIRED, bodySchema.getRequired());
                    }
                }
                props.put(APISpecParserConstants.REQUEST_BODY, requestBodyNode);
                if (Boolean.TRUE.equals(requestBody.getRequired())) {
                    requiredFields.add(APISpecParserConstants.REQUEST_BODY);
                }
            }
        }
        root.put(APISpecParserConstants.PROPERTIES, props);
        if (!requiredFields.isEmpty()) {
            root.put(APISpecParserConstants.REQUIRED, requiredFields);
        }
        return root;
    }

    /**
     * Resolves a schema by recursively resolving $ref, allOf, oneOf, anyOf, and not properties.
     * Returns the resolved Schema object.
     *
     * @param schema   Schema to resolve
     * @param openAPI  OpenAPI definition to resolve against
     * @return Resolved Schema object
     */
    private Schema<?> resolveSchema(Schema<?> schema, OpenAPI openAPI) {

        return resolveSchema(schema, openAPI, new HashSet<>());
    }

    /**
     * Resolves a schema by recursively resolving $ref, allOf, oneOf, anyOf, and not properties.
     * This version tracks visited references to prevent circular references.
     *
     * @param schema      Schema to resolve
     * @param openAPI     OpenAPI definition to resolve against
     * @param visitedRefs Set of visited reference names to detect circular references
     * @return Resolved Schema object
     */
    private Schema<?> resolveSchema(Schema<?> schema, OpenAPI openAPI, Set<String> visitedRefs) {

        if (schema == null) return null;

        if (schema.get$ref() != null) {
            schema = resolveComponentRef(schema.get$ref(), openAPI, visitedRefs, Schema.class);
        }
        if (schema == null) {
            return null;
        }
        // Resolve allOf
        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            Schema<?> merged = new ObjectSchema();
            Map<String, Schema> mergedProps = new LinkedHashMap<>();
            List<String> mergedRequired = new ArrayList<>();
            for (Schema<?> part : schema.getAllOf()) {
                Schema<?> resolved = resolveSchema(part, openAPI);
                if (resolved.getProperties() != null) mergedProps.putAll(resolved.getProperties());
                if (resolved.getRequired() != null) mergedRequired.addAll(resolved.getRequired());
            }
            merged.setProperties(mergedProps);
            merged.setRequired(mergedRequired);
            return merged;
        }

        // oneOf / anyOf
        if (schema.getOneOf() != null) {
            schema.setOneOf(schema.getOneOf().stream()
                    .map(s -> resolveSchema(s, openAPI))
                    .collect(Collectors.toList()));
        }

        if (schema.getAnyOf() != null) {
            schema.setAnyOf(schema.getAnyOf().stream()
                    .map(s -> resolveSchema(s, openAPI))
                    .collect(Collectors.toList()));
        }
        if (schema.getNot() != null) {
            schema.setNot(resolveSchema(schema.getNot(), openAPI));
        }

        // Recursively resolve properties
        if (schema.getProperties() != null) {
            Map<String, Schema> resolvedProps = new LinkedHashMap<>();
            for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
                resolvedProps.put(entry.getKey(), resolveSchema(entry.getValue(), openAPI));
            }
            schema.setProperties(resolvedProps);
        }

        // Array items
        if ("array".equals(schema.getType()) && schema.getItems() != null) {
            schema.setItems(resolveSchema(schema.getItems(), openAPI));
        }

        // Additional properties
        if (schema.getAdditionalProperties() instanceof Schema) {
            schema.setAdditionalProperties(resolveSchema((Schema<?>) schema.getAdditionalProperties(), openAPI));
        }

        return schema;
    }

    /**
     * Resolves a component reference ($ref) to its actual definition in the OpenAPI components.
     * Handles circular references by tracking visited references.
     *
     * @param ref          Reference string (e.g., "#/components/schemas/ComponentName")
     * @param openAPI      OpenAPI definition containing components
     * @param visitedRefs  Set of visited reference names to detect circular references
     * @param expectedType Expected class type of the resolved component
     * @param <T>          Type parameter for the expected component type
     * @return Resolved component of type T, or null if not found or circular reference detected
     */
    @SuppressWarnings("unchecked")
    private <T> T resolveComponentRef(String ref, OpenAPI openAPI, Set<String> visitedRefs, Class<T> expectedType) {

        if (log.isDebugEnabled()) {
            log.debug("Resolving component reference:" + ref + " of type: " + expectedType.getSimpleName());
        }
        if (ref == null || !ref.startsWith("#/components/")) {
            log.warn("Invalid component reference: " + ref);
            return null;
        }
        String[] parts = ref.split("/");
        if (parts.length < 4) {
            log.warn("Malformed component reference: " + ref);
            return null;
        }
        String category = parts[2];
        String name = parts[3].replace("~1", "/").replace("~0", "~"); // JSON Pointer unescape
        String refKey = category + ":" + name;
        if (visitedRefs.contains(refKey)) {
            if (log.isDebugEnabled()) {
                log.debug("Circular reference detected: " + refKey);
            }
            return null;
        }
        visitedRefs.add(refKey);
        Object resolved = null;
        if (openAPI == null || openAPI.getComponents() == null) {
            return null;
        }
        switch (category) {
            case APISpecParserConstants.SCHEMAS:
                resolved = openAPI.getComponents().getSchemas() != null ?
                        openAPI.getComponents().getSchemas().get(name) : null;
                break;
            case APISpecParserConstants.REQUEST_BODIES:
                resolved = openAPI.getComponents().getRequestBodies() != null ?
                        openAPI.getComponents().getRequestBodies().get(name) : null;
                break;
            case APISpecParserConstants.PARAMETERS:
                resolved = openAPI.getComponents().getParameters() != null ?
                        openAPI.getComponents().getParameters().get(name) : null;
                break;
            default:
                return null;
        }
        if (resolved == null) {
            log.warn("Unknown component category: " + category + " in reference: " + ref);
            return null;
        }
        if (resolved instanceof Schema && ((Schema<?>) resolved).get$ref() != null) {
            return (T) resolveComponentRef(((Schema<?>) resolved).get$ref(), openAPI, visitedRefs, expectedType);
        } else if (resolved instanceof RequestBody && ((RequestBody) resolved).get$ref() != null) {
            return (T) resolveComponentRef(((RequestBody) resolved).get$ref(), openAPI, visitedRefs, expectedType);
        } else if (resolved instanceof Parameter && ((Parameter) resolved).get$ref() != null) {
            return (T) resolveComponentRef(((Parameter) resolved).get$ref(), openAPI, visitedRefs, expectedType);
        }
        return expectedType.isInstance(resolved) ? expectedType.cast(resolved) : null;
    }

    /**
     * Adds a resource to the given {@link OpenAPI} definition with the specified HTTP verb and request body.
     * Builds the {@link Operation}, configures authentication extensions, and optionally includes a request body
     * schema.
     *
     * @param openAPI        the {@link OpenAPI} object to which the resource will be added
     * @param resource       the {@link SwaggerData.Resource} describing the path, verb, policy, and auth type
     * @param hasRequestBody whether the resource requires a JSON request body
     */
    private void addResourceToSwagger(OpenAPI openAPI, SwaggerData.Resource resource,
                                      boolean hasRequestBody) {

        Operation operation = createOperation(resource);

        if (hasRequestBody) {
            // Create request body
            RequestBody requestBody = new RequestBody();
            requestBody.setDescription("Request payload to send to the server");
            requestBody.setRequired(true);

            JSONObject typeOfPayload = new JSONObject();
            JSONObject payload = new JSONObject();
            typeOfPayload.put(APISpecParserConstants.TYPE, APISpecParserConstants.STRING);
            payload.put(APISpecParserConstants.OperationParameter.PAYLOAD_PARAM_NAME, typeOfPayload);

            Schema postSchema = new Schema();
            postSchema.setType(APISpecParserConstants.OBJECT);
            postSchema.setProperties(payload);

            MediaType mediaType = new MediaType();
            mediaType.setSchema(postSchema);

            Content content = new Content();
            content.addMediaType(APISpecParserConstants.APPLICATION_JSON_MEDIA_TYPE, mediaType);
            requestBody.setContent(content);

            operation.setRequestBody(requestBody);
        }
        if (resource.getAuthType().equals(APISpecParserConstants.AUTH_NO_AUTHENTICATION)) {
            operation.addExtension(APISpecParserConstants.SWAGGER_X_AUTH_TYPE,
                    APISpecParserConstants.AUTH_NO_AUTHENTICATION);
            operation.addExtension(APISpecParserConstants.X_WSO2_DISABLE_SECURITY, true);
        }

        Paths paths = openAPI.getPaths();
        PathItem pathItem = (paths != null) ? paths.get(resource.getPath()) : null;
        if (pathItem == null) {
            pathItem = new PathItem();
        }

        switch (resource.getVerb()) {
            case APISpecParserConstants.HTTP_POST:
                pathItem.setPost(operation);
                break;
            case APISpecParserConstants.HTTP_GET:
                pathItem.setGet(operation);
                break;
        }
        if (openAPI.getPaths() == null) {
            openAPI.setPaths(new Paths());
        }
        openAPI.getPaths().addPathItem(resource.getPath(), pathItem);
    }

    /**
     * Adds a default HTTP POST resource to the given {@link OpenAPI} definition.
     * This resource is configured with application or user-level token authentication and unlimited subscription
     * policy.
     *
     * @param openAPI the {@link OpenAPI} object to which the resource will be added
     * @param path    the resource path to add
     */
    private void addDefaultPostPathToSwagger(OpenAPI openAPI, String path) {

        SwaggerData.Resource resource = new SwaggerData.Resource();
        resource.setAuthType(APISpecParserConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
        resource.setPolicy(APISpecParserConstants.DEFAULT_SUB_POLICY_UNLIMITED);
        resource.setPath(path);
        resource.setVerb(APISpecParserConstants.HTTP_POST);
        addResourceToSwagger(openAPI, resource, true);
    }

    /**
     * Adds a default HTTP GET resource to the given {@link OpenAPI} definition.
     * This resource is configured with application or user-level token authentication and unlimited subscription
     * policy.
     *
     * @param openAPI the {@link OpenAPI} object to which the resource will be added
     * @param path    the resource path to add
     */
    private void addDefaultGetPathToSwagger(OpenAPI openAPI, String path) {

        SwaggerData.Resource resource = new SwaggerData.Resource();
        resource.setAuthType(APISpecParserConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
        resource.setPolicy(APISpecParserConstants.DEFAULT_SUB_POLICY_UNLIMITED);
        resource.setPath(path);
        resource.setVerb(APISpecParserConstants.HTTP_GET);
        addResourceToSwagger(openAPI, resource, false);
    }

    /**
     * Adds a default HTTP GET resource to the given {@link OpenAPI} definition with no authentication required.
     * Typically used for adding metadata endpoints like authentication server info.
     *
     * @param openAPI the {@link OpenAPI} object to which the resource will be added
     * @param path    the resource path to add
     */
    private void addAuthServerMetaEndpointPathToSwagger(OpenAPI openAPI, String path) {

        SwaggerData.Resource resource = new SwaggerData.Resource();
        resource.setAuthType(APISpecParserConstants.AUTH_NO_AUTHENTICATION);
        resource.setPolicy(APISpecParserConstants.DEFAULT_SUB_POLICY_UNLIMITED);
        resource.setPath(path);
        resource.setVerb(APISpecParserConstants.HTTP_GET);
        addResourceToSwagger(openAPI, resource, false);
    }

    private static class OperationMatch {
        String path;
        PathItem.HttpMethod method;
        Operation operation;

        OperationMatch(String path, PathItem.HttpMethod method, Operation operation) {
            this.path = path;
            this.method = method;
            this.operation = operation;
        }
    }
}
