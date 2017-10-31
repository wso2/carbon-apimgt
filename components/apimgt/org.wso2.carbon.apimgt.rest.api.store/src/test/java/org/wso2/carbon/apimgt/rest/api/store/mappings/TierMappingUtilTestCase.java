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
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.rest.api.store.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class TierMappingUtilTestCase {

    @Test
    public void testFromTierListToDTO() {
        Policy policy1 = SampleTestObjectCreator.createSubscriptionPolicyWithRequestLimit("Gold");
        Policy policy2 = SampleTestObjectCreator.createSubscriptionPolicyWithBndwidthLimit("Silver");
        List<Policy> policyList = new ArrayList<>();
        policyList.add(policy1);
        policyList.add(policy2);
        TierListDTO tierListDTO = TierMappingUtil.fromTierListToDTO(policyList, "subscription", 10, 0);
        assertEquals(tierListDTO.getCount(), (Integer) policyList.size());
        assertEquals(tierListDTO.getList().get(0).getName(), policy1.getPolicyName());
        assertEquals(tierListDTO.getList().get(0).getDescription(), policy1.getDescription());
        assertEquals(tierListDTO.getList().get(0).getTierLevel().name(), "SUBSCRIPTION");
        assertEquals(tierListDTO.getList().get(0).getUnitTime().longValue(), policy1.
                                                        getDefaultQuotaPolicy().getLimit().getUnitTime());
        assertEquals(tierListDTO.getList().get(0).getRequestCount().longValue(), ((RequestCountLimit)
                                                    policy1.getDefaultQuotaPolicy().getLimit()).getRequestCount());
        assertEquals(tierListDTO.getList().get(1).getName(), policy2.getPolicyName());
        assertEquals(tierListDTO.getList().get(1).getDescription(), policy2.getDescription());
        assertEquals(tierListDTO.getList().get(1).getTierLevel().name(), "SUBSCRIPTION");
        assertEquals(tierListDTO.getList().get(1).getUnitTime().longValue(), policy2.
                getDefaultQuotaPolicy().getLimit().getUnitTime());
        assertEquals(tierListDTO.getList().get(1).getRequestCount().longValue(), ((BandwidthLimit)
                policy2.getDefaultQuotaPolicy().getLimit()).getDataAmount());
    }
}
