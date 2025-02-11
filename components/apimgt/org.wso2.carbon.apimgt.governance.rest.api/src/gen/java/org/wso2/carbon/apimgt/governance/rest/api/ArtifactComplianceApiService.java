package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyValidationResultDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ArtifactComplianceApiService {
      public Response getComplianceByAPIId(String apiId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getComplianceStatusListOfAPIs(Integer limit, Integer offset, MessageContext messageContext) throws APIMGovernanceException;
      public Response getComplianceSummaryForAPIs(MessageContext messageContext) throws APIMGovernanceException;
      public Response getPolicyValidationResultsByAPIId(String apiId, String policyId, MessageContext messageContext) throws APIMGovernanceException;
}
