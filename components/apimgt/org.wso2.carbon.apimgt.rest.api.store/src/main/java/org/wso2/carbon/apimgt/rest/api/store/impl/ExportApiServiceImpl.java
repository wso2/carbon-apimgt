package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.APIDetails;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import org.wso2.carbon.apimgt.rest.api.store.utils.FileBasedApplicationImportExportManager;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ExportApiServiceImpl extends ExportApiService {

    private static final Logger log = LoggerFactory.getLogger(ExportApiServiceImpl.class);

    /**
     * Export an existing Application
     *
     * @param query   Search query
     * @param limit   maximum applications to export
     * @param offset  starting position of the Search
     * @param request msf4j request object
     * @return Zip file containing exported Applications
     * @throws NotFoundException When the particular resource does not exist in the system
     */

    @Override
    public Response exportApplicationsGet(String query, Integer limit, Integer offset, Request request)
            throws NotFoundException {


        APIStore consumer = null;
        String exportedFilePath, zippedFilePath = null;
        Application applicationDetails;
        String exportedAppDirName = "exported-applications";
        String pathToExportDir = System.getProperty("java.io.tmpdir") + File.separator + "exported-app-archives-" +
                UUID.randomUUID().toString();

        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                    (consumer, pathToExportDir);
            applicationDetails = importExportManager.getApplicationDetails(limit, offset, query, username);
            if (applicationDetails == null) {
                // 404
                String errorMsg = "No applications found for query " + query;
                log.error(errorMsg);
                HashMap<String, String> paramList = new HashMap<>();
                paramList.put("query", query);
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
