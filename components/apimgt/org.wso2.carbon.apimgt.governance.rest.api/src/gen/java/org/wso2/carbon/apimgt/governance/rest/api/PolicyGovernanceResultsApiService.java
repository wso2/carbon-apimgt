package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.impl.error.GovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyGovernanceResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyGovernanceResultsDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface PolicyGovernanceResultsApiService {
      public Response getGovernanceResultsByPolicyId(String policyId, MessageContext messageContext) throws GovernanceException;
      public Response getGovernanceResultsForAllPolicies(Integer limit, Integer offset, MessageContext messageContext) throws GovernanceException;
}
