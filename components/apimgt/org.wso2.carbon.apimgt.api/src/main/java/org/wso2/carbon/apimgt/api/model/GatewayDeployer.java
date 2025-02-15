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

import org.wso2.carbon.apimgt.api.APIManagementException;


/**
 * This Interface is  providing functionality of external gateway specific core operations.
 * You can implement deploy/undeploy  API methods by implementing this interface.
 */
public interface GatewayDeployer {

    /**
     * Initialize the external gateway deployer
     *
     * @throws APIManagementException if error occurs when initializing the external gateway deployer
     */
    void init(GatewayConfiguration configuration) throws APIManagementException;

    /**
     * This method returns the type of Gateway
     * @return gateway type
     */
    String getType();

    /**
     * Deploy API artifact to provided environment in the external gateway
     *
     * @param api API to be deployed into in the external gateway
     * @param externalReference reference artifact
     * @throws APIManagementException if error occurs when deploying APIs to in the external gateway
     */
    String deploy(API api, String externalReference) throws APIManagementException;

    /**
     * Undeploy API artifact from provided environment
     *
     * @param externalReference reference artifact
     * @throws APIManagementException if error occurs when un-deploying APIs from external gateway
     */
    boolean undeploy(String externalReference) throws APIManagementException;

    /**
     * This method returns the validation result of a given API with the external gateway
     *
     * @return List<String> validation result
     */
    GatewayAPIValidationResult validateApi(API api) throws APIManagementException;

    /**
     * This method returns the resolved API execution URL by replacing all placeholders appropriately
     *
     * @return String api execution url
     */
    String getAPIExecutionURL(String externalReference) throws APIManagementException;

    /**
     * This method returns refined API by manipulating the API object according to the external gateway requirements
     *
     * @return API api object
     */
    void transformAPI(API api) throws APIManagementException;
}
