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

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dao.UploadedUsageFileInfoDAO;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dto.UploadedFileInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.MicroGatewayAPIUsageConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsagePublisherException;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsagePublisherUtils;

import org.wso2.carbon.databridge.agent.DataPublisher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class sends events to DAS which are read from the uploaded usage file
 */
public class UploadedUsagePublisher implements Runnable {

    private static final Log log = LogFactory.getLog(UploadedUsagePublisher.class);

    private DataPublisher dataPublisher;
    private UploadedFileInfoDTO infoDTO;

    public UploadedUsagePublisher(UploadedFileInfoDTO infoDTO) throws UsagePublisherException {
        this.infoDTO = infoDTO;
        dataPublisher = UsagePublisherUtils.getDataPublisher();
    }

    @Override
    public void run() {
        log.info("Started publishing API usage in file : " + infoDTO.toString());
        publishEvents();
    }

    private void publishEvents() {

        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        InputStream fileContentStream = null;
        ZipInputStream zipInputStream = null;
        try {
            //Get Content of the file and start processing
            fileContentStream = UploadedUsageFileInfoDAO.getFileContent(infoDTO);
            if (fileContentStream == null) {
                log.warn("No content available in the file : " + infoDTO.toString()
                        + ". Therefore, not publishing the record.");
                UploadedUsageFileInfoDAO.updateCompletion(infoDTO);
                return;
            }
            zipInputStream = new ZipInputStream(fileContentStream);
            for (ZipEntry zipEntry; (zipEntry = zipInputStream.getNextEntry()) != null; ) {
                if (zipEntry.getName().equals(MicroGatewayAPIUsageConstants.API_USAGE_OUTPUT_FILE_NAME)) {
                    InputStream inputStream = zipInputStream;
                    inputStreamReader = new InputStreamReader(inputStream);
                    bufferedReader  = new BufferedReader(inputStreamReader);
                    String readLine;

                    while ((readLine = bufferedReader.readLine()) != null) {
                        String[] elements = readLine.split(MicroGatewayAPIUsageConstants.EVENT_SEPARATOR);
                        //StreamID
                        String streamId = elements[0].split(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)[1];
                        //Timestamp
                        String timeStamp = elements[1].split(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)[1];
                        //MetaData
                        String metaData = elements[2].split(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)[1];
                        //correlationData
                        String correlationData = elements[3].split(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)[1];
                        //PayloadData
                        String payloadData = elements[4].split(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)[1];
                        try {
                            dataPublisher.tryPublish(streamId, Long.parseLong(timeStamp),
                                    (Object[]) UsagePublisherUtils.createMetaData(metaData),
                                    (Object[]) UsagePublisherUtils.createMetaData(correlationData),
                                    UsagePublisherUtils.createPayload(streamId, payloadData));
                        } catch (Exception e) {
                            log.warn("Error occurred while publishing event : " + Arrays.toString(elements), e);
                        }
                    }
                }
            }
            //There is no way to check the current size of the queue, hence wait 30 seconds in order to get the
            //data publisher queue cleaned up
            Thread.sleep(30000);
            //Update the database
            UploadedUsageFileInfoDAO.updateCompletion(infoDTO);
            log.info("Completed publishing API Usage from file : " + infoDTO.toString());
        } catch (IOException e) {
            log.error("Error occurred while reading the API Usage file.", e);
        } catch (UsagePublisherException e) {
            log.error("Error occurred while updating the completion for the processed file.", e);
        } catch (InterruptedException e) {
            //Ignore
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(inputStreamReader);
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(fileContentStream);
            IOUtils.closeQuietly(zipInputStream);
        }
    }

}
