/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
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
*
*/
package org.wso2.carbon.apimgt.core.impl;

import org.wso2.carbon.apimgt.core.exception.ContainerBasedGatewayException;

import java.util.Map;

/**
 * Abstract class to manage the auto-created gateways in the container Management system
 */
public abstract class ContainerBasedGatewayGenerator {

    /**
     * Initializing implementation parameters
     *
     * @param implParameters implementation parameters
     * @throws ContainerBasedGatewayException if an error occurs while setting the implParameters
     */
    public void init(Map<String, String> implParameters) throws ContainerBasedGatewayException {
        initImpl(implParameters);
    }

    /**
     * Initialization method for the implementation class
     *
     * @param implParameters implementation parameters
     * @throws ContainerBasedGatewayException if an error occurs while initializing
     */
    abstract void initImpl(Map<String, String> implParameters) throws ContainerBasedGatewayException;

    /**
     * Remove the existing gateway and Broker from Container Management System
     *
     * @param label auto-generated label of gateway
     * @throws ContainerBasedGatewayException If there is a failure to update API in gateway
     */
    public abstract void removeContainerBasedGateway(String label) throws ContainerBasedGatewayException;


    /**
     * Startup a new Gateway in Container Management System
     *
     * @param label Auto-generated label of the API
     * @throws ContainerBasedGatewayException
     */
    public abstract void createContainerGateway(String label) throws ContainerBasedGatewayException;

}
