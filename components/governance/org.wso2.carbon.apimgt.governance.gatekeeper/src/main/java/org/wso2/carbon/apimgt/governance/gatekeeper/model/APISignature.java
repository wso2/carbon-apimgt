/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.gatekeeper.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model class representing an API MinHash signature.
 */
public class APISignature implements Serializable {

    private static final long serialVersionUID = 1L;

    private String apiUuid;
    private byte[] signatureBlob;
    private String organization;
    private Timestamp createdTime;
    private Timestamp updatedTime;

    // Optional metadata for conflict reports
    private String apiName;
    private String apiVersion;
    private String apiContext;

    /**
     * Default constructor.
     */
    public APISignature() {
    }

    /**
     * Constructor with essential fields.
     *
     * @param apiUuid       The API UUID
     * @param signatureBlob The signature as byte array
     * @param organization  The organization
     */
    public APISignature(String apiUuid, byte[] signatureBlob, String organization) {
        this.apiUuid = apiUuid;
        this.signatureBlob = signatureBlob != null ? signatureBlob.clone() : null;
        this.organization = organization;
    }

    public String getApiUuid() {
        return apiUuid;
    }

    public void setApiUuid(String apiUuid) {
        this.apiUuid = apiUuid;
    }

    public byte[] getSignatureBlob() {
        return signatureBlob != null ? signatureBlob.clone() : null;
    }

    public void setSignatureBlob(byte[] signatureBlob) {
        this.signatureBlob = signatureBlob != null ? signatureBlob.clone() : null;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Timestamp getCreatedTime() {
        return createdTime != null ? new Timestamp(createdTime.getTime()) : null;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime != null ? new Timestamp(createdTime.getTime()) : null;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime != null ? new Timestamp(updatedTime.getTime()) : null;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime != null ? new Timestamp(updatedTime.getTime()) : null;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    @Override
    public String toString() {
        return "APISignature{" +
                "apiUuid='" + apiUuid + '\'' +
                ", organization='" + organization + '\'' +
                ", apiName='" + apiName + '\'' +
                ", signatureLength=" + (signatureBlob != null ? signatureBlob.length : 0) +
                '}';
    }
}
