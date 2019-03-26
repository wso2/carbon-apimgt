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

/**
 *  DTO to store micro gateway details
 */
public class MicroGatewayInitializationDTO {

    private String tenantDomain = null;
    private String macAddress = null;
    private String port = null;
    private String hostName = null;

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

}
