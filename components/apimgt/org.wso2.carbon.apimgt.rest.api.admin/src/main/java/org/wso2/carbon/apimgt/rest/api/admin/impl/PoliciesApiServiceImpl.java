/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.AdvancedThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.ApplicationThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.CustomPolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.SubscriptionThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoliciesApiServiceImpl extends PoliciesApiService {

    private static final Logger log = LoggerFactory.getLogger(PoliciesApiServiceImpl.class);

    /**
     *
     * @param accept            Accept header value
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedGet(String accept, String ifNoneMatch, String ifModifiedSince,
            Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received Advance Throttle Policy GET request");
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<APIPolicy> policies = apiMgtAdminService.getApiPolicies();
            AdvancedThrottlePolicyListDTO advancedThrottlePolicyListDTO = AdvancedThrottlePolicyMappingUtil
                    .fromAPIPolicyArrayToListDTO(policies);
            return Response.ok().entity(advancedThrottlePolicyListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Advance Policies";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     *
     * @param policyId          Uuid of the Advanced policy.
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        APIMgtAdminService.PolicyLevel tierLevel = APIMgtAdminService.PolicyLevel.api;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Advanced policy.
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.info("Received Advanced Policy Get request. Policy uuid: " + policyId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            APIPolicy policy = apiMgtAdminService.getApiPolicyByUuid(policyId);
            return Response.status(Response.Status.OK).entity(AdvancedThrottlePolicyMappingUtil.
                    fromAdvancedPolicyToDTO(policy)).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting Advanced Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     *
     * @param policyId          Uuid of the Advanced policy.
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response policiesThrottlingAdvancedPolicyIdPut(String policyId, AdvancedThrottlePolicyDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        APIMgtAdminService.PolicyLevel tierLevel = APIMgtAdminService.PolicyLevel.api;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy PUT request " + body + " with tierLevel = " + tierLevel);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(body);
            apiPolicy.setUuid(policyId);
            apiMgtAdminService.updateApiPolicy(apiPolicy);
            return Response.status(Response.Status.CREATED).entity(AdvancedThrottlePolicyMappingUtil
                    .fromAdvancedPolicyToDTO(apiMgtAdminService.getApiPolicyByUuid(policyId))).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while updating Advanced Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        }
    }

    /**
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedPost(AdvancedThrottlePolicyDTO body, String contentType,
            Request request) throws NotFoundException {
        APIMgtAdminService.PolicyLevel tierLevel = APIMgtAdminService.PolicyLevel.api;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }

        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy PUT request " + body + " with tierLevel = " + tierLevel);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(body);
            String policyId = apiMgtAdminService.addApiPolicy(apiPolicy);
            return Response.status(Response.Status.CREATED).entity(AdvancedThrottlePolicyMappingUtil
                    .fromAdvancedPolicyToDTO(apiMgtAdminService.getApiPolicyByUuid(policyId)))
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding Advanced Throttle Policy, policy name: " + body
                    .getPolicyName();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Returns all Application Policies deployed in the system
     *
     * @param accept            Accept header value
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object  Response containing the Application Policy list {@link ApplicationThrottlePolicyListDTO}
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationGet(String accept, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {

        if (log.isDebugEnabled()) {
            log.debug("Received Application Throttle Policy GET request");
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<ApplicationPolicy> policies = apiMgtAdminService.getApplicationPolicies();
            ApplicationThrottlePolicyListDTO applicationThrottlePolicyListDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationPolicyArrayToListDTO(policies);
            return Response.ok().entity(applicationThrottlePolicyListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Application Policies";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Deletes an Application throttle policy
     *
     * @param policyId          Uuid of the Application policy.
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {

        APIMgtAdminService.PolicyLevel tierLevel = APIMgtAdminService.PolicyLevel.application;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    /**
     * Returns a matching Application policy for the given policy id @{policyId}
     *
     * @param policyId          Uuid of the Application policy
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object  Response with matching {@link ApplicationThrottlePolicyDTO} object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {

        if (log.isDebugEnabled()) {
            log.info("Received Application Policy Get request. Policy uuid: " + policyId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            Policy applicationPolicy = apiMgtAdminService.getApplicationPolicyByUuid(policyId);
            return Response.status(Response.Status.OK).entity(ApplicationThrottlePolicyMappingUtil.
                    fromApplicationThrottlePolicyToDTO(applicationPolicy)).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting Application Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Updates/adds a new Application throttle policy to the system
     *
     * @param policyId          Uuid of the policy.
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object  response object with the updated application throttle policy resource
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationPolicyIdPut(String policyId,
            ApplicationThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince,
            Request request) throws NotFoundException {

        APIMgtAdminService.PolicyLevel tierLevel = APIMgtAdminService.PolicyLevel.application;
        if (log.isDebugEnabled()) {
            log.info("Received Application Policy PUT request " + body + " with tierLevel = " + tierLevel);
        }

        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            ApplicationPolicy applicationPolicy = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationThrottlePolicyDTOToModel(body);
            applicationPolicy.setUuid(policyId);
            apiMgtAdminService.updateApplicationPolicy(applicationPolicy);
            return Response.status(Response.Status.OK).entity(ApplicationThrottlePolicyMappingUtil.
                    fromApplicationThrottlePolicyToDTO(apiMgtAdminService.getApplicationPolicyByUuid(policyId))).
                    build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while updating Application Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves all custom policies.
     *
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return All matched Global Throttle policies to the given request
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response policiesThrottlingCustomGet(String accept, String ifNoneMatch, String ifModifiedSince,
            Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received Custom Policy GET request.");
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<CustomPolicy> policies = apiMgtAdminService.getCustomRules();
            CustomRuleListDTO customRuleListDTO = CustomPolicyMappingUtil.fromCustomPolicyArrayToListDTO(policies);
            return Response.ok().entity(customRuleListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving custom policies";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Add a Custom Policy.
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @param request     msf4j request object
     * @return Created policy along with the location of it with Location header
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response policiesThrottlingCustomPost(CustomRuleDTO body, String contentType, Request request)
            throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received Custom Policy POST request " + body);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            CustomPolicy customPolicy = CustomPolicyMappingUtil.fromCustomPolicyDTOToModel(body);
            String uuid = apiMgtAdminService.addCustomRule(customPolicy);
            return Response.status(Response.Status.CREATED)
                    .entity(CustomPolicyMappingUtil.fromCustomPolicyToDTO(apiMgtAdminService.getCustomRuleByUUID(uuid)))
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding custom policy, policy name: " + body.getPolicyName();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Delete a custom rule specified by uuid.
     *
     * @param ruleId            uuid of the policy
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return 200 OK response if successfully deleted the policy
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response policiesThrottlingCustomRuleIdDelete(String ruleId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received Custom Policy DELETE request with rule ID = " + ruleId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            apiMgtAdminService.deleteCustomRule(ruleId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while deleting a custom policy uuid : " + ruleId;
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.TIER, ruleId);
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil
                    .getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Get a specific custom rule by its id.
     *
     * @param ruleId          uuid of the policy
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return Matched Global Throttle Policy by the given name
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response policiesThrottlingCustomRuleIdGet(String ruleId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received Custom Policy GET request with rule ID = " + ruleId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            CustomPolicy customPolicy = apiMgtAdminService.getCustomRuleByUUID(ruleId);
            CustomRuleDTO dto = CustomPolicyMappingUtil.fromCustomPolicyToDTO(customPolicy);
            return Response.status(Response.Status.OK).entity(dto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting custom policy. policy uuid: " + ruleId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Updates a given Global level policy/custom rule specified by uuid.
     *
     * @param ruleId            uuid of the policy
     * @param body              DTO of policy to be updated
     * @param contentType       Content-Type header
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Updated policy
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response policiesThrottlingCustomRuleIdPut(String ruleId, CustomRuleDTO body, String contentType,
            String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received Custom Policy PUT request " + body + " with rule ID = " + ruleId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            CustomPolicy customPolicy = CustomPolicyMappingUtil.fromCustomPolicyDTOToModel(body);
            customPolicy.setUuid(ruleId);
            apiMgtAdminService.updateCustomRule(customPolicy);
            return Response.status(Response.Status.OK).entity(CustomPolicyMappingUtil
                    .fromCustomPolicyToDTO(apiMgtAdminService.getCustomRuleByUUID(ruleId))).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while updating Custom Policy. policy ID: " + ruleId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Adds a new Application throttle policy to the system
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object  response object with the created application throttle policy resource
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationPost(ApplicationThrottlePolicyDTO body, String contentType,
            Request request) throws NotFoundException {

        APIMgtAdminService.PolicyLevel tierLevel = APIMgtAdminService.PolicyLevel.application;
        if (log.isDebugEnabled()) {
            log.info("Received Application Policy PUT request " + body + " with tierLevel = " + tierLevel);
        }

        String policyName = null;
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            ApplicationPolicy applicationPolicy = ApplicationThrottlePolicyMappingUtil.
                    fromApplicationThrottlePolicyDTOToModel(body);
            policyName = applicationPolicy.getPolicyName();
            String applicationPolicyUuid = apiMgtAdminService.addApplicationPolicy(applicationPolicy);
            return Response.status(Response.Status.CREATED).entity(ApplicationThrottlePolicyMappingUtil
                    .fromApplicationThrottlePolicyToDTO(apiMgtAdminService
                            .getApplicationPolicyByUuid(applicationPolicyUuid))).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding Application Policy. policy name: " + policyName;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     *
     * @param accept            Accept header value
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionGet(String accept, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received Application Throttle Policy GET request");
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<SubscriptionPolicy> policies = apiMgtAdminService.getSubscriptionPolicies();
            SubscriptionThrottlePolicyListDTO subscriptionThrottlePolicyListDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionPolicyArrayToListDTO(policies);
            return Response.ok().entity(subscriptionThrottlePolicyListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Application Policies";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     *
     * @param policyId          Uuid of the Subscription policy.
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        APIMgtAdminService.PolicyLevel tierLevel = APIMgtAdminService.PolicyLevel.subscription;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Subscription policy
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.info("Received Subscription Policy Get request. Policy uuid: " + policyId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            SubscriptionPolicy subscriptionPolicy = apiMgtAdminService.getSubscriptionPolicyByUuid(policyId);
            SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyToDTO(subscriptionPolicy);
            return Response.status(Response.Status.OK).entity(subscriptionThrottlePolicyDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting Subscription Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     *
     * @param policyId          Uuid of the Subscription policy.
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response policiesThrottlingSubscriptionPolicyIdPut(String policyId,
            SubscriptionThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince,
            Request request) throws NotFoundException {
        APIMgtAdminService.PolicyLevel tierLevel = APIMgtAdminService.PolicyLevel.subscription;
        if (log.isDebugEnabled()) {
            log.info("Received Subscription Policy PUT request " + body + " with tierLevel = " + tierLevel);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            SubscriptionPolicy subscriptionPolicy = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyDTOToModel(body);
            subscriptionPolicy.setUuid(policyId);
            apiMgtAdminService.updateSubscriptionPolicy(subscriptionPolicy);
            return Response.status(Response.Status.CREATED).entity(SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyToDTO(apiMgtAdminService.
                            getSubscriptionPolicyByUuid(subscriptionPolicy.getUuid()))).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while updating Application Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        }
    }

    /**
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionPost(SubscriptionThrottlePolicyDTO body, String contentType,
            Request request) throws NotFoundException {
        APIMgtAdminService.PolicyLevel tierLevel = APIMgtAdminService.PolicyLevel.subscription;
        if (log.isDebugEnabled()) {
            log.info("Received Subscription Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            SubscriptionPolicy subscriptionPolicy = SubscriptionThrottlePolicyMappingUtil.
                    fromSubscriptionThrottlePolicyDTOToModel(body);
            String policyId = apiMgtAdminService.addSubscriptionPolicy(subscriptionPolicy);
            return Response.status(Response.Status.CREATED).entity(SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyToDTO(apiMgtAdminService
                            .getSubscriptionPolicyByUuid(policyId))).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding Subscription Policy. policy name: " +
                    body.getPolicyName();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    private Response deletePolicy(String policyId, APIMgtAdminService.PolicyLevel tierLevel) {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            apiMgtAdminService.deletePolicyByUuid(policyId, tierLevel);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while deleting a Policy uuid : " + policyId;
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.TIER, policyId);
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil
                    .getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

}
