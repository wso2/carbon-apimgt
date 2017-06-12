/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.FileApi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Util class for API file based operations.
 */
public class APIFileUtils {

    private static final Logger log = LoggerFactory.getLogger(APIFileUtils.class);

    // Private constructor because this is a utility class with static methods. No point of initializing.
    private APIFileUtils() {

    }

    /**
     * Creates a file
     *
     * @param location full path to create the file
     * @throws APIMgtDAOException if an error occurs while extracting the file
     */
    public static void createFile(String location) throws APIMgtDAOException {
        try {
            Files.createFile(Paths.get(location));
        } catch (IOException e) {
            String msg = "Error in creating file at: " + location;
            log.error(msg, e);
            throw new APIMgtDAOException(msg, e);
        }
    }

    /**
     * Creates a directory
     *
     * @param path path of the directory to create
     * @throws APIMgtDAOException if an error occurs while creating the directory
     */
    public static void createDirectory(String path) throws APIMgtDAOException {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            String msg = "Error in creating directory at: " + path;
            log.error(msg, e);
            throw new APIMgtDAOException(msg, e);
        }
    }

    /**
     * Creates a file writing the content of the object as json.
     *
     * @param object   object to be written as JSON.
     * @param filePath full path to create the file
     * @throws APIMgtDAOException if an error occurs while writing the object as json to file.
     */
    public static void writeObjectAsJsonToFile(Object object, String filePath) throws APIMgtDAOException {
        Gson gson = new Gson();

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,
                     StandardCharsets.UTF_8)) {
            gson.toJson(object, outputStreamWriter);
        } catch (IOException e) {
            String msg = "Error while writing the object to the file path " + filePath;
            log.error(msg, e);
            throw new APIMgtDAOException(msg, e);
        }
    }

    /**
     * Creates a file writing the content of the string as json.
     *
     * @param content  string content to be written as JSON.
     * @param filePath full path to create the file
     * @throws APIMgtDAOException if an error occurs while writing the string as json to file.
     */
    public static void writeStringAsJsonToFile(String content, String filePath) throws APIMgtDAOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(content).getAsJsonObject();
        writeToFile(filePath, gson.toJson(json));
    }

    /**
     * Writes the string content to a file
     *
     * @param path    path of the file to be written.
     * @param content Content to be written.
     * @throws APIMgtDAOException if an error occurs while writing to file
     */
    public static void writeToFile(String path, String content) throws APIMgtDAOException {
        try {
            Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            String msg = "I/O error while writing to file at: " + path;
            log.error(msg, e);
            throw new APIMgtDAOException(msg, e);
        }
    }

    /**
     * Read contents of a file as text
     *
     * @param path full path of the file to read
     * @return text content of the file
     * @throws APIMgtDAOException if an error occurs while reading the file
     */
    public static String readFileContentAsText(String path) throws APIMgtDAOException {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            String msg = "Error while reading file " + path;
            log.error(msg, e);
            throw new APIMgtDAOException(msg, e);
        }
    }

    /**
     * Find a file in file system
     *
     * @param file File to start searching
     * @param name File name to search
     * @return absolute path of the file
     */
    public static String findInFileSystem(File file, String name) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    String found = findInFileSystem(f, name);
                    if (found != null) {
                        return found;
                    }
                }
            }
        } else {
            if (file.getName().contains(name)) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Delete a given directory
     *
     * @param path Path to the directory to be deleted
     * @throws APIMgtDAOException if unable to delete the directory
     */
    public static void deleteDirectory(String path) throws APIMgtDAOException {
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            String errorMsg = "Error while deleting directory : " + path;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Delete a given file
     *
     * @param path Path to the file to be deleted
     * @throws APIMgtDAOException if unable to delete the file
     */
    public static void deleteFile(String path) throws APIMgtDAOException {
        try {
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            String errorMsg = "Error while deleting file : " + path;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * write the given API definition to file system
     *
     * @param api            {@link FileApi} object to be exported
     * @param exportLocation file system location to write the API definition
     * @throws APIMgtDAOException if an error occurs while writing the API definition
     */
    public static void exportApiDefinitionToFileSystem(FileApi api, String exportLocation) throws APIMgtDAOException {
        String apiFileLocation = exportLocation + File.separator + APIMgtConstants.APIFileUtilConstants
                .API_DEFINITION_FILE_PREFIX + api.getId() + APIMgtConstants.APIFileUtilConstants.JSON_EXTENSION;
        APIFileUtils.writeObjectAsJsonToFile(api, apiFileLocation);
        if (log.isDebugEnabled()) {
            log.debug("Successfully saved API definition for api: " + api.getName() + ", version: " + api.getVersion());
        }
    }

    /**
     * write the given Endpoint definition to file system
     *
     * @param swaggerDefinition swagger definition
     * @param api               {@link API} instance relevant to the swagger definition
     * @param exportLocation    file system location to which the swagger definition will be written
     * @throws APIMgtDAOException if an error occurs while writing the Endpoint
     */
    public static void exportSwaggerDefinitionToFileSystem(String swaggerDefinition, API api, String exportLocation)
            throws APIMgtDAOException {
        String swaggerDefinitionLocation = exportLocation + File.separator + APIMgtConstants.APIFileUtilConstants
                .SWAGGER_DEFINITION_FILE_PREFIX + api.getId() + APIMgtConstants.APIFileUtilConstants.JSON_EXTENSION;
        APIFileUtils.writeStringAsJsonToFile(swaggerDefinition, swaggerDefinitionLocation);

        if (log.isDebugEnabled()) {
            log.debug("Successfully exported Swagger definition for api: " + api.getName() + ", version: " + api
                    .getVersion());
        }
    }

    /**
     * write the given Endpoint definition to file system
     * `
     *
     * @param endpoint       {@link Endpoint} object to be exported
     * @param exportLocation file system location to write the Endpoint
     * @throws APIMgtDAOException if an error occurs while writing the Endpoint
     */
    public static void exportEndpointToFileSystem(Endpoint endpoint, String exportLocation) throws APIMgtDAOException {
        String endpointFileLocation = exportLocation + File.separator + endpoint.getName() + APIMgtConstants
                .APIFileUtilConstants.JSON_EXTENSION;
        APIFileUtils.writeObjectAsJsonToFile(endpoint, endpointFileLocation);

        if (log.isDebugEnabled()) {
            log.debug("Successfully saved endpoint  definition for endpoint: " + endpoint.getName());
        }
    }

    /**
     * write the given API gateway config to file system
     *
     * @param config         gateway config of the api
     * @param api            {@link API} instance
     * @param exportLocation file system location to write the API gateway config.
     * @throws APIMgtDAOException if an error occurs while writing the API definition
     */
    public static void exportGatewayConfigToFileSystem(String config, API api, String exportLocation)
            throws APIMgtDAOException {
        if (config == null) {
            // not gateway config found, return
            log.warn("No gateway configuration found for API with api: " + api.getName() + ", version: " + api
                    .getVersion());
            return;
        }
        String gatewayConfigLocation = exportLocation + File.separator + APIMgtConstants.APIFileUtilConstants
                .GATEWAY_CONFIGURATION_DEFINITION_FILE;
        APIFileUtils.writeToFile(gatewayConfigLocation, config);
        if (log.isDebugEnabled()) {
            log.debug("Successfully exported gateway configuration for api: " + api.getName() + ", version: " + api
                    .getVersion());
        }
    }

    /**
     * Writes the API thumbnail to file system
     *
     * @param thumbnailInputStream {@link InputStream} instance with thumbnail data
     * @param exportLocation       file system location to which the thumbnail will be written
     * @throws APIMgtDAOException if unable to export thumbnail
     */
    public static void exportThumbnailToFileSystem(InputStream thumbnailInputStream, String exportLocation)
            throws APIMgtDAOException {
        String thumbnailFileLocation = exportLocation + File.separator + APIMgtConstants.APIFileUtilConstants
                .THUMBNAIL_FILE_NAME;
        try {
            APIFileUtils.createFile(thumbnailFileLocation);
            APIFileUtils.writeStreamToFile(thumbnailFileLocation, thumbnailInputStream);

        } catch (APIMgtDAOException e) {
            String errorMsg = "Unable to export the thumbnail to file " + exportLocation;
            log.error(errorMsg, e);
            APIFileUtils.deleteFile(thumbnailFileLocation);
            throw (e);
        }
    }

    /**
     * Writes date read from {@link InputStream} to a file
     *
     * @param path        full path of the file to write
     * @param inputStream {@link InputStream} instance
     * @throws APIMgtDAOException if an error occurs while writing to the file
     */
    public static void writeStreamToFile(String path, InputStream inputStream) throws APIMgtDAOException {
        try (FileOutputStream outputStream = new FileOutputStream(path)) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            String errorMsg = "Unable to write to file at path: " + path;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Create an {@link InputStream} instance by reading a file
     *
     * @param path full path of the file to read
     * @return {@link InputStream} instance with file data
     * @throws APIMgtDAOException if an error occurs while reading the file
     */
    public static InputStream readFileContentAsStream(String path) throws APIMgtDAOException {
        try {
            return new FileInputStream(path);
        } catch (IOException e) {
            String errorMsg = "Error while reading file " + path;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Creates a zip archive from the given {@link InputStream} inputStream
     *
     * @param inputStream {@link InputStream} instance
     * @param archivePath path to create the zip archive
     * @throws APIMgtDAOException if an error occurs while creating the archive
     */
    public static void createArchiveFromInputStream(InputStream inputStream, String archivePath)
            throws APIMgtDAOException {
        try (FileOutputStream outFileStream = new FileOutputStream(new File(archivePath))) {
            IOUtils.copy(inputStream, outFileStream);
        } catch (IOException e) {
            String errorMsg = "Error in Creating archive from data.";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }

    }

    /**
     * Extracts a a given zip archive
     *
     * @param archiveFilePath path of the zip archive
     * @param destination     extract location
     * @return name of the extracted zip archive
     * @throws APIMgtDAOException if an error occurs while extracting the archive
     */
    public static String extractArchive(String archiveFilePath, String destination)
            throws APIMgtDAOException {
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

                // create the parent directory structure
                if (destinationParent.mkdirs()) {
                    log.debug("Creation of folder is successful. Directory Name : " + destinationParent.getName());
                }

                if (!entry.isDirectory()) {
                    writeFileToDestination(zip, entry, destinationFile);
                }
            }
            return archiveName;
        } catch (IOException e) {
            String errorMsg = "Failed to extract archive file: " + archiveFilePath + " to destination: " + destination;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Retrieves thumbnail as a binary stream from the file
     *
     * @param thumbnailFilePath path to file
     * @return thumbnail as a {@link InputStream} instance
     * @throws APIMgtDAOException if an error occurs when getting thumbnail
     */
    public static InputStream getThumbnailImage(String thumbnailFilePath) throws APIMgtDAOException {
        File thumbnailFile = new File(thumbnailFilePath);
        if (!thumbnailFile.exists()) {
            return null;
        }
        return APIFileUtils.readFileContentAsStream(thumbnailFilePath);

    }

    /**
     * Utility method to get API directory path
     *
     * @param basePath path where the apis are saved
     * @param api      api to get details
     * @return Directory path of the api
     */
    public static String getAPIBaseDirectory(String basePath, FileApi api) {
        return basePath + File.separator + api.getProvider() + "-" + api.getName() + "-" + api.getVersion();
    }

    /**
     * Extracts the APIs to the file system by reading the incoming {@link InputStream} object
     * uploadedApiArchiveInputStream
     *
     * @param uploadedApiArchiveInputStream Incoming {@link InputStream}
     * @param importedDirectoryName         directory to extract the archive
     * @param apiArchiveLocation            full path of the archive location
     * @param extractLocation               full path to the location to which the archive will be written
     * @return location to which APIs were extracted
     * @throws APIMgtDAOException if an error occurs while extracting the archive
     */
    public static String extractUploadedArchive(InputStream uploadedApiArchiveInputStream, String importedDirectoryName,
            String apiArchiveLocation, String extractLocation) throws APIMgtDAOException {
        String archiveExtractLocation;
        try {
            // create api import directory structure
            APIFileUtils.createDirectory(extractLocation);
            // create archive
            createArchiveFromInputStream(uploadedApiArchiveInputStream, apiArchiveLocation);
            // extract the archive
            archiveExtractLocation = extractLocation + File.separator + importedDirectoryName;
            extractArchive(apiArchiveLocation, archiveExtractLocation);

        } catch (APIMgtDAOException e) {
            APIFileUtils.deleteDirectory(extractLocation);
            String errorMsg = "Error in accessing uploaded API archive";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
        return archiveExtractLocation;
    }

    /**
     * Creates a zip archive from of a directory
     *
     * @param sourceDirectory directory to create zip archive from
     * @param archiveLocation path to the archive location, excluding archive name
     * @param archiveName     name of the archive to create
     * @throws APIMgtDAOException if an error occurs while creating the archive
     */
    public static void archiveDirectory(String sourceDirectory, String archiveLocation, String archiveName)
            throws APIMgtDAOException {

        File directoryToZip = new File(sourceDirectory);

        List<File> fileList = new ArrayList<>();
        getAllFiles(directoryToZip, fileList);
        try {
            writeArchiveFile(directoryToZip, fileList, archiveLocation, archiveName);

        } catch (IOException e) {
            String errorMsg = "Error while writing archive file " + directoryToZip.getPath() + " to archive " +
                    archiveLocation;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Archived API generated successfully" + archiveName);
        }

    }

    /**
     * Queries the list of directories available under a root directory path
     *
     * @param path full path of the root directory
     * @return Set of directory path under the root directory given by path
     * @throws APIMgtDAOException if an error occurs while listing directories
     */
    public static Set<String> getDirectoryList(String path) throws APIMgtDAOException {
        Set<String> directoryNames = new HashSet<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(path))) {
            for (Path directoryPath : directoryStream) {
                directoryNames.add(directoryPath.toString());
            }
        } catch (IOException e) {
            String errorMsg = "Error while listing directories under " + path;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
        return directoryNames;
    }

    public static Collection<File> searchFilesWithMatchingExtension(File folder, String extension) {
        return FileUtils.listFiles(folder, new String[] {extension}, true);
    }

    /**
     * Queries all files under a directory recursively
     *
     * @param sourceDirectory full path to the root directory
     * @param fileList        list containing the files
     */
    private static void getAllFiles(File sourceDirectory, List<File> fileList) {
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

    private static void writeArchiveFile(File directoryToZip, List<File> fileList, String archiveLocation,
            String archiveName) throws IOException {

        try (FileOutputStream fileOutputStream = new FileOutputStream(archiveLocation + File.separator + archiveName
                + ".zip");
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            for (File file : fileList) {
                if (!file.isDirectory()) {
                    addToArchive(directoryToZip, file, zipOutputStream);
                }
            }
        }
    }

    private static void addToArchive(File directoryToZip, File file, ZipOutputStream zipOutputStream)
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

    private static void writeFileToDestination(ZipFile zip, ZipEntry entry, File destinationFile) throws IOException {
        try (InputStream zipInputStream = zip.getInputStream(entry);
             BufferedInputStream inputStream = new BufferedInputStream(zipInputStream);
             // write the current file to the destination
             FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

}
