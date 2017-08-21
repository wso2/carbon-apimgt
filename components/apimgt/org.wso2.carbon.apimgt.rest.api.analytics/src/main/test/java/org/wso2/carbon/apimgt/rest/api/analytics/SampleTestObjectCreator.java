/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.analytics;

import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.APISubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionInfo;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Creates sample object required for unit tests.
 */
public class SampleTestObjectCreator {

    /**
     * Create Random ApplicationCount Object.
     *
     * @return ApplicationCount object
     */
    public static ApplicationCount createRandomApplicationCountObject() {
        ApplicationCount applicationCount = new ApplicationCount();
        applicationCount.setCount(ThreadLocalRandom.current().nextLong());
        applicationCount.setTimestamp(ThreadLocalRandom.current().nextLong());
        return applicationCount;
    }

    /**
     * Create Random APICount Object.
     *
     * @return Random APICount Object
     */
    public static APICount createRandomAPICountObject() {
        APICount apiCount = new APICount();
        apiCount.setCount(ThreadLocalRandom.current().nextLong());
        apiCount.setTimestamp(ThreadLocalRandom.current().nextLong());
        return apiCount;
    }

    /**
     * Create Random APICount Object.
     *
     * @return Random APICount Object
     */
    public static APIInfo createRandomAPIInfoObject() {
        APIInfo apiInfo = new APIInfo();
        apiInfo.setId(UUID.randomUUID().toString());
        apiInfo.setName(UUID.randomUUID().toString());
        apiInfo.setDescription(UUID.randomUUID().toString());
        apiInfo.setContext(UUID.randomUUID().toString());
        apiInfo.setVersion(UUID.randomUUID().toString());
        apiInfo.setProvider(UUID.randomUUID().toString());
        apiInfo.setLifeCycleStatus(UUID.randomUUID().toString());
        apiInfo.setWorkflowStatus(UUID.randomUUID().toString());
        apiInfo.setCreatedTime(ThreadLocalRandom.current().nextLong());
        return apiInfo;
    }

    /**
     * Create Random APISubscriptionCount Object.
     *
     * @return Random APISubscriptionCount Object
     */
    public static APISubscriptionCount createRandomAPISubscriptionCountObject() {
        APISubscriptionCount apiSubscriptionCount = new APISubscriptionCount();
        apiSubscriptionCount.setId(UUID.randomUUID().toString());
        apiSubscriptionCount.setName(UUID.randomUUID().toString());
        apiSubscriptionCount.setVersion(UUID.randomUUID().toString());
        apiSubscriptionCount.setProvider(UUID.randomUUID().toString());
        apiSubscriptionCount.setCount(new Random().nextInt());
        return apiSubscriptionCount;
    }

    /**
     * Create Random SubscriptionCount Object.
     *
     * @return Random SubscriptionCount Object
     */
    public static SubscriptionCount createRandomSubscriptionCountObject() {
        SubscriptionCount subscriptionCount = new SubscriptionCount();
        subscriptionCount.setCount(new Random().nextInt());
        subscriptionCount.setTimestamp(ThreadLocalRandom.current().nextLong());
        return subscriptionCount;
    }

    /**
     * Create Random SubscriptionInfo Object.
     *
     * @return Random SubscriptionInfo Object
     */
    public static SubscriptionInfo createRandomSubscriptionInfoObject() {
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
        subscriptionInfo.setId(UUID.randomUUID().toString());
        subscriptionInfo.setSubscriptionTier(UUID.randomUUID().toString());
        subscriptionInfo.setSubscriptionStatus(UUID.randomUUID().toString());
        subscriptionInfo.setVersion(UUID.randomUUID().toString());
        subscriptionInfo.setName(UUID.randomUUID().toString());
        subscriptionInfo.setCreatedTime(ThreadLocalRandom.current().nextLong());
        subscriptionInfo.setDescription(UUID.randomUUID().toString());
        subscriptionInfo.setAppName(UUID.randomUUID().toString());
        return subscriptionInfo;
    }
}
