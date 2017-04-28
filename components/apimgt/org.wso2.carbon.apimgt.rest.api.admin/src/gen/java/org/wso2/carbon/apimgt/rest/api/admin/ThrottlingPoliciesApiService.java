package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierListDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-28T14:28:58.278+05:30")
public abstract class ThrottlingPoliciesApiService {
    public abstract Response throttlingPoliciesAdvancedGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesAdvancedPolicyIdDelete(String policyId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesAdvancedPolicyIdGet(String policyId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesAdvancedPolicyIdPut(String policyId
 ,TierDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesAdvancedPost(TierDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesApplicationGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesApplicationPolicyIdDelete(String policyId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesApplicationPolicyIdGet(String policyId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesApplicationPolicyIdPut(String policyId
 ,TierDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesApplicationPost(TierDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesCustomGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesCustomPost(CustomRuleDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesCustomRuleIdDelete(String ruleId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesCustomRuleIdGet(String ruleId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesCustomRuleIdPut(String ruleId
 ,CustomRuleDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesSubscriptionGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesSubscriptionPolicyIdDelete(String policyId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesSubscriptionPolicyIdGet(String policyId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesSubscriptionPolicyIdPut(String policyId
 ,TierDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response throttlingPoliciesSubscriptionPost(TierDTO body
 ,String contentType
 , Request request) throws NotFoundException;
}
