package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.APIDetails;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import java.io.File;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ExportApiServiceImpl extends ExportApiService {
    @Override
    public Response exportApplicationsGet(String query, Integer limit, Integer offset, Request request)
            throws NotFoundException {

        APIStore apiConsumer = null;
        String exportedFilePath, zippedFilePath = null;
        Application application;
        String exportedApiDirName = "exported-apis";
        String pathToExportDir = System.getProperty("java.io.tmpdir") + File.separator + "exported-api-archives-" +
                UUID.randomUUID().toString();

        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            apiConsumer = RestApiUtil.getConsumer(username);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }


        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
