package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.impl.error.GovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface GovernancePoliciesApiService {
      public Response createGovernancePolicy(GovernancePolicyDTO governancePolicyDTO, MessageContext messageContext) throws GovernanceException;
      public Response deleteGovernancePolicy(String policyId, MessageContext messageContext) throws GovernanceException;
      public Response getGovernancePolicies(Integer limit, Integer offset, MessageContext messageContext) throws GovernanceException;
      public Response getGovernancePolicyById(String policyId, MessageContext messageContext) throws GovernanceException;
      public Response updateGovernancePolicyById(String policyId, GovernancePolicyDTO governancePolicyDTO, MessageContext messageContext) throws GovernanceException;
}
