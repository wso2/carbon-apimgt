/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.api.model;

import java.io.InputStream;
import java.sql.Timestamp;

public class ServiceEntry {

    private String uuid = null;
    private String key = null;
    private String md5 = null;
    private String name = null;
    private String version = null;
    private String displayName = null;
    private String serviceUrl = null;
    private String definitionType = null;
    private String defUrl = null;
    private String description = null;

    public enum SecurityType {
        BASIC, DIGEST, OAUTH2, NONE
    }
    private SecurityType securityType = SecurityType.NONE;
    private boolean mutualSSLEnabled = false;
    private String createdBy = null;
    private String updatedBy = null;
    private Timestamp createdTime = null;
    private Timestamp lastUpdatedTime = null;
    private InputStream endpointDef = null;
    private InputStream metadata = null;

    public InputStream getEndpointDef() {
        return endpointDef;
    }

    public void setEndpointDef(InputStream endpointDef) {
        this.endpointDef = endpointDef;
    }

    public InputStream getMetadata() {
        return metadata;
    }

    public void setMetadata(InputStream metadata) {
        this.metadata = metadata;
    }

    public Timestamp getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Timestamp lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isMutualSSLEnabled() {
        return mutualSSLEnabled;
    }

    public void setMutualSSLEnabled(boolean mutualSSLEnabled) {
        this.mutualSSLEnabled = mutualSSLEnabled;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(String definitionType) {
        this.definitionType = definitionType;
    }

    public String getDefUrl() {
        return defUrl;
    }

    public void setDefUrl(String defUrl) {
        this.defUrl = defUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SecurityType getSecurityType() {
        return securityType;
    }

    public void setSecurityType(SecurityType securityType) {
        this.securityType = securityType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
}
