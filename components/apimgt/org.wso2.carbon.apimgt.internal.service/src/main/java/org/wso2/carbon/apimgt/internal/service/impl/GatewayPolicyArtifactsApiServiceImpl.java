/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayPolicyArtifactGeneratorUtil;
import org.wso2.carbon.apimgt.internal.service.GatewayPolicyArtifactsApiService;
import org.wso2.carbon.apimgt.internal.service.dto.SynapseArtifactListDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * This is the service implementation class for Gateway Policy Artifacts related operations.
 */
public class GatewayPolicyArtifactsApiServiceImpl implements GatewayPolicyArtifactsApiService {

    /**
     * Retrieve the gateway policy artifacts.
     *
     * @param xWSO2Tenant       tenant domain
     * @param policyMappingUuid policy mapping uuid
     * @param type              type of the policy
     * @param gatewayLabel      label of the gateway
     * @param messageContext    message context
     * @return gateway policy artifacts
     * @throws APIManagementException
     */
    public Response gatewayPolicyArtifactsGet(String xWSO2Tenant, String policyMappingUuid, String type,
            String gatewayLabel, MessageContext messageContext) throws APIManagementException {
        RuntimeArtifactDto runtimeArtifactDto = null;
        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        String organization = RestApiUtil.getOrganization(messageContext);
        if (StringUtils.isNotEmpty(organization) && !organization.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM)) {
            runtimeArtifactDto = GatewayPolicyArtifactGeneratorUtil.generateRuntimeArtifact(policyMappingUuid, type,
                    xWSO2Tenant, gatewayLabel);
        }
        if (runtimeArtifactDto != null) {
            if (runtimeArtifactDto.isFile()) {
                File artifact = (File) runtimeArtifactDto.getArtifact();
                StreamingOutput streamingOutput = (outputStream) -> {
                    try {
                        Files.copy(artifact.toPath(), outputStream);
                    } finally {
                        Files.delete(artifact.toPath());
                    }
                };
                return Response.ok(streamingOutput)
                        .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=policy.xml")
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_XML_MEDIA_TYPE).build();
            } else {
                SynapseArtifactListDTO synapseArtifactListDTO = new SynapseArtifactListDTO();
                if (runtimeArtifactDto.getArtifact() instanceof List) {
                    synapseArtifactListDTO.setList((List<String>) runtimeArtifactDto.getArtifact());
                    synapseArtifactListDTO.setCount(((List<String>) runtimeArtifactDto.getArtifact()).size());
                }
                return Response.ok().entity(synapseArtifactListDTO)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_JSON).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
