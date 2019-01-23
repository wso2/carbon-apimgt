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

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.gzip.GZIPException;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.gzip.GZIPUtils;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class writes the events to a file
 */
public class UsageFileWriter {

    private static final Log log = LogFactory.getLog(UsageFileWriter.class);

    private static volatile UsageFileWriter usageFileWriter = null;

    private Path filePath = null;
    private FileOutputStream fileOutputStream = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedWriter bufferedWriter = null;

    private UsageFileWriter() throws UsagePublisherException {
        initialize();
    }

    /**
     * Initializes the Usage File Writer. Creates the directory which is used to keep the usage file and creates the
     * necessary output streams
     *
     * @throws UsagePublisherException if there is an error while creating the usage directory or initializing the
     * output streams
     */
    private void initialize() throws UsagePublisherException {
        if (log.isDebugEnabled()) {
            log.debug("Initializing Usage File Writer");
        }
        //Create Directory in Carbon-Home to keep the files
        Path directoryPath = Paths.get(CarbonUtils.getCarbonHome(),
                MicroGatewayAPIUsageConstants.API_USAGE_OUTPUT_DIRECTORY);
        if (!Files.exists(directoryPath)) {
            try {
                Files.createDirectories(directoryPath);
            } catch (IOException e) {
                throw new UsagePublisherException("Error occurred while creating the Usage Data Directory : "
                        + directoryPath.toString(), e);
            }
        }

        filePath = Paths.get(directoryPath.toString(), MicroGatewayAPIUsageConstants.API_USAGE_OUTPUT_FILE_NAME);
        try {
            fileOutputStream = new FileOutputStream(filePath.toFile(), true);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
        } catch (FileNotFoundException e) {
            throw new UsagePublisherException("Error occurred while opening the file to write: " +
                    filePath.toString(), e);
        }
    }

    /**
     * Returns the {@link UsageFileWriter} instance
     *
     * @return {@link UsageFileWriter} instance
     * @throws UsagePublisherException if there is an error while initializing {@link UsageFileWriter} instance
     */
    public static UsageFileWriter getInstance() throws UsagePublisherException {
        if (usageFileWriter == null) {
            synchronized (UsageFileWriter.class) {
                if (usageFileWriter == null) {
                    usageFileWriter = new UsageFileWriter();
                }
            }
        }
        return usageFileWriter;
    }

    /**
     * Writes the given content to the usage file
     *
     * @param content String content to be written
     */
    public synchronized void writeToFile(String content) {
        //Check if the file size exceeds the max limit (12mb can contain roughly 10000 requests or 30000 events)
        try {
            String sizeInMb = ConfigManager.getConfigManager().getProperty("MaxUsageFileSize");
            int maxFileSize = Integer.parseInt((sizeInMb != null && !sizeInMb.isEmpty()) ? sizeInMb : "12")
                    * 1024 * 1024;
            if (Files.size(filePath) > maxFileSize) {
                if (log.isDebugEnabled()) {
                    log.debug("Rotating API Usage File. File Size is > MaxFileSize (" + maxFileSize + " Mb)");
                }
                rotateFile(filePath.toString());
            }
        } catch (IOException | OnPremiseGatewayException | UsagePublisherException e) {
            log.error("Error occurred while rotating the file : " + filePath.toString(), e);
        }

        try {
            bufferedWriter.write(content);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            log.warn("Error occurred while writing event [" + content + "] to the file : " + filePath.toString(), e);
        }
    }

    /**
     * Rotates the given file. This will compress the file into GZIP format and add a timestamp to file name
     *
     * @param fileToRotate ({@link String}) path value of the file to be rotated
     * @throws UsagePublisherException if there is an exception while compressing or creating the output streams to
     * the new file
     */
    public synchronized void rotateFile(String fileToRotate) throws UsagePublisherException {
        try {
            closeFileResources();
            Path currentPath = Paths.get(fileToRotate);

            //api-usage-data.dat.1511772769858.gz
            Path rotatedPath = Paths.get(fileToRotate + "." + System.currentTimeMillis()
                    + MicroGatewayAPIUsageConstants.GZIP_EXTENSION);
            GZIPUtils.compressFile(currentPath.toString(), rotatedPath.toString());
            Files.delete(currentPath);

            //ReCreate the streams
            fileOutputStream = new FileOutputStream(filePath.toFile(), true);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
        } catch (GZIPException | IOException e) {
            throw new UsagePublisherException("Error occurred while rotating the file : " + fileToRotate, e);
        }
    }

    /**
     * Closes the output streams
     */
    public synchronized void closeFileResources() {
        IOUtils.closeQuietly(fileOutputStream);
        IOUtils.closeQuietly(bufferedWriter);
        IOUtils.closeQuietly(outputStreamWriter);
    }
}
