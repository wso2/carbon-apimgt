/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.monetization;

import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;

import java.util.HashMap;
import java.util.Map;

public class DefaultMonetizationImpl implements Monetization {

    @Override
    public boolean createBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException {
        return true;
    }

    @Override
    public boolean updateBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException {
        return true;
    }

    @Override
    public boolean deleteBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException {
        return true;
    }

    @Override
    public boolean enableMonetization(String tenantDomain, API api, Map<String, String> monetizationProperties)
            throws MonetizationException {
        return true;
    }

    @Override
    public boolean disableMonetization(String tenantDomain, API api, Map<String, String> monetizationProperties)
            throws MonetizationException {
        return true;
    }

    @Override
    public Map<String, String> getMonetizedPoliciesToPlanMapping(API api) throws MonetizationException {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getCurrentUsage(String subscriptionUUID, APIProvider apiProvider)
            throws MonetizationException {
        return new HashMap<>();
    }
}
