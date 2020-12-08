/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;

/**
 * This class contains the utility methods related to Users and Roles
 */
public class APIRealmUtils {
    /**
     * Returns the claims of a User
     *
     * @param userName The name of the user
     * @return The looked up claims of the user
     * @throws APIManagementException if failed to get user
     */
    public static Map<String, String> getUserClaims(String userName) throws APIManagementException {
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        int tenantId = APIUtil.getTenantId(tenantDomain);
        Map<String, String> claimMap = APIUtil.getClaims(userName, tenantId, ClaimsRetriever.DEFAULT_DIALECT_URI);
        return claimMap;
    }
}
