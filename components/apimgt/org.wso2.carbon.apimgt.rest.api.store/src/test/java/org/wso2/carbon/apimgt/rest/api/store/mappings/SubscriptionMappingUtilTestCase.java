/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.rest.api.store.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;

public class SubscriptionMappingUtilTestCase {

    @Test
    public void testFromSubscriptionListToDTO() {
        String subUuid1 = UUID.randomUUID().toString();
        String subUuid2 = UUID.randomUUID().toString();
        Subscription subscription1 = SampleTestObjectCreator.createSubscription(subUuid1);
        Subscription subscription2 = SampleTestObjectCreator.createSubscription(subUuid2);
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);
        SubscriptionListDTO subscriptionListDTO = SubscriptionMappingUtil.
                                                                    fromSubscriptionListToDTO(subscriptionList, 10, 0);
        assertEquals(subscriptionListDTO.getCount(), (Integer)subscriptionList.size());
        assertEquals(subscription1.getId(), subscriptionListDTO.getList().get(0).getSubscriptionId());
        assertEquals(subscription1.getApi().getName(), subscriptionListDTO.getList().get(0).getApiName());
        assertEquals(subscription1.getApi().getId(), subscriptionListDTO.getList().get(0).getApiIdentifier());
        assertEquals(subscription1.getApi().getVersion(), subscriptionListDTO.getList().get(0).getApiVersion());
        assertEquals(SubscriptionDTO.LifeCycleStatusEnum.valueOf(subscription1.getStatus()
                .toString()).name(),subscriptionListDTO.getList().get(0).getLifeCycleStatus().name());

        assertEquals(subscriptionListDTO.getCount(), (Integer)subscriptionList.size());
        assertEquals(subscription2.getId(), subscriptionListDTO.getList().get(1).getSubscriptionId());
        assertEquals(subscription2.getApi().getName(), subscriptionListDTO.getList().get(1).getApiName());
        assertEquals(subscription2.getApi().getId(), subscriptionListDTO.getList().get(1).getApiIdentifier());
        assertEquals(subscription2.getApi().getVersion(), subscriptionListDTO.getList().get(1).getApiVersion());
        assertEquals(SubscriptionDTO.LifeCycleStatusEnum.valueOf(subscription2.getStatus()
                .toString()).name(),subscriptionListDTO.getList().get(1).getLifeCycleStatus().name());
    }
}
