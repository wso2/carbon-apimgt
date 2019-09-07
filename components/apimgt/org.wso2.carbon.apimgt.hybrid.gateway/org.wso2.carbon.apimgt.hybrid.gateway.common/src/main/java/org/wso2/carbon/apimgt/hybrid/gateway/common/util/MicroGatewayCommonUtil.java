/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.util;

import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This class contains common utility methods used in Micro Gateway components
 */
public class MicroGatewayCommonUtil {

    private static final String ALPHA_NUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Generates a Secure Alpha Numeric Random string of the given length
     *
     * @param length length of the String
     * @return Secure Random String
     */
    public static String getRandomString(int length) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHA_NUMERIC_CHARS.charAt(rnd.nextInt(ALPHA_NUMERIC_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * This method overwrites the char array to clean up password
     *
     * @param passwordCharArray char array of password
     */
    public static void cleanPasswordCharArray(char[] passwordCharArray) {
        Arrays.fill(passwordCharArray, '0');
    }

    /**
     * This method returns the URL value for the String value
     *
     * @param urlValue URL value
     * @return URL value
     * @throws OnPremiseGatewayException if failed to get the URL
     */
    public static URL getURLFromStringUrlValue(String urlValue) throws OnPremiseGatewayException {

        String errorMessage = "Error while retrieving URL from the string url value: ";
        try {
            URL url = new URL(urlValue);
            if (url != null) {
                return url;
            }
        } catch (MalformedURLException e) {
            throw new OnPremiseGatewayException(errorMessage + urlValue, e);
        }
        throw new OnPremiseGatewayException(errorMessage + urlValue);
    }
}
