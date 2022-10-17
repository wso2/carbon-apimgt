package org.wso2.carbon.apimgt.rest.api.admin.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyDTO;

import java.io.InputStream;

import javax.ws.rs.core.Response;


public interface ThrottlingApiService {
      public Response exportThrottlingPolicy(String policyId, String name, String type, String format, MessageContext messageContext) throws APIManagementException;
      public Response importThrottlingPolicy(InputStream fileInputStream, Attachment fileDetail, Boolean overwrite, MessageContext messageContext) throws APIManagementException;
      public Response throttlingDenyPoliciesGet(String accept, MessageContext messageContext) throws APIManagementException;
      public Response throttlingDenyPoliciesPost(String contentType, BlockingConditionDTO blockingConditionDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingDenyPolicyConditionIdDelete(String conditionId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingDenyPolicyConditionIdGet(String conditionId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingDenyPolicyConditionIdPatch(String conditionId, String contentType, BlockingConditionStatusDTO blockingConditionStatusDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedGet(String accept, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPolicyIdDelete(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPolicyIdGet(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPolicyIdPut(String policyId, String contentType, AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPost(String contentType, AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationGet(String accept, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPolicyIdDelete(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPolicyIdGet(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPolicyIdPut(String policyId, String contentType, ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPost(String contentType, ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomGet(String accept, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomPost(String contentType, CustomRuleDTO customRuleDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomRuleIdDelete(String ruleId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomRuleIdGet(String ruleId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomRuleIdPut(String ruleId, String contentType, CustomRuleDTO customRuleDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionGet(String accept, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPolicyIdDelete(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPolicyIdGet(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPolicyIdPut(String policyId, String contentType, SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPost(String contentType, SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPolicySearch(String query, MessageContext messageContext) throws APIManagementException;
}
