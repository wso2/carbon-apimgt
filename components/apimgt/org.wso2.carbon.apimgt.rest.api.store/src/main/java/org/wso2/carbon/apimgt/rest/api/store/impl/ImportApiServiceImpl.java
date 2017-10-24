package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import java.io.File;
import java.util.List;

import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;
import java.util.UUID;

import org.wso2.carbon.apimgt.rest.api.store.utils.FileBasedApplicationImportExportManager;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

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

        try {
            consumer = RestApiUtil.getConsumer(RestApiUtil.getLoggedInUsername(request));
            FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager(consumer, System.getProperty("java.io.tmpdir") + File.separator + "exported-app-archives-" +
                    UUID.randomUUID().toString());
            //Application application = importExportManager.importAndCreateApplications(fileInputStream);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }

        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response importApplicationsPut(InputStream fileInputStream, FileInfo fileDetail
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
