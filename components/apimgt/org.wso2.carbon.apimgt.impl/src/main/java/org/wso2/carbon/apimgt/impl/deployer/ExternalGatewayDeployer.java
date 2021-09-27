/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.deployer;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.deployer.exceptions.DeployerException;

public interface ExternalGatewayDeployer {


    /**
     * Deploy API artifact to provided environment in the external gateway
     *
     * @param api API to be deployed into in the external gateway
     * @param environment Environment to be deployed
     * @throws DeployerException if error occurs when deploying APIs to in the external gateway
     */
    public boolean deploy(API api, Environment environment) throws DeployerException;

    /**
     * Undeploy API artifact from provided environment in the external gateway
     *
     * @param api API to be undeployed from the external gateway
     * @param environment Environment needed to be undeployed API from the external gateway
     * @throws DeployerException if error occurs when undeploying APIs from the external gateway
     */
    public boolean undeploy(API api, Environment environment) throws DeployerException;

    /**
     * Get vendor type of the external gateway
     *
     * @return String vendor name
     */
    public String getType();
}
