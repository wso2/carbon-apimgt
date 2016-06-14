/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.publisher.ThrottlingApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.GlobalThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.ThrottleMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class ThrottlingApiServiceImpl extends ThrottlingApiService {

    private static final Log log = LogFactory.getLog(ThrottlingApiServiceImpl.class);

    @Override
    public Response throttlingBlockingConditionsGet(String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override public Response throttlingBlockingConditionsPost(BlockingConditionDTO body, String contentType) {
        return null;
    }

    @Override
    public Response throttlingBlockingConditionsConditionIdPut(String conditionId,BlockingConditionDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingBlockingConditionsConditionIdDelete(String conditionId,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override 
    public Response throttlingPoliciesAdvancedPoliciesGet(String accept, String ifNoneMatch,
            String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            APIPolicy[] apiPolicies = (APIPolicy[])apiProvider.getPolicies(userName, PolicyConstants.POLICY_LEVEL_API);
            return Response.ok().entity(apiPolicies).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Advanced level policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    
    @Override
    public Response throttlingPoliciesAdvancedPoliciesPost(AdvancedThrottlePolicyDTO body,String contentType){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            APIPolicy apiPolicy = ThrottleMappingUtil.fromAdvancedPolicyDTOToPolicy(body);
            apiProvider.addPolicy(apiPolicy);
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!!!!!!!!!")).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding an Advanced level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
    @Override
    public Response throttlingPoliciesAdvancedPoliciesPolicyNamePut(String policyName,AdvancedThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesAdvancedPoliciesPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesApplicationGet(String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesApplicationPost(ApplicationThrottlePolicyDTO body,String contentType){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesApplicationPolicyNamePut(String policyName,ApplicationThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesApplicationPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesGlobalGet(String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesGlobalPost(GlobalThrottlePolicyDTO body,String contentType){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesGlobalPolicyNamePut(String policyName,GlobalThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesGlobalPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesSubscriptionGet(String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesSubscriptionPost(SubscriptionThrottlePolicyDTO body,String contentType){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesSubscriptionPolicyNamePut(String policyName,SubscriptionThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response throttlingPoliciesSubscriptionPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
