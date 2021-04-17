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

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;

public class ApplicationImportExportManager {
    private static final Log log = LogFactory.getLog(ApplicationImportExportManager.class);
    private APIConsumer apiConsumer;

    ApplicationImportExportManager(APIConsumer apiConsumer) {
        this.apiConsumer = apiConsumer;
    }

    /**
     * Check whether a provided userId corresponds to a valid consumer of the store and subscribe if valid
     *
     * @param userId  username of the Owner
     * @param groupId the groupId to which the target subscriber belongs to
     * @throws APIManagementException if an error occurs while checking the validity of user
     */
    public void validateOwner(String userId, String groupId) throws APIManagementException {
        Subscriber subscriber = apiConsumer.getSubscriber(userId);
        try {
            if (subscriber == null && !APIUtil.isPermissionCheckDisabled()) {
                APIUtil.checkPermission(userId, APIConstants.Permissions.API_SUBSCRIBE);
                apiConsumer.addSubscriber(userId, groupId);
            }
        } catch (APIManagementException e) {
            String errorMsg = "Provided Application Owner is Invalid";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * This method validates the existence of the Application level throttling tier of Application
     *
     * @param application application
     * @throws APIManagementException
     */
    public void validateApplicationThrottlingPolicy(Application application) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Validating tier defined in the Application");
        }
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        ApplicationPolicy[] appPolicies = (ApplicationPolicy[]) apiProvider.getPolicies(userName,
                PolicyConstants.POLICY_LEVEL_APP);
        if (appPolicies != null || appPolicies.length > 0) {
            String applicationLevelPolicy = application.getTier();
            // To store whether the policy is available in the instance
            Boolean policyFound = false;
            for (ApplicationPolicy policy : appPolicies) {
                if (StringUtils.equals(policy.getPolicyName(), applicationLevelPolicy)) {
                    policyFound = true;
                    break;
                }
            }
            if (!policyFound) {
                String message = "Invalid Application level throttling tier " + applicationLevelPolicy +
                        " found in application definition";
                throw new APIManagementException(message);
            }
        }
    }

    /**
     * Check whether a provided userId corresponds to a valid Application Owner
     *
     * @param userId username of the Owner
     * @return true, if subscriber is available
     * @throws APIManagementException if an error occurs while checking the availability of user
     */
    public boolean isOwnerAvailable(String userId) throws APIManagementException {
        if (userId != null) {
            Subscriber subscriber = apiConsumer.getSubscriber(userId);
            return subscriber != null;
        }
        return false;
    }

    /**
     * Adds a key to a given Application
     *
     * @param username    User for import application
     * @param application Application used to add key
     * @param apiKey      API key for adding to application
     * @throws APIManagementException
     */
    public void addApplicationKey(String username, Application application, APIKey apiKey) throws APIManagementException {
        String[] accessAllowDomainsArray = {"ALL"};
        JSONObject jsonParamObj = new JSONObject();
        jsonParamObj.put(ApplicationConstants.OAUTH_CLIENT_USERNAME, username);
        String grantTypes = apiKey.getGrantTypes();
        if (!StringUtils.isEmpty(grantTypes)) {
            jsonParamObj.put(APIConstants.JSON_GRANT_TYPES, grantTypes);
        }
        /* Read clientId & clientSecret from ApplicationKeyGenerateRequestDTO object.
           User can provide clientId only or both clientId and clientSecret
           User cannot provide clientSecret only
         */
        if (!StringUtils.isEmpty(apiKey.getConsumerKey())) {
            jsonParamObj.put(APIConstants.JSON_CLIENT_ID, apiKey.getConsumerKey());
            if (!StringUtils.isEmpty(apiKey.getConsumerSecret())) {
                jsonParamObj.put(APIConstants.JSON_CLIENT_SECRET, apiKey.getConsumerSecret());
            }
        }
        String jsonParams = jsonParamObj.toString();
        String tokenScopes = apiKey.getTokenScope();
        apiConsumer.requestApprovalForApplicationRegistration(
                username, application.getName(), apiKey.getType(), apiKey.getCallbackUrl(),
                accessAllowDomainsArray, Long.toString(apiKey.getValidityPeriod()), tokenScopes, application.getGroupId(),
                jsonParams,apiKey.getKeyManager(), null);
    }
}
