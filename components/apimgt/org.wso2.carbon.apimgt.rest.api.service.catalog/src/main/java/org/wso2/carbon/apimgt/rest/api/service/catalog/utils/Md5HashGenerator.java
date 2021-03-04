/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ServiceEntryMappingUtil.fromFileToServiceEntry;

/**
 * This class generates MD5 hash value for the given files of the zip
 * using MD5 as the hashing algorithm.
 */
public class Md5HashGenerator {
    private static final Log log = LogFactory.getLog(Md5HashGenerator.class);
    private static HashMap<String, String> endpoints = new HashMap<String, String>();

    public String getHashForEndPoint(String endPointName)
    {
        return endpoints.get(endPointName);
    }

    /**
     * This generates the md5 hash value using the given algorithm
     *
     * @param path files available directory location
     * @return String
     */
    public static HashMap<String, String> generateHash(String path) {

        File dir = new File(path);
        return validateInputParams(Objects.requireNonNull(dir.listFiles()));
    }

    /**
     * This traversal through the directories and calculate md5
     *
     * @param files list of files available directory location
     * @return HashMap<String, String>
     */
    private static HashMap<String, String> validateInputParams(File[] files) {

        for (File file : files) {
            if (file.isDirectory()) {
                // Else is not required since this for taking only the directories. If it is an empty zip, then
                // the final map will be empty.
                File[] fileArray = file.listFiles();
                if (fileArray != null) {
                    // Order files according to the alphabetical order of file names when concatenating hashes.
                    Arrays.sort(fileArray, NameFileComparator.NAME_COMPARATOR);
                    String key = null;
                    for (File yamlFile : fileArray) {
                        if (yamlFile.getName().startsWith(APIConstants.METADATA_FILE_NAME)) {
                            // This if only check whether the file start with the name "metadata".
                            try {
                                ServiceEntry service = fromFileToServiceEntry(yamlFile, null);
                                key = service.getKey();
                            } catch (IOException e) {
                                RestApiUtil.handleBadRequest("Failed to fetch metadata information", log);
                            }
                        }
                    }
                    try {
                        endpoints.put(key, calculateHash(fileArray));
                    } catch (NoSuchAlgorithmException | IOException e) {
                        RestApiUtil.handleInternalServerError("Failed to generate MD5 Hash due to " +
                                e.getMessage(), log);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        RestApiUtil.handleBadRequest("Required metadata.yaml or definition.yaml is missing", log);
                    }
                }
            }
        }
        return endpoints;
    }


    /**
     * This generates the hash value for files using the MD5 algorithm
     *
     * @param files, the List of File objects included in zip
     * @return String
     * @throws NoSuchAlgorithmException if the given algorithm is invalid or not found in {@link MessageDigest}
     */
    private static String calculateHash(File[] files) throws NoSuchAlgorithmException, IOException {

        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
        return getFileChecksum(md5Digest, files[0]) + getFileChecksum(md5Digest, files[1]);
    }

    /**
     * Method returns the hashed value for the file using MD5 hashing as default
     *
     * @param file the updated/created time of the resource in UNIX time
     * @return String
     * @throws IOException if the given algorithm is invalid or not found in {@link MessageDigest}
     */
    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);

        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();

        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    static String calculateMD5Hash(byte[] content) {
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md5Digest.digest(content);
            StringBuilder sb = new StringBuilder();
            for (byte md5byte : md5Bytes) {
                sb.append(Integer.toString((md5byte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            RestApiUtil.handleInternalServerError("Error when calculating the MD5 hash", log);
            return null;
        }
    }

}
