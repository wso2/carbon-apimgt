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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class generates ETag hash value for the given files of the zip
 * using MD5 as the hashing algorithm.
 */
public class ETagValueGenerator {
    private static final Log log = LogFactory.getLog(ETagValueGenerator.class);

    /**
     * This generates the ETag value using the given algorithm
     *
     * @param files the List of File objects included in zip
     * @return String
     */
    public static String getETag(List<File> files) {
        String eTagValue = null;
        try {
            eTagValue = getHash(files);
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Failed to generate E-Tag due to " + e.getMessage(), e);
        }
        return eTagValue;
    }


    /**
     * This generates the hash value using the MD5 algorithm
     *
     * @param files, the List of File objects included in zip
     * @return String
     * @throws NoSuchAlgorithmException if the given algorithm is invalid or not found in {@link MessageDigest}
     */
    private static String getHash(List<File> files) throws NoSuchAlgorithmException, IOException {

        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
        String checksum = getFileChecksum(md5Digest, files.get(0)) + ":" + getFileChecksum(md5Digest, files.get(1));
        return getMd5(checksum);
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

    /**
     * Method returns the hashed value for the String using MD5 hashing as default
     *
     * @param input the any String value
     * @return String
     * @throws NoSuchAlgorithmException if the given algorithm is invalid or not found in {@link MessageDigest}
     */
    private static String getMd5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);

        StringBuilder hashtext = new StringBuilder(no.toString(16));
        while (hashtext.length() < 32) {
            hashtext.insert(0, "0");
        }
        return hashtext.toString();
    }


}
