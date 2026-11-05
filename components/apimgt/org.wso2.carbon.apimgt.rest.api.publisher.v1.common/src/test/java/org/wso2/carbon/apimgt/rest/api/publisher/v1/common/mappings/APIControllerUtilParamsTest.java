/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIControllerUtilParamsTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testConvertValuesToStringsConvertsIntegersInLists() throws Exception {
        // Endpoint config as read from a params file (Jackson keeps whole numbers as Integer)
        HashMap<String, Object> endpointConfig = new ObjectMapper().readValue("{\"endpoint_type\":\"load_balance\","
                + "\"production_endpoints\":{\"url\":\"https://api.example.com/1\",\"config\":{\"retryTimeOut\":1,"
                + "\"suspendErrorCode\":[101001,\"101500\"],\"factor\":2.5}},"
                + "\"sandbox_endpoints\":[{\"url\":\"https://api.example.com/2\",\"config\":{\"suspendDuration\":100,"
                + "\"retryErroCode\":[101001]}}]}", HashMap.class);

        Whitebox.invokeMethod(APIControllerUtil.class, "convertValuesToStrings", endpointConfig);

        Map<String, Object> productionConfig =
                (Map<String, Object>) ((Map<String, Object>) endpointConfig.get("production_endpoints")).get("config");
        Assert.assertEquals("1", productionConfig.get("retryTimeOut"));
        Assert.assertEquals(Arrays.asList("101001", "101500"), productionConfig.get("suspendErrorCode"));
        // non-integer numbers are not changed
        Assert.assertEquals(2.5, productionConfig.get("factor"));

        Map<String, Object> sandboxConfig = (Map<String, Object>) ((Map<String, Object>)
                ((List<Object>) endpointConfig.get("sandbox_endpoints")).get(0)).get("config");
        Assert.assertEquals("100", sandboxConfig.get("suspendDuration"));
        Assert.assertEquals(Arrays.asList("101001"), sandboxConfig.get("retryErroCode"));
        Assert.assertEquals("https://api.example.com/2", ((Map<String, Object>)
                ((List<Object>) endpointConfig.get("sandbox_endpoints")).get(0)).get("url"));
    }
}
