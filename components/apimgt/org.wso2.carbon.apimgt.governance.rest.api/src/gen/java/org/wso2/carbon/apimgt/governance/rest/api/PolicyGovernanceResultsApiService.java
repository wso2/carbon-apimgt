package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyGovernanceResultListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyGovernanceResultWithArtifactsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyGovernanceResultsSummaryDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface PolicyGovernanceResultsApiService {
      public Response getGovernanceResultsByPolicyId(String policyId, MessageContext messageContext) throws GovernanceException;
      public Response getGovernanceResultsForAllPolicies(Integer limit, Integer offset, MessageContext messageContext) throws GovernanceException;
      public Response getPolicyGovernanceResultsSummary(MessageContext messageContext) throws GovernanceException;
}
