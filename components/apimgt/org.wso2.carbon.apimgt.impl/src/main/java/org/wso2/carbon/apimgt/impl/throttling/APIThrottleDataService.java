/*
 *
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.throttling;

import org.wso2.carbon.apimgt.impl.dto.ConditionDto;

import java.util.List;

/**
 * Interface for retrieving ThrottleData.
 */
public interface APIThrottleDataService {


    public void addThrottledApiConditions(String resourceKey, String name,
                                          List<ConditionDto> conditionDtos);

    boolean isAPIThrottled(String resourceKey);

    void removeThrottledAPIKey(String resourceKey);

    void removeThrottledApiConditions(String resourceKey, String extractedKey);

    void addThrottleData(String throttleKey, Long timeStamp);

    void addThrottledAPIKey(String resourceKey, Long timeStamp);

    void removeThrottleData(String throttleKey);

    void addBlockingCondition(String type, String conditionKey, String conditionValue);

    void removeBlockCondition(String type, String conditionKey);

    void addIpBlockingCondition(String tenantDomain, int conditionId, String conditionValue, String type);

    void removeIpBlockingCondition(String tenantDomain, int conditionId);

    void addKeyTemplate(String key, String keyTemplateValue);

    void removeKeyTemplate(String key);
}
