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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Util class for API file based operations.
 */
public class APIFileUtils {

    private static final Logger log = LoggerFactory.getLogger(APIFileUtils.class);
    private static final String ENDPOINT_DIRECTORY_NAME = "Endpoints";

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
     */
    public static void deleteDirectory(String path) {
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            log.error("Error while deleting directory : " + path, e);
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
            log.error("Error while deleting file : " + path, e);
        }
    }

    /**
     * write the given API definition to file system
     *
     * @param api            {@link API} object to be exported
     * @param exportLocation file system location to write the API definition
     * @throws APIMgtDAOException if an error occurs while writing the API definition
     */
    public static void exportApiDefinitionToFileSystem(API api, String exportLocation) throws APIMgtDAOException {
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
        String endpointFileLocation = exportLocation + File.separator + endpoint.getName() + "-" + endpoint.getId()
                + APIMgtConstants.APIFileUtilConstants.JSON_EXTENSION;
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
     */
    public static void exportThumbnailToFileSystem(InputStream thumbnailInputStream, String exportLocation)
            throws APIMgtDAOException {
        String thumbnailFileLocation = exportLocation + File.separator + APIMgtConstants.APIFileUtilConstants
                .THUMBNAIL_FILE_NAME;
        try {
            APIFileUtils.createFile(thumbnailFileLocation);
            APIFileUtils.writeStreamToFile(thumbnailFileLocation, thumbnailInputStream);

        } catch (APIMgtDAOException e) {
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
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(path);
            IOUtils.copy(inputStream, outputStream);
        } catch (FileNotFoundException e) {
            throw new APIMgtDAOException(e);

        } catch (IOException e) {
            throw new APIMgtDAOException("Unable to write to file at path: " + path, e);

        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
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
            throw new APIMgtDAOException("Error while reading file " + path, e);
        }
    }

    /**
     * Retrieves thumbnail as a binary stream from the file
     *
     * @param thumbnailFilePath path to file
     * @return thumbnail as a {@link InputStream} instance
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
    public static String getAPIBaseDirectory(String basePath, API api) {
        return basePath + File.separator + api.getProvider() + "-" + api.getName() + "-" + api.getVersion();
    }


}
