/*
 * Copyright (c) 2025, WSO2 LLc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.api.dto;

public class OrganizationDetailsDTO {

    private String organizationId;
    private String externalOrganizationReference;
    private String Name;
    private String description;
    private String organizationHandle;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExternalOrganizationReference() {
        return externalOrganizationReference;
    }

    public void setExternalOrganizationReference(String externalOrganizationReference) {
        this.externalOrganizationReference = externalOrganizationReference;
    }

    public String getOrganizationHandle() {
        return organizationHandle;
    }

    public void setOrganizationHandle(String organizationHandle) {
        this.organizationHandle = organizationHandle;
    }

    @Override
    public String toString() {
        return "OrganizationDetailsDTO [organizationId=" + organizationId + ", externalOrganizationReference="
                + externalOrganizationReference + ", Name=" + Name + ", description=" + description
                + ", organizationHandle=" + organizationHandle + "]";
    }

}
