package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ThrottlingApiService {
    public abstract Response throttlingBlacklistConditionIdDelete(String conditionId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingBlacklistConditionIdGet(String conditionId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingBlacklistGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingBlacklistPost(BlockingConditionDTO body,String contentType);
    public abstract Response throttlingPoliciesAdvancedGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesAdvancedPolicyIdDelete(String policyId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesAdvancedPolicyIdGet(String policyId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesAdvancedPolicyIdPut(String policyId,AdvancedThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesAdvancedPost(AdvancedThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesApplicationGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesApplicationPolicyIdDelete(String policyId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesApplicationPolicyIdGet(String policyId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesApplicationPolicyIdPut(String policyId,ApplicationThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesApplicationPost(ApplicationThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesCustomGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesCustomPost(CustomRuleDTO body,String contentType);
    public abstract Response throttlingPoliciesCustomRuleIdDelete(String ruleId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesCustomRuleIdGet(String ruleId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesCustomRuleIdPut(String ruleId,CustomRuleDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesSubscriptionGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesSubscriptionPolicyIdDelete(String policyId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesSubscriptionPolicyIdGet(String policyId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesSubscriptionPolicyIdPut(String policyId,SubscriptionThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesSubscriptionPost(SubscriptionThrottlePolicyDTO body,String contentType);
}

