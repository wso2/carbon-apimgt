/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.model.util;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

public class SubscriptionValidationUtils {

    /**
     * Validates Subscription status and set the relevant error values in
     * {@link APIKeyValidationInfoDTO} object.
     */
    private boolean validateAndSetSubscriptionStatus(String subscriptionStatus, String keyType,
                                                     APIKeyValidationInfoDTO dto) {

        if (APIConstants.SubscriptionStatus.BLOCKED.equals(subscriptionStatus)) {
            dto.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
            dto.setAuthorized(false);
            return false;
        } else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus) || APIConstants
                .SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
            dto.setValidationStatus(APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
            dto.setAuthorized(false);
            return false;
        } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subscriptionStatus) &&
                !APIConstants.API_KEY_TYPE_SANDBOX.equals(keyType)) {
            dto.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
            dto.setType(keyType);
            dto.setAuthorized(false);
            return false;
        }

        return true;
    }

}
