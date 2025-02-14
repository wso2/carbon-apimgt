/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import java.util.List;

/**
 * This Interface providing functionality to register Gateway Agent Related Configurations
 */
public interface GatewayAgentConfiguration {

    /**
     * This method returns the Gateway Deployer implementation class name
     *
     * @return gateway deployer implementation class name
     */
    String getImplementation();

    /**
     * Get vendor type of the external gateway
     *
     * @return String vendor name
     */
    String getType();

    /**
     * This method returns the Configurations related to external gateway
     *
     * @return  List<ConfigurationDto> connectionConfigurations
     */
    List<ConfigurationDto> getConnectionConfigurations();

    /**
     * This method returns the Gateway Feature Catalog
     *
     * @return String Gateway Feature Catalog
     */
    GatewayFeatureCatalog getGatewayFeatureCatalog();

    /**
     * This method returns the default hostname template of the external gateway
     *
     * @return String default hostname template
     */
    public String getDefaultHostnameTemplate();
}
