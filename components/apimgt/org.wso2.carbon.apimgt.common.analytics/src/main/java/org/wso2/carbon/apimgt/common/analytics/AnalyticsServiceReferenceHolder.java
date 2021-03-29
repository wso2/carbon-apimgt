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
package org.wso2.carbon.apimgt.common.analytics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.analytics.publishers.impl.AnalyticsDataPublisher;

import java.util.Map;

/**
 * Configuration holder.
 */
public class AnalyticsServiceReferenceHolder {
    private static final Log log = LogFactory.getLog(AnalyticsServiceReferenceHolder.class);
    private static final AnalyticsServiceReferenceHolder instance = new AnalyticsServiceReferenceHolder();

    private Map<String, String> configurations;

    private AnalyticsServiceReferenceHolder() {

    }

    public static AnalyticsServiceReferenceHolder getInstance() {
        return instance;
    }

    public Map<String, String> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<String, String> configurations) {
        this.configurations = configurations;
        // initialize data publisher at server start up
        AnalyticsDataPublisher.getInstance().initialize(configurations);
        log.debug("Analytics data publisher initialized.");
    }
}
