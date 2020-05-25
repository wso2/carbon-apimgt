package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.internal.service.dto.ApplicationPolicyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApplicationPoliciesApiService {
      public Response applicationPoliciesGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response applicationPoliciesPolicyIdGet(Integer policyId, MessageContext messageContext) throws APIManagementException;
}
