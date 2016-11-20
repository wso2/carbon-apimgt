package org.wso2.carbon.apimgt.rest.api.util.utils;
/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class generates ETag hash value for the given timestamp of the resource
 * using MD5 as the default hashing algorithm.
 */
public class ETagGenerator {
    private static final Log log = LogFactory.getLog(ETagGenerator.class);
    /**
     * This generates the ETag value using the given algorithm
     * @param updatedTimeInMillis the updated/created time of the resource in UNIX time
     * @return String
     */
    private static String getETag(long updatedTimeInMillis) {
        String eTagValue=null;
        try {
            eTagValue= getHash(updatedTimeInMillis) ;
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate E-Tag due to " + e.getMessage(), e);
        }
        return eTagValue;
    }


    /**
     * If some other hashing algorithm is needed use this method.
     * @param updatedTimeInMillis, the updated/created time of the resource in UNIX time
     * @param algorithm the algorithm used for hashing
     * @return String
     * @throws NoSuchAlgorithmException if the given algorithm is invalid or not found in {@link MessageDigest}
     */
    private static String getHash(long updatedTimeInMillis, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(String.valueOf(updatedTimeInMillis).getBytes());
        byte[] digest = messageDigest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte aDigest : digest) {
            sb.append(Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1)); // not quite necessary
        }
        if (log.isDebugEnabled()){
            log.debug("ETag generated in HEX :: " + sb.toString());
        }
        return sb.toString();
    }

    /**
     * Method returns the hashed value for the updatedTimeInMillis using MD5 hashing as default
     * @param updatedTimeInMillis the updated/created time of the resource in UNIX time
     * @return String
     * @throws NoSuchAlgorithmException if the given algorithm is invalid or not found in {@link MessageDigest}
     */
    private static String getHash(long updatedTimeInMillis) throws NoSuchAlgorithmException {
        return getHash(updatedTimeInMillis, "MD5");
    }
    public static String getETag(String lastUpdatedTimeInMillis){
        try {
            return getETag(Long.parseLong(lastUpdatedTimeInMillis));
        }catch (NumberFormatException e){
            log.error("Error in ETagGenerator due to "+ e.getMessage(),e);
        }
        return null;
    }


}
