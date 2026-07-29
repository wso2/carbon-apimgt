package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CachedDiscoveryResultResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DiscoveryTaskStatusResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DiscoveryTaskSubmitResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FederatedAPIImportRequestDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FederatedAPIImportResponseDTO;
import java.util.List;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface FederatedApisApiService {
      public Response discoverFederatedAPIs(String environment, MessageContext messageContext) throws APIManagementException;
      public Response getCachedDiscoveryResults(String environment, MessageContext messageContext) throws APIManagementException;
      public Response getDiscoveryTaskStatus(String taskId, MessageContext messageContext) throws APIManagementException;
      public Response importFederatedAPIs(String environment, List<FederatedAPIImportRequestDTO> federatedAPIImportRequestDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateFederatedAPIs(String environment, List<FederatedAPIImportRequestDTO> federatedAPIImportRequestDTO, MessageContext messageContext) throws APIManagementException;
}
