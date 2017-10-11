/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.gateway.throttling.publisher;

import org.junit.Test;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

public class ThrottleDataPublisherPoolTest {

    @Test
    public void testGetThrottleDataPublisher() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        ThrottleProperties.DataPublisherPool dataPublisherPool = new ThrottleProperties.DataPublisherPool();
        dataPublisherPool.setMaxIdle(10);
        dataPublisherPool.setInitIdleCapacity(10);
        throttleProperties.setDataPublisherPool(dataPublisherPool);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        ThrottleDataPublisherPool throttleDataPublisherPool = ThrottleDataPublisherPool.getInstance();
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = throttleDataPublisherPool.get();
        throttleDataPublisherPool.release(dataProcessAndPublishingAgent);
        throttleDataPublisherPool.cleanup();
    }
}