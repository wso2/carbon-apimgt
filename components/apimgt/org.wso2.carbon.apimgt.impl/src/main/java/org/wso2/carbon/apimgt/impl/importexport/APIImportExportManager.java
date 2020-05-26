/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.importexport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIExportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;

import java.io.File;
import java.io.InputStream;

/**
 * This class is responsible for providing helper functions to import and export APIs.
 */
public class APIImportExportManager {

    private static final Log log = LogFactory.getLog(APIImportExportManager.class);
    private APIProvider apiProvider;
    private String loggedInUsername;

    /**
     * Constructor to initialize APIImportExportManager by APIProvider and current user.
     *
     * @param apiProvider      API Provider for logged in user
     * @param loggedInUsername Current username
     */
    public APIImportExportManager(APIProvider apiProvider, String loggedInUsername) {
        this.apiProvider = apiProvider;
        this.loggedInUsername = loggedInUsername;
    }

    public APIProvider getApiProvider() {
        return apiProvider;
    }

    /**
     * This method is used to export the given API as an archive (zip file).
     *
     * @param apiToReturn       Requested API to export
     * @param isStatusPreserved Is API status preserved or not
     * @param exportFormat      Export file format of the API
     * @return Archive file for the requested API
     * @throws APIImportExportException If an error occurs while exporting the API and creating the archive
     */
    public File exportAPIArchive(API apiToReturn, boolean isStatusPreserved, ExportFormat exportFormat)
            throws APIImportExportException {

        APIIdentifier apiIdentifier = apiToReturn.getId();
        String archiveBasePath = exportAPIArtifacts(apiToReturn, isStatusPreserved, exportFormat);
        CommonUtil.archiveDirectory(archiveBasePath);
        log.info("API" + apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion() + " exported successfully");
        FileUtils.deleteQuietly(new File(archiveBasePath));
        return new File(archiveBasePath + APIConstants.ZIP_FILE_EXTENSION);
    }

    /**
     * This method is used to export the given API artifacts to the temp location.
     *
     * @param apiToReturn       Requested API to export
     * @param isStatusPreserved Is API status preserved or not
     * @param exportFormat      Export file format of the API
     * @return tmp location for the exported API artifacts
     */
    public String exportAPIArtifacts(API apiToReturn, boolean isStatusPreserved, ExportFormat exportFormat)
            throws APIImportExportException {

        //create temp location for storing API data
        File exportFolder = CommonUtil.createTempDirectory(apiToReturn);
        String exportAPIBasePath = exportFolder.toString();
        //Retrieve the API and related artifacts and populate the api folder in the temp location
        APIExportUtil.retrieveApiToExport(exportAPIBasePath, apiToReturn, apiProvider, loggedInUsername,
                isStatusPreserved, exportFormat);
        return exportAPIBasePath;
    }

    /**
     * This method is used to import the given API archive file. Importing an API as a new API and overwriting an existing
     * API both are supported. In both cases, the state of the API will be preserved.
     *
     * @param uploadedInputStream Input stream for importing API archive file
     * @param isProviderPreserved Is API Provider preserved or not
     * @param overwrite           Whether to overwrite an existing API (update API)
     * @throws APIImportExportException If an error occurs while importing the API
     */
    public void importAPIArchive(InputStream uploadedInputStream, Boolean isProviderPreserved, Boolean overwrite)
            throws APIImportExportException {
        //Temporary directory is used to create the required folders
        File importFolder = CommonUtil.createTempDirectory(null);
        String uploadFileName = APIImportExportConstants.UPLOAD_FILE_NAME;
        String absolutePath = importFolder.getAbsolutePath() + File.separator;
        CommonUtil.transferFile(uploadedInputStream, uploadFileName, absolutePath);

        String extractedFolderName = CommonUtil.extractArchive(new File(absolutePath + uploadFileName), absolutePath);

        APIImportUtil.importAPI(absolutePath + extractedFolderName, loggedInUsername, isProviderPreserved,
                apiProvider, overwrite);
        FileUtils.deleteQuietly(importFolder);
        FileUtils.deleteQuietly(new File(extractedFolderName));
    }

}
