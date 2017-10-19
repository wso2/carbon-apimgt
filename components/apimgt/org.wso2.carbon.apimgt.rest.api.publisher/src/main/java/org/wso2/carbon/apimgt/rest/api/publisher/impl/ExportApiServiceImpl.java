package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.APIDetails;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.FileBasedApiImportExportManager;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2017-01-13T09:50:10.416+05:30")
public class ExportApiServiceImpl extends ExportApiService {

    private static final Logger log = LoggerFactory.getLogger(ExportApiServiceImpl.class);

    /**
     * Exports an existing API
     *
     * @param query       Search query
     * @param limit       maximum APIs to export
     * @param offset      Starting position of the search
     * @param request     ms4j request object
     * @return Zip file containing the exported APIs
     * @throws NotFoundException When the particular resource does not exist in the system
     */

    @Override
    public Response exportApisGet(String query, Integer limit, Integer offset, Request request)
            throws NotFoundException {

        APIPublisher publisher = null;
        String exportedFilePath, zippedFilePath = null;
        Set<APIDetails> apiDetails;
        String exportedApiDirName = "exported-apis";
        String pathToExportDir = System.getProperty("java.io.tmpdir") + File.separator + "exported-api-archives-" +
                UUID.randomUUID().toString();
        try {
            publisher = RestAPIPublisherUtil.getApiPublisher(RestApiUtil.getLoggedInUsername(request));
            FileBasedApiImportExportManager importExportManager = new FileBasedApiImportExportManager(publisher,
                    pathToExportDir);
            apiDetails = importExportManager.getAPIDetails(limit, offset, query);
            if (apiDetails.isEmpty()) {
                // 404
                String errorMsg = "No APIs found for query " + query;
                log.error(errorMsg);
                HashMap<String, String> paramList = new HashMap<>();
                paramList.put("query", query);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(ExceptionCodes.API_NOT_FOUND, paramList);
                return Response.status(Response.Status.NOT_FOUND).entity(errorDTO).build();
            }

            exportedFilePath = importExportManager.exportAPIs(apiDetails, exportedApiDirName);
            zippedFilePath = importExportManager.createArchiveFromExportedApiArtifacts(exportedFilePath,
                    pathToExportDir, exportedApiDirName);

        } catch (APIManagementException e) {
            String errorMessage = "Error while exporting APIs";
            log.error(errorMessage, e);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

        File exportedApiArchiveFile = new File(zippedFilePath);
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).entity(exportedApiArchiveFile);
        responseBuilder
                .header("Content-Disposition", "attachment; filename=\"" + exportedApiArchiveFile.getName() + "\"");
        Response response = responseBuilder.build();

        //
        // TODO: remove the local directory to which api info. was imported
        //        try {
        //            ImportExportUtils.deleteDirectory(apiUniqueDirectory);
        //        } catch (APIManagementException e) {
        //            // no need to throw, log and continue
        //            log.error("Error while deleteing directory " + apiUniqueDirectory, e);
        //        }

        return response;
    }
}
