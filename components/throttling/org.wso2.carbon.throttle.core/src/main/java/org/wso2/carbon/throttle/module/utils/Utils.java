/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.throttle.module.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;


public class Utils {

    private static Log log   = LogFactory.getLog(Utils.class);
    private static String  OAUTH_HEADER_SPLITTER = ",";
    private static String  OAUTH_CONSUMER_KEY = "oauth_consumer_key";

    /**
     * Check whether a given Ip is in a given ip range
     * @param address ip address to check
     * @param range  range ex: 192.168.2.2-192.168.2.100
     * @return true is in range , false if not
     * @throws java.net.UnknownHostException  If Host is not Valid or can't resolve it
     */
    public static boolean isIpInRange(String  address , String range) throws UnknownHostException {

        if(address == null || range == null) {
            return false;
        }

        String[] bounds = range.split("-");

        String ipLow = bounds[0].trim();

        String ipHigh  = bounds[1].trim();

        long ipLo = ipToLong(InetAddress.getByName(ipLow));
        long ipHi = ipToLong(InetAddress.getByName(ipHigh));
        long ipToTest = ipToLong(InetAddress.getByName(address));

        return ipToTest >= ipLo && ipToTest <= ipHi;
    }


    /**
     * Extract the customer key from the OAuth Authentication header
     * @param authHeader Header string
     * @return extracted customer key value
     */
    public static String extractCustomerKeyFromAuthHeader(String authHeader) {

        // Expected header :
        //OAuth oauth_consumer_key="nq21LN39VlKe6OezcOndBx",
        // oauth_signature_method="HMAC-SHA1", oauth_signature="DZKyT75hiOIdtMGCU%2BbITArs4sU%3D",
        // oauth_timestamp="1328590467", oauth_nonce="7031216264696", oauth_version="1.0"

        String  consumerKey = null;

        if (authHeader.startsWith("OAuth ") || authHeader.startsWith("oauth ")) {
            authHeader = authHeader.substring(authHeader.indexOf("o"));
        }

        String[] headers = authHeader.split(OAUTH_HEADER_SPLITTER);
        if (headers != null && headers.length > 0) {
            for (int i = 0; i < headers.length; i++) {
                String[] elements = headers[i].split("=");
                if (elements != null && elements.length > 1) {
                    if (OAUTH_CONSUMER_KEY.equals(elements[0].trim())) {
                        consumerKey = removeLeadingAndTrailing(elements[1].trim());
                    }
                }
            }
        }

        return consumerKey;
    }

    /**
     * Help to extract consumer key from OAuth header
     * @param base
     * @return
     */
    private static String removeLeadingAndTrailing(String base) {
        String result = base;

        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return result.trim();
    }

    private static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(isIpInRange("192.168.1.1","192.168.1.1-192.168.1.10"));
        System.out.println(isIpInRange("192.168.1.4","192.168.1.1-192.168.1.10"));
        System.out.println(isIpInRange("192.164.1.10","192.168.1.1-192.168.1.10"));
        System.out.println(isIpInRange("193.168.1.1","192.168.1.1-192.168.1.10"));


        System.out.println(extractCustomerKeyFromAuthHeader("oauth_consumer_key=\"nq21LN39VlKe6OezcOndBx\", oauth_signature_method=\"HMAC-SHA1\", oauth_signature=\"DZKyT75hiOIdtMGCU%2BbITArs4sU%3D\", oauth_timestamp=\"1328590467\", oauth_nonce=\"7031216264696\", oauth_version=\"1.0\""));
    }


}