/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.spec.parser.definitions;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    private static final Log log = LogFactory.getLog(FileUtil.class);

    /**
     * Extracts an uploaded API archive to a given directory.
     *
     * @param uploadedApiArchiveInputStream Input stream of the uploaded archive.
     * @param importedDirectoryName         Name of the directory to extract into.
     * @param apiArchiveLocation            Path to temporarily save the archive.
     * @param extractLocation               Directory where the archive will be extracted.
     * @return Path to the extracted archive directory.
     * @throws APIManagementException If extraction fails.
     */
    public static String extractUploadedArchive(InputStream uploadedApiArchiveInputStream, String importedDirectoryName,
                                                String apiArchiveLocation, String extractLocation)
            throws APIManagementException {
        String archiveExtractLocation;
        try {
            // create api import directory structure
            createDirectory(extractLocation);
            // create archive
            createArchiveFromInputStream(uploadedApiArchiveInputStream, apiArchiveLocation);
            // extract the archive
            archiveExtractLocation = extractLocation + File.separator + importedDirectoryName;
            extractArchive(apiArchiveLocation, archiveExtractLocation);
        } catch (APIManagementException e) {
            deleteDirectory(extractLocation);
            String errorMsg = "Error in accessing uploaded API archive";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
        return archiveExtractLocation;
    }

    /**
     * Creates a directory at the specified path.
     *
     * @param path the directory path to create
     * @throws APIManagementException if an I/O error occurs during directory creation
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
     * Deletes the directory at the specified path.
     *
     * @param path the path of the directory to be deleted
     * @throws APIManagementException if an error occurs while deleting the directory
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
     * Creates an archive file at the specified path using data from the given input stream.
     *
     * @param inputStream the input stream containing the data to be written to the archive
     * @param archivePath the path where the archive file will be created
     * @throws APIManagementException if an I/O error occurs during the archive creation
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
     * Extracts the contents of a ZIP archive to the specified destination directory.
     *
     * @param archiveFilePath the full path to the ZIP archive file
     * @param destination     the directory where the archive contents will be extracted
     * @return the name of the root directory inside the ZIP archive
     * @throws APIManagementException if an I/O error occurs, the archive is invalid, or extraction limits are exceeded.
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
                    if (log.isDebugEnabled()) {
                        log.debug("Creating directory " + destinationFile.getAbsolutePath());
                    }
                    destinationFile.mkdir();
                    continue;
                }

                // create the parent directory structure
                if (destinationParent.mkdirs()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creation of folder is successful. Directory Name : " + destinationParent.getName());
                    }
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
}
