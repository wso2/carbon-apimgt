/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.SubscribedApiDTO;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ApplicationKeyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.models.ExportedApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil.createDirectory;

public class ExportUtils {
    private static final Log log = LogFactory.getLog(ExportUtils.class);

    /**
     * Retrieve all the details of an Application by name for a given user.
     *
     * @param appName name of the application
     * @return {@link Application} instance
     * @throws APIManagementException if an error occurs while retrieving Application details
     */
    public static Application getApplicationDetails(String appName, String username, APIConsumer apiConsumer)
            throws APIManagementException {
        Application application;
        int appId = APIUtil.getApplicationId(appName, username);
        String groupId = apiConsumer.getGroupId(appId);
        application = apiConsumer.getApplicationById(appId);
        if (application != null) {
            application.setGroupId(groupId);
            application.setOwner(application.getSubscriber().getName());
        }
        return application;
    }

    /**
     * Export a given Application to a file system as zip archive.
     * The export root location is given by {@link @path}/exported-application.
     *
     * @param exportApplication Application{@link Application} to be exported
     * @param apiConsumer       API Consumer
     * @return Path to the exported directory with exported artifacts
     * @throws APIManagementException If an error occurs while exporting an application to a file system
     */
    public static File exportApplication(Application exportApplication, APIConsumer apiConsumer,
            ExportFormat exportFormat) throws APIManagementException {
        String archivePath = null;
        String exportApplicationBasePath;
        String appName = exportApplication.getName();
        String appOwner = exportApplication.getOwner();
        try {
            // Creates a temporary directory to store the exported application artifact
            File exportFolder = createTempApplicationDirectory(appName, appOwner);
            exportApplicationBasePath = exportFolder.toString();
            archivePath = exportApplicationBasePath.concat(File.separator + appOwner + "-" + appName);
            // Files.createDirectories(Paths.get(applicationArtifactBaseDirectoryPath));
        } catch (APIImportExportException e) {
            throw new APIManagementException("Unable to create the temporary directory to export the Application", e);
        }
        ExportedApplication applicationDtoToExport = createApplicationDTOToExport(exportApplication, apiConsumer);
        try {
            createDirectory(archivePath);
            // Export application details
            CommonUtil
                    .writeDtoToFile(archivePath + File.separator + ImportExportConstants.TYPE_APPLICATION, exportFormat,
                            ImportExportConstants.TYPE_APPLICATION, applicationDtoToExport);
            CommonUtil.archiveDirectory(exportApplicationBasePath);
            FileUtils.deleteQuietly(new File(exportApplicationBasePath));
            return new File(exportApplicationBasePath + APIConstants.ZIP_FILE_EXTENSION);
        } catch (IOException | APIImportExportException e) {
            throw new APIManagementException("Error while exporting Application: " + exportApplication.getName(), e);
        }
    }

    /**
     * Create an aggregated Application DTO to be exported.
     *
     * @param application Application{@link Application} to be exported
     * @param apiConsumer API Consumer
     * @throws APIManagementException If an error occurs while retrieving subscribed APIs
     */
    private static ExportedApplication createApplicationDTOToExport(Application application, APIConsumer apiConsumer)
            throws APIManagementException {
        ApplicationDTO applicationDto = ApplicationMappingUtil.fromApplicationtoDTO(application);

//        List<ApplicationKeyDTO> applicationKeyDTOs = new ArrayList<>();
//        for (APIKey apiKey : application.getKeys()) {
//            ApplicationKeyDTO applicationKeyDTO = ApplicationKeyMappingUtil.fromApplicationKeyToDTO(apiKey);
//            applicationKeyDTOs.add(applicationKeyDTO);
//        }
//        applicationDto.setKeys(applicationKeyDTOs);
        ExportedApplication exportedApplication = new ExportedApplication(applicationDto);

        Set<SubscribedAPI> subscriptions = apiConsumer
                .getSubscribedAPIs(application.getSubscriber(), application.getName(), application.getGroupId());

        exportedApplication.setSubscribedAPIs(subscriptions);
        exportedApplication.setKeyManagerWiseOAuthApp(application.getKeyManagerWiseOAuthApp());
        return exportedApplication;
    }

    /**
     * Create temporary directory for an Application in temporary location.
     * The format of the name of the directory would be {application_owner}_{application_name}
     *
     * @throws APIImportExportException If an error occurs while creating temporary location
     */
    public static File createTempApplicationDirectory(String appName, String appOwner) throws APIImportExportException {
        String currentDirectory = System.getProperty(APIConstants.JAVA_IO_TMPDIR);
        File tempDirectory = new File(currentDirectory + File.separator + appOwner + "_" + appName);
        createDirectory(tempDirectory.getPath());
        return tempDirectory;
    }
}
