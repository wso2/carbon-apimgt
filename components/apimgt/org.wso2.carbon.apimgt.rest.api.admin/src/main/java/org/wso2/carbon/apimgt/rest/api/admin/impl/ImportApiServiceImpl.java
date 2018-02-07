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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.admin.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.ws.rs.core.Response;

public class ImportApiServiceImpl extends ImportApiService {
    private static final Log log = LogFactory.getLog(ImportApiServiceImpl.class);

    /**
     * Import an Application which has been exported to a zip file
     *
     * @param fileInputStream content stream of the zip file which contains exported Application
     * @param fileDetail      meta information of the zip file
     * @return imported Application
     */
    @Override
    public Response importApplicationsPost(InputStream fileInputStream, Attachment fileDetail,
                                           Boolean preserveOwner, Boolean skipSubscriptions) {
        APIConsumer consumer;
        String username = RestApiUtil.getLoggedInUsername();
        String tempDirPath = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator +
                "imported-app-archive-" +
                UUID.randomUUID().toString();
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager =
                    new FileBasedApplicationImportExportManager(consumer, tempDirPath);
            Application applicationDetails = importExportManager.importApplication(fileInputStream);
            if (preserveOwner != null && preserveOwner) {
                username = applicationDetails.getSubscriber().getName();
            }
            int appId = consumer.addApplication(applicationDetails, username);
            if (skipSubscriptions == null || !skipSubscriptions) {
                importExportManager.importSubscriptions(applicationDetails, username, appId);
            }
            Application importedApplication = consumer.getApplicationById(appId);
            ApplicationInfoDTO importedApplicationDTO = ApplicationMappingUtil.fromApplicationToInfoDTO(importedApplication);
            URI location = new URI(RestApiConstants.RESOURCE_PATH_APPLICATIONS + "/" +
                    importedApplicationDTO.getApplicationId());
            return Response.created(location).entity(importedApplicationDTO).build();
        } catch (APIManagementException | URISyntaxException | UserStoreException e) {
            RestApiUtil
                    .handleInternalServerError("Error while importing Application" + username, e, log);
        }
        return null;
    }

    @Override
    public String importApplicationsPostGetLastUpdatedTime(InputStream fileInputStream, Attachment fileDetail,
                                                           Boolean preserveOwner, Boolean addSubscriptions) {
        return null;
    }
}
