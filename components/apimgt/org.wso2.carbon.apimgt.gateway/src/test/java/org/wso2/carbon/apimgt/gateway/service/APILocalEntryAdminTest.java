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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.service;

import org.apache.axis2.AxisFault;
import org.junit.After;
import org.junit.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Regression coverage for the tenant-access check on {@link APILocalEntryAdmin}'s single proxy
 * chokepoint: every public operation on this service resolves its {@code LocalEntryServiceProxy}
 * through {@code getLocalEntryAdminClient}, which is where the check lives.
 */
public class APILocalEntryAdminTest {

    private static final String ATTACKER_TENANT = "attacker.example";
    private static final String VICTIM_TENANT = "victim.example";

    @After
    public void tearDown() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    private void startAsTenant(String tenantDomain) {
        System.setProperty("carbon.home", APILocalEntryAdminTest.class.getResource("/").getFile());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
    }

    @Test
    public void testNonSuperCallerNamingOwnTenantIsAllowed() throws AxisFault {

        startAsTenant(ATTACKER_TENANT);
        APILocalEntryAdmin.assertTenantAccessAllowed(ATTACKER_TENANT);
    }

    @Test(expected = AxisFault.class)
    public void testNonSuperCallerNamingDifferentTenantIsForbidden() throws AxisFault {

        startAsTenant(ATTACKER_TENANT);
        APILocalEntryAdmin.assertTenantAccessAllowed(VICTIM_TENANT);
    }

    @Test
    public void testSuperAdminNamingDifferentTenantIsAllowed() throws AxisFault {

        startAsTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        APILocalEntryAdmin.assertTenantAccessAllowed(VICTIM_TENANT);
    }

    @Test(expected = AxisFault.class)
    public void testBlankCallerTenantIsRejectedEvenWithBlankTargetTenant() throws AxisFault {

        startAsTenant("");
        APILocalEntryAdmin.assertTenantAccessAllowed("");
    }
}
