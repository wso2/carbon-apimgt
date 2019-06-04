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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ApplicationKeyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;

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
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    ApplicationDTO applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
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
                RestApiUtil
                        .handleResourceAlreadyExistsError("Keys already generated for the application " + applicationId,
                                e,
                                log);
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
//                          verify that the provided validity period is a long
                            Long.parseLong(body.getValidityPeriod());
//                          verify that the provided jsonInput is a valid json
                            if (body.getAdditionalProperties() != null
                                    && !body.getAdditionalProperties().toString().isEmpty()) {
                                ObjectMapper mapper = new ObjectMapper();
                                jsonInput = mapper.writeValueAsString(body.getAdditionalProperties());
                                JSONParser parser = new JSONParser();
                                JSONObject json = (JSONObject) parser.parse(jsonInput);
                            }
                        } catch (NumberFormatException e) {
                            RestApiUtil.handleBadRequest("Error while generating " + keyType + " token for " +
                                    "application " + applicationId + ". Invalid validity period \'"
                                    + body.getValidityPeriod() + "\' provided.", log);
                        } catch (JsonProcessingException | ParseException | ClassCastException e) {
                            RestApiUtil.handleBadRequest("Error while generating " + keyType + " token for " +
                                    "application " + applicationId + ". Invalid jsonInput \'"
                                    + body.getAdditionalProperties() + "\' provided.", log);
                        }

                        String[] scopes = body.getScopes().toArray(new String[0]);
                        AccessTokenInfo response = apiConsumer.renewAccessToken(body.getRevokeToken(),
                                appKey.getConsumerKey(), appKey.getConsumerSecret(),
                                body.getValidityPeriod(), scopes, jsonInput);

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
        if (applicationKeys!= null) {
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
                String clientId = body.getConsumerKey();
                String keyType = body.getKeyType().toString();
                String tokenType = APIConstants.DEFAULT_TOKEN_TYPE;
                jsonParamObj.put(APIConstants.SUBSCRIPTION_KEY_TYPE, body.getKeyType().toString());
                jsonParamObj.put(APIConstants.JSON_CLIENT_SECRET, body.getConsumerSecret());
                Map<String, Object> keyDetails = apiConsumer.mapExistingOAuthClient(jsonParamObj.toJSONString(),
                        username, clientId, application.getName(), keyType, tokenType);
                ApplicationKeyDTO applicationKeyDTO =
                        ApplicationKeyMappingUtil.fromApplicationKeyToDTO(keyDetails, body.getKeyType().toString());
                return Response.ok().entity(applicationKeyDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APPLICATION, applicationId, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "is already registered")) {
                RestApiUtil.handleResourceAlreadyExistsError("Keys already generated for the application "
                        + applicationId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while generating keys for application "
                        + applicationId, e, log);
            }
        }
        return null;
    }

    @Override
    public Response applicationsApplicationIdScopesGet(String applicationId, Boolean filterByUserRoles,
            String ifNoneMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }
}
