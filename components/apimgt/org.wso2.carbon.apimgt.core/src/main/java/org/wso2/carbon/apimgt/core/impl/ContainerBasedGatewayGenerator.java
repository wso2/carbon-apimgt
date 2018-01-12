/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.apimgt.core.exception.GatewayException;

import java.io.IOException;
import java.util.Map;

/**
 * Abstract class to manage the auto-created gateways in the container Management system
 */
public abstract class ContainerBasedGatewayGenerator {

    /**
     * Initialization method for the implementation class
     *
     * @param implParameters implementation parameters
     * @throws GatewayException if an error occurs while initializing
     */
    abstract void initImpl(Map<String, String> implParameters) throws GatewayException;

    /**
     * Create a Service in the Container Management System
     *
     * @param gatewayServiceTemplate Definition of the Service as a String
     * @param serviceName            Name of the Service
     * @param apiId                  UUID of the API
     * @param namespace              namespace of the service
     * @param label                  label of the service
     * @throws GatewayException If there is a failure to update API in gateway
     */
    public abstract void createContainerBasedService(String gatewayServiceTemplate, String apiId, String serviceName,
                                                     String namespace, String label)
            throws GatewayException, IOException;

    /**
     * Create the gateway deployment in Container Management System
     *
     * @param deploymentTemplate Definition of the Deployment as a String
     * @param apiId              UUID of the API
     * @param deploymentName     Name of the deployment
     * @param namespace          Namespace of the deployment
     * @param label              gatewayLabel of the deployment
     * @throws GatewayException If there is a failure to update API in gateway
     */
    public abstract void createContainerBasedDeployment(String deploymentTemplate, String apiId, String deploymentName,
                                                        String namespace, String label) throws GatewayException;

    /**
     * Remove the existing gateway and Broker from Container Management System
     *
     * @param label     auto-generated label of gateway
     * @param apiId     UUID of the API
     * @param namespace Namespace of the Gateway Configuration
     * @throws GatewayException If there is a failure to update API in gateway
     */
    public abstract void removeContainerBasedGateway(String label, String apiId, String namespace)
            throws GatewayException;


    /**
     * Startup a new Gateway in Container Management System
     *
     * @param apiId UUID of the API
     * @param label Auto-generated label of the API
     * @throws GatewayException
     */
    public abstract void createContainerGateway(String apiId, String label) throws GatewayException;


    /**
     * Get Access Token of the Service Account reading from a File
     *
     * @return Access Token of Service Account as A String
     * @throws GatewayException If there is a failure to get the Access Token of the Service Account
     */
    public abstract String getServiceAccountAccessToken() throws GatewayException;

}
