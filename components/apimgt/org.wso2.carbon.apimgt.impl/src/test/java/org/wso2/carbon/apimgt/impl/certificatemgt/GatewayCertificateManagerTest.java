/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.certificatemgt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIGatewayAdminClient.class, ServiceReferenceHolder.class, GatewayCertificateManager.class, APIUtil.class})
public class GatewayCertificateManagerTest {

    private static final String PROD_1 = "PROD_1";
    private static final String PROD_1_PASSWORD = "wso2123";
    private static final String SND_1 = "SAND_BOX_1";
    private static final String SND_1_PASSWORD = "wso2345";
    private static final String BASE64_ENCODED_CERT =
            "MIIDEzCCAtGgAwIBAgIEC68tazALBgcqhkjOOAQDBQAwWzELMAkGA1UEBhMCbGsxCzAJBgNVBAgT\r\n" +
                    "AmxrMRAwDgYDVQQHEwdjb2xvbWJvMQ0wCwYDVQQKEwR3c28yMQ0wCwYDVQQLEwR3c28yMQ8wDQYD\r\n" +
                    "VQQDEwZtZW5ha2EwHhcNMTcxMDI2MTA0NzAzWhcNMTgwMTI0MTA0NzAzWjBbMQswCQYDVQQGEwJs\r\n" +
                    "azELMAkGA1UECBMCbGsxEDAOBgNVBAcTB2NvbG9tYm8xDTALBgNVBAoTBHdzbzIxDTALBgNVBAsT\r\n" +
                    "BHdzbzIxDzANBgNVBAMTBm1lbmFrYTCCAbgwggEsBgcqhkjOOAQBMIIBHwKBgQD9f1OBHXUSKVLf\r\n" +
                    "Spwu7OTn9hG3UjzvRADDHj+AtlEmaUVdQCJR+1k9jVj6v8X1ujD2y5tVbNeBO4AdNG/yZmC3a5lQ\r\n" +
                    "paSfn+gEexAiwk+7qdf+t8Yb+DtX58aophUPBPuD9tPFHsMCNVQTWhaRMvZ1864rYdcq7/IiAxmd\r\n" +
                    "0UgBxwIVAJdgUI8VIwvMspK5gqLrhAvwWBz1AoGBAPfhoIXWmz3ey7yrXDa4V7l5lK+7+jrqgvlX\r\n" +
                    "TAs9B4JnUVlXjrrUWU/mcQcQgYC0SRZxI+hMKBYTt88JMozIpuE8FnqLVHyNKOCjrh4rs6Z1kW6j\r\n" +
                    "fwv6ITVi8ftiegEkO8yk8b6oUZCJqIPf4VrlnwaSi2ZegHtVJWQBTDv+z0kqA4GFAAKBgQDAL5r4\r\n" +
                    "bix3HRG6LkBwAlWZtg+taHxOiLm3NzxMJLEIrJsOZ+ReO31zAO88Wkeibo6ff0D3mFtEdqZwdQDd\r\n" +
                    "zXwljpwk01xW0pg7IxDL/hdeC8jgxlDIB1Zz2NFwjDYeJtw8+l3e5T9c6fG0MsyhOYw3D2zvo66Z\r\n" +
                    "XUHI2Xu3P3ZLhKMhMB8wHQYDVR0OBBYEFOKUFMb/vRAyLr86vxJl0hwmy+jqMAsGByqGSM44BAMF\r\n" +
                    "AAMvADAsAhQW0OvWKXAO5V+37VtaAEX0yAYhgQIUG0q66Btv7Pk/HGGwBnYiHjCpuL4=\r\n";
    private static final String ALIAS = "TEST_ALIAS";
    private static GatewayCertificateManager gatewayCertificateManager;
    private static APIGatewayAdminClient apiGatewayAdminClient;

    @Before
    public void init() throws APIManagementException {
        apiGatewayAdminClient = PowerMockito.mock(APIGatewayAdminClient.class);
        Map<String, Environment> environmentMap = getEnvironments();

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService
                .class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn
                (apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getApiGatewayEnvironments()).thenReturn(environmentMap);

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getEnvironments()).thenReturn(environmentMap);
        gatewayCertificateManager = GatewayCertificateManager.getInstance();
    }

    @Test
    public void testAddToGatewaysAllSuccess() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIGatewayAdminClient.class, "addCertificate"))
                .toReturn(true);
        PowerMockito.whenNew(APIGatewayAdminClient.class).withAnyArguments().thenReturn(apiGatewayAdminClient);
        Map<String, List<Environment>> resultList = gatewayCertificateManager.addToGateways(BASE64_ENCODED_CERT, ALIAS);
        Assert.assertNotNull(resultList);
    }

    @Test
    public void testDeleteFromGateways() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIGatewayAdminClient.class, "deleteCertificate"))
                .toReturn(true);
        PowerMockito.whenNew(APIGatewayAdminClient.class).withAnyArguments().thenReturn(apiGatewayAdminClient);
        Map<String, List<Environment>> resultList = gatewayCertificateManager.removeFromGateways(ALIAS);
        Assert.assertNotNull(resultList);
    }

    /**
     * Creates dummy environment objects.
     */
    private static Map<String, Environment> getEnvironments() {
        Map<String, Environment> environmentMap = new HashMap<String, Environment>();
        Environment prodEnv = new Environment();
        prodEnv.setName(PROD_1);
        prodEnv.setPassword(PROD_1_PASSWORD);

        Environment sandBoxEnv = new Environment();
        sandBoxEnv.setName(SND_1);
        sandBoxEnv.setPassword(SND_1_PASSWORD);

        environmentMap.put(PROD_1, prodEnv);
        environmentMap.put(SND_1, sandBoxEnv);
        return environmentMap;
    }
}
