package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CreatePlatformGatewayRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayResponseWithTokenDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UpdatePlatformGatewayRequestDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface GatewaysApiService {
      public Response deletePlatformGateway(String gatewayId, MessageContext messageContext) throws APIManagementException;
      public Response updatePlatformGateway(String gatewayId, UpdatePlatformGatewayRequestDTO updatePlatformGatewayRequestDTO, MessageContext messageContext) throws APIManagementException;
      public Response regeneratePlatformGatewayToken(String gatewayId, MessageContext messageContext) throws APIManagementException;
      public Response getPlatformGateways(MessageContext messageContext) throws APIManagementException;
      public Response createPlatformGateway(CreatePlatformGatewayRequestDTO createPlatformGatewayRequestDTO, MessageContext messageContext) throws APIManagementException;
}
