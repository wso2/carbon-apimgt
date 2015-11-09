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

package org.wso2.carbon.apimgt.rest.api.impl;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.utils.mappings.ApplicationKeyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.utils.mappings.ApplicationMappingUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class ApplicationsApiServiceImpl extends ApplicationsApiService {
    @Override
    public Response applicationsGet(String subscriber, String groupId, String limit, String offset, String accept,
            String ifNoneMatch) {
        String username = RestApiUtil.getLoggedInUsername();

        //pre-processing
        if (groupId == null) {
            groupId = "";
        }
        if (subscriber == null) {
            subscriber = username;
        }
        ApplicationListDTO applicationListDTO;
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application[] applications = apiConsumer.getApplications(new Subscriber(subscriber), groupId);
            applicationListDTO = ApplicationMappingUtil.fromApplicationsToDTO(applications);
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
            Application application = ApplicationMappingUtil.fromDTOtoApplication(body);
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
    public Response applicationsApplicationIdGet(String applicationId, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            ApplicationDTO applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
            return Response.ok().entity(applicationDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String contentType, 
            String ifMatch, String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        Application application = ApplicationMappingUtil.fromDTOtoApplication(body);
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            apiConsumer.updateApplication(application);

            //retrieves the updated application and send as the response
            Application updatedApplication = apiConsumer.getApplicationByUUID(applicationId);
            ApplicationDTO updatedApplicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(updatedApplication);
            return Response.ok().entity(updatedApplicationDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch,
            String ifUnmodifiedSince) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = new Application(applicationId);
            apiConsumer.removeApplication(application);
            return Response.ok().build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response applicationsApplicationIdGenerateKeysPost(String applicationId,
            ApplicationKeyGenerateRequestDTO body, String contentType, String ifMatch, String ifUnmodifiedSince) {

        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
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

            //get the updated application again
            application = apiConsumer.getApplicationByUUID(applicationId);
            ApplicationDTO applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);

            boolean alreadyContainsKey = false;
            for (APIKey apiKey : application.getKeys()) {
                String keyType = apiKey.getType();
                if (keyType != null && keyType.equals(applicationKeyDTO.getKeyType().toString())) {
                    alreadyContainsKey = true;
                    break;
                }
            }
            if (!alreadyContainsKey) {
                applicationDTO.getKeys().add(applicationKeyDTO);
            }
            return Response.ok().entity(applicationDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }
}