package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.store.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.store.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import javax.ws.rs.core.Response;

public class ImportApiServiceImpl extends ImportApiService {
    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

    /**
     * Import an Application which has been exported to a zip file
     *
     * @param fileInputStream content stream of the zip file which contains exported Application
     * @param fileDetail      meta information of the zip file
     * @return imported Application
     */
    @Override
    public Response importApplicationsPost(InputStream fileInputStream, Attachment fileDetail,
                                           Boolean preserveOwner, Boolean addSubscriptions) {
        APIConsumer consumer = null;
        String username = RestApiUtil.getLoggedInUsername();
        String tempDirPath = System.getProperty("java.io.tmpdir") + File.separator + "imported-app-archive-" +
                UUID.randomUUID().toString();
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager =
                    new FileBasedApplicationImportExportManager(consumer, tempDirPath);
            Application applicationDetails = importExportManager.importApplication(fileInputStream);
            if (preserveOwner != null && preserveOwner) {
                username = applicationDetails.getSubscriber().getName();
            }
            int appId = consumer.addApplication(applicationDetails, username);
            if (addSubscriptions != null && addSubscriptions) {
                importExportManager.importSubscriptions(applicationDetails, username, appId);
            }
            Application importedApplication = consumer.getApplicationById(appId);
            return Response.ok().entity(importedApplication).build();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while importing Application" + username, e, log);
        }
        return null;
    }

    @Override
    public String importApplicationsPostGetLastUpdatedTime(InputStream fileInputStream, Attachment fileDetail,
                                                           Boolean preserveOwner, Boolean addSubscriptions) {
        return null;
    }
}
