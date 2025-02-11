package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAttachmentAdherenceSummaryDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface PolicyAttachmentAdherenceApiService {
      public Response getPolicyAttachmentAdherenceByPolicyAttachmentId(String policyAttachmentId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getPolicyAttachmentAdherenceForAllPolicyAttachments(Integer limit, Integer offset, MessageContext messageContext) throws APIMGovernanceException;
      public Response getPolicyAttachmentAdherenceSummary(MessageContext messageContext) throws APIMGovernanceException;
}
