/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDTO;

import java.util.Collections;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiCommonUtil.class})
public class OperationPolicyMappingUtilTest {

    private static final String API_UUID = "api-uuid";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String API_SPECIFIC_POLICY_ID = "api-specific-policy-id";
    private static final String COMMON_POLICY_ID = "common-policy-id";
    private static final String POLICY_NAME = "log_policy";
    private static final String POLICY_VERSION = "v1";

    @Test
    public void testFromOperationPolicyToDTOExposesCommonPolicyIdForClonedPolicy() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        OperationPolicyData policyData = new OperationPolicyData();
        policyData.setPolicyId(API_SPECIFIC_POLICY_ID);
        policyData.setClonedCommonPolicyId(COMMON_POLICY_ID);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyId(API_SPECIFIC_POLICY_ID, API_UUID,
                TENANT_DOMAIN, false)).thenReturn(policyData);

        OperationPolicyDTO dto = OperationPolicyMappingUtil.fromOperationPolicyToDTO(createOperationPolicy(),
                API_UUID, false);

        Assert.assertEquals("Expected cloned common policy ID to be exposed for publisher lookups",
                COMMON_POLICY_ID, dto.getPolicyId());
    }

    @Test
    public void testFromOperationPolicyToDTORetainsApiSpecificIdForNonClonedPolicy() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        OperationPolicyData policyData = new OperationPolicyData();
        policyData.setPolicyId(API_SPECIFIC_POLICY_ID);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(TENANT_DOMAIN);
        Mockito.when(apiProvider.getAPISpecificOperationPolicyByPolicyId(API_SPECIFIC_POLICY_ID, API_UUID,
                TENANT_DOMAIN, false)).thenReturn(policyData);

        OperationPolicyDTO dto = OperationPolicyMappingUtil.fromOperationPolicyToDTO(createOperationPolicy(),
                API_UUID, false);

        Assert.assertEquals("Expected API-specific policy ID to remain unchanged for non-cloned policies",
                API_SPECIFIC_POLICY_ID, dto.getPolicyId());
    }

    private OperationPolicy createOperationPolicy() {

        OperationPolicy operationPolicy = new OperationPolicy();
        operationPolicy.setPolicyName(POLICY_NAME);
        operationPolicy.setPolicyVersion(POLICY_VERSION);
        operationPolicy.setPolicyId(API_SPECIFIC_POLICY_ID);
        operationPolicy.setParameters(Collections.emptyMap());
        return operationPolicy;
    }
}
