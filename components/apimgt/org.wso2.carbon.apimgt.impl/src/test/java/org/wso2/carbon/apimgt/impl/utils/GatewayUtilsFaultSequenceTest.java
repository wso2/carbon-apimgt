/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for {@link GatewayUtils#setEndpointSequencesToBeRemoved}: undeploy must also remove the fault
 * sequences, or they're orphaned in gateway memory.
 */
public class GatewayUtilsFaultSequenceTest {

    @Test
    public void testFaultSequenceNamesAreQueuedForRemovalAlongsideTheEndpointsSequences() {

        API api = new API(new APIIdentifier("admin", "SampleAIAPI", "2.3.0"));
        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();

        GatewayUtils.setEndpointSequencesToBeRemoved(api, gatewayAPIDTO);

        Set<String> removed = new HashSet<>(Arrays.asList(gatewayAPIDTO.getSequencesToBeRemove()));
        // Deploy names the sequences "<name>--v<version>...", and removal must use the same names.
        String endpointKey = "SampleAIAPI--v2.3.0";

        // The endpoints sequences are queued under the names deploy gives them.
        Assert.assertTrue(removed.contains(endpointKey + AIAPIConstants.ENDPOINT_SEQUENCE
                + APIConstants.APIEndpoint.PRODUCTION));
        Assert.assertTrue(removed.contains(endpointKey + AIAPIConstants.ENDPOINT_SEQUENCE
                + APIConstants.APIEndpoint.SANDBOX));
        // Fault sequences are queued too, for both stages, under the exact names deploy gives them.
        Assert.assertTrue(removed.contains("SampleAIAPI--v2.3.0_EndpointsFaultSeqPRODUCTION"));
        Assert.assertTrue(removed.contains("SampleAIAPI--v2.3.0_EndpointsFaultSeqSANDBOX"));
    }

    @Test
    public void testFaultSequenceNameIsHarmlessToQueueEvenWhenTheApiNeverHadOne() {

        // No fault sequence was ever deployed; queueing its name for removal must not throw.
        API api = new API(new APIIdentifier("admin", "PlainChatAPI", "1.0"));
        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();

        GatewayUtils.setEndpointSequencesToBeRemoved(api, gatewayAPIDTO);

        Assert.assertEquals(4, gatewayAPIDTO.getSequencesToBeRemove().length);
    }
}
