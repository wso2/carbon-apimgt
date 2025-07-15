package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;
import org.wso2.carbon.apimgt.impl.dao.GatewayManagementDAO;

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

    public Response notifyApiDeploymentStatusPost(
            GatewayDeploymentStatusAcknowledgmentDTO gatewayDeploymentStatusAcknowledgmentDTO,
            MessageContext messageContext) {
        String gatewayId = gatewayDeploymentStatusAcknowledgmentDTO.getGatewayId();
        String apiId = gatewayDeploymentStatusAcknowledgmentDTO.getApiId();
        String status = gatewayDeploymentStatusAcknowledgmentDTO.getDeploymentStatus().toString();
        String action = gatewayDeploymentStatusAcknowledgmentDTO.getAction().toString();
        String revisionId = gatewayDeploymentStatusAcknowledgmentDTO.getRevisionId();

        try {
            GatewayManagementDAO dao = GatewayManagementDAO.getInstance();
            // Use DAO methods for gateway and deployment checks/updates
            if (!dao.gatewayExists(gatewayId)) {
                ErrorDTO errorObject = new ErrorDTO();
                errorObject.setCode(404);
                errorObject.setMessage("Gateway not found");
                errorObject.setFields("gatewayId");
                return Response.status(Response.Status.NOT_FOUND).entity(errorObject).build();
            }
            if (dao.deploymentExists(gatewayId, apiId)) {
                dao.updateDeployment(gatewayId, apiId, status, action, revisionId);
            } else {
                dao.insertDeployment(gatewayId, apiId, status, action, revisionId);
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
