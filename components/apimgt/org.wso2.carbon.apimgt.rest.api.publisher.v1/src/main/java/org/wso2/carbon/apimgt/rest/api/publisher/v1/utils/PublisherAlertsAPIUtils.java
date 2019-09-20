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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Base64;

public class PublisherAlertsAPIUtils {

    private static final  Log log = LogFactory.getLog(PublisherAlertsAPIUtils.class);

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
                    "The configuration id validation failed. Should be {apiName}#{apiVersion}#{tenantDomain}",
                    log);
        }

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            if (!apiProvider.isApiNameExist(parameters[0])) {
                RestApiUtil.handleBadRequest("Invalid API Name", log);
            }
            if (!MultitenantUtils.getTenantDomain(userName).equals(parameters[2])) {
                RestApiUtil.handleBadRequest("Invalid Tenant Domain", log);
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
