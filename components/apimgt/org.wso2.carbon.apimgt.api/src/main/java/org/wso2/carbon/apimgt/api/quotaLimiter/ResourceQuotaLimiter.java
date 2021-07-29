/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.api.quotaLimiter;

/**
 * This interface helps to bind API-quota-limiter extension to the carbon-apimgt and
 * contains a method to check API creation quota achieved or not.
 */
public interface ResourceQuotaLimiter {

    /**
     * This method used to check API creation quota achieved or not using the API-quota-limiter extension.
     * In normal scenarios API creation is quota is not checked and this interface implemented by
     * OnPremQuotaLimiter class to handle scenarios for not having API-quota-limiter extension.
     * @param orgID Organization ID relevant for the API creation.
     * @param userId User ID value relevant for API creation.
     * @param resourceType Resource type (APIs) used to check quota status.
     * @return A boolean value is returned to indicate API creation allowed or not.
     */
    boolean getAPIQuotaLimitStatus(String orgID, String userId, String resourceType);
}
