/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.gateway.throttling;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class ThrottleDataHolderTest {

    @Test
    public void addThrottleDataFromMap() throws Exception {
        Map<String,Long> map = new HashMap<>();
        map.put("/api/1.0.0",System.currentTimeMillis());
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        throttleDataHolder.addThrottleDataFromMap(map);
        throttleDataHolder.removeThrottleData("/api/1.0.0");
    }


    @Test
    public void removeThrottledAPIKey() throws Exception {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        throttleDataHolder.addThrottledAPIKey("/api/1.0.0",System.currentTimeMillis());
        throttleDataHolder.removeThrottledAPIKey("/api/1.0.0");
    }


    @Test
    public void addBlockingCondition() throws Exception {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        throttleDataHolder.addAPIBlockingCondition("/api1/1.0.0","enabled");
        throttleDataHolder.removeAPIBlockingCondition("/api1/1.0.0");
        throttleDataHolder.addApplicationBlockingCondition("admin:DefaultApplication","enabled");
        throttleDataHolder.removeApplicationBlockingCondition("admin:DefaultApplication");
        throttleDataHolder.addUserBlockingCondition("user1","enabled");
        throttleDataHolder.removeUserBlockingCondition("user1");
        throttleDataHolder.setKeyTemplatesPresent(true);
    }

    @Test
    public void addApplicationBlockingCondition() throws Exception {
    }

    @Test
    public void addUserBlockingCondition() throws Exception {
    }

    @Test
    public void addIplockingCondition() throws Exception {
    }

    @Test
    public void addUserBlockingConditionsFromMap() throws Exception {
    }

    @Test
    public void addIplockingConditionsFromMap() throws Exception {
    }

    @Test
    public void addAPIBlockingConditionsFromMap() throws Exception {
    }

    @Test
    public void addApplicationBlockingConditionsFromMap() throws Exception {
    }

    @Test
    public void removeAPIBlockingCondition() throws Exception {
    }

    @Test
    public void removeApplicationBlockingCondition() throws Exception {
    }

    @Test
    public void removeUserBlockingCondition() throws Exception {
    }

    @Test
    public void removeIpBlockingCondition() throws Exception {
    }

    @Test
    public void addKeyTemplate() throws Exception {
    }

    @Test
    public void addKeyTemplateFromMap() throws Exception {
    }

    @Test
    public void removeKeyTemplate() throws Exception {
    }

    @Test
    public void getKeyTemplateMap() throws Exception {
    }

    @Test
    public void isThrottled() throws Exception {
    }

    @Test
    public void getThrottleNextAccessTimestamp() throws Exception {
    }

    @Test
    public void isBlockingConditionsPresent() throws Exception {
    }

    @Test
    public void setBlockingConditionsPresent() throws Exception {
    }

    @Test
    public void isKeyTemplatesPresent() throws Exception {
    }

    @Test
    public void setKeyTemplatesPresent() throws Exception {
    }

}