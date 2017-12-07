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
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationResponse;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.utils.FileBasedApplicationImportExportManager;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import javax.ws.rs.core.Response;

public class ImportApiServiceImpl extends ImportApiService {

    private static final Logger log = LoggerFactory.getLogger(ImportApiServiceImpl.class);

    /**
     * Import an Application which has been exported to a zip file
     *
     * @param fileInputStream content stream of the zip file which contains exported Application
     * @param fileDetail      meta information of the zip file
     * @param request         msf4j request object
     * @return Application that was imported
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response importApplicationsPost(InputStream fileInputStream, FileInfo fileDetail
            , Request request) throws NotFoundException {

        APIStore consumer = null;
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                    (consumer, System.getProperty("java.io.tmpdir") + File.separator + "exported-app-archives-" +
                            UUID.randomUUID().toString());
            Application applicationDetails = importExportManager.importApplication(fileInputStream);
            applicationDetails.setCreatedUser(username);
            applicationDetails.setUpdatedUser(username);
            ApplicationCreationResponse response = consumer.addApplication(applicationDetails);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (APIManagementException e) {
            String errorMsg = "Error while importing the Applications";
            log.error(errorMsg, e);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response importApplicationsPut(InputStream fileInputStream, FileInfo fileDetail
            , Request request) throws NotFoundException {

        APIStore consumer = null;
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            consumer = RestApiUtil.getConsumer(RestApiUtil.getLoggedInUsername(request));
            FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                    (consumer, System.getProperty("java.io.tmpdir") + File.separator + "exported-app-archives-" +
                            UUID.randomUUID().toString());
            Application applicationDetails = importExportManager.importApplication(fileInputStream);
            applicationDetails.setCreatedUser(username);
            applicationDetails.setUpdatedUser(username);
            Application updatedApplication = importExportManager.updateApplication(applicationDetails, username);
            return Response.status(Response.Status.OK).entity(updatedApplication).build();
        } catch (APIManagementException e) {
            String errorMsg = "Error while importing the Applications";
            log.error(errorMsg, e);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
