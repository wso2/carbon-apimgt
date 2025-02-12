package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface PoliciesApiService {
      public Response createPolicy(String name, InputStream policyContentInputStream, Attachment policyContentDetail, String policyType, String artifactType, String description, String policyCategory, String documentationLink, String provider, MessageContext messageContext) throws APIMGovernanceException;
      public Response deletePolicy(String policyId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getPolicies(Integer limit, Integer offset, String query, MessageContext messageContext) throws APIMGovernanceException;
      public Response getPolicyById(String policyId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getPolicyContent(String policyId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getPolicyUsage(String policyId, MessageContext messageContext) throws APIMGovernanceException;
      public Response updatePolicyById(String policyId, String name, InputStream policyContentInputStream, Attachment policyContentDetail, String policyType, String artifactType, String description, String policyCategory, String documentationLink, String provider, MessageContext messageContext) throws APIMGovernanceException;
}
