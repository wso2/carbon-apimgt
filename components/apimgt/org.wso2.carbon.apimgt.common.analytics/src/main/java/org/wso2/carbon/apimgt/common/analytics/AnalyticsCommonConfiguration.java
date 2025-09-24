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

import java.util.Map;

/**
 * AnalyticsCommonConfiguration hold the common configurations.
 */
public class AnalyticsCommonConfiguration {
    private static final Log log = LogFactory.getLog(AnalyticsCommonConfiguration.class);
    private final Map<String, String> configurations;
    private String responseSchema;
    private String faultSchema;

    public AnalyticsCommonConfiguration(Map<String, String> configurations) {
        this.configurations = configurations;
    }

    public Map<String, String> getConfigurations() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving analytics configurations");
        }
        return configurations;
    }

    public String getResponseSchema() {
        return responseSchema;
    }

    public void setResponseSchema(String responseSchema) {
        if (log.isDebugEnabled()) {
            log.debug("Setting response schema: " + responseSchema);
        }
        this.responseSchema = responseSchema;
    }

    public String getFaultSchema() {
        return faultSchema;
    }

    public void setFaultSchema(String faultSchema) {
        if (log.isDebugEnabled()) {
            log.debug("Setting fault schema: " + faultSchema);
        }
        this.faultSchema = faultSchema;
    }
}
