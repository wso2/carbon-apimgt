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
import org.wso2.carbon.apimgt.rest.api.admin.v1.ThrottlingApi.*;


public interface ThrottlingApiService {
      public Response throttlingBlacklistConditionIdDelete(String conditionId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingBlacklistConditionIdGet(String conditionId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingBlacklistConditionIdPatch(String conditionId, BlockingConditionStatusDTO body, String contentType, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingBlacklistGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingBlacklistPost(BlockingConditionDTO body, String contentType, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPolicyIdDelete(String policyId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPolicyIdGet(String policyId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPolicyIdPut(String policyId, AdvancedThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesAdvancedPost(AdvancedThrottlePolicyDTO body, String contentType, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPolicyIdDelete(String policyId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPolicyIdGet(String policyId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPolicyIdPut(String policyId, ApplicationThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesApplicationPost(ApplicationThrottlePolicyDTO body, String contentType, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomPost(CustomRuleDTO body, String contentType, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomRuleIdDelete(String ruleId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomRuleIdGet(String ruleId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesCustomRuleIdPut(String ruleId, CustomRuleDTO body, String contentType, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionGet(String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPolicyIdDelete(String policyId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPolicyIdGet(String policyId, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPolicyIdPut(String policyId, SubscriptionThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPoliciesSubscriptionPost(SubscriptionThrottlePolicyDTO body, String contentType, MessageContext messageContext) throws APIManagementException;
}
