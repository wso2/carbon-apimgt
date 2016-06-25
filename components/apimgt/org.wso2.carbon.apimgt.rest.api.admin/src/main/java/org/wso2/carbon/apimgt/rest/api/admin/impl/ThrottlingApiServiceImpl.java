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

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.policy.*;
import org.wso2.carbon.apimgt.rest.api.admin.ThrottlingApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.throttling.*;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class ThrottlingApiServiceImpl extends ThrottlingApiService {

    private static final Log log = LogFactory.getLog(ThrottlingApiServiceImpl.class);

    @Override
    public Response throttlingPoliciesAdvancedPoliciesGet(Integer limit, Integer offset, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        try {
            //todo add sorting, limit, offset
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            APIPolicy[] apiPolicies = (APIPolicy[])apiProvider.getPolicies(userName, PolicyConstants.POLICY_LEVEL_API);
            AdvancedThrottlePolicyListDTO listDTO = AdvancedThrottlePolicyMappingUtil
                    .fromAPIPolicyArrayToListDTO(apiPolicies);
            return Response.ok().entity(listDTO).build();
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
            String userName = RestApiUtil.getLoggedInUsername();
            APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(body);
            apiProvider.addPolicy(apiPolicy);

            //retrieve the new policy and send back as the response
            APIPolicy newApiPolicy = apiProvider.getAPIPolicy(userName, body.getPolicyName());
            AdvancedThrottlePolicyDTO policyDTO = AdvancedThrottlePolicyMappingUtil
                    .fromAdvancedPolicyToDTO(newApiPolicy);
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_ADVANCED + "/" + policyDTO
                            .getPolicyName())).entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding an Advanced level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Advanced Throttle policy location : " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingPoliciesAdvancedPoliciesPolicyNameGet(String policyName, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            APIPolicy apiPolicy = apiProvider.getAPIPolicy(userName, policyName);
            AdvancedThrottlePolicyDTO policyDTO = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToDTO(apiPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Advanced level policy : " + policyName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }


    @Override
    public Response throttlingPoliciesAdvancedPoliciesPolicyNamePut(String policyName,AdvancedThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(body);
            apiProvider.updatePolicy(apiPolicy);

            //retrieve the new policy and send back as the response
            APIPolicy newApiPolicy = apiProvider.getAPIPolicy(userName, body.getPolicyName());
            AdvancedThrottlePolicyDTO policyDTO = AdvancedThrottlePolicyMappingUtil
                    .fromAdvancedPolicyToDTO(newApiPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Advanced level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    //todo  test this. This failed in 24/06 pack
    @Override
    public Response throttlingPoliciesAdvancedPoliciesPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince){
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            apiProvider.deletePolicy(userName, PolicyConstants.POLICY_LEVEL_API, policyName);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Advanced level policy : " + policyName; //todo
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

   
    @Override 
    public Response throttlingPoliciesApplicationGet(Integer limit, Integer offset, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        try {
            //todo add sorting, limit, offset
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            ApplicationPolicy[] appPolicies = (ApplicationPolicy[])apiProvider.getPolicies(userName, PolicyConstants.POLICY_LEVEL_APP);
            ApplicationThrottlePolicyListDTO listDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationPolicyArrayToListDTO(appPolicies);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Application level policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }


    @Override
    public Response throttlingPoliciesApplicationPost(ApplicationThrottlePolicyDTO body,String contentType){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            ApplicationPolicy appPolicy = ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyDTOToModel(
                    body);
            apiProvider.addPolicy(appPolicy);

            //retrieve the new policy and send back as the response
            ApplicationPolicy newAppPolicy = apiProvider.getApplicationPolicy(userName,
                    body.getPolicyName());
            ApplicationThrottlePolicyDTO policyDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationThrottlePolicyToDTO(newAppPolicy);
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_APPLICATION + "/" + policyDTO
                            .getPolicyName())).entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding an Application level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Application Throttle policy location : " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    
    @Override
    public Response throttlingPoliciesApplicationPolicyNameGet(String policyName, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            ApplicationPolicy appPolicy = apiProvider.getApplicationPolicy(userName, policyName);
            ApplicationThrottlePolicyDTO policyDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationThrottlePolicyToDTO(appPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Application level policy: " + policyName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingPoliciesApplicationPolicyNamePut(String policyName,ApplicationThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            ApplicationPolicy appPolicy = ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyDTOToModel(
                    body);
            apiProvider.updatePolicy(appPolicy);

            //retrieve the new policy and send back as the response
            ApplicationPolicy newAppPolicy = apiProvider.getApplicationPolicy(userName, body.getPolicyName());
            ApplicationThrottlePolicyDTO policyDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationThrottlePolicyToDTO(newAppPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Application level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    //todo  test this. This failed in 24/06 pack
    @Override
    public Response throttlingPoliciesApplicationPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince){
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            apiProvider.deletePolicy(userName, PolicyConstants.POLICY_LEVEL_APP, policyName);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Application level policy : " + policyName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }


    @Override
    public Response throttlingPoliciesSubscriptionGet(Integer limit, Integer offset, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        try {
            //todo add sorting, limit, offset
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            SubscriptionPolicy[] subscriptionPolicies = (SubscriptionPolicy[])apiProvider.getPolicies(userName, PolicyConstants.POLICY_LEVEL_SUB);
            SubscriptionThrottlePolicyListDTO listDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionPolicyArrayToListDTO(subscriptionPolicies);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException | ParseException e) {
            String errorMessage = "Error while retrieving Subscription level policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingPoliciesSubscriptionPost(SubscriptionThrottlePolicyDTO body,String contentType){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            SubscriptionPolicy subscriptionPolicy = SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyDTOToModel(
                    body);
            apiProvider.addPolicy(subscriptionPolicy);

            //retrieve the new policy and send back as the response
            SubscriptionPolicy newSubscriptionPolicy = apiProvider.getSubscriptionPolicy(userName,
                    body.getPolicyName());
            SubscriptionThrottlePolicyDTO policyDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyToDTO(newSubscriptionPolicy);
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_SUBSCRIPTION + "/" + policyDTO
                            .getPolicyName())).entity(policyDTO).build();
        } catch (APIManagementException | ParseException e) {
            String errorMessage = "Error while adding a Subscription level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Subscription Throttle policy location : " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingPoliciesSubscriptionPolicyNameGet(String policyName, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            SubscriptionPolicy subscriptionPolicy = apiProvider.getSubscriptionPolicy(userName, policyName);
            SubscriptionThrottlePolicyDTO policyDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyToDTO(subscriptionPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException | ParseException e) {
            String errorMessage = "Error while retrieving Subscription level policy: " + policyName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }


    @Override
    public Response throttlingPoliciesSubscriptionPolicyNamePut(String policyName,SubscriptionThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            SubscriptionPolicy subscriptionPolicy = SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyDTOToModel(
                    body);
            apiProvider.updatePolicy(subscriptionPolicy);

            //retrieve the new policy and send back as the response
            SubscriptionPolicy newSubscriptionPolicy = apiProvider.getSubscriptionPolicy(userName, body.getPolicyName());
            SubscriptionThrottlePolicyDTO policyDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyToDTO(newSubscriptionPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException | ParseException e) {
            String errorMessage = "Error while updating Subscription level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }


    //todo  test this. This failed in 24/06 pack
    @Override
    public Response throttlingPoliciesSubscriptionPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince){
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            apiProvider.deletePolicy(userName, PolicyConstants.POLICY_LEVEL_SUB, policyName);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Subscription level policy : " + policyName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }


    @Override
    public Response throttlingPoliciesGlobalGet(Integer limit, Integer offset, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        try {
            //todo add sorting, limit, offset
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            GlobalPolicy[] globalPolicies = (GlobalPolicy[])apiProvider.getPolicies(userName, PolicyConstants.POLICY_LEVEL_GLOBAL);
            GlobalThrottlePolicyListDTO listDTO = GlobalThrottlePolicyMappingUtil
                    .fromGlobalPolicyArrayToListDTO(globalPolicies);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Global level policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingPoliciesGlobalPost(GlobalThrottlePolicyDTO body,String contentType){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            GlobalPolicy globalPolicy = GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyDTOToModel(
                    body);
            apiProvider.addPolicy(globalPolicy);

            //retrieve the new policy and send back as the response
            GlobalPolicy newGlobalPolicy = apiProvider.getGlobalPolicy(body.getPolicyName());
            GlobalThrottlePolicyDTO policyDTO = GlobalThrottlePolicyMappingUtil
                    .fromGlobalThrottlePolicyToDTO(newGlobalPolicy);
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_GLOBAL + "/" + policyDTO
                            .getPolicyName())).entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding a Global level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Global Throttle policy location : " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingPoliciesGlobalPolicyNameGet(String policyName, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            GlobalPolicy globalPolicy = apiProvider.getGlobalPolicy(policyName);
            GlobalThrottlePolicyDTO policyDTO = GlobalThrottlePolicyMappingUtil
                    .fromGlobalThrottlePolicyToDTO(globalPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Global level policy: " + policyName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingPoliciesGlobalPolicyNamePut(String policyName,GlobalThrottlePolicyDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            GlobalPolicy globalPolicy = GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyDTOToModel(
                    body);
            apiProvider.updatePolicy(globalPolicy);

            //retrieve the new policy and send back as the response
            GlobalPolicy newGlobalPolicy= apiProvider.getGlobalPolicy(body.getPolicyName());
            GlobalThrottlePolicyDTO policyDTO = GlobalThrottlePolicyMappingUtil
                    .fromGlobalThrottlePolicyToDTO(newGlobalPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Global level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingPoliciesGlobalPolicyNameDelete(String policyName,String ifMatch,String ifUnmodifiedSince){
        try {
            //todo handle 404
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            apiProvider.deletePolicy(userName, PolicyConstants.POLICY_LEVEL_GLOBAL, policyName);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Global level policy : " + policyName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }


    @Override
    public Response throttlingBlockingConditionsGet(Integer limit, Integer offset, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        try {
            //todo add sorting, limit, offset
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            List<BlockConditionsDTO> blockConditions = apiProvider.getBlockConditions();
            BlockingConditionListDTO listDTO = BlockingConditionMappingUtil
                    .fromBlockConditionListToListDTO(blockConditions);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Block Conditions";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingBlockingConditionsPost(BlockingConditionDTO body, String contentType) { //todo BIG
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            apiProvider.addBlockCondition(body.getConditionType(), body.getConditionValue());
            //todo implement UUID for retrieving
            //retrieve the new blocking condition and send back as the response
            /*BlockConditionsDTO newBlockingCondition = apiProvider.getBlockCondition();
            GlobalThrottlePolicyDTO policyDTO = GlobalThrottlePolicyMappingUtil
                    .fromGlobalThrottlePolicyToDTO(newBlockingCondition);*/
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_BLOCK_CONDITIONS + "/" + "")).entity("").build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding Blocking Condition: " + ""; //todo
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Blocking Condition resource location : " + "" ; //todo
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingBlockingConditionsConditionIdGet(String conditionId, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            BlockConditionsDTO blockCondition = apiProvider.getBlockCondition(Integer.parseInt(conditionId)); //todo use uuid?
            BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(blockCondition);
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Block Condition. Id : " + conditionId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingBlockingConditionsConditionIdPut(String conditionId,BlockingConditionDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            apiProvider.updateBlockCondition(Integer.parseInt(conditionId), body.getEnabled().toString());

            BlockConditionsDTO updatedBlockCondition = apiProvider.getBlockCondition(Integer.parseInt(conditionId));
            return Response.ok().entity(updatedBlockCondition).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Block Condition. Id : " + conditionId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response throttlingBlockingConditionsConditionIdDelete(String conditionId,String ifMatch,String ifUnmodifiedSince){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            apiProvider.deleteBlockCondition(Integer.parseInt(conditionId));
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Block Condition. Id : " + conditionId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
    
}
