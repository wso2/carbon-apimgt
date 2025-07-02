package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import java.util.List;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface McpServersApiService {
      public Response createMCPServerRevision(String apiId, APIRevisionDTO apIRevisionDTO, MessageContext messageContext) throws APIManagementException;
      public Response deleteMCPServer(String apiId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteMCPServerRevision(String apiId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response deployMCPServerRevision(String apiId, String revisionId, List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response getAllMCPServers(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, String accept, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServer(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerEndpoint(String apiId, String endpointId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerEndpoints(String apiId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerRevision(String apiId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerRevisions(String apiId, String query, MessageContext messageContext) throws APIManagementException;
      public Response importMCPServerDefinition(InputStream fileInputStream, Attachment fileDetail, String url, String additionalProperties, MessageContext messageContext) throws APIManagementException;
      public Response restoreMCPServerRevision(String apiId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response undeployMCPServerRevision(String apiId, String revisionId, String revisionNumber, Boolean allEnvironments, List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateMCPServer(String apiId, APIDTO APIDTO, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response updateMCPServerEndpoint(String apiId, String endpointId, BackendEndpointDTO backendEndpointDTO, MessageContext messageContext) throws APIManagementException;
}
