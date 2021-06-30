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

import java.util.Map;

/**
 * AnalyticsCommonConfiguration hold the common configurations.
 */
public class AnalyticsCommonConfiguration {
    private final Map<String, String> configurations;
    private String responseSchema;
    private String faultSchema;

    public AnalyticsCommonConfiguration(Map<String, String> configurations) {
        this.configurations = configurations;
    }

    public Map<String, String> getConfigurations() {
        return configurations;
    }

    public String getResponseSchema() {
        return responseSchema;
    }

    public void setResponseSchema(String responseSchema) {
        this.responseSchema = responseSchema;
    }

    public String getFaultSchema() {
        return faultSchema;
    }

    public void setFaultSchema(String faultSchema) {
        this.faultSchema = faultSchema;
    }
}
