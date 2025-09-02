/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.wso2.carbon.apimgt.internal.service.NotifyApiDeploymentStatusApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.DeploymentAcknowledgmentResponseDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentStatusAcknowledgmentListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentStatusAcknowledgmentDTO;

import javax.ws.rs.core.Response;

public class NotifyApiDeploymentStatusApiServiceImpl implements NotifyApiDeploymentStatusApiService {

    private static final Log log = LogFactory.getLog(NotifyApiDeploymentStatusApiServiceImpl.class);

    /**
     * Handles API deployment status notifications from gateways.
     *
     * @param gatewayDeploymentStatusAcknowledgmentListDTO the list of deployment status acknowledgments
     * @param messageContext                               the JAX-RS message context
     * @return Response indicating success or error with appropriate status codes
     */
    public Response notifyApiDeploymentStatusPost(
            GatewayDeploymentStatusAcknowledgmentListDTO gatewayDeploymentStatusAcknowledgmentListDTO,
            MessageContext messageContext) throws APIManagementException {
        if (gatewayDeploymentStatusAcknowledgmentListDTO == null
                || gatewayDeploymentStatusAcknowledgmentListDTO.getList() == null
                || gatewayDeploymentStatusAcknowledgmentListDTO.getList().isEmpty()) {
            throw new APIManagementException(ExceptionCodes.GATEWAY_DEPLOYMENT_STATUS_ACKNOWLEDGMENT_LIST_EMPTY);
        }

        try {
            GatewayManagementDAO dao = GatewayManagementDAO.getInstance();

            for (GatewayDeploymentStatusAcknowledgmentDTO acknowledgment : gatewayDeploymentStatusAcknowledgmentListDTO.getList()) {
                if (acknowledgment.getGatewayId() == null || acknowledgment.getApiId() == null
                        || acknowledgment.getDeploymentStatus() == null || acknowledgment.getTimeStamp() == null
                        || acknowledgment.getAction() == null) {
                    log.error("Invalid acknowledgment: Missing required fields for acknowledgment: " + acknowledgment);
                    continue;
                }

                String gatewayId = acknowledgment.getGatewayId();
                String apiId = acknowledgment.getApiId();
                String tenantDomain = acknowledgment.getTenantDomain();
                String status = acknowledgment.getDeploymentStatus().toString();
                long timeStamp = acknowledgment.getTimeStamp();
                String action = acknowledgment.getAction().toString();
                String revisionUuid = acknowledgment.getRevisionId();

                if (!dao.gatewayExists(gatewayId, tenantDomain)) {
                    log.error("Gateway not found: " + gatewayId + " for acknowledgment: " + acknowledgment);
                    continue;
                }

                if (dao.apiExists(apiId)) {
                    if (dao.deploymentExists(gatewayId, apiId)) {
                        if (dao.isDeploymentTimestampInorder(gatewayId, apiId, timeStamp)) {
                            dao.updateDeployment(gatewayId, apiId, tenantDomain, status, action, revisionUuid,
                                                 timeStamp);
                        }
                    } else {
                        dao.insertDeployment(gatewayId, apiId, tenantDomain, status, action, revisionUuid, timeStamp);
                    }
                }
            }
            DeploymentAcknowledgmentResponseDTO response = new DeploymentAcknowledgmentResponseDTO();
            response.setStatus(DeploymentAcknowledgmentResponseDTO.StatusEnum.RECEIVED);
            return Response.ok().entity(response).build();
        } catch (APIManagementException e) {
            log.error("Error processing deployment status notifications", e);
            throw new APIManagementException(ExceptionCodes.GATEWAY_DEPLOYMENT_STATUS_INTERNAL_SERVER_ERROR);
        }
    }
}
