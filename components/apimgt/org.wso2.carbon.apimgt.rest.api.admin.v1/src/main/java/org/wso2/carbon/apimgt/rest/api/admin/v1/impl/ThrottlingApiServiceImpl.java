/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ThrottlingApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.ThrottlingCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.utils.RestApiAdminUtils;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * This is the service implementation class for Admin Portal Throttling related operations
 */
public class ThrottlingApiServiceImpl implements ThrottlingApiService {

    /**
     * Retrieves all Advanced level policies
     *
     * @param accept Accept header value
     * @return All matched Advanced Throttle policies to the given request
     * @throws APIManagementException When an internal error occurs
     */
    @Override
    public Response getAllAdvancedPolicy(String accept, MessageContext messageContext) throws APIManagementException {
        AdvancedThrottlePolicyListDTO listDTO = ThrottlingCommonImpl.getAllAdvancedPolicy();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Add an Advanced Level Throttle Policy
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @return Created policy along with the location of it with Location header
     */
    @Override
    public Response addAdvancedPolicy(String contentType, AdvancedThrottlePolicyDTO body,
                                      MessageContext messageContext) throws APIManagementException {
        try {
            AdvancedThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.addAdvancedPolicy(body);
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_ADVANCED
                    + RestApiConstants.PATH_DELIMITER
                    + policyDTO.getPolicyId())).entity(policyDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Advanced Throttle policy location : " + body.getPolicyName();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get a specific Advanced Level Policy
     *
     * @param policyId uuid of the policy
     * @return Required policy specified by name
     */
    @Override
    public Response getAdvancedPolicyById(String policyId, MessageContext messageContext)
            throws APIManagementException {
        AdvancedThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.getAdvancedPolicyById(policyId);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Updates a given Advanced level policy specified by uuid
     *
     * @param policyId    uuid of the policy
     * @param body        DTO of policy to be updated
     * @param contentType Content-Type header
     * @return Updated policy
     */
    @Override
    public Response updateAdvancedPolicy(String policyId, String contentType,
                                         AdvancedThrottlePolicyDTO body, MessageContext messageContext)
            throws APIManagementException {
        AdvancedThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.updateAdvancedPolicy(policyId, body);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Delete an Advanced level policy specified by uuid
     *
     * @param policyId uuid of the policy
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response removeAdvancedPolicy(String policyId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ThrottlingCommonImpl.removeAdvancedPolicy(policyId, organization);
        return Response.ok().build();
    }

    /**
     * Retrieves all Application Throttle Policies
     *
     * @param accept Accept header value
     * @return Retrieves all Application Throttle Policies
     */
    @Override
    public Response getApplicationThrottlePolicies(String accept, MessageContext messageContext)
            throws APIManagementException {
        ApplicationThrottlePolicyListDTO listDTO = ThrottlingCommonImpl.getApplicationThrottlePolicies();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Add an Application Level Throttle Policy
     *
     * @param body        DTO of the Application Policy to add
     * @param contentType Content-Type header
     * @return Newly created Application Throttle Policy with the location with the Location header
     */
    @Override
    public Response addApplicationThrottlePolicy(String contentType, ApplicationThrottlePolicyDTO body,
                                                 MessageContext messageContext) throws APIManagementException {
        try {
            ApplicationThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.addApplicationThrottlePolicy(body);
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_APPLICATION
                    + RestApiConstants.PATH_DELIMITER
                    + policyDTO.getPolicyId())).entity(policyDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving Application Throttle policy location : " + body.getPolicyName();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get a specific Application Policy by its uuid
     *
     * @param policyId uuid of the policy
     * @return Matched Application Throttle Policy by the given name
     */
    @Override
    public Response getApplicationThrottlePolicyById(String policyId, MessageContext messageContext)
            throws APIManagementException {
        ApplicationThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.getApplicationThrottlePolicyById(policyId);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Updates a given Application level policy specified by uuid
     *
     * @param policyId    uuid of the policy
     * @param body        DTO of policy to be updated
     * @param contentType Content-Type header
     * @return Updated policy
     */
    @Override
    public Response updateApplicationThrottlePolicy(String policyId, String contentType,
                                                    ApplicationThrottlePolicyDTO body, MessageContext messageContext)
            throws APIManagementException {

        ApplicationThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.updateApplicationThrottlePolicy(policyId, body);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Delete an Application level policy specified by uuid
     *
     * @param policyId uuid of the policy
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response removeApplicationThrottlePolicy(String policyId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ThrottlingCommonImpl.removeApplicationThrottlePolicy(policyId, organization);
        return Response.ok().build();
    }

    /**
     * Retrieves all Subscription level policies
     *
     * @param accept Accept header value
     * @return All matched Subscription Throttle policies to the given request
     */
    @Override
    public Response getAllSubscriptionThrottlePolicies(String accept, MessageContext messageContext)
            throws APIManagementException {
        SubscriptionThrottlePolicyListDTO listDTO = ThrottlingCommonImpl.getAllSubscriptionThrottlePolicies();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Add a Subscription Level Throttle Policy
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @return Created policy along with the location of it with Location header
     */
    @Override
    public Response addSubscriptionThrottlePolicy(String contentType, SubscriptionThrottlePolicyDTO body,
                                                  MessageContext messageContext) throws APIManagementException {
        SubscriptionThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.addSubscriptionThrottlePolicy(body);
        try {
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_SUBSCRIPTION
                    + RestApiConstants.PATH_DELIMITER
                    + policyDTO.getPolicyId())).entity(policyDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving Subscription Throttle policy location : " + body.getPolicyName();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Returns list of throttling policy details filtered using query parameters
     *
     * @param query filtering parameters
     * @return Throttle Policies List filtered using query
     */
    @Override
    public Response throttlingPolicySearch(String query, MessageContext messageContext)
            throws APIManagementException {
        ThrottlePolicyDetailsListDTO resultListDTO = ThrottlingCommonImpl.throttlingPolicySearch(query);
        return Response.ok().entity(resultListDTO).build();
    }

    /**
     * Get a specific Subscription Policy by its uuid
     *
     * @param policyId uuid of the policy
     * @return Matched Subscription Throttle Policy by the given name
     */
    @Override
    public Response getSubscriptionThrottlePolicyById(String policyId, MessageContext messageContext)
            throws APIManagementException {
        SubscriptionThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.getSubscriptionThrottlePolicyById(policyId);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Updates a given Subscription level policy specified by uuid
     *
     * @param policyId    u
     * @param body        DTO of policy to be updated
     * @param contentType Content-Type header
     * @return Updated policy
     */
    @Override
    public Response updateSubscriptionThrottlePolicy(String policyId, String contentType,
                                                     SubscriptionThrottlePolicyDTO body, MessageContext messageContext)
            throws APIManagementException {
        SubscriptionThrottlePolicyDTO policyDTO = ThrottlingCommonImpl.updateSubscriptionThrottlePolicy(policyId, body);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Delete a Subscription level policy specified by uuid
     *
     * @param policyId uuid of the policyu
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response removeSubscriptionThrottlePolicy(String policyId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ThrottlingCommonImpl.removeSubscriptionThrottlePolicy(policyId, organization);
        return Response.ok().build();
    }

    /**
     * Retrieves all Global level policies
     *
     * @param accept Accept header value
     * @return All matched Global Throttle policies to the given request
     */
    @Override
    public Response getAllCustomRoles(String accept, MessageContext messageContext) throws APIManagementException {
        CustomRuleListDTO listDTO = ThrottlingCommonImpl.getAllCustomRules();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Add an Global Level Throttle Policy
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @return Created policy along with the location of it with Location header
     */
    @Override
    public Response addCustomRule(String contentType, CustomRuleDTO body, MessageContext messageContext)
            throws APIManagementException {

        RestApiAdminUtils
                .validateCustomRuleRequiredProperties(body, (String) messageContext.get(Message.HTTP_REQUEST_METHOD));

        try {
            CustomRuleDTO policyDTO = ThrottlingCommonImpl.addCustomRule(body);
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_GLOBAL
                            + RestApiConstants.PATH_DELIMITER + policyDTO.getPolicyId()))
                    .entity(policyDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Global Throttle policy location : " + body.getPolicyName();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get a specific custom rule by its name
     *
     * @param ruleId uuid of the policy
     * @return Matched Global Throttle Policy by the given name
     */
    @Override
    public Response getCustomRuleById(String ruleId, MessageContext messageContext) throws APIManagementException {
        CustomRuleDTO policyDTO = ThrottlingCommonImpl.getCustomRuleById(ruleId);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Updates a given Global level policy/custom rule specified by uuid
     *
     * @param ruleId      uuid of the policy
     * @param body        DTO of policy to be updated
     * @param contentType Content-Type header
     * @return Updated policy
     */
    @Override
    public Response updateCustomRule(String ruleId, String contentType, CustomRuleDTO body,
                                     MessageContext messageContext) throws APIManagementException {

        RestApiAdminUtils
                .validateCustomRuleRequiredProperties(body, (String) messageContext.get(Message.HTTP_REQUEST_METHOD));
        CustomRuleDTO policyDTO = ThrottlingCommonImpl.updateCustomRule(ruleId, body);
        return Response.ok().entity(policyDTO).build();
    }

    /**
     * Delete a Global level policy/custom rule specified by uuid
     *
     * @param ruleId uuid of the policy
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response removeCustomRule(String ruleId, MessageContext messageContext) throws APIManagementException {
        ThrottlingCommonImpl.removeCustomRule(ruleId);
        return Response.ok().build();
    }

    /**
     * Export a Throttling Policy by the policy name with/without specifying the policy type
     * If policy type is not specified first found throttling policy is returned
     *
     * @param policyId   UUID of the throttling policy to be exported(for future use)
     * @param policyName Name of the policy to be exported
     * @param type       type of the policy to be exported
     * @param format     format of the policy details
     * @return Throttling Policy details in ExportThrottlePolicyDTO format
     */
    @Override
    public Response exportThrottlingPolicy(String policyId, String policyName, String type, String format,
                                           MessageContext messageContext) throws APIManagementException {
        ExportThrottlePolicyDTO exportPolicy = ThrottlingCommonImpl.exportThrottlingPolicy(policyName, type);
        return Response.ok().entity(exportPolicy).build();
    }

    /**
     * Imports a Throttling policy with the overwriting capability
     *
     * @param fileInputStream Input stream from the REST request
     * @param fileDetail      exportThrottlePolicyDTO Exported Throttling policy details
     * @param overwrite       User can either update an existing throttling policy with the same name or let the conflict happen
     * @return Response with message indicating the status of the importation and the imported/updated policy name
     */
    @Override
    public Response importThrottlingPolicy(InputStream fileInputStream, Attachment fileDetail,
                                           Boolean overwrite, MessageContext messageContext) throws APIManagementException {
        String fileName = fileDetail.getContentDisposition().getFilename();
        Map<String, Object> policyResponse = ThrottlingCommonImpl
                .importThrottlingPolicy(fileInputStream, fileName, overwrite);
        return Response.ok().entity(policyResponse.get("message")).build();

    }

    /**
     * Retrieves all Block Conditions
     *
     * @param accept Accept header value
     * @return All matched block conditions to the given request
     */
    @Override
    public Response getAllDenyPolicies(String accept, MessageContext messageContext) throws APIManagementException {
        BlockingConditionListDTO listDTO = ThrottlingCommonImpl.getAllDenyPolicies();
        return Response.ok().entity(listDTO).build();
    }

    /**
     * Add a Block Condition
     *
     * @param body        DTO of new block condition to be created
     * @param contentType Content-Type header
     * @return Created block condition along with the location of it with Location header
     */
    @Override
    public Response addDenyPolicy(String contentType, BlockingConditionDTO body,
                                  MessageContext messageContext) throws APIManagementException {
        try {
            BlockingConditionDTO dto = ThrottlingCommonImpl.addDenyPolicy(body);
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_BLOCK_CONDITIONS
                    + RestApiConstants.PATH_DELIMITER
                    + dto.getConditionId())).entity(dto).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Blocking Condition resource location: Condition type: "
                    + body.getConditionType() + ", " + "value: " + body.getConditionValue() + ". " + e.getMessage();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get a specific Block condition by its id
     *
     * @param conditionId Id of the block condition
     * @return Matched block condition for the given Id
     */
    @Override
    public Response getDenyPolicyById(String conditionId, MessageContext messageContext) throws APIManagementException {

        BlockingConditionDTO dto = ThrottlingCommonImpl.getDenyPolicyById(conditionId);
        return Response.ok().entity(dto).build();
    }

    /**
     * Delete a block condition specified by the condition Id
     *
     * @param conditionId Id of the block condition
     * @return 200 OK response if successfully deleted the block condition
     */
    @Override
    public Response removeDenyPolicy(String conditionId, MessageContext messageContext) throws APIManagementException {
        ThrottlingCommonImpl.removeDenyPolicy(conditionId);
        return Response.ok().build();
    }

    /**
     * Updates an existing condition status of a blocking condition
     *
     * @param conditionId Id of the block condition
     * @param body        content to update
     * @param contentType Content-Type header
     * @return 200 response if successful
     */
    @Override
    public Response updateDenyPolicy(String conditionId, String contentType,
                                     BlockingConditionStatusDTO body, MessageContext messageContext)
            throws APIManagementException {
        BlockingConditionDTO dto = ThrottlingCommonImpl.updateDenyPolicy(conditionId, body);
        return Response.ok().entity(dto).build();
    }
}
