/*
 *
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.Tier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(PowerMockRunner.class)
public class PublisherCommonUtilsTest {

    @Test
    public void testGetInvalidTierNames() throws Exception {

        List<String>  currentTiers = Arrays.asList(new String[]{"Unlimitted", "Platinum", "gold"});
        Tier mockTier = Mockito.mock(Tier.class);
        Tier tier1 = new Tier("Gold");
        Tier tier2 = new Tier("Unlimitted");
        Tier tier3 = new Tier("Silver");
        Set<Tier> allTiers = new HashSet<Tier>();
        allTiers.add(tier1);
        allTiers.add(tier2);
        allTiers.add(tier3);
        PowerMockito.whenNew(Tier.class).withAnyArguments().thenReturn(mockTier);
        Mockito.when(mockTier.getName()).thenReturn("Unlimitted");
        List<String> expectedInvalidTier = Arrays.asList(new String[]{"Platinum", "gold"});
        Assert.assertEquals(PublisherCommonUtils.getInvalidTierNames(allTiers, currentTiers), expectedInvalidTier);
    }

}
