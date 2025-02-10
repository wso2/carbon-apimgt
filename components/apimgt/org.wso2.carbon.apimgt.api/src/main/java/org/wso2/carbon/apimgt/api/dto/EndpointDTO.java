/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
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

/**
 * DTO object to represent endpoints.
 */
public class EndpointDTO {

    private String endpointUuid;
    private String endpointName;
    private String organization;
    private String environment;
    private String endpointType;
    private EndpointConfigDTO endpointConfig;

    public String getEndpointType() {

        return endpointType;
    }

    public void setEndpointType(String endpointType) {

        this.endpointType = endpointType;
    }

    /**
     * To get the tier name which the certificate is subscribed to.
     *
     * @return tier name.
     */
    public String getOrganization() {

        return organization;
    }

    /**
     * To set the subscription tier for the current certificate.
     *
     * @param organization Name of the tier.
     */
    public void setOrganization(String organization) {

        this.organization = organization;
    }

    /**
     * To get the alias of the certificate.
     *
     * @return alias of the certificate.
     */
    public String getEndpointUuid() {

        return endpointUuid;
    }

    /**
     * To set the alias of the certificate.
     *
     * @param endpointUuid Specific alias.
     */
    public void setEndpointUuid(String endpointUuid) {

        this.endpointUuid = endpointUuid;
    }

    /**
     * To get the certificate content.
     *
     * @return certificate content.
     */
    public String getEndpointName() {

        return endpointName;
    }

    /**
     * To set the certificate content.
     *
     * @param endpointName certificate content.
     */
    public void setEndpointName(String endpointName) {

        this.endpointName = endpointName;
    }

    public String getEnvironment() {

        return environment;
    }

    public void setEnvironment(String environment) {

        this.environment = environment;
    }

    public EndpointConfigDTO getEndpointConfig() {

        return endpointConfig;
    }

    public void setEndpointConfig(EndpointConfigDTO endpointConfig) {

        this.endpointConfig = endpointConfig;
    }
}
