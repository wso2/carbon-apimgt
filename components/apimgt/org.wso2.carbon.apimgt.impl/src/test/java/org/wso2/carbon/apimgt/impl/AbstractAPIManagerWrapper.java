/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl;


import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

public class AbstractAPIManagerWrapper extends AbstractAPIManager {
    private GenericArtifactManager genericArtifactManager;

    public AbstractAPIManagerWrapper(GenericArtifactManager genericArtifactManager) throws APIManagementException {
        this.genericArtifactManager = genericArtifactManager;
    }

    @Override
    protected GenericArtifactManager getGenericArtifactManager() throws APIManagementException {
        return genericArtifactManager;
    }

    protected API getApi(GenericArtifact artifact) throws APIManagementException {
        try {

            APIIdentifier apiIdentifier = new APIIdentifier(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER),
                    artifact.getAttribute(APIConstants.API_OVERVIEW_NAME), artifact.getAttribute(APIConstants
                    .API_OVERVIEW_VERSION));
            API api = new API(apiIdentifier);
            return api;
        } catch (GovernanceException e) {
            throw new APIManagementException("Error while getting attribute", e);
        }
    }

}
