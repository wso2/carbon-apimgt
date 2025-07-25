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

import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentStatusAcknowledgmentListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentStatusAcknowledgmentDTO;
import org.wso2.carbon.apimgt.internal.service.dto.InlineResponse200DTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class NotifyApiDeploymentStatusApiServiceImpl implements NotifyApiDeploymentStatusApiService {

    /**
     * Handles API deployment status notifications from gateways.
     * Processes a list of deployment status acknowledgments and updates the database
     * with the current deployment status for each API on each gateway.
     * Validates gateway existence and either updates existing deployment records
     * or inserts new ones based on the deployment status.
     *
     * @param gatewayDeploymentStatusAcknowledgmentListDTO the list of deployment status acknowledgments
     * @param messageContext the JAX-RS message context
     * @return Response indicating success or error with appropriate status codes
     */
    public Response notifyApiDeploymentStatusPost(GatewayDeploymentStatusAcknowledgmentListDTO gatewayDeploymentStatusAcknowledgmentListDTO, MessageContext messageContext) {
        // Validate input
        if (gatewayDeploymentStatusAcknowledgmentListDTO == null ||
            gatewayDeploymentStatusAcknowledgmentListDTO.getList() == null ||
            gatewayDeploymentStatusAcknowledgmentListDTO.getList().isEmpty()) {
            ErrorDTO errorObject = new ErrorDTO();
            errorObject.setCode(400);
            errorObject.setMessage("Invalid request: Empty or null acknowledgment list");
            errorObject.setFields("gatewayDeploymentStatusAcknowledgmentListDTO");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorObject).build();
        }

        try {
            GatewayManagementDAO dao = GatewayManagementDAO.getInstance();

            // Process each acknowledgment in the list
            for (GatewayDeploymentStatusAcknowledgmentDTO acknowledgment : gatewayDeploymentStatusAcknowledgmentListDTO.getList()) {
                String gatewayId = acknowledgment.getGatewayId();
                String apiId = acknowledgment.getApiId();
                String status = acknowledgment.getDeploymentStatus().toString();
                java.sql.Timestamp timeStamp = new java.sql.Timestamp(acknowledgment.getTimeStamp());
                String action = acknowledgment.getAction().toString();
                String revisionId = acknowledgment.getRevisionId();

                // Check if gateway exists for each acknowledgment
                if (!dao.gatewayExists(gatewayId)) {
                    ErrorDTO errorObject = new ErrorDTO();
                    errorObject.setCode(404);
                    errorObject.setMessage("Gateway not found: " + gatewayId);
                    errorObject.setFields("gatewayId");
                    return Response.status(Response.Status.NOT_FOUND).entity(errorObject).build();
                }

                // Update or insert deployment status
                if (dao.deploymentExists(gatewayId, apiId)) {
                    if (dao.isDeploymentTimestampInorder(gatewayId, apiId, timeStamp)) {
                        dao.updateDeployment(gatewayId, apiId, status, timeStamp, action, revisionId);
                    }
                } else {
                    dao.insertDeployment(gatewayId, apiId, status, timeStamp, action, revisionId);
                }
            }

                return Response.ok().entity("{\"status\":\"success\"}").build();
        } catch (Exception e) {
            ErrorDTO errorObject = new ErrorDTO();
            errorObject.setCode(500);
            errorObject.setMessage("Database error: " + e.getMessage());
            errorObject.setFields("database");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorObject).build();
        }
    }
}
