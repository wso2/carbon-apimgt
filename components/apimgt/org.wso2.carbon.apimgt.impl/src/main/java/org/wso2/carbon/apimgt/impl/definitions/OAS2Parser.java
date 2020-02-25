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
import io.swagger.models.Contact;
import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
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
import io.swagger.util.Yaml;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Models API definition using OAS (swagger 2.0) parser
 */
public class OAS2Parser extends APIDefinition {
    private static final Log log = LogFactory.getLog(OAS2Parser.class);
    private static final String SWAGGER_SECURITY_SCHEMA_KEY = "default";

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
                        template = OASParserUtil.setScopesToTemplate(template, opScopes);
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
            return sortScopes(scopeSet);
        } else {
            return sortScopes(getScopesFromExtensions(swagger));
        }
    }

    /**
     * Get scope information from the extensions
     *
     * @param swagger swagger object
     * @return Scope set
     * @throws APIManagementException if an error occurred
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
     * Sort scopes by name.
     * This method was added to display scopes in publisher in a sorted manner.
     *
     * @param scopeSet
     * @return Scope set
     */
    private Set<Scope> sortScopes(Set<Scope> scopeSet) {
        List<Scope> scopesSortedlist = new ArrayList<>(scopeSet);
        scopesSortedlist.sort(Comparator.comparing(Scope::getName));
        return new LinkedHashSet<>(scopesSortedlist);
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
        return getSwaggerJsonString(swaggerObj);
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
            OASParserUtil.updateValidationResponseAsSuccess(validationResponse, apiDefinition, swagger.getSwagger(),
                    info.getTitle(), info.getVersion(), swagger.getBasePath(), info.getDescription());
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
        swagger.setVendorExtension(APIConstants.X_WSO2_TRANSPORTS,
                OASParserUtil.getTransportSecurity(api.getApiSecurity(), api.getTransports()));
        swagger.setVendorExtension(APIConstants.SWAGGER_X_WSO2_APP_SECURITY,
                OASParserUtil.getAppSecurity(api.getApiSecurity()));

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
                oAuth2Definition.addScope(scope.getName(), scope.getDescription());
                scopeBindings.put(scope.getName(), scope.getRoles());
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
                if (resource.getScope() == null) {
                    requirement.put(oauth2SchemeKey, Collections.EMPTY_LIST);
                } else {
                    requirement.put(oauth2SchemeKey, Arrays.asList(resource.getScope().getKey()));
                }
                return;
            }
        }
        // if oauth2SchemeKey not present, add a new
        Map<String, List<String>> defaultRequirement = new HashMap<>();
        if (resource.getScope() == null) {
            defaultRequirement.put(oauth2SchemeKey, Collections.EMPTY_LIST);
        } else {
            defaultRequirement.put(oauth2SchemeKey, Arrays.asList(resource.getScope().getKey()));
        }
        security.add(defaultRequirement);
    }

    /**
     * Remove legacy scope information from swagger operation
     *
     * @param operation
     */
    private void updateLegacyScopesFromOperation(SwaggerData.Resource resource, Operation operation) {
        if (isLegacyExtensionsPreserved()) {
            log.debug("preserveLegacyExtensions is enabled.");
            if (resource.getScope() != null) {
                operation.setVendorExtension(APIConstants.SWAGGER_X_SCOPE, resource.getScope().getKey());
            }
            return;
        }
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
        if (isLegacyExtensionsPreserved()) {
            log.debug("preserveLegacyExtensions is enabled.");
            setLegacyScopeExtensionToSwagger(swagger, swaggerData);
            return;
        }
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
        if (securityDefinitions != null) {
            for (Map.Entry<String, SecuritySchemeDefinition> definitionEntry : securityDefinitions.entrySet()) {
                if (oauth2Type.equals(definitionEntry.getValue().getType())) {
                    return definitionEntry.getKey();
                }
            }
        }
        return null;
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
            return Collections.singletonList(scopeKey);
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
     * @param product        APIProduct
     * @param hostsWithSchemes  GW hosts with protocol mapping
     * @param swagger        Swagger
     */
    private void updateEndpoints(APIProduct product, Map<String,String> hostsWithSchemes, Swagger swagger) {
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
}
