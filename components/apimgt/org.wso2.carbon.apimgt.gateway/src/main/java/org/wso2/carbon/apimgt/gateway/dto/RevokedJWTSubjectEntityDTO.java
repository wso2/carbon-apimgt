/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO of revoked JWT Subject Entity.
 */
public class RevokedJWTSubjectEntityDTO {

    @SerializedName("entity_id")
    private String entityId;
    @SerializedName("entity_type")
    private String entityType;
    @SerializedName("revocation_time")
    private Long revocationTime;
    @SerializedName("organization")
    private String organization;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getRevocationTime() {
        return revocationTime;
    }

    public void setRevocationTime(Long revocationTime) {
        this.revocationTime = revocationTime;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
