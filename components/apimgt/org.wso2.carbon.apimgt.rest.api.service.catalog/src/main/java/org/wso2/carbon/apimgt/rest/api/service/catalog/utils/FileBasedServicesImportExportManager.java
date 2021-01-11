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

package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.model.ExportArchive;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Manager class for File System based service Import and Export handling
 */
public class FileBasedServicesImportExportManager {
    private static final Log log = LogFactory.getLog(FileBasedServicesImportExportManager.class);
    private static final String IMPORTED_SERVICES = "imported-services";
    private String path;
    private APIConsumer apiConsumer;

    public FileBasedServicesImportExportManager(APIConsumer apiConsumer, String path) {
        this.apiConsumer = apiConsumer;
        this.path = path;
    }

    /**
     * Import a given service from an InputStream
     *
     * @param uploadedAppArchiveInputStream Content stream of the zip file which contains exported service
     * @throws APIManagementException if an error occurs while importing an service archive
     */
    public void importService(InputStream uploadedAppArchiveInputStream) throws APIManagementException {
        String appArchiveLocation = path + File.separator + IMPORTED_SERVICES +
                APIConstants.ZIP_FILE_EXTENSION;
        try {
            extractUploadedArchiveService(uploadedAppArchiveInputStream,
                    IMPORTED_SERVICES,
                    appArchiveLocation, path);
        } catch (IOException e) {
            String errorMsg = "Error occurred while importing service archive" + appArchiveLocation;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Extracts the service to the file system by reading the incoming {@link InputStream} object
     * uploadedServiceArchiveInputStream
     *
     * @param uploadedServiceArchiveInputStream Incoming {@link InputStream}
     * @param importedDirectoryName         directory to extract the archive
     * @param appArchiveLocation            full path of the archive location
     * @param extractLocation               full path to the location to which the archive will be written
     * @throws IOException if an error occurs while extracting the archive
     */
    private void extractUploadedArchiveService(InputStream uploadedServiceArchiveInputStream,
                                               String importedDirectoryName,
                                               String appArchiveLocation, String extractLocation)
            throws IOException, APIManagementException {
        String archiveExtractLocation;
        // create import directory structure
        Files.createDirectories(Paths.get(extractLocation));
        // create archive
        createArchiveFromInputStream(uploadedServiceArchiveInputStream, appArchiveLocation);
        // extract the archive
        extractArchive(appArchiveLocation, extractLocation);
    }

    /**
     * Creates an archive of the contained service details.
     *
     * @param sourceDirectory Directory which contains source file
     * @param archiveLocation Directory to generate the zip archive
     * @param archiveName     Name of the zip archive
     * @return path to the created archive file
     * @throws APIManagementException if an error occurs while creating an archive from app details
     */
    public ExportArchive createArchiveFromExportedServices(String sourceDirectory, String archiveLocation,
                                                           String archiveName) throws APIManagementException {
        String archivedFilePath;
        ExportArchive exportArchive = new ExportArchive();
        try {
            exportArchive.setETag(archiveDirectory(sourceDirectory, archiveLocation, archiveName));
        } catch (IOException e) {
            // cleanup the archive root directory
            try {
                FileUtils.deleteDirectory(new File(path));
            } catch (IOException e1) {
                log.warn("Unable to remove directory " + path);
            }
            String errorMsg = "Error while archiving directory " + sourceDirectory;
            throw new APIManagementException(errorMsg);
        }
        archivedFilePath = archiveLocation + File.separator + archiveName + APIConstants.ZIP_FILE_EXTENSION;
        exportArchive.setArchiveName(archivedFilePath);
        return exportArchive;
    }

    /**
     * Creates a zip archive from of a directory
     *
     * @param sourceDirectory directory to create zip archive from
     * @param archiveLocation path to the archive location, excluding archive name
     * @param archiveName     name of the archive to create
     * @throws IOException if an error occurs while creating the archive
     */
    private String archiveDirectory(String sourceDirectory, String archiveLocation, String archiveName)
            throws IOException {
        File directoryToZip = new File(sourceDirectory);
        List<File> fileList = new ArrayList<>();
        getAllFiles(directoryToZip, fileList);
        writeArchiveFile(directoryToZip, fileList, archiveLocation, archiveName);
        if (log.isDebugEnabled()) {
            log.debug("Archive generated successfully " + archiveName);
        }
        return Md5HashGenerator.generateHash(fileList);
    }

    /**
     * Queries all files under a directory recursively
     *
     * @param sourceDirectory full path to the root directory
     * @param fileList        list containing the files
     */
    private void getAllFiles(File sourceDirectory, List<File> fileList) {
        File[] files = sourceDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                fileList.add(file);
                if (file.isDirectory()) {
                    getAllFiles(file, fileList);
                }
            }
        }
    }

    /**
     * @param directoryToZip  directory to create zip archive
     * @param fileList        list of files
     * @param archiveLocation path to the archive location, excluding archive name
     * @param archiveName     name of the archive to create
     * @throws IOException if an error occurs while writing to the archive file
     */
    private void writeArchiveFile(File directoryToZip, List<File> fileList, String archiveLocation,
                                  String archiveName) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(archiveLocation + File.separator +
                archiveName
                + APIConstants.ZIP_FILE_EXTENSION);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            for (File file : fileList) {
                if (!file.isDirectory()) {
                    addToArchive(directoryToZip, file, zipOutputStream);
                }
            }
        }
    }

    /**
     * @param directoryToZip  directory to create zip archive
     * @param file            file to archive
     * @param zipOutputStream output stream of the written archive file
     * @throws IOException if an error occurs while adding file to archive
     */
    private void addToArchive(File directoryToZip, File file, ZipOutputStream zipOutputStream)
            throws IOException {
        // Add a file to archive
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // Get relative path from archive directory to the specific file
            String zipFilePath = file.getCanonicalPath()
                    .substring(directoryToZip.getCanonicalPath().length() + 1, file.getCanonicalPath().length());
            if (File.separatorChar != '/') {
                zipFilePath = zipFilePath.replace(File.separatorChar, '/');
            }
            ZipEntry zipEntry = new ZipEntry(zipFilePath);
            zipOutputStream.putNextEntry(zipEntry);
            IOUtils.copy(fileInputStream, zipOutputStream);
            zipOutputStream.closeEntry();
        }
    }

    /**
     * Creates a zip archive from the given {@link InputStream} inputStream
     *
     * @param inputStream {@link InputStream} instance
     * @param archivePath path to create the zip archive
     * @throws IOException if an error occurs while creating the archive
     */
    private void createArchiveFromInputStream(InputStream inputStream, String archivePath)
            throws IOException {
        FileOutputStream outFileStream = new FileOutputStream(new File(archivePath));
        IOUtils.copy(inputStream, outFileStream);
    }

    /**
     * Extracts a given zip archive
     *
     * @param archiveFilePath path of the zip archive
     * @param destination     extract location
     * @return name of the extracted zip archive
     * @throws IOException if an error occurs while extracting the archive
     */
    private String extractArchive(String archiveFilePath, String destination)
            throws IOException, APIManagementException {
        String archiveName = null;

        try (ZipFile zip = new ZipFile(new File(archiveFilePath))) {
            Enumeration zipFileEntries = zip.entries();
            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                archiveName = zip.getName();

                File destinationFile = new File(destination, currentEntry);
                File destinationParent = destinationFile.getParentFile();
                String canonicalizedDestinationFilePath = destinationFile.getCanonicalPath();
                String canonicalizedDestinationFolderPath = new File(destination).getCanonicalPath();
                if (!canonicalizedDestinationFilePath.startsWith(canonicalizedDestinationFolderPath)) {
                    String errorMessage ="Attempt to upload invalid zip archive with file at " + currentEntry +
                            ". File path is outside target directory.";
                    log.error(errorMessage);
                    throw new APIManagementException(errorMessage);
                }

                // create the parent directory structure
                if (destinationParent.mkdirs()) {
                    log.debug("Creation of folder is successful. Directory Name : " + destinationParent.getName());
                }
                if (!entry.isDirectory()) {
                    try (InputStream zipInputStream = zip.getInputStream(entry);
                         BufferedInputStream inputStream = new BufferedInputStream(zipInputStream);
                         // write the current file to the destination
                         FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                        IOUtils.copy(inputStream, outputStream);
                    }
                }
            }
            return archiveName;
        }
    }

    /**
     * Create directory with unique uuid as name
     *
     * @param path Path to the directory
     */
    public static String directoryCreator(String path) throws APIManagementException {
        String tempDirPath = System.getProperty(path) + File.separator + UUID.randomUUID().toString();
        File file = new File(tempDirPath);
        file.mkdir();
        return tempDirPath;
    }
}
