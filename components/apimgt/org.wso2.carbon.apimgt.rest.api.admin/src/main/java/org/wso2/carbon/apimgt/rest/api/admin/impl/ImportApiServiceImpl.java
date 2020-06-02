/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportManager;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.APIInfoMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;

public class ImportApiServiceImpl extends ImportApiService {
    private static final Log log = LogFactory.getLog(ImportApiServiceImpl.class);
    private static final String APPLICATION_IMPORT_DIR_PREFIX = "imported-app-archive-";
    private static final String PRODUCTION = "PRODUCTION";
    private static final String SANDBOX = "SANDBOX";
    private static final String GRANT_TYPES = "grant_types";
    private static final String GRANT_TYPE_IMPLICIT = "implicit";
    private static final String GRANT_TYPE_CODE = "code";
    private static final String REDIRECT_URIS = "redirect_uris";
    private static final String DEFAULT_TOKEN_SCOPE = "am_application_scope default";
    private static final int DEFAULT_VALIDITY_PERIOD = 3600;

    /**
     * Import an API by uploading an archive file. All relevant API data will be included upon the creation of
     * the API. Depending on the choice of the user, provider of the imported API will be preserved or modified.
     *
     * @param fileInputStream  uploadedInputStream input stream from the REST request
     * @param fileDetail       file details as Attachment
     * @param preserveProvider user choice to keep or replace the API provider
     * @param overwrite        whether to update the API or not. This is used when updating already existing APIs.
     * @return API import response
     */
    @Override
    public Response importApiPost(InputStream fileInputStream, Attachment fileDetail, Boolean preserveProvider,
                                  Boolean overwrite) {

        //Check whether to update. If not specified, default value is false.
        if (overwrite == null) {
            overwrite = false;
        }

        //Check if the URL parameter value is specified, otherwise the default value is true.
        if (preserveProvider == null) {
            preserveProvider = true;
        }

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            APIImportExportManager apiImportExportManager = new APIImportExportManager(apiProvider, userName);
            apiImportExportManager.importAPIArchive(fileInputStream, preserveProvider, overwrite);
            return Response.status(Response.Status.OK).entity("API imported successfully.").build();
        } catch (APIImportExportException | APIManagementException e) {
            if (RestApiUtil.isDueToResourceAlreadyExists(e)) {
                String errorMessage = "Error occurred while importing. Duplicate API already exists.";
                RestApiUtil.handleResourceAlreadyExistsError(errorMessage, e, log);
            } else if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                //Auth failure occurs when cross tenant accessing APIs with preserve provider true.
                String errorMessage = "Not Authorized to import cross tenant APIs with preserveProvider true.";
                RestApiUtil.handleAuthorizationFailure(errorMessage, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError("Requested " + RestApiConstants.RESOURCE_API
                        + " not found", e, log);
            }
            RestApiUtil.handleInternalServerError("Error while importing API", e, log);
        }
        return null;
    }

    /**
     * Import an Application which has been exported to a zip file
     *
     * @param appOwner            target owner of the application
     * @param preserveOwner       if true, preserve the original owner of the application
     * @param skipSubscriptions   if true, skip subscriptions of the application
     * @param fileInputStream     content stream of the zip file which contains exported Application
     * @param fileDetail          meta information of the zip file
     * @param skipApplicationKeys Skip application keys while importing
     * @param update              update if existing application found or import
     * @return imported Application
     */
    @Override
    public Response importApplicationsPost(InputStream fileInputStream, Attachment fileDetail,
                                           Boolean preserveOwner, Boolean skipSubscriptions, String appOwner,
                                           Boolean skipApplicationKeys, Boolean update) {
        APIConsumer consumer;
        String ownerId;
        int appId;
        String username = RestApiUtil.getLoggedInUsername();
        String tempDirPath = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator +
                APPLICATION_IMPORT_DIR_PREFIX +
                UUID.randomUUID().toString();
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager =
                    new FileBasedApplicationImportExportManager(consumer, tempDirPath);
            Application applicationDetails = importExportManager.importApplication(fileInputStream);

            // set tokenType of the application to DEFAULT if it is null
            if (StringUtils.isEmpty(applicationDetails.getTokenType())) {
                applicationDetails.setTokenType(APIConstants.DEFAULT_TOKEN_TYPE);
            }

            // decode Oauth secrets
            Map<String, OAuthApplicationInfo>
                    keyManagerWiseProductionOAuthApps = applicationDetails.getOAuthApp(PRODUCTION);
            if (keyManagerWiseProductionOAuthApps != null) {
                keyManagerWiseProductionOAuthApps.forEach((keyManagerName, oAuthApplicationInfo)->{
                    String encodedConsumerSecretBytes = oAuthApplicationInfo.getClientSecret();
                    String decodedConsumerSecret = new String(Base64.decodeBase64(encodedConsumerSecretBytes));
                    oAuthApplicationInfo.setClientSecret(decodedConsumerSecret);
                    applicationDetails.addKey(getAPIKeyFromOauthApp(PRODUCTION, keyManagerName, oAuthApplicationInfo));
                });

            }
            Map<String, OAuthApplicationInfo>
                    keyManagerWiseSandBoxOAuthApps = applicationDetails.getOAuthApp(SANDBOX);
            if (keyManagerWiseSandBoxOAuthApps != null) {
                keyManagerWiseSandBoxOAuthApps.forEach((keyManagerName, oAuthApplicationInfo)->{
                    String encodedConsumerSecretBytes = oAuthApplicationInfo.getClientSecret();
                    String decodedConsumerSecret = new String(Base64.decodeBase64(encodedConsumerSecretBytes));
                    oAuthApplicationInfo.setClientSecret(decodedConsumerSecret);
                    applicationDetails.addKey(getAPIKeyFromOauthApp(SANDBOX,keyManagerName,oAuthApplicationInfo));
                });

            }
            if (!StringUtils.isBlank(appOwner)) {
                ownerId = appOwner;
            } else if (preserveOwner != null && preserveOwner) {
                ownerId = applicationDetails.getOwner();
            } else {
                ownerId = username;
            }
            if (!MultitenantUtils.getTenantDomain(ownerId).equals
                    (MultitenantUtils.getTenantDomain(username))) {
                String errorMsg = "Cross Tenant Imports are not allowed";
                log.error(errorMsg);
                return Response.status(Response.Status.FORBIDDEN).entity(errorMsg).build();
            }
            importExportManager.validateOwner(ownerId, applicationDetails.getGroupId());

            // check whether we needs to update application or add it
            if (APIUtil.isApplicationExist(ownerId, applicationDetails.getName(), applicationDetails.getGroupId()) && update != null && update) {
                appId = APIUtil.getApplicationId(applicationDetails.getName(), ownerId);
                Application application = consumer.getApplicationById(appId);
                applicationDetails.setId(appId);
                applicationDetails.setUUID(application.getUUID());
                applicationDetails.setOwner(application.getOwner());
                applicationDetails.updateSubscriber(application.getSubscriber());
                consumer.updateApplication(applicationDetails);
            } else {
                appId = consumer.addApplication(applicationDetails, ownerId);
            }

            List<APIIdentifier> skippedAPIs = new ArrayList<>();
            if (skipSubscriptions == null || !skipSubscriptions) {
                skippedAPIs = importExportManager
                        .importSubscriptions(applicationDetails, ownerId, appId, update);
            }
            Application importedApplication = consumer.getApplicationById(appId);
            importedApplication.setOwner(ownerId);
            ApplicationInfoDTO importedApplicationDTO = ApplicationMappingUtil
                    .fromApplicationToInfoDTO(importedApplication);
            URI location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
                    importedApplicationDTO.getApplicationId());

            // check whether keys need to be skipped while import
            if (skipApplicationKeys == null || !skipApplicationKeys) {
                // Add application keys if present and keys does not exists in the current application
                if (applicationDetails.getKeys().size() > 0 && importedApplication.getKeys().size() == 0) {
                    for (APIKey apiKey : applicationDetails.getKeys()) {
                        importExportManager.addApplicationKey(ownerId, importedApplication, apiKey);
                    }
                }
            }

            if (skippedAPIs.isEmpty()) {
                return Response.created(location).entity(importedApplicationDTO).build();
            } else {
                APIInfoListDTO skippedAPIListDTO = APIInfoMappingUtil.fromAPIInfoListToDTO(skippedAPIs);
                return Response.created(location).status(207).entity(skippedAPIListDTO).build();
            }
        } catch (APIManagementException | URISyntaxException | UserStoreException e) {
            RestApiUtil
                    .handleInternalServerError("Error while importing Application", e, log);
        } catch (UnsupportedEncodingException e) {
            String errorMessage = "Error while Decoding apiId";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * This extracts information for creating an APIKey from an OAuthApplication
     * @param type Type of the OAuthApp(SANDBOX or PRODUCTION)
     * @param keyManagerName
     * @param oAuthApplicationInfo OAuth Application information
     * @return An APIKey containing keys from OAuthApplication
     */
    private APIKey getAPIKeyFromOauthApp(String type, String keyManagerName,
                                         OAuthApplicationInfo oAuthApplicationInfo){
        APIKey apiKey = new APIKey();
        apiKey.setType(type);
        apiKey.setConsumerKey(oAuthApplicationInfo.getClientId());
        apiKey.setConsumerSecret(oAuthApplicationInfo.getClientSecret());
        apiKey.setGrantTypes((String) oAuthApplicationInfo.getParameter(GRANT_TYPES));
        apiKey.setKeyManager(keyManagerName);
        if (apiKey.getGrantTypes().contains(GRANT_TYPE_IMPLICIT) && apiKey.getGrantTypes().contains(GRANT_TYPE_CODE)){
            apiKey.setCallbackUrl((String) oAuthApplicationInfo.getParameter(REDIRECT_URIS));
        }

        long validityPeriod = OAuthServerConfiguration.getInstance().getApplicationAccessTokenValidityPeriodInSeconds();
        apiKey.setValidityPeriod(validityPeriod);
        apiKey.setTokenScope(DEFAULT_TOKEN_SCOPE);
        apiKey.setAdditionalProperties(oAuthApplicationInfo.getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES));
        return apiKey;
    }

    public String importApplicationsPostGetLastUpdatedTime(InputStream fileInputStream, Attachment fileDetail,
                                                           Boolean preserveOwner, Boolean skipSubscriptions,
                                                           String appOwner) {
        return null;
    }
}
