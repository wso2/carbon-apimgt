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

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.internal.service.NotifyGatewayApiService;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayPayloadDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayStatusResponseDTO;
import org.wso2.carbon.apimgt.api.ExceptionCodes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

public class NotifyGatewayApiServiceImpl implements NotifyGatewayApiService {

    private static final Log log = LogFactory.getLog(NotifyGatewayApiServiceImpl.class);
    private GatewayNotificationConfiguration configuration;
    private GatewayManagementDAO gatewayManagementDAO;

    /**
     * Handles gateway notification requests for both registration and heartbeat operations.
     *
     * @param notifyGatewayPayloadDTO the notification payload containing gateway information
     * @param messageContext          the JAX-RS message context
     * @return Response containing status and gateway information, or error details
     */
    @Override
    public Response notifyGatewayPost(NotifyGatewayPayloadDTO notifyGatewayPayloadDTO, MessageContext messageContext)
            throws APIManagementException {
        if (notifyGatewayPayloadDTO == null || notifyGatewayPayloadDTO.getPayloadType() == null) {
            throw new APIManagementException(APIConstants.GatewayNotification.ERROR_INVALID_PAYLOAD,
                                             ExceptionCodes.GATEWAY_NOTIFICATION_BAD_REQUEST);
        }
        String payloadType = notifyGatewayPayloadDTO.getPayloadType().value();
        if (APIConstants.GatewayNotification.PAYLOAD_TYPE_REGISTER.equalsIgnoreCase(payloadType)) {
            return handleRegister(notifyGatewayPayloadDTO, notifyGatewayPayloadDTO.getLoadingTenants());
        } else if (APIConstants.GatewayNotification.PAYLOAD_TYPE_HEARTBEAT.equalsIgnoreCase(payloadType)) {
            return handleHeartbeat(notifyGatewayPayloadDTO, notifyGatewayPayloadDTO.getLoadingTenants());
        } else {
            throw new APIManagementException(APIConstants.GatewayNotification.ERROR_INVALID_PAYLOAD_TYPE + payloadType,
                                             ExceptionCodes.GATEWAY_NOTIFICATION_BAD_REQUEST);
        }
    }

    private Response handleRegister(NotifyGatewayPayloadDTO dto, List<String> organizations)
            throws APIManagementException {
        String gatewayId = dto.getGatewayId();
        List<String> envLabels = dto.getEnvironmentLabels();
        byte[] gwProperties = new Gson().toJson(dto.getGatewayProperties()).getBytes();
        if (organizations == null || organizations.isEmpty()) {
            organizations = new ArrayList<>();
            organizations.add(APIConstants.GatewayNotification.WSO2_ALL_TENANTS);
        }

        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
        boolean anyRegistered = false;

        try {
            GatewayManagementDAO dao = GatewayManagementDAO.getInstance();
            Timestamp timeStamp = new Timestamp(dto.getTimeStamp());

            // Loop through each organization
            for (String organization : organizations) {
                if (dao.gatewayExists(gatewayId, organization)) {
                    if (dao.isGatewayTimestampInorder(gatewayId, organization, timeStamp)) {
                        dao.updateGatewayInstance(gatewayId, organization, envLabels, timeStamp, gwProperties);
                    }
                } else {
                    anyRegistered = true;
                    performGatewayDataCleanup();
                    dao.insertGatewayInstance(gatewayId, organization, envLabels, timeStamp, gwProperties);
                }
            }
        } catch (APIManagementException e) {
            throw new APIManagementException(ExceptionCodes.GATEWAY_NOTIFICATION_INTERNAL_SERVER_ERROR);
        }

        if (anyRegistered) {
            responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue(
                    APIConstants.GatewayNotification.STATUS_REGISTERED));
        } else {
            responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue(
                    APIConstants.GatewayNotification.STATUS_ACKNOWLEDGED));
        }

        responseDTO.setGatewayId(gatewayId);
        return Response.ok(responseDTO).build();
    }

    private Response handleHeartbeat(NotifyGatewayPayloadDTO dto, List<String> organizations)
            throws APIManagementException {
        String gatewayId = dto.getGatewayId();

        // If organizations list is null or empty, add default organization
        if (organizations == null || organizations.isEmpty()) {
            organizations = new java.util.ArrayList<>();
            organizations.add(APIConstants.GatewayNotification.WSO2_ALL_TENANTS);
        }

        GatewayManagementDAO dao = GatewayManagementDAO.getInstance();

        try {
            java.sql.Timestamp timeStamp = new java.sql.Timestamp(dto.getTimeStamp());

            // Loop through each organization
            for (String organization : organizations) {
                if (!dao.gatewayExists(gatewayId, organization)) {
                    log.error("Gateway with ID " + gatewayId + " and organization " + organization
                                      + " not found for received heartbeat notification.");
                } else {
                    if (dao.isGatewayTimestampInorder(gatewayId, organization, timeStamp)) {
                        dao.updateGatewayHeartbeat(gatewayId, organization, timeStamp);
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new APIManagementException(ExceptionCodes.GATEWAY_NOTIFICATION_INTERNAL_SERVER_ERROR);
        }

        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
        responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue(
                APIConstants.GatewayNotification.STATUS_ACKNOWLEDGED));
        responseDTO.setGatewayId(gatewayId);
        return Response.ok(responseDTO).build();
    }

    /**
     * Performs cleanup of old gateway records based on the configured retention period.
     */
    private void performGatewayDataCleanup() {
        try {
            long currentTime = System.currentTimeMillis();
            configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration().getGatewayNotificationConfiguration();
            long retentionThreshold =
                    currentTime - (configuration.getGatewayCleanupConfiguration().getDataRetentionPeriodSeconds()
                            * 1000L);
            Timestamp retentionTimestamp = new Timestamp(retentionThreshold);

            if (gatewayManagementDAO == null) {
                gatewayManagementDAO = GatewayManagementDAO.getInstance();
            }

            int deletedCount = gatewayManagementDAO.deleteOldGatewayRecords(retentionTimestamp);
            if (log.isInfoEnabled() && (deletedCount > 0)) {
                log.info("Gateway cleanup completed - Deleted: " + deletedCount);
            }
        } catch (APIManagementException e) {
            log.error("Gateway cleanup failed: " + e.getMessage(), e);
        }
    }
}
