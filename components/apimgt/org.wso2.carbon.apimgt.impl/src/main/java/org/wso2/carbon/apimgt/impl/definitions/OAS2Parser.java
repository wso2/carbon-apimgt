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
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.models.Example;
import io.swagger.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.models.Contact;
import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.RefPath;
import io.swagger.models.RefResponse;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.DeserializationUtils;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import java.io.IOException;
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
import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_APIM_DEFAULT_SECURITY;
import static org.wso2.carbon.apimgt.impl.APIConstants.SWAGGER_APIM_RESTAPI_SECURITY;

/**
 * Models API definition using OAS (swagger 2.0) parser
 */
public class OAS2Parser extends APIDefinition {
    private static final Log log = LogFactory.getLog(OAS2Parser.class);
    private static final String SWAGGER_SECURITY_SCHEMA_KEY = "default";
    private List<String> otherSchemes;
    private List<String> getOtherSchemes() {
        return otherSchemes;
    }
    private void setOtherSchemes(List<String> otherSchemes) {
        this.otherSchemes = otherSchemes;
    }

    /**
     * This method  generates Sample/Mock payloads for Swagger (2.0) definitions
     *
     * @param swaggerDef Swagger Definition
     * @return Swagger Json
     */
    @Override
    public Map<String, Object> generateExample(String swaggerDef) {
        // create APIResourceMediationPolicy List = policyList
        SwaggerParser parser = new SwaggerParser();
        SwaggerDeserializationResult parseAttemptForV2 = parser.readWithInfo(swaggerDef);
        Swagger swagger = parseAttemptForV2.getSwagger();
        //return map
        Map<String, Object> returnMap = new HashMap<>();
        //List for APIResMedPolicyList
        List<APIResourceMediationPolicy> apiResourceMediationPolicyList = new ArrayList<>();
        for (Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
            int responseCode = 0;
            int minResponseCode = 0;
            String path = entry.getKey();
            //initializing apiResourceMediationPolicyObject
            APIResourceMediationPolicy apiResourceMediationPolicyObject = new APIResourceMediationPolicy();
            //setting path for apiResourceMediationPolicyObject
            apiResourceMediationPolicyObject.setPath(path);
            Map<String, Model> definitions = swagger.getDefinitions();
            //operation map to get verb
            Map<HttpMethod, Operation> operationMap = entry.getValue().getOperationMap();
            List<Operation> operations = swagger.getPaths().get(path).getOperations();
            for (Operation op : operations) {
                ArrayList<Integer> responseCodes = new ArrayList<Integer>();
                //for each HTTP method get the verb
                for (Map.Entry<HttpMethod, Operation> HTTPMethodMap : operationMap.entrySet()) {
                    //add verb to apiResourceMediationPolicyObject
                    apiResourceMediationPolicyObject.setVerb(String.valueOf(HTTPMethodMap.getKey()));
                }
                StringBuilder genCode = new StringBuilder();
                boolean hasJsonPayload = false;
                boolean hasXmlPayload = false;
                //for setting only one initializing if condition per response code
                boolean respCodeInitialized = false;
                for (String responseEntry : op.getResponses().keySet()) {
                    if (!responseEntry.equals("default")) {
                        responseCode = Integer.parseInt(responseEntry);
                        responseCodes.add(responseCode);
                        minResponseCode = Collections.min(responseCodes);
                    }
                    if (op.getResponses().get(responseEntry).getExamples() != null) {
                        Object applicationJson = op.getResponses().get(responseEntry).getExamples().get(APPLICATION_JSON_MEDIA_TYPE);
                        Object applicationXml = op.getResponses().get(responseEntry).getExamples().get(APPLICATION_XML_MEDIA_TYPE);
                        if (applicationJson != null) {
                            String jsonExample = Json.pretty(applicationJson);
                            genCode.append(getGeneratedResponsePayloads(responseEntry, jsonExample, "json", false));
                            respCodeInitialized = true;
                            hasJsonPayload = true;
                        }
                        if (applicationXml != null) {
                            String xmlExample = applicationXml.toString();
                            genCode.append(getGeneratedResponsePayloads(responseEntry, xmlExample, "xml", respCodeInitialized));
                            hasXmlPayload = true;
                        }
                    } else if (op.getResponses().get(responseEntry).getResponseSchema() != null) {
                        Model model = op.getResponses().get(responseEntry).getResponseSchema();
                        String schemaExample = getSchemaExample(model, definitions, new HashSet<String>());
                        genCode.append(getGeneratedResponsePayloads(responseEntry, schemaExample, "json", respCodeInitialized));
                        hasJsonPayload = true;
                    } else if (op.getResponses().get(responseEntry).getExamples() == null
                            && op.getResponses().get(responseEntry).getResponseSchema() == null) {
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
                apiResourceMediationPolicyList.add(apiResourceMediationPolicyObject);
                //sets script to each resource in the swagger
                op.setVendorExtension(APIConstants.SWAGGER_X_MEDIATION_SCRIPT, finalScript);
            }
            returnMap.put(APIConstants.SWAGGER, Json.pretty(swagger));
            returnMap.put(APIConstants.MOCK_GEN_POLICY_LIST, apiResourceMediationPolicyList);
        }
        return returnMap;
    }

    /**
     * This method  generates Sample/Mock payloads of Schema Examples for operations in the swagger definition
     * @param model model
     * @param definitions definitions
     * @return Example Json
     */
    private String getSchemaExample(Model model, Map<String, Model> definitions, HashSet<String> strings) {
        Example example = ExampleBuilder.fromModel("Model", model, definitions, new HashSet<String>());
        SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
        Json.mapper().registerModule(simpleModule);
        return Json.pretty(example);
    }

    /**
     * Sets default script
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
            genRespPayload.append("\nif (!responses[").append(responseCode).append("]) {").append("\n responses [")
                    .append(responseCode).append("] = [];").append("\n}");
        }
        genRespPayload.append("\nresponses[").append(responseCode).append("][\"application/").append(type)
                .append("\"] = ").append(example).append(";\n");
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
                "\nvar responses = [];\n" +
                "\nvar responseCodeSC;\n" +
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
                " responseCode = 501;\n" +
                "}\n\n" +
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
        Swagger swagger = getSwagger(resourceConfigsJSON);
        Set<URITemplate> urlTemplates = new LinkedHashSet<>();
        Set<Scope> scopes = getScopes(resourceConfigsJSON);
        String oauth2SchemeKey = getOAuth2SecuritySchemeKey(swagger);

        for (String pathString : swagger.getPaths().keySet()) {
            Path path = swagger.getPath(pathString);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                Operation operation = entry.getValue();
                URITemplate template = new URITemplate();
                template.setHTTPVerb(entry.getKey().name().toUpperCase());
                template.setHttpVerbs(entry.getKey().name().toUpperCase());
                template.setUriTemplate(pathString);
                List<String> opScopes = getScopeOfOperations(oauth2SchemeKey, operation);
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
                }
                Map<String, Object> extensions = operation.getVendorExtensions();
                if (extensions != null) {
                    if (extensions.containsKey(APIConstants.SWAGGER_X_AUTH_TYPE)) {
                        String authType = (String) extensions.get(APIConstants.SWAGGER_X_AUTH_TYPE);
                        template.setAuthType(authType);
                        template.setAuthTypes(authType);
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
                }
                urlTemplates.add(template);
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
        Swagger swagger = getSwagger(resourceConfigsJSON);
        String oauth2SchemeKey = getOAuth2SecuritySchemeKey(swagger);

        Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
        OAuth2Definition oAuth2Definition;
        if (securityDefinitions != null
                && (oAuth2Definition = (OAuth2Definition) securityDefinitions.get(oauth2SchemeKey)) != null
                && oAuth2Definition.getScopes() != null) {
            Set<Scope> scopeSet = new LinkedHashSet<>();
            for (Map.Entry<String, String> entry : oAuth2Definition.getScopes().entrySet()) {
                Scope scope = new Scope();
                scope.setKey(entry.getKey());
                scope.setName(entry.getKey());
                scope.setDescription(entry.getValue());
                Map<String, String> scopeBindings;
                if (oAuth2Definition.getVendorExtensions() != null && (scopeBindings =
                        (Map<String, String>) oAuth2Definition.getVendorExtensions()
                                .get(APIConstants.SWAGGER_X_SCOPES_BINDINGS)) != null) {
                    if (scopeBindings.get(scope.getKey()) != null) {
                        scope.setRoles(scopeBindings.get(scope.getKey()));
                    }
                }
                scopeSet.add(scope);
            }
            return OASParserUtil.sortScopes(scopeSet);
        } else {
            return OASParserUtil.sortScopes(getScopesFromExtensions(swagger));
        }
    }

    /**
     * Get scope information from the extensions
     *
     * @param swagger swagger object
     * @return Scope set
     */
    private Set<Scope> getScopesFromExtensions(Swagger swagger) throws APIManagementException {
        Set<Scope> scopeList = new LinkedHashSet<>();
        Map<String, Object> extensions = swagger.getVendorExtensions();
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
     * This method generates API definition to the given api
     *
     * @param swaggerData api
     * @return API definition in string format
     * @throws APIManagementException
     */
    @Override
    public String generateAPIDefinition(SwaggerData swaggerData) throws APIManagementException {
        Swagger swagger = new Swagger();

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
        swagger.setInfo(info);
        updateSwaggerSecurityDefinition(swagger, swaggerData, "https://test.com");
        updateLegacyScopesFromSwagger(swagger, swaggerData);
        for (SwaggerData.Resource resource : swaggerData.getResources()) {
            addOrUpdatePathToSwagger(swagger, resource);
        }

        return getSwaggerJsonString(swagger);
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
        Swagger swaggerObj = getSwagger(swagger);
        return generateAPIDefinition(swaggerData, swaggerObj);
    }

    /**
     * This method generates API definition using the given api's URI templates and the swagger.
     * It will alter the provided swagger definition based on the URI templates. For example: if there is a new
     * URI template which is not included in the swagger, it will be added to the swagger as a basic resource. Any
     * additional resources inside the swagger will be removed from the swagger. Changes to scopes, throtting policies,
     * on the resource will be updated on the swagger
     *
     * @param swaggerData api
     * @param swaggerObj  swagger
     * @return API definition in string format
     * @throws APIManagementException if error occurred when generating API Definition
     */
    private String generateAPIDefinition(SwaggerData swaggerData, Swagger swaggerObj) throws APIManagementException {
        //Generates below model using the API's URI template
        // path -> [verb1 -> template1, verb2 -> template2, ..]
        Map<String, Map<String, SwaggerData.Resource>> resourceMap = getResourceMap(swaggerData);

        Iterator<Map.Entry<String, Path>> itr = swaggerObj.getPaths().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Path> pathEntry = itr.next();
            String pathName = pathEntry.getKey();
            Path path = pathEntry.getValue();
            Map<String, SwaggerData.Resource> resourcesForPath = resourceMap.get(pathName);
            if (resourcesForPath == null) {
                //remove paths that are not in URI Templates
                itr.remove();
            } else {
                //If path is available in the URI template, then check for operations(verbs)
                for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                    HttpMethod httpMethod = operationEntry.getKey();
                    Operation operation = operationEntry.getValue();
                    SwaggerData.Resource resource = resourcesForPath.get(httpMethod.toString().toUpperCase());
                    if (resource == null) {
                        // if particular operation is not available in URI templates, then remove it from swagger
                        path.set(httpMethod.toString().toLowerCase(), null);
                    } else {
                        // if operation is available in URI templates, update swagger operation
                        // with auth type, scope etc
                        updateOperationManagedInfo(resource, operation);
                    }
                }

                // if there are any verbs (operations) not defined in swagger then add them
                for (Map.Entry<String, SwaggerData.Resource> resourcesForPathEntry : resourcesForPath.entrySet()) {
                    String verb = resourcesForPathEntry.getKey();
                    SwaggerData.Resource resource = resourcesForPathEntry.getValue();
                    HttpMethod method = HttpMethod.valueOf(verb.toUpperCase());
                    Operation operation = path.getOperationMap().get(method);
                    if (operation == null) {
                        operation = createOperation(resource);
                        path.set(resource.getVerb().toLowerCase(), operation);
                    }
                }
            }
        }

        // add to swagger if there are any new templates
        for (Map.Entry<String, Map<String, SwaggerData.Resource>> resourceMapEntry : resourceMap.entrySet()) {
            String path = resourceMapEntry.getKey();
            Map<String, SwaggerData.Resource> verbMap = resourceMapEntry.getValue();
            if (swaggerObj.getPath(path) == null) {
                for (Map.Entry<String, SwaggerData.Resource> verbMapEntry : verbMap.entrySet()) {
                    SwaggerData.Resource resource = verbMapEntry.getValue();
                    addOrUpdatePathToSwagger(swaggerObj, resource);
                }
            }
        }

        updateSwaggerSecurityDefinition(swaggerObj, swaggerData, "https://test.com");
        updateLegacyScopesFromSwagger(swaggerObj, swaggerData);
        
        if (StringUtils.isEmpty(swaggerObj.getInfo().getTitle())) {
            swaggerObj.getInfo().setTitle(swaggerData.getTitle());
        }
        if (StringUtils.isEmpty(swaggerObj.getInfo().getVersion())) {
            swaggerObj.getInfo().setVersion(swaggerData.getVersion());
        }
        preserveResourcePathOrderFromAPI(swaggerData, swaggerObj);
        return getSwaggerJsonString(swaggerObj);
    }

    /**
     * Preserve and rearrange the Swagger definition according to the resource path order of the updating API payload.
     *
     * @param swaggerData Updating API swagger data
     * @param swaggerObj  Updated Swagger definition
     */
    private void preserveResourcePathOrderFromAPI(SwaggerData swaggerData, Swagger swaggerObj) {

        Set<String> orderedResourcePaths = new LinkedHashSet<>();
        Map<String, Path> orderedSwaggerPaths = new LinkedHashMap<>();
        // Iterate the URI template order given in the updating API payload (Swagger Data) and rearrange resource paths
        // order in OpenAPI with relevance to the first matching resource path item from the swagger data path list.
        for (SwaggerData.Resource resource : swaggerData.getResources()) {
            String path = resource.getPath();
            if (!orderedResourcePaths.contains(path)) {
                orderedResourcePaths.add(path);
                // Get the resource path item for the path from existing Swagger
                orderedSwaggerPaths.put(path, swaggerObj.getPath(path));
            }
        }
        swaggerObj.setPaths(orderedSwaggerPaths);
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
        SwaggerParser parser = new SwaggerParser();
        SwaggerDeserializationResult parseAttemptForV2 = parser.readWithInfo(apiDefinition);
        boolean swaggerErrorFound = false;
        for (String message : parseAttemptForV2.getMessages()) {
            OASParserUtil.addErrorToValidationResponse(validationResponse, message);
            if (message.contains(APIConstants.SWAGGER_IS_MISSING_MSG)) {
                ErrorItem errorItem = new ErrorItem();
                errorItem.setErrorCode(ExceptionCodes.INVALID_OAS2_FOUND.getErrorCode());
                errorItem.setMessage(ExceptionCodes.INVALID_OAS2_FOUND.getErrorMessage());
                errorItem.setDescription(ExceptionCodes.INVALID_OAS2_FOUND.getErrorMessage());
                validationResponse.getErrorItems().add(errorItem);
                swaggerErrorFound = true;
            }
        }
        if (parseAttemptForV2.getSwagger() == null || swaggerErrorFound) {
            validationResponse.setValid(false);
        } else {
            Swagger swagger = parseAttemptForV2.getSwagger();
            Info info = swagger.getInfo();
            OASParserUtil.updateValidationResponseAsSuccess(
                    validationResponse, apiDefinition, swagger.getSwagger(),
                    info.getTitle(), info.getVersion(), swagger.getBasePath(), info.getDescription(),
                    (swagger.getHost() == null || swagger.getHost().isEmpty()) ? null :
                            new ArrayList<String>(Arrays.asList(swagger.getHost()))
            );
            validationResponse.setParser(this);
            if (returnJsonContent) {
                if (!apiDefinition.trim().startsWith("{")) { // not a json (it is yaml)
                    try {
                        JsonNode jsonNode = DeserializationUtils.readYamlTree(apiDefinition);
                        validationResponse.setJsonContent(jsonNode.toString());
                    } catch (IOException e) {
                        throw new APIManagementException("Error while reading API definition yaml", e);
                    }
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
     * @param swaggerData   API
     * @return Generated OAS definition
     * @throws APIManagementException If an error occurred
     */
    @Override
    public String populateCustomManagementInfo(String oasDefinition, SwaggerData swaggerData)
            throws APIManagementException {
        Swagger swagger = getSwagger(oasDefinition);
        removePublisherSpecificInfo(swagger);
        return generateAPIDefinition(swaggerData, swagger);
    }

    /**
     * Remove MG related information
     *
     * @param swagger Swagger
     */
    private void removePublisherSpecificInfo(Swagger swagger) {
        Map<String, Object> extensions = swagger.getVendorExtensions();
        OASParserUtil.removePublisherSpecificInfo(extensions);
        for (String pathKey : swagger.getPaths().keySet()) {
            Path path = swagger.getPath(pathKey);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                Operation operation = entry.getValue();
                OASParserUtil.removePublisherSpecificInfofromOperation(operation.getVendorExtensions());
            }
        }
    }

    /**
     * Remove x-wso2-examples from all the paths from the swagger.
     *
     * @param swaggerString Swagger as String
     */
    public String removeExamplesFromSwagger(String swaggerString) throws APIManagementException {
        try {
            SwaggerParser swaggerParser = new SwaggerParser();
            Swagger swagger = swaggerParser.parse(swaggerString);
            swagger.getPaths().values().forEach(path -> {
                path.getOperations().forEach(operation -> {
                    if (operation.getVendorExtensions().keySet().contains(APIConstants.SWAGGER_X_EXAMPLES)) {
                        operation.getVendorExtensions().remove(APIConstants.SWAGGER_X_EXAMPLES);
                    }
                });
            });
            return Yaml.pretty().writeValueAsString(swagger);
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error while removing examples from OpenAPI definition", e,
                    ExceptionCodes.ERROR_REMOVING_EXAMPLES);
        }
    }

    /**
     * Update OAS definition for store
     *
     * @param api            API
     * @param oasDefinition  OAS definition
     * @param hostsWithSchemes host addresses with protocol mapping
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    @Override
    public String getOASDefinitionForStore(API api, String oasDefinition, Map<String, String> hostsWithSchemes)
            throws APIManagementException {

        Swagger swagger = getSwagger(oasDefinition);
        updateOperations(swagger);
        updateEndpoints(api, hostsWithSchemes, swagger);
        return updateSwaggerSecurityDefinitionForStore(swagger, new SwaggerData(api), hostsWithSchemes);
    }

    /**
     * Update OAS definition for store
     *
     * @param product        APIProduct
     * @param oasDefinition  OAS definition
     * @param hostsWithSchemes host addresses with protocol mapping
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    @Override
    public String getOASDefinitionForStore(APIProduct product, String oasDefinition,
                                           Map<String, String> hostsWithSchemes) throws APIManagementException {

        Swagger swagger = getSwagger(oasDefinition);
        updateOperations(swagger);
        updateEndpoints(product, hostsWithSchemes, swagger);
        return updateSwaggerSecurityDefinitionForStore(swagger, new SwaggerData(product), hostsWithSchemes);
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
        Swagger swagger = getSwagger(oasDefinition);
        if (api.getAuthorizationHeader() != null) {
            swagger.setVendorExtension(APIConstants.X_WSO2_AUTH_HEADER, api.getAuthorizationHeader());
        }
        if (api.getApiLevelPolicy() != null) {
            swagger.setVendorExtension(APIConstants.X_THROTTLING_TIER, api.getApiLevelPolicy());
        }
        swagger.setVendorExtension(APIConstants.X_WSO2_CORS, api.getCorsConfiguration());
        Object prodEndpointObj = OASParserUtil.generateOASConfigForEndpoints(api, true);
        if (prodEndpointObj != null) {
            swagger.setVendorExtension(APIConstants.X_WSO2_PRODUCTION_ENDPOINTS, prodEndpointObj);
        }
        Object sandEndpointObj = OASParserUtil.generateOASConfigForEndpoints(api, false);
        if (sandEndpointObj != null) {
            swagger.setVendorExtension(APIConstants.X_WSO2_SANDBOX_ENDPOINTS, sandEndpointObj);
        }
        swagger.setVendorExtension(APIConstants.X_WSO2_BASEPATH, api.getContext());
        if (api.getTransports() != null) {
            swagger.setVendorExtension(APIConstants.X_WSO2_TRANSPORTS, api.getTransports().split(","));
        }
        String apiSecurity = api.getApiSecurity();
        // set mutual ssl extension if enabled
        if (apiSecurity != null) {
            List<String> securityList = Arrays.asList(apiSecurity.split(","));
            if (securityList.contains(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                String mutualSSLOptional = !securityList.contains(APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY) ?
                        APIConstants.OPTIONAL : APIConstants.MANDATORY;
                swagger.setVendorExtension(APIConstants.X_WSO2_MUTUAL_SSL, mutualSSLOptional);
            }
        }
        // This app security is should given in resource level,
        // otherwise the default oauth2 scheme defined at each resouce level will override application securities
        JsonNode appSecurityExtension = OASParserUtil.getAppSecurity(apiSecurity);
        for (String pathKey : swagger.getPaths().keySet()) {
            Path path = swagger.getPath(pathKey);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                Operation operation = entry.getValue();
                operation.setVendorExtension(APIConstants.X_WSO2_APP_SECURITY, appSecurityExtension);
            }
        }
        swagger.setVendorExtension(APIConstants.X_WSO2_APP_SECURITY, appSecurityExtension);
        swagger.setVendorExtension(APIConstants.X_WSO2_RESPONSE_CACHE,
                OASParserUtil.getResponseCacheConfig(api.getResponseCache(), api.getCacheTimeout()));

        return getSwaggerJsonString(swagger);
    }

    @Override
    public String getOASVersion(String oasDefinition) {
        Swagger swagger = getSwagger(oasDefinition);
        return swagger.getInfo().getVersion();
    }

    /**
     * Update swagger with security definition
     *
     * @param swagger     swagger object
     * @param swaggerData Swagger related data
     */
    private void updateSwaggerSecurityDefinition(Swagger swagger, SwaggerData swaggerData, String authUrl) {
        OAuth2Definition oAuth2Definition = new OAuth2Definition().implicit(authUrl);
        Set<Scope> scopes = swaggerData.getScopes();
        if (scopes != null && !scopes.isEmpty()) {
            Map<String, String> scopeBindings = new HashMap<>();
            for (Scope scope : scopes) {
                String description = scope.getDescription() != null ? scope.getDescription() : "";
                oAuth2Definition.addScope(scope.getKey(), description);
                String roles = (StringUtils.isNotBlank(scope.getRoles())
                        && scope.getRoles().trim().split(",").length > 0) ? scope.getRoles() : StringUtils.EMPTY;
                scopeBindings.put(scope.getKey(), roles);
            }
            oAuth2Definition.setVendorExtension(APIConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
        }
        swagger.addSecurityDefinition(APIConstants.SWAGGER_APIM_DEFAULT_SECURITY, oAuth2Definition);
        if (swagger.getSecurity() == null) {
            SecurityRequirement securityRequirement = new SecurityRequirement();
            securityRequirement.setRequirements(APIConstants.SWAGGER_APIM_DEFAULT_SECURITY, new ArrayList<String>());
            swagger.addSecurity(securityRequirement);
        }
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
            authType = APIConstants.OASResourceAuthTypes.APPLICATION_OR_APPLICATION_USER;
        }
        if (APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authType)) {
            authType = APIConstants.OASResourceAuthTypes.APPLICATION_USER;
        }
        if (APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authType)) {
            authType = APIConstants.OASResourceAuthTypes.APPLICATION;
        }
        operation.setVendorExtension(APIConstants.SWAGGER_X_AUTH_TYPE, authType);
        operation.setVendorExtension(APIConstants.SWAGGER_X_THROTTLING_TIER, resource.getPolicy());
        // AWS Lambda: set arn & timeout to swagger
        if (resource.getAmznResourceName() != null) {
            operation.setVendorExtension(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME, resource.getAmznResourceName());
        }
        if (resource.getAmznResourceTimeout() != 0) {
            operation.setVendorExtension(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT, resource.getAmznResourceTimeout());
        }
        updateLegacyScopesFromOperation(resource, operation);
        String oauth2SchemeKey = APIConstants.SWAGGER_APIM_DEFAULT_SECURITY;
        List<Map<String, List<String>>> security = operation.getSecurity();
        if (security == null) {
            security = new ArrayList<>();
            operation.setSecurity(security);
        }
        for (Map<String, List<String>> requirement : security) {
            if (requirement.get(oauth2SchemeKey) != null) {
                if (resource.getScopes().isEmpty()) {
                    requirement.put(oauth2SchemeKey, Collections.EMPTY_LIST);
                } else {
                     requirement.put(oauth2SchemeKey, resource.getScopes().stream().map(Scope::getKey).collect(
                            Collectors.toList()));
                }
                return;
            }
        }
        // if oauth2SchemeKey not present, add a new
        Map<String, List<String>> defaultRequirement = new HashMap<>();
        if (resource.getScopes().isEmpty()) {
            defaultRequirement.put(oauth2SchemeKey, Collections.EMPTY_LIST);
        } else {
            defaultRequirement.put(oauth2SchemeKey, resource.getScopes().stream().map(Scope::getKey).collect(
                    Collectors.toList()));
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

        Map<String, Object> extensions = operation.getVendorExtensions();
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_SCOPE)) {
            extensions.remove(APIConstants.SWAGGER_X_SCOPE);
        }
    }

    /**
     * Remove legacy scope from swagger
     *
     * @param swagger
     */
    private void updateLegacyScopesFromSwagger(Swagger swagger, SwaggerData swaggerData) {

        Map<String, Object> extensions = swagger.getVendorExtensions();
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_WSO2_SECURITY)) {
            extensions.remove(APIConstants.SWAGGER_X_WSO2_SECURITY);
        }
    }

    /**
     * Set scopes to the swagger extension
     *
     * @param swagger     swagger object
     * @param swaggerData Swagger API data
     */
    private void setLegacyScopeExtensionToSwagger(Swagger swagger, SwaggerData swaggerData) {
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

            swagger.setVendorExtension(APIConstants.SWAGGER_X_WSO2_SECURITY, xWSO2SecurityDefinitionObject);
        }
    }

    /**
     * Creates a new operation object using the URI template object
     *
     * @param resource API resource data
     * @return a new operation object using the URI template object
     */
    private Operation createOperation(SwaggerData.Resource resource) {
        Operation operation = new Operation();
        List<String> pathParams = getPathParamNames(resource.getPath());
        for (String pathParam : pathParams) {
            PathParameter pathParameter = new PathParameter();
            pathParameter.setName(pathParam);
            pathParameter.setType("string");
            operation.addParameter(pathParameter);
        }

        updateOperationManagedInfo(resource, operation);

        Response response = new Response();
        response.setDescription("OK");
        operation.addResponse(APIConstants.SWAGGER_RESPONSE_200, response);
        return operation;
    }

    /**
     * Add a new path based on the provided URI template to swagger if it does not exists. If it exists,
     * adds the respective operation to the existing path
     *
     * @param swagger  swagger object
     * @param resource API resource data
     */
    private void addOrUpdatePathToSwagger(Swagger swagger, SwaggerData.Resource resource) {
        Path path;
        if (swagger.getPath(resource.getPath()) != null) {
            path = swagger.getPath(resource.getPath());
        } else {
            path = new Path();
        }

        Operation operation = createOperation(resource);
        path.set(resource.getVerb().toLowerCase(), operation);

        swagger.path(resource.getPath(), path);
    }

    /**
     * Creates a json string using the swagger object.
     *
     * @param swaggerObj swagger object
     * @return json string using the swagger object
     * @throws APIManagementException error while creating swagger json
     */
    private String getSwaggerJsonString(Swagger swaggerObj) throws APIManagementException {
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
     * Retrieves the "Auth2" security scheme key
     *
     * @param swagger Swgger object
     * @return "Auth2" security scheme key
     */
    private String getOAuth2SecuritySchemeKey(Swagger swagger) {
        final String oauth2Type = new OAuth2Definition().getType();
        Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
        boolean hasDefaultKey = false;
        boolean hasRESTAPIScopeKey = false;
        if (securityDefinitions != null) {
            for (Map.Entry<String, SecuritySchemeDefinition> definitionEntry : securityDefinitions.entrySet()) {
                if (oauth2Type.equals(definitionEntry.getValue().getType())) {
                    //sets hasDefaultKey to true if at least once SWAGGER_APIM_DEFAULT_SECURITY becomes the key
                    hasDefaultKey = hasDefaultKey || SWAGGER_APIM_DEFAULT_SECURITY.equals(definitionEntry.getKey());
                    //sets hasRESTAPIScopeKey to true if at least once SWAGGER_APIM_RESTAPI_SECURITY becomes the key
                    hasRESTAPIScopeKey = hasRESTAPIScopeKey
                            || SWAGGER_APIM_RESTAPI_SECURITY.equals(definitionEntry.getKey());
                }
            }
        }
        if (hasDefaultKey) {
            return SWAGGER_APIM_DEFAULT_SECURITY;
        } else if (hasRESTAPIScopeKey) {
            return SWAGGER_APIM_RESTAPI_SECURITY;
        } else {
            return null;
        }
    }

    /**
     * Gets a list of scopes using the security requirements
     *
     * @param oauth2SchemeKey OAuth2 security element key
     * @param operation       Swagger path operation
     * @return list of scopes using the security requirements
     */
    private List<String> getScopeOfOperations(String oauth2SchemeKey, Operation operation) {
        List<Map<String, List<String>>> security = operation.getSecurity();
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

        Map<String, Object> extensions = operation.getVendorExtensions();
        if (extensions.containsKey(APIConstants.SWAGGER_X_SCOPE)) {
            String scopeKey = (String) extensions.get(APIConstants.SWAGGER_X_SCOPE);
            return Stream.of(scopeKey.split(",")).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Update OAS operations for Store
     *
     * @param swagger Swagger to be updated
     */
    private void updateOperations(Swagger swagger) {
        for (String pathKey : swagger.getPaths().keySet()) {
            Path path = swagger.getPath(pathKey);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                Operation operation = entry.getValue();
                Map<String, Object> extensions = operation.getVendorExtensions();
                if (extensions != null) {
                    // remove mediation extension
                    if (extensions.containsKey(APIConstants.SWAGGER_X_MEDIATION_SCRIPT)) {
                        extensions.remove(APIConstants.SWAGGER_X_MEDIATION_SCRIPT);
                    }
                    // set x-scope value to security definition if it not there.
                    if (extensions.containsKey(APIConstants.SWAGGER_X_WSO2_SCOPES)) {
                        String scope = (String) extensions.get(APIConstants.SWAGGER_X_WSO2_SCOPES);
                        List<Map<String, List<String>>> security = operation.getSecurity();
                        if (security == null) {
                            security = new ArrayList<>();
                            operation.setSecurity(security);
                        }
                        for (Map<String, List<String>> requirement : security) {
                            if (requirement.get(APIConstants.SWAGGER_APIM_DEFAULT_SECURITY) == null || !requirement
                                    .get(APIConstants.SWAGGER_APIM_DEFAULT_SECURITY).contains(scope)) {
                                requirement
                                        .put(APIConstants.SWAGGER_APIM_DEFAULT_SECURITY, Collections.singletonList(scope));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get parsed Swagger object
     *
     * @param oasDefinition OAS definition
     * @return Swagger
     * @throws APIManagementException
     */
    Swagger getSwagger(String oasDefinition) {
        SwaggerParser parser = new SwaggerParser();
        SwaggerDeserializationResult parseAttemptForV2 = parser.readWithInfo(oasDefinition);
        if (CollectionUtils.isNotEmpty(parseAttemptForV2.getMessages())) {
            log.debug("Errors found when parsing OAS definition");
        }
        return parseAttemptForV2.getSwagger();
    }

    /**
     * Update OAS definition with GW endpoints
     *
     * @param product          APIProduct
     * @param hostsWithSchemes GW hosts with protocol mapping
     * @param swagger          Swagger
     */
    private void updateEndpoints(APIProduct product, Map<String, String> hostsWithSchemes, Swagger swagger) {
        String basePath = product.getContext();
        String transports = product.getTransports();
        updateEndpoints(swagger, basePath, transports, hostsWithSchemes);
    }

    /**
     * Update OAS definition with GW endpoints
     *
     * @param api            API
     * @param hostsWithSchemes  GW hosts with protocol mapping
     * @param swagger        Swagger
     */
    private void updateEndpoints(API api, Map<String,String> hostsWithSchemes, Swagger swagger) {
        String basePath = api.getContext();
        String transports = api.getTransports();
        updateEndpoints(swagger, basePath, transports, hostsWithSchemes);
    }

    /**
     * Update OAS definition with GW endpoints and API information
     *
     * @param swagger        Swagger
     * @param basePath       API context
     * @param transports     transports types
     * @param hostsWithSchemes GW hosts with protocol mapping
     */
    private void updateEndpoints(Swagger swagger, String basePath, String transports,
                                 Map<String, String> hostsWithSchemes) {

        String host = StringUtils.EMPTY;
        String[] apiTransports = transports.split(",");
        List<Scheme> schemes = new ArrayList<>();
        if (ArrayUtils.contains(apiTransports, APIConstants.HTTPS_PROTOCOL)
                && hostsWithSchemes.get(APIConstants.HTTPS_PROTOCOL) != null) {
            schemes.add(Scheme.HTTPS);
            host = hostsWithSchemes.get(APIConstants.HTTPS_PROTOCOL).trim()
                    .replace(APIConstants.HTTPS_PROTOCOL_URL_PREFIX, "");
        }
        if (ArrayUtils.contains(apiTransports, APIConstants.HTTP_PROTOCOL)
                && hostsWithSchemes.get(APIConstants.HTTP_PROTOCOL) != null) {
            schemes.add(Scheme.HTTP);
            if (StringUtils.isEmpty(host)) {
                host = hostsWithSchemes.get(APIConstants.HTTP_PROTOCOL).trim()
                        .replace(APIConstants.HTTP_PROTOCOL_URL_PREFIX, "");
            }
        }
        swagger.setSchemes(schemes);
        swagger.setBasePath(basePath);
        swagger.setHost(host);
    }

    /**
     * Update OAS definition with authorization endpoints
     *
     * @param swagger           Swagger
     * @param swaggerData       SwaggerData
     * @param hostsWithSchemes  GW hosts with protocols
     * @return updated OAS definition
     */
    private String updateSwaggerSecurityDefinitionForStore(Swagger swagger, SwaggerData swaggerData,
                                                           Map<String,String> hostsWithSchemes)
            throws APIManagementException {

        String authUrl;
        // By Default, add the GW host with HTTPS protocol if present.
        if (hostsWithSchemes.containsKey(APIConstants.HTTPS_PROTOCOL)) {
            authUrl = (hostsWithSchemes.get(APIConstants.HTTPS_PROTOCOL)).concat("/authorize");
        } else {
            authUrl = (hostsWithSchemes.get(APIConstants.HTTP_PROTOCOL)).concat("/authorize");
        }
        updateSwaggerSecurityDefinition(swagger, swaggerData, authUrl);
        return getSwaggerJsonString(swagger);
    }

    @Override
    public String getOASDefinitionWithTierContentAwareProperty(String oasDefinition,
            List<String> contentAwareTiersList, String apiLevelTier) throws APIManagementException {
        SwaggerParser parser = new SwaggerParser();
        SwaggerDeserializationResult parseAttemptForV2 = parser.readWithInfo(oasDefinition);
        Swagger swagger = parseAttemptForV2.getSwagger();
        // check if API Level tier is content aware. if so, we set a extension as a global property
        if (contentAwareTiersList.contains(apiLevelTier)) {
            swagger.setVendorExtension(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH, true);
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
            for (Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
                String path = entry.getKey();
                List<Operation> operations = swagger.getPaths().get(path).getOperations();
                for (Operation op : operations) {
                    if (contentAwareTiersList
                            .contains(op.getVendorExtensions().get(APIConstants.SWAGGER_X_THROTTLING_TIER))) {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    "API resource Level policy is content aware for operation " + op.getOperationId());
                        }
                        op.setVendorExtension(APIConstants.SWAGGER_X_THROTTLING_BANDWIDTH, true);
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
        Swagger swagger = getSwagger(swaggerContent);

        Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
        if (securityDefinitions == null) {
            return false;
        }
        OAuth2Definition checkDefault = (OAuth2Definition) securityDefinitions.get(SWAGGER_SECURITY_SCHEMA_KEY);
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
        Swagger swagger = getSwagger(swaggerContent);
        Set<Scope> legacyScopes = getScopesFromExtensions(swagger);

        if (!isDefaultGiven(swaggerContent) && legacyScopes.isEmpty()) {
            swagger = injectOtherScopesToDefaultScheme(swagger);
            swagger = injectOtherResourceScopesToDefaultScheme(swagger);
            return getSwaggerJsonString(swagger);
        } else if (!legacyScopes.isEmpty()) {
            swagger = processLegacyScopes(swagger);
            return getSwaggerJsonString(swagger);
        }
        return swaggerContent;
    }

    /**
     * This method returns swagger definition which replaced X-WSO2-throttling-tier extension comes from
     * mgw with X-throttling-tier extensions in swagger file(Swagger version 2)
     *
     * @param swaggerContent String
     * @return String
     * @throws APIManagementException
     */
    @Override
    public String injectMgwThrottlingExtensionsToDefault(String swaggerContent) throws APIManagementException {
        Swagger swagger = getSwagger(swaggerContent);
        Map<String, Path> paths = swagger.getPaths();
        for (String pathKey : paths.keySet()) {
            Map<HttpMethod, Operation> operationsMap = paths.get(pathKey).getOperationMap();
            for (Map.Entry<HttpMethod, Operation> entry : operationsMap.entrySet()) {
                Operation operation = entry.getValue();
                Map<String, Object> extensions = operation.getVendorExtensions();
                if (extensions != null && extensions.containsKey(APIConstants.X_WSO2_THROTTLING_TIER)) {
                    Object tier = extensions.get(APIConstants.X_WSO2_THROTTLING_TIER);
                    extensions.remove(APIConstants.X_WSO2_THROTTLING_TIER);
                    extensions.put(APIConstants.SWAGGER_X_THROTTLING_TIER, tier);
                }
            }
        }
        return getSwaggerJsonString(swagger);
    }

    /**
     * This method will extract scopes from legacy x-wso2-security and add them to default scheme
     * @param swagger swagger definition
     * @return
     * @throws APIManagementException
     */
    private Swagger processLegacyScopes(Swagger swagger) throws APIManagementException {

        Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
        OAuth2Definition oAuth2Definition = new OAuth2Definition();
        if (securityDefinitions != null && securityDefinitions.get(APIConstants.OAUTH2_DEFAULT_SCOPE) != null) {
            oAuth2Definition = (OAuth2Definition) securityDefinitions.get(APIConstants.OAUTH2_DEFAULT_SCOPE);
        }
        Map<String, String> scopeBindings = new HashMap<>();
        Map<String, Object> vendorExtensions = oAuth2Definition.getVendorExtensions();
        if (vendorExtensions != null && vendorExtensions.get(APIConstants.SWAGGER_X_SCOPES_BINDINGS) != null) {
            scopeBindings = (Map<String, String>) vendorExtensions.get(APIConstants.SWAGGER_X_SCOPES_BINDINGS);
        }
        Set<Scope> scopes = getScopesFromExtensions(swagger);
        if (scopes != null && !scopes.isEmpty()) {
            for (Scope scope : scopes) {
                oAuth2Definition.addScope(scope.getKey(), scope.getDescription());
                String roles = (StringUtils.isNotBlank(scope.getRoles())
                        && scope.getRoles().trim().split(",").length > 0) ? scope.getRoles() : StringUtils.EMPTY;
                scopeBindings.put(scope.getKey(), roles);
            }
            oAuth2Definition.setVendorExtension(APIConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
        }
        swagger.addSecurityDefinition(APIConstants.SWAGGER_APIM_DEFAULT_SECURITY, oAuth2Definition);
        return swagger;
    }

    /**
     * This method returns the oauth scopes according to the given swagger(version 2)
     *
     * @param swagger resource json
     * @return Swagger
     * @throws APIManagementException
     */
    private Swagger injectOtherScopesToDefaultScheme(Swagger swagger) throws APIManagementException {
        //Get security definitions from swagger
        Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
        List<String> otherSetOfSchemes = new ArrayList<>();
        Map<String, String> defaultScopeBindings = null;
        if (securityDefinitions != null) {
            //If there is no default type schemes set a one
            OAuth2Definition newDefault = new OAuth2Definition().implicit("https://test.com");
            newDefault.setType("oauth2");
            newDefault.setDescription("");
            securityDefinitions.put(SWAGGER_SECURITY_SCHEMA_KEY, newDefault);
            //Check all the security definitions
            for (Map.Entry<String, SecuritySchemeDefinition> definition : securityDefinitions.entrySet()) {
                String checkType = definition.getValue().getType();
                //Inject other scheme scopes into default scope
                if (!SWAGGER_SECURITY_SCHEMA_KEY.equals(definition.getKey()) && "oauth2".equals(checkType)) {
                    //Add non default scopes to other scopes list
                    otherSetOfSchemes.add(definition.getKey());
                    //Check for default one
                    OAuth2Definition noneDefaultFlowType = (OAuth2Definition) definition.getValue();
                    OAuth2Definition defaultTypeFlow = (OAuth2Definition) securityDefinitions.get(SWAGGER_SECURITY_SCHEMA_KEY);
                    Map<String, String> noneDefaultFlowScopes = noneDefaultFlowType.getScopes();
                    Map<String, String> defaultTypeScopes = defaultTypeFlow.getScopes();
                    if (defaultTypeScopes == null) {
                        defaultTypeScopes = new HashMap<>();
                    }
                    for (Map.Entry<String, String> input : noneDefaultFlowScopes.entrySet()) {
                        defaultTypeScopes.put(input.getKey(), input.getValue());
                    }
                    defaultTypeFlow.setScopes(defaultTypeScopes);
                    //Check X-Scope Bindings
                    Map<String, String> noneDefaultScopeBindings = null;
                    Map<String, Object> defaultTypeExtension = defaultTypeFlow.getVendorExtensions();
                    if (noneDefaultFlowType.getVendorExtensions() != null && (noneDefaultScopeBindings =
                            (Map<String, String>) noneDefaultFlowType.getVendorExtensions().get(APIConstants.SWAGGER_X_SCOPES_BINDINGS))
                            != null) {
                        if (defaultScopeBindings == null) {
                            defaultScopeBindings = new HashMap<>();
                        }
                        //Inject non default scope bindings into default scheme
                        for (Map.Entry<String, String> roleInUse : noneDefaultScopeBindings.entrySet()) {
                            defaultScopeBindings.put(roleInUse.getKey(), roleInUse.getValue());
                        }
                    }
                    defaultTypeExtension.put(APIConstants.SWAGGER_X_SCOPES_BINDINGS, defaultScopeBindings);
                    defaultTypeFlow.setVendorExtensions(defaultTypeExtension);
                    securityDefinitions.put(SWAGGER_SECURITY_SCHEMA_KEY, defaultTypeFlow);
                }
            }
            //update list of security schemes in the swagger object
            swagger.setSecurityDefinitions(securityDefinitions);
        }
        setOtherSchemes(otherSetOfSchemes);
        return swagger;
    }

    /**
     * This method returns URI templates according to the given swagger file(Swagger version 2)
     *
     * @param swagger Swagger
     * @return Swagger
     * @throws APIManagementException
     */
    private Swagger injectOtherResourceScopesToDefaultScheme(Swagger swagger) throws APIManagementException {
        List<String> schemes = getOtherSchemes();

        Map<String, Path> paths = swagger.getPaths();
        for (String pathKey : paths.keySet()) {
            Path pathItem = paths.get(pathKey);
            Map<HttpMethod, Operation> operationsMap = pathItem.getOperationMap();
            for (Map.Entry<HttpMethod, Operation> entry : operationsMap.entrySet()) {
                HttpMethod httpMethod = entry.getKey();
                Operation operation = entry.getValue();
                Map<String, List<String>> updatedDefaultSecurityRequirement = new HashMap<>();
                List<Map<String, List<String>>> securityRequirements = operation.getSecurity();
                if (securityRequirements == null) {
                    securityRequirements = new ArrayList<>();
                }
                if (APIConstants.SUPPORTED_METHODS.contains(httpMethod.name().toLowerCase())) {
                    List<String> opScopesDefault = new ArrayList<>();
                    List<String> opScopesDefaultInstance = getScopeOfOperations(SWAGGER_SECURITY_SCHEMA_KEY, operation);
                    if (opScopesDefaultInstance != null) {
                        opScopesDefault.addAll(opScopesDefaultInstance);
                    }
                    updatedDefaultSecurityRequirement.put(SWAGGER_SECURITY_SCHEMA_KEY, opScopesDefault);
                    for (Map<String, List<String>> input : securityRequirements) {
                        for (String scheme : schemes) {
                            if (!SWAGGER_SECURITY_SCHEMA_KEY.equals(scheme)) {
                                List<String> opScopesOthers = getScopeOfOperations(scheme, operation);
                                if (opScopesOthers != null) {
                                    for (String scope : opScopesOthers) {
                                        if (!opScopesDefault.contains(scope)) {
                                            opScopesDefault.add(scope);
                                        }
                                    }
                                }
                            }
                            updatedDefaultSecurityRequirement.put(SWAGGER_SECURITY_SCHEMA_KEY, opScopesDefault);
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
        swagger.setPaths(paths);
        return swagger;
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
        Swagger swagger = getSwagger(apiDefinition);
        Map<String, Object> extensions = swagger.getVendorExtensions();
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
     * This method will extractX-WSO2-disable-security extension provided in API level
     * by mgw and inject that extension to all resources in OAS file
     *
     * @param swaggerContent String
     * @return String
     * @throws APIManagementException
     */
    @Override
    public String processDisableSecurityExtension(String swaggerContent) throws APIManagementException {
        Swagger swagger = getSwagger(swaggerContent);
        Map<String, Object> apiExtensions = swagger.getVendorExtensions();
        if (apiExtensions == null) {
            return swaggerContent;
        }
        //Check Disable Security is enabled in API level
        boolean apiLevelDisableSecurity = OASParserUtil.getDisableSecurity(apiExtensions);
        Map<String, Path> paths = swagger.getPaths();
        for (String pathKey : paths.keySet()) {
            Map<HttpMethod, Operation> operationsMap = paths.get(pathKey).getOperationMap();
            for (Map.Entry<HttpMethod, Operation> entry : operationsMap.entrySet()) {
                Operation operation = entry.getValue();
                Map<String, Object> resourceExtensions = operation.getVendorExtensions();
                boolean extensionsAreEmpty = false;
                if (apiLevelDisableSecurity) {
                    if (resourceExtensions == null) {
                        resourceExtensions = new HashMap<>();
                        extensionsAreEmpty = true;
                    }
                    resourceExtensions.put(APIConstants.SWAGGER_X_AUTH_TYPE, "None");
                    if (extensionsAreEmpty) {
                        operation.setVendorExtensions(resourceExtensions);
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

        return getSwaggerJsonString(swagger);
    }

}
