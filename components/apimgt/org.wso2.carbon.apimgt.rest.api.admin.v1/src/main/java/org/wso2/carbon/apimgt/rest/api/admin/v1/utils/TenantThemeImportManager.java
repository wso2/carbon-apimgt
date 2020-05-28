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

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TenantThemeImportManager {

    private static final Log log = LogFactory.getLog(TenantThemeImportManager.class);

    //using a set for file extensions white list since it will be faster to search
    private static final Set<String> EXTENSION_WHITELIST = new HashSet<String>(Arrays.asList(
            "css", "jpg", "png", "gif", "svg", "ttf", "html", "js", "json", "ico"));

    public static String getStoreTenantThemesPath() {

        return "repository" + File.separator + "deployment" + File.separator + "server" + File.separator + "jaggeryapps"
                + File.separator + "devportal" + File.separator + "site" + File.separator + "public"
                + File.separator + "tenant_themes" + File.separator;
    }

    public static void deployTenantTheme(InputStream themeFile, String tenantDomain) throws APIManagementException {

        ZipInputStream zipInputStream = null;
        byte[] buffer = new byte[1024];

        String outputFolder = getStoreTenantThemesPath() + tenantDomain;

        try {
            //create output directory if it does not exist
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    handleException("Unable to create tenant theme directory");
                }
            } else {
                //remove existing files inside the directory
                FileUtils.cleanDirectory(folder);
            }

            //get the zip file content
            zipInputStream = new ZipInputStream(themeFile);
            //get the zipped file list entry
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {

                String fileName = zipEntry.getName();
                APIUtil.validateFileName(fileName);
                File newFile = new File(outputFolder + File.separator + fileName);
                String canonicalizedNewFilePath = newFile.getCanonicalPath();
                String canonicalizedDestinationPath = new File(outputFolder).getCanonicalPath();
                if (!canonicalizedNewFilePath.startsWith(canonicalizedDestinationPath)) {
                    handleException(
                            "Attempt to upload invalid zip archive with file at " + fileName + ". File path is " +
                                    "outside target directory");
                }

                if (zipEntry.isDirectory()) {
                    if (!newFile.exists()) {
                        boolean status = newFile.mkdir();
                        if (!status) {
                            handleException("Error while creating " + newFile.getName() + " directory");
                        }
                    }
                } else {
                    String ext = FilenameUtils.getExtension(zipEntry.getName());
                    if (EXTENSION_WHITELIST.contains(ext)) {
                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream fileOutputStream = new FileOutputStream(newFile);

                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, len);
                        }

                        fileOutputStream.close();
                    } else {
                        handleException(
                                "Unsupported file is uploaded with tenant theme by " + tenantDomain + " : file name : "
                                        + zipEntry.getName());
                    }

                }
                zipEntry = zipInputStream.getNextEntry();
            }

            zipInputStream.closeEntry();
            zipInputStream.close();

        } catch (IOException ex) {
            handleException("Failed to deploy tenant theme", ex);
        } finally {
            IOUtils.closeQuietly(zipInputStream);
            IOUtils.closeQuietly(themeFile);
        }
    }

    private static void handleException(String msg) throws APIManagementException {

        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {

        throw new APIManagementException(msg, t);
    }
}
