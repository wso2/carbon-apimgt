/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.util.test;

import junit.framework.TestCase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class TierTest extends TestCase {

    /**
     * Test whether the APIUtil properly converts the billing plan and the custom attributes in the SubscriptionPolicy
     * when constructing the Tier object.
     */
    public void testBillingPlanAndCustomAttr(){
        SubscriptionPolicy policy = new SubscriptionPolicy("JUnitTest");
        JSONArray jsonArray = new JSONArray();

        JSONObject json1 = new JSONObject();
        json1.put("name", "key1");
        json1.put("value", "value1");
        jsonArray.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("name", "key2");
        json2.put("value", "value2");
        jsonArray.add(json2);

        policy.setCustomAttributes(jsonArray.toJSONString().getBytes());
        policy.setBillingPlan("FREE");

        Tier tier = new Tier("JUnitTest");

        APIUtil.setBillingPlanAndCustomAttributesToTier(policy, tier);

        assertTrue("Expected FREE but received " + tier.getTierPlan(), "FREE".equals(tier.getTierPlan()));

        if("key1".equals(tier.getTierAttributes().get("name"))){
            assertTrue("Expected to have 'value1' as the value of 'key1' but found " +
                            tier.getTierAttributes().get("value"),
                    tier.getTierAttributes().get("value").equals("value1"));
        }
        if("key2".equals(tier.getTierAttributes().get("name"))){
            assertTrue("Expected to have 'value2' as the value of 'key2' but found " +
                            tier.getTierAttributes().get("value"),
                    tier.getTierAttributes().get("value").equals("value2"));
        }
    }
}
