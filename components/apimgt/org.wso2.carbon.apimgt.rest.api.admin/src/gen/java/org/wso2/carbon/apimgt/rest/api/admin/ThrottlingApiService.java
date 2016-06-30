package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.GlobalThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.GlobalThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ThrottlingApiService {
    public abstract Response throttlingBlacklistGet(Integer limit,Integer offset,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingBlacklistPost(BlockingConditionDTO body,String contentType);
    public abstract Response throttlingBlacklistConditionIdGet(String conditionId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingBlacklistConditionIdPut(String conditionId,BlockingConditionDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingBlacklistConditionIdDelete(String conditionId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesAdvancedPoliciesGet(Integer limit,Integer offset,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesAdvancedPoliciesPost(AdvancedThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesAdvancedPoliciesPolicyNameGet(String policyName,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesAdvancedPoliciesPolicyNamePut(String policyName,AdvancedThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesAdvancedPoliciesPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesApplicationGet(Integer limit,Integer offset,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesApplicationPost(ApplicationThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesApplicationPolicyNameGet(String policyName,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesApplicationPolicyNamePut(String policyName,ApplicationThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesApplicationPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesGlobalGet(Integer limit,Integer offset,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesGlobalPost(GlobalThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesGlobalPolicyNameGet(String policyName,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesGlobalPolicyNamePut(String policyName,GlobalThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesGlobalPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesSubscriptionGet(Integer limit,Integer offset,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesSubscriptionPost(SubscriptionThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesSubscriptionPolicyNameGet(String policyName,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesSubscriptionPolicyNamePut(String policyName,SubscriptionThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesSubscriptionPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince);
}

