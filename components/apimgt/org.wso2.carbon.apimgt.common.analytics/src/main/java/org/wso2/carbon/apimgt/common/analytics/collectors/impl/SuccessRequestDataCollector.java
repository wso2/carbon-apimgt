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
import org.wso2.carbon.apimgt.common.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.common.analytics.exceptions.AnalyticsException;
import org.wso2.carbon.apimgt.common.analytics.publishers.RequestDataPublisher;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.API;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Event;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.MetaInfo;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Target;
import org.wso2.carbon.apimgt.common.analytics.publishers.impl.SuccessRequestDataPublisher;

import java.util.Iterator;
import java.util.Map;

import static org.wso2.carbon.apimgt.common.analytics.Constants.EMAIL_MASK_VALUE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.EMAIL_PROP_TYPE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.IPV4_MASK_VALUE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.IPV4_PROP_TYPE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.IPV6_MASK_VALUE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.IPV6_PROP_TYPE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.USERNAME_MASK_VALUE;
import static org.wso2.carbon.apimgt.common.analytics.Constants.USERNAME_PROP_TYPE;

/**
 * Success request data collector.
 */
public class SuccessRequestDataCollector extends CommonRequestDataCollector implements RequestDataCollector {
    private static final Log log = LogFactory.getLog(SuccessRequestDataCollector.class);
    private RequestDataPublisher processor;
    private AnalyticsDataProvider provider;
    public SuccessRequestDataCollector(AnalyticsDataProvider provider, RequestDataPublisher processor) {
        super(provider);
        this.processor = processor;
        this.provider = provider;
    }

    public SuccessRequestDataCollector(AnalyticsDataProvider provider) {
        this(provider, new SuccessRequestDataPublisher());
    }

    public void collectData() throws AnalyticsException {
        log.debug("Handling success analytics types");

        long requestInTime = provider.getRequestTime();
        String offsetDateTime = getTimeInISO(requestInTime);

        Event event = new Event();
        event.setProperties(provider.getProperties());

        // Masking the configured data
        Map<String, String> maskData = provider.getMaskProperties();
        Iterator<Map.Entry<String, String>> iterator = maskData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            Map<String, Object> props = event.getProperties();
            if (props != null) {
                Object value = props.get(entry.getKey());
                if (value != null) {
                    String maskStr = maskAnalyticsData(entry.getValue(), value);
                    props.replace(entry.getKey(), maskStr);
                }
            }
        }

        API api = provider.getApi();
        Operation operation = provider.getOperation();
        Target target = provider.getTarget();

        Application application;
        if (provider.isAnonymous()) {
            application = getAnonymousApp();
        } else {
            application = provider.getApplication();
        }
        Latencies latencies = provider.getLatencies();
        MetaInfo metaInfo = provider.getMetaInfo();

        String userAgent = provider.getUserAgentHeader();
        String userName = provider.getUserName();

        // Mask UserName if configured
        if (userName != null) {
            if (maskData.containsKey("api.ut.userName")) {
                userName = maskAnalyticsData(maskData.get("api.ut.userName"), userName);
            } else if (maskData.containsKey("api.ut.userId")) {
                userName = maskAnalyticsData(maskData.get("api.ut.userId"), userName);
            }
        }

        String userIp = provider.getEndUserIP();
        if (userName == null) {
            userName = Constants.UNKNOWN_VALUE;
        }
        if (userIp == null) {
            userIp = Constants.UNKNOWN_VALUE;
        } else {
            // Mask User IP if configured
            if (maskData.containsKey("api.analytics.user.ip")) {
                userIp = maskAnalyticsData(maskData.get("api.analytics.user.ip"), userIp);
            }
        }
        if (userAgent == null) {
            userAgent = Constants.UNKNOWN_VALUE;
        } else {
            if (maskData.containsKey("api.analytics.user.agent")) {
                userAgent = maskAnalyticsData(maskData.get("api.analytics.user.agent"), userAgent);
            }
        }

        event.setApi(api);
        event.setOperation(operation);
        event.setTarget(target);
        event.setApplication(application);
        event.setLatencies(latencies);
        event.setProxyResponseCode(provider.getProxyResponseCode());
        event.setRequestTimestamp(offsetDateTime);
        event.setMetaInfo(metaInfo);
        event.setUserAgentHeader(userAgent);
        event.setUserName(userName);
        event.setUserIp(userIp);

        this.processor.publish(event);
    }

    private String maskAnalyticsData(String type, Object value) {
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
                    return EMAIL_MASK_VALUE + "@" + email[1];
                case USERNAME_PROP_TYPE:
                    return USERNAME_MASK_VALUE;
                default:
                    // Sample output: ********
                    return USERNAME_MASK_VALUE;
            }
        }
        return null;
    }

}
