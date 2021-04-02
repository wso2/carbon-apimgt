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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.models.HttpMethod;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.XmlExampleSerializer;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIResourceMediationPolicy;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.carbon.apimgt.impl.APIConstants.APPLICATION_JSON_MEDIA_TYPE;
import static org.wso2.carbon.apimgt.impl.APIConstants.APPLICATION_XML_MEDIA_TYPE;

/**
 * Models API definition using OAS (OpenAPI 3.0) parser
 */
public class OAS3Parser extends APIDefinition {
    private static final Log log = LogFactory.getLog(OAS3Parser.class);
    static final String OPENAPI_SECURITY_SCHEMA_KEY = "default";
    static final String OPENAPI_DEFAULT_AUTHORIZATION_URL = "https://test.com";
    private List<String> otherSchemes;
    private List<String> getOtherSchemes() {
        return otherSchemes;
    }
    private void setOtherSchemes(List<String> otherSchemes) {
        this.otherSchemes = otherSchemes;
    }

    /**
     * This method  generates Sample/Mock payloads for Open API Specification (3.0) definitions
     *
     * @param apiDefinition API Definition
     * @return swagger Json
     */
    @Override
    public Map<String, Object> generateExample(String apiDefinition) {
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
            //initializing apiResourceMediationPolicyObject
            APIResourceMediationPolicy apiResourceMediationPolicyObject = new APIResourceMediationPolicy();
            //setting path for apiResourceMediationPolicyObject
            apiResourceMediationPolicyObject.setPath(path);
            Map<String, Schema> definitions = swagger.getComponents().getSchemas();
            //operation map to get verb
            Map<PathItem.HttpMethod, Operation> operationMap = entry.getValue().readOperationsMap();
            List<Operation> operations = swagger.getPaths().get(path).readOperations();
            for (Operation op : operations) {
                ArrayList<Integer> responseCodes = new ArrayList<Integer>();
                //for each HTTP method get the verb
                StringBuilder genCode = new StringBuilder();
                boolean hasJsonPayload = false;
                boolean hasXmlPayload = false;
                //for setting only one initializing if condition per response code
                boolean respCodeInitialized = false;
                for (Map.Entry<PathItem.HttpMethod, Operation> HTTPMethodMap : operationMap.entrySet()) {
                    //add verb to apiResourceMediationPolicyObject
                    apiResourceMediationPolicyObject.setVerb(String.valueOf(HTTPMethodMap.getKey()));
                }
                for (String responseEntry : op.getResponses().keySet()) {
                    if (!responseEntry.equals("default")) {
                        responseCode = Integer.parseInt(responseEntry);
                        responseCodes.add(responseCode);
                        minResponseCode = Collections.min(responseCodes);
                    }
                    Content content = op.getResponses().get(responseEntry).getContent();
                    if (content != null) {
                        MediaType applicationJson = content.get(APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                        MediaType applicationXml = content.get(APIConstants.APPLICATION_XML_MEDIA_TYPE);
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
                //inserts minimum response code and mock payload variables to static script
                String finalGenCode = getMandatoryScriptSection(minResponseCode, genCode);
                //gets response section string depending on availability of json/xml payloads
                String responseConditions = getResponseConditionsSection(hasJsonPayload, hasXmlPayload);
                String finalScript = finalGenCode + responseConditions;
                apiResourceMediationPolicyObject.setContent(finalScript);
                //sets script to each resource in the swagger
                op.addExtension(APIConstants.SWAGGER_X_MEDIATION_SCRIPT, finalScript);
                apiResourceMediationPolicyList.add(apiResourceMediationPolicyObject);
            }
            checkAndSetEmptyScope(swagger);
            returnMap.put(APIConstants.SWAGGER, Json.pretty(swagger));
            returnMap.put(APIConstants.MOCK_GEN_POLICY_LIST, apiResourceMediationPolicyList);
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
                if (APIConstants.SUPPORTED_METHODS.contains(entry.getKey().name().toLowerCase())) {
                    template.setHTTPVerb(entry.getKey().name().toUpperCase());
                    template.setHttpVerbs(entry.getKey().name().toUpperCase());
                    template.setUriTemplate(pathKey);
                    List<String> opScopes = getScopeOfOperations(OPENAPI_SECURITY_SCHEMA_KEY, operation);
                    if (!opScopes.isEmpty()) {
                        if (opScopes.size() == 1) {
                            String firstScope = opScopes.get(0);
                            Scope scope = APIUtil.findScopeByKey(scopes, firstScope);
                            if (scope == null) {
                                throw new APIManagementException("Scope '" + firstScope + "' not found.");
                            }
                            template.setScope(scope);
                            template.setScopes(scope);
                        } else {
                            template = OASParserUtil.setScopesToTemplate(template, opScopes, scopes);
                        }
                    } else if (!getScopeOfOperations("OAuth2Security", operation).isEmpty()) {
                        opScopes = getScopeOfOperations("OAuth2Security", operation);
                        if (opScopes.size() == 1) {
                            String firstScope = opScopes.get(0);
                            Scope scope = APIUtil.findScopeByKey(scopes, firstScope);
                            if (scope == null) {
                                throw new APIManagementException("Scope '" + firstScope + "' not found.");
                            }
                            template.setScope(scope);
                            template.setScopes(scope);
                        } else {
                            template = OASParserUtil.setScopesToTemplate(template, opScopes, scopes);
                        }
                    }
                    Map<String, Object> extensions = operation.getExtensions();
                    if (extensions != null) {
                        if (extensions.containsKey(APIConstants.SWAGGER_X_AUTH_TYPE)) {
                            String scopeKey = (String) extensions.get(APIConstants.SWAGGER_X_AUTH_TYPE);
                            template.setAuthType(scopeKey);
                            template.setAuthTypes(scopeKey);
                        } else {
                            template.setAuthType("Any");
                            template.setAuthTypes("Any");
                        }
                        if (extensions.containsKey(APIConstants.SWAGGER_X_THROTTLING_TIER)) {
                            String throttlingTier = (String) extensions.get(APIConstants.SWAGGER_X_THROTTLING_TIER);
                            template.setThrottlingTier(throttlingTier);
                            template.setThrottlingTiers(throttlingTier);
                        }
                        if (extensions.containsKey(APIConstants.SWAGGER_X_MEDIATION_SCRIPT)) {
                            String mediationScript = (String) extensions.get(APIConstants.SWAGGER_X_MEDIATION_SCRIPT);
                            template.setMediationScript(mediationScript);
                            template.setMediationScripts(template.getHTTPVerb(), mediationScript);
                        }
                        if (extensions.containsKey(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME)) {
                            template.setAmznResourceName((String)
                                    extensions.get(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME));
                        }
                        if (extensions.containsKey(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)) {
                            template.setAmznResourceTimeout(((Number)
                                    extensions.get(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)).intValue());
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
        Map<String, SecurityScheme> securitySchemes;
        SecurityScheme securityScheme;
        OAuthFlows oAuthFlows;
        OAuthFlow oAuthFlow;
        Scopes scopes;
        if (openAPI.getComponents() != null && (securitySchemes = openAPI.getComponents().getSecuritySchemes())
                != null) {
            Set<Scope> scopeSet = new HashSet<>();
            if  ((securityScheme = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY)) != null &&
                    (oAuthFlows = securityScheme.getFlows()) != null && (oAuthFlow = oAuthFlows.getImplicit()) != null
                    && (scopes = oAuthFlow.getScopes()) != null) {
                for (Map.Entry<String, String> entry : scopes.entrySet()) {
                    Scope scope = new Scope();
                    scope.setKey(entry.getKey());
                    scope.setName(entry.getKey());
                    scope.setDescription(entry.getValue());
                    Map<String, String> scopeBindings;
                    if (oAuthFlow.getExtensions() != null && (scopeBindings =
                            (Map<String, String>) oAuthFlow.getExtensions().get(APIConstants.SWAGGER_X_SCOPES_BINDINGS))
                            != null) {
                        if (scopeBindings.get(scope.getKey()) != null) {
                            scope.setRoles(scopeBindings.get(scope.getKey()));
                        }
                    }
                    scopeSet.add(scope);
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
        updateSwaggerSecurityDefinition(openAPI, swaggerData, OPENAPI_DEFAULT_AUTHORIZATION_URL);
        updateLegacyScopesFromSwagger(openAPI, swaggerData);
        if (APIConstants.GRAPHQL_API.equals(swaggerData.getTransportType())) {
            modifyGraphQLSwagger(openAPI);
        } else {
            for (SwaggerData.Resource resource : swaggerData.getResources()) {
                addOrUpdatePathToSwagger(openAPI, resource);
            }
        }
        return Json.pretty(openAPI);
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
    public String generateAPIDefinition(SwaggerData swaggerData, String swagger) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swagger);
        return generateAPIDefinition(swaggerData, openAPI);
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
                    if (pathKey.equalsIgnoreCase(resource.getPath()) && entry.getKey().name()
                            .equalsIgnoreCase(resource.getVerb())) {
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
        if (APIConstants.GRAPHQL_API.equals(swaggerData.getTransportType())) {
            modifyGraphQLSwagger(openAPI);
        } else {
            //adding new operations to the definition
            for (SwaggerData.Resource resource : copy) {
                addOrUpdatePathToSwagger(openAPI, resource);
            }
        }
        updateSwaggerSecurityDefinition(openAPI, swaggerData, OPENAPI_DEFAULT_AUTHORIZATION_URL);
        updateLegacyScopesFromSwagger(openAPI, swaggerData);
        
        if (StringUtils.isEmpty(openAPI.getInfo().getTitle())) {
            openAPI.getInfo().setTitle(swaggerData.getTitle());
        }
        if (StringUtils.isEmpty(openAPI.getInfo().getVersion())) {
            openAPI.getInfo().setVersion(swaggerData.getVersion());
        }
        preserveResourcePathOrderFromAPI(swaggerData, openAPI);
        return Json.pretty(openAPI);
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
     * @param returnJsonContent whether to return the converted json form of the OpenAPI definition
     * @return APIDefinitionValidationResponse object with validation information
     */
    @Override
    public APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent)
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
                if (message.contains(APIConstants.OPENAPI_IS_MISSING_MSG)) {
                    ErrorItem errorItem = new ErrorItem();
                    errorItem.setErrorCode(ExceptionCodes.INVALID_OAS3_FOUND.getErrorCode());
                    errorItem.setMessage(ExceptionCodes.INVALID_OAS3_FOUND.getErrorMessage());
                    errorItem.setDescription(ExceptionCodes.INVALID_OAS3_FOUND.getErrorMessage());
                    validationResponse.getErrorItems().add(errorItem);
                }
            }
        } else {
            OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
            io.swagger.v3.oas.models.info.Info info = openAPI.getInfo();
            OASParserUtil.updateValidationResponseAsSuccess(
                    validationResponse, apiDefinition, openAPI.getOpenapi(),
                    info.getTitle(), info.getVersion(), null, info.getDescription(),
                    (openAPI.getServers()==null || openAPI.getServers().isEmpty() ) ? null :
                            openAPI.getServers().stream().map(url -> url.getUrl()).collect(Collectors.toList())
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
    public String populateCustomManagementInfo(String oasDefinition, SwaggerData swaggerData)
            throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(oasDefinition);
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
     * @param api            API
     * @param oasDefinition  OAS definition
     * @param hostsWithSchemes host addresses with protocol mapping
     * @return OAS definition
     */
    @Override
    public String getOASDefinitionForStore(API api, String oasDefinition, Map<String, String> hostsWithSchemes) {

        OpenAPI openAPI = getOpenAPI(oasDefinition);
        updateOperations(openAPI);
        updateEndpoints(api, hostsWithSchemes, openAPI);
        return updateSwaggerSecurityDefinitionForStore(openAPI, new SwaggerData(api), hostsWithSchemes);
    }

    /**
     * Update OAS definition for store
     *
     * @param product        APIProduct
     * @param oasDefinition  OAS definition
     * @param hostsWithSchemes host addresses with protocol mapping
     * @return OAS definition
     */
    @Override
    public String getOASDefinitionForStore(APIProduct product, String oasDefinition,
                                           Map<String, String> hostsWithSchemes) {

        OpenAPI openAPI = getOpenAPI(oasDefinition);
        updateOperations(openAPI);
        updateEndpoints(product, hostsWithSchemes, openAPI);
        return updateSwaggerSecurityDefinitionForStore(openAPI, new SwaggerData(product), hostsWithSchemes);
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
    public String getOASDefinitionForPublisher(API api, String oasDefinition) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(oasDefinition);
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
        oAuthFlow.setAuthorizationUrl(OPENAPI_DEFAULT_AUTHORIZATION_URL);

        if (api.getAuthorizationHeader() != null) {
            openAPI.addExtension(APIConstants.X_WSO2_AUTH_HEADER, api.getAuthorizationHeader());
        }
        if (api.getApiLevelPolicy() != null) {
            openAPI.addExtension(APIConstants.X_THROTTLING_TIER, api.getApiLevelPolicy());
        }
        openAPI.addExtension(APIConstants.X_WSO2_CORS, api.getCorsConfiguration());
        Object prodEndpointObj = OASParserUtil.generateOASConfigForEndpoints(api, true);
        if (prodEndpointObj != null) {
            openAPI.addExtension(APIConstants.X_WSO2_PRODUCTION_ENDPOINTS, prodEndpointObj);
        }
        Object sandEndpointObj = OASParserUtil.generateOASConfigForEndpoints(api, false);
        if (sandEndpointObj != null) {
            openAPI.addExtension(APIConstants.X_WSO2_SANDBOX_ENDPOINTS, sandEndpointObj);
        }
        openAPI.addExtension(APIConstants.X_WSO2_BASEPATH, api.getContext());
        if (api.getTransports() != null) {
            openAPI.addExtension(APIConstants.X_WSO2_TRANSPORTS, api.getTransports().split(","));
        }
        String apiSecurity = api.getApiSecurity();
        // set mutual ssl extension if enabled
        if (apiSecurity != null) {
            List<String> securityList = Arrays.asList(apiSecurity.split(","));
            if (securityList.contains(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                String mutualSSLOptional = !securityList.contains(APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY) ?
                        APIConstants.OPTIONAL : APIConstants.MANDATORY;
                openAPI.addExtension(APIConstants.X_WSO2_MUTUAL_SSL, mutualSSLOptional);
            }
        }
        // This app security is should given in resource level,
        // otherwise the default oauth2 scheme defined at each resouce level will override application securities
        JsonNode appSecurityExtension = OASParserUtil.getAppSecurity(apiSecurity);
        for (String pathKey : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(pathKey);
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                Operation operation = entry.getValue();
                operation.addExtension(APIConstants.X_WSO2_APP_SECURITY, appSecurityExtension);
            }
        }
        openAPI.addExtension(APIConstants.X_WSO2_RESPONSE_CACHE,
                OASParserUtil.getResponseCacheConfig(api.getResponseCache(), api.getCacheTimeout()));
        return Json.pretty(openAPI);
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
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_SCOPE)) {
            String scopeKey = (String) extensions.get(APIConstants.SWAGGER_X_SCOPE);
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
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_WSO2_SECURITY)) {
            Map<String, Object> securityDefinitions =
                    (Map<String, Object>) extensions.get(APIConstants.SWAGGER_X_WSO2_SECURITY);
            for (Map.Entry<String, Object> entry : securityDefinitions.entrySet()) {
                Map<String, Object> securityDefinition = (Map<String, Object>) entry.getValue();
                if (securityDefinition.containsKey(APIConstants.SWAGGER_X_WSO2_SCOPES)) {
                    List<Map<String, String>> oauthScope =
                            (List<Map<String, String>>) securityDefinition.get(APIConstants.SWAGGER_X_WSO2_SCOPES);
                    for (Map<String, String> anOauthScope : oauthScope) {
                        Scope scope = new Scope();
                        scope.setKey(anOauthScope.get(APIConstants.SWAGGER_SCOPE_KEY));
                        scope.setName(anOauthScope.get(APIConstants.SWAGGER_NAME));
                        scope.setDescription(anOauthScope.get(APIConstants.SWAGGER_DESCRIPTION));
                        scope.setRoles(anOauthScope.get(APIConstants.SWAGGER_ROLES));
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
    private void updateSwaggerSecurityDefinition(OpenAPI openAPI, SwaggerData swaggerData, String authUrl) {

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
        oAuthFlow.setAuthorizationUrl(authUrl);
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
            oAuthFlow.addExtension(APIConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
        }
        oAuthFlow.setScopes(oas3Scopes);
    }

    /**
     * Remove legacy scope from swagger
     *
     * @param openAPI
     */
    private void updateLegacyScopesFromSwagger(OpenAPI openAPI, SwaggerData swaggerData) {

        Map<String, Object> extensions = openAPI.getExtensions();
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_WSO2_SECURITY)) {
            extensions.remove(APIConstants.SWAGGER_X_WSO2_SECURITY);
        }
    }

    /**
     * Set scopes to the openAPI extension
     *
     * @param openAPI     OpenAPI object
     * @param swaggerData Swagger API data
     */
    private void setLegacyScopeExtensionToSwagger(OpenAPI openAPI, SwaggerData swaggerData) {
        Set<Scope> scopes = swaggerData.getScopes();

        if (scopes != null && !scopes.isEmpty()) {
            List<Map<String, String>> xSecurityScopesArray = new ArrayList<>();
            for (Scope scope : scopes) {
                Map<String, String> xWso2ScopesObject = new LinkedHashMap<>();
                xWso2ScopesObject.put(APIConstants.SWAGGER_SCOPE_KEY, scope.getKey());
                xWso2ScopesObject.put(APIConstants.SWAGGER_NAME, scope.getName());
                xWso2ScopesObject.put(APIConstants.SWAGGER_ROLES, scope.getRoles());
                xWso2ScopesObject.put(APIConstants.SWAGGER_DESCRIPTION, scope.getDescription());
                xSecurityScopesArray.add(xWso2ScopesObject);
            }
            Map<String, Object> xWSO2Scopes = new LinkedHashMap<>();
            xWSO2Scopes.put(APIConstants.SWAGGER_X_WSO2_SCOPES, xSecurityScopesArray);
            Map<String, Object> xWSO2SecurityDefinitionObject = new LinkedHashMap<>();
            xWSO2SecurityDefinitionObject.put(APIConstants.SWAGGER_OBJECT_NAME_APIM, xWSO2Scopes);

            openAPI.addExtension(APIConstants.SWAGGER_X_WSO2_SECURITY, xWSO2SecurityDefinitionObject);
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
        apiResponses.addApiResponse(APIConstants.SWAGGER_RESPONSE_200, apiResponse);
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
        if (APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN.equals(authType)) {
            authType = "Application & Application User";
        }
        if (APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authType)) {
            authType = "Application User";
        }
        if (APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authType)) {
            authType = "Application";
        }
        operation.addExtension(APIConstants.SWAGGER_X_AUTH_TYPE, authType);
        operation.addExtension(APIConstants.SWAGGER_X_THROTTLING_TIER, resource.getPolicy());
        // AWS Lambda: set arn & timeout to swagger
        if (resource.getAmznResourceName() != null) {
            operation.addExtension(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME, resource.getAmznResourceName());
        }
        if (resource.getAmznResourceTimeout() != 0) {
            operation.addExtension(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT, resource.getAmznResourceTimeout());
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
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_SCOPE)) {
            extensions.remove(APIConstants.SWAGGER_X_SCOPE);
        }
    }

    /**
     * Update OAS definition with authorization endpoints.
     *
     * @param openAPI        OpenAPI
     * @param swaggerData    SwaggerData
     * @param hostsWithSchemes GW hosts with protocols
     * @return updated OAS definition
     */
    private String updateSwaggerSecurityDefinitionForStore(OpenAPI openAPI, SwaggerData swaggerData,
            Map<String,String> hostsWithSchemes) {

        String authUrl;
        // By Default, add the GW host with HTTPS protocol if present.
        if (hostsWithSchemes.containsKey(APIConstants.HTTPS_PROTOCOL)) {
            authUrl = (hostsWithSchemes.get(APIConstants.HTTPS_PROTOCOL)).concat("/authorize");
        } else {
            authUrl = (hostsWithSchemes.get(APIConstants.HTTP_PROTOCOL)).concat("/authorize");
        }
        updateSwaggerSecurityDefinition(openAPI, swaggerData, authUrl);
        return Json.pretty(openAPI);
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
        updateEndpoints(openAPI, basePath, transports, hostsWithSchemes);
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
        updateEndpoints(openAPI, basePath, transports, hostsWithSchemes);
    }

    /**
     * Update OAS definition with GW endpoints and API information
     *
     * @param openAPI          OpenAPI
     * @param basePath         API context
     * @param transports       transports types
     * @param hostsWithSchemes GW hosts with protocol mapping
     */
    private void updateEndpoints(OpenAPI openAPI, String basePath, String transports,
                                 Map<String, String> hostsWithSchemes) {

        String[] apiTransports = transports.split(",");
        List<Server> servers = new ArrayList<>();
        if (ArrayUtils.contains(apiTransports, APIConstants.HTTPS_PROTOCOL) && hostsWithSchemes
                .containsKey(APIConstants.HTTPS_PROTOCOL)) {
            String host = hostsWithSchemes.get(APIConstants.HTTPS_PROTOCOL).trim()
                    .replace(APIConstants.HTTPS_PROTOCOL_URL_PREFIX, "");
            String httpsURL = APIConstants.HTTPS_PROTOCOL + "://" + host + basePath;
            Server httpsServer = new Server();
            httpsServer.setUrl(httpsURL);
            servers.add(httpsServer);
        }
        if (ArrayUtils.contains(apiTransports, APIConstants.HTTP_PROTOCOL) && hostsWithSchemes
                .containsKey(APIConstants.HTTP_PROTOCOL)) {
            String host = hostsWithSchemes.get(APIConstants.HTTP_PROTOCOL).trim()
                    .replace(APIConstants.HTTP_PROTOCOL_URL_PREFIX, "");
            String httpURL = APIConstants.HTTP_PROTOCOL + "://" + host + basePath;
            Server httpsServer = new Server();
            httpsServer.setUrl(httpURL);
            servers.add(httpsServer);
        }
        openAPI.setServers(servers);
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
                    if (extensions.containsKey(APIConstants.SWAGGER_X_MEDIATION_SCRIPT)) {
                        extensions.remove(APIConstants.SWAGGER_X_MEDIATION_SCRIPT);
                    }
                    // set x-scope value to security definition if it not there.
                    if (extensions.containsKey(APIConstants.SWAGGER_X_WSO2_SCOPES)) {
                        String scope = (String) extensions.get(APIConstants.SWAGGER_X_WSO2_SCOPES);
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
     * Construct openAPI definition for graphQL. Add get and post operations
     *
     * @param openAPI OpenAPI
     * @return modified openAPI for GraphQL
     */
    private void modifyGraphQLSwagger(OpenAPI openAPI) {
        SwaggerData.Resource resource = new SwaggerData.Resource();
        resource.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
        resource.setPolicy(APIConstants.DEFAULT_SUB_POLICY_UNLIMITED);
        resource.setPath("/");
        resource.setVerb(APIConstants.HTTP_POST);
        Operation postOperation = createOperation(resource);

        //post operation
        RequestBody requestBody = new RequestBody();
        requestBody.setDescription("Query or mutation to be passed to graphQL API");
        requestBody.setRequired(true);

        JSONObject typeOfPayload = new JSONObject();
        JSONObject payload = new JSONObject();
        typeOfPayload.put(APIConstants.TYPE, APIConstants.STRING);
        payload.put(APIConstants.OperationParameter.PAYLOAD_PARAM_NAME, typeOfPayload);

        Schema postSchema = new Schema();
        postSchema.setType(APIConstants.OBJECT);
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
            swagger.addExtension(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH, true);
            // no need to check resource levels since both cannot exist at the same time.
            log.debug("API Level policy is content aware..");
            return Json.pretty(swagger);
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
                            .contains(op.getExtensions().get(APIConstants.SWAGGER_X_THROTTLING_TIER))) {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    "API resource Level policy is content aware for operation " + op.getOperationId());
                        }
                        op.addExtension(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH, true);
                    }
                }
            }
            return Json.pretty(swagger);
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
    public String processOtherSchemeScopes(String swaggerContent) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent);
        Set<Scope> legacyScopes = getScopesFromExtensions(openAPI);

        //In case default scheme already exists we check whether the legacy x-wso2-scopes are there in the default scheme
        //If not we proceed to process legacy scopes to make sure old local scopes work in migrated pack too.
        //This is to fix https://github.com/wso2/product-apim/issues/8724
        if (isDefaultGiven(swaggerContent) && !legacyScopes.isEmpty()) {
            SecurityScheme defaultScheme = openAPI.getComponents().getSecuritySchemes()
                    .get(OPENAPI_SECURITY_SCHEMA_KEY);
            OAuthFlows oAuthFlows = defaultScheme.getFlows();
            if (oAuthFlows != null) {
                OAuthFlow oAuthFlow = oAuthFlows.getImplicit();
                if (oAuthFlow != null) {
                    Scopes defaultScopes = oAuthFlow.getScopes();
                    if (defaultScopes != null) {
                        for (Scope legacyScope : legacyScopes) {
                            if (!defaultScopes.containsKey(legacyScope.getKey())) {
                                openAPI = processLegacyScopes(openAPI);
                                return Json.pretty(openAPI);
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
            return Json.pretty(openAPI);
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
    public String injectMgwThrottlingExtensionsToDefault(String swaggerContent) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent);
        Paths paths = openAPI.getPaths();
        for (String pathKey : paths.keySet()) {
            Map<PathItem.HttpMethod, Operation> operationsMap = paths.get(pathKey).readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : operationsMap.entrySet()) {
                Operation operation = entry.getValue();
                Map<String, Object> extensions = operation.getExtensions();
                if (extensions != null && extensions.containsKey(APIConstants.X_WSO2_THROTTLING_TIER)) {
                    Object tier = extensions.get(APIConstants.X_WSO2_THROTTLING_TIER);
                    extensions.remove(APIConstants.X_WSO2_THROTTLING_TIER);
                    extensions.put(APIConstants.SWAGGER_X_THROTTLING_TIER, tier);
                }
            }
        }
        return Json.pretty(openAPI);
    }

    @Override
    public String copyVendorExtensions(String existingOASContent, String updatedOASContent) {

        OpenAPI existingOpenAPI = getOpenAPI(existingOASContent);
        OpenAPI updatedOpenAPI = getOpenAPI(updatedOASContent);
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
        return Json.pretty(updatedOpenAPI);
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
            oAuthFlow.setAuthorizationUrl(OPENAPI_DEFAULT_AUTHORIZATION_URL);
            Scopes oas3Scopes = oAuthFlow.getScopes() != null ? oAuthFlow.getScopes() : new Scopes();

            if (scopes != null && !scopes.isEmpty()) {
                Map<String, String> scopeBindings = new HashMap<>();
                if (oAuthFlow.getExtensions() != null) {
                    scopeBindings =
                            (Map<String, String>) oAuthFlow.getExtensions().get(APIConstants.SWAGGER_X_SCOPES_BINDINGS)
                                    != null ?
                                    (Map<String, String>) oAuthFlow.getExtensions()
                                            .get(APIConstants.SWAGGER_X_SCOPES_BINDINGS) :
                                    new HashMap<>();

                }
                for (Scope scope : scopes) {
                    oas3Scopes.put(scope.getKey(), scope.getDescription());
                    String roles = (StringUtils.isNotBlank(scope.getRoles())
                            && scope.getRoles().trim().split(",").length > 0)
                            ? scope.getRoles() : StringUtils.EMPTY;
                    scopeBindings.put(scope.getKey(), roles);
                }
                oAuthFlow.addExtension(APIConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
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
        Map<String, SecurityScheme> securitySchemes ;
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

        for (Map.Entry<String, String> input : noneDefaultFlowScopes.entrySet()) {
            //Inject scopes set into default scheme
            defaultFlowScopes.addString(input.getKey(), input.getValue());
        }
        defaultTypeFlow.setScopes(defaultFlowScopes);
        //Check X-Scope Bindings
        Map<String, String> noneDefaultScopeBindings = null;
        Map<String, Object> defaultTypeExtension = defaultTypeFlow.getExtensions();
        if (defaultTypeExtension == null) {
            defaultTypeExtension = new HashMap<>();
        }
        if (noneDefaultTypeFlow.getExtensions() != null && (noneDefaultScopeBindings =
                (Map<String, String>) noneDefaultTypeFlow.getExtensions().get(APIConstants.SWAGGER_X_SCOPES_BINDINGS))
                != null) {
            defaultScopeBindings = (Map<String, String>) defaultTypeExtension.get(APIConstants.SWAGGER_X_SCOPES_BINDINGS);
            if (defaultScopeBindings == null) {
                defaultScopeBindings = new HashMap<>();
            }
            for (Map.Entry<String, String> roleInUse : noneDefaultScopeBindings.entrySet()) {
                defaultScopeBindings.put(roleInUse.getKey(), roleInUse.getValue());
            }
        }
        defaultTypeExtension.put(APIConstants.SWAGGER_X_SCOPES_BINDINGS, defaultScopeBindings);
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
                if (APIConstants.SUPPORTED_METHODS.contains(httpMethod.name().toLowerCase())) {
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
        //Setup application Security
        List<String> applicationSecurity = OASParserUtil.getApplicationSecurityTypes(extensions);
        Boolean isOptional = OASParserUtil.getAppSecurityStateFromSwagger(extensions);
        if (!applicationSecurity.isEmpty()) {
            String securityList = api.getApiSecurity();
            securityList = securityList == null ? "" : securityList;
            for (String securityType : applicationSecurity) {
                if (APIConstants.DEFAULT_API_SECURITY_OAUTH2.equals(securityType) &&
                        !securityList.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
                    securityList = securityList + "," + APIConstants.DEFAULT_API_SECURITY_OAUTH2;
                }
                if (APIConstants.API_SECURITY_BASIC_AUTH.equals(securityType) &&
                        !securityList.contains(APIConstants.API_SECURITY_BASIC_AUTH)) {
                    securityList = securityList + "," + APIConstants.API_SECURITY_BASIC_AUTH;
                }
                if (APIConstants.API_SECURITY_API_KEY.equals(securityType) &&
                        !securityList.contains(APIConstants.API_SECURITY_API_KEY)) {
                    securityList = securityList + "," + APIConstants.API_SECURITY_API_KEY;
                }
            }
            if (!(isOptional || securityList.contains(APIConstants.MANDATORY))) {
                securityList = securityList + "," + APIConstants.MANDATORY;
            }
            api.setApiSecurity(securityList);
        }
        //Setup mutualSSL configuration
        String mutualSSL = OASParserUtil.getMutualSSLEnabledFromSwagger(extensions);
        if (StringUtils.isNotBlank(mutualSSL)) {
            String securityList = api.getApiSecurity();
            if (StringUtils.isBlank(securityList)) {
                securityList = APIConstants.DEFAULT_API_SECURITY_OAUTH2;
            }
            if (APIConstants.OPTIONAL.equals(mutualSSL) &&
                    !securityList.contains(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                securityList = securityList + "," + APIConstants.API_SECURITY_MUTUAL_SSL;
            } else if (APIConstants.MANDATORY.equals(mutualSSL) &&
                    !securityList.contains(APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY)){
                securityList = securityList + "," + APIConstants.API_SECURITY_MUTUAL_SSL + "," +
                        APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY;
            }
            api.setApiSecurity(securityList);
        }
        //Setup CORSConfigurations
        CORSConfiguration corsConfiguration = OASParserUtil.getCorsConfigFromSwagger(extensions);
        if (corsConfiguration != null) {
            api.setCorsConfiguration(corsConfiguration);
        }
        //Setup Response cache enabling
        boolean responseCacheEnable = OASParserUtil.getResponseCacheFromSwagger(extensions);
        if (responseCacheEnable) {
            api.setResponseCache(APIConstants.ENABLED);
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
                            .contains(APIConstants.SWAGGER_X_EXAMPLES)) {
                        operation.getExtensions().remove(APIConstants.SWAGGER_X_EXAMPLES);
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
    public String processDisableSecurityExtension(String swaggerContent) throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(swaggerContent);
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
                    resourceExtensions.put(APIConstants.SWAGGER_X_AUTH_TYPE, "None");
                    if (extensionsAreEmpty) {
                        operation.setExtensions(resourceExtensions);
                    }
                } else if (resourceExtensions != null && resourceExtensions.containsKey(APIConstants.X_WSO2_DISABLE_SECURITY)) {
                    //Check Disable Security is enabled in resource level
                    boolean resourceLevelDisableSecurity = Boolean.parseBoolean(String.valueOf(resourceExtensions.get(APIConstants.X_WSO2_DISABLE_SECURITY)));
                    if (resourceLevelDisableSecurity) {
                        resourceExtensions.put(APIConstants.SWAGGER_X_AUTH_TYPE, "None");
                    }
                }
            }
        }
        return Json.pretty(openAPI);
    }

}
