/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.MicroGatewayAPIUsageConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsageFileWriter;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsagePublisherException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.HttpRequestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.MicroGatewayCommonUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.TokenUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.internal.ServiceReferenceHolder;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

/**
 * Task for uploading the usage file
 */
public class APIUsageFileUploadTask implements Task {

    private static final Log log = LogFactory.getLog(APIUsageFileUploadTask.class);

    private ConfigManager configManager;

    @Override
    public void setProperties(Map<String, String> map) {
    }

    @Override
    public void init() {
    }

    @Override
    public void execute() {
        log.info("Running API Usage File Upload Task.");
        try {
            configManager = ConfigManager.getConfigManager();
        } catch (OnPremiseGatewayException e) {
            log.error("Error occurred while reading the configuration. Usage upload was cancelled.", e);
            return;
        }

        //[CARBON_HOME]/api-usage-data/
        Path usageFileDirectory = Paths.get(CarbonUtils.getCarbonHome(),
                MicroGatewayAPIUsageConstants.API_USAGE_OUTPUT_DIRECTORY);

        //Rotate current file
        Path activeFilePath = Paths.get(usageFileDirectory.toString(),
                MicroGatewayAPIUsageConstants.API_USAGE_OUTPUT_FILE_NAME);
        try {
            if (Files.size(activeFilePath) > 0) {       //Don't rotate if the current file is empty
                if (log.isDebugEnabled()) {
                    log.debug("Rotating current file for uploading.");
                }
                UsageFileWriter.getInstance().rotateFile(activeFilePath.toString());
            }
        } catch (UsagePublisherException | IOException e) {
            log.error("Error occurred while rotating the current file. Will only upload the previously " +
                    "rotated files.");
        }

        File[] listOfFiles = new File(usageFileDirectory.toUri()).listFiles();
        if (listOfFiles != null) {
            Arrays.sort(listOfFiles);
            for (File file : listOfFiles) {
                String fileName = file.getName();
                //Only get the files which have been rotated
                if (fileName.endsWith(MicroGatewayAPIUsageConstants.GZIP_EXTENSION)) {
                    try {
                        boolean uploadStatus = uploadCompressedFile(file.toPath(), fileName);
                        if (uploadStatus) {
                            //Rename the file to mark as uploaded
                            Path renamedPath = Paths.get(file.getAbsolutePath()
                                    + MicroGatewayAPIUsageConstants.UPLOADED_FILE_SUFFIX);
                            Files.move(file.toPath(), renamedPath);
                        } else {
                            log.error("Usage file Upload failed. It will be retried in the next task run.");
                        }
                    } catch (IOException e) {
                        log.error("Error occurred while moving the uploaded the File : " + fileName, e);
                    }
                }
            }
        }
    }

    /**
     * Uploads the API Usage file to Upload Service
     *
     * @param compressedFilePath File Path to the compressed file
     * @param fileName  Name of the uploading file
     * @return  Returns boolean true if uploading is successful
     */
    private boolean uploadCompressedFile(Path compressedFilePath, String fileName) {
        String response;

        try {
            String uploadServiceUrl = configManager.getProperty(MicroGatewayAPIUsageConstants.USAGE_UPLOAD_SERVICE_URL);
            uploadServiceUrl = (uploadServiceUrl != null && !uploadServiceUrl.isEmpty()) ? uploadServiceUrl :
                    MicroGatewayAPIUsageConstants.DEFAULT_UPLOAD_SERVICE_URL;
            URL uploadServiceUrlValue = MicroGatewayCommonUtil.getURLFromStringUrlValue(uploadServiceUrl);
            HttpClient httpClient = APIUtil.getHttpClient(uploadServiceUrlValue.getPort(), uploadServiceUrlValue
                    .getProtocol());
            HttpPost httppost = new HttpPost(uploadServiceUrl);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", compressedFilePath.toFile())
                    .build();
            httppost.setHeader(MicroGatewayAPIUsageConstants.FILE_NAME_HEADER, fileName);

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
            char[] password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD).toCharArray();

            String authHeaderValue = TokenUtil.getBasicAuthHeaderValue(username, password);
            MicroGatewayCommonUtil.cleanPasswordCharArray(password);
            httppost.setHeader(MicroGatewayAPIUsageConstants.AUTHORIZATION_HEADER, authHeaderValue);
            httppost.setHeader(MicroGatewayAPIUsageConstants.ACCEPT_HEADER,
                    MicroGatewayAPIUsageConstants.ACCEPT_HEADER_APPLICATION_JSON);
            httppost.setEntity(reqEntity);

            response = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httppost,
                    MicroGatewayAPIUsageConstants.MAX_RETRY_COUNT);
            log.info("API Usage file : " + compressedFilePath.getFileName() + " uploaded successfully. " +
                    "Server Response : " + response);
            return true;
        } catch (OnPremiseGatewayException e) {
            log.error("Error occurred while uploading API Usage file.", e);
        }
        return false;
    }
}
