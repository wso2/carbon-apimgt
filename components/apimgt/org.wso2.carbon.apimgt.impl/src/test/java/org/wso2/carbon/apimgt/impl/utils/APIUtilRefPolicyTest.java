/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, ServiceReferenceHolder.class})
public class APIUtilRefPolicyTest {

    /**
     * When a policy is active, {@code populateRefResolutionPolicy} reads the configured OAS import file-size limit
     * from the API Manager configuration to cap the $ref crawl's per-document fetch. Stub that config chain so the
     * unit tests can exercise the wiring without standing up the OSGi configuration service.
     */
    @Before
    public void stubFileSizeConfig() {
        APIManagerConfiguration config = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(config.getFirstProperty(Mockito.anyString())).thenReturn("10");
        APIManagerConfigurationService svc = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(svc.getAPIManagerConfiguration()).thenReturn(config);
        ServiceReferenceHolder holder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(holder.getAPIManagerConfigurationService()).thenReturn(svc);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(holder);
    }

    /**
     * Drives {@link APIUtil#populateRefResolutionPolicy(OASParserOptions, String)} with the platform-level
     * network-security policy toggled on/off and the tenant config stubbed out (null). Returns the populated
     * options so callers can assert the new ref-validation wiring.
     */
    private OASParserOptions run(boolean platformEnabled) throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", org.mockito.ArgumentMatchers.anyString());
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", platformEnabled);
        OASParserOptions o = new OASParserOptions();
        APIUtil.populateRefResolutionPolicy(o, "carbon.super");
        return o;
    }

    @Test public void testPlatformPolicyActiveSetsValidator() throws Exception {
        OASParserOptions o = run(true);
        Assert.assertNotNull("A platform policy must wire a ref validator", o.getRefValidator());
        // fetch hook needed for nested/transitive refs
        Assert.assertNotNull("A platform policy must wire the crawl HTTP client provider", o.getHttpClientProvider());
        Assert.assertEquals("carbon.super", o.getRefValidationTenantDomain());
    }

    @Test public void testNoPolicyLeavesValidatorNull() throws Exception {
        OASParserOptions o = run(false);
        Assert.assertNull("With no active policy no ref validator must be set", o.getRefValidator());
        Assert.assertNull("With no active policy no crawl HTTP client provider must be set",
                o.getHttpClientProvider());
        Assert.assertEquals("carbon.super", o.getRefValidationTenantDomain());
    }

    @Test public void testTenantPolicyActiveSetsValidator() throws Exception {
        PowerMockito.spy(APIUtil.class);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", false);
        org.json.simple.JSONObject tenant = new org.json.simple.JSONObject();
        org.json.simple.JSONObject ac = new org.json.simple.JSONObject();
        org.json.simple.JSONArray hosts = new org.json.simple.JSONArray();
        hosts.add("good.com");
        ac.put("Hosts", hosts);
        tenant.put("NetworkSecurityAccessControl", ac);
        PowerMockito.doReturn(tenant).when(APIUtil.class, "getTenantConfig", org.mockito.ArgumentMatchers.anyString());
        OASParserOptions o = new OASParserOptions();
        APIUtil.populateRefResolutionPolicy(o, "carbon.super");
        Assert.assertNotNull("An active tenant policy must wire a ref validator", o.getRefValidator());
        Assert.assertEquals("carbon.super", o.getRefValidationTenantDomain());
    }

    @Test public void testPlatformAndTenantBothActiveSetsValidator() throws Exception {
        PowerMockito.spy(APIUtil.class);
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        org.json.simple.JSONObject tenant = new org.json.simple.JSONObject();
        org.json.simple.JSONObject ac = new org.json.simple.JSONObject();
        ac.put("Mode", "allow");
        org.json.simple.JSONArray hosts = new org.json.simple.JSONArray();
        hosts.add("x.com");
        ac.put("Hosts", hosts);
        tenant.put("NetworkSecurityAccessControl", ac);
        PowerMockito.doReturn(tenant).when(APIUtil.class, "getTenantConfig", org.mockito.ArgumentMatchers.anyString());
        OASParserOptions o = new OASParserOptions();
        APIUtil.populateRefResolutionPolicy(o, "tenant.com");
        Assert.assertNotNull(o.getRefValidator());
        Assert.assertEquals("tenant.com", o.getRefValidationTenantDomain());
    }

    @Test public void testBuildSetsHook() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", org.mockito.ArgumentMatchers.anyString());
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", true);
        OASParserOptions o = APIUtil.buildRefAwareOASParserOptions(new OASParserOptions(), "carbon.super");
        Assert.assertNotNull(o.getRefValidator());
        Assert.assertNotNull("buildRefAwareOASParserOptions must wire the crawl HTTP client provider when active",
                o.getHttpClientProvider());
        Assert.assertEquals("carbon.super", o.getRefValidationTenantDomain());
    }

    @Test public void testBuildWithNoPolicyLeavesValidatorNull() throws Exception {
        PowerMockito.spy(APIUtil.class);
        PowerMockito.doReturn(null).when(APIUtil.class, "getTenantConfig", org.mockito.ArgumentMatchers.anyString());
        Whitebox.setInternalState(APIUtil.class, "networkSecurityEnabled", false);
        OASParserOptions o = APIUtil.buildRefAwareOASParserOptions(new OASParserOptions(), "carbon.super");
        Assert.assertNull(o.getRefValidator());
        Assert.assertNull(o.getHttpClientProvider());
        Assert.assertEquals("carbon.super", o.getRefValidationTenantDomain());
    }
}
