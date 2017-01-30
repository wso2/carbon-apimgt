package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.ImportExportManager;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-13T09:50:10.416+05:30")
public class ExportApiServiceImpl extends ExportApiService {

    private static final Logger log = LoggerFactory.getLogger(ExportApiServiceImpl.class);

    @Override
    public Response exportApisGet(String contentType, String query, Integer limit, Integer offset) throws NotFoundException {

        APIPublisher publisher = null;
        String exportedFilePath = null;
        List<API> apis;
        try {
            publisher = RestAPIPublisherUtil.getApiPublisher(RestApiUtil.getLoggedInUsername());
            apis = publisher.searchAPIs(limit, offset, query);
            if (apis.isEmpty()) {
                // 404
                String errorMsg = "No APIs found for query " + query;
                log.error(errorMsg);
                HashMap<String, String> paramList = new HashMap<>();
                paramList.put("query", query);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(ExceptionCodes.API_NOT_FOUND, paramList);
                return Response.status(Response.Status.NOT_FOUND).entity(errorDTO).build();
            }

            ImportExportManager importExportManager = new ImportExportManager(publisher, System.getProperty("java.io.tmpdir")
                    + File.separator + "exported-api-archives-" + UUID.randomUUID().toString());
            exportedFilePath = importExportManager.exportAPIs(apis);

        } catch (APIManagementException e) {
            String errorMessage = "Error while exporting APIs";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

        File exportedApiArchiveFile = new File(exportedFilePath);
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).entity(exportedApiArchiveFile);
        responseBuilder.header("Content-Disposition", "attachment; filename=\"" + exportedApiArchiveFile.getName() + "\"");
        Response response = responseBuilder.build();

        // remove the local directory to which api info. was imported
//        try {
//            ImportExportUtils.deleteDirectory(apiUniqueDirectory);
//        } catch (APIManagementException e) {
//            // no need to throw, log and continue
//            log.error("Error while deleteing directory " + apiUniqueDirectory, e);
//        }

        return response;
    }
}
