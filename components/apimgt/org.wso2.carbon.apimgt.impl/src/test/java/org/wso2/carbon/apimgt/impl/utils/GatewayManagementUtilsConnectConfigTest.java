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
import org.wso2.carbon.apimgt.impl.dto.ConnectGatewayConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GatewayManagementUtilsConnectConfigTest {

    @Test
    public void testValidateConnectGatewayEntriesReportsMissingToken() {
        ConnectGatewayConfig entry = new ConnectGatewayConfig();
        entry.setUrl("https://gw.example.com:8243");

        List<String> errors = GatewayManagementUtils.validateConnectGatewayEntries(Collections.singletonList(entry));

        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).contains("mandatory 'registration_token' is missing"));
    }

    @Test
    public void testValidateConnectGatewayEntriesReportsMissingUrl() {
        ConnectGatewayConfig entry = new ConnectGatewayConfig();
        entry.setRegistrationToken("token-id.plain-token-value");

        List<String> errors = GatewayManagementUtils.validateConnectGatewayEntries(Collections.singletonList(entry));

        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).contains("mandatory 'url' is missing"));
    }

    @Test
    public void testValidateConnectGatewayEntriesReportsInvalidTokenFormat() {
        ConnectGatewayConfig entry = new ConnectGatewayConfig();
        entry.setRegistrationToken("invalid-token-without-separator");
        entry.setUrl("https://gw.example.com:8243");

        List<String> errors = GatewayManagementUtils.validateConnectGatewayEntries(Collections.singletonList(entry));

        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).contains("invalid registration_token format"));
    }

    @Test
    public void testValidateConnectGatewayEntriesAcceptsValidEntry() {
        ConnectGatewayConfig entry = new ConnectGatewayConfig();
        entry.setRegistrationToken("token-id.plain-token-value");
        entry.setUrl("https://gw.example.com:8243");

        List<String> errors = GatewayManagementUtils.validateConnectGatewayEntries(Arrays.asList(entry, null));

        Assert.assertTrue(errors.isEmpty());
    }
}
