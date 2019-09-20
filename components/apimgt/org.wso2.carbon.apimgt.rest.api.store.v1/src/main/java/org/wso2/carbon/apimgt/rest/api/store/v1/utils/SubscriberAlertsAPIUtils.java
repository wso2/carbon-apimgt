/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Base64;

public class SubscriberAlertsAPIUtils {

    private static final  Log log = LogFactory.getLog(SubscriberAlertsAPIUtils.class);

    /**
     * Utility method to get the application name by applicationId
     *
     * @param applicationId : The applicationId
     * @return ApplicationName of the application if exists, otherwise null.
     * */
    public static String getApplicationNameById(int applicationId) throws APIManagementException {
        String userName = RestApiUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiUtil.getConsumer(userName);
        Application application = apiConsumer.getApplicationById(applicationId, userName, null);
        return application != null ? application.getName() : null;
    }

    /**
     * Get application id by application name.
     *
     * @param applicationName The application name.
     * */
    public static int getApplicationIdByName(String applicationName) throws APIManagementException {
        String userName = RestApiUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiUtil.getConsumer(userName);
        Application application = apiConsumer.getApplicationsByName(userName, applicationName, null);
        return application != null ? application.getId() : null;
    }

    /**
     * Validate the provided configuration id.
     *
     * @param configId : The configuration id
     * @return true if the validation is successful. Error response otherwise.
     * */
    public static boolean validateConfigParameters(String configId) {
        String userName = RestApiUtil.getLoggedInUsername();
        String decodedConfigurationId = new String(Base64.getDecoder().decode(configId.getBytes()));
        String[] parameters = decodedConfigurationId.split("#");
        if (parameters.length != 3) {
            RestApiUtil.handleBadRequest(
                    "The configuration id validation failed. Should be {apiName}#{apiVersion}#{applicationName}",
                    log);
        }

        try {
            APIConsumer apiConsumer = RestApiUtil.getConsumer(userName);
            if (!apiConsumer.isApiNameExist(parameters[0])) {
                RestApiUtil.handleBadRequest("Invalid API Name", log);
            }
            Application application = apiConsumer.getApplicationsByName(userName, parameters[2], null);
            if (application == null) {
                RestApiUtil.handleBadRequest("Invalid Application Name", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while validating payload", e, log);
        }
        return true;
    }

    /**
     * Get the user name with the tenant domain.
     *
     * @param userName : The required user name.
     * @return User name with tenant domain.
     * */
    public static String getTenantAwareUserName(String userName) {
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return userName + "@" + tenantDomain;
        }
        return userName;
    }
}
