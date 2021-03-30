/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.schema.validation.SchemaValidationError;
import graphql.schema.validation.SchemaValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.definitions.OAS2Parser;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLValidationResponseGraphQLInfoDTO;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a publisher rest api utility class.
 */
public class PublisherCommonUtils {

    private static final Log log = LogFactory.getLog(PublisherCommonUtils.class);

    /**
     * Update an API.
     *
     * @param originalAPI    Existing API
     * @param apiDtoToUpdate New API DTO to update
     * @param apiProvider    API Provider
     * @param tokenScopes    Scopes of the token
     * @throws ParseException         If an error occurs while parsing the endpoint configuration
     * @throws CryptoException        If an error occurs while encrypting the secret key of API
     * @throws APIManagementException If an error occurs while updating the API
     * @throws FaultGatewaysException If an error occurs while updating manage of an existing API
     */
    public static API updateApi(API originalAPI, APIDTO apiDtoToUpdate, APIProvider apiProvider, String[] tokenScopes)
            throws ParseException, CryptoException, APIManagementException, FaultGatewaysException {

        APIIdentifier apiIdentifier = originalAPI.getId();
        // Validate if the USER_REST_API_SCOPES is not set in WebAppAuthenticator when scopes are validated
        if (tokenScopes == null) {
            throw new APIManagementException("Error occurred while updating the  API " + originalAPI.getUUID()
                    + " as the token information hasn't been correctly set internally",
                    ExceptionCodes.TOKEN_SCOPES_NOT_SET);
        }
        boolean isGraphql = originalAPI.getType() != null && APIConstants.APITransportType.GRAPHQL.toString()
                .equals(originalAPI.getType());
        boolean isAsyncAPI = originalAPI.getType() != null
                && (APIConstants.APITransportType.WS.toString().equals(originalAPI.getType())
                || APIConstants.APITransportType.WEBSUB.toString().equals(originalAPI.getType())
                || APIConstants.APITransportType.SSE.toString().equals(originalAPI.getType()));

        Scope[] apiDtoClassAnnotatedScopes = APIDTO.class.getAnnotationsByType(Scope.class);
        boolean hasClassLevelScope = checkClassScopeAnnotation(apiDtoClassAnnotatedScopes, tokenScopes);

        JSONParser parser = new JSONParser();
        String oldEndpointConfigString = originalAPI.getEndpointConfig();
        JSONObject oldEndpointConfig = null;
        if (StringUtils.isNotBlank(oldEndpointConfigString)) {
            oldEndpointConfig = (JSONObject) parser.parse(oldEndpointConfigString);
        }
        String oldProductionApiSecret = null;
        String oldSandboxApiSecret = null;

        if (oldEndpointConfig != null) {
            if ((oldEndpointConfig.containsKey(APIConstants.ENDPOINT_SECURITY))) {
                JSONObject oldEndpointSecurity = (JSONObject) oldEndpointConfig.get(APIConstants.ENDPOINT_SECURITY);
                if (oldEndpointSecurity.containsKey(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION)) {
                    JSONObject oldEndpointSecurityProduction = (JSONObject) oldEndpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION);

                    if (oldEndpointSecurityProduction.get(APIConstants.OAuthConstants.OAUTH_CLIENT_ID) != null
                            && oldEndpointSecurityProduction.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)
                            != null) {
                        oldProductionApiSecret = oldEndpointSecurityProduction
                                .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).toString();
                    }
                }
                if (oldEndpointSecurity.containsKey(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX)) {
                    JSONObject oldEndpointSecuritySandbox = (JSONObject) oldEndpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX);

                    if (oldEndpointSecuritySandbox.get(APIConstants.OAuthConstants.OAUTH_CLIENT_ID) != null
                            && oldEndpointSecuritySandbox.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)
                            != null) {
                        oldSandboxApiSecret = oldEndpointSecuritySandbox
                                .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).toString();
                    }
                }
            }
        }

        Map endpointConfig = (Map) apiDtoToUpdate.getEndpointConfig();
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

        // OAuth 2.0 backend protection: Api Key and Api Secret encryption while updating the API
        String customParametersString = "{}";
        if (endpointConfig != null) {
            if ((endpointConfig.get(APIConstants.ENDPOINT_SECURITY) != null)) {
                Map endpointSecurity = (Map) endpointConfig.get(APIConstants.ENDPOINT_SECURITY);
                if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                    Map endpointSecurityProduction = (Map) endpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION);
                    String productionEndpointType = (String) endpointSecurityProduction
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);

                    // Change default value of customParameters JSONObject to String
                    if (!(endpointSecurityProduction
                            .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS) instanceof String)) {
                        LinkedHashMap<String, String> customParametersHashMap =
                                (LinkedHashMap<String, String>) endpointSecurityProduction
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                        customParametersString = JSONObject.toJSONString(customParametersHashMap);
                    }

                    endpointSecurityProduction
                            .put(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParametersString);

                    if (APIConstants.OAuthConstants.OAUTH.equals(productionEndpointType)) {
                        String apiSecret = endpointSecurityProduction
                                .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET).toString();

                        if (StringUtils.isNotEmpty(apiSecret)) {
                            String encryptedApiSecret = cryptoUtil.encryptAndBase64Encode(apiSecret.getBytes());
                            endpointSecurityProduction
                                    .put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, encryptedApiSecret);
                        } else {
                            endpointSecurityProduction
                                    .put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, oldProductionApiSecret);
                        }
                    }
                    endpointSecurity
                            .put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION, endpointSecurityProduction);
                    endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    apiDtoToUpdate.setEndpointConfig(endpointConfig);
                }
                if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                    Map endpointSecuritySandbox = (Map) endpointSecurity
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX);
                    String sandboxEndpointType = (String) endpointSecuritySandbox
                            .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);

                    // Change default value of customParameters JSONObject to String
                    if (!(endpointSecuritySandbox
                            .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS) instanceof String)) {
                        Map<String, String> customParametersHashMap = (Map<String, String>) endpointSecuritySandbox
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                        customParametersString = JSONObject.toJSONString(customParametersHashMap);
                    }
                    endpointSecuritySandbox
                            .put(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParametersString);

                    if (APIConstants.OAuthConstants.OAUTH.equals(sandboxEndpointType)) {
                        String apiSecret = endpointSecuritySandbox.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET)
                                .toString();

                        if (StringUtils.isNotEmpty(apiSecret)) {
                            String encryptedApiSecret = cryptoUtil.encryptAndBase64Encode(apiSecret.getBytes());
                            endpointSecuritySandbox
                                    .put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, encryptedApiSecret);
                        } else {
                            endpointSecuritySandbox
                                    .put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET, oldSandboxApiSecret);
                        }
                    }
                    endpointSecurity
                            .put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX, endpointSecuritySandbox);
                    endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    apiDtoToUpdate.setEndpointConfig(endpointConfig);
                }
            }
        }

        // AWS Lambda: secret key encryption while updating the API
        if (apiDtoToUpdate.getEndpointConfig() != null) {
            if (endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)) {
                String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                if (!StringUtils.isEmpty(secretKey)) {
                    if (!APIConstants.AWS_SECRET_KEY.equals(secretKey)) {
                        String encryptedSecretKey = cryptoUtil.encryptAndBase64Encode(secretKey.getBytes());
                        endpointConfig.put(APIConstants.AMZN_SECRET_KEY, encryptedSecretKey);
                        apiDtoToUpdate.setEndpointConfig(endpointConfig);
                    } else {
                        JSONParser jsonParser = new JSONParser();
                        JSONObject originalEndpointConfig = (JSONObject) jsonParser
                                .parse(originalAPI.getEndpointConfig());
                        String encryptedSecretKey = (String) originalEndpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                        endpointConfig.put(APIConstants.AMZN_SECRET_KEY, encryptedSecretKey);
                        apiDtoToUpdate.setEndpointConfig(endpointConfig);
                    }
                }
            }
        }

        if (!hasClassLevelScope) {
            // Validate per-field scopes
            apiDtoToUpdate = getFieldOverriddenAPIDTO(apiDtoToUpdate, originalAPI, tokenScopes);
        }
        //Overriding some properties:
        apiDtoToUpdate.setName(apiIdentifier.getApiName());
        apiDtoToUpdate.setVersion(apiIdentifier.getVersion());
        apiDtoToUpdate.setProvider(apiIdentifier.getProviderName());
        apiDtoToUpdate.setContext(originalAPI.getContextTemplate());
        apiDtoToUpdate.setLifeCycleStatus(originalAPI.getStatus());
        apiDtoToUpdate.setType(APIDTO.TypeEnum.fromValue(originalAPI.getType()));

        List<APIResource> removedProductResources = getRemovedProductResources(apiDtoToUpdate, originalAPI);

        if (!removedProductResources.isEmpty()) {
            throw new APIManagementException(
                    "Cannot remove following resource paths " + removedProductResources.toString()
                            + " because they are used by one or more API Products", ExceptionCodes
                    .from(ExceptionCodes.API_PRODUCT_USED_RESOURCES, originalAPI.getId().getApiName(),
                            originalAPI.getId().getVersion()));
        }

        // Validate API Security
        List<String> apiSecurity = apiDtoToUpdate.getSecurityScheme();
        //validation for tiers
        List<String> tiersFromDTO = apiDtoToUpdate.getPolicies();
        String originalStatus = originalAPI.getStatus();
        if (apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2) || apiSecurity
                .contains(APIConstants.API_SECURITY_API_KEY)) {
            if (tiersFromDTO == null || tiersFromDTO.isEmpty() && !(APIConstants.CREATED.equals(originalStatus)
                    || APIConstants.PROTOTYPED.equals(originalStatus))) {
                throw new APIManagementException(
                        "A tier should be defined if the API is not in CREATED or PROTOTYPED state",
                        ExceptionCodes.TIER_CANNOT_BE_NULL);
            }
        }

        if (tiersFromDTO != null && !tiersFromDTO.isEmpty()) {
            //check whether the added API's tiers are all valid
            Set<Tier> definedTiers = apiProvider.getTiers();
            List<String> invalidTiers = getInvalidTierNames(definedTiers, tiersFromDTO);
            if (invalidTiers.size() > 0) {
                throw new APIManagementException(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid",
                        ExceptionCodes.TIER_NAME_INVALID);
            }
        }
        if (apiDtoToUpdate.getAccessControlRoles() != null) {
            String errorMessage = validateUserRoles(apiDtoToUpdate.getAccessControlRoles());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_USER_ROLES);
            }
        }
        if (apiDtoToUpdate.getVisibleRoles() != null) {
            String errorMessage = validateRoles(apiDtoToUpdate.getVisibleRoles());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_USER_ROLES);
            }
        }
        if (apiDtoToUpdate.getAdditionalProperties() != null) {
            String errorMessage = validateAdditionalProperties(apiDtoToUpdate.getAdditionalProperties());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes
                        .from(ExceptionCodes.INVALID_ADDITIONAL_PROPERTIES, apiDtoToUpdate.getName(),
                                apiDtoToUpdate.getVersion()));
            }
        }
        // Validate if resources are empty
        if (apiDtoToUpdate.getOperations() == null || apiDtoToUpdate.getOperations().isEmpty()) {
            throw new APIManagementException(ExceptionCodes.NO_RESOURCES_FOUND);
        }
        API apiToUpdate = APIMappingUtil.fromDTOtoAPI(apiDtoToUpdate, apiIdentifier.getProviderName());
        if (APIConstants.PUBLIC_STORE_VISIBILITY.equals(apiToUpdate.getVisibility())) {
            apiToUpdate.setVisibleRoles(StringUtils.EMPTY);
        }
        apiToUpdate.setUUID(originalAPI.getUUID());
        validateScopes(apiToUpdate);
        apiToUpdate.setThumbnailUrl(originalAPI.getThumbnailUrl());
        if (apiDtoToUpdate.getKeyManagers() instanceof List) {
            apiToUpdate.setKeyManagers((List<String>) apiDtoToUpdate.getKeyManagers());
        } else {
            apiToUpdate.setKeyManagers(Collections.singletonList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS));
        }

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

        //preserve monetization status in the update flow
        //apiProvider.configureMonetizationInAPIArtifact(originalAPI); ////////////TODO /////////REG call
        apiIdentifier.setUuid(apiToUpdate.getUuid());

        if (!isAsyncAPI) {
            String oldDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier, tenantDomain);
            APIDefinition apiDefinition = OASParserUtil.getOASParser(oldDefinition);
            SwaggerData swaggerData = new SwaggerData(apiToUpdate);
            String newDefinition = apiDefinition.generateAPIDefinition(swaggerData, oldDefinition);
            apiProvider.saveSwaggerDefinition(apiToUpdate, newDefinition, tenantDomain);
            if (!isGraphql) {
                apiToUpdate.setUriTemplates(apiDefinition.getURITemplates(newDefinition));
            }
        } else {
             String oldDefinition = apiProvider.getAsyncAPIDefinition(apiIdentifier.getUUID(), tenantDomain);
            AsyncApiParser asyncApiParser = new AsyncApiParser();
            String updateAsyncAPIDefinition = asyncApiParser.updateAsyncAPIDefinition(oldDefinition, apiToUpdate);
            apiProvider.saveAsyncApiDefinition(originalAPI, updateAsyncAPIDefinition);
        }
        apiToUpdate.setWsdlUrl(apiDtoToUpdate.getWsdlUrl());

        //validate API categories
        List<APICategory> apiCategories = apiToUpdate.getApiCategories();
        if (apiCategories != null && apiCategories.size() > 0) {
            if (!APIUtil.validateAPICategories(apiCategories, RestApiCommonUtil.getLoggedInUserTenantDomain())) {
                throw new APIManagementException("Invalid API Category name(s) defined",
                        ExceptionCodes.from(ExceptionCodes.API_CATEGORY_INVALID));
            }
        }

        apiProvider.updateAPI(apiToUpdate, originalAPI);

        return apiProvider.getAPIbyUUID(originalAPI.getUuid(),
                CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        // TODO use returend api
    }

    /**
     * Check whether the token has APIDTO class level Scope annotation.
     *
     * @return true if the token has APIDTO class level Scope annotation
     */
    private static boolean checkClassScopeAnnotation(Scope[] apiDtoClassAnnotatedScopes, String[] tokenScopes) {

        for (Scope classAnnotation : apiDtoClassAnnotatedScopes) {
            for (String tokenScope : tokenScopes) {
                if (classAnnotation.name().equals(tokenScope)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Override the API DTO field values with the user passed new values considering the field-wise scopes defined as
     * allowed to update in REST API definition yaml.
     */
    private static JSONObject overrideDTOValues(JSONObject originalApiDtoJson, JSONObject newApiDtoJson, Field field,
                                                String[] tokenScopes, Scope[] fieldAnnotatedScopes)
            throws APIManagementException {

        for (String tokenScope : tokenScopes) {
            for (Scope scopeAnt : fieldAnnotatedScopes) {
                if (scopeAnt.name().equals(tokenScope)) {
                    // do the overriding
                    originalApiDtoJson.put(field.getName(), newApiDtoJson.get(field.getName()));
                    return originalApiDtoJson;
                }
            }
        }
        throw new APIManagementException("User is not authorized to update one or more API fields. None of the "
                + "required scopes found in user token to update the field. So the request will be failed.",
                ExceptionCodes.INVALID_SCOPE);
    }

    /**
     * Get the API DTO object in which the API field values are overridden with the user passed new values.
     *
     * @throws APIManagementException
     */
    private static APIDTO getFieldOverriddenAPIDTO(APIDTO apidto, API originalAPI, String[] tokenScopes)
            throws APIManagementException {

        APIDTO originalApiDTO;
        APIDTO updatedAPIDTO;

        try {
            originalApiDTO = APIMappingUtil.fromAPItoDTO(originalAPI);

            Field[] fields = APIDTO.class.getDeclaredFields();
            ObjectMapper mapper = new ObjectMapper();
            String newApiDtoJsonString = mapper.writeValueAsString(apidto);
            JSONParser parser = new JSONParser();
            JSONObject newApiDtoJson = (JSONObject) parser.parse(newApiDtoJsonString);

            String originalApiDtoJsonString = mapper.writeValueAsString(originalApiDTO);
            JSONObject originalApiDtoJson = (JSONObject) parser.parse(originalApiDtoJsonString);

            for (Field field : fields) {
                Scope[] fieldAnnotatedScopes = field.getAnnotationsByType(Scope.class);
                String originalElementValue = mapper.writeValueAsString(originalApiDtoJson.get(field.getName()));
                String newElementValue = mapper.writeValueAsString(newApiDtoJson.get(field.getName()));

                if (!StringUtils.equals(originalElementValue, newElementValue)) {
                    originalApiDtoJson = overrideDTOValues(originalApiDtoJson, newApiDtoJson, field, tokenScopes,
                            fieldAnnotatedScopes);
                }
            }

            updatedAPIDTO = mapper.readValue(originalApiDtoJson.toJSONString(), APIDTO.class);

        } catch (IOException | ParseException e) {
            String msg = "Error while processing API DTO json strings";
            throw new APIManagementException(msg, e, ExceptionCodes.JSON_PARSE_ERROR);
        }
        return updatedAPIDTO;
    }

    /**
     * Finds resources that have been removed in the updated API, that are currently reused by API Products.
     *
     * @param updatedDTO  Updated API
     * @param existingAPI Existing API
     * @return List of removed resources that are reused among API Products
     */
    private static List<APIResource> getRemovedProductResources(APIDTO updatedDTO, API existingAPI) {

        List<APIOperationsDTO> updatedOperations = updatedDTO.getOperations();
        Set<URITemplate> existingUriTemplates = existingAPI.getUriTemplates();
        List<APIResource> removedReusedResources = new ArrayList<>();

        for (URITemplate existingUriTemplate : existingUriTemplates) {

            // If existing URITemplate is used by any API Products
            if (!existingUriTemplate.retrieveUsedByProducts().isEmpty()) {
                String existingVerb = existingUriTemplate.getHTTPVerb();
                String existingPath = existingUriTemplate.getUriTemplate();
                boolean isReusedResourceRemoved = true;

                for (APIOperationsDTO updatedOperation : updatedOperations) {
                    String updatedVerb = updatedOperation.getVerb();
                    String updatedPath = updatedOperation.getTarget();

                    //Check if existing reused resource is among updated resources
                    if (existingVerb.equalsIgnoreCase(updatedVerb) && existingPath.equalsIgnoreCase(updatedPath)) {
                        isReusedResourceRemoved = false;
                        break;
                    }
                }

                // Existing reused resource is not among updated resources
                if (isReusedResourceRemoved) {
                    APIResource removedResource = new APIResource(existingVerb, existingPath);
                    removedReusedResources.add(removedResource);
                }
            }
        }

        return removedReusedResources;
    }

    /**
     * To validate the roles against user roles and tenant roles.
     *
     * @param inputRoles Input roles.
     * @return relevant error string or empty string.
     * @throws APIManagementException API Management Exception.
     */
    public static String validateUserRoles(List<String> inputRoles) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        String[] tenantRoleList = APIUtil.getRoleNames(userName);
        boolean isMatched = false;
        String[] userRoleList = null;

        if (APIUtil.hasPermission(userName, APIConstants.Permissions.APIM_ADMIN)) {
            isMatched = true;
        } else {
            userRoleList = APIUtil.getListOfRoles(userName);
        }
        if (inputRoles != null && !inputRoles.isEmpty()) {
            if (tenantRoleList != null || userRoleList != null) {
                for (String inputRole : inputRoles) {
                    if (!isMatched && userRoleList != null && APIUtil.compareRoleList(userRoleList, inputRole)) {
                        isMatched = true;
                    }
                    if (tenantRoleList != null && !APIUtil.compareRoleList(tenantRoleList, inputRole)) {
                        return "Invalid user roles found in accessControlRole list";
                    }
                }
                return isMatched ? "" : "This user does not have at least one role specified in API access control.";
            } else {
                return "Invalid user roles found";
            }
        }
        return "";
    }

    /**
     * To validate the roles against and tenant roles.
     *
     * @param inputRoles Input roles.
     * @return relevant error string or empty string.
     * @throws APIManagementException API Management Exception.
     */
    public static String validateRoles(List<String> inputRoles) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        boolean isMatched = false;
        if (inputRoles != null && !inputRoles.isEmpty()) {
            String roleString = String.join(",", inputRoles);
            isMatched = APIUtil.isRoleNameExist(userName, roleString);
            if (!isMatched) {
                return "Invalid user roles found in visibleRoles list";
            }
        }
        return "";
    }

    /**
     * To validate the additional properties.
     * Validation will be done for the keys of additional properties. Property keys should not contain spaces in it
     * and property keys should not conflict with reserved key words.
     *
     * @param additionalProperties Map<String, String>  properties to validate
     * @return error message if there is an validation error with additional properties.
     */
    public static String validateAdditionalProperties(Map<String, String> additionalProperties) {

        if (additionalProperties != null) {
            for (Map.Entry<String, String> entry : additionalProperties.entrySet()) {
                String propertyKey = entry.getKey().trim();
                String propertyValue = entry.getValue();
                if (propertyKey.contains(" ")) {
                    return "Property names should not contain space character. Property '" + propertyKey + "' "
                            + "contains space in it.";
                }
                if (Arrays.asList(APIConstants.API_SEARCH_PREFIXES).contains(propertyKey.toLowerCase())) {
                    return "Property '" + propertyKey + "' conflicts with the reserved keywords. Reserved keywords "
                            + "are [" + Arrays.toString(APIConstants.API_SEARCH_PREFIXES) + "]";
                }
                // Maximum allowable characters of registry property name and value is 100 and 1000. Hence we are
                // restricting them to be within 80 and 900.
                if (propertyKey.length() > 80) {
                    return "Property name can have maximum of 80 characters. Property '" + propertyKey + "' + contains "
                            + propertyKey.length() + "characters";
                }
                if (propertyValue.length() > 900) {
                    return "Property value can have maximum of 900 characters. Property '" + propertyKey + "' + "
                            + "contains a value with " + propertyValue.length() + "characters";
                }
            }
        }
        return "";
    }

    /**
     * validate user inout scopes.
     *
     * @param api api information
     * @throws APIManagementException throw if validation failure
     */
    public static void validateScopes(API api) throws APIManagementException {

        APIIdentifier apiId = api.getId();
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        Set<org.wso2.carbon.apimgt.api.model.Scope> sharedAPIScopes = new HashSet<>();

        for (org.wso2.carbon.apimgt.api.model.Scope scope : api.getScopes()) {
            String scopeName = scope.getKey();
            if (!(APIUtil.isAllowedScope(scopeName))) {
                // Check if each scope key is already assigned as a local scope to a different API which is also not a
                // different version of the same API. If true, return error.
                // If false, check if the scope key is already defined as a shared scope. If so, do not honor the
                // other scope attributes (description, role bindings) in the request payload, replace them with
                // already defined values for the existing shared scope.
                if (apiProvider.isScopeKeyAssignedLocally(apiId, scopeName, tenantId)) {
                    throw new APIManagementException(
                            "Scope " + scopeName + " is already assigned locally by another API",
                            ExceptionCodes.SCOPE_ALREADY_ASSIGNED);
                } else if (apiProvider.isSharedScopeNameExists(scopeName, tenantDomain)) {
                    sharedAPIScopes.add(scope);
                    continue;
                }
            }

            //set display name as empty if it is not provided
            if (StringUtils.isBlank(scope.getName())) {
                scope.setName(scopeName);
            }

            //set description as empty if it is not provided
            if (StringUtils.isBlank(scope.getDescription())) {
                scope.setDescription("");
            }
            if (scope.getRoles() != null) {
                for (String aRole : scope.getRoles().split(",")) {
                    boolean isValidRole = APIUtil.isRoleNameExist(username, aRole);
                    if (!isValidRole) {
                        throw new APIManagementException("Role '" + aRole + "' does not exist.",
                                ExceptionCodes.ROLE_DOES_NOT_EXIST);
                    }
                }
            }
        }

        apiProvider.validateSharedScopes(sharedAPIScopes, tenantDomain);
    }

    /**
     * Add API with the generated swagger from the DTO.
     *
     * @param apiDto     API DTO of the API
     * @param oasVersion Open API Definition version
     * @param username   Username
     * @return Created API object
     * @throws APIManagementException Error while creating the API
     * @throws CryptoException        Error while encrypting
     */
    public static API addAPIWithGeneratedSwaggerDefinition(APIDTO apiDto, String oasVersion, String username)
            throws APIManagementException, CryptoException {

        boolean isWSAPI = APIDTO.TypeEnum.WS.equals(apiDto.getType());
        boolean isAsyncAPI =
                isWSAPI || APIDTO.TypeEnum.WEBSUB.equals(apiDto.getType()) ||
                        APIDTO.TypeEnum.SSE.equals(apiDto.getType());
        username = StringUtils.isEmpty(username) ? RestApiCommonUtil.getLoggedInUsername() : username;
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);

        // validate web socket api endpoint configurations
        if (isWSAPI && !PublisherCommonUtils.isValidWSAPI(apiDto)) {
            throw new APIManagementException("Endpoint URLs should be valid web socket URLs",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }

        // AWS Lambda: secret key encryption while creating the API
        if (apiDto.getEndpointConfig() != null) {
            Map endpointConfig = (Map) apiDto.getEndpointConfig();
            if (endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)) {
                String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                if (!StringUtils.isEmpty(secretKey)) {
                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                    String encryptedSecretKey = cryptoUtil.encryptAndBase64Encode(secretKey.getBytes());
                    endpointConfig.put(APIConstants.AMZN_SECRET_KEY, encryptedSecretKey);
                    apiDto.setEndpointConfig(endpointConfig);
                }
            }
        }

       /* if (isWSAPI) {
            ArrayList<String> websocketTransports = new ArrayList<>();
            websocketTransports.add(APIConstants.WS_PROTOCOL);
            websocketTransports.add(APIConstants.WSS_PROTOCOL);
            apiDto.setTransport(websocketTransports);
        }*/

        API apiToAdd = prepareToCreateAPIByDTO(apiDto, apiProvider, username);
        validateScopes(apiToAdd);
        //validate API categories
        List<APICategory> apiCategories = apiToAdd.getApiCategories();
        if (apiCategories != null && apiCategories.size() > 0) {
            if (!APIUtil.validateAPICategories(apiCategories, RestApiCommonUtil.getLoggedInUserTenantDomain())) {
                throw new APIManagementException("Invalid API Category name(s) defined",
                        ExceptionCodes.from(ExceptionCodes.API_CATEGORY_INVALID));
            }
        }

        if (!isAsyncAPI) {
            APIDefinition oasParser;
            if (RestApiConstants.OAS_VERSION_2.equalsIgnoreCase(oasVersion)) {
                oasParser = new OAS2Parser();
            } else {
                oasParser = new OAS3Parser();
            }
            SwaggerData swaggerData = new SwaggerData(apiToAdd);
            String apiDefinition = oasParser.generateAPIDefinition(swaggerData);
            apiToAdd.setSwaggerDefinition(apiDefinition);
        } else {
            AsyncApiParser asyncApiParser = new AsyncApiParser();
            String asyncApiDefinition = asyncApiParser.generateAsyncAPIDefinition(apiToAdd);
            apiToAdd.setAsyncApiDefinition(asyncApiDefinition);
        }

        if (isAsyncAPI) {
            AsyncApiParser asyncApiParser = new AsyncApiParser();
            String apiDefinition = asyncApiParser.generateAsyncAPIDefinition(apiToAdd);
            apiProvider.saveAsyncApiDefinition(apiToAdd, apiDefinition);
        }

        //adding the api
        apiProvider.addAPI(apiToAdd);
        return apiToAdd;
    }

    /**
     * Validate endpoint configurations of {@link APIDTO} for web socket endpoints.
     *
     * @param api api model
     * @return validity of the web socket api
     */
    public static boolean isValidWSAPI(APIDTO api) {

        boolean isValid = false;

        if (api.getEndpointConfig() != null) {
            Map endpointConfig = (Map) api.getEndpointConfig();
            String prodEndpointUrl = String
                    .valueOf(((Map) endpointConfig.get("production_endpoints")).get("url"));
            String sandboxEndpointUrl = String
                    .valueOf(((Map) endpointConfig.get("sandbox_endpoints")).get("url"));
            isValid = prodEndpointUrl.startsWith("ws://") || prodEndpointUrl.startsWith("wss://");

            if (isValid) {
                isValid = sandboxEndpointUrl.startsWith("ws://") || sandboxEndpointUrl.startsWith("wss://");
            }
        }

        return isValid;
    }

    public static String constructEndpointConfigForService(String serviceUrl, String protocol) {

        StringBuilder sb = new StringBuilder();
        String endpointType = APIDTO.TypeEnum.HTTP.value().toLowerCase();
        if (StringUtils.isNotEmpty(protocol) && (APIDTO.TypeEnum.SSE.equals(protocol.toUpperCase())
                || APIDTO.TypeEnum.WS.equals(protocol.toUpperCase()))) {
            endpointType = "ws";
        }
        if (StringUtils.isNotEmpty(serviceUrl)) {
            sb.append("{\"endpoint_type\": \"")
                    .append(endpointType)
                    .append("\",")
                    .append("\"production_endpoints\": {\"url\": \"")
                    .append(serviceUrl)
                    .append("\"}}");
        } // TODO Need to check on the endpoint security
        return sb.toString();
    }

    public static APIDTO.TypeEnum getAPIType(ServiceEntry.DefinitionType definitionType, String protocol) {

        switch (definitionType) {
            case WSDL1:
            case WSDL2:
                return APIDTO.TypeEnum.SOAP;
            case GRAPHQL_SDL:
                return APIDTO.TypeEnum.GRAPHQL;
            case ASYNC_API:
                return APIDTO.TypeEnum.fromValue(protocol.toUpperCase());
            default:
                return APIDTO.TypeEnum.HTTP;
        }
    }

    /**
     * Prepares the API Model object to be created using the DTO object.
     *
     * @param body        APIDTO of the API
     * @param apiProvider API Provider
     * @param username    Username
     * @return API object to be created
     * @throws APIManagementException Error while creating the API
     */
    public static API prepareToCreateAPIByDTO(APIDTO body, APIProvider apiProvider, String username)
            throws APIManagementException {

        String context = body.getContext();
        //Make sure context starts with "/". ex: /pizza
        context = context.startsWith("/") ? context : ("/" + context);

        if (body.getAccessControlRoles() != null) {
            String errorMessage = PublisherCommonUtils.validateUserRoles(body.getAccessControlRoles());

            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_USER_ROLES);
            }
        }
        if (body.getAdditionalProperties() != null) {
            String errorMessage = PublisherCommonUtils.validateAdditionalProperties(body.getAdditionalProperties());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes
                        .from(ExceptionCodes.INVALID_ADDITIONAL_PROPERTIES, body.getName(), body.getVersion()));
            }
        }
        if (body.getContext() == null) {
            throw new APIManagementException("Parameter: \"context\" cannot be null",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        } else if (body.getContext().endsWith("/")) {
            throw new APIManagementException("Context cannot end with '/' character", ExceptionCodes.INVALID_CONTEXT);
        }
        if (apiProvider.isApiNameWithDifferentCaseExist(body.getName())) {
            throw new APIManagementException(
                    "Error occurred while adding API. API with name " + body.getName() + " already exists.",
                    ExceptionCodes.from(ExceptionCodes.API_NAME_ALREADY_EXISTS, body.getName()));
        }
        if (body.getAuthorizationHeader() == null) {
            body.setAuthorizationHeader(APIUtil.getOAuthConfigurationFromAPIMConfig(APIConstants.AUTHORIZATION_HEADER));
        }
        if (body.getAuthorizationHeader() == null) {
            body.setAuthorizationHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT);
        }

        if (body.getVisibility() == APIDTO.VisibilityEnum.RESTRICTED && body.getVisibleRoles().isEmpty()) {
            throw new APIManagementException(
                    "Valid roles should be added under 'visibleRoles' to restrict " + "the visibility",
                    ExceptionCodes.USER_ROLES_CANNOT_BE_NULL);
        }
        if (body.getVisibleRoles() != null) {
            String errorMessage = PublisherCommonUtils.validateRoles(body.getVisibleRoles());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_USER_ROLES);
            }
        }

        //Get all existing versions of  api been adding
        List<String> apiVersions = apiProvider.getApiVersionsMatchingApiName(body.getName(), username);
        if (apiVersions.size() > 0) {
            //If any previous version exists
            for (String version : apiVersions) {
                if (version.equalsIgnoreCase(body.getVersion())) {
                    //If version already exists
                    throw new APIManagementException(
                            "Error occurred while adding the API. The version already exists: " + version,
                            ExceptionCodes.from(ExceptionCodes.API_VERSION_ALREADY_EXISTS, version, body.getName()));
                }
            }
        } else {
            //If no any previous version exists
            if (apiProvider.isDuplicateContextTemplate(context)) {
                throw new APIManagementException(
                        "Error occurred while adding the API. A duplicate API context already exists for "
                                + context, ExceptionCodes.from(ExceptionCodes.API_CONTEXT_ALREADY_EXISTS, context));
            }
        }

        //Check if the user has admin permission before applying a different provider than the current user
        String provider = body.getProvider();
        if (!StringUtils.isBlank(provider) && !provider.equals(username)) {
            if (!APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN)) {
                if (log.isDebugEnabled()) {
                    log.debug("User " + username + " does not have admin permission ("
                            + APIConstants.Permissions.APIM_ADMIN + ") hence provider (" + provider
                            + ") overridden with current user (" + username + ")");
                }
                provider = username;
            } else {
                if (!APIUtil.isUserExist(provider)) {
                    throw new APIManagementException("Specified provider " + provider + " not exist.",
                            ExceptionCodes.PARAMETER_NOT_PROVIDED);
                }
            }
        } else {
            //Set username in case provider is null or empty
            provider = username;
        }

        List<String> tiersFromDTO = body.getPolicies();

        //check whether the added API's tiers are all valid
        Set<Tier> definedTiers = apiProvider.getTiers();
        List<String> invalidTiers = getInvalidTierNames(definedTiers, tiersFromDTO);
        if (invalidTiers.size() > 0) {
            throw new APIManagementException(
                    "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid",
                    ExceptionCodes.TIER_NAME_INVALID);
        }
        APIPolicy apiPolicy = apiProvider.getAPIPolicy(username, body.getApiThrottlingPolicy());
        if (apiPolicy == null && body.getApiThrottlingPolicy() != null) {
            throw new APIManagementException("Specified policy " + body.getApiThrottlingPolicy() + " is invalid",
                    ExceptionCodes.UNSUPPORTED_THROTTLE_LIMIT_TYPE);
        }

        API apiToAdd = APIMappingUtil.fromDTOtoAPI(body, provider);
        //Overriding some properties:
        //only allow CREATED as the stating state for the new api if not status is PROTOTYPED
        if (!APIConstants.PROTOTYPED.equals(apiToAdd.getStatus())) {
            apiToAdd.setStatus(APIConstants.CREATED);
        }

        if (!apiToAdd.isAdvertiseOnly() || StringUtils.isBlank(apiToAdd.getApiOwner())) {
            //we are setting the api owner as the logged in user until we support checking admin privileges and
            //assigning the owner as a different user
            apiToAdd.setApiOwner(provider);
        }

        if (body.getKeyManagers() instanceof List) {
            apiToAdd.setKeyManagers((List<String>) body.getKeyManagers());
        } else if (body.getKeyManagers() == null) {
            apiToAdd.setKeyManagers(Collections.singletonList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS));
        } else {
            throw new APIManagementException("KeyManagers value need to be an array");
        }
        return apiToAdd;
    }

    public static String updateAPIDefinition(String apiId, APIDefinitionValidationResponse response,
                                             ServiceEntry service) throws APIManagementException,
            FaultGatewaysException {

        if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) ||
                ServiceEntry.DefinitionType.OAS3.equals(service.getDefinitionType())) {
            return updateSwagger(apiId, response, true);
        } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
            return updateAsyncAPIDefinition(apiId, response);
        }
        return null;
    }

    /**
     * update AsyncPI definition of the given api.
     *
     * @param apiId    API Id
     * @param response response of the AsyncAPI definition validation call
     * @return updated AsyncAPI definition
     * @throws APIManagementException when error occurred updating AsyncAPI definition
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    public static String updateAsyncAPIDefinition(String apiId, APIDefinitionValidationResponse response)
            throws APIManagementException, FaultGatewaysException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        //this will fall if user does not have access to the API or the API does not exist
        API existingAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        String apiDefinition = response.getJsonContent();
        //updating APi with the new AsyncAPI definition
        apiProvider.saveAsyncApiDefinition(existingAPI, apiDefinition);
        apiProvider.updateAPI(existingAPI);
        //load new topics
        apiProvider.updateAPI(AsyncApiParserUtil.loadTopicsFromAsyncAPIDefinition(existingAPI, apiDefinition));
        //retrieves the updated AsyncAPI definition
        return apiProvider.getAsyncAPIDefinition(existingAPI.getId());
    }

    /**
     * update swagger definition of the given api.
     *
     * @param apiId    API Id
     * @param response response of a swagger definition validation call
     * @return updated swagger definition
     * @throws APIManagementException when error occurred updating swagger
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    public static String updateSwagger(String apiId, APIDefinitionValidationResponse response, boolean isServiceAPI)
            throws APIManagementException, FaultGatewaysException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        //this will fail if user does not have access to the API or the API does not exist
        API existingAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        APIDefinition oasParser = response.getParser();
        String apiDefinition = response.getJsonContent();
        if (isServiceAPI) {
            apiDefinition = oasParser.copyVendorExtensions(existingAPI.getSwaggerDefinition(), apiDefinition);
        } else {
            apiDefinition = OASParserUtil.preProcess(apiDefinition);
        }
        Set<URITemplate> uriTemplates = null;
        uriTemplates = oasParser.getURITemplates(apiDefinition);

        if (uriTemplates == null || uriTemplates.isEmpty()) {
            throw new APIManagementException(ExceptionCodes.NO_RESOURCES_FOUND);
        }
        Set<org.wso2.carbon.apimgt.api.model.Scope> scopes = oasParser.getScopes(apiDefinition);
        //validating scope roles
        for (org.wso2.carbon.apimgt.api.model.Scope scope : scopes) {
            String roles = scope.getRoles();
            if (roles != null) {
                for (String aRole : roles.split(",")) {
                    boolean isValidRole = APIUtil.isRoleNameExist(RestApiCommonUtil.getLoggedInUsername(), aRole);
                    if (!isValidRole) {
                        throw new APIManagementException("Role '" + aRole + "' Does not exist.");
                    }
                }
            }
        }

        List<APIResource> removedProductResources = apiProvider.getRemovedProductResources(uriTemplates, existingAPI);

        if (!removedProductResources.isEmpty()) {
            throw new APIManagementException(
                    "Cannot remove following resource paths " + removedProductResources.toString()
                            + " because they are used by one or more API Products", ExceptionCodes
                    .from(ExceptionCodes.API_PRODUCT_USED_RESOURCES, existingAPI.getId().getApiName(),
                            existingAPI.getId().getVersion()));
        }

        existingAPI.setUriTemplates(uriTemplates);
        existingAPI.setScopes(scopes);
        PublisherCommonUtils.validateScopes(existingAPI);

        //Update API is called to update URITemplates and scopes of the API
        SwaggerData swaggerData = new SwaggerData(existingAPI);
        String updatedApiDefinition = oasParser.populateCustomManagementInfo(apiDefinition, swaggerData);
        apiProvider.saveSwaggerDefinition(existingAPI, updatedApiDefinition, tenantDomain);
        existingAPI.setSwaggerDefinition(updatedApiDefinition);
        API unModifiedAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        existingAPI.setStatus(unModifiedAPI.getStatus());
        apiProvider.updateAPI(existingAPI, unModifiedAPI);
        //retrieves the updated swagger definition
        String apiSwagger = apiProvider.getOpenAPIDefinition(apiId, tenantDomain); // TODO see why we need to get it
        // instead of passing same
        return oasParser.getOASDefinitionForPublisher(existingAPI, apiSwagger);
    }

    /**
     * Add GraphQL schema.
     *
     * @param originalAPI      API
     * @param schemaDefinition GraphQL schema definition to add
     * @param apiProvider      API Provider
     * @return the arrayList of APIOperationsDTOextractGraphQLOperationList
     */
    public static API addGraphQLSchema(API originalAPI, String schemaDefinition, APIProvider apiProvider)
            throws APIManagementException, FaultGatewaysException {

        List<APIOperationsDTO> operationListWithOldData = APIMappingUtil
                .getOperationListWithOldData(originalAPI.getUriTemplates(),
                        extractGraphQLOperationList(schemaDefinition));

        Set<URITemplate> uriTemplates = APIMappingUtil.getURITemplates(originalAPI, operationListWithOldData);
        originalAPI.setUriTemplates(uriTemplates);

        apiProvider.saveGraphqlSchemaDefinition(originalAPI, schemaDefinition);
        apiProvider.updateAPI(originalAPI);

        return originalAPI;
    }

    /**
     * Extract GraphQL Operations from given schema.
     *
     * @param schema graphQL Schema
     * @return the arrayList of APIOperationsDTOextractGraphQLOperationList
     */
    public static List<APIOperationsDTO> extractGraphQLOperationList(String schema) {

        List<APIOperationsDTO> operationArray = new ArrayList<>();
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
        Map<java.lang.String, TypeDefinition> operationList = typeRegistry.types();
        for (Map.Entry<String, TypeDefinition> entry : operationList.entrySet()) {
            if (entry.getValue().getName().equals(APIConstants.GRAPHQL_QUERY) || entry.getValue().getName()
                    .equals(APIConstants.GRAPHQL_MUTATION) || entry.getValue().getName()
                    .equals(APIConstants.GRAPHQL_SUBSCRIPTION)) {
                for (FieldDefinition fieldDef : ((ObjectTypeDefinition) entry.getValue()).getFieldDefinitions()) {
                    APIOperationsDTO operation = new APIOperationsDTO();
                    operation.setVerb(entry.getKey());
                    operation.setTarget(fieldDef.getName());
                    operationArray.add(operation);
                }
            }
        }
        return operationArray;
    }

    /**
     * Validate GraphQL Schema.
     *
     * @param filename file name of the schema
     * @param schema   GraphQL schema
     */
    public static GraphQLValidationResponseDTO validateGraphQLSchema(String filename, String schema)
            throws APIManagementException {

        String errorMessage;
        GraphQLValidationResponseDTO validationResponse = new GraphQLValidationResponseDTO();
        boolean isValid = false;
        try {
            if (filename.endsWith(".graphql") || filename.endsWith(".txt") || filename.endsWith(".sdl")) {
                if (schema.isEmpty()) {
                    throw new APIManagementException("GraphQL Schema cannot be empty or null to validate it",
                            ExceptionCodes.GRAPHQL_SCHEMA_CANNOT_BE_NULL);
                }
                SchemaParser schemaParser = new SchemaParser();
                TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
                GraphQLSchema graphQLSchema = UnExecutableSchemaGenerator.makeUnExecutableSchema(typeRegistry);
                SchemaValidator schemaValidation = new SchemaValidator();
                Set<SchemaValidationError> validationErrors = schemaValidation.validateSchema(graphQLSchema);

                if (validationErrors.toArray().length > 0) {
                    errorMessage = "InValid Schema";
                    validationResponse.isValid(Boolean.FALSE);
                    validationResponse.errorMessage(errorMessage);
                } else {
                    validationResponse.setIsValid(Boolean.TRUE);
                    GraphQLValidationResponseGraphQLInfoDTO graphQLInfo = new GraphQLValidationResponseGraphQLInfoDTO();
                    GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
                    List<URITemplate> operationList = graphql.extractGraphQLOperationList(schema, null);
                    List<APIOperationsDTO> operationArray = APIMappingUtil
                            .fromURITemplateListToOprationList(operationList);
                    graphQLInfo.setOperations(operationArray);
                    GraphQLSchemaDTO schemaObj = new GraphQLSchemaDTO();
                    schemaObj.setSchemaDefinition(schema);
                    graphQLInfo.setGraphQLSchema(schemaObj);
                    validationResponse.setGraphQLInfo(graphQLInfo);
                }
            } else {
                throw new APIManagementException("Unsupported extension type of file: " + filename,
                        ExceptionCodes.UNSUPPORTED_GRAPHQL_FILE_EXTENSION);
            }
            isValid = validationResponse.isIsValid();
            errorMessage = validationResponse.getErrorMessage();
        } catch (SchemaProblem e) {
            errorMessage = e.getMessage();
        }

        if (!isValid) {
            validationResponse.setIsValid(isValid);
            validationResponse.setErrorMessage(errorMessage);
        }
        return validationResponse;
    }

    /**
     * Add document DTO.
     *
     * @param documentDto Document DTO
     * @param apiId       API UUID
     * @return Added documentation
     * @throws APIManagementException If an error occurs when retrieving API Identifier,
     *                                when checking whether the documentation exists and when adding the documentation
     */
    public static Documentation addDocumentationToAPI(DocumentDTO documentDto, String apiId)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Documentation documentation = DocumentationMappingUtil.fromDTOtoDocumentation(documentDto);
        String documentName = documentDto.getName();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (documentDto.getType() == null) {
            throw new APIManagementException("Documentation type cannot be empty",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        if (documentDto.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils
                .isBlank(documentDto.getOtherTypeName())) {
            //check otherTypeName for not null if doc type is OTHER
            throw new APIManagementException("otherTypeName cannot be empty if type is OTHER.",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        String sourceUrl = documentDto.getSourceUrl();
        if (documentDto.getSourceType() == DocumentDTO.SourceTypeEnum.URL && (
                org.apache.commons.lang3.StringUtils.isBlank(sourceUrl) || !RestApiCommonUtil.isURL(sourceUrl))) {
            throw new APIManagementException("Invalid document sourceUrl Format",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        if (apiProvider.isDocumentationExist(apiId, documentName, tenantDomain)) {
            throw new APIManagementException("Requested document '" + documentName + "' already exists",
                    ExceptionCodes.DOCUMENT_ALREADY_EXISTS);
        }
        documentation = apiProvider.addDocumentation(apiId, documentation, tenantDomain);

        return documentation;
    }

    /**
     * Checks whether the list of tiers are valid given the all valid tiers.
     *
     * @param allTiers     All defined tiers
     * @param currentTiers tiers to check if they are a subset of defined tiers
     * @return null if there are no invalid tiers or returns the set of invalid tiers if there are any
     */
    public static List<String> getInvalidTierNames(Set<Tier> allTiers, List<String> currentTiers) {

        List<String> invalidTiers = new ArrayList<>();
        for (String tierName : currentTiers) {
            boolean isTierValid = false;
            for (Tier definedTier : allTiers) {
                if (tierName.equals(definedTier.getName())) {
                    isTierValid = true;
                    break;
                }
            }
            if (!isTierValid) {
                invalidTiers.add(tierName);
            }
        }
        return invalidTiers;
    }

    /**
     * Update an API Product.
     *
     * @param originalAPIProduct    Existing API Product
     * @param apiProductDtoToUpdate New API Product DTO to update
     * @param apiProvider           API Provider
     * @param username              Username
     * @throws APIManagementException If an error occurs while retrieving and updating an existing API Product
     * @throws FaultGatewaysException If an error occurs while updating an existing API Product
     */
    public static APIProduct updateApiProduct(APIProduct originalAPIProduct, APIProductDTO apiProductDtoToUpdate,
                                              APIProvider apiProvider, String username, String orgId)
            throws APIManagementException, FaultGatewaysException {

        List<String> apiSecurity = apiProductDtoToUpdate.getSecurityScheme();
        //validation for tiers
        List<String> tiersFromDTO = apiProductDtoToUpdate.getPolicies();
        if (apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2) || apiSecurity
                .contains(APIConstants.API_SECURITY_API_KEY)) {
            if (tiersFromDTO == null || tiersFromDTO.isEmpty()) {
                throw new APIManagementException("No tier defined for the API Product",
                        ExceptionCodes.TIER_CANNOT_BE_NULL);
            }
        }

        //check whether the added API Products's tiers are all valid
        Set<Tier> definedTiers = apiProvider.getTiers();
        List<String> invalidTiers = PublisherCommonUtils.getInvalidTierNames(definedTiers, tiersFromDTO);
        if (!invalidTiers.isEmpty()) {
            throw new APIManagementException(
                    "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid",
                    ExceptionCodes.TIER_NAME_INVALID);
        }
        if (apiProductDtoToUpdate.getAdditionalProperties() != null) {
            String errorMessage = PublisherCommonUtils
                    .validateAdditionalProperties(apiProductDtoToUpdate.getAdditionalProperties());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage, ExceptionCodes
                        .from(ExceptionCodes.INVALID_ADDITIONAL_PROPERTIES, originalAPIProduct.getId().getName(),
                                originalAPIProduct.getId().getVersion()));
            }
        }

        //only publish api product if tiers are defined
        if (APIProductDTO.StateEnum.PUBLISHED.equals(apiProductDtoToUpdate.getState())) {
            //if the already created API product does not have tiers defined and the update request also doesn't
            //have tiers defined, then the product should not moved to PUBLISHED state.
            if (originalAPIProduct.getAvailableTiers() == null && apiProductDtoToUpdate.getPolicies() == null) {
                throw new APIManagementException("Policy needs to be defined before publishing the API Product",
                        ExceptionCodes.THROTTLING_POLICY_CANNOT_BE_NULL);
            }
        }

        APIProduct product = APIMappingUtil.fromDTOtoAPIProduct(apiProductDtoToUpdate, username);
        //We do not allow to modify provider,name,version  and uuid. Set the origial value
        APIProductIdentifier productIdentifier = originalAPIProduct.getId();
        product.setID(productIdentifier);
        product.setUuid(originalAPIProduct.getUuid());

        Map<API, List<APIProductResource>> apiToProductResourceMapping = apiProvider.updateAPIProduct(product);
        apiProvider.updateAPIProductSwagger(originalAPIProduct.getUuid(), apiToProductResourceMapping, product, orgId);

        //preserve monetization status in the update flow
        apiProvider.configureMonetizationInAPIProductArtifact(product);
        return apiProvider.getAPIProduct(productIdentifier);
    }

    /**
     * Add API Product with the generated swagger from the DTO.
     *
     * @param apiProductDTO API Product DTO
     * @param username      Username
     * @return Created API Product object
     * @throws APIManagementException Error while creating the API Product
     * @throws FaultGatewaysException Error while adding the API Product to gateway
     */
    public static APIProduct addAPIProductWithGeneratedSwaggerDefinition(APIProductDTO apiProductDTO, String username)
            throws APIManagementException, FaultGatewaysException {

        username = StringUtils.isEmpty(username) ? RestApiCommonUtil.getLoggedInUsername() : username;
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        // if not add product
        String provider = apiProductDTO.getProvider();
        String context = apiProductDTO.getContext();
        if (!StringUtils.isBlank(provider) && !provider.equals(username)) {
            if (!APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN)) {
                if (log.isDebugEnabled()) {
                    log.debug("User " + username + " does not have admin permission ("
                            + APIConstants.Permissions.APIM_ADMIN + ") hence provider (" + provider
                            + ") overridden with current user (" + username + ")");
                }
                provider = username;
            }
        } else {
            // Set username in case provider is null or empty
            provider = username;
        }

        List<String> tiersFromDTO = apiProductDTO.getPolicies();
        Set<Tier> definedTiers = apiProvider.getTiers();
        List<String> invalidTiers = PublisherCommonUtils.getInvalidTierNames(definedTiers, tiersFromDTO);
        if (!invalidTiers.isEmpty()) {
            throw new APIManagementException(
                    "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid",
                    ExceptionCodes.TIER_NAME_INVALID);
        }
        if (apiProductDTO.getAdditionalProperties() != null) {
            String errorMessage = PublisherCommonUtils
                    .validateAdditionalProperties(apiProductDTO.getAdditionalProperties());
            if (!errorMessage.isEmpty()) {
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INVALID_ADDITIONAL_PROPERTIES, apiProductDTO.getName()));
            }
        }
        if (apiProductDTO.getVisibility() == null) {
            //set the default visibility to PUBLIC
            apiProductDTO.setVisibility(APIProductDTO.VisibilityEnum.PUBLIC);
        }

        if (apiProductDTO.getAuthorizationHeader() == null) {
            apiProductDTO.setAuthorizationHeader(
                    APIUtil.getOAuthConfigurationFromAPIMConfig(APIConstants.AUTHORIZATION_HEADER));
        }
        if (apiProductDTO.getAuthorizationHeader() == null) {
            apiProductDTO.setAuthorizationHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT);
        }

        //Remove the /{version} from the context.
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        //Make sure context starts with "/". ex: /pizzaProduct
        context = context.startsWith("/") ? context : ("/" + context);
        //Check whether the context already exists
        if (apiProvider.isContextExist(context)) {
            throw new APIManagementException(
                    "Error occurred while adding API Product. API Product with the context " + context + " already " +
                            "exists.", ExceptionCodes.from(ExceptionCodes.API_PRODUCT_CONTEXT_ALREADY_EXISTS, context));
        }

        APIProduct productToBeAdded = APIMappingUtil.fromDTOtoAPIProduct(apiProductDTO, provider);

        APIProductIdentifier createdAPIProductIdentifier = productToBeAdded.getId();
        Map<API, List<APIProductResource>> apiToProductResourceMapping = apiProvider
                .addAPIProductWithoutPublishingToGateway(productToBeAdded);
        APIProduct createdProduct = apiProvider.getAPIProduct(createdAPIProductIdentifier);
        apiProvider.addAPIProductSwagger(createdProduct.getUuid(), apiToProductResourceMapping, createdProduct,
                tenantDomain);

        createdProduct = apiProvider.getAPIProduct(createdAPIProductIdentifier);
        return createdProduct;
    }

    public static boolean isStreamingAPI(APIDTO apidto) {

        return APIDTO.TypeEnum.WS.equals(apidto.getType()) || APIDTO.TypeEnum.SSE.equals(apidto.getType()) ||
                APIDTO.TypeEnum.WEBSUB.equals(apidto.getType());
    }
}
