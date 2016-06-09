package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.QuotaPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.GlobalThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.GlobalThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionThrottlePolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ThrottlingApiService {
    public abstract Response throttlingBlockingConditionsGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingBlockingConditionsPost(QuotaPolicyDTO body,String contentType);
    public abstract Response throttlingBlockingConditionsConditionIdPut(String conditionId,BlockingConditionDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingBlockingConditionsConditionIdDelete(String conditionId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesAdvancedPoliciesGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesAdvancedPoliciesPost(AdvancedThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesAdvancedPoliciesPolicyNamePut(String policyName,AdvancedThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesAdvancedPoliciesPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesApplicationGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesApplicationPost(ApplicationThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesApplicationPolicyNamePut(String policyName,ApplicationThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesApplicationPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesGlobalGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesGlobalPost(GlobalThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesGlobalPolicyNamePut(String policyName,GlobalThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesGlobalPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesSubscriptionGet(String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response throttlingPoliciesSubscriptionPost(SubscriptionThrottlePolicyDTO body,String contentType);
    public abstract Response throttlingPoliciesSubscriptionPolicyNamePut(String policyName,SubscriptionThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response throttlingPoliciesSubscriptionPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince);
}

