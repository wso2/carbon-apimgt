/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.impl;

import org.wso2.apk.apimgt.api.model.APIStore;
import org.wso2.apk.apimgt.impl.dto.DatasourceProperties;
import org.wso2.apk.apimgt.impl.dto.ThrottleProperties;
import org.wso2.apk.apimgt.impl.dto.WorkflowProperties;
import org.wso2.apk.apimgt.impl.monetization.MonetizationConfigurationDto;
import org.wso2.apk.apimgt.api.model.Environment;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationHolder {

    private Map<String, List<String>> configuration = new ConcurrentHashMap<>();

    private ThrottleProperties throttleProperties = new ThrottleProperties();

    private DatasourceProperties datasourceProperties = new DatasourceProperties();

    private WorkflowProperties workflowProperties = new WorkflowProperties();

    private Map<String, Environment> apiGatewayEnvironments = new LinkedHashMap<>();

    private static Map<String, String> analyticsProperties;

    private MonetizationConfigurationDto monetizationConfigurationDto = new MonetizationConfigurationDto();

    private Set<APIStore> externalAPIStores = new HashSet<>();

    public ThrottleProperties getThrottleProperties() {
        return throttleProperties;
    }

    public void setThrottleProperties(ThrottleProperties throttleProperties) {
        //TODO: Read configs and assign
        this.throttleProperties = throttleProperties;
    }

    public DatasourceProperties getDatasourceProperties() {
        return datasourceProperties;
    }

    public void setDatasourceProperties(DatasourceProperties datasourceProperties) {
        //TODO: Read configs and assign
        this.datasourceProperties = datasourceProperties;
    }

    public String getFirstProperty(String key) {

        List<String> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

    public List<String> getProperty(String key) {

        return configuration.get(key);
    }

    public MonetizationConfigurationDto getMonetizationConfigurationDto() {

        return monetizationConfigurationDto;
    }

    public void setMonetizationConfigurationDto(MonetizationConfigurationDto monetizationConfigurationDto) {

        this.monetizationConfigurationDto = monetizationConfigurationDto;
    }

    public WorkflowProperties getWorkflowProperties() {

        return workflowProperties;
    }

    public void setWorkflowProperties(WorkflowProperties workflowProperties) {

        this.workflowProperties = workflowProperties;
    }

    public Map<String, Environment> getApiGatewayEnvironments() {

        return apiGatewayEnvironments;
    }

    public void setApiGatewayEnvironments(Map<String, Environment> apiGatewayEnvironments) {

        this.apiGatewayEnvironments = apiGatewayEnvironments;
    }

    public Set<APIStore> getExternalAPIStores() {

        return externalAPIStores;
    }

    public static Map<String, String> getAnalyticsProperties() {

        return analyticsProperties;
    }
}
