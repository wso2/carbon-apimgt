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

public class ExternalGatewayDeployerImpl implements ExternalGatewayDeployer {

    @Override
    public boolean deploy(API api, Environment environment) throws DeployerException {
        return false;
    }

    @Override
    public boolean undeploy(String apiName, String apiVersion, String apiContext, Environment environment)
            throws DeployerException {
        return false;
    }

    @Override
    public boolean undeployWhenRetire(API api, Environment environment) throws DeployerException {
        return false;
    }

    @Override
    public String getType() {
        return null;
    }
}
