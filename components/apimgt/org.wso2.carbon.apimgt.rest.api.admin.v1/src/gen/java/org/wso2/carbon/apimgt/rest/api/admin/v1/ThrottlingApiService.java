package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ThrottlingApiService {
      public Response throttlingDenyPoliciesGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingDenyPoliciesPost(String contentType, BlockingConditionDTO blockingConditionDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingDenyPolicyConditionIdDelete(String conditionId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingDenyPolicyConditionIdGet(String conditionId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingDenyPolicyConditionIdPatch(String conditionId, String contentType, BlockingConditionStatusDTO blockingConditionStatusDTO, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPolicyIdDelete(String policyId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPolicyIdGet(String policyId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPolicyIdPut(String policyId, String contentType, AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPost(String contentType, AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPolicyIdDelete(String policyId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPolicyIdGet(String policyId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPolicyIdPut(String policyId, String contentType, ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPost(String contentType, ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomPost(String contentType, CustomRuleDTO customRuleDTO, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomRuleIdDelete(String ruleId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomRuleIdGet(String ruleId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomRuleIdPut(String ruleId, String contentType, CustomRuleDTO customRuleDTO, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPolicyIdDelete(String policyId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPolicyIdGet(String policyId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPolicyIdPut(String policyId, String contentType, SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPost(String contentType, SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
}
