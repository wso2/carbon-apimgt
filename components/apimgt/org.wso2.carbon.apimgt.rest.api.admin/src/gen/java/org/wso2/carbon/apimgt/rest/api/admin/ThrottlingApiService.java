package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ThrottlingApiService {
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
}
