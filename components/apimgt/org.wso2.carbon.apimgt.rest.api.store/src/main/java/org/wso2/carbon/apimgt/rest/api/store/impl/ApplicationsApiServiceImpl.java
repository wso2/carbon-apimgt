/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.ApplicationKeyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for Store application related operations
 */
public class ApplicationsApiServiceImpl extends ApplicationsApiService {

    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

    /**
     * Retrieves all the applications that the user has access to
     * 
     * @param groupId group Id
     * @param limit max number of objects returns
     * @param offset starting index
     * @param accept accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted applications
     */
    @Override
    public Response applicationsGet(String groupId, Integer limit, Integer offset, String accept,
            String ifNoneMatch) {
        String username = RestApiUtil.getLoggedInUsername();

        // currently groupId is taken from the user so that groupId coming as a query parameter is not honored.
        // As a improvement, we can check admin privileges of the user and honor groupId.
        groupId = RestAPIStoreUtils.getLoggedInUserGroupId();

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        ApplicationListDTO applicationListDTO;
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application[] allMatchedApps = apiConsumer.getApplications(new Subscriber(username), groupId);
            //allMatchedApps are already sorted to application name
            applicationListDTO = ApplicationMappingUtil.fromApplicationsToDTO(allMatchedApps, limit, offset);
            ApplicationMappingUtil.setPaginationParams(applicationListDTO, groupId, limit, offset,
                    allMatchedApps.length);

            return Response.ok().entity(applicationListDTO).build();
        } catch (APIManagementException e) {
            handleException("Error while retrieving applications of the user " + username, e);
            return null;
        }
    }

    /**
     * Creates a new application
     * 
     * @param body request body containing application details
     * @param contentType Content-Type header value
     * @return 201 response if successful
     */
    @Override
    public Response applicationsPost(ApplicationDTO body, String contentType) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            //subscriber field of the body is not honored. It is taken from the context
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);

            //setting the proper groupId. This is not honored for now.
            // Later we can honor it by checking admin privileges of the user.
            String groupId = RestAPIStoreUtils.getLoggedInUserGroupId();
            application.setGroupId(groupId);
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
                throw RestApiUtil.buildConflictException("An application already exists with name " + body.getName());
            } else {
                handleException("Error while adding a new application for the user " + username, e);
                return null;
            }
        }
    }

    /**
     * Generate keys for a application
     * 
     * @param applicationId application identifier
     * @param body request body
     * @param contentType Content-Type header value
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return A response object containing application keys
     */
    @Override
    @SuppressWarnings("unchecked")
    public Response applicationsGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequestDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince) {

        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    String[] accessAllowDomainsArray = body.getAccessAllowDomains().toArray(new String[1]);
                    JSONObject jsonParamObj = new JSONObject();
                    jsonParamObj.put(ApplicationConstants.OAUTH_CLIENT_USERNAME, username);
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
                    throw RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_APPLICATION, applicationId);
                }
            } else {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_APPLICATION, applicationId);
            }
        } catch (APIManagementException e) {
            handleException("Error while generating keys for application " + applicationId, e);
            return null;
        }
    }

    /**
     * Get an application by Id
     * 
     * @param applicationId application identifier
     * @param accept accepted media type of the client
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return response containing the required application object
     */
    @Override
    public Response applicationsApplicationIdGet(String applicationId, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    ApplicationDTO applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
                    return Response.ok().entity(applicationDTO).build();
                } else {
                    throw RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_APPLICATION, applicationId);
                }
            } else {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_APPLICATION, applicationId);
            }
        } catch (APIManagementException e) {
            handleException("Error while retrieving application " + applicationId, e);
            return null;
        }
    }

    /**
     * Update an application by Id
     * 
     * @param applicationId application identifier
     * @param body request body containing application details
     * @param contentType Content-Type header value
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return response containing the updated application object
     */
    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String contentType,
            String ifMatch, String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application oldApplication = apiConsumer.getApplicationByUUID(applicationId);
            if (oldApplication != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(oldApplication)) {
                    //we do not honor the subscriber coming from the request body as we can't change the subscriber of the application
                    Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);
                    //groupId of the request body is not honored for now.
                    // Later we can improve by checking admin privileges of the user.
                    application.setGroupId(oldApplication.getGroupId());
                    //we do not honor the application id which is sent via the request body
                    application.setUUID(oldApplication.getUUID());

                    apiConsumer.updateApplication(application);

                    //retrieves the updated application and send as the response
                    Application updatedApplication = apiConsumer.getApplicationByUUID(applicationId);
                    ApplicationDTO updatedApplicationDTO = ApplicationMappingUtil
                            .fromApplicationtoDTO(updatedApplication);
                    return Response.ok().entity(updatedApplicationDTO).build();
                } else {
                    throw RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_APPLICATION, applicationId);
                }
            } else {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_APPLICATION, applicationId);
            }
        } catch (APIManagementException e) {
            handleException("Error while updating application " + applicationId, e);
            return null;
        }
    }

    /**
     * Deletes an application by id
     * 
     * @param applicationId application identifier
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 Response if successfully deleted the application 
     */ 
    @Override
    @SuppressWarnings("unchecked")
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch,
            String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (application != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForApplication(application)) {
                    apiConsumer.removeApplication(application);
                    return Response.ok().build();
                } else {
                    throw RestApiUtil.buildForbiddenException(RestApiConstants.RESOURCE_APPLICATION, applicationId);
                }
            } else {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_APPLICATION, applicationId);
            }
        } catch (APIManagementException e) {
            handleException("Error while deleting application " + applicationId, e);
            return null;
        }
    }

    private void handleException(String msg, Throwable t) throws InternalServerErrorException {
        log.error(msg, t);
        throw new InternalServerErrorException(t);
    }

}