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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

        try (FileOutputStream fileOutStream = new FileOutputStream(path);
             StringReader stringReader = new StringReader(content);
             OutputStreamWriter writer = new OutputStreamWriter(fileOutStream, StandardCharsets.UTF_8);) {
            IOUtils.copy(stringReader, writer);
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
     * @throws APIMgtDAOException if an error occurs while deleting the directory
     */
    public static void deleteDirectory(String path) throws APIMgtDAOException {

        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            String msg = "Error while deleting directory : " + path;
            log.error(msg, e);
            throw new APIMgtDAOException(msg, e);
        }
    }

    /**
     * Delete a given file
     *
     * @param path Path to the file to be deleted
     * @throws APIMgtDAOException if an error occurs while deleting the directory
     */
    public static void deleteFile(String path) throws APIMgtDAOException {

        try {
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            String msg = "Error while deleting file : " + path;
            log.error(msg, e);
            throw new APIMgtDAOException(msg, e);
        }
    }
}
