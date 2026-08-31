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

package org.wso2.carbon.apimgt.impl.resolver;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtBadRequestException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Regression coverage for the E04-012 fix: {@link OnPremResolver#resolve} must let a super
 * tenant admin act on any tenant via the X-WSO2-Tenant header, but must forbid a non-super
 * tenant admin from naming any tenant other than their own.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(APIUtil.class)
public class OnPremResolverTest {

    private static final String ATTACKER_TENANT = "attacker.com";
    private static final String VICTIM_TENANT = "victim.com";

    private final OnPremResolver resolver = new OnPremResolver();

    @After
    public void tearDown() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    private void startAsTenant(String tenantDomain) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
    }

    private Map<String, Object> propertiesWithHeader(String headerValue) {
        return propertiesWithHeader(OnPremResolver.HEADER_X_WSO2_TENANT, headerValue);
    }

    private Map<String, Object> propertiesWithHeader(String headerName, String headerValue) {
        Map<String, Object> properties = new HashMap<>();
        TreeMap<String, ArrayList<String>> headers = new TreeMap<>();
        if (headerValue != null) {
            ArrayList<String> values = new ArrayList<>();
            values.add(headerValue);
            headers.put(headerName, values);
        }
        properties.put(APIConstants.PROPERTY_HEADERS_KEY, headers);
        return properties;
    }

    private Map<String, Object> propertiesWithNoHeader() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(APIConstants.PROPERTY_HEADERS_KEY, new TreeMap<String, ArrayList<String>>());
        return properties;
    }

    @Test
    public void testNoHeaderReturnsCallerOwnTenant() throws Exception {
        startAsTenant(ATTACKER_TENANT);
        String resolved = resolver.resolve(propertiesWithNoHeader());
        Assert.assertEquals(ATTACKER_TENANT, resolved);
    }

    @Test
    public void testHeaderEqualsCallerOwnTenantIsAllowedForNonSuperCaller() throws Exception {
        startAsTenant(ATTACKER_TENANT);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isTenantAvailable(ATTACKER_TENANT)).thenReturn(true);

        String resolved = resolver.resolve(propertiesWithHeader(ATTACKER_TENANT));
        Assert.assertEquals(ATTACKER_TENANT, resolved);
    }

    @Test
    public void testNonSuperCallerNamingDifferentTenantIsForbidden() throws Exception {
        startAsTenant(ATTACKER_TENANT);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isTenantAvailable(VICTIM_TENANT)).thenReturn(true);

        try {
            resolver.resolve(propertiesWithHeader(VICTIM_TENANT));
            Assert.fail("Expected APIMgtAuthorizationFailedException for a non-super caller naming a different tenant");
        } catch (APIMgtAuthorizationFailedException e) {
            Assert.assertTrue(e.getMessage().contains(ATTACKER_TENANT));
            Assert.assertTrue(e.getMessage().contains(VICTIM_TENANT));
        }
    }

    @Test
    public void testSuperAdminNamingDifferentTenantIsAllowed() throws Exception {
        startAsTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isTenantAvailable(VICTIM_TENANT)).thenReturn(true);

        String resolved = resolver.resolve(propertiesWithHeader(VICTIM_TENANT));
        Assert.assertEquals(VICTIM_TENANT, resolved);
    }

    @Test
    public void testSuperAdminWithNonExistentTenantStillGetsBadRequest() throws Exception {
        startAsTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isTenantAvailable("does-not-exist.com")).thenReturn(false);

        try {
            resolver.resolve(propertiesWithHeader("does-not-exist.com"));
            Assert.fail("Expected APIMgtBadRequestException for a non-existent tenant");
        } catch (APIMgtBadRequestException e) {
            Assert.assertTrue(e.getMessage().contains("does-not-exist.com"));
        }
    }

    @Test
    public void testNonSuperCallerNamingNonExistentTenantIsForbiddenNotBadRequest() throws Exception {
        // Authorization must be checked before tenant existence, so a non-super caller cannot use the
        // response (403 vs 400) to probe which tenant domains exist.
        startAsTenant(ATTACKER_TENANT);
        PowerMockito.mockStatic(APIUtil.class);

        try {
            resolver.resolve(propertiesWithHeader("does-not-exist.com"));
            Assert.fail("Expected APIMgtAuthorizationFailedException, not a tenant-existence check");
        } catch (APIMgtAuthorizationFailedException e) {
            // expected: rejected before APIUtil.isTenantAvailable was ever consulted
        }
    }

    @Test
    public void testOrgAllParamIsForbiddenForNonSuperCaller() throws Exception {
        startAsTenant(ATTACKER_TENANT);
        try {
            resolver.resolve(propertiesWithHeader(APIConstants.ORG_ALL_QUERY_PARAM));
            Assert.fail("Expected APIMgtAuthorizationFailedException for a non-super caller requesting ALL");
        } catch (APIMgtAuthorizationFailedException e) {
            // expected
        }
    }

    @Test
    public void testOrgAllParamIsAllowedForSuperAdmin() throws Exception {
        startAsTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        String resolved = resolver.resolve(propertiesWithHeader(APIConstants.ORG_ALL_QUERY_PARAM));
        Assert.assertEquals(APIConstants.ORG_ALL_QUERY_PARAM, resolved);
    }

    @Test
    public void testTenantHeaderDisabledIgnoresHeaderEvenForMismatch() throws Exception {
        // Mirrors publisher/admin, which set allowTenantHeader=false in beans.xml.
        startAsTenant(ATTACKER_TENANT);
        Map<String, Object> properties = propertiesWithHeader(VICTIM_TENANT);
        properties.put(APIConstants.PROPERTY_ALLOW_TENANT_HEADER_KEY, Boolean.FALSE);

        String resolved = resolver.resolve(properties);
        Assert.assertEquals(ATTACKER_TENANT, resolved);
    }

    @Test
    public void testEmptyHeaderValueFallsBackToCallerOwnTenant() throws Exception {
        startAsTenant(ATTACKER_TENANT);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isTenantAvailable(ATTACKER_TENANT)).thenReturn(true);

        String resolved = resolver.resolve(propertiesWithHeader(""));
        Assert.assertEquals(ATTACKER_TENANT, resolved);
    }

    @Test
    public void testTenantAliasHeaderIsAlsoEnforced() throws Exception {
        startAsTenant(ATTACKER_TENANT);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isTenantAvailable(VICTIM_TENANT)).thenReturn(true);

        try {
            resolver.resolve(propertiesWithHeader(APIConstants.HEADER_TENANT, VICTIM_TENANT));
            Assert.fail("Expected APIMgtAuthorizationFailedException via the xWSO2Tenant alias header too");
        } catch (APIMgtAuthorizationFailedException e) {
            // expected
        }
    }
}
