package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyAttachmentDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyAttachmentListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface PolicyAttachmentsApiService {
      public Response createGovernancePolicyAttachment(APIMGovernancePolicyAttachmentDTO apIMGovernancePolicyAttachmentDTO, MessageContext messageContext) throws APIMGovernanceException;
      public Response deleteGovernancePolicyAttachment(String policyAttachmentId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getGovernancePolicyAttachmentById(String policyAttachmentId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getGovernancePolicyAttachments(Integer limit, Integer offset, String query, MessageContext messageContext) throws APIMGovernanceException;
      public Response updateGovernancePolicyAttachmentById(String policyAttachmentId, APIMGovernancePolicyAttachmentDTO apIMGovernancePolicyAttachmentDTO, MessageContext messageContext) throws APIMGovernanceException;
}
