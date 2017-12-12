package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;

import java.io.File;

import java.util.HashMap;
import java.util.List;

import java.io.InputStream;
import java.util.UUID;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.rest.api.store.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ExportApiServiceImpl extends ExportApiService {

    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

    /**
     * Export an existing Application
     *
     * @param appId Search query
     * @return Zip file containing exported Application
     */
    @Override
    public Response exportApplicationsGet(String appId, String accept, String ifNoneMatch) {

        APIConsumer consumer = null;
        String exportedFilePath, zippedFilePath = null;
        Application applicationDetails;
        String exportedAppDirName = "exported-application";
        String pathToExportDir = System.getProperty("java.io.tmpdir") + File.separator + "exported-app-archives-" +
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
        File exportedApplicationArchiveFile = new File(zippedFilePath);
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK)
                .entity(exportedApplicationArchiveFile).type(MediaType.APPLICATION_OCTET_STREAM);
        responseBuilder
                .header("Content-Disposition", "attachment; filename=\"" + exportedApplicationArchiveFile
                        .getName() + "\"");
        Response response = responseBuilder.build();
        return response;
    }

    @Override
    public String exportApplicationsGetGetLastUpdatedTime(String appId, String accept, String ifNoneMatch) {
        return null;
    }
}
