package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

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

    private static final String INSERT_OR_UPDATE_GATEWAY =
            "INSERT INTO AM_GW_INSTANCES (GATEWAY_ID, ENV_LABELS, LAST_UPDATED, GW_PROPERTIES) VALUES (?, ?, ?, ?) ";
    private static final String UPDATE_GATEWAY_HEARTBEAT =
            "UPDATE AM_GW_INSTANCES SET LAST_UPDATED=? WHERE GATEWAY_ID=?";
    private static final String CHECK_GATEWAY_EXISTS =
            "SELECT 1 FROM AM_GW_INSTANCES WHERE GATEWAY_ID=?";

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
        // convert this List<String> to single string by joining with comma
        String envLabels = String.join(",", dto.getEnvironmentLabels());
        byte[] gwProperties = dto.getGatewayProperties() != null ? dto.getGatewayProperties().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8) : null;

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT_OR_UPDATE_GATEWAY)) {
                ps.setString(1, gatewayId);
                ps.setString(2, envLabels);
                ps.setTimestamp(3, new Timestamp(dto.getTimeStamp()));
                ps.setBytes(4, gwProperties);
                ps.addBatch();
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                ErrorDTO error = new ErrorDTO();
                error.setCode(500);
                error.setMessage("Database error during registration: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
            }
        } catch (SQLException e) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(500);
            error.setMessage("Database connection error during registration: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }

        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
        responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue("registered"));
        responseDTO.setGatewayId(gatewayId);
        return Response.ok(responseDTO).build();
    }

    private Response handleHeartbeat(NotifyGatewayPayloadDTO dto) {
        String gatewayId = dto.getGatewayId();
        if (gatewayId == null || gatewayId.trim().isEmpty() || !gatewayExists(gatewayId)) {
            NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
            responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue("re-register"));
            return Response.ok(responseDTO).build();
        }
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_GATEWAY_HEARTBEAT)) {
                ps.setTimestamp(1, new Timestamp(dto.getTimeStamp()));
                ps.setString(2, gatewayId);
                ps.addBatch();
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                ErrorDTO error = new ErrorDTO();
                error.setCode(500);
                error.setMessage("Database error during heartbeat: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
            }
        } catch (SQLException e) {
            ErrorDTO error = new ErrorDTO();
            error.setCode(500);
            error.setMessage("Database connection error during heartbeat: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }

        NotifyGatewayStatusResponseDTO responseDTO = new NotifyGatewayStatusResponseDTO();
        responseDTO.setStatus(NotifyGatewayStatusResponseDTO.StatusEnum.fromValue("acknowledged"));
        responseDTO.setGatewayId(gatewayId);
        return Response.ok(responseDTO).build();
    }

    private boolean gatewayExists(String gatewayId) {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(CHECK_GATEWAY_EXISTS)) {
                ps.setString(1, gatewayId);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean exists = rs.next();
                    conn.commit();
                    return exists;
                }
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }
    private String generateGatewayId() {
        return "GW_" + java.util.UUID.randomUUID();
    }
}
