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

package org.wso2.carbon.apimgt.multitenant.auth.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.oauth2.util.JWTSignatureValidationUtils;

import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for {@link OIDCTokenValidationUtil}.
 */
@Listeners(MockitoTestNGListener.class)
public class OIDCTokenValidationUtilTest {

    private static final String TEST_ISSUER = "https://localhost:9443/oauth2/token";

    @Mock
    private IdentityProvider mockIdentityProvider;

    @Mock
    private SignedJWT mockSignedJWT;

    @Test
    public void testGetIssuer() throws Exception {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(TEST_ISSUER)
                .build();

        String issuer = OIDCTokenValidationUtil.getIssuer(claimsSet);
        Assert.assertEquals(issuer, TEST_ISSUER);
    }

    @Test
    public void testGetIssuerNull() throws Exception {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().build();

        String issuer = OIDCTokenValidationUtil.getIssuer(claimsSet);
        Assert.assertNull(issuer);
    }

    @Test
    public void testPassValidateSignature() throws Exception {

        try (MockedStatic<JWTSignatureValidationUtils> sigMock =
                     mockStatic(JWTSignatureValidationUtils.class)) {

            sigMock.when(() -> JWTSignatureValidationUtils.validateSignature(mockSignedJWT, mockIdentityProvider))
                    .thenReturn(true);

            OIDCTokenValidationUtil.validateSignature(mockSignedJWT, mockIdentityProvider);
        }
    }

    @Test(expectedExceptions = AuthenticationFailedException.class)
    public void testFailValidateSignature() throws Exception {

        try (MockedStatic<JWTSignatureValidationUtils> sigMock =
                     mockStatic(JWTSignatureValidationUtils.class)) {

            sigMock.when(() -> JWTSignatureValidationUtils.validateSignature(mockSignedJWT, mockIdentityProvider))
                    .thenReturn(false);

            OIDCTokenValidationUtil.validateSignature(mockSignedJWT, mockIdentityProvider);
        }
    }

    @Test
    public void testPassValidateIssuerClaim() throws AuthenticationFailedException {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(TEST_ISSUER)
                .build();

        OIDCTokenValidationUtil.validateIssuerClaim(claimsSet);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class)
    public void testFailValidateIssuerClaimBlank() throws AuthenticationFailedException {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("")
                .build();

        OIDCTokenValidationUtil.validateIssuerClaim(claimsSet);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class)
    public void testFailValidateIssuerClaimNull() throws AuthenticationFailedException {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().build();

        OIDCTokenValidationUtil.validateIssuerClaim(claimsSet);
    }
}
