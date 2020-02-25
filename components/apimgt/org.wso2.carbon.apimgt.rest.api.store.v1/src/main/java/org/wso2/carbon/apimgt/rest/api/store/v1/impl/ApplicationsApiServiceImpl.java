/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyRevokeRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ScopeInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ApplicationKeyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplicationsApiServiceImpl implements ApplicationsApiService {
    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

    /**
     * Retrieves all the applications that the user has access to
     *
     * @param groupId     group Id
     * @param query       search condition
     * @param limit       max number of objects returns
     * @param offset      starting index
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted applications
     */
    @Override
    public Response applicationsGet(String groupId, String query, String sortBy, String sortOrder,
            Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext) {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DEFAULT_SORT_ORDER;
        sortBy = sortBy != null ?
                ApplicationMappingUtil.getApplicationSortByField(sortBy) :
                RestApiConstants.SORT_BY_NAME;
        query = query == null ? "" : query;
        ApplicationListDTO applicationListDTO = new ApplicationListDTO();

        String username = RestApiUtil.getLoggedInUsername();
        
        // todo: Do a second level filtering for the incoming group ID.
        // todo: eg: use case is when there are lots of applications which is accessible to his group "g1", he wants to see
        // todo: what are the applications shared to group "g2" among them. 
        groupId = RestApiUtil.getLoggedInUserGroupId();
        try {
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
            Subscriber subscriber = new Subscriber(username);
            Application[] applications;
            applications = apiConsumer
                    .getApplicationsWithPagination(new Subscriber(username), groupId, offset, limit, query, sortBy,
                            sortOrder);
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            int applicationCount = apiMgtDAO.getAllApplicationCount(subscriber, groupId, query);

            applicationListDTO = ApplicationMappingUtil.fromApplicationsToDTO(applications);
            ApplicationMappingUtil.setPaginationParams(applicationListDTO, groupId, limit, offset, applicationCount);

            return Response.ok().entity(applicationListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "start index seems to be greater than the limit count")) {
                //this is not an error of the user as he does not know the total number of applications available.
                // Thus sends an empty response
                applicationListDTO.setCount(0);
                applicationListDTO.setPagination(new PaginationDTO());
                return Response.ok().entity(applicationListDTO).build();
            } else {
                String errorMessage = "Error while retrieving Applications";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Creates a new application
     *
     * @param body        request body containing application details
     * @return 201 response if successful
     */
    @Override
    public Response applicationsPost(ApplicationDTO body, MessageContext messageContext){
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //validate the tier specified for the application
            String tierName = body.getThrottlingPolicy();
            if (tierName == null) {
                RestApiUtil.handleBadRequest("Throttling tier cannot be null", log);
            }

            Map<String, Tier> appTierMap = APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, tenantDomain);
            if (appTierMap == null || RestApiUtil.findTier(appTierMap.values(), tierName) == null) {
                RestApiUtil.handleBadRequest("Specified tier " + tierName + " is invalid", log);
            }

            Object applicationAttributesFromUser = body.getAttributes();
            Map<String, String> applicationAttributes =
                    new ObjectMapper().convertValue(applicationAttributesFromUser, Map.class);
            if (applicationAttributes != null) {
                body.setAttributes(applicationAttributes);
            }

            //subscriber field of the body is not honored. It is taken from the context
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);

            int applicationId = apiConsumer.addApplication(application, username);

            //retrieves the created application and send as the response
            Application createdApplication = apiConsumer.getApplicationById(applicationId);
            ApplicationDTO createdApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(createdApplication);

            //to be set as the Location header
            URI location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
                    createdApplicationDTO.getApplicationId());
            return Response.created(location).entity(createdApplicationDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            if (RestApiUtil.isDueToResourceAlreadyExists(e)) {
                RestApiUtil.handleResourceAlreadyExistsError(
                        "An application already exists with name " + body.getName(), e,
                        log);
            } else if (RestApiUtil.isDueToApplicationNameWhiteSpaceValidation(e)) {
                RestApiUtil.handleBadRequest("Application name cannot contain leading or trailing white spaces", log);
            } else if (RestApiUtil.isDueToApplicationNameWithInvalidCharacters(e)) {
                RestApiUtil.handleBadRequest("Application name cannot contain invalid characters", log);
            } else {
                RestApiUtil.handleInternalServerError("Error while adding a new application for the user " + username,
                        e, log);
            }
        }
        return null;
    }

    /**
     * Get an application by Id
     *
     * @param applicationId   application identifier
     * @param ifNoneMatch     If-None-Match header value
     * @return response containing the required application object
     */
    @Override
    public Response applicationsApplicationIdGet(String applicationId, String ifNoneMatch, MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                // Remove hidden attributes and set the rest of the attributes from config
                JSONArray applicationAttributesFromConfig = apiConsumer.getAppAttributesFromConfig(username);
                Map<String, String> existingApplicationAttributes = application.getApplicationAttributes();
                Map<String, String> applicationAttributes = new HashMap<>();
                if (existingApplicationAttributes != null && applicationAttributesFromConfig != null) {
                    for (Object object : applicationAttributesFromConfig) {
                        JSONObject attribute = (JSONObject) object;
                        Boolean hidden = (Boolean) attribute.get(APIConstants.ApplicationAttributes.HIDDEN);
                        String attributeName = (String) attribute.get(APIConstants.ApplicationAttributes.ATTRIBUTE);

                        if (!BooleanUtils.isTrue(hidden)) {
                            String attributeVal = existingApplicationAttributes.get(attributeName);
                            if (attributeVal != null) {
                                applicationAttributes.put(attributeName, attributeVal);
                            } else {
                                applicationAttributes.put(attributeName, "");
                            }
                        }
                    }
                }
                application.setApplicationAttributes(applicationAttributes);
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    ApplicationDTO applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
                    applicationDTO.setHashEnabled(OAuthServerConfiguration.getInstance().isClientSecretHashEnabled());
                    Set<Scope> scopes = apiConsumer.getScopesForApplicationSubscription(username, application.getId());
                    List<ScopeInfoDTO> scopeInfoList = ApplicationMappingUtil.getScopeInfoDTO(scopes);
                    applicationDTO.setSubscriptionScopes(scopeInfoList);
                    return Response.ok().entity(applicationDTO).build();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving application " + applicationId, e, log);
        }
        return null;
    }

    /**
     * Update an application by Id
     *
     * @param applicationId     application identifier
     * @param body              request body containing application details
     * @param ifMatch           If-Match header value
     * @return response containing the updated application object
     */
    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String ifMatch, MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application oldApplication = apiConsumer.getApplicationByUUID(applicationId);
            
            if (oldApplication == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
            
            if (!RestAPIStoreUtils.isUserOwnerOfApplication(oldApplication)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }

            Object applicationAttributesFromUser = body.getAttributes();
            Map<String, String> applicationAttributes = new ObjectMapper()
                    .convertValue(applicationAttributesFromUser, Map.class);

            if (applicationAttributes != null) {
                body.setAttributes(applicationAttributes);
            }
            
            //we do not honor the subscriber coming from the request body as we can't change the subscriber of the application
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);

            //we do not honor the application id which is sent via the request body
            application.setUUID(oldApplication != null ? oldApplication.getUUID() : null);

            apiConsumer.updateApplication(application);

            //retrieves the updated application and send as the response
            Application updatedApplication = apiConsumer.getApplicationByUUID(applicationId);
            ApplicationDTO updatedApplicationDTO = ApplicationMappingUtil
                    .fromApplicationtoDTO(updatedApplication);
            return Response.ok().entity(updatedApplicationDTO).build();
                
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToApplicationNameWhiteSpaceValidation(e)) {
                RestApiUtil.handleBadRequest("Application name cannot contains leading or trailing white spaces", log);
            } else if (RestApiUtil.isDueToApplicationNameWithInvalidCharacters(e)) {
                RestApiUtil.handleBadRequest("Application name cannot contain invalid characters", log);
            } else {
                RestApiUtil.handleInternalServerError("Error while updating application " + applicationId, e, log);
            }
        }
        return null;
    }

    @Override
    public Response applicationsApplicationIdApiKeysKeyTypeGeneratePost(
            String applicationId, String keyType, APIKeyGenerateRequestDTO body, String ifMatch, MessageContext messageContext) {

        String userName = RestApiUtil.getLoggedInUsername();
        Application application;
        int validityPeriod;
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(userName);
            if ((application = apiConsumer.getApplicationByUUID(applicationId)) == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            } else {
                if (!RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                } else {
                    if (APIConstants.API_KEY_TYPE_PRODUCTION.equalsIgnoreCase(keyType)) {
                        application.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
                    } else if (APIConstants.API_KEY_TYPE_SANDBOX.equalsIgnoreCase(keyType)) {
                        application.setKeyType(APIConstants.API_KEY_TYPE_SANDBOX);
                    } else {
                        RestApiUtil.handleBadRequest("Invalid keyType. KeyType should be either PRODUCTION or SANDBOX", log);
                    }
                    if (body != null && body.getValidityPeriod() != null && body.getValidityPeriod() > 0) {
                        validityPeriod = body.getValidityPeriod();
                    } else {
                        validityPeriod = -1;
                    }
                    String apiKey = apiConsumer.generateApiKey(application, userName, (long) validityPeriod);
                    APIKeyDTO apiKeyDto = ApplicationKeyMappingUtil.formApiKeyToDTO(apiKey, validityPeriod);
                    return Response.ok().entity(apiKeyDto).build();
                }
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while generatig API Keys for application " + applicationId, e, log);
        }
        return null;
    }

    @Override
    public Response applicationsApplicationIdApiKeysKeyTypeRevokePost(String applicationId, String keyType,
                                                                      APIKeyRevokeRequestDTO body, String ifMatch,
                                                                      MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        String apiKey = body.getApikey();
        if (!StringUtils.isEmpty(apiKey) && APIUtil.isValidJWT(apiKey)) {
            try {
                String splitToken[] = apiKey.split("\\.");
                String signatureAlgorithm = APIUtil.getSignatureAlgorithm(splitToken);
                String certAlias = APIUtil.getSigningAlias(splitToken);
                Certificate certificate = APIUtil.getCertificateFromTrustStore(certAlias);
                if(APIUtil.verifyTokenSignature(splitToken, certificate, signatureAlgorithm)) {
                    APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
                    Application application = apiConsumer.getApplicationByUUID(applicationId);
                    org.json.JSONObject decodedBody = new org.json.JSONObject(
                                        new String(Base64.getUrlDecoder().decode(splitToken[1])));
                    org.json.JSONObject appInfo = decodedBody.getJSONObject(APIConstants.JwtTokenConstants.APPLICATION);
                    if (appInfo != null && application != null) {
                        if (RestAPIStoreUtils.isUserOwnerOfApplication(application)) {
                            String appUuid = appInfo.getString(APIConstants.JwtTokenConstants.APPLICATION_UUID);
                            if (applicationId.equals(appUuid)) {
                                long expiryTime = Long.MAX_VALUE;
                                org.json.JSONObject payload = new org.json.JSONObject(
                                        new String(Base64.getUrlDecoder().decode(splitToken[1])));
                                if (payload.has(APIConstants.JwtTokenConstants.EXPIRY_TIME)) {
                                    expiryTime = APIUtil.getExpiryifJWT(apiKey);
                                }
                                String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
                                apiConsumer.revokeAPIKey(apiKey, expiryTime, tenantDomain);
                                return Response.ok().build();
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Application uuid " + applicationId + " isn't matched with the " +
                                            "application in the token " + appUuid + " of API Key " +
                                                                                    APIUtil.getMaskedToken(apiKey));
                                }
                                RestApiUtil.handleBadRequest("Validation failed for the given token ", log);
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Logged in user " + username + " isn't the owner of the application "
                                                                                                        + applicationId);
                            }
                            RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION,
                                                                                                      applicationId, log);
                        }
                    } else {
                        if(log.isDebugEnabled()) {
                            if (application == null) {
                                log.debug("Application with given id " + applicationId + " doesn't not exist ");
                            }

                            if (appInfo == null) {
                                log.debug("Application information doesn't exist in the token "
                                                                                    + APIUtil.getMaskedToken(apiKey));
                            }
                        }
                        RestApiUtil.handleBadRequest("Validation failed for the given token ", log);
                    }
                } else {
                    if(log.isDebugEnabled()) {
                        log.debug("Signature verification of given token " + APIUtil.getMaskedToken(apiKey) +
                                                                                                            " is failed");
                    }
                    RestApiUtil.handleInternalServerError("Validation failed for the given token", log);
                }
            } catch (APIManagementException e) {
                String msg = "Error while revoking API Key of application " + applicationId;
                if(log.isDebugEnabled()) {
                    log.debug("Error while revoking API Key of application " +
                            applicationId+ " and token " + APIUtil.getMaskedToken(apiKey));
                }
                log.error(msg, e);
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        } else {
            log.debug("Provided API Key " + APIUtil.getMaskedToken(apiKey) + " is not valid");
            RestApiUtil.handleBadRequest("Provided API Key isn't valid ", log);
        }
        return null;
    }

    /**
     * Deletes an application by id
     *
     * @param applicationId     application identifier
     * @param ifMatch           If-Match header value
     * @return 200 Response if successfully deleted the application
     */
    @Override
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch, MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserOwnerOfApplication(application)) {
                    apiConsumer.removeApplication(application, username);
                    return Response.ok().build();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while deleting application " + applicationId, e, log);
        }
        return null;
    }

    /**
     * Generate keys for a application
     *
     * @param applicationId     application identifier
     * @param body              request body
     * @return A response object containing application keys
     */
    @Override
    public Response applicationsApplicationIdGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequestDTO
            body, MessageContext messageContext) {

        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserOwnerOfApplication(application)) {
                    String[] accessAllowDomainsArray = {"ALL"};
                    JSONObject jsonParamObj = new JSONObject();
                    jsonParamObj.put(ApplicationConstants.OAUTH_CLIENT_USERNAME, username);
                    String grantTypes = StringUtils.join(body.getGrantTypesToBeSupported(), ',');
                    if (!StringUtils.isEmpty(grantTypes)) {
                        jsonParamObj.put(APIConstants.JSON_GRANT_TYPES, grantTypes);
                    }
                    /* Read clientId & clientSecret from ApplicationKeyGenerateRequestDTO object.
                       User can provide clientId only or both clientId and clientSecret
                       User cannot provide clientSecret only */
                    if (!StringUtils.isEmpty(body.getClientId())) {
                        jsonParamObj.put(APIConstants.JSON_CLIENT_ID, body.getClientId());
                        if (!StringUtils.isEmpty(body.getClientSecret())) {
                            jsonParamObj.put(APIConstants.JSON_CLIENT_SECRET, body.getClientSecret());
                        }
                    }
                    
                    if (!StringUtils.isEmpty(body.getAdditionalProperties())) {
                        jsonParamObj.put(APIConstants.JSON_ADDITIONAL_PROPERTIES, body.getAdditionalProperties());
                    }
                    String jsonParams = jsonParamObj.toString();
                    String tokenScopes = StringUtils.join(body.getScopes(), " ");

                    Map<String, Object> keyDetails = apiConsumer.requestApprovalForApplicationRegistration(
                            username, application.getName(), body.getKeyType().toString(), body.getCallbackUrl(),
                            accessAllowDomainsArray, body.getValidityTime(), tokenScopes, application.getGroupId(),
                            jsonParams);
                    ApplicationKeyDTO applicationKeyDTO =
                            ApplicationKeyMappingUtil.fromApplicationKeyToDTO(keyDetails, body.getKeyType().toString());

                    return Response.ok().entity(applicationKeyDTO).build();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "is already registered")) {
                RestApiUtil.handleResourceAlreadyExistsError("Keys already generated for the application " +
                        applicationId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while generating keys for application " + applicationId, e,
                        log);
            }
        }
        return null;
    }

    /**
     * Retrieve all keys of an application
     *
     * @param applicationId Application Id
     * @return Application Key Information list
     */
    @Override
    public Response applicationsApplicationIdKeysGet(String applicationId, MessageContext messageContext) {

        List<APIKey> applicationKeys = getApplicationKeys(applicationId);
        List<ApplicationKeyDTO> keyDTOList = new ArrayList<>();
        ApplicationKeyListDTO applicationKeyListDTO = new ApplicationKeyListDTO();
        applicationKeyListDTO.setCount(0);

        if (applicationKeys != null) {
            for (APIKey apiKey : applicationKeys) {
                ApplicationKeyDTO appKeyDTO = ApplicationKeyMappingUtil.fromApplicationKeyToDTO(apiKey);
                keyDTOList.add(appKeyDTO);
            }
            applicationKeyListDTO.setList(keyDTOList);
            applicationKeyListDTO.setCount(keyDTOList.size());
        }
        return Response.ok().entity(applicationKeyListDTO).build();
    }

    /**
     * Clean up application keys
     * @param applicationId Application Id
     * @param keyType Key Type whether PRODUCTION or SANDBOX
     * @param ifMatch
     * @param messageContext
     * @return
     */
    @Override
    public Response applicationsApplicationIdKeysKeyTypeCleanUpPost(String applicationId, String keyType, String ifMatch,
                             MessageContext messageContext) {

        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getLightweightApplicationByUUID(applicationId);
            apiConsumer.cleanUpApplicationRegistrationByApplicationId(Integer.toString(application.getId()), keyType);
            return Response.ok().build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while application key cleanup process", e, log);
        }
        return null;
    }

    /**
     * Used to get all keys of an application
     *
     * @param applicationId Id of the application
     * @return List of application keys
     */
    private List<APIKey> getApplicationKeys(String applicationId) {

        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    return application.getKeys();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving application " + applicationId, e, log);
        }
        return null;
    }

    @Override
    public Response applicationsApplicationIdKeysKeyTypeGenerateTokenPost(String applicationId,
            String keyType, ApplicationTokenGenerateRequestDTO body, String ifMatch, MessageContext messageContext) {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);

            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    ApplicationKeyDTO appKey = getApplicationKeyByAppIDAndKeyType(applicationId, keyType);
                    if (appKey != null) {
                        String jsonInput = null;
                        try {
                            // verify that the provided jsonInput is a valid json
                            if (body.getAdditionalProperties() != null
                                    && !body.getAdditionalProperties().toString().isEmpty()) {
                                ObjectMapper mapper = new ObjectMapper();
                                jsonInput = mapper.writeValueAsString(body.getAdditionalProperties());
                                JSONParser parser = new JSONParser();
                                JSONObject json = (JSONObject) parser.parse(jsonInput);
                            }
                        } catch (JsonProcessingException | ParseException | ClassCastException e) {
                            RestApiUtil.handleBadRequest("Error while generating " + keyType + " token for " +
                                    "application " + applicationId + ". Invalid jsonInput \'"
                                    + body.getAdditionalProperties() + "\' provided.", log);
                        }

                        String[] scopes = body.getScopes().toArray(new String[0]);
                        AccessTokenInfo response = apiConsumer.renewAccessToken(body.getRevokeToken(),
                                appKey.getConsumerKey(), appKey.getConsumerSecret(),
                                body.getValidityPeriod().toString(), scopes, jsonInput);

                        ApplicationTokenDTO appToken = new ApplicationTokenDTO();
                        appToken.setAccessToken(response.getAccessToken());
                        appToken.setTokenScopes(Arrays.asList(response.getScopes()));
                        appToken.setValidityTime(response.getValidityPeriod());
                        return Response.ok().entity(appToken).build();
                    } else {
                        RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APP_CONSUMER_KEY,
                                keyType, log);
                    }
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while generating " + keyType + " token for application "
                    + applicationId, e, log);
        }
        return null;
    }

    /**
     * Retrieve Keys of an application by key type
     *
     * @param applicationId Application Id
     * @param keyType       Key Type (Production | Sandbox)
     * @param groupId       Group id of application (if any)
     * @return Application Key Information
     */
    @Override
    public Response applicationsApplicationIdKeysKeyTypeGet(String applicationId, String keyType,
            String groupId, MessageContext messageContext) {
        return Response.ok().entity(getApplicationKeyByAppIDAndKeyType(applicationId, keyType)).build();
    }

    /**
     * Returns Keys of an application by key type
     *
     * @param applicationId Application Id
     * @param keyType       Key Type (Production | Sandbox)
     * @return Application Key Information
     */
    private ApplicationKeyDTO getApplicationKeyByAppIDAndKeyType(String applicationId, String keyType) {
        List<APIKey> applicationKeys = getApplicationKeys(applicationId);
        if (applicationKeys != null) {
            for (APIKey apiKey : applicationKeys) {
                if (keyType != null && keyType.equals(apiKey.getType())) {
                    return ApplicationKeyMappingUtil.fromApplicationKeyToDTO(apiKey);
                }
            }
        }
        return null;
    }

    /**
     * Update grant types/callback URL
     *
     * @param applicationId Application Id
     * @param keyType       Key Type (Production | Sandbox)
     * @param body          Grant type and callback URL information
     * @return Updated Key Information
     */
    @Override
    public Response applicationsApplicationIdKeysKeyTypePut(String applicationId, String keyType,
            ApplicationKeyDTO body, MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserOwnerOfApplication(application)) {
                    String grantTypes = StringUtils.join(body.getSupportedGrantTypes(), ',');
                    JsonObject jsonParams = new JsonObject();
                    jsonParams.addProperty(APIConstants.JSON_GRANT_TYPES, grantTypes);
                    jsonParams.addProperty(APIConstants.JSON_USERNAME, username);
                    jsonParams.addProperty(APIConstants.JSON_ADDITIONAL_PROPERTIES, body.getAdditionalProperties());
                    OAuthApplicationInfo updatedData = apiConsumer.updateAuthClient(username, application.getName(),
                            keyType, body.getCallbackUrl(), null, null, null, body.getGroupId(),
                            new Gson().toJson(jsonParams));
                    ApplicationKeyDTO applicationKeyDTO = new ApplicationKeyDTO();
                    applicationKeyDTO.setCallbackUrl(updatedData.getCallBackURL());
                    JsonObject json = new Gson().fromJson(updatedData.getJsonString(), JsonObject.class);
                    if (json.get(APIConstants.JSON_GRANT_TYPES) != null) {
                        String[] updatedGrantTypes = json.get(APIConstants.JSON_GRANT_TYPES).getAsString().split(" ");
                        applicationKeyDTO.setSupportedGrantTypes(Arrays.asList(updatedGrantTypes));
                    }
                    applicationKeyDTO.setConsumerKey(updatedData.getClientId());
                    applicationKeyDTO.setConsumerSecret(updatedData.getClientSecret());
                    applicationKeyDTO.setKeyType(ApplicationKeyDTO.KeyTypeEnum.valueOf(keyType));
                    Object additionalProperties = updatedData.getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES);
                    if (additionalProperties != null) {
                        applicationKeyDTO.setAdditionalProperties((String) additionalProperties);
                    }
                    return Response.ok().entity(applicationKeyDTO).build();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while updating application " + applicationId, e, log);
        }
        return null;
    }

    /**
     * Re generate consumer secret.
     *
     * @param applicationId Application Id
     * @param keyType       Key Type (Production | Sandbox)
     * @return A response object containing application keys.
     */
    @Override
    public Response applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(String applicationId,
            String keyType, MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            List<APIKey> applicationKeys = getApplicationKeys(applicationId);
            for (APIKey apiKey : applicationKeys) {
                if (keyType != null && keyType.equals(apiKey.getType())) {
                    APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
                    String clientId = apiKey.getConsumerKey();
                    String clientSecret = apiConsumer.renewConsumerSecret(clientId);

                    ApplicationKeyDTO applicationKeyDTO = new ApplicationKeyDTO();
                    applicationKeyDTO.setConsumerKey(clientId);
                    applicationKeyDTO.setConsumerSecret(clientSecret);

                    return Response.ok().entity(applicationKeyDTO).build();
                }
            }

        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while re generating the consumer secret ", e, log);
        }
        return null;
    }

    /**
     * Generate keys using existing consumer key and consumer secret
     *
     * @param applicationId Application id
     * @param body          Contains consumer key, secret and key type information
     * @return A response object containing application keys
     */
    @Override
    public Response applicationsApplicationIdMapKeysPost(String applicationId, ApplicationKeyMappingRequestDTO body,
                                                         MessageContext messageContext) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            JSONObject jsonParamObj = new JSONObject();
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserOwnerOfApplication(application)) {
                    String clientId = body.getConsumerKey();
                    String keyType = body.getKeyType().toString();
                    String tokenType = APIConstants.DEFAULT_TOKEN_TYPE;
                    jsonParamObj.put(APIConstants.SUBSCRIPTION_KEY_TYPE, body.getKeyType().toString());
                    jsonParamObj.put(APIConstants.JSON_CLIENT_SECRET, body.getConsumerSecret());
                    Map<String, Object> keyDetails = apiConsumer
                            .mapExistingOAuthClient(jsonParamObj.toJSONString(), username, clientId,
                                    application.getName(), keyType, tokenType);
                    ApplicationKeyDTO applicationKeyDTO = ApplicationKeyMappingUtil
                            .fromApplicationKeyToDTO(keyDetails, body.getKeyType().toString());
                    return Response.ok().entity(applicationKeyDTO).build();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "is already registered")) {
                RestApiUtil.handleResourceAlreadyExistsError("Keys already generated for the application "
                        + applicationId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while providing keys for application "
                        + applicationId, e, log);
            }
        }
        return null;
    }
}
