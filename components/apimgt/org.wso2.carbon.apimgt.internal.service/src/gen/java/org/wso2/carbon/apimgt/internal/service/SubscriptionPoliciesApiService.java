package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SubscriptionPolicyListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface SubscriptionPoliciesApiService {
      public Response subscriptionPoliciesGet(String xWSO2Tenant, String policyName, MessageContext messageContext) throws APIManagementException;
}
