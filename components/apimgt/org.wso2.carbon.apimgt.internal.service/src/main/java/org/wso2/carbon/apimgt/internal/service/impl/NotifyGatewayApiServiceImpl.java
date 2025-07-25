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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.NotifyGatewayApiService;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayPayloadDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayStatusResponseDTO;

import javax.ws.rs.core.Response;

public class NotifyGatewayApiServiceImpl implements NotifyGatewayApiService {

    private static final Log log = LogFactory.getLog(NotifyGatewayApiServiceImpl.class);

    /**
     * Handles gateway notification requests for both registration and heartbeat operations.
     *
     * @param notifyGatewayPayloadDTO the notification payload containing gateway information
     * @param messageContext          the JAX-RS message context
     * @return Response containing status and gateway information, or error details
     */
    @Override
    public Response notifyGatewayPost(NotifyGatewayPayloadDTO notifyGatewayPayloadDTO, MessageContext messageContext) {
        if (notifyGatewayPayloadDTO == null || notifyGatewayPayloadDTO.getPayloadType() == null) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(400);
            error.setMessage(APIConstants.GatewayNotification.ERROR_INVALID_PAYLOAD);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        String payloadType = notifyGatewayPayloadDTO.getPayloadType().value();
        if (APIConstants.GatewayNotification.PAYLOAD_TYPE_REGISTER.equalsIgnoreCase(payloadType)) {
            return handleRegister(notifyGatewayPayloadDTO);
        } else if (APIConstants.GatewayNotification.PAYLOAD_TYPE_HEARTBEAT.equalsIgnoreCase(payloadType)) {
            return handleHeartbeat(notifyGatewayPayloadDTO);
        } else {
            ErrorDTO error = new ErrorDTO();
            error.setCode(400);
            error.setMessage(APIConstants.GatewayNotification.ERROR_INVALID_PAYLOAD_TYPE + payloadType);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    private Response handleRegister(NotifyGatewayPayloadDTO dto) {
        String gatewayId = dto.getGatewayId();
        String envLabels = String.join(APIConstants.DELEM_COMMA, dto.getEnvironmentLabels());
        byte[] gwProperties = dto.getGatewayProperties() != null ? dto.getGatewayProperties().toString().getBytes(
                java.nio.charset.StandardCharsets.UTF_8) : null;

        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();

        try {
            GatewayManagementDAO dao = GatewayManagementDAO.getInstance();
            java.sql.Timestamp timeStamp = new java.sql.Timestamp(dto.getTimeStamp());
            if (dao.gatewayExists(gatewayId)) {
                responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue(
                        APIConstants.GatewayNotification.STATUS_ACKNOWLEDGED));
                if (dao.isGatewayTimestampInorder(gatewayId, timeStamp)) {
                    dao.updateGatewayInstance(gatewayId, envLabels, timeStamp, gwProperties);
                }
            } else {
                dao.insertGatewayInstance(gatewayId, envLabels, timeStamp, gwProperties);
                responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue(
                        APIConstants.GatewayNotification.STATUS_REGISTERED));
            }
            responseDTO.setGatewayId(gatewayId);
            return Response.ok(responseDTO).build();

        } catch (APIManagementException e) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(500);
            error.setMessage(APIConstants.GatewayNotification.ERROR_DATABASE_REGISTRATION + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    private Response handleHeartbeat(NotifyGatewayPayloadDTO dto) {
        String gatewayId = dto.getGatewayId();
        GatewayManagementDAO dao = GatewayManagementDAO.getInstance();

        try {
            if (!dao.gatewayExists(gatewayId)) {
                log.error("Gateway with ID " + gatewayId + " not found for received heartbeat notification.");
                throw new APIManagementException(
                        APIConstants.GatewayNotification.ERROR_GATEWAY_NOT_FOUND + gatewayId);
            }
            java.sql.Timestamp timeStamp = new java.sql.Timestamp(dto.getTimeStamp());
            if (dao.isGatewayTimestampInorder(gatewayId, timeStamp)) {
                dao.updateGatewayHeartbeat(gatewayId, timeStamp);
            }
        } catch (APIManagementException e) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(500);
            error.setMessage(APIConstants.GatewayNotification.ERROR_HEARTBEAT_REGISTREATION + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }

        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
        responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue(
                APIConstants.GatewayNotification.STATUS_ACKNOWLEDGED));
        responseDTO.setGatewayId(gatewayId);
        return Response.ok(responseDTO).build();
    }
}
