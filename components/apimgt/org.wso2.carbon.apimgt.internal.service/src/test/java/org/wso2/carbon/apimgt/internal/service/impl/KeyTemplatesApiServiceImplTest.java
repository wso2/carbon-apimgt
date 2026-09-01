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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeyTemplatesApiServiceImplTest {

    @Test
    public void testSuperTenantRecognized() {

        assertTrue(KeyTemplatesApiServiceImpl.isSuperTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
    }

    @Test
    public void testSuperTenantRecognizedRegardlessOfCase() {

        assertTrue(KeyTemplatesApiServiceImpl.isSuperTenant(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.toUpperCase()));
    }

    @Test
    public void testNonSuperTenantRejected() {

        assertFalse(KeyTemplatesApiServiceImpl.isSuperTenant("tenant-a.com"));
    }

    @Test
    public void testUnresolvedAuthenticatedTenantFailsClosed() {

        assertFalse(KeyTemplatesApiServiceImpl.isSuperTenant(null));
        assertFalse(KeyTemplatesApiServiceImpl.isSuperTenant(""));
    }
}
