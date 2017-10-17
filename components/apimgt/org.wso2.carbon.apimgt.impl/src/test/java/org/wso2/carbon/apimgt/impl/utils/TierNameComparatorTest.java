/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.utils.TierNameComparator;

public class TierNameComparatorTest {

    @Test
    public void testCompareEquals() {
        TierNameComparator tierNameComparator = new TierNameComparator();
        Tier tier1 = new Tier("GOLD");
        tier1.setDisplayName("GOLD");
        Tier tier2 = new Tier("GOLD");
        tier2.setDisplayName("GOLD");
        int result = tierNameComparator.compare(tier1, tier2);
        Assert.assertEquals(0, result);
    }

    @Test
    public void testCompareNotEquals() {
        TierNameComparator tierNameComparator = new TierNameComparator();
        Tier tier1 = new Tier("GOLD");
        tier1.setDisplayName("GOLD");
        Tier tier2 = new Tier("SILVER");
        tier2.setDisplayName("SILVER");
        int result = tierNameComparator.compare(tier1, tier2);
        Assert.assertNotEquals(0, result);
    }

    @Test
    public void testCompareWhenDisplayNameNotSetEquals() {
        TierNameComparator tierNameComparator = new TierNameComparator();
        Tier tier1 = new Tier("GOLD");
        Tier tier2 = new Tier("GOLD");
        int result = tierNameComparator.compare(tier1, tier2);
        Assert.assertEquals(0, result);
    }

    @Test
    public void testCompareWhenDisplayNameNotSetNotEquals() {
        TierNameComparator tierNameComparator = new TierNameComparator();
        Tier tier1 = new Tier("GOLD");
        Tier tier2 = new Tier("SILVER");
        int result = tierNameComparator.compare(tier1, tier2);
        Assert.assertNotEquals(0, result);
    }
}
