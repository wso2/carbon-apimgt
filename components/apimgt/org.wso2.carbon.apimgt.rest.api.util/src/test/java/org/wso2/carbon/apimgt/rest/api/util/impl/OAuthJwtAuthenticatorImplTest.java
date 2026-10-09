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

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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
import org.wso2.carbon.apimgt.impl.dto.TokenValidationDto;
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

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;

/**
 * Tests the organization resolution and token class checks performed by
 * {@link OAuthJwtAuthenticatorImpl} when it authenticates a JWT bearer token
 * presented to the management (System) REST APIs.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIMConfigUtil.class, RestApiUtil.class, RestApiCommonUtil.class,
        PrivilegedCarbonContext.class, APIUtil.class, CarbonUtils.class})
@SuppressStaticInitializationFor("org.wso2.carbon.context.PrivilegedCarbonContext")
public class OAuthJwtAuthenticatorImplTest {

    private static final String SUPER_TENANT = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    private static final String SIGNING_TENANT = "signer.com";
    private static final String OTHER_TENANT = "other.com";
    private static final String TOKEN = "header.payload.signature";
    private static final String MASKED_TOKEN = "...XXXXsignature";

    private OAuthJwtAuthenticatorImpl authenticator;
    private Message message;
    private PrivilegedCarbonContext carbonContext;
    private TokenValidationDto tokenValidationDto;

    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(APIMConfigUtil.class);
        //the constructor reads the configured token issuers
        PowerMockito.when(APIMConfigUtil.getTokenIssuerMap()).thenReturn(new HashMap<>());
        tokenValidationDto = new TokenValidationDto();
        PowerMockito.when(APIMConfigUtil.getTokenValidationDto()).thenReturn(tokenValidationDto);

        authenticator = new OAuthJwtAuthenticatorImpl();

        message = Mockito.mock(Message.class);
        Mockito.when(message.get(RestApiConstants.MASKED_TOKEN)).thenReturn(MASKED_TOKEN);
    }

    /**
     * Wires up everything {@code handleScopeValidation} touches once it gets past the
     * organization check, so that a successful call can be asserted on.
     */
    private void stubSuccessfulScopeValidation() throws Exception {

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
        Mockito.when(serverConfiguration.getFirstProperty("EnableEmailUserName")).thenReturn("false");
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);

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
     * Builds a signed token info object carrying the given organization claims.
     *
     * @param appTenantDomain  value of the app_td claim, null to leave it out
     * @param userTenantDomain value of the user_td claim, null to leave it out
     */
    private SignedJWTInfo jwtWithOrganizationClaims(String appTenantDomain, Object userTenantDomain) {

        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .subject("admin")
                .claim("scope", "apim:admin");
        if (appTenantDomain != null) {
            claims.claim("app_td", appTenantDomain);
        }
        if (userTenantDomain != null) {
            claims.claim("user_td", userTenantDomain);
        }
        return new SignedJWTInfo(TOKEN, null, claims.build());
    }

    private SignedJWTInfo jwtWithTypeHeader(JOSEObjectType type) {

        JWSHeader.Builder header = new JWSHeader.Builder(JWSAlgorithm.RS256);
        if (type != null) {
            header.type(type);
        }
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("admin").build();
        return new SignedJWTInfo(TOKEN, new SignedJWT(header.build(), claims), claims);
    }

    private boolean handleScopeValidation(SignedJWTInfo signedJWTInfo) throws Exception {

        return Whitebox.invokeMethod(authenticator, "handleScopeValidation", message, signedJWTInfo, TOKEN);
    }

    private boolean isAccessToken(SignedJWTInfo signedJWTInfo) throws Exception {

        return Whitebox.invokeMethod(authenticator, "isAccessToken", signedJWTInfo, MASKED_TOKEN);
    }

    // ------------------------------------------------------------------
    // Signer authority: an organization's key may only speak for its own
    // users, and only the super tenant key may name another organization.
    // ------------------------------------------------------------------

    @Test
    public void testTokenSignedByTenantNamingAnotherTenantIsRejected() throws Exception {

        Assert.assertFalse("A token signed by one organization must not be honoured as authority over another",
                handleScopeValidation(jwtWithOrganizationClaims(SIGNING_TENANT, OTHER_TENANT)));
        Mockito.verify(message, Mockito.never())
                .put(Mockito.eq(RestApiConstants.SUB_ORGANIZATION), Mockito.any());
    }

    @Test
    public void testTokenSignedByTenantNamingSuperTenantIsRejected() throws Exception {

        Assert.assertFalse("A tenant signed token must not be able to claim the super tenant",
                handleScopeValidation(jwtWithOrganizationClaims(SIGNING_TENANT, SUPER_TENANT)));
    }

    @Test
    public void testTokenSignedBySuperTenantMayNameAnotherTenant() throws Exception {

        stubSuccessfulScopeValidation();
        Assert.assertTrue("Super tenant SaaS applications must keep working across organizations",
                handleScopeValidation(jwtWithOrganizationClaims(SUPER_TENANT, OTHER_TENANT)));
        Mockito.verify(carbonContext).setTenantDomain(OTHER_TENANT);
        Mockito.verify(message).put(RestApiConstants.SUB_ORGANIZATION, OTHER_TENANT);
    }

    @Test
    public void testTokenSignedByTenantForItsOwnUserIsAccepted() throws Exception {

        stubSuccessfulScopeValidation();
        Assert.assertTrue(handleScopeValidation(jwtWithOrganizationClaims(SIGNING_TENANT, SIGNING_TENANT)));
        Mockito.verify(carbonContext).setTenantDomain(SIGNING_TENANT);
        Mockito.verify(message).put(RestApiConstants.SUB_ORGANIZATION, SIGNING_TENANT);
    }

    @Test
    public void testMissingUserDomainFallsBackToTheSigningOrganization() throws Exception {

        stubSuccessfulScopeValidation();
        Assert.assertTrue(handleScopeValidation(jwtWithOrganizationClaims(SIGNING_TENANT, null)));
        Mockito.verify(carbonContext).setTenantDomain(SIGNING_TENANT);
        Mockito.verify(carbonContext, Mockito.never()).setTenantDomain(SUPER_TENANT);
    }

    @Test
    public void testEmptyUserDomainFallsBackToTheSigningOrganization() throws Exception {

        stubSuccessfulScopeValidation();
        Assert.assertTrue(handleScopeValidation(jwtWithOrganizationClaims(SIGNING_TENANT, "")));
        Mockito.verify(carbonContext).setTenantDomain(SIGNING_TENANT);
    }

    @Test
    public void testMissingOrganizationClaimsResolveToTheSuperTenant() throws Exception {

        stubSuccessfulScopeValidation();
        Assert.assertTrue("Tokens without organization claims must keep their single tenant behaviour",
                handleScopeValidation(jwtWithOrganizationClaims(null, null)));
        Mockito.verify(carbonContext).setTenantDomain(SUPER_TENANT);
    }

    @Test
    public void testMissingAppDomainMeansTheSuperTenantSignedTheToken() throws Exception {

        stubSuccessfulScopeValidation();
        Assert.assertTrue(handleScopeValidation(jwtWithOrganizationClaims(null, OTHER_TENANT)));
        Mockito.verify(carbonContext).setTenantDomain(OTHER_TENANT);
    }

    @Test
    public void testUserDomainComparisonIsCaseSensitive() throws Exception {

        Assert.assertFalse("A user_td differing in case names a different organization",
                handleScopeValidation(jwtWithOrganizationClaims(SIGNING_TENANT,
                        SIGNING_TENANT.toUpperCase())));
    }

    @Test(expected = ParseException.class)
    public void testNonStringUserDomainIsNotAccepted() throws Exception {

        //a claim of the wrong type must not be cast blindly
        handleScopeValidation(jwtWithOrganizationClaims(SIGNING_TENANT, 42));
    }

    // ------------------------------------------------------------------
    // Token class: only RFC 9068 access tokens carry the at+jwt type
    // header, and the check is configuration gated.
    // ------------------------------------------------------------------

    @Test
    public void testTypeHeaderIsNotCheckedWhenValidationIsDisabled() throws Exception {

        tokenValidationDto.setEnforceTypeHeaderValidation(false);
        Assert.assertTrue("Type header validation is disabled by default for backward compatibility",
                isAccessToken(jwtWithTypeHeader(null)));
        Assert.assertTrue(isAccessToken(jwtWithTypeHeader(new JOSEObjectType("at+jwt"))));
    }

    @Test
    public void testAccessTokenIsAcceptedWhenValidationIsEnforced() throws Exception {

        tokenValidationDto.setEnforceTypeHeaderValidation(true);
        Assert.assertTrue(isAccessToken(jwtWithTypeHeader(new JOSEObjectType("at+jwt"))));
    }

    @Test
    public void testTokenWithoutTypeHeaderIsRejectedWhenValidationIsEnforced() throws Exception {

        tokenValidationDto.setEnforceTypeHeaderValidation(true);
        Assert.assertFalse("An id token carries no type header and is not an access token",
                isAccessToken(jwtWithTypeHeader(null)));
    }

    @Test
    public void testIdTokenTypeHeaderIsRejectedWhenValidationIsEnforced() throws Exception {

        tokenValidationDto.setEnforceTypeHeaderValidation(true);
        Assert.assertFalse(isAccessToken(jwtWithTypeHeader(JOSEObjectType.JWT)));
    }

    @Test
    public void testTypeHeaderComparisonIsCaseSensitive() throws Exception {

        tokenValidationDto.setEnforceTypeHeaderValidation(true);
        Assert.assertFalse(isAccessToken(jwtWithTypeHeader(new JOSEObjectType("AT+JWT"))));
    }
}
