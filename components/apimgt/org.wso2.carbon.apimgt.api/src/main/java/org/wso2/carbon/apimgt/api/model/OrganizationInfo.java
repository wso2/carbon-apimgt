/*
 *  Copyright (c) 2025, WSO2 LLc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.api.model;

public class OrganizationInfo {

    private String superOrganization;
    private String name;
    private String id;
    private String organizationId;
    private OrganizationInfo parentOrganization;
    private OrganizationInfo[] childOrganizations;

    public String getSuperOrganization() {
        return superOrganization;
    }

    public void setSuperOrganization(String superOrganization) {
        this.superOrganization = superOrganization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OrganizationInfo getParentOrganization() {
        return parentOrganization;
    }

    public void setParentOrganization(OrganizationInfo parentOrganization) {
        this.parentOrganization = parentOrganization;
    }

    public OrganizationInfo[] getChildOrganizations() {
        return childOrganizations;
    }

    public void setChildOrganizations(OrganizationInfo[] childOrganizations) {
        this.childOrganizations = childOrganizations;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
