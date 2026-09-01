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

package org.wso2.carbon.apimgt.internal.service.impl;

import org.junit.Test;
import org.wso2.carbon.apimgt.internal.service.dto.KeyManagerDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class KeymanagersApiServiceImplTest {

    @Test
    public void testSuperTenantKeepsAdditionalProperties() {

        List<KeyManagerDTO> keyManagerDTOList = Collections.singletonList(
                keyManagerWithProperties());

        List<KeyManagerDTO> result = KeymanagersApiServiceImpl.redactAdditionalPropertiesForNonSuperTenant(
                keyManagerDTOList, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

        assertEquals("secret", ((Map<?, ?>) result.get(0).getAdditionalProperties()).get("Password"));
    }

    @Test
    public void testSuperTenantRecognizedRegardlessOfCase() {

        List<KeyManagerDTO> keyManagerDTOList = Collections.singletonList(
                keyManagerWithProperties());

        List<KeyManagerDTO> result = KeymanagersApiServiceImpl.redactAdditionalPropertiesForNonSuperTenant(
                keyManagerDTOList, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.toUpperCase());

        assertEquals("secret", ((Map<?, ?>) result.get(0).getAdditionalProperties()).get("Password"));
    }

    @Test
    public void testNonSuperTenantRedactsOwnAndGlobalEntries() {

        List<KeyManagerDTO> keyManagerDTOList = Arrays.asList(
                keyManagerWithProperties(), keyManagerWithProperties());

        List<KeyManagerDTO> result = KeymanagersApiServiceImpl.redactAdditionalPropertiesForNonSuperTenant(
                keyManagerDTOList, "attacker-tenant.com");

        assertNull(result.get(0).getAdditionalProperties());
        assertNull(result.get(1).getAdditionalProperties());
    }

    @Test
    public void testUnresolvedAuthenticatedTenantFailsClosed() {

        List<KeyManagerDTO> keyManagerDTOList = Collections.singletonList(
                keyManagerWithProperties());

        List<KeyManagerDTO> result = KeymanagersApiServiceImpl.redactAdditionalPropertiesForNonSuperTenant(
                keyManagerDTOList, null);

        assertNull(result.get(0).getAdditionalProperties());
    }

    private KeyManagerDTO keyManagerWithProperties() {

        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("Password", "secret");
        KeyManagerDTO keyManagerDTO = new KeyManagerDTO();
        keyManagerDTO.setAdditionalProperties(additionalProperties);
        return keyManagerDTO;
    }
}
