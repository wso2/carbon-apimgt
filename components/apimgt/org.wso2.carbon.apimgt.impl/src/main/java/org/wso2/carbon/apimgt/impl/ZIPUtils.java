/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZIPUtils {

    /**
     * Creates a zip archive from the provided folder
     *
     * @param dirName folder to zip
     * @param nameZipFile absolute path to the zip file which needs to be created
     * @throws IOException when error occurred while creating the zip file
     */
    public static void zipDir(String dirName, String nameZipFile) throws IOException {
        try (FileOutputStream fW = new FileOutputStream(nameZipFile); ZipOutputStream zip = new ZipOutputStream(fW)) {
            addFolderToZip("", dirName, zip);
        }

    }

    /**
     * Creates a zip archive from the provided list of files
     *
     * @param zipFile absolute path to the zip file which needs to be created
     * @param fileList list of files
     * @throws APIManagementException when error occurred while creating the zip file
     */
    public static void zipFiles(String zipFile, Collection<File> fileList) throws APIManagementException {
        byte[] buffer = new byte[1024];
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (File file : fileList) {
                String path = file.getAbsolutePath().substring(
                        file.getAbsolutePath().indexOf(APIConstants.API_WSDL_EXTRACTED_DIRECTORY)
                                + APIConstants.API_WSDL_EXTRACTED_DIRECTORY.length() + 1);
                ZipEntry ze = new ZipEntry(path);
                zos.putNextEntry(ze);
                try (FileInputStream in = new FileInputStream(file)) {
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            throw new APIManagementException("Error occurred while creating the ZIP file: " + zipFile, e);
        }
    }

    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws IOException {
        File folder = new File(srcFolder);
        if (folder.list().length == 0) {
            addFileToZip(path, srcFolder, zip, true);
        } else {
            for (String fileName : folder.list()) {
                if (path.equals("")) {
                    addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip, false);
                } else {
                    addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip, false);
                }
            }
        }
    }

    private static void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean flag) throws IOException {
        File folder = new File(srcFile);
        if (flag) {
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName() + "/"));
        } else {
            if (folder.isDirectory()) {
                addFolderToZip(path, srcFile, zip);
            } else {
                byte[] buf = new byte[1024];
                int len;
                try (FileInputStream in = new FileInputStream(srcFile)) {
                    zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                    while ((len = in.read(buf)) > 0) {
                        zip.write(buf, 0, len);
                    }
                }
            }
        }
    }
}