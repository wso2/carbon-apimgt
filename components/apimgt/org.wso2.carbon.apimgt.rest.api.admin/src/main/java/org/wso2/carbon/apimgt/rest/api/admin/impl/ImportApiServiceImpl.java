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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.admin.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.APIInfoMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;

public class ImportApiServiceImpl extends ImportApiService {
    private static final Log log = LogFactory.getLog(ImportApiServiceImpl.class);
    private static final String APPLICATION_IMPORT_DIR_PREFIX = "imported-app-archive-";

    /**
     * Import an Application which has been exported to a zip file
     *
     * @param appOwner          target owner of the application
     * @param preserveOwner     if true, preserve the original owner of the application
     * @param skipSubscriptions if true, skip subscriptions of the application
     * @param fileInputStream   content stream of the zip file which contains exported Application
     * @param fileDetail        meta information of the zip file
     * @return imported Application
     */
    @Override
    public Response importApplicationsPost(InputStream fileInputStream, Attachment fileDetail,
                                           Boolean preserveOwner, Boolean skipSubscriptions, String appOwner) {
        APIConsumer consumer;
        String ownerId;
        String username = RestApiUtil.getLoggedInUsername();
        String tempDirPath = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator +
                APPLICATION_IMPORT_DIR_PREFIX +
                UUID.randomUUID().toString();
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager =
                    new FileBasedApplicationImportExportManager(consumer, tempDirPath);
            Application applicationDetails = importExportManager.importApplication(fileInputStream);
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
            int appId = consumer.addApplication(applicationDetails, ownerId);
            List<APIIdentifier> skippedAPIs = new ArrayList<>();
            if (skipSubscriptions == null || !skipSubscriptions) {
                skippedAPIs = importExportManager
                        .importSubscriptions(applicationDetails, username, appId);
            }
            Application importedApplication = consumer.getApplicationById(appId);
            importedApplication.setOwner(ownerId);
            ApplicationInfoDTO importedApplicationDTO = ApplicationMappingUtil
                    .fromApplicationToInfoDTO(importedApplication);
            URI location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
                    importedApplicationDTO.getApplicationId());
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

    public String importApplicationsPostGetLastUpdatedTime(InputStream fileInputStream, Attachment fileDetail,
                                                           Boolean preserveOwner, Boolean skipSubscriptions,
                                                           String appOwner) {
        return null;
    }
}
