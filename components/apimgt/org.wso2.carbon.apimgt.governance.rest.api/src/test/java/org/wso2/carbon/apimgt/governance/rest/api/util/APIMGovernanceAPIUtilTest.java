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

package org.wso2.carbon.apimgt.governance.rest.api.util;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.governance.api.APIMGovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Regression coverage for the E04-015 fix: {@link APIMGovernanceAPIUtil#getValidatedOrganization}
 * must let a super tenant admin resolve to any organization via the X-WSO2-Tenant header
 * (handled upstream by OrganizationInterceptor/OnPremResolver, this method only receives the
 * already-resolved value), but must forbid a non-super tenant admin from being handed an
 * organization other than their own.
 */
public class APIMGovernanceAPIUtilTest {

    private static final String OWN_TENANT = "tenant-a.example";
    private static final String OTHER_TENANT = "tenant-b.example";

    @After
    public void tearDown() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    private void startAsTenant(String tenantDomain) {

        System.setProperty("carbon.home", APIMGovernanceAPIUtilTest.class.getResource("/").getFile());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
    }

    private MessageContext messageContextFor(String organization) {

        MessageContext ctx = Mockito.mock(MessageContext.class);
        Mockito.when(ctx.get(APIMGovernanceAPIConstants.ORGANIZATION)).thenReturn(organization);
        return ctx;
    }

    @Test
    public void testNonSuperCallerNamingOwnTenantIsAllowed() throws APIMGovernanceException {

        startAsTenant(OWN_TENANT);
        assertEquals(OWN_TENANT, APIMGovernanceAPIUtil.getValidatedOrganization(messageContextFor(OWN_TENANT)));
    }

    @Test
    public void testNonSuperCallerNamingDifferentTenantIsForbidden() {

        startAsTenant(OWN_TENANT);
        assertThrows(APIMGovernanceException.class,
                () -> APIMGovernanceAPIUtil.getValidatedOrganization(messageContextFor(OTHER_TENANT)));
    }

    @Test
    public void testSuperTenantCallerNamingDifferentTenantIsAllowed() throws APIMGovernanceException {

        startAsTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        assertEquals(OTHER_TENANT,
                APIMGovernanceAPIUtil.getValidatedOrganization(messageContextFor(OTHER_TENANT)));
    }

    @Test
    public void testMissingOrganizationStillRejected() {

        startAsTenant(OWN_TENANT);
        assertThrows(APIMGovernanceException.class,
                () -> APIMGovernanceAPIUtil.getValidatedOrganization(messageContextFor(null)));
    }
}
