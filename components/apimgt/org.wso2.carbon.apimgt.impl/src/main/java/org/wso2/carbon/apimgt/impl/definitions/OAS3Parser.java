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
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.HashSet;
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

    /**
     * This method returns URI templates according to the given swagger file
     *
     * @param swaggerData                 API
     * @param resourceConfigsJSON swaggerJSON
     * @return URI Templates
     * @throws APIManagementException
     */
    @Override
    public Set<URITemplate> getURITemplates(SwaggerData swaggerData, String resourceConfigsJSON) throws APIManagementException {
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(resourceConfigsJSON, null, null);
        if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
            throw new APIManagementException("Error Occurred while parsing OpenAPI3 definition.");
        }
        OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
        Set<URITemplate> urlTemplates = new LinkedHashSet<>();
        Set<Scope> scopes = getScopes(resourceConfigsJSON);

        for (String pathKey : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(pathKey);
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                Operation op = entry.getValue();
                URITemplate template = new URITemplate();
                if (APIConstants.SUPPORTED_METHODS.contains(entry.getKey().name().toLowerCase())) {
                    template.setHTTPVerb(entry.getKey().name().toUpperCase());
                    template.setHttpVerbs(entry.getKey().name().toUpperCase());
                    template.setUriTemplate(pathKey);
                    Map<String, Object> extensios = op.getExtensions();
                    if (extensios != null) {
                        if (extensios.containsKey(APIConstants.SWAGGER_X_SCOPE)) {
                            String scopeKey = (String) extensios.get(APIConstants.SWAGGER_X_SCOPE);
                            Scope scope = APIUtil.findScopeByKey(scopes, scopeKey);
                            if (scopeKey != null && scope == null) {
                                throw new APIManagementException("Scope '" + scopeKey + "' not found.");
                            }
                            template.setScope(scope);
                            template.setScopes(scope);
                        }
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
        Set<Scope> scopeList = new LinkedHashSet<>();
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(resourceConfigsJSON, null, null);
        if (CollectionUtils.isNotEmpty(parseAttemptForV3.getMessages())) {
            throw new APIManagementException("Error Occurred while parsing OpenAPI3 definition.");
        }
        OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
        Map<String, Object> extensions = openAPI.getExtensions();
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

        for (SwaggerData.Resource resource : swaggerData.getResources()) {
            createOperationFromTemplate(openAPI, resource);
        }
        updateScopesInDefinition(openAPI, swaggerData);
        return Json.pretty(openAPI);
    }

    /**
     * This method generates API definition using the given api's URI templates and the swagger.
     * It will alter the provided swagger definition based on the URI templates. For example: if there is a new
     * URI template which is not included in the swagger, it will be added to the swagger as a basic resource. Any
     * additional resources inside the swagger will be removed from the swagger. Changes to scopes, throtting policies,
     * on the resource will be updated on the swagger
     *
     * @param swaggerData            api
     * @param swagger        swagger definition
     * @param syncOperations whether to sync operations between API and swagger. If true, the operations of the swagger
     *                       will be synced from the API's operations. Additional operations of the swagger will be
     *                       removed and new operations of API will be added. If false, all the operations will be
     *                       taken from swagger.
     * @return API definition in string format
     * @throws APIManagementException if error occurred when generating API Definition
     */
    @Override
    public String generateAPIDefinition(SwaggerData swaggerData, String swagger, boolean syncOperations) throws APIManagementException {
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(swagger, null, null);
        OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
        Set<SwaggerData.Resource> copy = new HashSet<>(swaggerData.getResources());

        for (String pathKey : openAPI.getPaths().keySet()) {
            PathItem pathItem = openAPI.getPaths().get(pathKey);
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
                    pathItem.readOperationsMap().remove(entry.getKey());
                }
            }
            //remove path
        }

        //adding new opeations to the deinifition
        for (SwaggerData.Resource resource : copy) {
            createOperationFromTemplate(openAPI, resource);
        }
        updateScopesInDefinition(openAPI, swaggerData);
        return Json.pretty(openAPI);
    }

    /**
     * Updates managed info of a provided operation such as auth type and throttling
     *
     * @param resource URI template
     * @param operation   swagger operation
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
        if (resource.getScope() != null) {
            operation.addExtension(APIConstants.SWAGGER_X_SCOPE, resource.getScope().getKey());
        } else {
            if (operation.getExtensions().containsKey(APIConstants.SWAGGER_X_SCOPE)) {
                operation.getExtensions().remove(APIConstants.SWAGGER_X_SCOPE);
            }
        }
    }

    /**
     * Creates a new operation object using the URI template object
     *
     * @param resource Swagger Resource data
     * @return a new operation object using the URI template object
     */
    private void createOperationFromTemplate(OpenAPI openAPI, SwaggerData.Resource resource) {
        if (openAPI.getPaths() == null) {
            openAPI.setPaths(new Paths());
        }
        String pathName = resource.getPath();
        PathItem pathItem;
        if (openAPI.getPaths().containsKey(pathName)) {
            pathItem = openAPI.getPaths().get(pathName);
        } else {
            pathItem = new PathItem();
            openAPI.getPaths().addPathItem(pathName, pathItem);
        }
        Operation operation = new Operation();
        PathItem.HttpMethod httpMethod = PathItem.HttpMethod.valueOf(resource.getVerb());
        pathItem.operation(httpMethod, operation);
        updateOperationManagedInfo(resource, operation);

        ApiResponses apiResponses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.description("OK");
        apiResponses.addApiResponse(APIConstants.SWAGGER_RESPONSE_200, apiResponse);

        operation.setResponses(apiResponses);
        populatePathParameters(operation, pathName);
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
     * Include Scope details to the definition
     *  @param openAPI openapi definition
     * @param swaggerData     API data
     */
    private void updateScopesInDefinition(OpenAPI openAPI, SwaggerData swaggerData) {
        Set<Scope> scopes = swaggerData.getScopes();
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
        openAPI.addExtension(APIConstants.SWAGGER_X_WSO2_SECURITY, securityMap);
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
     * @param swaggerData           API
     * @return Generated OAS definition
     * @throws APIManagementException If an error occurred
     */
    @Override
    public String populateCustomManagementInfo(String oasDefinition, SwaggerData swaggerData) throws APIManagementException {
        return oasDefinition;
    }
}
