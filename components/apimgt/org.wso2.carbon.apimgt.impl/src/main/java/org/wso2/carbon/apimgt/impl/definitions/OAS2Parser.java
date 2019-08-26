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
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
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

        for (String pathString : swagger.getPaths().keySet()) {
            Path path = swagger.getPath(pathString);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                Operation op = entry.getValue();
                URITemplate template = new URITemplate();
                template.setHTTPVerb(entry.getKey().name().toUpperCase());
                template.setHttpVerbs(entry.getKey().name().toUpperCase());
                template.setUriTemplate(pathString);
                Map<String, Object> extensions = op.getVendorExtensions();
                if (extensions != null) {
                    if (extensions.containsKey(APIConstants.SWAGGER_X_SCOPE)) {
                        String scopeKey = (String) extensions.get(APIConstants.SWAGGER_X_SCOPE);
                        Scope scope = APIUtil.findScopeByKey(scopes, scopeKey);
                        if (scopeKey != null && scope == null) {
                            throw new APIManagementException("Scope '" + scopeKey + "' not found.");
                        }
                        template.setScope(scope);
                        template.setScopes(scope);
                    }
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
        Set<Scope> scopeList = new LinkedHashSet<>();
        SwaggerParser parser = new SwaggerParser();
        Swagger swagger = parser.parse(resourceConfigsJSON);
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
        OAuth2Definition oAuth2Definition = new OAuth2Definition().password("https://test.com");

        Set<Scope> scopes = api.getScopes();

        if (scopes != null && !scopes.isEmpty()) {
            List<Map<String, String>> xSecurityScopesArray = new ArrayList<>();
            for (Scope scope : scopes) {
                oAuth2Definition.addScope(scope.getName(), scope.getDescription());

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

        swagger.addSecurityDefinition("OAuth2Security", oAuth2Definition);
        swagger.setInfo(info);

        for (URITemplate uriTemplate : api.getUriTemplates()) {
            addOrUpdatePathToSwagger(swagger, uriTemplate);
        }

        updateScopesInDefinition(swagger, api);
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
                //                swaggerObj.getPaths().remove(pathName);
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

        updateScopesInDefinition(swaggerObj, api);
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
        if (CollectionUtils.isNotEmpty(parseAttemptForV2.getMessages())) {
            validationResponse.setValid(false);
            for (String message : parseAttemptForV2.getMessages()) {
                OASParserUtil.addErrorToValidationResponse(validationResponse, message);
                if (message.contains(APIConstants.SWAGGER_IS_MISSING_MSG)) {
                    ErrorItem errorItem = new ErrorItem();
                    errorItem.setErrorCode(ExceptionCodes.INVALID_OAS2_FOUND.getErrorCode());
                    errorItem.setMessage(ExceptionCodes.INVALID_OAS2_FOUND.getErrorMessage());
                    errorItem.setDescription(ExceptionCodes.INVALID_OAS2_FOUND.getErrorMessage());
                    validationResponse.getErrorItems().add(errorItem);
                }
            }
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
     * Include Scope details to the definition
     *
     * @param swagger openapi definition
     * @param api     API data
     */
    private void updateScopesInDefinition(Swagger swagger, API api) {
        Set<Scope> scopes = api.getScopes();
        List<Map<String, String>> scopesList = new ArrayList<>();
        Map<String, String> aScope;
        for (Scope scope : scopes) {
            aScope = new LinkedHashMap<>();
            aScope.put(APIConstants.SWAGGER_SCOPE_KEY, scope.getKey());
            aScope.put(APIConstants.SWAGGER_NAME, scope.getName());
            aScope.put(APIConstants.SWAGGER_ROLES, scope.getRoles());
            aScope.put(APIConstants.SWAGGER_DESCRIPTION, scope.getDescription());
            scopesList.add(aScope);
        }

        Map<String, Object> securityMap = new LinkedHashMap<>();
        Map<String, Object> apimSecurityMap = new LinkedHashMap<>();
        apimSecurityMap.put(APIConstants.SWAGGER_X_WSO2_SCOPES, scopesList);
        securityMap.put(APIConstants.SWAGGER_OBJECT_NAME_APIM, apimSecurityMap);
        swagger.setVendorExtension(APIConstants.SWAGGER_X_WSO2_SECURITY, securityMap);
    }
}
