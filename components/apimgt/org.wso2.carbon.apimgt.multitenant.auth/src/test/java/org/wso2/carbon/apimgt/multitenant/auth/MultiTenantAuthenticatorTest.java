/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.multitenant.auth;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.wso2.carbon.identity.application.common.model.Property;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.when;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.AUTHENTICATOR_NAME;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.COMMON_SP_NAME;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.TENANT_IDENTIFIER;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.TENANT_SELECTION_URL_PROP;

/**
 * Unit tests for {@link MultiTenantAuthenticator}.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class MultiTenantAuthenticatorTest {

    private MultiTenantAuthenticator authenticator;

    @Mock
    private HttpServletRequest mockRequest;

    @Before
    public void setUp() {

        authenticator = new MultiTenantAuthenticator();
    }

    @Test
    public void testGetFriendlyName() {

        Assert.assertEquals(AUTHENTICATOR_FRIENDLY_NAME, authenticator.getFriendlyName());
    }

    @Test
    public void testGetName() {

        Assert.assertEquals(AUTHENTICATOR_NAME, authenticator.getName());
    }

    @Test
    public void testGetConfigurationProperties() {

        List<Property> properties = authenticator.getConfigurationProperties();
        Assert.assertNotNull(properties);
        Assert.assertFalse(properties.isEmpty());

        boolean hasCommonSpName = false;
        boolean hasTenantSelectionUrl = false;
        for (Property prop : properties) {
            if (COMMON_SP_NAME.equals(prop.getName())) {
                hasCommonSpName = true;
            } else if (TENANT_SELECTION_URL_PROP.equals(prop.getName())) {
                hasTenantSelectionUrl = true;
            }
        }
        Assert.assertTrue("CommonSPName property should be present", hasCommonSpName);
        Assert.assertTrue("TenantSelectionPageUrl property should be present", hasTenantSelectionUrl);
    }

    @Test
    public void testCanHandleWithTenantIdentifier() {

        when(mockRequest.getParameter(TENANT_IDENTIFIER)).thenReturn("abc.com");
        Assert.assertTrue(authenticator.canHandle(mockRequest));
    }

    @Test
    public void testCanHandleWithoutTenantIdentifier() {

        Assert.assertFalse(authenticator.canHandle(mockRequest));
    }

    @Test
    public void testGetScopeBlank() {

        Assert.assertEquals("openid groups", authenticator.getScope("", new HashMap<>()));
    }

    @Test
    public void testGetScopeNonBlank() {

        Assert.assertEquals("openid email", authenticator.getScope("openid email", new HashMap<>()));
    }
}
