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

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-05-03T09:48:03.292+05:30")
public abstract class PoliciesApiService {
    public abstract Response policiesThrottlingAdvancedGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingAdvancedPolicyIdDelete(String policyId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingAdvancedPolicyIdGet(String policyId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingAdvancedPolicyIdPut(String policyId
 ,TierDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingAdvancedPost(TierDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationPolicyIdDelete(String policyId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationPolicyIdGet(String policyId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationPolicyIdPut(String policyId
 ,TierDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationPost(TierDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingCustomGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingCustomPost(CustomRuleDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingCustomRuleIdDelete(String ruleId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingCustomRuleIdGet(String ruleId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingCustomRuleIdPut(String ruleId
 ,CustomRuleDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionPolicyIdDelete(String policyId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionPolicyIdGet(String policyId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionPolicyIdPut(String policyId
 ,TierDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionPost(TierDTO body
 ,String contentType
 , Request request) throws NotFoundException;
}
