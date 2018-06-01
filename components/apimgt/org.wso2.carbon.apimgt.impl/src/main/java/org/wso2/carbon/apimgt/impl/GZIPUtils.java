/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GZIPUtils {
    private static final Log log = LogFactory.getLog(GZIPUtils.class);
    private static final int BUFFER_SIZE = 1028;

    public static void compressFile(String sourcePath, String destinationPath) throws APIManagementException{
        if (log.isDebugEnabled()) {
            log.debug("Compressing file : " + sourcePath + " to : " + destinationPath);
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        GZIPOutputStream gzipOutputStream = null;
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;

        try {
            fileOutputStream = new FileOutputStream(destinationPath);
            gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            fileInputStream = new FileInputStream(sourcePath);

            int length = 0;
            while ((length = fileInputStream.read(buffer)) > 0) {
                gzipOutputStream.write(buffer, 0, length);
            }
            gzipOutputStream.finish();
        } catch (IOException e) {
            throw new APIManagementException("Error while compressing file at " + sourcePath + " to" + destinationPath, e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(gzipOutputStream);
        }
    }

    public static File constructZippedResponse(Object data) throws APIManagementException {
        String tmpSourceFileName = System.currentTimeMillis() + APIConstants.JSON_FILE_EXTENSION;
        String tmpDestinationFileName = System.currentTimeMillis() + APIConstants.JSON_GZIP_FILENAME_EXTENSION;
        String sourcePath = System.getProperty(APIConstants.JAVA_IO_TMPDIR) + File.separator  + tmpSourceFileName;
        String destinationPath = System.getProperty(APIConstants.JAVA_IO_TMPDIR) + File.separator + tmpDestinationFileName;
        File zippedResponse;

        File file = new File(sourcePath);
        FileWriter fileWriter = null;
        try {
            file.createNewFile();
            fileWriter = new FileWriter(file);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(fileWriter, data);
            compressFile(sourcePath, destinationPath);
            zippedResponse = new File(destinationPath);
        } catch (IOException e) {
            throw new APIManagementException("Error while constructing zipped response..", e);
        } finally {
            IOUtils.closeQuietly(fileWriter);
        }
        return zippedResponse;
    }
}
