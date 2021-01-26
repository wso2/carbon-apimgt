/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dto;

/**
 * This class represents a Uploaded File
 */
public class UploadedFileInfoDTO {
    private String tenantDomain;
    private String fileName;
    private long timeStamp;

    /**
     * Constructor
     *
     * @param tenantDomain tenant domain
     * @param fileName  name of the file
     * @param timeStamp Timestamp of the file creation
     */
    public UploadedFileInfoDTO(String tenantDomain, String fileName, long timeStamp) {
        this.tenantDomain = tenantDomain;
        this.fileName = fileName;
        this.timeStamp = timeStamp;
    }

    /**
     * Get the tenant domain set by a constructor
     * @return String tenant domain
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Get the file name set by a constructor
     * @return String name of the file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     *
     * Get the time stamp set by a constructor
     * @return long Timestamp of the file creation
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Generates a key based on the composite primary key value
     *
     * @return String key
     */
    public String getKey() {
        return tenantDomain + ":" + fileName;
    }

    @Override
    public String toString() {
        return "[ Tenant : " + tenantDomain + ", FileName : " + fileName + ", TimeStamp : " + timeStamp + "]";
    }
}
