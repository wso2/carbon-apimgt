package org.wso2.carbon.apimgt.internal.service;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.core.Response;

public interface SubscriptionPoliciesApiService {
      public Response subscriptionPoliciesGet(String xWSO2Tenant, String policyName, MessageContext messageContext) throws APIManagementException;
}
