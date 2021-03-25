/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.gateway.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.gateway.UndeployApiApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.DeployResponseDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class UndeployApiApiServiceImpl implements UndeployApiApiService {

    private static final Log log = LogFactory.getLog(UndeployApiApiServiceImpl.class);
    private boolean debugEnabled = log.isDebugEnabled();

    public Response undeployAPI(String apiName, String version, String tenantDomain,
                                    MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        tenantDomain = RestApiCommonUtil.getValidateTenantDomain(tenantDomain);
        try {
            inMemoryApiDeployer.unDeployAPI(apiName, version, tenantDomain);
            if (debugEnabled) {
                log.debug("Successfully undeployed " + apiName + " in gateway");
            }
            DeployResponseDTO deployResponseDTO = new DeployResponseDTO();
            deployResponseDTO.setDeployStatus(DeployResponseDTO.DeployStatusEnum.UNDEPLOYED);
            deployResponseDTO.setJsonPayload(apiName + " Undeployed from the gateway");
            return Response.ok().entity(deployResponseDTO).build();
        } catch (ArtifactSynchronizerException e) {
            String errorMessage = "Error in fetching artifacts from storage";
            log.error(errorMessage, e);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
