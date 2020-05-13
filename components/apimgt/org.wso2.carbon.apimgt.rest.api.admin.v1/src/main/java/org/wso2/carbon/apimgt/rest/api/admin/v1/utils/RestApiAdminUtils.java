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
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class RestApiAdminUtils {

    /**
     * Checks whether given policy is allowed to access to user
     *
     * @param user   username with tenant domain
     * @param policy policy to check
     * @return true if user is allowed to access the policy
     */
    public static boolean isPolicyAccessibleToUser(String user, Policy policy) {
        //This block checks whether policy's tenant domain and user's tenant domain are same
        String userTenantDomain = MultitenantUtils.getTenantDomain(user);
        if (!StringUtils.isBlank(policy.getTenantDomain())) {
            return policy.getTenantDomain().equals(userTenantDomain);
        } else {
            String tenantDomainFromId = APIUtil.getTenantDomainFromTenantId(policy.getTenantId());
            return !StringUtils.isBlank(tenantDomainFromId) && tenantDomainFromId.equals(userTenantDomain);
        }
    }

    /**
     * Checks whether given block condition is allowed to access to user
     *
     * @param user           username with tenant domain
     * @param blockCondition Block condition to check
     * @return true if user is allowed to access the block condition
     */
    public static boolean isBlockConditionAccessibleToUser(String user, BlockConditionsDTO blockCondition) {
        String userTenantDomain = MultitenantUtils.getTenantDomain(user);
        return !StringUtils.isBlank(blockCondition.getTenantDomain()) && blockCondition.getTenantDomain()
                .equals(userTenantDomain);
    }
}
