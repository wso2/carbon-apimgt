package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.utils.PolicyExportManager;
import org.wso2.msf4j.Request;

import java.io.File;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class contains the implementation of export policies api.
 */
public class ExportApiServiceImpl extends ExportApiService {
    private APIMgtAdminService apiMgtAdminService;
    private static final Logger log = LoggerFactory.getLogger(ExportApiServiceImpl.class);

    public ExportApiServiceImpl(APIMgtAdminService apiMgtAdminService) {
        this.apiMgtAdminService = apiMgtAdminService;
    }

    /**
     * Export throttle policies containing zip.
     *
     * @param accept  Accept header value
     * @param request msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response exportPoliciesThrottleGet(String accept, Request request) throws NotFoundException {

        String archiveName = "exported-policies";
        //files will be written to following directory
        String exportedPoliciesDirName = "exported-policies";
        //archive will be here at following location tmp directory
        String archiveDir = System.getProperty("java.io.tmpdir");

        if (log.isDebugEnabled()) {
            log.debug("Received export policies GET request ");
        }
        try {
            PolicyExportManager policyExportManager = new PolicyExportManager(apiMgtAdminService);
            //create archive and get the archive location
            String zippedFilePath = policyExportManager
                    .createArchiveFromExecutionPlans(exportedPoliciesDirName, archiveDir, archiveName);
            APIFileUtils.deleteDirectory(exportedPoliciesDirName);
            File exportedApiArchiveFile = new File(zippedFilePath);
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK).entity(exportedApiArchiveFile);
            responseBuilder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + exportedApiArchiveFile.getName() + "\"");
            Response response = responseBuilder.build();
            return response;
        } catch (APIManagementException e) {
            String errorMessage = "Error while exporting policies";
            log.error(errorMessage, e);
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
