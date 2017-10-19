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

package org.wso2.carbon.apimgt.impl.template;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class HandlerConfigTest {

    @Test
    public void testHandlerConfig() throws Exception {

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("key", "value");
        HandlerConfig handlerConfig = new HandlerConfig("test", properties);
        Assert.assertTrue("test".equalsIgnoreCase(handlerConfig.getClassName()));
        Assert.assertTrue(handlerConfig.getProperties().get("key").equalsIgnoreCase("value"));
        handlerConfig.setClassName("HandlerConfigTest");
        Assert.assertTrue("HandlerConfigTest".equalsIgnoreCase(handlerConfig.getClassName()));
        properties.put("key1", "value1");
        handlerConfig.setProperties(properties);
        Assert.assertTrue(handlerConfig.getProperties().get("key1").equalsIgnoreCase("value1"));
    }
}
