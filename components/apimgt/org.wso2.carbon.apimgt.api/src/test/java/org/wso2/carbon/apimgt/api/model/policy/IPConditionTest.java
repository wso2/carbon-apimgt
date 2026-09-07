/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

package org.wso2.carbon.apimgt.api.model.policy;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the address representation and inclusive bounds in generated IP policy conditions.
 */
public class IPConditionTest {

    @Test
    public void testSpecificIPv6AddressUsesUnsignedValue() {

        IPCondition condition = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
        condition.setSpecificIP("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");

        Assert.assertEquals("(throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), "
                + "'340282366920938463463374607431768211455')==0)", condition.getCondition());
    }

    @Test
    public void testEquivalentIPv6RepresentationsGenerateSameCondition() {

        IPCondition condition = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
        condition.setSpecificIP("::1");
        String expected = "(throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), '1')==0)";
        Assert.assertEquals(expected, condition.getCondition());

        condition.setSpecificIP("0000:0000:0000:0000:0000:0000:0000:0001");
        Assert.assertEquals(expected, condition.getCondition());
    }

    @Test
    public void testInvertedSpecificIPv6Condition() {

        IPCondition condition = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
        condition.setSpecificIP("::1");
        condition.setInvertCondition(true);

        Assert.assertEquals("NOT(throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), '1')==0)",
                condition.getCondition());
    }

    @Test
    public void testIPv6RangeIncludesBothBounds() {

        IPCondition condition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
        condition.setStartingIP("::");
        condition.setEndingIP("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");

        Assert.assertEquals("(throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), '0')>=0 AND "
                + "throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), "
                + "'340282366920938463463374607431768211455')<=0)", condition.getCondition());
    }

    @Test
    public void testInvertedIPv6RangeNegatesWholeCondition() {

        IPCondition condition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
        condition.setStartingIP("::1");
        condition.setEndingIP("::2");
        condition.setInvertCondition(true);

        Assert.assertEquals("NOT(throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), '1')>=0 AND "
                + "throttler:bigIntcmp(map:get(propertiesMap,'ipv6'), '2')<=0)", condition.getCondition());
    }

    @Test
    public void testIPv4RangeRetainsUnsignedLongBounds() {

        IPCondition condition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
        condition.setStartingIP("128.0.0.0");
        condition.setEndingIP("255.255.255.255");

        Assert.assertEquals("(2147483648l<=cast(map:get(propertiesMap,'ip'),'Long') AND "
                + "4294967295l>=cast(map:get(propertiesMap,'ip'),'Long'))", condition.getCondition());
    }
}
