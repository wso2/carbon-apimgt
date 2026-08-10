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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.impl;

import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Tests organization resolution for the internal LLM provider endpoints.
 *
 * The resolved organization is the only scoping applied before the query, and a null
 * organization removes the ORGANIZATION predicate from the generated SQL entirely, so
 * these cases cover both the scoping behaviour and the fail-closed behaviour.
 */
public class LlmProvidersApiServiceImplTest {

    private static final String TENANT_DOMAIN = "provider-owner.example";
    private static final String OTHER_TENANT_DOMAIN = "another-tenant.example";
    private static final String ALL = APIConstants.AIAPIConstants.LLM_PROVIDER_TENANT_ALL;
    private static final String SUPER = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    // --- tenant-scoped caller: always pinned to its own organization ---

    @Test
    public void testTenantCallerRequestingAnotherOrganizationIsPinnedToItsOwn()
            throws APIManagementException {

        assertEquals(TENANT_DOMAIN,
                LlmProvidersApiServiceImpl.getOrganizationXWSO2Tenant(OTHER_TENANT_DOMAIN, TENANT_DOMAIN));
    }

    @Test
    public void testTenantCallerRequestingSuperTenantIsPinnedToItsOwn() throws APIManagementException {

        assertEquals(TENANT_DOMAIN,
                LlmProvidersApiServiceImpl.getOrganizationXWSO2Tenant(SUPER, TENANT_DOMAIN));
    }

    @Test
    public void testTenantCallerRequestingAllIsPinnedToItsOwn() throws APIManagementException {

        // "ALL" must not widen scope for a tenant caller, and must not resolve to null,
        // which would drop the ORGANIZATION predicate from the query.
        assertEquals(TENANT_DOMAIN,
                LlmProvidersApiServiceImpl.getOrganizationXWSO2Tenant(ALL, TENANT_DOMAIN));
    }

    @Test
    public void testTenantCallerRequestingItsOwnOrganizationIsUnchanged() throws APIManagementException {

        assertEquals(TENANT_DOMAIN,
                LlmProvidersApiServiceImpl.getOrganizationXWSO2Tenant(TENANT_DOMAIN, TENANT_DOMAIN));
    }

    @Test
    public void testTenantCallerWithoutRequestedTenantIsPinnedToItsOwn() throws APIManagementException {

        assertEquals(TENANT_DOMAIN,
                LlmProvidersApiServiceImpl.getOrganizationXWSO2Tenant(null, TENANT_DOMAIN));
    }

    // --- super tenant caller: existing behaviour preserved ---

    @Test
    public void testSuperTenantCallerCanRequestAnotherOrganization() throws APIManagementException {

        assertEquals(TENANT_DOMAIN,
                LlmProvidersApiServiceImpl.getOrganizationXWSO2Tenant(TENANT_DOMAIN, SUPER));
    }

    @Test
    public void testSuperTenantCallerRequestingAllResolvesToNoOrganizationFilter()
            throws APIManagementException {

        assertNull(LlmProvidersApiServiceImpl.getOrganizationXWSO2Tenant(ALL, SUPER));
    }

    @Test
    public void testSuperTenantCallerWithoutRequestedTenantDefaultsToSuperTenant()
            throws APIManagementException {

        assertEquals(SUPER, LlmProvidersApiServiceImpl.getOrganizationXWSO2Tenant(null, SUPER));
    }

    // --- unresolvable caller organization: must fail closed ---

    @Test
    public void testNullAuthenticatedOrganizationIsRejected() {

        assertRejected(null);
    }

    @Test
    public void testEmptyAuthenticatedOrganizationIsRejected() {

        // An empty organization must not be passed through: it removes the ORGANIZATION
        // predicate in the list query while retaining it in the by-id query.
        assertRejected("");
    }

    private void assertRejected(String authenticatedOrganization) {

        try {
            LlmProvidersApiServiceImpl.getOrganizationXWSO2Tenant(ALL, authenticatedOrganization);
            fail("Expected APIManagementException when the authenticated organization is unavailable");
        } catch (APIManagementException expected) {
            // expected
        }
    }
}
