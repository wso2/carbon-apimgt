/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.admin.v1.common.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.apk.apimgt.api.*;
import org.wso2.apk.apimgt.api.model.BlockConditionsDTO;
import org.wso2.apk.apimgt.api.model.policy.*;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.RestApiAdminUtils;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.throttling.*;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.*;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThrottlingCommonImpl {

    private static final Log log = LogFactory.getLog(ThrottlingCommonImpl.class);
    private static final String ALL_TYPES = "all";
    private static final String EXISTS_CONSTANT = " already exists";

    private static final String DTO = "dto";
    private static final String MESSAGE = "message";

    private ThrottlingCommonImpl() {
    }

    /**
     * Get all advanced policies
     *
     * @return List of advanced throttling policies
     * @throws APIManagementException When an internal error occurs
     */
    public static AdvancedThrottlePolicyListDTO getAllAdvancedPolicy() throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        Policy[] apiPolicies = apiAdmin.getPolicies(tenantId, PolicyConstants.POLICY_LEVEL_API);
        List<APIPolicy> policies = new ArrayList<>();
        for (Policy policy : apiPolicies) {
            policies.add((APIPolicy) policy);
        }
        return AdvancedThrottlePolicyMappingUtil
                .fromAPIPolicyArrayToListDTO(policies.toArray(new APIPolicy[policies.size()]));
    }

    /**
     * Add new advanced policy
     *
     * @param body Advanced policy
     * @return Advanced throttle policy
     * @throws APIManagementException When advanced policy addition fails
     */
    public static AdvancedThrottlePolicyDTO addAdvancedPolicy(AdvancedThrottlePolicyDTO body)
            throws APIManagementException {

        RestApiAdminUtils.validateThrottlePolicyNameProperty(body.getPolicyName());

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(body);

        Policy policyIfExists = apiProvider.getAPIPolicy(userName, apiPolicy.getPolicyName());
        if (policyIfExists != null) {
            String error = "Advanced Policy with name " + apiPolicy.getPolicyName() + EXISTS_CONSTANT;
            throw new APIManagementException(error,
                    ExceptionCodes.from(ExceptionCodes.ADVANCED_POLICY_EXISTS, apiPolicy.getPolicyName()));
        }

        apiProvider.addPolicy(apiPolicy);
        APIPolicy newApiPolicy = apiProvider.getAPIPolicy(userName, body.getPolicyName());
        return AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToDTO(newApiPolicy);
    }

    /**
     * Get advanced policy by ID
     *
     * @param policyId Policy ID
     * @return Advanced throttle policy
     * @throws APIManagementException When getting advanced policy fails
     */
    public static AdvancedThrottlePolicyDTO getAdvancedPolicyById(String policyId) throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //This will give PolicyNotFoundException if there's no policy exists with UUID
            APIPolicy apiPolicy = apiProvider.getAPIPolicyByUUID(policyId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(apiPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_ADVANCED_POLICY, policyId));
            }
            return AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToDTO(apiPolicy);
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_ADVANCED_POLICY, policyId));
        }
    }

    /**
     * Update advanced policy
     *
     * @param policyId Policy ID
     * @param body     Advanced policy
     * @return Updated advanced policy
     * @throws APIManagementException When policy update fails
     */
    public static AdvancedThrottlePolicyDTO updateAdvancedPolicy(String policyId, AdvancedThrottlePolicyDTO body)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //will give PolicyNotFoundException if there's no policy exists with UUID
            APIPolicy existingPolicy = apiProvider.getAPIPolicyByUUID(policyId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(existingPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_ADVANCED_POLICY, policyId));
            }

            //overridden parameters
            body.setPolicyId(policyId);
            body.setPolicyName(existingPolicy.getPolicyName());

            //update the policy
            APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(body);
            apiProvider.updatePolicy(apiPolicy);

            //retrieve the new policy and send back as the response
            APIPolicy newApiPolicy = apiProvider.getAPIPolicyByUUID(policyId);
            return AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToDTO(newApiPolicy);
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_ADVANCED_POLICY, policyId));
        }
    }

    /**
     * Delete an advanced policy
     *
     * @param policyId     Policy ID
     * @param organization Tenant organization
     * @throws APIManagementException When an internal error occurs
     */
    public static void removeAdvancedPolicy(String policyId, String organization) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();

        APIPolicy existingPolicy = null;
        try {
            existingPolicy = apiProvider.getAPIPolicyByUUID(policyId);
        } catch (APIManagementException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_ADVANCED_POLICY, policyId));
        }
        if (!RestApiAdminUtils.isPolicyAccessibleToUser(existingPolicy)) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                    RestApiConstants.RESOURCE_ADVANCED_POLICY, policyId));
        }

        if (apiProvider.hasAttachments(username, existingPolicy.getPolicyName(), PolicyConstants.POLICY_LEVEL_API,
                organization)) {
            String message = "Advanced Throttling Policy " + existingPolicy.getPolicyName() + ": " + policyId
                    + " already attached to API/Resource";
            throw new APIManagementException(message, ExceptionCodes
                    .from(ExceptionCodes.ALREADY_ASSIGNED_ADVANCED_POLICY_DELETE_ERROR,
                            existingPolicy.getPolicyName()));
        }
        apiProvider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_API, existingPolicy.getPolicyName());
    }

    /**
     * Get application policies
     *
     * @return Application policy list
     * @throws APIManagementException When getting application policies fail
     */
    public static ApplicationThrottlePolicyListDTO getApplicationThrottlePolicies() throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        Policy[] appPolicies = apiAdmin.getPolicies(tenantId, PolicyConstants.POLICY_LEVEL_APP);
        List<ApplicationPolicy> policies = new ArrayList<>();
        for (Policy policy : appPolicies) {
            policies.add((ApplicationPolicy) policy);
        }
        return ApplicationThrottlePolicyMappingUtil
                .fromApplicationPolicyArrayToListDTO(policies.toArray(new ApplicationPolicy[policies.size()]));
    }

    /**
     * Add new application throttle policy
     *
     * @param body Application policy DTO
     * @return Application policy DTO
     * @throws APIManagementException When adding application policy fails
     */
    public static ApplicationThrottlePolicyDTO addApplicationThrottlePolicy(ApplicationThrottlePolicyDTO body)
            throws APIManagementException {
        RestApiAdminUtils.validateThrottlePolicyNameProperty(body.getPolicyName());

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();
        ApplicationPolicy appPolicy =
                ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyDTOToModel(body);

        //Check if there's a policy exists before adding the new policy
        Policy policyIfExists = apiProvider.getApplicationPolicy(username, appPolicy.getPolicyName());
        if (policyIfExists != null) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.APPLICATION_POLICY_EXISTS,
                    appPolicy.getPolicyName()));
        }
        //Add the policy
        apiProvider.addPolicy(appPolicy);

        //retrieve the new policy and send back as the response
        ApplicationPolicy newAppPolicy = apiProvider.getApplicationPolicy(username, body.getPolicyName());
        return ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyToDTO(newAppPolicy);
    }

    /**
     * Get application policy by policy ID
     *
     * @param policyId Policy ID
     * @return Application throttle policy
     * @throws APIManagementException When an intenal error occurs
     */
    public static ApplicationThrottlePolicyDTO getApplicationThrottlePolicyById(String policyId)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //This will give PolicyNotFoundException if there's no policy exists with UUID
            ApplicationPolicy appPolicy = apiProvider.getApplicationPolicyByUUID(policyId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(appPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_APP_POLICY, policyId));
            }
            return ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyToDTO(appPolicy);

        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_APP_POLICY, policyId));
        }
    }

    /**
     * Update application throttle policy
     *
     * @param policyId Policy ID
     * @param body     Application throttle policy DTO
     * @return Application throttle policy DTO
     * @throws APIManagementException When an internal error occurs
     */
    public static ApplicationThrottlePolicyDTO updateApplicationThrottlePolicy(String policyId,
                                                                               ApplicationThrottlePolicyDTO body)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //will give PolicyNotFoundException if there's no policy exists with UUID
            ApplicationPolicy existingPolicy = apiProvider.getApplicationPolicyByUUID(policyId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(existingPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_APP_POLICY, policyId));
            }
            //overridden properties
            body.setPolicyId(policyId);
            body.setPolicyName(existingPolicy.getPolicyName());

            //update the policy
            ApplicationPolicy appPolicy =
                    ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyDTOToModel(body);
            apiProvider.updatePolicy(appPolicy);

            //retrieve the new policy and send back as the response
            ApplicationPolicy newAppPolicy = apiProvider.getApplicationPolicyByUUID(policyId);
            return ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyToDTO(newAppPolicy);
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_APP_POLICY, policyId));
        }
    }

    /**
     * Delete application policy
     *
     * @param policyId     Policy ID
     * @param organization Tenant organization
     * @throws APIManagementException When an internal error occurs
     */
    public static void removeApplicationThrottlePolicy(String policyId, String organization)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String username = RestApiCommonUtil.getLoggedInUsername();
            //This will give PolicyNotFoundException if there's no policy exists with UUID
            ApplicationPolicy existingPolicy = apiProvider.getApplicationPolicyByUUID(policyId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(existingPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_APP_POLICY, policyId));
            }
            if (apiProvider.hasAttachments(organization, existingPolicy.getPolicyName(),
                    PolicyConstants.POLICY_LEVEL_APP, organization)) {
                String message = "Policy " + policyId + " already attached to an application";
                throw new APIManagementException(message,
                        ExceptionCodes.from(ExceptionCodes.ALREADY_ASSIGNED_APP_POLICY_DELETE_ERROR, policyId));
            }
            apiProvider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_APP, existingPolicy.getPolicyName());
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_APP_POLICY, policyId));
        }
    }

    /**
     * Get all subscription policies
     *
     * @return Subscription policy list
     * @throws APIManagementException When an internal error occurs
     */
    public static SubscriptionThrottlePolicyListDTO getAllSubscriptionThrottlePolicies() throws APIManagementException {
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            String userName = RestApiCommonUtil.getLoggedInUsername();
            int tenantId = APIUtil.getTenantId(userName);
            Policy[] subscriptionPolicies = apiAdmin.getPolicies(tenantId, PolicyConstants.POLICY_LEVEL_SUB);
            List<SubscriptionPolicy> policies = new ArrayList<>();
            for (Policy policy : subscriptionPolicies) {
                policies.add((SubscriptionPolicy) policy);
            }
            return SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionPolicyArrayToListDTO(policies.toArray(new SubscriptionPolicy[policies.size()]));
        } catch (ParseException e) {
            String errorMessage = "Error while retrieving Subscription level policies";
            throw new APIManagementException(errorMessage, e, ExceptionCodes.SUBSCRIPTION_POLICY_GET_ALL_FAILED);
        }
    }

    /**
     * Add new subscription policy
     *
     * @param body Subscription throttle policy DTO
     * @return Subscription throttle policy DTO
     * @throws APIManagementException When an internal error occurs
     */
    public static SubscriptionThrottlePolicyDTO addSubscriptionThrottlePolicy(SubscriptionThrottlePolicyDTO body)
            throws APIManagementException {
        try {
            RestApiAdminUtils.validateThrottlePolicyNameProperty(body.getPolicyName());

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String username = RestApiCommonUtil.getLoggedInUsername();
            SubscriptionPolicy subscriptionPolicy =
                    SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyDTOToModel(body);

            //Check if there's a policy exists before adding the new policy
            Policy policyIfExists = apiProvider.getSubscriptionPolicy(username, subscriptionPolicy.getPolicyName());
            if (policyIfExists != null) {
                String errorMessage = "Subscription Policy with name "
                        + subscriptionPolicy.getPolicyName() + EXISTS_CONSTANT;
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.SUBSCRIPTION_POLICY_EXISTS, body.getPolicyName()));

            }
            // validate if permission info exists and halt the execution in case of an error
            validatePolicyPermissions(body);

            //Add the policy
            apiProvider.addPolicy(subscriptionPolicy);

            //update policy permissions
            updatePolicyPermissions(body);

            //retrieve the new policy and send back as the response
            SubscriptionPolicy newSubscriptionPolicy = apiProvider.getSubscriptionPolicy(username,
                    body.getPolicyName());
            SubscriptionThrottlePolicyDTO policyDTO =
                    SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyToDTO(newSubscriptionPolicy);

            //setting policy permissions
            setPolicyPermissionsToDTO(policyDTO);
            return policyDTO;
        } catch (ParseException e) {
            String errorMessage = "Error while adding a Subscription level policy: " + body.getPolicyName();
            throw new APIManagementException(errorMessage, ExceptionCodes.SUBSCRIPTION_POLICY_ADD_FAILED);
        }
    }

    /**
     * Search throttle policies
     *
     * @param query Search query
     * @return Throttle policy details
     * @throws APIManagementException When an internal error occurs
     */
    public static ThrottlePolicyDetailsListDTO throttlingPolicySearch(String query) throws APIManagementException {
        ThrottlePolicyDetailsListDTO resultListDTO = new ThrottlePolicyDetailsListDTO();
        String policyType;
        String policyName;
        Map<String, String> filters;

        if (query == null) {
            query = "type:" + ALL_TYPES;
        } else if (!query.toLowerCase().contains("type:")) {
            String errorMessage = "Invalid query format";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        log.debug("Extracting query info...");
        try {
            filters = Splitter.on(" ").withKeyValueSeparator(":").split(query);
        } catch (IllegalArgumentException ex) {
            throw new APIManagementException("Illegal format of query parameter" + query,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR));
        }
        policyType = filters.get("type");
        policyName = filters.get("name");

        List<ThrottlePolicyDetailsDTO> result = null;

        // In current implementation policy filtering by either type or name and type both.
        if (policyName != null && !policyType.equalsIgnoreCase("all")) {
            result = getThrottlingPolicyByTypeAndName(policyType, policyName);
        } else {
            result = getThrottlingPoliciesByType(policyType);
        }

        resultListDTO.setCount(result.size());
        resultListDTO.setList(result);
        return resultListDTO;
    }

    /**
     * Get subscription policy by ID
     *
     * @param policyId Policy ID
     * @return Subscription policy
     * @throws APIManagementException When an internal error occurs
     */
    public static SubscriptionThrottlePolicyDTO getSubscriptionThrottlePolicyById(String policyId)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //This will give PolicyNotFoundException if there's no policy exists with UUID
            SubscriptionPolicy subscriptionPolicy = apiProvider.getSubscriptionPolicyByUUID(policyId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(subscriptionPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY, policyId));
            }
            SubscriptionThrottlePolicyDTO policyDTO =
                    SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyToDTO(subscriptionPolicy);

            //setting policy permissions
            setPolicyPermissionsToDTO(policyDTO);
            return policyDTO;
        } catch (ParseException e) {
            String errorMessage = "Error while retrieving Subscription level policy: " + policyId;
            throw new APIManagementException(errorMessage, ExceptionCodes.SUBSCRIPTION_POLICY_GET_FAILED);
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY, policyId));
        }
    }

    /**
     * Update subscription policy
     *
     * @param policyId Policy ID
     * @param body     Subscription policy DTO
     * @return Subscription policy DTO
     * @throws APIManagementException When an internal error occurs
     */
    public static SubscriptionThrottlePolicyDTO updateSubscriptionThrottlePolicy(String policyId,
                                                                                 SubscriptionThrottlePolicyDTO body)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String username = RestApiCommonUtil.getLoggedInUsername();

            //will give PolicyNotFoundException if there's no policy exists with UUID
            SubscriptionPolicy existingPolicy = apiProvider.getSubscriptionPolicyByUUID(policyId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(existingPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY, policyId));
            }

            //overridden properties
            body.setPolicyId(policyId);
            body.setPolicyName(existingPolicy.getPolicyName());

            // validate if permission info exists and halt the execution in case of an error
            validatePolicyPermissions(body);

            //update the policy
            SubscriptionPolicy subscriptionPolicy =
                    SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyDTOToModel(body);
            apiProvider.updatePolicy(subscriptionPolicy);

            //update policy permissions
            updatePolicyPermissions(body);

            //retrieve the new policy and send back as the response
            SubscriptionPolicy newSubscriptionPolicy = apiProvider.getSubscriptionPolicy(username,
                    body.getPolicyName());
            SubscriptionThrottlePolicyDTO policyDTO =
                    SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyToDTO(newSubscriptionPolicy);
            //setting policy permissions
            setPolicyPermissionsToDTO(policyDTO);
            return policyDTO;
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY, policyId));
        } catch (ParseException e) {
            String errorMessage = "Error while updating Subscription level policy: " + policyId;
            throw new APIManagementException(errorMessage, ExceptionCodes.SUBSCRIPTION_POLICY_GET_FAILED);
        }
    }

    /**
     * Delete subscription policy
     *
     * @param policyId     Policy ID
     * @param organization Tenant organization
     * @throws APIManagementException When an internal error occurs
     */
    public static void removeSubscriptionThrottlePolicy(String policyId, String organization)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String username = RestApiCommonUtil.getLoggedInUsername();

            //This will give PolicyNotFoundException if there's no policy exists with UUID
            SubscriptionPolicy existingPolicy = apiProvider.getSubscriptionPolicyByUUID(policyId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(existingPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY, policyId));
            }
            if (apiProvider.hasAttachments(username, existingPolicy.getPolicyName(),
                    PolicyConstants.POLICY_LEVEL_SUB, organization)) {
                String message = "Policy " + policyId + " already has subscriptions";
                log.error(message);
                throw new APIManagementException(message, ExceptionCodes.ALREADY_ASSIGNED_SUB_POLICY_DELETE_ERROR);
            }
            apiProvider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_SUB, existingPolicy.getPolicyName());
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY, policyId));
        }
    }

    /**
     * Get all custom rules
     *
     * @return List of custom rules
     * @throws APIManagementException When an internal error occurs
     */
    public static CustomRuleListDTO getAllCustomRules() throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);

        //only super tenant is allowed to access global policies/custom rules
        checkTenantDomainForCustomRules();

        Policy[] globalPolicies = apiAdmin.getPolicies(tenantId, PolicyConstants.POLICY_LEVEL_GLOBAL);
        List<GlobalPolicy> policies = new ArrayList<>();
        for (Policy policy : globalPolicies) {
            policies.add((GlobalPolicy) policy);
        }
        return GlobalThrottlePolicyMappingUtil
                .fromGlobalPolicyArrayToListDTO(policies.toArray(new GlobalPolicy[policies.size()]));
    }

    /**
     * Create new custom rule
     *
     * @param body Custom rule DTO
     * @return Custom rule DTO
     * @throws APIManagementException When an internal error occurs
     */
    public static CustomRuleDTO addCustomRule(CustomRuleDTO body, String httpMethod) throws APIManagementException {
        RestApiAdminUtils
                .validateCustomRuleRequiredProperties(body, httpMethod);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //only super tenant is allowed to access global policies/custom rules
        checkTenantDomainForCustomRules();

        GlobalPolicy globalPolicy = GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyDTOToModel(body);
        //Check if there's a policy exists before adding the new policy
        try {
            Policy policyIfExists = apiProvider.getGlobalPolicy(globalPolicy.getPolicyName());
            if (policyIfExists != null) {
                String errorMessage = "Custom rule with name " + globalPolicy.getPolicyName() + EXISTS_CONSTANT;
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.CUSTOM_RULE_EXISTS, globalPolicy.getPolicyName()));
            }
        } catch (PolicyNotFoundException ignore) {
            //do nothing
        }
        //Add the policy
        apiProvider.addPolicy(globalPolicy);

        //retrieve the new policy and send back as the response
        GlobalPolicy newGlobalPolicy = apiProvider.getGlobalPolicy(body.getPolicyName());
        return GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyToDTO(newGlobalPolicy);
    }

    /**
     * Get custom rule by ID
     *
     * @param ruleId Custom rule ID
     * @return Custom rule
     * @throws APIManagementException When an internal error occurs
     */
    public static CustomRuleDTO getCustomRuleById(String ruleId) throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //only super tenant is allowed to access global policies/custom rules
            checkTenantDomainForCustomRules();

            //This will give PolicyNotFoundException if there's no policy exists with UUID
            GlobalPolicy globalPolicy = apiProvider.getGlobalPolicyByUUID(ruleId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(globalPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_CUSTOM_RULE, ruleId));
            }
            return GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyToDTO(globalPolicy);
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_CUSTOM_RULE, ruleId));
        }
    }

    /**
     * Update custom rule
     *
     * @param ruleId Custom rule ID
     * @param body   Custom rule DTO
     * @return Custom rule DTO
     * @throws APIManagementException When an internal error occurs
     */
    public static CustomRuleDTO updateCustomRule(String ruleId, CustomRuleDTO body) throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //only super tenant is allowed to access global policies/custom rules
            checkTenantDomainForCustomRules();

            //will give PolicyNotFoundException if there's no policy exists with UUID
            GlobalPolicy existingPolicy = apiProvider.getGlobalPolicyByUUID(ruleId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(existingPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_CUSTOM_RULE, ruleId));
            }

            //overridden properties
            body.setPolicyId(ruleId);
            body.setPolicyName(existingPolicy.getPolicyName());

            //update the policy
            GlobalPolicy globalPolicy = GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyDTOToModel(body);
            apiProvider.updatePolicy(globalPolicy);

            //retrieve the new policy and send back as the response
            GlobalPolicy newGlobalPolicy = apiProvider.getGlobalPolicyByUUID(ruleId);
            return GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyToDTO(newGlobalPolicy);
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_CUSTOM_RULE, ruleId));
        }
    }

    /**
     * Delete custom rule
     *
     * @param ruleId Custom rule ID
     * @throws APIManagementException When an internal error occurs
     */
    public static void removeCustomRule(String ruleId) throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //only super tenant is allowed to access global policies/custom rules
            checkTenantDomainForCustomRules();

            String username = RestApiCommonUtil.getLoggedInUsername();

            //This will give PolicyNotFoundException if there's no policy exists with UUID
            GlobalPolicy existingPolicy = apiProvider.getGlobalPolicyByUUID(ruleId);
            if (!RestApiAdminUtils.isPolicyAccessibleToUser(existingPolicy)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_CUSTOM_RULE, ruleId));
            }
            apiProvider.deletePolicy(username, PolicyConstants.POLICY_LEVEL_GLOBAL, existingPolicy.getPolicyName());
        } catch (PolicyNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_CUSTOM_RULE, ruleId));
        }

    }

    /**
     * Export throttle policy
     *
     * @param policyName Policy name
     * @param type       Policy type
     * @return Export throttle policy DTO
     * @throws APIManagementException When an internal error occurs
     */
    public static ExportThrottlePolicyDTO exportThrottlingPolicy(String policyName, String type)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        ExportThrottlePolicyDTO exportPolicy = new ExportThrottlePolicyDTO();
        exportPolicy.type(RestApiConstants.RESOURCE_THROTTLING_POLICY);
        exportPolicy.version(ImportExportConstants.APIM_VERSION);
        type = (type == null) ? StringUtils.EMPTY : type;

        if (StringUtils.EMPTY.equals(type) || PolicyConstants.POLICY_LEVEL_APP.equals(type)) {
            ApplicationPolicy appPolicy = apiProvider.getApplicationPolicy(userName, policyName);
            if (appPolicy != null) {
                String policyId = appPolicy.getUUID();
                if (!RestApiAdminUtils.isPolicyAccessibleToUser(appPolicy)) {
                    throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                            RestApiConstants.RESOURCE_APP_POLICY, policyId));
                }
                ApplicationThrottlePolicyDTO policyDTO
                        = ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyToDTO(
                        appPolicy);
                exportPolicy.data(policyDTO);
                exportPolicy.subtype(RestApiConstants.RESOURCE_APP_POLICY);
                return exportPolicy;
            } else if (!type.equals(StringUtils.EMPTY)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                        RestApiConstants.RESOURCE_APP_POLICY, policyName));
            }
        }

        if (StringUtils.EMPTY.equals(type) || PolicyConstants.POLICY_LEVEL_SUB.equals(type)) {
            try {
                SubscriptionPolicy subPolicy = apiProvider.getSubscriptionPolicy(userName, policyName);
                if (subPolicy != null) {
                    String policyId = subPolicy.getUUID();
                    if (!RestApiAdminUtils.isPolicyAccessibleToUser(subPolicy)) {
                        throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                                RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY, policyId));
                    }
                    SubscriptionThrottlePolicyDTO policyDTO
                            = SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyToDTO(subPolicy);
                    //setting policy permissions
                    setPolicyPermissionsToDTO(policyDTO);
                    exportPolicy.data(policyDTO);
                    exportPolicy.subtype(RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY);
                    return exportPolicy;
                } else if (!type.equals(StringUtils.EMPTY)) {
                    throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                            RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY, policyName));
                }
            } catch (ParseException e) {
                String errorMessage = "Error while retrieving Subscription level policy: " + policyName;
                throw new APIManagementException(errorMessage, ExceptionCodes.SUBSCRIPTION_POLICY_GET_FAILED);
            }
        }

        if (StringUtils.EMPTY.equals(type) || PolicyConstants.POLICY_LEVEL_API.equals(type)) {
            exportPolicy.subtype(RestApiConstants.RESOURCE_ADVANCED_POLICY);
            APIPolicy apiPolicy = apiProvider.getAPIPolicy(userName, policyName);
            if (apiPolicy != null) {
                String policyId = apiPolicy.getUUID();
                if (!RestApiAdminUtils.isPolicyAccessibleToUser(apiPolicy)) {
                    throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                            RestApiConstants.RESOURCE_ADVANCED_POLICY, policyId));
                }
                AdvancedThrottlePolicyDTO policyDTO = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToDTO(
                        apiPolicy);
                exportPolicy.data(policyDTO);
                return exportPolicy;
            } else if (!type.equals(StringUtils.EMPTY)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                        RestApiConstants.RESOURCE_ADVANCED_POLICY, policyName));
            }
        }

        if (StringUtils.EMPTY.equals(type) || PolicyConstants.POLICY_LEVEL_GLOBAL.equals(type)) {
            GlobalPolicy globalPolicy = apiProvider.getGlobalPolicy(policyName);
            if (globalPolicy != null) {
                //only super tenant is allowed to access global policies/custom rules
                checkTenantDomainForCustomRules();
                CustomRuleDTO policyDTO = GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyToDTO(
                        globalPolicy);
                exportPolicy.data(policyDTO);
                exportPolicy.subtype(RestApiConstants.RESOURCE_CUSTOM_RULE);
                return exportPolicy;
            } else if (!type.equals(StringUtils.EMPTY)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                        RestApiConstants.RESOURCE_CUSTOM_RULE, policyName));
            }
        }

        throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.THROTTLING_POLICY_NOT_FOUND,
                policyName, type));
    }

    public static Map<String, Object> importThrottlingPolicy(InputStream fileInputStream, String fileName,
                                                             boolean overwrite, String httpMethod)
            throws APIManagementException {
        ExportThrottlePolicyDTO exportThrottlePolicyDTO = null;
        String policyType = "";
        try {
            exportThrottlePolicyDTO = getImportedPolicy(fileInputStream, fileName);
        } catch (APIImportExportException | IOException | ParseException e) {
            String errorMessage = "Error retrieving Throttling policy";
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        }
        if (exportThrottlePolicyDTO != null) {
            policyType = exportThrottlePolicyDTO.getSubtype();
        } else {
            String errorMessage = "Error resolving ExportThrottlePolicyDTO";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        }

        return resolveUpdateThrottlingPolicy(policyType, overwrite, exportThrottlePolicyDTO, httpMethod);
    }

    /**
     * Get all deny policies
     *
     * @return Block condition DTO
     * @throws APIManagementException When an internal error occurs
     */
    public static BlockingConditionListDTO getAllDenyPolicies() throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            List<BlockConditionsDTO> blockConditions = apiProvider.getBlockConditions();
            return BlockingConditionMappingUtil.fromBlockConditionListToListDTO(blockConditions);
        } catch (ParseException e) {
            String errorMessage = "Error while retrieving Block Conditions";
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        }
    }

    /**
     * Create new deny policy
     *
     * @param body Block condition DTO
     * @return Block condition DTO
     * @throws APIManagementException When an internal error occurs
     */
    public static BlockingConditionDTO addDenyPolicy(BlockingConditionDTO body) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //Add the block condition. It will throw BlockConditionAlreadyExistsException if the condition already
        //  exists in the system
        String uuid = null;
        try {
            if (BlockingConditionDTO.ConditionTypeEnum.API.equals(body.getConditionType()) ||
                    BlockingConditionDTO.ConditionTypeEnum.APPLICATION.equals(body.getConditionType()) ||
                    BlockingConditionDTO.ConditionTypeEnum.USER.equals(body.getConditionType())) {
                uuid = apiProvider.addBlockCondition(body.getConditionType().toString(),
                        (String) body.getConditionValue(), body.isConditionStatus());
            } else if ((BlockingConditionDTO.ConditionTypeEnum.IP.equals(body.getConditionType())
                    || BlockingConditionDTO.ConditionTypeEnum.IPRANGE.equals(body.getConditionType()))
                    && body.getConditionValue() instanceof Map) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.putAll((Map) body.getConditionValue());

                if (BlockingConditionDTO.ConditionTypeEnum.IP.equals(body.getConditionType())) {
                    RestApiAdminUtils.validateIPAddress(jsonObject.get("fixedIp").toString());
                }
                if (BlockingConditionDTO.ConditionTypeEnum.IPRANGE.equals(body.getConditionType())) {
                    RestApiAdminUtils.validateIPAddress(jsonObject.get("startingIp").toString());
                    RestApiAdminUtils.validateIPAddress(jsonObject.get("endingIp").toString());
                }
                uuid = apiProvider.addBlockCondition(body.getConditionType().toString(),
                        jsonObject.toJSONString(), body.isConditionStatus());
            }
        } catch (BlockConditionAlreadyExistsException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.BLOCK_CONDITION_ALREADY_EXISTS,
                    body.getConditionType().toString(), body.getConditionValue().toString()));
        }

        try {
            //retrieve the new blocking condition and send back as the response
            BlockConditionsDTO newBlockingCondition = apiProvider.getBlockConditionByUUID(uuid);
            return BlockingConditionMappingUtil.fromBlockingConditionToDTO(newBlockingCondition);
        } catch (ParseException e) {
            String errorMessage = "Error while adding Blocking Condition. Condition type: "
                    + body.getConditionType() + ", " + "value: " + body.getConditionValue() + ". " + e.getMessage();
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        }
    }

    public static BlockingConditionDTO getDenyPolicyById(String conditionId) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        try {
            //This will give BlockConditionNotFoundException if there's no block condition exists with UUID
            BlockConditionsDTO blockCondition = apiProvider.getBlockConditionByUUID(conditionId);
            if (!RestApiAdminUtils.isBlockConditionAccessibleToUser(blockCondition)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId));
            }
            return BlockingConditionMappingUtil.fromBlockingConditionToDTO(blockCondition);
        } catch (BlockConditionAlreadyExistsException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId));
        } catch (ParseException e) {
            String errorMessage = "Error while retrieving Blocking Conditions";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        }
    }

    /**
     * Remove deny policy
     *
     * @param conditionId Deny policy ID
     * @throws APIManagementException When an internal error occurs
     */
    public static void removeDenyPolicy(String conditionId) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        try {
            //This will give BlockConditionNotFoundException if there's no block condition exists with UUID
            BlockConditionsDTO existingCondition = apiProvider.getBlockConditionByUUID(conditionId);
            if (!RestApiAdminUtils.isBlockConditionAccessibleToUser(existingCondition)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId));
            }
            apiProvider.deleteBlockConditionByUUID(conditionId);
        } catch (BlockConditionNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId));
        }
    }

    public static BlockingConditionDTO updateDenyPolicy(String conditionId, BlockingConditionStatusDTO body)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        try {
            //This will give BlockConditionNotFoundException if there's no block condition exists with UUID
            BlockConditionsDTO existingCondition = apiProvider.getBlockConditionByUUID(conditionId);
            if (!RestApiAdminUtils.isBlockConditionAccessibleToUser(existingCondition)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.AUTHORIZATION_ERROR,
                        RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId));
            }

            //update the status
            apiProvider.updateBlockConditionByUUID(conditionId, String.valueOf(body.isConditionStatus()));

            //retrieve the new blocking condition and send back as the response
            BlockConditionsDTO newBlockingCondition = apiProvider.getBlockConditionByUUID(conditionId);
            return BlockingConditionMappingUtil.fromBlockingConditionToDTO(newBlockingCondition);
        } catch (BlockConditionNotFoundException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_DESC,
                    RestApiConstants.RESOURCE_BLOCK_CONDITION, conditionId));
        } catch (ParseException e) {
            String errorMessage = "Error while updating Block Condition Status. Id : " + conditionId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        }
    }


    /**
     * Validates the permission element of the subscription throttle policy
     *
     * @param body subscription throttle policy
     * @throws APIManagementException when there are validation errors
     */
    private static void validatePolicyPermissions(SubscriptionThrottlePolicyDTO body) throws APIManagementException {
        SubscriptionThrottlePolicyPermissionDTO policyPermissions = body.getPermissions();
        if (policyPermissions != null && policyPermissions.getRoles().isEmpty()) {
            throw new APIManagementException(ExceptionCodes.ROLES_CANNOT_BE_EMPTY);
        }
    }

    /**
     * Update APIM with the subscription throttle policy permission
     *
     * @param body subscription throttle policy
     * @throws APIManagementException when there are validation errors or error while updating the permissions
     */
    private static void updatePolicyPermissions(SubscriptionThrottlePolicyDTO body) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        SubscriptionThrottlePolicyPermissionDTO policyPermissions = body.getPermissions();
        if (policyPermissions != null) {
            if (!policyPermissions.getRoles().isEmpty()) {
                String roles = StringUtils.join(policyPermissions.getRoles(), ",");
                String permissionType;
                if (policyPermissions.getPermissionType() ==
                        SubscriptionThrottlePolicyPermissionDTO.PermissionTypeEnum.ALLOW) {
                    permissionType = org.wso2.carbon.apimgt.impl.APIConstants.TIER_PERMISSION_ALLOW;
                } else {
                    permissionType = APIConstants.TIER_PERMISSION_DENY;
                }
                apiProvider.updateThrottleTierPermissions(body.getPolicyName(), permissionType, roles);
            } else {
                throw new APIManagementException(ExceptionCodes.ROLES_CANNOT_BE_EMPTY);
            }
        } else {
            apiProvider.deleteTierPermissions(body.getPolicyName());
        }
    }

    /**
     * Set subscription throttle policy permission info into the DTO
     *
     * @param policyDTO subscription throttle policy DTO
     * @throws APIManagementException error while setting/retrieve the permissions to the DTO
     */
    private static void setPolicyPermissionsToDTO(SubscriptionThrottlePolicyDTO policyDTO) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        TierPermissionDTO addedPolicyPermission =
                (TierPermissionDTO) apiProvider.getThrottleTierPermission(policyDTO.getPolicyName());
        if (addedPolicyPermission != null) {
            SubscriptionThrottlePolicyPermissionDTO addedPolicyPermissionDTO =
                    SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyPermissionToDTO(addedPolicyPermission);
            policyDTO.setPermissions(addedPolicyPermissionDTO);
        }
    }

    /**
     * Returns throttle policy details as a list
     *
     * @param policyLevel type of the throttling policy to be returned as list
     * @param policyName  name of the throttling policy to be returned as a list
     * @return throttling policy list filtered by policy type and policy name
     * @throws APIManagementException When an internal error occurs
     */
    private static List<ThrottlePolicyDetailsDTO> getThrottlingPolicyByTypeAndName(String policyLevel, String policyName)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        Policy policy;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<ThrottlePolicyDetailsDTO> policies = new ArrayList<>();

        policy = apiAdmin.getPolicyByNameAndType(tenantId, policyLevel, policyName);

        if (policy == null) {
            throw new APIManagementException(
                    "Couldn't retrieve an existing throttling policy with Name: " + policyName + " and type: "
                            + policyLevel,
                    ExceptionCodes.from(ExceptionCodes.THROTTLING_POLICY_NOT_FOUND, policyName, policyLevel));
        }

        ThrottlePolicyDetailsDTO policyDetails = mapper.convertValue(policy, ThrottlePolicyDetailsDTO.class);
        policyDetails.setType(policyLevel);
        policies.add(policyDetails);

        return policies;
    }

    /**
     * Returns throttle policies details as a list
     *
     * @param policyLevel type of the throttling policy list to be returned
     * @return throttling policy list filtered by policy type
     * @throws APIManagementException When an internal error occurs
     */
    private static List<ThrottlePolicyDetailsDTO> getThrottlingPoliciesByType(String policyLevel)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        Policy[] temporaryPolicies;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<ThrottlePolicyDetailsDTO> policies = new ArrayList<>();
        if (ALL_TYPES.equals(policyLevel) || PolicyConstants.POLICY_LEVEL_APP.equals(policyLevel)) {
            temporaryPolicies = apiAdmin.getPolicies(tenantId, PolicyConstants.POLICY_LEVEL_APP);
            populatePolicyList(mapper, temporaryPolicies, policies, PolicyConstants.POLICY_LEVEL_APP);
        }
        if (ALL_TYPES.equals(policyLevel) || PolicyConstants.POLICY_LEVEL_SUB.equals(policyLevel)) {
            temporaryPolicies = apiAdmin.getPolicies(tenantId, PolicyConstants.POLICY_LEVEL_SUB);
            populatePolicyList(mapper, temporaryPolicies, policies, PolicyConstants.POLICY_LEVEL_SUB);
        }
        if (ALL_TYPES.equals(policyLevel) || PolicyConstants.POLICY_LEVEL_API.equals(policyLevel)) {
            temporaryPolicies = apiAdmin.getPolicies(tenantId, PolicyConstants.POLICY_LEVEL_API);
            populatePolicyList(mapper, temporaryPolicies, policies, PolicyConstants.POLICY_LEVEL_API);
        }
        if (ALL_TYPES.equals(policyLevel) || PolicyConstants.POLICY_LEVEL_GLOBAL.equals(policyLevel)) {
            temporaryPolicies = apiAdmin.getPolicies(tenantId, PolicyConstants.POLICY_LEVEL_GLOBAL);
            populatePolicyList(mapper, temporaryPolicies, policies, PolicyConstants.POLICY_LEVEL_GLOBAL);
        }
        return policies;
    }

    /**
     * Helper method to populate the policies
     *
     * @param mapper            Object mapper
     * @param temporaryPolicies Temporary policy list
     * @param policies          List of policies
     * @param policyLevel       Policy Level
     */
    private static void populatePolicyList(ObjectMapper mapper, Policy[] temporaryPolicies,
                                           List<ThrottlePolicyDetailsDTO> policies, String policyLevel) {
        for (Policy policy : temporaryPolicies) {
            ThrottlePolicyDetailsDTO policyDetails = mapper.convertValue(policy, ThrottlePolicyDetailsDTO.class);
            policyDetails.setType(policyLevel);
            policies.add(policyDetails);
        }
    }

    /**
     * Checks if the logged-in user belongs to super tenant and throws 403 error if not
     *
     * @throws APIManagementException When an internal error occurs
     */
    private static void checkTenantDomainForCustomRules() throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (!tenantDomain.equals(RestApiConstants.SUPER_TENANT_DOMAIN_NAME)) {
            throw new APIManagementException("Tenant " + tenantDomain + " is not allowed to access custom rules. " +
                    "Only super tenant is allowed", ExceptionCodes.FORBIDDEN_ERROR);
        }
    }

    /**
     * Returns the ExportThrottlePolicyDTO by reading the file from input stream
     *
     * @param uploadedInputStream Input stream from the REST request
     * @param uploadFileName      Details of the file received via InputStream
     * @return ExportThrottlePolicyDTO of the file to be imported
     */
    private static ExportThrottlePolicyDTO getImportedPolicy(InputStream uploadedInputStream, String uploadFileName)
            throws ParseException, APIImportExportException, IOException {
        File importFolder = CommonUtil.createTempDirectory(null);
        String fileType = (uploadFileName.contains(ImportExportConstants.YAML_EXTENSION)) ?
                ImportExportConstants.EXPORT_POLICY_TYPE_YAML :
                ImportExportConstants.EXPORT_POLICY_TYPE_JSON;
        String absolutePath = importFolder.getAbsolutePath() + File.separator + uploadFileName;
        File targetFile = new File(absolutePath);
        FileUtils.copyInputStreamToFile(uploadedInputStream, targetFile);
        return preprocessImportedArtifact(absolutePath, fileType);
    }

    /**
     * Preprocesses either yaml or json file into the ExportThrottlePolicyDTO
     *
     * @param absolutePath temporary location of the throttle policy file
     * @param fileType     Type of the file to be imported (.yaml/.json)
     * @return ExportThrottlePolicyDTO from the file
     */
    private static ExportThrottlePolicyDTO preprocessImportedArtifact(String absolutePath, String fileType)
            throws IOException, ParseException {
        ExportThrottlePolicyDTO importPolicy;
        FileReader fileReader = new FileReader(absolutePath);
        if (ImportExportConstants.EXPORT_POLICY_TYPE_YAML.equals(fileType)) {
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            importPolicy = yamlMapper.readValue(fileReader, ExportThrottlePolicyDTO.class);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(fileReader);
            JSONObject jsonObject = (JSONObject) obj;
            importPolicy = mapper.convertValue(jsonObject, ExportThrottlePolicyDTO.class);
        }
        return importPolicy;
    }

    /**
     * Checks if the policy exists to either update the policy or indicate the conflict or import a new policy
     *
     * @param policyType              Throttling policy type
     * @param overwrite               User can either update an existing throttling policy with the same name or let the conflict happen
     * @param exportThrottlePolicyDTO the policy to be imported
     * @param httpMethod              HTTP Method
     * @return Response with  message indicating the status of the importation and the imported/updated policy name
     */
    private static Map<String, Object> resolveUpdateThrottlingPolicy(String policyType, boolean overwrite,
                                                                     ExportThrottlePolicyDTO exportThrottlePolicyDTO,
                                                                     String httpMethod)
            throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();
        Map<String, Object> responseObject;

        if (RestApiConstants.RESOURCE_SUBSCRIPTION_POLICY.equals(policyType)) {
            responseObject = resolveUpdateSubscriptionPolicy(mapper, apiProvider, overwrite, username,
                    exportThrottlePolicyDTO);
        } else if (RestApiConstants.RESOURCE_APP_POLICY.equals(policyType)) {
            responseObject = resolveUpdateApplicationPolicy(mapper, apiProvider, overwrite, username,
                    exportThrottlePolicyDTO);
        } else if (RestApiConstants.RESOURCE_CUSTOM_RULE.equals(policyType)) {
            responseObject = resolveUpdateCustomRule(mapper, apiProvider, overwrite, exportThrottlePolicyDTO,
                    httpMethod);
        } else if (RestApiConstants.RESOURCE_ADVANCED_POLICY.equals(policyType)) {
            responseObject = resolveUpdateAdvancedPolicy(mapper, apiProvider, overwrite, username,
                    exportThrottlePolicyDTO);
        } else {
            String errorMessage = "Error with Throttling Policy Type : " + policyType;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, errorMessage));
        }
        return responseObject;
    }

    /**
     * Checks if the policy exists to either update the policy or indicate the conflict or import a new policy
     *
     * @param mapper                  Object mapper
     * @param apiProvider             API Provider
     * @param overwrite               Override the existing policy
     * @param username                Username
     * @param exportThrottlePolicyDTO Throttle policy DTO
     * @return Map of policy DTO and message
     * @throws APIManagementException When an internal error occurs
     */
    private static Map<String, Object> resolveUpdateSubscriptionPolicy(ObjectMapper mapper, APIProvider apiProvider,
                                                                       boolean overwrite, String username,
                                                                       ExportThrottlePolicyDTO exportThrottlePolicyDTO)
            throws APIManagementException {
        Map<String, Object> responseObject = new HashMap<>();
        SubscriptionThrottlePolicyDTO subscriptionPolicy = mapper.convertValue(exportThrottlePolicyDTO.getData(),
                SubscriptionThrottlePolicyDTO.class);
        Policy policyIfExists = apiProvider.getSubscriptionPolicy(username, subscriptionPolicy.getPolicyName());
        if (policyIfExists != null) {
            if (overwrite) {
                String uuid = policyIfExists.getUUID();
                SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO
                        = updateSubscriptionThrottlePolicy(uuid, subscriptionPolicy);
                String message = "Successfully updated Subscription Throttling Policy : "
                        + subscriptionPolicy.getPolicyName();
                responseObject.put(DTO, subscriptionThrottlePolicyDTO);
                responseObject.put(MESSAGE, message);
                return responseObject;
            } else {
                String errorMessage = "Subscription Policy with name " + subscriptionPolicy.getPolicyName()
                        + EXISTS_CONSTANT;
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.SUBSCRIPTION_POLICY_EXISTS,
                                subscriptionPolicy.getPolicyName()));
            }
        } else {
            SubscriptionThrottlePolicyDTO subscriptionThrottlePolicyDTO
                    = addSubscriptionThrottlePolicy(subscriptionPolicy);
            String message =
                    "Successfully imported Subscription Throttling Policy : " + subscriptionPolicy.getPolicyName();
            responseObject.put(DTO, subscriptionThrottlePolicyDTO);
            responseObject.put(MESSAGE, message);
            return responseObject;
        }
    }

    /**
     * Checks if the policy exists to either update the policy or indicate the conflict or import a new policy
     *
     * @param mapper                  Object mapper
     * @param apiProvider             API Provider
     * @param overwrite               Override the existing policy
     * @param username                Username
     * @param exportThrottlePolicyDTO Throttle policy DTO
     * @return Map of policy DTO and message
     * @throws APIManagementException When an internal error occurs
     */
    private static Map<String, Object> resolveUpdateApplicationPolicy(ObjectMapper mapper, APIProvider apiProvider,
                                                                      boolean overwrite, String username,
                                                                      ExportThrottlePolicyDTO exportThrottlePolicyDTO)
            throws APIManagementException {
        Map<String, Object> responseObject = new HashMap<>();
        ApplicationThrottlePolicyDTO applicationPolicy = mapper.convertValue(exportThrottlePolicyDTO.getData(),
                ApplicationThrottlePolicyDTO.class);
        Policy policyIfExists = apiProvider.getApplicationPolicy(username, applicationPolicy.getPolicyName());
        if (policyIfExists != null) {
            if (overwrite) {
                String uuid = policyIfExists.getUUID();
                ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO
                        = updateApplicationThrottlePolicy(uuid, applicationPolicy);
                String message = "Successfully updated Application Throttling Policy : "
                        + applicationPolicy.getPolicyName();
                responseObject.put(DTO, applicationThrottlePolicyDTO);
                responseObject.put(MESSAGE, message);
            } else {
                String errorMessage = "Application Policy with name " + applicationPolicy.getPolicyName()
                        + EXISTS_CONSTANT;
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.APPLICATION_POLICY_EXISTS,
                                applicationPolicy.getPolicyName()));
            }
        } else {
            ApplicationThrottlePolicyDTO applicationThrottlePolicyDTO = addApplicationThrottlePolicy(applicationPolicy);
            String message =
                    "Successfully imported Application Throttling Policy : " + applicationPolicy.getPolicyName();
            responseObject.put(DTO, applicationThrottlePolicyDTO);
            responseObject.put(MESSAGE, message);
        }
        return responseObject;
    }

    /**
     * Checks if the policy exists to either update the policy or indicate the conflict or import a new policy
     *
     * @param mapper                  Object mapper
     * @param apiProvider             API Provider
     * @param overwrite               Override the existing policy
     * @param exportThrottlePolicyDTO Throttle policy DTO
     * @param httpMethod              HTTP Method
     * @return Map of policy DTO and message
     * @throws APIManagementException When an internal error occurs
     */
    private static Map<String, Object> resolveUpdateCustomRule(ObjectMapper mapper, APIProvider apiProvider,
                                                               boolean overwrite,
                                                               ExportThrottlePolicyDTO exportThrottlePolicyDTO,
                                                               String httpMethod)
            throws APIManagementException {
        Map<String, Object> responseObject = new HashMap<>();
        CustomRuleDTO customPolicy = mapper.convertValue(exportThrottlePolicyDTO.getData(), CustomRuleDTO.class);
        Policy policyIfExists = apiProvider.getGlobalPolicy(customPolicy.getPolicyName());
        if (policyIfExists != null) {
            if (overwrite) {
                String uuid = policyIfExists.getUUID();
                CustomRuleDTO customRuleDTO = updateCustomRule(uuid, customPolicy);
                String message = "Successfully updated Custom Throttling Policy : " + customPolicy.getPolicyName();
                responseObject.put(DTO, customRuleDTO);
                responseObject.put(MESSAGE, message);
            } else {
                String errorMessage = "Custom Policy with name " + customPolicy.getPolicyName() + EXISTS_CONSTANT;
                throw new APIManagementException(errorMessage, ExceptionCodes.from(ExceptionCodes.CUSTOM_RULE_EXISTS,
                        customPolicy.getPolicyName()));
            }
        } else {
            CustomRuleDTO customRuleDTO = addCustomRule(customPolicy, httpMethod);
            String message = "Successfully imported Custom Throttling Policy : " + customPolicy.getPolicyName();
            responseObject.put(DTO, customRuleDTO);
            responseObject.put(MESSAGE, message);
        }
        return responseObject;
    }

    /**
     * Checks if the policy exists to either update the policy or indicate the conflict or import a new policy
     *
     * @param mapper                  Object mapper
     * @param apiProvider             API Provider
     * @param overwrite               Override the existing policy
     * @param username                Username
     * @param exportThrottlePolicyDTO Throttle policy DTO
     * @return Map of policy DTO and message
     * @throws APIManagementException When an internal error occurs
     */
    private static Map<String, Object> resolveUpdateAdvancedPolicy(ObjectMapper mapper, APIProvider apiProvider,
                                                                   boolean overwrite, String username,
                                                                   ExportThrottlePolicyDTO exportThrottlePolicyDTO)
            throws APIManagementException {
        Map<String, Object> responseObject = new HashMap<>();
        AdvancedThrottlePolicyDTO advancedPolicy = mapper.convertValue(exportThrottlePolicyDTO.getData(),
                AdvancedThrottlePolicyDTO.class);
        Policy policyIfExists = apiProvider.getAPIPolicy(username, advancedPolicy.getPolicyName());
        if (policyIfExists != null) {
            if (overwrite) {
                String uuid = policyIfExists.getUUID();
                AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO = updateAdvancedPolicy(uuid, advancedPolicy);
                String message =
                        "Successfully updated Advanced Throttling Policy : " + advancedPolicy.getPolicyName();
                responseObject.put(DTO, advancedThrottlePolicyDTO);
                responseObject.put(MESSAGE, message);
            } else {
                String errorMessage = "Advanced Policy with name " + advancedPolicy.getPolicyName() + EXISTS_CONSTANT;
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.ADVANCED_POLICY_EXISTS, advancedPolicy.getPolicyName()));
            }
        } else {
            AdvancedThrottlePolicyDTO advancedThrottlePolicyDTO = addAdvancedPolicy(advancedPolicy);
            String message = "Successfully imported Advanced Throttling Policy : " + advancedPolicy.getPolicyName();
            responseObject.put(DTO, advancedThrottlePolicyDTO);
            responseObject.put(MESSAGE, message);
        }
        return responseObject;
    }
}
