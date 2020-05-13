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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Manager class for File System based Application Import and Export handling
 */
public class FileBasedApplicationImportExportManager extends ApplicationImportExportManager {
    private static final Log log = LogFactory.getLog(FileBasedApplicationImportExportManager.class);
    private static final String IMPORTED_APPLICATIONS_DIRECTORY_NAME = "imported-applications";
    private String path;
    private APIConsumer apiConsumer;

    public FileBasedApplicationImportExportManager(APIConsumer apiConsumer, String path) {
        super(apiConsumer);
        this.apiConsumer = apiConsumer;
        this.path = path;
    }

    /**
     * Export a given Application to a file system as zip archive.
     * The export root location is given by {@link FileBasedApplicationImportExportManager#path}/exported-application.
     *
     * @param exportApplication   Application{@link Application} to be exported
     * @param exportDirectoryName Name of directory to be exported
     * @return path to the exported directory with exported artifacts
     * @throws APIManagementException if an error occurs while exporting an application to a file system
     */
    public String exportApplication(Application exportApplication, String exportDirectoryName) throws
            APIManagementException {

        String applicationArtifactBaseDirectoryPath = path + File.separator + exportDirectoryName;
        try {
            Files.createDirectories(Paths.get(applicationArtifactBaseDirectoryPath));
        } catch (IOException e) {
            String errorMsg = "Unable to create the directory for export Application at :"
                    + applicationArtifactBaseDirectoryPath;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
        Set<SubscribedAPI> subscriptions = apiConsumer.getSubscribedAPIs(exportApplication.getSubscriber(),
                exportApplication.getName(), exportApplication.getGroupId());
        exportApplication.setSubscribedAPIs(subscriptions);
        String applicationExportDirectory = applicationArtifactBaseDirectoryPath + File.separator +
                exportApplication.getName();
        try {
            //create directory per application
            Files.createDirectories(Paths.get(applicationExportDirectory));
            //export application details
            exportApplicationDetailsToFileSystem(exportApplication,
                    applicationExportDirectory);
        } catch (IOException e) {
            log.error("Error while exporting Application: " + exportApplication.getName(), e);
        }
        // Check if no application is exported
        try {
            if (getDirectoryList(applicationArtifactBaseDirectoryPath).isEmpty()) {
                // cleanup the archive root directory
                FileUtils.deleteDirectory(new File(path));
            }
        } catch (IOException e) {
            String errorMsg = "Unable to find Application Details at: " + applicationArtifactBaseDirectoryPath;
            throw new APIManagementException(errorMsg);
        }
        return applicationArtifactBaseDirectoryPath;
    }

    /**
     * Import a given Application from an InputStream
     *
     * @param uploadedAppArchiveInputStream Content stream of the zip file which contains exported Application
     * @return the imported application
     * @throws APIManagementException if an error occurs while importing an application archive
     */
    public Application importApplication(InputStream uploadedAppArchiveInputStream) throws APIManagementException {
        String appArchiveLocation = path + File.separator + IMPORTED_APPLICATIONS_DIRECTORY_NAME +
                APIConstants.ZIP_FILE_EXTENSION;
        String archiveExtractLocation;
        try {
            archiveExtractLocation = extractUploadedArchiveApplication(uploadedAppArchiveInputStream,
                    IMPORTED_APPLICATIONS_DIRECTORY_NAME,
                    appArchiveLocation, path);
            return parseApplicationFile(archiveExtractLocation);
        } catch (IOException e) {
            String errorMsg = "Error occurred while importing Application archive" + appArchiveLocation;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Extracts the Application to the file system by reading the incoming {@link InputStream} object
     * uploadedApplicationArchiveInputStream
     *
     * @param uploadedAppArchiveInputStream Incoming {@link InputStream}
     * @param importedDirectoryName         directory to extract the archive
     * @param appArchiveLocation            full path of the archive location
     * @param extractLocation               full path to the location to which the archive will be written
     * @return location to which Applications were extracted
     * @throws IOException if an error occurs while extracting the archive
     */
    private String extractUploadedArchiveApplication(InputStream uploadedAppArchiveInputStream,
                                                     String importedDirectoryName,
                                                     String appArchiveLocation, String extractLocation)
            throws IOException, APIManagementException {
        String archiveExtractLocation;
        String archiveName;
        String extractedFilePath;
        // create import directory structure
        Files.createDirectories(Paths.get(extractLocation));
        // create archive
        createArchiveFromInputStream(uploadedAppArchiveInputStream, appArchiveLocation);
        // extract the archive
        archiveExtractLocation = extractLocation + File.separator + importedDirectoryName;
        archiveName = extractArchive(appArchiveLocation, archiveExtractLocation);
        extractedFilePath = archiveExtractLocation + File.separator + archiveName + File.separator +
                archiveName + APIConstants.JSON_FILE_EXTENSION;
        return extractedFilePath;
    }

    /**
     * Extracts the details of an Application from a json file
     *
     * @param applicationDetailsFilePath Directory which contains the json file
     * @return an application object containing the details extracted from the json file
     * @throws IOException if an error occurs while extracting the Application Details
     */
    private Application parseApplicationFile(String applicationDetailsFilePath)
            throws IOException {
        String applicationDetailsString;
        applicationDetailsString = new String(Files.readAllBytes(Paths.get(applicationDetailsFilePath)),
                StandardCharsets.UTF_8);
        //convert to bean
        Gson gson = new GsonBuilder().create();
        //returns an application object from a json string
        return gson.fromJson(applicationDetailsString, Application.class);
    }

    /**
     * Creates an archive of the contained application details.
     *
     * @param sourceDirectory Directory which contains source file
     * @param archiveLocation Directory to generate the zip archive
     * @param archiveName     Name of the zip archive
     * @return path to the created archive file
     * @throws APIManagementException if an error occurs while creating an archive from app details
     */
    public String createArchiveFromExportedAppArtifacts(String sourceDirectory, String archiveLocation,
                                                        String archiveName) throws APIManagementException {
        String archivedFilePath;
        try {
            archiveDirectory(sourceDirectory, archiveLocation, archiveName);
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
        return archivedFilePath;
    }

    /**
     * write the given Application details to file system
     *
     * @param application    {@link Application} object to be exported
     * @param exportLocation file system location to write the Application Details
     * @throws IOException if an error occurs while writing the Application Details
     */
    private void exportApplicationDetailsToFileSystem(Application application, String exportLocation)
            throws IOException {
        String applicationFileLocation = exportLocation + File.separator + application.getName() +
                APIConstants.JSON_FILE_EXTENSION;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileOutputStream fileOutputStream = new FileOutputStream(applicationFileLocation);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,
                     StandardCharsets.UTF_8)) {
            gson.toJson(application, outputStreamWriter);
        }
    }

    /**
     * Creates a zip archive from of a directory
     *
     * @param sourceDirectory directory to create zip archive from
     * @param archiveLocation path to the archive location, excluding archive name
     * @param archiveName     name of the archive to create
     * @throws IOException if an error occurs while creating the archive
     */
    private void archiveDirectory(String sourceDirectory, String archiveLocation, String archiveName)
            throws IOException {
        File directoryToZip = new File(sourceDirectory);
        List<File> fileList = new ArrayList<>();
        getAllFiles(directoryToZip, fileList);
        writeArchiveFile(directoryToZip, fileList, archiveLocation, archiveName);
        if (log.isDebugEnabled()) {
            log.debug("Archive generated successfully " + archiveName);
        }
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
     * Queries the list of directories available under a root directory path
     *
     * @param path full path of the root directory
     * @return Set of directory path under the root directory given by path
     * @throws IOException if an error occurs while listing directories
     */
    private Set<String> getDirectoryList(String path) throws IOException {
        Set<String> directoryNames = new HashSet<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(path))) {
            for (Path directoryPath : directoryStream) {
                directoryNames.add(directoryPath.toString());
            }
        }
        return directoryNames;
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
            int index = 0;
            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                //This index variable is used to get the extracted folder name; that is root directory
                if (index == 0) {
                    archiveName = currentEntry.substring(0, currentEntry.indexOf('/'));
                    --index;
                }
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
}
