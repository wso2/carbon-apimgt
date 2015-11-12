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
import org.wso2.carbon.apimgt.rest.api.util.exception.ForbiddenException;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.core.Response;

public class ApplicationsApiServiceImpl extends ApplicationsApiService {

    @Override
    public Response applicationsGet(String groupId, Integer limit, Integer offset, String accept,
            String ifNoneMatch) {
        String username = RestApiUtil.getLoggedInUsername();

        // currently groupId is taken from the user so that groupId coming as a query parameter is not honored.
        // As a improvement, we can check admin privileges of the user and honor groupId.
        groupId = RestAPIStoreUtils.getLoggedInUserGroupIds();

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
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsPost(ApplicationDTO body, String contentType) {
        String username = RestApiUtil.getLoggedInUsername();
        String subscriber = body.getSubscriber();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            //subscriber field of the body is not honored. It is taken from the context
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body, username);

            //setting the proper groupId. This is not honored for now.
            // Later we can honor it by checking admin privileges of the user.
            String groupId = RestAPIStoreUtils.getLoggedInUserGroupIds();
            application.setGroupId(groupId);
            int applicationId = apiConsumer.addApplication(application, subscriber);

            //retrieves the created application and send as the response
            Application createdApplication = apiConsumer.getApplicationById(applicationId);
            ApplicationDTO createdApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(createdApplication);

            //to be set as the Location header
            URI location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
                    createdApplicationDTO.getApplicationId());
            return Response.created(location).entity(createdApplicationDTO).build();
        } catch (APIManagementException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response applicationsGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequestDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince) {

        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (RestAPIStoreUtils.isUserAccessAllowedToApplication(application)) {
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
                throw new ForbiddenException(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT);
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdGet(String applicationId, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (RestAPIStoreUtils.isUserAccessAllowedToApplication(application)) {
                ApplicationDTO applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
                return Response.ok().entity(applicationDTO).build();
            } else {
                throw new ForbiddenException(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT);
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String contentType, 
            String ifMatch, String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application oldApplication = apiConsumer.getApplicationByUUID(applicationId);
            //we do not honor the subscriber coming from the request body as we can't change the subscriber of the application
            Application application = ApplicationMappingUtil
                    .fromDTOtoApplication(body, oldApplication.getSubscriber().getName());
            //groupId of the request body is not honored for now.
            // Later we can improve by checking admin privileges of the user.
            String groupId = RestAPIStoreUtils.getLoggedInUserGroupIds();
            application.setGroupId(groupId);
            //we do not honor the application id which is sent via the request body
            application.setUUID(applicationId);
            if (RestAPIStoreUtils.isUserAccessAllowedToApplication(application)) {
                apiConsumer.updateApplication(application);

                //retrieves the updated application and send as the response
                Application updatedApplication = apiConsumer.getApplicationByUUID(applicationId);
                ApplicationDTO updatedApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(updatedApplication);
                return Response.ok().entity(updatedApplicationDTO).build();
            } else {
                throw new ForbiddenException(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT);
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch,
            String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            if (RestAPIStoreUtils.isUserAccessAllowedToApplication(application)) {
                apiConsumer.removeApplication(application);
                return Response.ok().build();
            } else {
                throw new ForbiddenException(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT);
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

}