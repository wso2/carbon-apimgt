/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Util class for API file based operations.
 */
public class APIFileUtil {

    public static final String WSDL_FILE_EXTENSION = "wsdl";
    public static final String XSD_FILE_EXTENSION = "xsd";

    private static final Logger log = LoggerFactory.getLogger(APIFileUtil.class);

    public static String extractUploadedArchive(InputStream uploadedApiArchiveInputStream, String importedDirectoryName,
            String apiArchiveLocation, String extractLocation) throws APIManagementException {
        String archiveExtractLocation;
        try {
            // create api import directory structure
            APIFileUtil.createDirectory(extractLocation);
            // create archive
            createArchiveFromInputStream(uploadedApiArchiveInputStream, apiArchiveLocation);
            // extract the archive
            archiveExtractLocation = extractLocation + File.separator + importedDirectoryName;
            extractArchive(apiArchiveLocation, archiveExtractLocation);

        } catch (APIManagementException e) {
            APIFileUtil.deleteDirectory(extractLocation);
            String errorMsg = "Error in accessing uploaded API archive";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
        return archiveExtractLocation;
    }

    /**
     * Creates a directory
     *
     * @param path path of the directory to create
     * @throws APIManagementException if an error occurs while creating the directory
     */
    public static void createDirectory(String path) throws APIManagementException {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            String msg = "Error in creating directory.";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Delete a given directory
     *
     * @param path Path to the directory to be deleted
     * @throws APIManagementException if unable to delete the directory
     */
    public static void deleteDirectory(String path) throws APIManagementException {
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            String errorMsg = "Error while deleting directory : " + path;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Creates a zip archive from the given {@link InputStream} inputStream
     *
     * @param inputStream {@link InputStream} instance
     * @param archivePath path to create the zip archive
     * @throws APIManagementException if an error occurs while creating the archive
     */
    public static void createArchiveFromInputStream(InputStream inputStream, String archivePath)
            throws APIManagementException {
        try (FileOutputStream outFileStream = new FileOutputStream(new File(archivePath))) {
            IOUtils.copy(inputStream, outFileStream);
        } catch (IOException e) {
            String errorMsg = "Error occurred while archiving the data.";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Extracts a given zip archive
     *
     * @param archiveFilePath path of the zip archive
     * @param destination     extract location
     * @return name of the extracted zip archive
     * @throws APIManagementException if an error occurs while extracting the archive
     */
    public static String extractArchive(String archiveFilePath, String destination)
            throws APIManagementException {
        String archiveName = null;

        try (ZipFile zip = new ZipFile(new File(archiveFilePath))) {
            Enumeration zipFileEntries = zip.entries();
            int index = 0;

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {

                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                //This index variable is used to get the extracted folder name; that is root directory
                if (index == 0 && currentEntry.indexOf('/') != -1) {
                    archiveName = currentEntry.substring(0, currentEntry.indexOf('/'));
                    --index;
                }

                File destinationFile = new File(destination, currentEntry);
                File destinationParent = destinationFile.getParentFile();
                String canonicalizedDestinationFilePath = destinationFile.getCanonicalPath();

                if (!canonicalizedDestinationFilePath.startsWith(new File(destination).getCanonicalPath())) {
                    String errorMessage = "Attempt to upload invalid zip archive with file at " + currentEntry +
                            ". File path is outside target directory";
                    log.error(errorMessage);
                    throw new APIManagementException(errorMessage);
                }

                // create the parent directory structure
                if (destinationParent.mkdirs()) {
                    log.debug("Successfully created folder with directory Name : " + destinationParent.getName());
                }

                if (!entry.isDirectory()) {
                    writeFileToDestination(zip, entry, destinationFile);
                }
            }
            return archiveName;
        } catch (IOException e) {
            String errorMsg = "Failed to extract archive file: " + archiveFilePath + " to destination: " + destination;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    private static void writeFileToDestination(ZipFile zip, ZipEntry entry, File destinationFile) throws IOException {
        try (InputStream zipInputStream = zip.getInputStream(entry);
                BufferedInputStream inputStream = new BufferedInputStream(zipInputStream);
                // write the current file to the destination
                FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    /**
     * Extract single wsdl file file uploaded when creating a soap api.
     *
     * @param inputStream  file input stream of the uploaded file
     * @param wsdlFileDir  wsdl file extraction directory
     * @param wsdlFilePath file path of the uploaded wsdl file
     */
    public static void extractSingleWSDLFile(InputStream inputStream, String wsdlFileDir, String wsdlFilePath) {
        try {
            APIFileUtil.createDirectory(wsdlFileDir);
            APIFileUtil.createArchiveFromInputStream(inputStream, wsdlFilePath);
        } catch (APIManagementException e) {
            String errorMsg = "Failed to extract wsdl file: " + wsdlFilePath;
            log.error(errorMsg, e);
        }
    }

    /**
     * Returns a collection of files for the given extension.
     *
     * @param folder    folder that include files
     * @param extension file extension
     * @return collection of files for the extension
     */
    public static Collection<File> searchFilesWithMatchingExtension(File folder, String extension) {
        return FileUtils.listFiles(folder, new String[] {extension}, true);
    }

    /**
     * Deletes a parent directory of a given file path.
     *
     * @param filePath file path
     */
    public static void deleteParentDirectory(String filePath) {
        if (StringUtils.isNotBlank(filePath)) {
            try {
                FileUtils.deleteDirectory(new File(filePath).getParentFile());
            } catch (IOException e) {
                log.warn("Failed to clean temporary files at : " + filePath +
                        " Delete those files manually or it will be cleared after a server restart.");
            }
        }
    }
}
