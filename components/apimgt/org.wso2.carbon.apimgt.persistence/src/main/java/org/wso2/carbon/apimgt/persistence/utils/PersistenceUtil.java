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
 */

package org.wso2.carbon.apimgt.persistence.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dto.DeploymentEnvironments;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.APIConstants;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PersistenceUtil {
    private static final Log log = LogFactory.getLog(PersistenceUtil.class);

    public static void handleException(String msg, Exception e) throws APIManagementException {
        throw new APIManagementException(msg, e);
    }

    public static void handleException(String msg) throws APIManagementException {
        throw new APIManagementException(msg);
    }

    /**
     * When an input is having '-AT-',replace it with @ [This is required to persist API data between registry and database]
     *
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomainBack(String input) {

        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT,
                                            APIConstants.EMAIL_DOMAIN_SEPARATOR);
        }
        return input;
    }

    public static boolean isAllowDisplayAPIsWithMultipleStatus() {

//        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
//                                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
//        String displayAllAPIs = config.getFirstProperty(APIConstants.API_STORE_DISPLAY_ALL_APIS);
//        if (displayAllAPIs == null) {
//            log.warn("The configurations related to show deprecated APIs in APIStore " +
//                                            "are missing in api-manager.xml.");
//            return false;
//        }
//        return Boolean.parseBoolean(displayAllAPIs);
        return false;
    }
    /**
     * This method used to set selected deployment environment values to governance artifact of API .
     *
     * @param deployments DeploymentEnvironments attributes value
     */
    public static Set<DeploymentEnvironments> extractDeploymentsForAPI(String deployments) {

        HashSet<DeploymentEnvironments> deploymentEnvironmentsSet = new HashSet<>();
        if (deployments != null && !"null".equals(deployments)) {
            Type deploymentEnvironmentsSetType = new TypeToken<HashSet<DeploymentEnvironments>>() {
            }.getType();
            deploymentEnvironmentsSet = new Gson().fromJson(deployments, deploymentEnvironmentsSetType);
            return deploymentEnvironmentsSet;
        }
        return deploymentEnvironmentsSet;
    }

    public static boolean isAdminUser(UserContext userContext) {
        boolean isAdmin = false;
        Map<String, Object> properties = userContext.getProperties();
        if (properties != null && properties.containsKey(APIConstants.USER_CTX_PROPERTY_ISADMIN)) {
            isAdmin = (Boolean) properties.get(APIConstants.USER_CTX_PROPERTY_ISADMIN);
        }
        return isAdmin;
    }

    public static String getSkipRoles(UserContext userContext) {
        String skipRoles = "";
        Map<String, Object> properties = userContext.getProperties();
        if (properties != null && properties.containsKey(APIConstants.USER_CTX_PROPERTY_SKIP_ROLES)) {
            skipRoles = (String) properties.get(APIConstants.USER_CTX_PROPERTY_SKIP_ROLES);
        }
        return skipRoles;
    }
}
