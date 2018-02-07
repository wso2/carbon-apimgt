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
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.admin.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ExportApiServiceImpl extends ExportApiService {

    private static final Log log = LogFactory.getLog(ExportApiServiceImpl.class);

    /**
     * Export an existing Application
     *
     * @param appName Search query
     * @return Zip file containing exported Application
     */
    @Override
    public Response exportApplicationsGet(String appName, String appOwner) {
        APIConsumer consumer;
        String exportedFilePath;
        Application applicationDetails = null;
        File exportedApplicationArchiveFile = null;
        String exportedAppDirName = "exported-application";
        String pathToExportDir = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator +
                "exported-app-archives-" +
                UUID.randomUUID().toString(); //creates a directory in default temporary-file directory
        String username = RestApiUtil.getLoggedInUsername();
        String exportedFileName = null;
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                    (consumer, pathToExportDir);
            if (appOwner!=null && importExportManager.isValidOwner(appOwner)) {
                applicationDetails = importExportManager.getApplicationDetails(appName, appOwner);
                if (applicationDetails == null) {
                    // 404
                    String errorMsg = "No application found with name " + appName + "owned by " + appOwner;
                    log.error(errorMsg);
                    return Response.status(Response.Status.NOT_FOUND).entity(errorMsg).build();
                } else if (!MultitenantUtils.getTenantDomain(applicationDetails.getSubscriber().getName()).equals
                        (MultitenantUtils.getTenantDomain(username))) {
                    String errorMsg = "Cross Tenant Exports are not allowed";
                    log.error(errorMsg);
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
                }
            }
            exportedFilePath = importExportManager.exportApplication(applicationDetails, exportedAppDirName);
            String zippedFilePath = importExportManager.createArchiveFromExportedAppArtifacts(exportedFilePath,
                    pathToExportDir, exportedAppDirName);
            exportedApplicationArchiveFile = new File(zippedFilePath);
            exportedFileName = exportedApplicationArchiveFile.getName();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while exporting Application: " + appName, e, log);
        }
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK)
                .entity(exportedApplicationArchiveFile).type(MediaType.APPLICATION_OCTET_STREAM);
        responseBuilder
                .header("Content-Disposition", "attachment; filename=\"" + exportedFileName + "\"");
        return responseBuilder.build();
    }

    @Override
    public String exportApplicationsGetGetLastUpdatedTime(String appName) {
        return null;
    }
}
