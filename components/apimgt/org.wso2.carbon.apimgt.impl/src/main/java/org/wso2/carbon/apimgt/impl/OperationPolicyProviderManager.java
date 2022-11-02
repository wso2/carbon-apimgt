/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;


import org.wso2.carbon.apimgt.api.OperationPolicyProvider;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.Map;
import java.util.Objects;

import static org.wso2.carbon.apimgt.impl.APIConstants.OperationPolicyConstants;


public class OperationPolicyProviderManager {

    private static OperationPolicyProvider operationPolicyProvider = null;

    public static OperationPolicyProvider getPolicyProviderInstance() {

        Map<String, String> configs = APIManagerConfiguration.getOperationalPolicyProperties();

        if (operationPolicyProvider == null) {
            ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
            if (serviceReferenceHolder.getPolicyProvider() != null) {
                if (Objects.equals(configs.get(OperationPolicyConstants.CONFIG_KEY_PROVIDER),
                        OperationPolicyConstants.CONFIG_VALUE_BCENTRAL)) {
                    operationPolicyProvider = serviceReferenceHolder.getPolicyProvider();
                } else {
                    operationPolicyProvider = new DefaultOperationPolicyProvider();
                }
            } else {
                operationPolicyProvider = new DefaultOperationPolicyProvider();
            }
        }

        return operationPolicyProvider;
    }
}
