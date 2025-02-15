/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl;

import org.wso2.carbon.apimgt.governance.api.ArtifactGovernanceHandler;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;

/**
 * This class is used to get the relevant governance artifact implementation based on the artifact type
 */
public class ArtifactGovernanceFactory {

    private ArtifactGovernanceFactory() {
        
    }

    private static class SingletonHelper {
        private static final ArtifactGovernanceFactory INSTANCE = new ArtifactGovernanceFactory();
    }

    /**
     * Get the instance of ArtifactGovernanceFactory
     *
     * @return ArtifactGovernanceFactory instance
     */
    public static ArtifactGovernanceFactory getInstance() {
        return ArtifactGovernanceFactory.SingletonHelper.INSTANCE;
    }


    /**
     * This method is used to get the relevant governance artifact implementation based on the artifact type
     *
     * @param artifactType artifact type
     * @return ArtifactGovernanceHandler implementation
     * @throws APIMGovernanceException if an error occurs while getting the handler
     */
    public ArtifactGovernanceHandler getHandler(ArtifactType artifactType)
            throws APIMGovernanceException {
        if (ArtifactType.API.equals(artifactType)) {
            return new APIGovernanceHandler();
        }
        throw new APIMGovernanceException("Unsupported artifact type: " + artifactType);
    }

}
