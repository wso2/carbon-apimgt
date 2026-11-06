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
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.APIMConfigUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
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
        PrivilegedCarbonContext.class, APIUtil.class, CarbonUtils.class, ServerConfiguration.class,
        ServiceReferenceHolder.class})
@SuppressStaticInitializationFor({"org.wso2.carbon.context.PrivilegedCarbonContext",
        "org.wso2.carbon.apimgt.impl.utils.APIUtil"})
public class OAuthJwtAuthenticatorImplTest {

    private static final String SUPER_TENANT = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    private static final String TENANT = "tenant.com";
    private static final String OTHER_TENANT = "other.com";
    private static final String TOKEN = "header.payload.signature";
    private static final String MASKED_TOKEN = "...XXXXsignature";
    private static final String CLIENT_ID = "client-id";
    private static final String SUBJECT_AWARE_USER_RESOLUTION = "RESTAPI.EnableSubjectAwareUserResolution";

    private OAuthJwtAuthenticatorImpl authenticator;
    private Message message;
    private PrivilegedCarbonContext carbonContext;

    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(APIMConfigUtil.class);
        //the constructor reads the configured token issuers
        PowerMockito.when(APIMConfigUtil.getTokenIssuerMap()).thenReturn(new HashMap<>());
        authenticator = Mockito.spy(new OAuthJwtAuthenticatorImpl());
        Mockito.doReturn(false).when(authenticator).isUsernameUsedAsSubject();

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
     * Sets the RESTAPI.EnableSubjectAwareUserResolution value of the API Manager configuration.
     *
     * @param value configured value, null when it is not configured
     */
    private void stubSubjectAwareUserResolution(String value) {

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getFirstProperty(SUBJECT_AWARE_USER_RESOLUTION)).thenReturn(value);
        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
    }

    /**
     * Registers the service provider of {@link #CLIENT_ID} in the given tenant with the given subject identifier
     * setting. No service provider is found for the client in any other tenant.
     *
     * @param clientTenantDomain      tenant domain that owns the client
     * @param isTenantDomainInSubject value of "Use tenant domain in local subject identifier"
     */
    private void stubServiceProvider(String clientTenantDomain, boolean isTenantDomainInSubject) throws Exception {

        LocalAndOutboundAuthenticationConfig localAndOutboundConfig = new LocalAndOutboundAuthenticationConfig();
        localAndOutboundConfig.setUseTenantDomainInLocalSubjectIdentifier(isTenantDomainInSubject);
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundConfig);
        Mockito.doReturn(null).when(authenticator).getServiceProvider(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(serviceProvider).when(authenticator).getServiceProvider(CLIENT_ID, clientTenantDomain);
    }

    /**
     * Builds a signed token info object issued to {@link #CLIENT_ID}.
     *
     * @param subject          value of the sub claim
     * @param appTenantDomain  value of the app_td claim, null to leave it out
     * @param userTenantDomain value of the user_td claim, null to leave it out
     */
    private SignedJWTInfo jwtWithSubject(String subject, String appTenantDomain, String userTenantDomain) {

        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .claim("azp", CLIENT_ID)
                .claim("scope", "apim:admin");
        if (appTenantDomain != null) {
            claims.claim("app_td", appTenantDomain);
        }
        if (userTenantDomain != null) {
            claims.claim("user_td", userTenantDomain);
        }
        return new SignedJWTInfo(TOKEN, null, claims.build());
    }

    /**
     * Authenticates the given token and asserts the username set in the carbon context.
     *
     * @param signedJWTInfo          token to authenticate
     * @param isEmailUserNameEnabled value of the EnableEmailUserName server configuration
     * @param expectedUsername       username expected in the carbon context
     */
    private void assertUsername(SignedJWTInfo signedJWTInfo, boolean isEmailUserNameEnabled,
                                String expectedUsername) throws Exception {

        stubSuccessfulScopeValidation(isEmailUserNameEnabled);
        boolean isAuthenticated = Whitebox.invokeMethod(authenticator, "handleScopeValidation", message,
                signedJWTInfo, TOKEN);
        Assert.assertTrue(isAuthenticated);
        Mockito.verify(carbonContext).setUsername(expectedUsername);
    }

    private void assertUsername(String subject, String appTenantDomain, String userTenantDomain,
                                boolean isEmailUserNameEnabled, String expectedUsername) throws Exception {

        assertUsername(jwtWithSubject(subject, appTenantDomain, userTenantDomain), isEmailUserNameEnabled,
                expectedUsername);
    }

    private void assertServiceProviderIsNotLookedUp() throws Exception {

        Mockito.verify(authenticator, Mockito.never()).getServiceProvider(Mockito.anyString(), Mockito.anyString());
    }

    // ------------------------------------------------------------------
    // The tenant domain is appended to the subject unless subject aware
    // user resolution is enabled. When it is enabled, the tenant domain is
    // not appended again for tokens of the resident identity provider whose
    // subject already ends with it, when the client's service provider uses
    // the tenant domain in the local subject identifier.
    // ------------------------------------------------------------------

    @Test
    public void testTenantDomainIsAppendedByDefault() throws Exception {

        stubSubjectAwareUserResolution(null);
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, false,
                "admin@" + SUPER_TENANT + "@" + SUPER_TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testUnrecognisedSubjectAwareUserResolutionValueAppendsTheTenantDomain() throws Exception {

        stubSubjectAwareUserResolution("yes");
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, false,
                "admin@" + SUPER_TENANT + "@" + SUPER_TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testTenantDomainIsAlwaysAppendedWhenSubjectAwareUserResolutionIsDisabled() throws Exception {

        stubSubjectAwareUserResolution("false");
        stubServiceProvider(TENANT, true);
        assertUsername("admin@" + TENANT, TENANT, TENANT, false, "admin@" + TENANT + "@" + TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testSuperTenantPlainSubjectIsResolvedWithoutServiceProviderLookup() throws Exception {

        stubSubjectAwareUserResolution("true");
        assertUsername("admin", SUPER_TENANT, SUPER_TENANT, false, "admin");
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testTenantPlainSubjectIsResolvedWithoutServiceProviderLookup() throws Exception {

        stubSubjectAwareUserResolution("true");
        assertUsername("admin", TENANT, TENANT, false, "admin@" + TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testSuperTenantSubjectIsNotQualifiedAgainWhenServiceProviderUsesTenantDomain() throws Exception {

        stubSubjectAwareUserResolution("true");
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, false, "admin");
    }

    @Test
    public void testTenantSubjectIsNotQualifiedAgainWhenServiceProviderUsesTenantDomain() throws Exception {

        stubSubjectAwareUserResolution("true");
        stubServiceProvider(TENANT, true);
        assertUsername("admin@" + TENANT, TENANT, TENANT, false, "admin@" + TENANT);
        Mockito.verify(authenticator).getServiceProvider(CLIENT_ID, TENANT);
    }

    @Test
    public void testTokenWithoutTenantClaimsUsesTheSuperTenantServiceProvider() throws Exception {

        //without the app_td and user_td claims, both the user and the client resolve to the super tenant
        stubSubjectAwareUserResolution("true");
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("admin@" + SUPER_TENANT, null, null, false, "admin");
        Mockito.verify(authenticator).getServiceProvider(CLIENT_ID, SUPER_TENANT);
    }

    @Test
    public void testSuperTenantUserNameEndingWithTenantDomainIsQualified() throws Exception {

        //a user named bob@carbon.super whose service provider does not add the tenant domain to the subject
        stubSubjectAwareUserResolution("true");
        stubServiceProvider(SUPER_TENANT, false);
        assertUsername("bob@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, false,
                "bob@" + SUPER_TENANT + "@" + SUPER_TENANT);
    }

    @Test
    public void testTenantUserNameEndingWithTenantDomainIsQualified() throws Exception {

        stubSubjectAwareUserResolution("true");
        stubServiceProvider(TENANT, false);
        assertUsername("bob@" + TENANT, TENANT, TENANT, false, "bob@" + TENANT + "@" + TENANT);
    }

    @Test
    public void testServiceProviderIsReadFromTheTenantThatOwnsTheClient() throws Exception {

        //a tenant user who signs in through a client owned by the super tenant
        stubSubjectAwareUserResolution("true");
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("admin@" + OTHER_TENANT, SUPER_TENANT, OTHER_TENANT, false, "admin@" + OTHER_TENANT);
        Mockito.verify(authenticator).getServiceProvider(CLIENT_ID, SUPER_TENANT);
    }

    @Test
    public void testSuperTenantUserNameEndingWithTenantDomainIsQualifiedWhenUsernameIsTheSubject() throws Exception {

        //the key manager uses the username as the subject, so the service provider setting does not apply
        stubSubjectAwareUserResolution("true");
        Mockito.doReturn(true).when(authenticator).isUsernameUsedAsSubject();
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("bob@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, false,
                "bob@" + SUPER_TENANT + "@" + SUPER_TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testTenantUserNameEndingWithTenantDomainIsQualifiedWhenUsernameIsTheSubject() throws Exception {

        stubSubjectAwareUserResolution("true");
        Mockito.doReturn(true).when(authenticator).isUsernameUsedAsSubject();
        stubServiceProvider(TENANT, true);
        assertUsername("bob@" + TENANT, TENANT, TENANT, false, "bob@" + TENANT + "@" + TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testPlainSubjectIsQualifiedWhenUsernameIsTheSubject() throws Exception {

        stubSubjectAwareUserResolution("true");
        Mockito.doReturn(true).when(authenticator).isUsernameUsedAsSubject();
        assertUsername("bob", TENANT, TENANT, false, "bob@" + TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testServiceProviderOfAnotherTenantIsNotUsed() throws Exception {

        //the client's service provider is only registered in the super tenant, but the client belongs to a tenant
        stubSubjectAwareUserResolution("true");
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("admin@" + TENANT, TENANT, TENANT, false, "admin@" + TENANT + "@" + TENANT);
    }

    @Test
    public void testSubjectQualifiedWithAnotherTenantIsQualifiedWithTheUserTenant() throws Exception {

        stubSubjectAwareUserResolution("true");
        assertUsername("admin@" + OTHER_TENANT, TENANT, TENANT, false, "admin@" + OTHER_TENANT + "@" + TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testServiceProviderLookupErrorAppendsTheTenantDomain() throws Exception {

        stubSubjectAwareUserResolution("true");
        Mockito.doThrow(new IdentityOAuth2Exception("lookup failed")).when(authenticator)
                .getServiceProvider(CLIENT_ID, SUPER_TENANT);
        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, false,
                "admin@" + SUPER_TENANT + "@" + SUPER_TENANT);
        Mockito.verify(authenticator).getServiceProvider(CLIENT_ID, SUPER_TENANT);
    }

    @Test
    public void testMissingServiceProviderAppendsTheTenantDomain() throws Exception {

        stubSubjectAwareUserResolution("true");
        Mockito.doReturn(null).when(authenticator).getServiceProvider(CLIENT_ID, SUPER_TENANT);
        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, false,
                "admin@" + SUPER_TENANT + "@" + SUPER_TENANT);
        Mockito.verify(authenticator).getServiceProvider(CLIENT_ID, SUPER_TENANT);
    }

    @Test
    public void testServiceProviderWithoutSubjectSettingsAppendsTheTenantDomain() throws Exception {

        stubSubjectAwareUserResolution("true");
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(null);
        Mockito.doReturn(serviceProvider).when(authenticator).getServiceProvider(CLIENT_ID, SUPER_TENANT);
        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, false,
                "admin@" + SUPER_TENANT + "@" + SUPER_TENANT);
        Mockito.verify(authenticator).getServiceProvider(CLIENT_ID, SUPER_TENANT);
    }

    @Test
    public void testTokenWithoutClientIdAppendsTheTenantDomain() throws Exception {

        stubSubjectAwareUserResolution("true");
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("admin@" + SUPER_TENANT)
                .claim("user_td", SUPER_TENANT)
                .claim("scope", "apim:admin")
                .build();
        assertUsername(new SignedJWTInfo(TOKEN, null, claims), false, "admin@" + SUPER_TENANT + "@" + SUPER_TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testConfiguredTokenIssuersAppendTheTenantDomain() throws Exception {

        HashMap<String, TokenIssuerDto> tokenIssuers = new HashMap<>();
        tokenIssuers.put("https://idp.example.com/token", new TokenIssuerDto("https://idp.example.com/token"));
        PowerMockito.when(APIMConfigUtil.getTokenIssuerMap()).thenReturn(tokenIssuers);
        authenticator = Mockito.spy(new OAuthJwtAuthenticatorImpl());
        stubSubjectAwareUserResolution("true");
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, false,
                "admin@" + SUPER_TENANT + "@" + SUPER_TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testSuperTenantEmailSubject() throws Exception {

        stubSubjectAwareUserResolution("true");
        assertUsername("bob@gmail.com", SUPER_TENANT, SUPER_TENANT, true, "bob@gmail.com");
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testSuperTenantEmailSubjectIsNotQualifiedAgain() throws Exception {

        stubSubjectAwareUserResolution("true");
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("bob@gmail.com@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, true, "bob@gmail.com");
    }

    @Test
    public void testSuperTenantPlainSubjectWithTenantDomainWhenEmailUserNameIsEnabled() throws Exception {

        stubSubjectAwareUserResolution("true");
        stubServiceProvider(SUPER_TENANT, true);
        assertUsername("admin@" + SUPER_TENANT, SUPER_TENANT, SUPER_TENANT, true, "admin");
    }

    @Test
    public void testTenantPlainSubjectWithTenantDomainWhenEmailUserNameIsEnabled() throws Exception {

        stubSubjectAwareUserResolution("true");
        stubServiceProvider(TENANT, true);
        assertUsername("admin@" + TENANT, TENANT, TENANT, true, "admin@" + TENANT);
    }

    @Test
    public void testTenantEmailSubject() throws Exception {

        stubSubjectAwareUserResolution("true");
        assertUsername("carol@gmail.com", TENANT, TENANT, true, "carol@gmail.com@" + TENANT);
        assertServiceProviderIsNotLookedUp();
    }

    @Test
    public void testTenantEmailSubjectIsNotQualifiedAgain() throws Exception {

        stubSubjectAwareUserResolution("true");
        stubServiceProvider(TENANT, true);
        assertUsername("carol@gmail.com@" + TENANT, TENANT, TENANT, true, "carol@gmail.com@" + TENANT);
    }

    @Test
    public void testTenantEmailSubjectInTheTenantDomainIsQualified() throws Exception {

        //an email username in the tenant's own domain, whose service provider does not add the tenant domain
        stubSubjectAwareUserResolution("true");
        stubServiceProvider(TENANT, false);
        assertUsername("bob@" + TENANT, TENANT, TENANT, true, "bob@" + TENANT + "@" + TENANT);
    }

    @Test
    public void testTenantEmailSubjectInTheTenantDomainIsNotQualifiedAgain() throws Exception {

        stubSubjectAwareUserResolution("true");
        stubServiceProvider(TENANT, true);
        assertUsername("bob@" + TENANT + "@" + TENANT, TENANT, TENANT, true, "bob@" + TENANT + "@" + TENANT);
    }
}
