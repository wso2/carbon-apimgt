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

import io.swagger.v3.core.util.Json;
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
import io.swagger.v3.parser.core.models.SwaggerParseResult;
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
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.net.MalformedURLException;
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

/**
 * Models API definition using OAS (OpenAPI 3.0) parser
 */
public class OAS3Parser extends APIDefinition {
    private static final Log log = LogFactory.getLog(OAS3Parser.class);
    private static final String OPENAPI_SECURITY_SCHEMA_KEY = "default";

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
                            template = OASParserUtil.setScopesToTemplate(template, opScopes);
                        }
                    }
                    Map<String, Object> extensios = operation.getExtensions();
                    if (extensios != null) {
                        if (extensios.containsKey(APIConstants.SWAGGER_X_AUTH_TYPE)) {
                            String scopeKey = (String) extensios.get(APIConstants.SWAGGER_X_AUTH_TYPE);
                            template.setAuthType(scopeKey);
                            template.setAuthTypes(scopeKey);
                        } else {
                            template.setAuthType("Any");
                            template.setAuthTypes("Any");
                        }
                        if (extensios.containsKey(APIConstants.SWAGGER_X_THROTTLING_TIER)) {
                            String throttlingTier = (String) extensios.get(APIConstants.SWAGGER_X_THROTTLING_TIER);
                            template.setThrottlingTier(throttlingTier);
                            template.setThrottlingTiers(throttlingTier);
                        }
                        if (extensios.containsKey(APIConstants.SWAGGER_X_MEDIATION_SCRIPT)) {
                            String mediationScript = (String) extensios.get(APIConstants.SWAGGER_X_MEDIATION_SCRIPT);
                            template.setMediationScript(mediationScript);
                            template.setMediationScripts(template.getHTTPVerb(), mediationScript);
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
        OAuthFlow oAuthFlow;
        Scopes scopes;
        if (openAPI.getComponents() != null && (securitySchemes = openAPI.getComponents().getSecuritySchemes()) != null
                && (securityScheme = securitySchemes.get(OPENAPI_SECURITY_SCHEMA_KEY)) != null
                && (oAuthFlow = securityScheme.getFlows().getImplicit()) != null
                && (scopes = oAuthFlow.getScopes()) != null) {
            Set<Scope> scopeSet = new HashSet<>();
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
            return scopeSet;
        } else {
            return getScopesFromExtensions(openAPI);
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
        updateSwaggerSecurityDefinition(openAPI, swaggerData, "https://test.com");
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

        //adding new operations to the definition
        for (SwaggerData.Resource resource : copy) {
            addOrUpdatePathToSwagger(openAPI, resource);
        }
        updateSwaggerSecurityDefinition(openAPI, swaggerData, "https://test.com");
        updateLegacyScopesFromSwagger(openAPI, swaggerData);
        return Json.pretty(openAPI);
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
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(apiDefinition, null, null);
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
            OASParserUtil.updateValidationResponseAsSuccess(validationResponse, apiDefinition, openAPI.getOpenapi(),
                    info.getTitle(), info.getVersion(), null, info.getDescription());
            validationResponse.setParser(this);
            if (returnJsonContent) {
                validationResponse.setJsonContent(Json.pretty(parseAttemptForV3.getOpenAPI()));
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
    }

    /**
     * Update OAS definition for store
     *
     * @param api            API
     * @param oasDefinition  OAS definition
     * @param hostWithScheme host address with protocol
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    @Override
    public String getOASDefinitionForStore(API api, String oasDefinition, String hostWithScheme)
            throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(oasDefinition);
        updateOperations(openAPI);
        updateEndpoints(api, hostWithScheme, openAPI);
        return updateSwaggerSecurityDefinitionForStore(openAPI, new SwaggerData(api), hostWithScheme);
    }

    /**
     * Update OAS definition for store
     *
     * @param product        APIProduct
     * @param oasDefinition  OAS definition
     * @param hostWithScheme host address with protocol
     * @return OAS definition
     * @throws APIManagementException throws if an error occurred
     */
    @Override
    public String getOASDefinitionForStore(APIProduct product, String oasDefinition, String hostWithScheme)
            throws APIManagementException {
        OpenAPI openAPI = getOpenAPI(oasDefinition);
        updateOperations(openAPI);
        updateEndpoints(product, hostWithScheme, openAPI);
        return updateSwaggerSecurityDefinitionForStore(openAPI, new SwaggerData(product), hostWithScheme);
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
        // setting scopes id it is null
        // https://github.com/swagger-api/swagger-parser/issues/1202
        OAuthFlow oAuthFlow = openAPI.getComponents().getSecuritySchemes().get("default").getFlows().getImplicit();
        if (oAuthFlow.getScopes() == null) {
            oAuthFlow.setScopes(new Scopes());
        }

        if (api.getAuthorizationHeader() != null) {
            openAPI.addExtension(APIConstants.X_WSO2_AUTH_HEADER, api.getAuthorizationHeader());
        }
        if (api.getApiLevelPolicy() != null) {
            openAPI.addExtension(APIConstants.X_THROTTLING_TIER, api.getApiLevelPolicy());
        }
        openAPI.addExtension(APIConstants.X_WSO2_CORS, api.getCorsConfiguration());
        String endpointConfig = api.getEndpointConfig();
        if (!StringUtils.isBlank(endpointConfig)) {
            org.json.JSONObject endpoints = new org.json.JSONObject(endpointConfig);
            if (endpoints.has(APIConstants.API_DATA_PRODUCTION_ENDPOINTS)) {
                String prodUrls = endpoints.getJSONObject(APIConstants.API_DATA_PRODUCTION_ENDPOINTS)
                        .getString(APIConstants.API_DATA_URL);
                openAPI.addExtension(APIConstants.X_WSO2_PRODUCTION_ENDPOINTS, prodUrls);
            }
            if (endpoints.has(APIConstants.API_DATA_SANDBOX_ENDPOINTS)) {
                String sandUrls = endpoints.getJSONObject(APIConstants.API_DATA_SANDBOX_ENDPOINTS)
                        .getString(APIConstants.API_DATA_URL);
                openAPI.addExtension(APIConstants.X_WSO2_SANDBOX_ENDPOINTS, sandUrls);
            }
        }
        openAPI.addExtension(APIConstants.X_WSO2_BASEPATH, api.getContext());
        return Json.pretty(openAPI);
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
            return Collections.singletonList(scopeKey);
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
                oas3Scopes.put(scope.getName(), scope.getDescription());
                scopeBindings.put(scope.getName(), scope.getRoles());
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
        if (isLegacyExtensionsPreserved()) {
            log.debug("preserveLegacyExtensions is enabled.");
            setLegacyScopeExtensionToSwagger(openAPI, swaggerData);
            return;
        }
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

        updateLegacyScopesFromOperation(resource, operation);
        List<SecurityRequirement> security = operation.getSecurity();
        if (security == null) {
            security = new ArrayList<>();
            operation.setSecurity(security);
        }
        for (Map<String, List<String>> requirement : security) {
            if (requirement.get(OPENAPI_SECURITY_SCHEMA_KEY) != null) {

                if (resource.getScope() == null) {
                    requirement.put(OPENAPI_SECURITY_SCHEMA_KEY, Collections.EMPTY_LIST);
                } else {
                    requirement.put(OPENAPI_SECURITY_SCHEMA_KEY, Arrays.asList(resource.getScope().getKey()));
                }
                return;
            }
        }
        // if oauth2SchemeKey not present, add a new
        SecurityRequirement defaultRequirement = new SecurityRequirement();
        if (resource.getScope() == null) {
            defaultRequirement.put(OPENAPI_SECURITY_SCHEMA_KEY, Collections.EMPTY_LIST);
        } else {
            defaultRequirement.put(OPENAPI_SECURITY_SCHEMA_KEY, Arrays.asList(resource.getScope().getKey()));
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
                operation.addExtension(APIConstants.SWAGGER_X_SCOPE, resource.getScope().getKey());
            }
            return;
        }
        Map<String, Object> extensions = operation.getExtensions();
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_SCOPE)) {
            extensions.remove(APIConstants.SWAGGER_X_SCOPE);
        }
    }

    /**
     * Update OAS definition with authorization endpoints
     *
     * @param openAPI        OpenAPI
     * @param swaggerData    SwaggerData
     * @param hostWithScheme GW host with protocol
     * @return updated OAS definition
     */
    private String updateSwaggerSecurityDefinitionForStore(OpenAPI openAPI, SwaggerData swaggerData,
            String hostWithScheme) {
        String authUrl = hostWithScheme + "/authorize";
        updateSwaggerSecurityDefinition(openAPI, swaggerData, authUrl);

        return Json.pretty(openAPI);
    }

    /**
     * Update OAS definition with GW endpoints
     *
     * @param product        APIProduct
     * @param hostWithScheme GW host with protocol
     * @param openAPI        OpenAPI
     * @throws APIManagementException
     */
    private void updateEndpoints(APIProduct product, String hostWithScheme, OpenAPI openAPI)
            throws APIManagementException {
        String basePath = product.getContext();
        String transports = product.getTransports();
        try {
            updateEndpoints(openAPI, basePath, transports, hostWithScheme);
        } catch (MalformedURLException e) {
            throw new APIManagementException("Error occurred while setting server endpoints.", e);
        }
    }

    /**
     * Update OAS definition with GW endpoints
     *
     * @param api            API
     * @param hostWithScheme GW host with protocol
     * @param openAPI        OpenAPI
     * @throws APIManagementException
     */
    private void updateEndpoints(API api, String hostWithScheme, OpenAPI openAPI) throws APIManagementException {
        String basePath = api.getContext();
        String transports = api.getTransports();
        try {
            updateEndpoints(openAPI, basePath, transports, hostWithScheme);
        } catch (MalformedURLException e) {
            throw new APIManagementException("Error occurred while setting server endpoints.", e);
        }
    }

    /**
     * Update OAS definition with GW endpoints and API information
     *
     * @param openAPI        OpenAPI
     * @param basePath       API context
     * @param transports     transports types
     * @param hostWithScheme GW host with protocol
     * @throws MalformedURLException
     */
    private void updateEndpoints(OpenAPI openAPI, String basePath, String transports, String hostWithScheme)
            throws MalformedURLException {
        String host = hostWithScheme.trim().replace(APIConstants.HTTP_PROTOCOL_URL_PREFIX, "")
                .replace(APIConstants.HTTPS_PROTOCOL_URL_PREFIX, "");
        String[] apiTransports = transports.split(",");
        List<Server> servers = new ArrayList<>();
        if (ArrayUtils.contains(apiTransports, APIConstants.HTTPS_PROTOCOL)) {
            String httpsURL = APIConstants.HTTPS_PROTOCOL + "://" + host + basePath;
            Server httpsServer = new Server();
            httpsServer.setUrl(httpsURL);
            servers.add(httpsServer);
        }
        if (ArrayUtils.contains(apiTransports, APIConstants.HTTP_PROTOCOL)) {
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

    /**
     * Get parsed OpenAPI object
     *
     * @param oasDefinition OAS definition
     * @return OpenAPI
     * @throws APIManagementException
     */
    private OpenAPI getOpenAPI(String oasDefinition) throws APIManagementException {
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
        resource.setPath("/*");

        resource.setVerb(APIConstants.HTTP_GET);
        Operation getOperation = createOperation(resource);
        resource.setVerb(APIConstants.HTTP_POST);
        Operation postOperation = createOperation(resource);

        //get operation
        Parameter getParameter = new Parameter();
        getParameter.setName(APIConstants.GRAPHQL_QUERY);
        getParameter.setIn("query");
        getParameter.setRequired(true);
        getParameter.setDescription("Query to be passed to graphQL API");

        Schema getSchema = new Schema();
        getSchema.setType("string");
        getParameter.setSchema(getSchema);
        getOperation.addParametersItem(getParameter);

        //post operation
        RequestBody requestBody = new RequestBody();
        requestBody.setDescription("Query or mutation to be passed to graphQL API");
        requestBody.setRequired(true);

        JSONObject typeOfPayload = new JSONObject();
        JSONObject payload = new JSONObject();
        typeOfPayload.put(APIConstants.TYPE, "string");
        payload.put(APIConstants.OperationParameter.PAYLOAD_PARAM_NAME, typeOfPayload);

        Schema postSchema = new Schema();
        postSchema.setType("object");
        postSchema.setProperties(payload);

        MediaType mediaType = new MediaType();
        mediaType.setSchema(postSchema);

        Content content = new Content();
        content.addMediaType(APIConstants.APPLICATION_JSON_MEDIA_TYPE, mediaType);
        requestBody.setContent(content);
        postOperation.setRequestBody(requestBody);

        //add post and get operations to path /*
        PathItem pathItem = new PathItem();
        pathItem.setGet(getOperation);
        pathItem.setPost(postOperation);
        Paths paths = new Paths();
        paths.put("/*", pathItem);

        openAPI.setPaths(paths);
    }
}
