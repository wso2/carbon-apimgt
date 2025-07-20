package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayPayloadDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayStatusResponseDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.List;

import javax.ws.rs.core.Response;


public class NotifyGatewayApiServiceImpl implements NotifyGatewayApiService {

    private static final Log log = LogFactory.getLog(NotifyGatewayApiServiceImpl.class);

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
        if (gatewayId == null || gatewayId.trim().isEmpty()) {
            gatewayId = generateGatewayId();
        }
        String envLabels = String.join(APIConstants.DELEM_COMMA, dto.getEnvironmentLabels());
        byte[] gwProperties = dto.getGatewayProperties() != null ? 
            dto.getGatewayProperties().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8) : null;
        
        try {
            GatewayManagementDAO dao = GatewayManagementDAO.getInstance();
            java.sql.Timestamp ts = new java.sql.Timestamp(dto.getTimeStamp());
            if (dao.gatewayExists(gatewayId)) {
                dao.updateGatewayInstance(gatewayId, envLabels, ts, gwProperties);
            } else {
                dao.insertGatewayInstance(gatewayId, envLabels, ts, gwProperties);
            }
        } catch (Exception e) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(500);
            error.setMessage(APIConstants.GatewayNotification.ERROR_DATABASE_REGISTRATION + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
        
        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
        responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue(
            APIConstants.GatewayNotification.STATUS_REGISTERED));
        responseDTO.setGatewayId(gatewayId);
        return Response.ok(responseDTO).build();
    }

    private Response handleHeartbeat(NotifyGatewayPayloadDTO dto) {
        String gatewayId = dto.getGatewayId();
        GatewayManagementDAO dao = GatewayManagementDAO.getInstance();
        
        try {
            if (gatewayId == null || gatewayId.trim().isEmpty() || !dao.gatewayExists(gatewayId)) {
                log.error("Gateway ID is null or empty or does not exist.");
                log.warn("Requesting re-registration. This will not preserve previous revision deployment data in this"
                                 + " gateway");
                NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
                responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue(
                        APIConstants.GatewayNotification.STATUS_RE_REGISTER));
                return Response.ok(responseDTO).build();
            }
            dao.updateGatewayHeartbeat(gatewayId, new java.sql.Timestamp(dto.getTimeStamp()));
        } catch (Exception e) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(500);
            error.setMessage(APIConstants.GatewayNotification.ERROR_DATABASE_HEARTBEAT + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
        
        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
        responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue(
            APIConstants.GatewayNotification.STATUS_ACKNOWLEDGED));
        responseDTO.setGatewayId(gatewayId);
        return Response.ok(responseDTO).build();
    }

    private String generateGatewayId() {
        return String.valueOf(java.util.UUID.randomUUID());
    }
}
