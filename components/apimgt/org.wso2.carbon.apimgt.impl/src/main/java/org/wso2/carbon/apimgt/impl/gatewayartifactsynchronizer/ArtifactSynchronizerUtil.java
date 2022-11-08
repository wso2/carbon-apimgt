/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.APIArtifactPropertyValues;

import java.sql.Timestamp;

public class ArtifactSynchronizerUtil {

    private static final GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();

    public static void setArtifactProperties(APIRuntimeArtifactDto apiRuntimeArtifactDto)
            throws APIManagementException {
        APIArtifactPropertyValues apiArtifactPropertyValues =
                gatewayArtifactsMgtDAO.retrieveAPIArtifactPropertyValues(apiRuntimeArtifactDto.getApiId(),
                        apiRuntimeArtifactDto.getLabel(), apiRuntimeArtifactDto.getRevision());
        String organization = apiArtifactPropertyValues.getOrganization();
        Timestamp deployedTime = apiArtifactPropertyValues.getDeployedTime();
        if (organization != null) {
            apiRuntimeArtifactDto.setOrganization(apiArtifactPropertyValues.getOrganization());
        }
        if (deployedTime != null) {
            apiRuntimeArtifactDto.setDeployedTimeStamp(deployedTime.getTime());
        }
    }
}
