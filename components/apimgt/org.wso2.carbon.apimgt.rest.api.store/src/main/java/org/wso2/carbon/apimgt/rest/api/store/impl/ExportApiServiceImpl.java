/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.utils.FileBasedApplicationImportExportManager;
import org.wso2.msf4j.Request;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import javax.ws.rs.core.Response;

public class ExportApiServiceImpl extends ExportApiService {

    private static final Logger log = LoggerFactory.getLogger(ExportApiServiceImpl.class);

    /**
     * Export an existing Application
     *
     * @param appId   Search query
     * @param request msf4j request object
     * @return Zip file containing exported Applications
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response exportApplicationsGet(String appId, Request request)
            throws NotFoundException {

        APIStore consumer = null;
        String exportedFilePath, zippedFilePath = null;
        Application applicationDetails;
        String exportedAppDirName = "exported-application";
        String pathToExportDir = System.getProperty("java.io.tmpdir") + File.separator + "exported-app-archives-" +
                UUID.randomUUID().toString(); //creates a directory in default temporary-file directory
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                    (consumer, pathToExportDir);
            applicationDetails = importExportManager.getApplicationDetails(appId, username);
            if (applicationDetails == null) {
                // 404
                String errorMsg = "No application found for query " + appId;
                log.error(errorMsg);
                HashMap<String, String> paramList = new HashMap<>();
                paramList.put("query", appId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(ExceptionCodes.APPLICATION_NOT_FOUND, paramList);
                return Response.status(Response.Status.NOT_FOUND).entity(errorDTO).build();
            }

            exportedFilePath = importExportManager.exportApplication(applicationDetails, exportedAppDirName);
            zippedFilePath = importExportManager.createArchiveFromExportedAppArtifacts(exportedFilePath,
                    pathToExportDir, exportedAppDirName);

        } catch (APIManagementException e) {
            String errorMessage = "Error while exporting Application";
            log.error(errorMessage, e);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

        File exportedApplicationArchiveFile = new File(zippedFilePath);
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK)
                .entity(exportedApplicationArchiveFile);
        responseBuilder
                .header("Content-Disposition", "attachment; filename=\"" + exportedApplicationArchiveFile
                        .getName() + "\"");
        Response response = responseBuilder.build();
        return response;
    }
}
