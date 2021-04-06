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

/**
 * Contain the common data collectors.
 */
public abstract class CommonRequestDataCollector extends AbstractRequestDataCollector {
    private static final Log log = LogFactory.getLog(SuccessRequestDataCollector.class);

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
}
