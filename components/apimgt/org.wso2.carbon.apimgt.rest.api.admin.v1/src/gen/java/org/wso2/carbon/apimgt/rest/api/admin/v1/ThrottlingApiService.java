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
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ExportThrottlePolicyDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottlePolicyDetailsListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ThrottlingApiService {
      public Response addAdvancedPolicy(String contentType, AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response addApplicationThrottlePolicy(String contentType, ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response addCustomRule(String contentType, CustomRuleDTO customRuleDTO, MessageContext messageContext) throws APIManagementException;
      public Response addDenyPolicy(String contentType, BlockingConditionDTO blockingConditionDTO, MessageContext messageContext) throws APIManagementException;
      public Response addSubscriptionThrottlePolicy(String contentType, SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response exportThrottlingPolicy(String policyId, String name, String type, String format, MessageContext messageContext) throws APIManagementException;
      public Response getAdvancedPolicyById(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response getAllAdvancedPolicy(String accept, MessageContext messageContext) throws APIManagementException;
      public Response getAllCustomRoles(String accept, MessageContext messageContext) throws APIManagementException;
      public Response getAllDenyPolicies(String accept, MessageContext messageContext) throws APIManagementException;
      public Response getAllSubscriptionThrottlePolicies(String accept, MessageContext messageContext) throws APIManagementException;
      public Response getApplicationThrottlePolicies(String accept, MessageContext messageContext) throws APIManagementException;
      public Response getApplicationThrottlePolicyById(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response getCustomRuleById(String ruleId, MessageContext messageContext) throws APIManagementException;
      public Response getDenyPolicyById(String conditionId, MessageContext messageContext) throws APIManagementException;
      public Response getSubscriptionThrottlePolicyById(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response importThrottlingPolicy(InputStream fileInputStream, Attachment fileDetail, Boolean overwrite, MessageContext messageContext) throws APIManagementException;
      public Response removeAdvancedPolicy(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response removeApplicationThrottlePolicy(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response removeCustomRule(String ruleId, MessageContext messageContext) throws APIManagementException;
      public Response removeDenyPolicy(String conditionId, MessageContext messageContext) throws APIManagementException;
      public Response removeSubscriptionThrottlePolicy(String policyId, MessageContext messageContext) throws APIManagementException;
      public Response throttlingPolicySearch(String query, MessageContext messageContext) throws APIManagementException;
      public Response updateAdvancedPolicy(String policyId, String contentType, AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateApplicationThrottlePolicy(String policyId, String contentType, ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateCustomRule(String ruleId, String contentType, CustomRuleDTO customRuleDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateDenyPolicy(String conditionId, String contentType, BlockingConditionStatusDTO blockingConditionStatusDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateSubscriptionThrottlePolicy(String policyId, String contentType, SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO, MessageContext messageContext) throws APIManagementException;
}
