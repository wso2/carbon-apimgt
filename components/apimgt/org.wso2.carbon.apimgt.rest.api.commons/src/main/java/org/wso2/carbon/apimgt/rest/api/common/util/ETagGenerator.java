package org.wso2.carbon.apimgt.rest.api.common.util;
/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.common.exception.ETagGenerationException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class generates ETag hash value for the given timestamp of the resource
 * using MD5 as the default hashing algorithm.
 */
public class ETagGenerator {
    private static final Logger log = LoggerFactory.getLogger(ETagGenerator.class);
    
    private ETagGenerator () {
    }

    /**
     * If some other hashing algorithm is needed use this method.
     *
     * @param updatedTime, the updated/created time of the resource in UNIX time
     * @param algorithm            the algorithm used for hashing
     * @return String
     * @throws NoSuchAlgorithmException if the given algorithm is invalid or not found in {@link MessageDigest}
     * @throws ETagGenerationException if hash generation failed.
     */
    private static String getHash(String updatedTime, String algorithm)
            throws ETagGenerationException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        try {
            messageDigest.update(updatedTime.getBytes("UTF-8"));
            byte[] digest = messageDigest.digest();

            StringBuilder sb = new StringBuilder();
            for (byte aDigest : digest) {
                sb.append(Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1)); // not quite necessary
            }
            if (log.isDebugEnabled()) {
                log.debug("ETag generated in HEX :: " + sb.toString());
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            String errorMessage = "Error while converting timestamp to String :" + updatedTime;
            log.error(errorMessage, e);
            throw new ETagGenerationException(errorMessage, e);
        }
    }

    /**
     * Method returns the hashed value for the updatedTimeInMillis using MD5 hashing as default
     *
     * @param updatedTime the updated/created time of the resource in UNIX time
     * @return generated 
     * @throws ETagGenerationException if hash generation failed.
     */
    public static String getETag(String updatedTime) throws ETagGenerationException {
        try {
            return StringUtils.isBlank(updatedTime) ? null : getHash(updatedTime, "MD5");
        } catch (NoSuchAlgorithmException e) {
            String errorMessage = "Error while generating md5 hash for the timestamp :" + updatedTime;
            log.error(errorMessage, e);
            throw new ETagGenerationException(errorMessage, e);
        }
    }

}
