/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.client;

import com.google.gson.annotations.SerializedName;

public class CloudManagerEnvTemplate {

    @SerializedName("id")
    private String id;
    @SerializedName("env_name")
    private String name;
    @SerializedName("organization_uuid")
    private String organizationUUID;
    @SerializedName("external_apim_env_name")
    private String externalAPIMEnvironment;
    @SerializedName("internal_apim_env_name")
    private String internalAPIMEnvironment;
    @SerializedName("sandbox_apim_env_name")
    private String sandboxAPIMEnvironment;
    @SerializedName("critical")
    private boolean critical;
    @SerializedName("choreo_env")
    private String dataplaneType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizationUUID() {
        return organizationUUID;
    }

    public void setOrganizationUUID(String organizationUUID) {
        this.organizationUUID = organizationUUID;
    }

    public String getExternalAPIMEnvironment() {
        return externalAPIMEnvironment;
    }

    public void setExternalAPIMEnvironment(String externalAPIMEnvironment) {
        this.externalAPIMEnvironment = externalAPIMEnvironment;
    }

    public String getInternalAPIMEnvironment() {
        return internalAPIMEnvironment;
    }

    public void setInternalAPIMEnvironment(String internalAPIMEnvironment) {
        this.internalAPIMEnvironment = internalAPIMEnvironment;
    }

    public String getSandboxAPIMEnvironment() {
        return sandboxAPIMEnvironment;
    }

    public void setSandboxAPIMEnvironment(String sandboxAPIMEnvironment) {
        this.sandboxAPIMEnvironment = sandboxAPIMEnvironment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public String getDataplaneType() {
        return dataplaneType;
    }

    public void setDataplaneType(String dataplaneType) {
        this.dataplaneType = dataplaneType;
    }
}
