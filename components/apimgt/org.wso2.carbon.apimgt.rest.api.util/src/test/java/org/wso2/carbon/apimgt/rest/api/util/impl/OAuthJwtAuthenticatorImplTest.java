/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.OAuthTokenInfo;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.APIMConfigUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;
import java.util.HashMap;

/**
 * Tests the end user name that {@link OAuthJwtAuthenticatorImpl} sets in the carbon context when it
 * authenticates a JWT bearer token presented to the REST APIs.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIMConfigUtil.class, RestApiUtil.class, RestApiCommonUtil.class,
        PrivilegedCarbonContext.class, APIUtil.class, CarbonUtils.class, ServerConfiguration.class})
@SuppressStaticInitializationFor({"org.wso2.carbon.context.PrivilegedCarbonContext",
        "org.wso2.carbon.apimgt.impl.utils.APIUtil"})
public class OAuthJwtAuthenticatorImplTest {

    private static final String SUPER_TENANT = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    private static final String TENANT = "tenant.com";
    private static final String OTHER_TENANT = "other.com";
    private static final String TOKEN = "header.payload.signature";
    private static final String MASKED_TOKEN = "...XXXXsignature";

    private OAuthJwtAuthenticatorImpl authenticator;
    private Message message;
    private PrivilegedCarbonContext carbonContext;

    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(APIMConfigUtil.class);
        //the constructor reads the configured token issuers
        PowerMockito.when(APIMConfigUtil.getTokenIssuerMap()).thenReturn(new HashMap<>());
        authenticator = new OAuthJwtAuthenticatorImpl();

        message = Mockito.mock(Message.class);
        Mockito.when(message.get(RestApiConstants.MASKED_TOKEN)).thenReturn(MASKED_TOKEN);
    }

    /**
     * Wires up everything {@code handleScopeValidation} touches, so that a successful call can be asserted on.
     *
     * @param isEmailUserNameEnabled value of the EnableEmailUserName server configuration
     */
    private void stubSuccessfulScopeValidation(boolean isEmailUserNameEnabled) throws Exception {

        Mockito.when(message.get(RestApiConstants.BASE_PATH)).thenReturn("/api/am/admin/");
        Mockito.when(message.get(RestApiConstants.API_VERSION)).thenReturn("v4");
        Mockito.when(message.getExchange()).thenReturn(Mockito.mock(Exchange.class));

        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.resolveOrganization(message)).thenReturn("org");
        PowerMockito.when(RestApiUtil.addToJWTAuthenticationContext(message)).thenReturn(new HashMap<>());

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getURITemplatesForBasePath(Mockito.anyString()))
                .thenReturn(Collections.emptySet());
        PowerMockito.when(RestApiCommonUtil.validateScopes(Mockito.anyMap(), Mockito.any(OAuthTokenInfo.class)))
                .thenReturn(true);

        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        Mockito.when(serverConfiguration.getFirstProperty("EnableEmailUserName"))
                .thenReturn(String.valueOf(isEmailUserNameEnabled));
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        //MultitenantUtils reads the email username configuration from the server configuration instance
        PowerMockito.mockStatic(ServerConfiguration.class);
        PowerMockito.when(ServerConfiguration.getInstance()).thenReturn(serverConfiguration);

        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(tenantManager.getTenantId(Mockito.anyString())).thenReturn(1);
        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);

        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        carbonContext = PowerMockito.mock(PrivilegedCarbonContext.class);
        Mockito.when(carbonContext.getOSGiService(RealmService.class, null)).thenReturn(realmService);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);

        PowerMockito.mockStatic(APIUtil.class);
    }

    /**
     * Builds a signed token info object carrying the given subject and user tenant domain.
     *
     * @param subject          value of the sub claim
     * @param userTenantDomain value of the user_td claim, null to leave it out
     */
    private SignedJWTInfo jwtWithSubject(String subject, String userTenantDomain) {

        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .claim("scope", "apim:admin");
        if (userTenantDomain != null) {
            claims.claim("user_td", userTenantDomain);
        }
        return new SignedJWTInfo(TOKEN, null, claims.build());
    }

    /**
     * Authenticates a token with the given subject and asserts the username set in the carbon context.
     *
     * @param subject                value of the sub claim
     * @param userTenantDomain       value of the user_td claim, null to leave it out
     * @param isEmailUserNameEnabled value of the EnableEmailUserName server configuration
     * @param expectedUsername       username expected in the carbon context
     */
    private void assertUsername(String subject, String userTenantDomain, boolean isEmailUserNameEnabled,
                                String expectedUsername) throws Exception {

        stubSuccessfulScopeValidation(isEmailUserNameEnabled);
        boolean isAuthenticated = Whitebox.invokeMethod(authenticator, "handleScopeValidation", message,
                jwtWithSubject(subject, userTenantDomain), TOKEN);
        Assert.assertTrue(isAuthenticated);
        Mockito.verify(carbonContext).setUsername(expectedUsername);
    }

    // ------------------------------------------------------------------
    // The tenant domain is appended to the subject unless the subject is
    // already qualified with it, which is the case when the service provider
    // uses the tenant domain in the local subject identifier.
    // ------------------------------------------------------------------

    @Test
    public void testSuperTenantPlainSubject() throws Exception {

        assertUsername("admin", null, false, "admin");
    }

    @Test
    public void testSuperTenantSubjectWithTenantDomainIsNotQualifiedAgain() throws Exception {

        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, false, "admin");
    }

    @Test
    public void testTenantPlainSubject() throws Exception {

        assertUsername("admin", TENANT, false, "admin@" + TENANT);
    }

    @Test
    public void testTenantSubjectWithTenantDomainIsNotQualifiedAgain() throws Exception {

        assertUsername("admin@" + TENANT, TENANT, false, "admin@" + TENANT);
    }

    @Test
    public void testSubjectQualifiedWithAnotherTenantIsQualifiedWithTheUserTenant() throws Exception {

        assertUsername("admin@" + OTHER_TENANT, TENANT, false, "admin@" + OTHER_TENANT + "@" + TENANT);
    }

    @Test
    public void testSuperTenantEmailSubject() throws Exception {

        assertUsername("bob@gmail.com", SUPER_TENANT, true, "bob@gmail.com");
    }

    @Test
    public void testSuperTenantEmailSubjectWithTenantDomainIsNotQualifiedAgain() throws Exception {

        assertUsername("bob@gmail.com@" + SUPER_TENANT, SUPER_TENANT, true, "bob@gmail.com");
    }

    @Test
    public void testSuperTenantPlainSubjectWithTenantDomainWhenEmailUserNameIsEnabled() throws Exception {

        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, true, "admin");
    }

    @Test
    public void testTenantEmailSubject() throws Exception {

        assertUsername("carol@gmail.com", TENANT, true, "carol@gmail.com@" + TENANT);
    }

    @Test
    public void testTenantEmailSubjectWithTenantDomainIsNotQualifiedAgain() throws Exception {

        assertUsername("carol@gmail.com@" + TENANT, TENANT, true, "carol@gmail.com@" + TENANT);
    }

    @Test
    public void testTenantEmailSubjectInTheTenantDomainIsQualified() throws Exception {

        //an email username in the tenant's own domain ends with the tenant domain, but has a single '@'
        assertUsername("bob@" + TENANT, TENANT, true, "bob@" + TENANT + "@" + TENANT);
    }

    @Test
    public void testTenantEmailSubjectInTheTenantDomainWithTenantDomainIsNotQualifiedAgain() throws Exception {

        assertUsername("bob@" + TENANT + "@" + TENANT, TENANT, true, "bob@" + TENANT + "@" + TENANT);
    }
}
