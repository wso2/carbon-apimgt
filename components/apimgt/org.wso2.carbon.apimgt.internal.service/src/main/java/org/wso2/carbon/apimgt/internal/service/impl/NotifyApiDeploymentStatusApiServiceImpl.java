package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentStatusAcknowledgmentDTO;
import org.wso2.carbon.apimgt.internal.service.dto.InlineResponse200DTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class NotifyApiDeploymentStatusApiServiceImpl implements NotifyApiDeploymentStatusApiService {

    private static final String SELECT_GATEWAY = "SELECT 1 FROM AM_GW_INSTANCES WHERE GATEWAY_ID = ?";
    private static final String SELECT_DEPLOYMENT =
            "SELECT 1 FROM AM_GW_REVISION_DEPLOYMENT WHERE GATEWAY_ID = ? AND API_ID = ?";
    private static final String INSERT_DEPLOYMENT =
            "INSERT INTO AM_GW_REVISION_DEPLOYMENT (GATEWAY_ID, API_ID, STATUS, ACTION, REVISION_ID) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_DEPLOYMENT =
            "UPDATE AM_GW_REVISION_DEPLOYMENT SET STATUS = ?, ACTION = ?, REVISION_ID = ? WHERE GATEWAY_ID = ? AND API_ID = ?";

    public Response notifyApiDeploymentStatusPost(
            GatewayDeploymentStatusAcknowledgmentDTO gatewayDeploymentStatusAcknowledgmentDTO,
            MessageContext messageContext) {
        String gatewayId = gatewayDeploymentStatusAcknowledgmentDTO.getGatewayId();
        String apiId = gatewayDeploymentStatusAcknowledgmentDTO.getApiId();
        String status = gatewayDeploymentStatusAcknowledgmentDTO.getDeploymentStatus().toString();
        String action = gatewayDeploymentStatusAcknowledgmentDTO.getAction().toString();
        String revisionId = gatewayDeploymentStatusAcknowledgmentDTO.getRevisionId();

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            // Check if gateway exists
            try (PreparedStatement ps = conn.prepareStatement(SELECT_GATEWAY)) {
                ps.setString(1, gatewayId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        ErrorDTO errorObject = new ErrorDTO();
                        errorObject.setCode(404);
                        errorObject.setMessage("Gateway not found");
                        errorObject.setFields("gatewayId");
                        return Response.status(Response.Status.NOT_FOUND).entity(errorObject).build();
                    }
                }
            }

            // Check if deployment entry exists
            boolean exists;
            try (PreparedStatement ps = conn.prepareStatement(SELECT_DEPLOYMENT)) {
                ps.setString(1, gatewayId);
                ps.setString(2, apiId);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                // Update existing entry
                try (PreparedStatement ps = conn.prepareStatement(UPDATE_DEPLOYMENT)) {
                    ps.setString(1, status);
                    ps.setString(2, action);
                    ps.setString(3, revisionId);
                    ps.setString(4, gatewayId);
                    ps.setString(5, apiId);
                    ps.executeUpdate();
                }
            } else {
                // Insert new entry
                try (PreparedStatement ps = conn.prepareStatement(INSERT_DEPLOYMENT)) {
                    ps.setString(1, gatewayId);
                    ps.setString(2, apiId);
                    ps.setString(3, status);
                    ps.setString(4, action);
                    ps.setString(5, revisionId);
                    ps.executeUpdate();
                }
            }

            // Success response
            return Response.ok().entity("{\"status\":\"success\"}").build();
        } catch (SQLException e) {
            ErrorDTO errorObject = new ErrorDTO();
            errorObject.setCode(500);
            errorObject.setMessage("Database error: " + e.getMessage());
            errorObject.setFields("database");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorObject).build();
        }
    }
}
