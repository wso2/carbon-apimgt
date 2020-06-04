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
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIExportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIProductImportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIProductExportUtil;
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
     * @param apiTypeWrapper    Requested API or API Product to export
     * @param isStatusPreserved Is API or API Product  status preserved or not
     * @param exportFormat      Export file format of the API or the API Product
     * @return Archive file for the requested API or API Product
     * @throws APIImportExportException If an error occurs while exporting the API or the API Product and creating the archive
     */
    public File exportAPIOrAPIProductArchive(ApiTypeWrapper apiTypeWrapper, boolean isStatusPreserved, ExportFormat exportFormat)
            throws APIImportExportException {
        String archiveBasePath;
        if (!apiTypeWrapper.isAPIProduct()) {
            APIIdentifier apiIdentifier = apiTypeWrapper.getApi().getId();
            archiveBasePath = exportAPIArtifacts(apiTypeWrapper.getApi(), isStatusPreserved, exportFormat);
            if (log.isDebugEnabled()) {
                log.debug("API" + apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion() + " exported successfully");
            }
        } else {
            APIProductIdentifier apiProductIdentifier = apiTypeWrapper.getApiProduct().getId();
            archiveBasePath = exportAPIProductArtifacts(apiTypeWrapper.getApiProduct(), isStatusPreserved, exportFormat);
            if (log.isDebugEnabled()) {
                log.info("API Product" + apiProductIdentifier.getName() + "-" + apiProductIdentifier.getVersion() + " exported successfully");
            }
        }
        CommonUtil.archiveDirectory(archiveBasePath);
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
        File exportFolder = CommonUtil.createTempDirectory(apiToReturn.getId());
        String exportAPIBasePath = exportFolder.toString();
        //Retrieve the API and related artifacts and populate the api folder in the temp location
        APIExportUtil.retrieveApiToExport(exportAPIBasePath, apiToReturn, apiProvider, loggedInUsername,
                isStatusPreserved, exportFormat);
        return exportAPIBasePath;
    }

    /**
     * This method is used to export the given API Product artifacts to the temp location.
     *
     * @param apiProductToReturn    Requested API  Product to export
     * @param isStatusPreserved     Is API Product status preserved or not
     * @param exportFormat          Export file format of the API Product
     * @return tmp location for the exported API Product artifacts
     * @throws APIImportExportException If an error occurs while exporting the API Product
     */
    public String exportAPIProductArtifacts(APIProduct apiProductToReturn, boolean isStatusPreserved, ExportFormat exportFormat)
            throws APIImportExportException {

        // Create temp location for storing API Product data
        File exportFolder = CommonUtil.createTempDirectory(apiProductToReturn.getId());
        String exportAPIProductBasePath = exportFolder.toString();
        // Retrieve the API Product and related artifacts and populate the api folder in the temp location
        APIProductExportUtil.retrieveApiProductToExport(exportAPIProductBasePath, apiProductToReturn, apiProvider, loggedInUsername,
                isStatusPreserved, exportFormat);
        return exportAPIProductBasePath;
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

    /**
     * This method is used to import the given API Product archive file. Importing an API Product as a new API and overwriting an existing
     * API Product both are supported.
     *
     * @param uploadedInputStream       Input stream for importing API archive file
     * @param isProviderPreserved       Is API Provider preserved or not
     * @param overwriteAPIProduct       Whether to overwrite an existing API Product (update API Product)
     * @param overwriteAPIs             Whether to update the dependent APIs or not. This is used when updating already existing dependent APIs of an API Product.
     * @param isImportAPIs              Whether to import the dependent APIs or not.
     * @throws APIImportExportException If an error occurs while importing the API
     */
    public void importAPIProductArchive(InputStream uploadedInputStream, Boolean isProviderPreserved, Boolean overwriteAPIProduct,
                                        Boolean overwriteAPIs, Boolean isImportAPIs)
            throws APIImportExportException {
        // Temporary directory is used to create the required folders
        File importFolder = CommonUtil.createTempDirectory(null);
        String uploadFileName = APIImportExportConstants.UPLOAD_FILE_NAME;
        String absolutePath = importFolder.getAbsolutePath() + File.separator;
        CommonUtil.transferFile(uploadedInputStream, uploadFileName, absolutePath);

        String extractedFolderName = CommonUtil.extractArchive(new File(absolutePath + uploadFileName), absolutePath);

        APIProductImportUtil.importAPIProduct(absolutePath + extractedFolderName, loggedInUsername, isProviderPreserved,
                    apiProvider, overwriteAPIProduct, overwriteAPIs, isImportAPIs);

        FileUtils.deleteQuietly(importFolder);
        FileUtils.deleteQuietly(new File(extractedFolderName));
    }
}
