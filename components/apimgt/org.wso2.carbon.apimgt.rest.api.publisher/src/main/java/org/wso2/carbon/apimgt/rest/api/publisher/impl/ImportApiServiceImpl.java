package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.FileBasedApiImportExportManager;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-19T18:39:38.447+05:30")
public class ImportApiServiceImpl extends ImportApiService {

    private static final Logger log = LoggerFactory.getLogger(ImportApiServiceImpl.class);

    @Override
    public Response importApisPut(InputStream fileInputStream, FileInfo fileDetail, String contentType,
                                  String provider) throws NotFoundException {

        APIPublisher publisher = null;

        try {
            publisher = RestAPIPublisherUtil.getApiPublisher(RestApiUtil.getLoggedInUsername());

            FileBasedApiImportExportManager importManager = new FileBasedApiImportExportManager(publisher,
                    System.getProperty("java.io.tmpdir") + File.separator + "imported-api-archives-" +
                            UUID.randomUUID().toString());
            APIListDTO apiList = importManager.importAPIs(fileInputStream, provider);
            return Response.status(Response.Status.CREATED).entity(apiList).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error while importing APIs from extracted archive";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
