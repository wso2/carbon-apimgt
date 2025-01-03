/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.notifier.events;

public class LLMProviderEvent extends Event {

    private String id;
    private String name;
    private String apiVersion;
    private String configuration;

    public LLMProviderEvent(String eventId, long timeStamp, String type, int tenantId, String tenantDomain,
                            String id, String name, String apiVersion, String configuration) {

        this.eventId = eventId;
        this.timeStamp = timeStamp;
        this.type = type;
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.id = id;
        this.name = name;
        this.apiVersion = apiVersion;
        this.configuration = configuration;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getApiVersion() {

        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {

        this.apiVersion = apiVersion;
    }

    public String getConfiguration() {

        return configuration;
    }

    public void setConfiguration(String configuration) {

        this.configuration = configuration;
    }
}
