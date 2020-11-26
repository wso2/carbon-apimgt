/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.importexport.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * This is the util class which consists of all the common functions for importing and exporting API.
 */
public class CommonUtil {
    private static final Log log = LogFactory.getLog(CommonUtil.class);

    /**
     * Create directory at the given path.
     *
     * @param path Path of the directory
     * @throws APIImportExportException If directory creation failed
     */
    public static void createDirectory(String path) throws APIImportExportException {

        if (path != null) {
            File file = new File(path);
            if (!file.exists() && !file.mkdirs()) {
                String errorMessage = "Error while creating directory : " + path;
                throw new APIImportExportException(errorMessage);
            }
        }
    }

    /**
     * Create temporary directory in temporary location.
     *
     * @throws APIImportExportException If an error occurs while creating temporary location
     */
    public static File createTempDirectory(Identifier identifier) throws APIImportExportException {

        String currentDirectory = System.getProperty(APIConstants.JAVA_IO_TMPDIR);
        String createdDirectories;
        if (identifier != null) {
            createdDirectories = File.separator + identifier.toString() + File.separator;
        } else {
            createdDirectories = File.separator + RandomStringUtils
                    .randomAlphanumeric(APIImportExportConstants.TEMP_FILENAME_LENGTH) + File.separator;
        }
        File tempDirectory = new File(currentDirectory + createdDirectories);
        createDirectory(tempDirectory.getPath());
        return tempDirectory;
    }

    /**
     * Archive a provided source directory to a zipped file.
     *
     * @param sourceDirectory Source directory
     * @throws APIImportExportException If an error occurs while generating archive
     */
    public static void archiveDirectory(String sourceDirectory) throws APIImportExportException {

        File directoryToZip = new File(sourceDirectory);
        List<File> fileList = new ArrayList<>();
        getAllFiles(directoryToZip, fileList);
        writeArchiveFile(directoryToZip, fileList);

        if (log.isDebugEnabled()) {
            log.debug("Archived API generated successfully from source: " + sourceDirectory);
        }
    }

    /**
     * Recursively retrieve all the files included in the source directory to be archived.
     *
     * @param sourceDirectory Source directory
     * @param fileList        List of files
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

    /**
     * Generate archive file.
     *
     * @param directoryToZip Location of the archive
     * @param fileList       List of files to be included in the archive
     * @throws APIImportExportException If an error occurs while adding files to the archive
     */
    private static void writeArchiveFile(File directoryToZip, List<File> fileList) throws APIImportExportException {

        try (FileOutputStream fileOutputStream = new FileOutputStream(directoryToZip.getPath() + ".zip");
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            for (File file : fileList) {
                if (!file.isDirectory()) {
                    addToArchive(directoryToZip, file, zipOutputStream);
                }
            }
        } catch (IOException e) {
            String errorMessage = "I/O error while adding files to archive";
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Add files of the directory to the archive.
     *
     * @param directoryToZip  Location of the archive
     * @param file            File to be included in the archive
     * @param zipOutputStream Output stream
     * @throws APIImportExportException If an error occurs while writing files to the archive
     */
    private static void addToArchive(File directoryToZip, File file, ZipOutputStream zipOutputStream)
            throws APIImportExportException {

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // Get relative path from archive directory to the specific file
            String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1);
            if (File.separatorChar != APIImportExportConstants.ZIP_FILE_SEPARATOR) {
                zipFilePath = zipFilePath.replace(File.separatorChar, APIImportExportConstants.ZIP_FILE_SEPARATOR);
            }
            ZipEntry zipEntry = new ZipEntry(zipFilePath);
            zipOutputStream.putNextEntry(zipEntry);

            IOUtils.copy(fileInputStream, zipOutputStream);

            zipOutputStream.closeEntry();
        } catch (IOException e) {
            String errorMessage = "I/O error while writing files to archive";
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * Write content to file.
     *
     * @param path    Location of the file
     * @param content Content to be written
     * @throws APIImportExportException If an error occurs while writing to file
     */
    public static void writeFile(String path, String content) throws APIImportExportException {

        try (FileWriter writer = new FileWriter(path)) {
            IOUtils.copy(new StringReader(content), writer);
        } catch (IOException e) {
            String errorMessage = "I/O error while writing to file: " + path;
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * This method checks whether a given file exists in a given location.
     *
     * @param fileLocation location of the file
     * @return true if the file exists, false otherwise
     */
    public static boolean checkFileExistence(String fileLocation) {

        File testFile = new File(fileLocation);
        return testFile.exists();
    }

    /**
     * Converts a YAML file into JSON.
     *
     * @param yaml yaml representation
     * @return yaml file as a json
     * @throws IOException If an error occurs while converting YAML to JSON
     */
    public static String yamlToJson(String yaml) throws IOException {

        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);

        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    /**
     * Converts JSON to YAML.
     *
     * @param json json representation
     * @return json file as a yaml document
     * @throws IOException If an error occurs while converting JSON to YAML
     */
    public static String jsonToYaml(String json) throws IOException {

        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory()
                .enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER));
        JsonNode jsonNodeTree = yamlReader.readTree(json);
        YAMLMapper yamlMapper = new YAMLMapper()
                .disable(YAMLGenerator.Feature.SPLIT_LINES)
                .enable(YAMLGenerator.Feature.INDENT_ARRAYS)
                .disable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS);
        return yamlMapper.writeValueAsString(jsonNodeTree);
    }

    /**
     * This method uploads a given file to specified location
     *
     * @param uploadedInputStream input stream of the file
     * @param newFileName         name of the file to be created
     * @param storageLocation     destination of the new file
     * @throws APIImportExportException If the file transfer fails
     */
    public static void transferFile(InputStream uploadedInputStream, String newFileName, String storageLocation)
            throws APIImportExportException {

        try (FileOutputStream outFileStream = new FileOutputStream(new File(storageLocation, newFileName))) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outFileStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            String errorMessage = "Error in transferring files.";
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * This method decompresses API the archive.
     *
     * @param sourceFile  The archive containing the API
     * @param destination location of the archive to be extracted
     * @return Name of the extracted directory
     * @throws APIImportExportException If the decompressing fails
     */
    public static String extractArchive(File sourceFile, String destination) throws APIImportExportException {

        String archiveName = null;
        try (ZipFile zip = new ZipFile(sourceFile)) {

            Enumeration zipFileEntries = zip.entries();
            int index = 0;

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {

                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                //This index variable is used to get the extracted folder name; that is root directory
                if (index == 0) {
                    archiveName = currentEntry.substring(0, currentEntry.indexOf(
                            APIImportExportConstants.ZIP_FILE_SEPARATOR));
                    --index;
                }

                File destinationFile = new File(destination, currentEntry);
                File destinationParent = destinationFile.getParentFile();
                String canonicalizedDestinationFilePath = destinationFile.getCanonicalPath();
                if (!canonicalizedDestinationFilePath.startsWith(new File(destination).getCanonicalPath())) {
                    String errorMessage = "Attempt to upload invalid zip archive with file at " + currentEntry +
                            ". File path is outside target directory";
                    throw new APIImportExportException(errorMessage);
                }

                // create the parent directory structure
                if (destinationParent.mkdirs()) {
                    log.info("Creation of folder is successful. Directory Name : " + destinationParent.getName());
                }

                if (!entry.isDirectory()) {
                    // write the current file to the destination
                    try (InputStream zipInputStream = zip.getInputStream(entry);
                         BufferedInputStream inputStream = new BufferedInputStream(zipInputStream);
                         FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                        IOUtils.copy(inputStream, outputStream);
                    }
                }
            }
            return archiveName;
        } catch (IOException e) {
            String errorMessage = "Failed to extract the archive (zip) file. ";
            throw new APIImportExportException(errorMessage, e);
        }
    }

    /**
     * This method will be used to generate Endpoint certificates and meta information related to endpoint certs
     *
     * @param filePath String of new file path
     * @param content  String of content to write into the file
     * @throws IOException If an error occurs when generating new certs and yaml file
     */
    public static void generateFiles(String filePath, String content) throws IOException {
        FileOutputStream fos = null;
        File file;
        try {
            //Specify the file path here
            file = new File(filePath);
            fos = new FileOutputStream(file);

            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] bytesArray = content.getBytes();

            fos.write(bytesArray);
            fos.flush();

        } catch (IOException e) {
            String errorMessage = "Error while generating meta information of client certificates from path.";
            throw new IOException(errorMessage, e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                String errorMessage = "Error while generating meta information of client certificates from path.";
                throw new IOException(errorMessage, e);
            }
        }
    }


    /**
     * This method will be used to copy files from source to destination
     *
     * @param source String of the source file path
     * @param dest  String of the destination file path
     * @throws IOException If an error occurs when copying files
     */
    public static void moveFile(String source, String dest ) throws IOException {
        try {
             Files.move(Paths.get(source), Paths.get(dest));
        } catch (IOException e) {
            String errorMessage = "Error while moving file from" + source + "to" + dest;
            throw new IOException(errorMessage, e);
        }
    }
}
