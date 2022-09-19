package org.wso2.carbon.apimgt.rest.api.publisher.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;


import javax.ws.rs.core.Response;


public interface ThrottlingPoliciesApiService {
      public Response getAllThrottlingPolicies(String policyLevel, Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getSubscriptionThrottlingPolicies(Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getThrottlingPolicyByName(String policyName, String policyLevel, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
}
