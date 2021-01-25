package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;
/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ServiceEntryMappingUtil.fromFileToServiceCatalogInfo;
import static org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ServiceEntryMappingUtil.generateServiceKey;

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
            if (file.isDirectory()) { //else ignore zip
                File[] fArray = file.listFiles();
                if (fArray != null) {
                    Arrays.sort(fArray, NameFileComparator.NAME_COMPARATOR);
                    String key = null;
                    for (File aFile : fArray) {
                        if (aFile.getName().startsWith(APIConstants.METADATA_FILE_NAME)) { //else!!
                            try {
                                ServiceEntry serviceEntry = fromFileToServiceCatalogInfo(aFile);
                                if (!StringUtils.isBlank(serviceEntry.getKey())) {
                                    key  = serviceEntry.getKey();
                                } else {
                                    key = generateServiceKey(serviceEntry);
                                }
                            } catch (IOException e) {
                                // Missing metadata throw Error!! ****add to definition
                                log.error("Failed to fetch metadata information from zip due to generate key" + e.getMessage(), e);
                            }
                        }
                    }
                    try {
                        endpoints.put(key, calculateHash(fArray));
                    } catch (NoSuchAlgorithmException | IOException e) {
                        log.error("Failed to generate MD5 Hash due to " + e.getMessage(), e);
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


}
