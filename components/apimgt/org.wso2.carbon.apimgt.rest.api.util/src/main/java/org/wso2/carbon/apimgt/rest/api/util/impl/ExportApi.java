package org.wso2.carbon.apimgt.rest.api.util.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportManager;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.io.File;

public class ExportApi {
    private static final Log log = LogFactory.getLog(ExportApi.class);
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

    public Response exportApiGet(String name, String version, String providerName, String format, Boolean preserveStatus) {
        ExportFormat exportFormat;
        API api;
        APIImportExportManager apiImportExportManager;
        String userName;
        APIIdentifier apiIdentifier;
        APIProvider apiProvider;
        String apiDomain;
        String apiRequesterDomain;
        //If not specified status is preserved by default
        boolean isStatusPreserved = preserveStatus == null || preserveStatus;

        if (name == null || version == null || providerName == null) {
            RestApiUtil.handleBadRequest("Invalid API Information ", log);
        }

        try {
            //Default export format is YAML
            exportFormat = StringUtils.isNotEmpty(format) ? ExportFormat.valueOf(format.toUpperCase()) :
                    ExportFormat.YAML;

            userName = RestApiUtil.getLoggedInUsername();
            //provider names with @ signs are only accepted
            apiDomain = MultitenantUtils.getTenantDomain(providerName);
            apiRequesterDomain = RestApiUtil.getLoggedInUserTenantDomain();

            if (!StringUtils.equals(apiDomain, apiRequesterDomain)) {
                //not authorized to export requested API
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API +
                        " name:" + name + " version:" + version + " provider:" + providerName, (String) null, log);
            }

            apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), name, version);
            apiProvider = RestApiUtil.getLoggedInUserProvider();
            // Checking whether the API exists
            if (!apiProvider.isAPIAvailable(apiIdentifier)) {
                String errorMessage = "Error occurred while exporting. API: " + name + " version: " + version
                        + " not found";
                RestApiUtil.handleResourceNotFoundError(errorMessage, log);
            }

            api = apiProvider.getAPI(apiIdentifier);
            apiImportExportManager = new APIImportExportManager(apiProvider, userName);
            File file = apiImportExportManager.exportAPIArchive(api, isStatusPreserved, exportFormat);
            return Response.ok(file)
                    .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\""
                            + file.getName() + "\"")
                    .build();
        } catch (APIManagementException | APIImportExportException e) {
            RestApiUtil.handleInternalServerError("Error while exporting " + RestApiConstants.RESOURCE_API, e, log);
        }
        return null;
    }
}
