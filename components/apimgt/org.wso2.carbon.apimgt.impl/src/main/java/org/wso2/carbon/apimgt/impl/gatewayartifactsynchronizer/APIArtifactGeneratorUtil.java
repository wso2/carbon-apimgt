/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.List;
import java.util.Set;

public class APIArtifactGeneratorUtil {

    private static final GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();

    public static RuntimeArtifactDto generateAPIArtifact(List<String> apiUuids, String name, String version,
                                                         String gatewayLabel, String type, String tenantDomain)
            throws APIManagementException {

        GatewayArtifactGenerator gatewayArtifactGenerator =
                ServiceReferenceHolder.getInstance().getGatewayArtifactGenerator(type);
        if (gatewayArtifactGenerator != null) {
            List<APIRuntimeArtifactDto> gatewayArtifacts;
            if (StringUtils.isNotEmpty(gatewayLabel)) {
                byte[] decodedValue = Base64.decodeBase64(gatewayLabel.getBytes());
                String[] gatewayLabels = new String(decodedValue).split("\\|");
                if (!apiUuids.isEmpty()) {
                    gatewayArtifacts = gatewayArtifactsMgtDAO.
                            retrieveGatewayArtifactsByAPIIDs(apiUuids, gatewayLabels, tenantDomain);
                } else {
                    gatewayArtifacts =
                            gatewayArtifactsMgtDAO.retrieveGatewayArtifactsByLabel(gatewayLabels, tenantDomain);
                }
            } else {
                gatewayArtifacts = gatewayArtifactsMgtDAO.retrieveGatewayArtifacts(tenantDomain);
            }
            if (gatewayArtifacts != null) {
                if (gatewayArtifacts.isEmpty()) {
                    throw new APIManagementException("No API Artifacts", ExceptionCodes.NO_API_ARTIFACT_FOUND);
                }
                for (APIRuntimeArtifactDto apiRuntimeArtifactDto: gatewayArtifacts) {
                    String organizationId = gatewayArtifactsMgtDAO.retrieveOrganization(apiRuntimeArtifactDto.getApiId());
                    if (organizationId != null) {
                        apiRuntimeArtifactDto.setOrganization(organizationId);
                    }
                }
            }
            if (gatewayArtifacts == null || gatewayArtifacts.isEmpty()) {
                return null;
            }
            return gatewayArtifactGenerator.generateGatewayArtifact(gatewayArtifacts);
        } else {
            Set<String> gatewayArtifactGeneratorTypes =
                    ServiceReferenceHolder.getInstance().getGatewayArtifactGeneratorTypes();
            throw new APIManagementException("Couldn't find gateway Type",
                    ExceptionCodes.from(ExceptionCodes.GATEWAY_TYPE_NOT_FOUND, String.join(",",
                            gatewayArtifactGeneratorTypes)));
        }
    }

}
