package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class PoliciesApiService {
    public abstract Response policiesThrottlingAdvancedGet(String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingAdvancedIdDelete(String id
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingAdvancedIdGet(String id
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingAdvancedIdPut(String id
 ,AdvancedThrottlePolicyDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingAdvancedPost(AdvancedThrottlePolicyDTO body
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationGet(String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationIdDelete(String id
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationIdGet(String id
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationIdPut(String id
 ,ApplicationThrottlePolicyDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingApplicationPost(ApplicationThrottlePolicyDTO body
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingCustomGet(String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingCustomPost(CustomRuleDTO body
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
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionGet(String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionIdDelete(String id
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionIdGet(String id
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionIdPut(String id
 ,SubscriptionThrottlePolicyDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesThrottlingSubscriptionPost(SubscriptionThrottlePolicyDTO body
 , Request request) throws NotFoundException;
}
