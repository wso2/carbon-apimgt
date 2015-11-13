/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 *  This class contains REST API Store related utility operations
 */
public class RestAPIStoreUtils {

    private static final Log log = LogFactory.getLog(RestAPIStoreUtils.class);

    /** 
     * Returns the current logged in consumer's group id
     * @return group id of the current logged in user.
     */
    @SuppressWarnings("unchecked")
    public static String getLoggedInUserGroupIds() {
        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        JSONObject loginInfoJsonObj = new JSONObject();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            loginInfoJsonObj.put("user", username);
            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                loginInfoJsonObj.put("isSuperTenant", true);
            } else {
                loginInfoJsonObj.put("isSuperTenant", false);
            }
            String loginInfoString = loginInfoJsonObj.toJSONString();
            return apiConsumer.getGroupIds(loginInfoString);
        } catch (APIManagementException e) {
            log.error("Unable to get groupIds of user " + username);
            throw new InternalServerErrorException(e);
        }
    }

    /** 
     * check whether current logged in consumer has access to the specified application
     * 
     * @param application Application object
     * @return true if current logged in consumer has access to the specified application
     */
    public static boolean isUserAccessAllowedForApplication(Application application) {
        String username = RestApiUtil.getLoggedInUsername();

        //if groupId is null or empty, it is not a shared app 
        if (StringUtils.isEmpty(application.getGroupId())) {
            //if the application is not shared, its subscriber and the current logged in user must be same
            if (application.getSubscriber() != null && application.getSubscriber().getName().equals(username)) {
                return true;
            }
        } else {
            String userGroupIds = RestAPIStoreUtils.getLoggedInUserGroupIds();
            //if the application is a shared one, application's group id and the user's group id should be same
            if (application.getGroupId().equals(userGroupIds)) {
                return true;
            }
        }

        //user don't have access
        return false;
    }

    /**
     * check whether the specified API exists and the current logged in user has access to it
     *
     * @param apiId API identifier
     * @throws APIManagementException
     */
    public static void checkUserAccessAllowedToAPI(String apiId) throws APIManagementException {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        //this is just to check whether the user has access to the api or the api exists. When it tries to retrieve 
        // the resource from the registry, it will fail with AuthorizationFailedException if user does not have enough
        // privileges.
        APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
    }
}
