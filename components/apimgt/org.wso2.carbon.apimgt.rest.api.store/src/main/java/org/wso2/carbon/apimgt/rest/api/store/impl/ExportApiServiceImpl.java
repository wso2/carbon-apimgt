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

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.store.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.store.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ExportApiServiceImpl extends ExportApiService {

    private static final Log log = LogFactory.getLog(ExportApiServiceImpl.class);

    /**
     * Export an existing Application
     *
     * @param appId Search query
     * @return Zip file containing exported Application
     */
    @Override
    public Response exportApplicationsGet(String appId) {
        APIConsumer consumer;
        String exportedFilePath, zippedFilePath = null;
        Application applicationDetails;
        String exportedAppDirName = "exported-application";
        String pathToExportDir = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator +
                "exported-app-archives-" +
                UUID.randomUUID().toString(); //creates a directory in default temporary-file directory
        String username = RestApiUtil.getLoggedInUsername();
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                    (consumer, pathToExportDir);
            applicationDetails = importExportManager.getApplicationDetails(appId);
            if (applicationDetails == null) {
                // 404
                String errorMsg = "No application found for query " + appId;
                log.error(errorMsg);
                return Response.status(Response.Status.NOT_FOUND).entity(errorMsg).build();
            }
            exportedFilePath = importExportManager.exportApplication(applicationDetails, exportedAppDirName);
            zippedFilePath = importExportManager.createArchiveFromExportedAppArtifacts(exportedFilePath,
                    pathToExportDir, exportedAppDirName);
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while exporting Application" + username, e, log);
        }
        File exportedApplicationArchiveFile = null;
        if (zippedFilePath != null) {
            exportedApplicationArchiveFile = new File(zippedFilePath);
        }
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK)
                .entity(exportedApplicationArchiveFile).type(MediaType.APPLICATION_OCTET_STREAM);
        responseBuilder
                .header("Content-Disposition", "attachment; filename=\"" + exportedApplicationArchiveFile
                        .getName() + "\"");
        return responseBuilder.build();
    }

    @Override
    public String exportApplicationsGetGetLastUpdatedTime(String appId) {
        return RestAPIStoreUtils.getLastUpdatedTimeByApplicationId(appId);
    }
}
