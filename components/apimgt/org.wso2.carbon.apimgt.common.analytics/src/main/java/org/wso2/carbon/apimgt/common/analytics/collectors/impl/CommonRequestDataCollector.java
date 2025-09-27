/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.common.analytics.collectors.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.analytics.Constants;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Application;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.wso2.carbon.apimgt.common.analytics.Constants.EMAIL_PROP_TYPE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.IPV4_MASK_VALUE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.IPV4_PROP_TYPE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.IPV6_MASK_VALUE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.IPV6_PROP_TYPE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.MASK_VALUE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.USERNAME_PROP_TYPE;

/**
 * Contain the common data collectors.
 */
public abstract class CommonRequestDataCollector extends AbstractRequestDataCollector {
    private static final Log log = LogFactory.getLog(CommonRequestDataCollector.class);

    public CommonRequestDataCollector(AnalyticsDataProvider provider) {
        super(provider);
    }

    public Application getAnonymousApp() {
        Application application = new Application();
        application.setApplicationId(Constants.ANONYMOUS_VALUE);
        application.setApplicationName(Constants.ANONYMOUS_VALUE);
        application.setKeyType(Constants.ANONYMOUS_VALUE);
        application.setApplicationOwner(Constants.ANONYMOUS_VALUE);
        return application;
    }

    public Application getUnknownApp() {
        Application application = new Application();
        application.setApplicationId(Constants.UNKNOWN_VALUE);
        application.setApplicationName(Constants.UNKNOWN_VALUE);
        application.setKeyType(Constants.UNKNOWN_VALUE);
        application.setApplicationOwner(Constants.UNKNOWN_VALUE);
        return application;
    }

    public static String getTimeInISO(long time) {
        OffsetDateTime offsetDateTime = OffsetDateTime
                .ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC.normalized());
        return offsetDateTime.toString();
    }

    /**
     * Masks sensitive analytics data based on the provided type.
     * The method supports masking for IPv4, IPv6, email addresses, and usernames,
     * returning appropriately formatted and partially masked strings.
     *
     * @param type  the type of data to be masked. Supported types include:
     *              "IPV4", "IPV6", "EMAIL", "USERNAME".
     * @param value the actual value to be masked. Must be of type String.
     * @return the masked string value based on the specified type.
     * Returns a fully masked string for unrecognized types or null if the value is not a String.
     */
    public String maskAnalyticsData(String type, Object value) {
        if (log.isDebugEnabled()) {
            log.debug("Masking analytics data of type: " + type);
        }
        if (value instanceof String) {
            switch (type) {
                case IPV4_PROP_TYPE:
                    String[] octets = value.toString().split("\\.");

                    // Sample output: 192.168.***.98
                    return octets[0] + "." + octets[1] + "." + IPV4_MASK_VALUE + "." + octets[3];
                case IPV6_PROP_TYPE:
                    octets = value.toString().split(":");

                    // Sample output: 2001:0db8:85a3:****:****:****:****:7334
                    return octets[0] + ":" + octets[1] + ":" + octets[2] + ":" + IPV6_MASK_VALUE + ":" + IPV6_MASK_VALUE
                            + ":" + IPV6_MASK_VALUE + ":" + IPV6_MASK_VALUE + ":" + octets[7];
                case EMAIL_PROP_TYPE:
                    String[] email = value.toString().split("@");

                    // Sample output: *****@gmail.com
                    return MASK_VALUE + "@" + email[1];
                case USERNAME_PROP_TYPE:
                    return MASK_VALUE;
                default:
                    // Sample output: ********
                    return MASK_VALUE;
            }
        }
        return null;
    }
}
