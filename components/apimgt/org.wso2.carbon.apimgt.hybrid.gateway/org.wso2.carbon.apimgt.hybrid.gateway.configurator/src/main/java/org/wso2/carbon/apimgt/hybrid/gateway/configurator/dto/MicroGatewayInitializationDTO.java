/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.apimgt.hybrid.gateway.configurator.dto;

import java.util.Map;

/**
 *  DTO to store micro gateway details
 */
public class MicroGatewayInitializationDTO {

    private String tenantDomain = null;
    private String macAddress = null;
    private String port = null;
    private String hostName = null;
    private String gwUrl = null;
    private String label = null;
    private Map<String, String> envMetadataMap = null;
    private Map<String, String> customMetadataMap = null;

    /**
     * Retrieve tenant domain
     *
     * @return String
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Assign tennat domain
     *
     * @param tenantDomain String
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * Retireve mac address
     *
     * @return String
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Assign mac address
     *
     * @param macAddress String
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Retrieve port
     *
     * @return String
     */
    public String getPort() {
        return port;
    }

    /**
     * Assign port
     *
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Retrieve host name
     *
     * @return String
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Assign host name
     *
     * @param hostName String
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Get the label configured for this micro GW instance. Can be null if no label
     * is configured.
     *
     * @return label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the label configured for this GW.
     *
     * @param label configured label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get system environment specific metadata
     *
     * @return envMetadataMap environment metadata
     */
    public Map<String, String> getEnvMetadataMap() {
        return envMetadataMap;
    }

    /**
     * Set the system environment specific metadata
     *
     * @param envMetadataMap environment metadata
     */
    public void setEnvMetadataMap(Map<String, String> envMetadataMap) {
        this.envMetadataMap = envMetadataMap;
    }

    /**
     * Get the custom configured metadata (which are not related to system environments)
     *
     * @return customMetadataMap custom metadata
     */
    public Map<String, String> getCustomMetadataMap() {
        return customMetadataMap;
    }

    /**
     * Set the custom configured metadata (which are not related to system environments)
     *
     * @param customMetadataMap custom metadata
     */
    public void setCustomMetadataMap(Map<String, String> customMetadataMap) {
        this.customMetadataMap = customMetadataMap;
    }
}
