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
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
        SwaggerParser parser = new SwaggerParser();
        Swagger swagger = parser.parse(resourceConfigsJSON);
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
                if(!opScopes.isEmpty()) {
                    String firstScope = opScopes.get(0);
                    Scope scope = APIUtil.findScopeByKey(scopes, firstScope);
                    if (scope == null) {
                        throw new APIManagementException("Scope '" + firstScope + "' not found.");
                    }
                    template.setScope(scope);
                    template.setScopes(scope);
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
        SwaggerParser parser = new SwaggerParser();
        Swagger swagger = parser.parse(resourceConfigsJSON);
        String oauth2SchemeKey = getOAuth2SecuritySchemeKey(swagger);

        Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
        OAuth2Definition oAuth2Definition;
        if (oauth2SchemeKey != null && securityDefinitions != null
                && (oAuth2Definition = (OAuth2Definition) securityDefinitions.get(oauth2SchemeKey)) != null
                && oAuth2Definition.getScopes() != null) {
            Set<Scope> scopeSet = new HashSet<>();
            for (Map.Entry<String, String> entry : oAuth2Definition.getScopes().entrySet()) {
                Scope scope = new Scope();
                scope.setKey(entry.getKey());
                scope.setName(entry.getKey());
                scope.setDescription(entry.getValue());
                scopeSet.add(scope);
                Map<String, String> scopeBindings;
                if (oAuth2Definition.getVendorExtensions() != null &&
                        (scopeBindings = (Map<String, String>) oAuth2Definition.getVendorExtensions()
                                .get(APIConstants.SWAGGER_X_SCOPES_BINDINGS)) != null) {
                    if (scopeBindings.get(scope.getKey()) != null) {
                        scope.setRoles(scopeBindings.get(scope.getKey()));
                    }
                }
            }
            return scopeSet;
        } else {
            return getScopesFromExtensions(swagger);
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
            Map<String, Object> securityDefinitions = (Map<String, Object>) extensions
                    .get(APIConstants.SWAGGER_X_WSO2_SECURITY);
            for (Map.Entry<String, Object> entry : securityDefinitions.entrySet()) {
                Map<String, Object> securityDefinition = (Map<String, Object>) entry.getValue();
                if (securityDefinition.containsKey(APIConstants.SWAGGER_X_WSO2_SCOPES)) {
                    List<Map<String, String>> oauthScope = (List<Map<String, String>>) securityDefinition
                            .get(APIConstants.SWAGGER_X_WSO2_SCOPES);
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

    @Override
    public void saveAPIDefinition(APIProduct apiProduct, String apiDefinitionJSON, Registry registry)
            throws APIManagementException {

    }

    /**
     * This method generates API definition to the given api
     *
     * @param api api
     * @return API definition in string format
     * @throws APIManagementException
     */
    @Override
    public String generateAPIDefinition(API api) throws APIManagementException {
        Swagger swagger = new Swagger();

        //Create info object
        Info info = new Info();
        info.setTitle(api.getId().getApiName());
        if (api.getDescription() != null) {
            info.setDescription(api.getDescription());
        }

        Contact contact = new Contact();
        //Create contact object and map business owner info
        if (api.getBusinessOwner() != null) {
            contact.setName(api.getBusinessOwner());
        }
        if (api.getBusinessOwnerEmail() != null) {
            contact.setEmail(api.getBusinessOwnerEmail());
        }
        if (api.getBusinessOwner() != null || api.getBusinessOwnerEmail() != null) {
            //put contact object to info object
            info.setContact(contact);
        }

        info.setVersion(api.getId().getVersion());
        swagger.setInfo(info);
        updateSwaggerSecurityDefinition(swagger, api);
        for (URITemplate uriTemplate : api.getUriTemplates()) {
            addOrUpdatePathToSwagger(swagger, uriTemplate);
        }

        return getSwaggerJsonString(swagger);
    }

    @Override
    public String generateAPIDefinition(APIProduct apiProduct) throws APIManagementException {
        return null;
    }

    /**
     * This method generates API definition using the given api's URI templates and the swagger.
     * It will alter the provided swagger definition based on the URI templates. For example: if there is a new
     * URI template which is not included in the swagger, it will be added to the swagger as a basic resource. Any
     * additional resources inside the swagger will be removed from the swagger. Changes to scopes, throtting policies,
     * on the resource will be updated on the swagger
     *
     * @param api            api
     * @param swagger        swagger definition
     * @param syncOperations whether to sync operations between API and swagger. If true, the operations of the swagger
     *                       will be synced from the API's operations. Additional operations of the swagger will be
     *                       removed and new operations of API will be added. If false, all the operations will be
     *                       taken from swagger.
     * @return API definition in string format
     * @throws APIManagementException if error occurred when generating API Definition
     */
    @Override
    public String generateAPIDefinition(API api, String swagger, boolean syncOperations) throws APIManagementException {
        SwaggerParser parser = new SwaggerParser();
        Swagger swaggerObj = parser.parse(swagger);

        //Generates below model using the API's URI template
        // path -> [verb1 -> template1, verb2 -> template2, ..]
        Map<String, Map<String, URITemplate>> uriTemplateMap = getURITemplateMap(api);

        for (Map.Entry<String, Path> pathEntry : swaggerObj.getPaths().entrySet()) {
            String pathName = pathEntry.getKey();
            Path path = pathEntry.getValue();
            Map<String, URITemplate> uriTemplatesForPath = uriTemplateMap.get(pathName);
            if (uriTemplatesForPath == null) {
                //remove paths that are not in URI Templates
                swaggerObj.getPaths().remove(pathName);
            } else {
                //If path is available in the URI template, then check for operations(verbs)
                for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                    HttpMethod httpMethod = operationEntry.getKey();
                    Operation operation = operationEntry.getValue();
                    URITemplate template = uriTemplatesForPath.get(httpMethod.toString().toUpperCase());
                    if (template == null) {
                        // if particular operation is not available in URI templates, then remove it from swagger
                        path.set(httpMethod.toString().toLowerCase(), null);
                    } else {
                        // if operation is available in URI templates, update swagger operation
                        // with auth type, scope etc
                        updateOperationManagedInfo(template, operation);
                    }
                }

                // if there are any verbs (operations) not defined in swagger then add them
                for (Map.Entry<String, URITemplate> uriTemplatesForPathEntry : uriTemplatesForPath.entrySet()) {
                    String verb = uriTemplatesForPathEntry.getKey();
                    URITemplate uriTemplate = uriTemplatesForPathEntry.getValue();
                    HttpMethod method = HttpMethod.valueOf(verb.toUpperCase());
                    Operation operation = path.getOperationMap().get(method);
                    if (operation == null) {
                        operation = createOperation(uriTemplate);
                        path.set(uriTemplate.getHTTPVerb().toLowerCase(), operation);
                    }
                }
            }
        }

        // add to swagger if there are any new templates
        for (Map.Entry<String, Map<String, URITemplate>> uriTemplateMapEntry : uriTemplateMap.entrySet()) {
            String path = uriTemplateMapEntry.getKey();
            Map<String, URITemplate> verbMap = uriTemplateMapEntry.getValue();
            if (swaggerObj.getPath(path) == null) {
                for (Map.Entry<String, URITemplate> verbMapEntry : verbMap.entrySet()) {
                    URITemplate uriTemplate = verbMapEntry.getValue();
                    addOrUpdatePathToSwagger(swaggerObj, uriTemplate);
                }
            }
        }

        updateSwaggerSecurityDefinition(swaggerObj, api);
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
                validationResponse.setJsonContent(OASParserUtil.getSwaggerJsonString(parseAttemptForV2.getSwagger()));
            }
        }
        return validationResponse;
    }

    /**
     * Update swagger with security definition
     *
     * @param swagger swagger object
     * @param api     API
     */
    private void updateSwaggerSecurityDefinition(Swagger swagger, API api) {
        OAuth2Definition oAuth2Definition = new OAuth2Definition().password("https://test.com");
        Set<Scope> scopes = api.getScopes();
        if (scopes != null && !scopes.isEmpty()) {
            Map<String, String> scopeBindings = new HashMap<>();
            for (Scope scope : scopes) {
                oAuth2Definition.addScope(scope.getName(), scope.getDescription());
                scopeBindings.put(scope.getName(), scope.getRoles());
            }
            oAuth2Definition.setVendorExtension(APIConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
        }
        swagger.addSecurityDefinition(APIConstants.SWAGGER_APIM_DEFAULT_SECURITY, oAuth2Definition);
        removeLegacyScopesFromSwagger(swagger);
    }

    /**
     * Updates managed info of a provided operation such as auth type and throttling
     *
     * @param uriTemplate URI template
     * @param operation   swagger operation
     */
    private void updateOperationManagedInfo(URITemplate uriTemplate, Operation operation) {
        String authType = uriTemplate.getAuthType();
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
        operation.setVendorExtension(APIConstants.SWAGGER_X_THROTTLING_TIER, uriTemplate.getThrottlingTier());

        removeLegacyScopesFromOperation(operation);
        if(uriTemplate.getScope() != null) {
            String oauth2SchemeKey = APIConstants.SWAGGER_APIM_DEFAULT_SECURITY;
            List<Map<String, List<String>>> security = operation.getSecurity();
            if (security == null) {
                security = new ArrayList<>();
                operation.setSecurity(security);
            }
            for (Map<String, List<String>> requirement : security) {
                if (requirement.get(oauth2SchemeKey) != null) {
                    requirement.put(oauth2SchemeKey, Arrays.asList(uriTemplate.getScope().getKey()));
                    return;
                }
            }
            // if oauth2SchemeKey not present, add a new
            Map<String, List<String>> defaultRequirement = new HashMap<>();
            defaultRequirement.put(oauth2SchemeKey, Arrays.asList(uriTemplate.getScope().getKey()));
            security.add(defaultRequirement);
        }
    }

    /**
     * Remove legacy scope information from swagger operation
     *
     * @param operation
     */
    private void removeLegacyScopesFromOperation(Operation operation) {
        if(isLegacyExtensionsPreserved()) {
            log.debug("preserveLegacyExtensions is enabled.");
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
    private void removeLegacyScopesFromSwagger(Swagger swagger) {
        if(isLegacyExtensionsPreserved()) {
            log.debug("preserveLegacyExtensions is enabled.");
            return;
        }
        Map<String, Object> extensions = swagger.getVendorExtensions();
        if (extensions != null && extensions.containsKey(APIConstants.SWAGGER_X_WSO2_SECURITY)) {
            extensions.remove(APIConstants.SWAGGER_X_WSO2_SECURITY);
        }
    }

    /**
     * Creates a new operation object using the URI template object
     *
     * @param uriTemplate URI template
     * @return a new operation object using the URI template object
     */
    private Operation createOperation(URITemplate uriTemplate) {
        Operation operation = new Operation();
        List<String> pathParams = getPathParamNames(uriTemplate.getUriTemplate());
        for (String pathParam : pathParams) {
            PathParameter pathParameter = new PathParameter();
            pathParameter.setName(pathParam);
            pathParameter.setType("string");
            operation.addParameter(pathParameter);
        }

        updateOperationManagedInfo(uriTemplate, operation);

        Response response = new Response();
        response.setDescription("OK");
        operation.addResponse(APIConstants.SWAGGER_RESPONSE_200, response);
        return operation;
    }

    /**
     * Add a new path based on the provided URI template to swagger if it does not exists. If it exists,
     * adds the respective operation to the existing path
     *
     * @param swagger     swagger object
     * @param uriTemplate URI template
     */
    private void addOrUpdatePathToSwagger(Swagger swagger, URITemplate uriTemplate) {
        Path path;
        if (swagger.getPath(uriTemplate.getUriTemplate()) != null) {
            path = swagger.getPath(uriTemplate.getUriTemplate());
        } else {
            path = new Path();
        }

        Operation operation = createOperation(uriTemplate);
        path.set(uriTemplate.getHTTPVerb().toLowerCase(), operation);

        swagger.path(uriTemplate.getUriTemplate(), path);
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
     * @param oauth2SchemeKey      OAuth2 security element key
     * @param operation Swagger path operation
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
     * Populate definition with wso2 APIM specific information
     *
     * @param oasDefinition OAS definition
     * @param api           API
     * @return Generated OAS definition
     * @throws APIManagementException If an error occurred
     */
    @Override
    public String populateCustomManagementInfo(String oasDefinition, API api) throws APIManagementException {
        return generateAPIDefinition(api, oasDefinition, true);
    }
}
