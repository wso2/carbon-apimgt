package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface PoliciesApiService {
      public Response createGovernancePolicy(APIMGovernancePolicyDTO apIMGovernancePolicyDTO, MessageContext messageContext) throws APIMGovernanceException;
      public Response deleteGovernancePolicy(String policyId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getGovernancePolicies(Integer limit, Integer offset, String query, MessageContext messageContext) throws APIMGovernanceException;
      public Response getGovernancePolicyById(String policyId, MessageContext messageContext) throws APIMGovernanceException;
      public Response updateGovernancePolicyById(String policyId, APIMGovernancePolicyDTO apIMGovernancePolicyDTO, MessageContext messageContext) throws APIMGovernanceException;
}
