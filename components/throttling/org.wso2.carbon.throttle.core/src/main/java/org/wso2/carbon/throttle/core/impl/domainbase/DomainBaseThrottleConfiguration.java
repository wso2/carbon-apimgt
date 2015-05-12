/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.throttle.core.impl.domainbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.throttle.core.CallerConfiguration;
import org.wso2.carbon.throttle.core.ThrottleConfiguration;
import org.wso2.carbon.throttle.core.ThrottleConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Holds all the controlling policy parameter for all domain names
 */

public class DomainBaseThrottleConfiguration implements ThrottleConfiguration {

    private static Log log = LogFactory.getLog(DomainBaseThrottleConfiguration.class.getName());

    /* The key for "Other" configuration  */
    private String keyOfOther;

    /* The default configuration for a throttle and this will apply to
     callersMap that have not a custom configuration */
    private CallerConfiguration defaultCallerConfiguration;

    /* To hold configurations */
    private Map configurationsMap;

    public DomainBaseThrottleConfiguration() {
        this.configurationsMap = new HashMap();
    }

    /**
     * To get a DomainBaseCallerConfiguration - if a configuration for given key found ,it returns ,
     * other wise , the default configuration will return.
     *
     * @param ID - The Remote caller id (domain name)
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
     * To add a DomainBaseCallerConfiguration
     *
     * @param configuration - The configuration of the caller
     */
    public void addCallerConfiguration(CallerConfiguration configuration) {
        String key = configuration.getID();
        if (key == null) {
            return;
        }
        key = key.trim();
        if (key.equals(ThrottleConstants.KEY_OF_DEFAULT_CONFIGURATION_FOR_OTHER)) {
            keyOfOther = ThrottleConstants.KEY_OF_DEFAULT_CONFIGURATION_FOR_OTHER;
            defaultCallerConfiguration = configuration;
        } else {
            configurationsMap.put(key, configuration);
        }
    }

    /**
     * To get key for caller configuration
     * if there is a configuration with callerID , it returns
     * otherwise ,
     * on the first the ID contains one or more "." ,then recursively
     * try to find the nearest root callerID.
     * else if the ID doesn't contains ".", try to find a key with *.{ID} .
     * Note : For valid ID , it should contain only  zero or more  "*."  as a prefix.
     * example:
     * (if ID is a.a.a), check pattern *.a.a and a.a
     * then *.*.a or *.a or a  )
     * if ID a then check start with *.a
     *
     * @param callerID The id of the remote caller (callerID name)
     * @return String value -String representation of  corrected epr-key for get configuration
     */
    public String getConfigurationKeyOfCaller(String callerID) {

        if (callerID != null) {
            callerID = callerID.trim();
            //if there is a unique Domain
            if (configurationsMap.containsKey(callerID)) {
                return callerID;
            } else {
                int index = callerID.indexOf(".");
                if (index > 0) {
                    String rootDomain = callerID.substring(index + 1, callerID.length());
                    if (rootDomain != null) {
                        String all = "*." + rootDomain;
                        Set keyset = configurationsMap.keySet();
                        if (keyset != null && !keyset.isEmpty()) {
                            for (Iterator iter = keyset.iterator(); iter.hasNext();) {
                                String key = (String) iter.next();
                                if (key != null && key.endsWith(all)) {
                                    return key;
                                }
                            }
                        }
                        return getConfigurationKeyOfCaller(rootDomain);
                    }
                } else {
                    String all = "*." + callerID;
                    Set keyset = configurationsMap.keySet();
                    if (keyset != null && !keyset.isEmpty()) {
                        for (Iterator iter = keyset.iterator(); iter.hasNext();) {
                            String key = (String) iter.next();
                            if (key != null && key.endsWith(all)) {
                                return key;
                            }
                        }
                    }
                }
            }
        }
        return keyOfOther;
    }

    public int getType() {
        return ThrottleConstants.DOMAIN_BASE;
    }

    public static void main(String[] args) {
        new DomainBaseThrottleConfiguration().getCallerConfiguration("www.abc.co.uk");
    }

}
