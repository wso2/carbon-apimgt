/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class ApiMetadataProjectDto {
    private String apiFile;
    private Set<EnvironmentDto> environments = new HashSet<>();
    private String organizationId;
    private String version;
    private String apiContext;

    public String getApiContext() { return apiContext; }

    public void setApiContext(String apiContext) { this.apiContext = apiContext; }

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getApiFile() {
        return apiFile;
    }

    public void setApiFile(String apiFile) {
        this.apiFile = apiFile;
    }

    public Set<EnvironmentDto> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<EnvironmentDto> environments) {
        this.environments = environments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ApiMetadataProjectDto)) {
            return false;
        }

        ApiMetadataProjectDto apiMetadataProjectDto = (ApiMetadataProjectDto) obj;
        // check only file name
        return StringUtils.equals(this.apiFile, apiMetadataProjectDto.apiFile);
    }

    @Override
    public int hashCode() {
        return (apiFile == null ? 0 : apiFile.hashCode());
    }

}
