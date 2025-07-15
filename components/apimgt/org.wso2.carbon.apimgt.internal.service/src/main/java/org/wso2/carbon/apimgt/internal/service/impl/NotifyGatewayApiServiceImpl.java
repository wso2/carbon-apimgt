package org.wso2.carbon.apimgt.internal.service.impl;

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
//import java.time.OffsetDateTime;
//import java.util.List;
//
//import java.io.InputStream;

import javax.ws.rs.core.Response;
//import javax.ws.rs.core.SecurityContext;


public class NotifyGatewayApiServiceImpl implements NotifyGatewayApiService {

    @Override
    public Response notifyGatewayPost(NotifyGatewayPayloadDTO notifyGatewayPayloadDTO, MessageContext messageContext) {
        if (notifyGatewayPayloadDTO == null || notifyGatewayPayloadDTO.getPayloadType() == null) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(400);
            error.setMessage("Invalid payload: payloadType is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        String payloadType = notifyGatewayPayloadDTO.getPayloadType().value();
        if ("register".equalsIgnoreCase(payloadType)) {
            return handleRegister(notifyGatewayPayloadDTO);
        } else if ("heartbeat".equalsIgnoreCase(payloadType)) {
            return handleHeartbeat(notifyGatewayPayloadDTO);
        } else {
            ErrorDTO error = new ErrorDTO();
            error.setCode(400);
            error.setMessage("Invalid payloadType: " + payloadType);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    private Response handleRegister(NotifyGatewayPayloadDTO dto) {
        String gatewayId = dto.getGatewayId();
        if (gatewayId == null || gatewayId.trim().isEmpty()) {
            gatewayId = generateGatewayId();
        }
        String envLabels = String.join(",", dto.getEnvironmentLabels());
        byte[] gwProperties = dto.getGatewayProperties() != null ? dto.getGatewayProperties().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8) : null;
        try {
            GatewayManagementDAO dao = GatewayManagementDAO.getInstance();
            java.sql.Timestamp ts = new java.sql.Timestamp(dto.getTimeStamp());
            if (dao.gatewayExists(gatewayId)) {
                // Update if exists
                dao.updateGatewayInstance(gatewayId, envLabels, ts, gwProperties);
            } else {
                // Insert if not exists
                dao.insertGatewayInstance(gatewayId, envLabels, ts, gwProperties);
            }
        } catch (Exception e) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(500);
            error.setMessage("Database error during registration: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
        responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue("registered"));
        responseDTO.setGatewayId(gatewayId);
        return Response.ok(responseDTO).build();
    }

    private Response handleHeartbeat(NotifyGatewayPayloadDTO dto) {
        String gatewayId = dto.getGatewayId();
        GatewayManagementDAO dao = GatewayManagementDAO.getInstance();
        try {
            if (gatewayId == null || gatewayId.trim().isEmpty() || !dao.gatewayExists(gatewayId)) {
                NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
                responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue("re-register"));
                return Response.ok(responseDTO).build();
            }
            dao.updateGatewayHeartbeat(gatewayId, new java.sql.Timestamp(dto.getTimeStamp()));
        } catch (Exception e) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(500);
            error.setMessage("Database error during heartbeat: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
        responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue("acknowledged"));
        responseDTO.setGatewayId(gatewayId);
        return Response.ok(responseDTO).build();
    }

    private String generateGatewayId() {
        return "GW_" + java.util.UUID.randomUUID();
    }
}
