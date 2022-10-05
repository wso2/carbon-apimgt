/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.impl.ApplicationServiceImpl;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyRevokeRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.models.ExportedApplication;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
            Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        ApplicationListDTO applicationListDTO;
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        applicationListDTO = ApplicationServiceImpl.applicationsGet(groupId, query, sortBy, sortOrder, limit,
                offset, organization);
        return Response.ok().entity(applicationListDTO).build();
    }

    /**
     * Import an Application which has been exported to a zip file
     *
     * @param fileInputStream     Content stream of the zip file which contains exported Application
     * @param fileDetail          Meta information of the zip file
     * @param preserveOwner       If true, preserve the original owner of the application
     * @param skipSubscriptions   If true, skip subscriptions of the application
     * @param appOwner            Target owner of the application
     * @param skipApplicationKeys Skip application keys while importing
     * @param update              Update if existing application found or import
     * @param messageContext      Message Context
     * @return imported Application
     */
    @Override
    public Response applicationsImportPost(InputStream fileInputStream, Attachment fileDetail, Boolean preserveOwner,
            Boolean skipSubscriptions, String appOwner, Boolean skipApplicationKeys, Boolean update,
            MessageContext messageContext) throws APIManagementException {

        try {
            ExportedApplication exportedApp = ApplicationServiceImpl.getExportedApplication(fileInputStream);

            // Retrieve the application DTO object from the aggregated exported application
            ApplicationDTO applicationDTO = exportedApp.getApplicationInfo();

            String ownerId = ApplicationServiceImpl.getOwnerId(applicationDTO.getGroups(), appOwner, preserveOwner,
                    applicationDTO.getOwner());

            String applicationGroupId = String.join(",", applicationDTO.getGroups());

            // Preprocess application
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            boolean isApplicationExist = APIUtil.isApplicationExist(ownerId, applicationDTO.getName(),
                    applicationGroupId, organization);
            if (!isApplicationExist || update == null) {
                update = Boolean.FALSE;
            }
            Application application = ApplicationServiceImpl.preProcessApplication(ownerId, applicationDTO,
                    organization, isApplicationExist);

            // Retrieve skippedAPIList
            APIInfoListDTO skippedAPIListDTO = ApplicationServiceImpl.getSkippedAPIs(
                    exportedApp.getSubscribedAPIs(), ownerId, update, skipSubscriptions, application, organization);

            // Import Application
            ApplicationInfoDTO importedApplicationDTO = ApplicationServiceImpl.applicationImport(
                    application.getId(), ownerId, applicationDTO, skipApplicationKeys, update);

            URI location = new URI(
                    RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" + importedApplicationDTO.getApplicationId());

            if (skippedAPIListDTO == null) {
                return Response.created(location).entity(importedApplicationDTO).build();
            } else {
                return Response.created(location).status(207).entity(skippedAPIListDTO).build();
            }
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while importing Application", e);
        }
    }

    /**
     * Creates a new application
     *
     * @param body        request body containing application details
     * @return 201 response if successful
     */
    @Override
    public Response applicationsPost(ApplicationDTO body, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ApplicationDTO createdApplicationDTO = ApplicationServiceImpl.addApplication(body, organization);

        try {
            URI location = new URI(
                    RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" + createdApplicationDTO.getApplicationId());
            return Response.created(location).entity(createdApplicationDTO).build();
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError(e.getLocalizedMessage(), log);
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
    public Response applicationsApplicationIdGet(String applicationId, String ifNoneMatch, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ApplicationDTO applicationDTO = ApplicationServiceImpl.getApplicationById(applicationId, organization);
        return Response.ok().entity(applicationDTO).build();
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
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String ifMatch,
            MessageContext messageContext) throws APIManagementException {
        ApplicationDTO updatedApplicationDTO = ApplicationServiceImpl.updateApplication(applicationId, body);
        return Response.ok().entity(updatedApplicationDTO).build();
    }

    /**
     * Export an existing Application
     *
     * @param appName        Search query
     * @param appOwner       Owner of the Application
     * @param withKeys       Export keys with application
     * @param format         Export format
     * @param messageContext Message Context
     * @return Zip file containing exported Application
     */
    @Override
    public Response applicationsExportGet(String appName, String appOwner, Boolean withKeys, String format,
            MessageContext messageContext) throws APIManagementException {

        File file = ApplicationServiceImpl.exportApplication(appName, appOwner, withKeys, format);
        return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getName() + "\"").build();
    }

    @Override
    public Response applicationsApplicationIdApiKeysKeyTypeGeneratePost(String applicationId, String keyType,
            String ifMatch, APIKeyGenerateRequestDTO body, MessageContext messageContext)
            throws APIManagementException {

        APIKeyDTO apiKeyDto = ApplicationServiceImpl.generateAPIKey(applicationId, keyType, body);
        return Response.ok().entity(apiKeyDto).build();
    }

    @Override
    public Response applicationsApplicationIdApiKeysKeyTypeRevokePost(String applicationId, String keyType,
            String ifMatch, APIKeyRevokeRequestDTO body, MessageContext messageContext) throws APIManagementException {
        ApplicationServiceImpl.revokeAPIKey(applicationId, body);
        return Response.ok().build();
    }

    /**
     * Deletes an application by id
     *
     * @param applicationId     application identifier
     * @param ifMatch           If-Match header value
     * @return 200 Response if successfully deleted the application
     */
    @Override
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        int deletedApplicationId = ApplicationServiceImpl.deleteApplication(applicationId);
        if (deletedApplicationId == -1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.CREATED).build();
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
            body, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ApplicationKeyDTO applicationKeyDTO = ApplicationServiceImpl.generateKeys(applicationId, body, organization);
        return Response.ok().entity(applicationKeyDTO).build();
    }

    /**
     * Retrieve all keys of an application
     *
     * @param applicationId Application Id
     * @return Application Key Information list
     */
    @Override
    public Response applicationsApplicationIdKeysGet(String applicationId, MessageContext messageContext)
            throws APIManagementException {

        ApplicationKeyListDTO applicationKeyListDTO = ApplicationServiceImpl.getApplicationKeysByApplicationId(
                applicationId);
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
            MessageContext messageContext) throws APIManagementException {

        ApplicationServiceImpl.cleanupApplicationRegistration(applicationId, keyType);
        return Response.ok().build();
    }

    @Override
    public Response applicationsApplicationIdKeysKeyTypeGenerateTokenPost(String applicationId, String keyType,
            ApplicationTokenGenerateRequestDTO body, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        ApplicationTokenDTO appToken = ApplicationServiceImpl.generateToken(applicationId, keyType, body);
        return Response.ok().entity(appToken).build();
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
    public Response applicationsApplicationIdKeysKeyTypeGet(String applicationId, String keyType, String groupId,
            MessageContext messageContext) throws APIManagementException {
        return Response.ok()
                .entity(ApplicationServiceImpl.getApplicationKeyByAppIDAndKeyType(applicationId, keyType))
                .build();
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
            ApplicationKeyDTO body, MessageContext messageContext) throws APIManagementException {

        ApplicationKeyDTO applicationKeyDTO = ApplicationServiceImpl.updateApplicationKeysKeyType(
                applicationId, keyType, body);
        return Response.ok().entity(applicationKeyDTO).build();
    }

    /**
     * Re generate consumer secret.
     *
     * @param applicationId Application Id
     * @param keyType       Key Type (Production | Sandbox)
     * @return A response object containing application keys.
     */
    @Override
    public Response applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(String applicationId, String keyType,
            MessageContext messageContext) throws APIManagementException {

        ApplicationKeyDTO applicationKeyDTO = ApplicationServiceImpl.renewConsumerSecret(applicationId,
                keyType);
        return Response.ok().entity(applicationKeyDTO).build();
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
            String xWSO2Tenant, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ApplicationKeyDTO applicationKeyDTO = ApplicationServiceImpl.mapApplicationKeys(applicationId, body,
                organization);
        return Response.ok().entity(applicationKeyDTO).build();
    }

    @Override
    public Response applicationsApplicationIdOauthKeysGet(String applicationId,
            String xWso2Tenant, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ApplicationKeyListDTO applicationKeyListDTO = ApplicationServiceImpl.getApplicationIdOauthKeys(
                applicationId, organization);
        return Response.ok().entity(applicationKeyListDTO).build();
    }

    @Override
    public Response applicationsApplicationIdOauthKeysKeyMappingIdCleanUpPost(String applicationId, String keyMappingId,
            String ifMatch,
            MessageContext messageContext)
            throws APIManagementException {

        ApplicationServiceImpl.cleanUpApplicationRegistrationByApplicationIdAndKeyMappingId(applicationId,
                keyMappingId);
        return Response.ok().build();
    }

    @Override
    public Response applicationsApplicationIdOauthKeysKeyMappingIdGenerateTokenPost(String applicationId,
            String keyMappingId, ApplicationTokenGenerateRequestDTO body, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        ApplicationTokenDTO appToken = ApplicationServiceImpl.generateTokenByOauthKeysKeyMappingId(
                applicationId, keyMappingId, body);
        return Response.ok().entity(appToken).build();
    }

    @Override
    public Response applicationsApplicationIdOauthKeysKeyMappingIdGet(String applicationId, String keyMappingId,
            String groupId, MessageContext messageContext) throws APIManagementException {
        return Response.ok().entity(ApplicationServiceImpl.getApplicationKeyByAppIDAndKeyMapping(applicationId,
                keyMappingId)).build();
    }

    @Override
    public Response applicationsApplicationIdOauthKeysKeyMappingIdPut(String applicationId, String keyMappingId,
            ApplicationKeyDTO body, MessageContext messageContext) throws APIManagementException {

        ApplicationKeyDTO applicationKeyDTO = ApplicationServiceImpl.applicationsApplicationIdOauthKeysKeyMappingIdPut(
                applicationId, keyMappingId, body);
        return Response.ok().entity(applicationKeyDTO).build();
    }

    @Override
    public Response applicationsApplicationIdOauthKeysKeyMappingIdRegenerateSecretPost(String applicationId,
            String keyMappingId, MessageContext messageContext) throws APIManagementException {

        ApplicationKeyDTO retrievedApplicationKey = ApplicationServiceImpl
                .applicationsApplicationIdOauthKeysKeyMappingIdRegenerateSecretPost(applicationId, keyMappingId);
        return Response.ok().entity(retrievedApplicationKey).build();
    }
}
