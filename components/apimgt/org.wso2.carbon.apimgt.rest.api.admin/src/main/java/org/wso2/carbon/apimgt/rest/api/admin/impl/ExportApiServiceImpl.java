/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.rest.api.admin.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.utils.FileBasedApplicationImportExportManager;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.impl.ExportApiUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.charset.Charset;
import java.util.UUID;

public class ExportApiServiceImpl extends ExportApiService {

    private static final Log log = LogFactory.getLog(ExportApiServiceImpl.class);
    private static final String APPLICATION_EXPORT_DIR_PREFIX = "exported-app-archives-";
    private static final String DEFAULT_APPLICATION_EXPORT_DIR = "exported-application";
    private static final String PRODUCTION = "PRODUCTION";
    private static final String SANDBOX = "SANDBOX";

    /**
     * Exports an API from API Manager for a given API ID. Meta information, API icon, documentation, WSDL
     * and sequences are exported. This service generates a zipped archive which contains all the above mentioned
     * resources for a given API.
     *
     * @param name           Name of the API that needs to be exported
     * @param version        Version of the API that needs to be exported
     * @param providerName   Provider name of the API that needs to be exported
     * @param format         Format of output documents. Can be YAML or JSON
     * @param preserveStatus Preserve API status on export
     * @return Zipped file containing exported API
     */
    @Override
    public Response exportApiGet(String name, String version, String format, String providerName,
                                 Boolean preserveStatus) {
        ExportApiUtil exportApi = new ExportApiUtil();
        return exportApi.exportApiOrApiProductByParams(name, version, providerName, format, preserveStatus, RestApiConstants.RESOURCE_API);
    }

    /**
     * Export an existing Application
     *
     * @param appName  Search query
     * @param appOwner Owner of the Application
     * @param withKeys Export keys with application
     * @return Zip file containing exported Application
     */
    @Override
    public Response exportApplicationsGet(String appName, String appOwner, Boolean withKeys) {
        APIConsumer consumer;
        String exportedFilePath;
        Application applicationDetails = null;
        File exportedApplicationArchiveFile = null;
        String pathToExportDir = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator +
                APPLICATION_EXPORT_DIR_PREFIX +
                UUID.randomUUID().toString(); //creates a directory in default temporary-file directory
        String username = RestApiUtil.getLoggedInUsername();
        String exportedFileName = null;
        if (StringUtils.isBlank(appName) || StringUtils.isBlank(appOwner)) {
            RestApiUtil.handleBadRequest("Application name or owner should not be empty or null.", log);
        }
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedApplicationImportExportManager importExportManager = new FileBasedApplicationImportExportManager
                    (consumer, pathToExportDir);
            if (importExportManager.isOwnerAvailable(appOwner)) {
                applicationDetails = importExportManager.getApplicationDetails(appName, appOwner);
            }
            if (applicationDetails == null) {
                String errorMsg = "No application found with name " + appName + " owned by " + appOwner;
                log.error(errorMsg);
                return Response.status(Response.Status.NOT_FOUND).entity(errorMsg).build();
            } else if (Boolean.getBoolean(RestApiConstants.MIGRATION_MODE)) { // migration flow
                String appTenant = MultitenantUtils.getTenantDomain(applicationDetails.getSubscriber().getName());
                RestApiUtil.handleMigrationSpecificPermissionViolations(appTenant, username);
            } else if (!MultitenantUtils.getTenantDomain(applicationDetails.getSubscriber().getName()).equals
                    (MultitenantUtils.getTenantDomain(username))) {  // normal non-migration flow
                String errorMsg = "Cross Tenant Exports are not allowed";
                log.error(errorMsg);
                return Response.status(Response.Status.FORBIDDEN).entity(errorMsg).build();
            }

            // clear all duplicate keys with tokens
            applicationDetails.getKeys().clear();

            // export keys for application
            if (withKeys == null || !withKeys) {
                applicationDetails.clearOAuthApps();
            } else {
                // encode Oauth secrets
                OAuthApplicationInfo productionOAuthApplicationInfo = applicationDetails.getOAuthApp(PRODUCTION);
                if (productionOAuthApplicationInfo != null) {
                    byte[] consumerSecretBytes = productionOAuthApplicationInfo.getClientSecret().getBytes(Charset.defaultCharset());
                    productionOAuthApplicationInfo.setClientSecret(new String(Base64.encodeBase64(consumerSecretBytes)));
                }
                OAuthApplicationInfo sandboxOAuthApplicationInfo = applicationDetails.getOAuthApp(SANDBOX);
                if (sandboxOAuthApplicationInfo != null) {
                    byte[] consumerSecretBytes = sandboxOAuthApplicationInfo.getClientSecret().getBytes(Charset.defaultCharset());
                    sandboxOAuthApplicationInfo.setClientSecret(new String(Base64.encodeBase64(consumerSecretBytes)));
                }
            }

            exportedFilePath = importExportManager.exportApplication(applicationDetails,
                    DEFAULT_APPLICATION_EXPORT_DIR);
            String zippedFilePath = importExportManager.createArchiveFromExportedAppArtifacts(exportedFilePath,
                    pathToExportDir, DEFAULT_APPLICATION_EXPORT_DIR);
            exportedApplicationArchiveFile = new File(zippedFilePath);
            exportedFileName = exportedApplicationArchiveFile.getName();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while exporting Application: " + appName, e, log);
        }
        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK)
                .entity(exportedApplicationArchiveFile).type(MediaType.APPLICATION_OCTET_STREAM);
        responseBuilder
                .header("Content-Disposition", "attachment; filename=\"" + exportedFileName + "\"");
        return responseBuilder.build();
    }

    public String exportApplicationsGetGetLastUpdatedTime(String appName, String appOwner) {
        return null;
    }
}
