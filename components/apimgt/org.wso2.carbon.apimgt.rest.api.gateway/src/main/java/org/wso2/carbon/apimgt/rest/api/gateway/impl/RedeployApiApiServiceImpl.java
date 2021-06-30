/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.gateway.RedeployApiApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.DeployResponseDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import javax.ws.rs.core.Response;

public class RedeployApiApiServiceImpl implements RedeployApiApiService {

    private static final Log log = LogFactory.getLog(RedeployApiApiServiceImpl.class);
    private boolean debugEnabled = log.isDebugEnabled();

    public Response redployAPI(String apiName, String version, String tenantDomain,
                                    MessageContext messageContext) {

        tenantDomain = RestApiCommonUtil.getValidateTenantDomain(tenantDomain);
        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        try {
            inMemoryApiDeployer.reDeployAPI(apiName, version, tenantDomain);
            if (debugEnabled) {
                log.debug("Successfully deployed " + apiName + " in gateway");
            }
            DeployResponseDTO responseDTO = new DeployResponseDTO();
            responseDTO.setDeployStatus(DeployResponseDTO.DeployStatusEnum.DEPLOYED);
            responseDTO.setJsonPayload(apiName + " redeployed from the gateway");
            return Response.ok().entity(responseDTO).build();
        } catch (ArtifactSynchronizerException e) {
            String errorMessage = "Error in fetching artifacts from storage";
            log.error(errorMessage, e);
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
