/*
* Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/

package org.wso2.carbon.throttle.core.impl.ipbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.throttle.core.CallerConfiguration;
import org.wso2.carbon.throttle.core.ThrottleConfiguration;
import org.wso2.carbon.throttle.core.ThrottleConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class IPBaseThrottleConfiguration implements ThrottleConfiguration {

    private static Log log = LogFactory.getLog(IPBaseThrottleConfiguration.class.getName());

    /* The key for "Other" configuration */
    private String keyOfOther;

    /* The default configuration for a throttle and this will apply to callersMap
    that have not a custom configuration */
    private CallerConfiguration defaultCallerConfiguration;

    /* To hold configurations */
    private Map configurationsMap;

    public IPBaseThrottleConfiguration() {
        this.configurationsMap = new HashMap();
    }

    /**
     * To get a IPBaseCallerConfiguration - if a configuration for given key found ,it returns ,
     * other wise , the default configuration will return.
     *
     * @param ID - The Remote caller id (IP)
     * @return Returns the corresponding configuration for the caller with given ID
     */
    public CallerConfiguration getCallerConfiguration(String ID) {

        if (ID.equals(ThrottleConstants.KEY_OF_DEFAULT_CONFIGURATION_FOR_OTHER)) {
            return defaultCallerConfiguration;
        } else {
            String key = getConfigurationKeyOfCaller(ID);
            if (key != null) {
                if (key.equals(ThrottleConstants.KEY_OF_DEFAULT_CONFIGURATION_FOR_OTHER)) {
                    return defaultCallerConfiguration;
                }
                return (CallerConfiguration) configurationsMap.get(key);
            }
        }
        return null;
    }

    /**
     * To add a IPBaseCallerConfiguration
     *
     * @param configuration The configuration for a caller
     */
    public void addCallerConfiguration(CallerConfiguration configuration) {
        //TODO need to allow overlapping of ip with FirstPartOfIPRange
        IPBaseCallerConfiguration ipBaseCallerConfiguration = (IPBaseCallerConfiguration) configuration;
        String key = ipBaseCallerConfiguration.getFirstPartOfIPRange();
        if (key == null) {
            return;
        }
        key = key.trim();
        if (key.equals(ThrottleConstants.KEY_OF_DEFAULT_CONFIGURATION_FOR_OTHER)) {
            keyOfOther = ThrottleConstants.KEY_OF_DEFAULT_CONFIGURATION_FOR_OTHER;
            defaultCallerConfiguration = ipBaseCallerConfiguration;
        } else {
            configurationsMap.put(key, ipBaseCallerConfiguration);
        }
    }

    /**
     * To get key for access configuration
     *
     * @param callerID The remote caller id (ip)
     * @return Object-String representation of  corrected epr-key for get configuration
     */
    public String getConfigurationKeyOfCaller(String callerID) {

        if (callerID != null) {
            callerID = callerID.trim();
            //if there is a unique IP
            if (configurationsMap.containsKey(callerID)) {
                return callerID;
            } else {
                int index = callerID.lastIndexOf(".");
                if (index > 0) {
                    String net = callerID.substring(0, index);     // get the network portion
                    String host = callerID.substring(index + 1, callerID.length()); //get the host portion
                    if (net != null && host != null) {
                        Set keys = configurationsMap.keySet();
                        if (keys != null && !keys.isEmpty()) {
                            for (Iterator it = keys.iterator(); it.hasNext();) {
                                String key = (String) it.next();
                                if (key != null && key.startsWith(net) && isAfter(key, host)) {
                                    // all ips with in same network
                                    IPBaseCallerConfiguration con = (IPBaseCallerConfiguration)
                                            configurationsMap.get(key);
                                    if (con != null) {
                                        String secondPart = con.getSecondPartOfIPRange();
                                        if (secondPart != null && isBefore(secondPart, host)) {
                                            return key;
                                        }
                                    }
                                }
                            }
                            String all = net + ".*";
                            if (configurationsMap.containsKey(all)) {
                                return all;
                            }
                        }
                    }
                }
            }
        }
        return keyOfOther;
    }

    /**
     * Helper method to check the ip with the given host is exists  after the given  ip
     *
     * @param ip   The lower IP
     * @param host host
     * @return true if the ip with host is higher than given ip ,ow. false
     */
    private boolean isAfter(String ip, String host) {
        int index = ip.lastIndexOf(".");
        if (index > 0) {
            // host of first part of ip
            String hostfromip = ip.substring(index + 1, ip.length());
            if (hostfromip != null && host != null) {

                return toInt(hostfromip) <= toInt(host);
            }
        }
        return false;
    }

    /**
     * Helper method to check the ip with the given host is exists  before the given  ip
     *
     * @param ip   the higher ip
     * @param host host
     * @return true if the ip with host is lower than given ip ,ow. false
     */
    private boolean isBefore(String ip, String host) {
        int index = ip.lastIndexOf(".");
        if (index > 0) {
            // host of second part of ip
            String hostfromip = ip.substring(index + 1, ip.length());
            if (hostfromip != null && host != null) {

                return toInt(hostfromip) >= toInt(host);

            }
        }
        return false;
    }

    public int getType() {
        return ThrottleConstants.IP_BASE;
    }

    private int toInt(String str) {

        if (str == null || "".equals(str)) {
            handleException("Invalid string - null value");
        }

        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            handleException("Invalid string '" + str + "' , except an integer value ");
        }
        return -1;
    }

    private void handleException(String message) {
        String msg = "Error was occurred during ip(ip-range) processing  " + message;
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }
}
