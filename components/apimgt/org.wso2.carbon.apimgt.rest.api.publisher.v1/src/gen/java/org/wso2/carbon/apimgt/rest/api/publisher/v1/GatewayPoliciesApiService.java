package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingsDTO;
import java.util.List;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface GatewayPoliciesApiService {
      public Response addGatewayPoliciesToFlows(GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO, MessageContext messageContext) throws APIManagementException;
      public Response deleteGatewayPolicyByPolicyId(String gatewayPolicyMappingId, MessageContext messageContext) throws APIManagementException;
      public Response engageGlobalPolicy(String gatewayPolicyMappingId, List<GatewayPolicyDeploymentDTO> gatewayPolicyDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response getAllGatewayPolicies(Integer limit, Integer offset, String query, MessageContext messageContext) throws APIManagementException;
      public Response getGatewayPolicyMappingContentByPolicyMappingId(String gatewayPolicyMappingId, MessageContext messageContext) throws APIManagementException;
      public Response updateGatewayPoliciesToFlows(String gatewayPolicyMappingId, GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO, MessageContext messageContext) throws APIManagementException;
}
