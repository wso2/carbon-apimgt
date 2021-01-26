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

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.zip;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class has methods for Compressing and De-Compressing files into and from ZIP format
 */
public class ZIPUtils {

    private static final Log log = LogFactory.getLog(ZIPUtils.class);

    private static final int BUFFER_SIZE = 1024;

    /**
     * Compresses the given file into ZIP format
     *
     * @param sourcePath source file path
     * @param destinationPath destination file path
     * @throws ZIPException if there is an error while compressing
     */
    public static void compressFile(String sourcePath, String destinationPath) throws ZIPException {
        if (log.isDebugEnabled()) {
            log.debug("Compressing file : " + sourcePath + " to : " + destinationPath);
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        ZipOutputStream zipOutputStream = null;
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;

        try {
            fileOutputStream = new FileOutputStream(destinationPath);
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            ZipEntry zipEntry = new ZipEntry(sourcePath);
            zipOutputStream.putNextEntry(zipEntry);
            fileInputStream = new FileInputStream(sourcePath);

            int len;
            while ((len = fileInputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
            }
            zipOutputStream.finish();
        } catch (IOException ex) {
            throw new ZIPException("Error occurred while compressing the file : " + sourcePath, ex);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(zipOutputStream);
        }
    }
}
