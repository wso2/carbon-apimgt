/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.gateway.service;

import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ConditionDto;
import org.wso2.carbon.apimgt.impl.throttling.APIThrottleDataService;
import java.util.List;

/**
 * Implementation of  {@code APIThrottleDataService}. This class holds a singleton of ThrottleDataHolder and provides
 * access to throttle key data.
 */
public class APIThrottleDataServiceImpl implements APIThrottleDataService {

    private ThrottleDataHolder throttleDataHolder;

    public APIThrottleDataServiceImpl(ThrottleDataHolder throttleDataHolder) {

        this.throttleDataHolder = throttleDataHolder;
    }

    @Override
    public void addThrottledApiConditions(String resourceKey, String name,
                                          List<ConditionDto> conditionDtos) {

        throttleDataHolder.addThrottledApiConditions(resourceKey, name, conditionDtos);
    }

    @Override
    public boolean isAPIThrottled(String resourceKey) {

        return throttleDataHolder.isAPIThrottled(resourceKey);
    }

    @Override
    public void removeThrottledAPIKey(String resourceKey) {

        throttleDataHolder.removeThrottledAPIKey(resourceKey);
    }

    @Override
    public void removeThrottledApiConditions(String resourceKey, String extractedKey) {

        throttleDataHolder.removeThrottledApiConditions(resourceKey, extractedKey);
    }

    @Override
    public void addThrottleData(String throttleKey, Long timeStamp) {

        throttleDataHolder.addThrottleData(throttleKey, timeStamp);
    }

    @Override
    public void addThrottledAPIKey(String resourceKey, Long timeStamp) {

        throttleDataHolder.addThrottledAPIKey(resourceKey, timeStamp);
    }

    @Override
    public void removeThrottleData(String throttleKey) {

        throttleDataHolder.removeThrottleData(throttleKey);
    }

    @Override
    public void addBlockingCondition(String type, String conditionKey, String conditionValue) {

        switch (type) {
            case APIConstants.BLOCKING_CONDITIONS_APPLICATION:
                throttleDataHolder.addApplicationBlockingCondition(conditionKey, conditionValue);
                break;
            case APIConstants.BLOCKING_CONDITIONS_API:
                throttleDataHolder.addAPIBlockingCondition(conditionKey, conditionValue);
                break;
            case APIConstants.BLOCKING_CONDITIONS_USER:
                throttleDataHolder.addUserBlockingCondition(conditionKey, conditionValue);
                break;
            case APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION:
                throttleDataHolder.addSubscriptionBlockingCondition(conditionKey, conditionValue);
                break;
        }
    }

    @Override
    public void removeBlockCondition(String type, String conditionKey) {

        switch (type) {
            case APIConstants.BLOCKING_CONDITIONS_APPLICATION:
                throttleDataHolder.removeApplicationBlockingCondition(conditionKey);
                break;
            case APIConstants.BLOCKING_CONDITIONS_API:
                throttleDataHolder.removeAPIBlockingCondition(conditionKey);
                break;
            case APIConstants.BLOCKING_CONDITIONS_USER:
                throttleDataHolder.removeUserBlockingCondition(conditionKey);
                break;
            case APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION:
                throttleDataHolder.removeSubscriptionBlockingCondition(conditionKey);
                break;
        }
    }

    @Override
    public void addIpBlockingCondition(String tenantDomain, int conditionId, String conditionValue, String type) {

        throttleDataHolder.addIpBlockingCondition(tenantDomain, conditionId, conditionValue, type);
    }

    @Override
    public void removeIpBlockingCondition(String tenantDomain, int conditionId) {

        throttleDataHolder.removeIpBlockingCondition(tenantDomain, conditionId);
    }

    @Override
    public void addKeyTemplate(String key, String keyTemplateValue) {

        throttleDataHolder.addKeyTemplate(key, keyTemplateValue);
    }

    @Override
    public void removeKeyTemplate(String key) {

        throttleDataHolder.removeKeyTemplate(key);
    }

    public void setThrottleDataHolder(ThrottleDataHolder holder) {

        this.throttleDataHolder = holder;
    }
}
