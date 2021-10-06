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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
            String msg = "Error in creating directory at: " + path;
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
            String errorMsg = "Error in Creating archive from data.";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Extracts a a given zip archive
     *
     * @param archiveFilePath path of the zip archive
     * @param destination     extract location
     * @return name of the extracted zip archive
     * @throws APIManagementException if an error occurs while extracting the archive
     */
    public static String extractArchive(String archiveFilePath, String destination)
            throws APIManagementException {
        int bufferSize = 512;
        long sizeLimit = 0x6400000; // Max size of unzipped data, 100MB
        int maxEntryCount = 1024;
        String archiveName = null;

        try {
            FileInputStream fis = new FileInputStream(archiveFilePath);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            int entries = 0;
            long total = 0;

            // Process each entry
            while ((entry = zis.getNextEntry()) != null) {
                String currentEntry = entry.getName();
                int index = 0;
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
                if (entry.isDirectory()) {
                    log.debug("Creating directory " + destinationFile.getAbsolutePath());
                    destinationFile.mkdir();
                    continue;
                }

                // create the parent directory structure
                if (destinationParent.mkdirs()) {
                    log.debug("Creation of folder is successful. Directory Name : " + destinationParent.getName());
                }

                int count;
                byte[] data = new byte[bufferSize];
                FileOutputStream fos = new FileOutputStream(destinationFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize);
                while (total + bufferSize <= sizeLimit && (count = zis.read(data, 0, bufferSize)) != -1) {
                    dest.write(data, 0, count);
                    total += count;
                }
                dest.flush();
                dest.close();
                zis.closeEntry();
                entries++;
                if (entries > maxEntryCount) {
                    throw new APIManagementException("Too many files to unzip.");
                }
                if (total + bufferSize > sizeLimit) {
                    throw new APIManagementException("File being unzipped is too big.");
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
     * @param recursive recursion of extensions
     * @return collection of files for the extension
     */
    public static Collection<File> searchFilesWithMatchingExtension(File folder, String extension, boolean recursive) {
        return FileUtils.listFiles(folder, new String[]{extension}, recursive);
    }

    /**
     * Returns a collection of files for the given extension in the given folder.
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

    /**
     * Iterates through the files in the given path with extension and search the provided string. If a file is found,
     * then returns true
     *
     * @param path folder path
     * @param extension file extension to filter
     * @param stringTosearch string to search in files
     * @return true when a file is found in the folder path whose content contains the provided string
     */
    public static boolean hasFileContainsString(String path, String extension, String stringTosearch) {
        File folderToImport = new File(path);
        Collection<File> foundWSDLFiles = APIFileUtil.searchFilesWithMatchingExtension(folderToImport, extension);
        for (File file : foundWSDLFiles) {
            String absWSDLPath = file.getAbsolutePath();
            if (log.isDebugEnabled()) {
                log.debug("Processing WSDL file: " + absWSDLPath);
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    if (inputLine.indexOf(stringTosearch) > 0) {
                        return true;
                    }
                }
            } catch (IOException e) {
                log.error("Error while validating WSDL files in path " + path, e);
                return false;
            }
        }
        return false;
    }
}
