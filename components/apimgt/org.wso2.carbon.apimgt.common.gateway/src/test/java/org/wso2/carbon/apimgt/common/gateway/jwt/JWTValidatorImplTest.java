/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.common.gateway.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.common.gateway.bootstrap.Bootstrap;
import org.wso2.carbon.apimgt.common.gateway.dto.JWKSConfigurationDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.common.gateway.exception.CommonGatewayException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bootstrap.class)
public class JWTValidatorImplTest {
    private static final String keyId = "keyId";
    private final String jwksURL = "https://localhost:9443/oauth2/jwks";
    private static final String CERT_HASH =  "HXKfuMRo6tggoqC-StuPur7ZqxuhJsnSFGbFcG6OTTA";

    private static JWSHeader jwsHeader;
    private static CloseableHttpClient httpClient;

    private static KeyStore trustStore;

    @BeforeClass
    public static void setup() {
        jwsHeader = new JWSHeader(JWSAlgorithm.RS256);
        jwsHeader = new JWSHeader.Builder(jwsHeader).keyID(keyId).build();

        httpClient = PowerMockito.mock(CloseableHttpClient.class);
        // Bootstrap class needs to be mocked as it is where the HttpClient is configured.
        Bootstrap bootstrap = Mockito.mock(Bootstrap.class);
        Mockito.when(bootstrap.getHttpClient()).thenReturn(httpClient);
        PowerMockito.mockStatic(Bootstrap.class);
        BDDMockito.given(Bootstrap.getInstance()).willReturn(bootstrap);

        String trustStorePath = Objects.requireNonNull(JWTValidatorImplTest.class.getClassLoader()
                .getResource("security/client-truststore.jks")).getPath();

        try (InputStream trustStoreContent = Files.newInputStream(Paths.get(trustStorePath))) {
            trustStore = KeyStore.getInstance("JKS");
            trustStore.load(trustStoreContent, "wso2carbon".toCharArray());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void validateTokenWithJWKSTestSuccess() {
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo();

        SignedJWT signedJWT = Mockito.mock(SignedJWT.class);
        Mockito.when(signedJWT.getHeader()).thenReturn(jwsHeader);
        signedJWTInfo.setSignedJWT(signedJWT);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, 1);

        JSONObject transportCertHash = new JSONObject();
        transportCertHash.put("x5t#S256", CERT_HASH);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .expirationTime(now.getTime())
                .claim("cnf", transportCertHash)
                .claim("consumerKey", "testConsumerKey")
                .claim("scope", "testScope")
                .claim("customClaim", "testCustomClaim")
                .claim("aut", "application")
                .claim("jti", "testJTI")
                .build();
        signedJWTInfo.setJwtClaimsSet(jwtClaimsSet);
        signedJWTInfo.setToken("tokenPayload");

        // Mocks the HTTP Client such that we can test various behaviors related JWKS endpoint calls
        CloseableHttpResponse response = PowerMockito.mock(CloseableHttpResponse.class);
        StatusLine statusLine = PowerMockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
        HttpEntity httpEntity = PowerMockito.mock(HttpEntity.class);
        try {
            // Dummy payload. JWKS response's n value is not considered for validation.
            Mockito.when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(("" +
                    "{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\"," +
                    "\"use\":\"sig\",\"kid\":\"keyId\",\"alg\":\"RS256\",\"n\":\"kdgncoCrz655Lq8pTdX07" +
                    "eoVBjdZDCUE6ueBd0D1hpJ0_zE3x3Az6tlvzs98PsPuGzaQOMmuLa4qxNJ-OKxJmutDUlClpuvxuf-jyq4gCV" +
                    "5tEIILWRMBjlBEpJfWm63-VKKU4nvBWNJ7KfhWjl8-DUdNSh2pCDLpUObmb9Kquqc1x4BgttjN4rx_P-3_v-" +
                    "1jETXzIP1L44yHtpQNv0khYf4j_aHjcEri9ykvpz1mtdacbrKK25N4V1HHRwDqZiJzOCCISXDuqB6wguY_v4" +
                    "n0l1XtrEs7iCyfRFwNSKNrLqr23tR1CscmLfbH6ZLg5CYJTD-1uPSx0HMOB4Wv51PbWw\"}]}")
                    .getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            Assert.fail();
        }
        Mockito.when(response.getEntity()).thenReturn(httpEntity);
        try {
            Mockito.when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto("https://localhost:9444/services");
        JWKSConfigurationDTO jwksConfigurationDTO = new JWKSConfigurationDTO();
        tokenIssuerDto.setJwksConfigurationDTO(jwksConfigurationDTO);
        jwksConfigurationDTO.setEnabled(true);
        jwksConfigurationDTO.setUrl(jwksURL);

        // JWTValidatorConfiguration is mocked in order to test JWT Validator for different configurations.
        JWTValidatorConfiguration jwtValidatorConfiguration = PowerMockito.mock(JWTValidatorConfiguration.class);
        Mockito.when(jwtValidatorConfiguration.getJwtIssuer()).thenReturn(tokenIssuerDto);
        Mockito.when(jwtValidatorConfiguration.getTrustStore()).thenReturn(trustStore);
        try {
            Mockito.when(signedJWT.verify(any(JWSVerifier.class))).thenReturn(true);
        } catch (JOSEException e) {
            Assert.fail();
        }

        // This corresponds to the happy path where JWKS endpoint is used for validation, JWKS response is received,
        // And the signedJWT is verified against the JWK.
        JWTValidator jwtValidator = new JWTValidatorImpl();
        jwtValidator.loadValidatorConfiguration(jwtValidatorConfiguration);
        JWTValidationInfo jwtValidationInfo = null;
        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertTrue(jwtValidationInfo.isValid());
        // Apart from validation, here it asserts if the claims are properly populated in the JWTValidationInfo
        // for the happy path scenario
        Assert.assertEquals("testConsumerKey", jwtValidationInfo.getConsumerKey());
        Assert.assertNotNull(jwtValidationInfo.getScopes());
        Assert.assertEquals("testScope", jwtValidationInfo.getScopes().get(0));
        Assert.assertTrue(jwtValidationInfo.getAppToken());
        Assert.assertEquals("testJTI", jwtValidationInfo.getJti());
        Assert.assertEquals("tokenPayload", jwtValidationInfo.getRawPayload());
        Assert.assertNotNull(jwtValidationInfo.getClaims());
        Assert.assertEquals("testCustomClaim",
                String.valueOf(jwtValidationInfo.getClaims().get("customClaim")));

        // Here tests the behavior when the returned JWKS response does not verify the signature of the JWT.
        try {
            Mockito.when(signedJWT.verify(any(JWSVerifier.class))).thenReturn(false);
        } catch (JOSEException e) {
            Assert.fail();
        }
        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertFalse(jwtValidationInfo.isValid());

        // Here tests the behavior if some exception is thrown while signature verification is done.
        try {
            Mockito.when(signedJWT.verify(any(JWSVerifier.class))).thenThrow(new JOSEException(""));
        } catch (JOSEException e) {
            Assert.fail();
        }
        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertFalse(jwtValidationInfo.isValid());

        // Test if the token validation fails where signature is valid but the token is expired.
        Calendar pastDate = Calendar.getInstance();
        pastDate.add(Calendar.HOUR, -1);
        JWTClaimsSet jwtClaimsSetWithExpiry = new JWTClaimsSet.Builder()
                .expirationTime(pastDate.getTime())
                .claim("cnf", transportCertHash)
                .build();

        signedJWTInfo.setJwtClaimsSet(jwtClaimsSetWithExpiry);
        try {
            Mockito.when(signedJWT.verify(any(JWSVerifier.class))).thenReturn(true);
        } catch (JOSEException e) {
            Assert.fail();
        }
        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertFalse(jwtValidationInfo.isValid());
        Assert.assertEquals(900901, jwtValidationInfo.getValidationCode());
    }

    @Test
    public void validateTokenWithCertTest() throws KeyStoreException {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.RS256);
        jwsHeader = new JWSHeader.Builder(jwsHeader).keyID(keyId).build();
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo();
        SignedJWT signedJWT = Mockito.mock(SignedJWT.class);
        Mockito.when(signedJWT.getHeader()).thenReturn(jwsHeader);
        signedJWTInfo.setSignedJWT(signedJWT);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, 1);
        JSONObject transportCertHash = new JSONObject();
        transportCertHash.put("x5t#S256", CERT_HASH);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .expirationTime(now.getTime())
                .claim("cnf", transportCertHash)
                .build();
        signedJWTInfo.setJwtClaimsSet(jwtClaimsSet);

        CloseableHttpResponse response = PowerMockito.mock(CloseableHttpResponse.class);
        StatusLine statusLine = PowerMockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);
        HttpEntity httpEntity = PowerMockito.mock(HttpEntity.class);
        try {
            // Dummy payload. JWKS response's n value is not considered for validation.
            Mockito.when(httpEntity.getContent()).thenReturn(
                    new ByteArrayInputStream(("{\"keys\":[{\"kty\":\"RSA\",\"e\":\"AQAB\"," +
                    "\"use\":\"sig\",\"kid\":\"keyId\",\"alg\":\"RS256\",\"n\":\"kdgncoCrz655Lq8pTdX07" +
                    "eoVBjdZDCUE6ueBd0D1hpJ0_zE3x3Az6tlvzs98PsPuGzaQOMmuLa4qxNJ-OKxJmutDUlClpuvxuf-jyq4gCV" +
                    "5tEIILWRMBjlBEpJfWm63-VKKU4nvBWNJ7KfhWjl8-DUdNSh2pCDLpUObmb9Kquqc1x4BgttjN4rx_P-3_v-" +
                    "1jETXzIP1L44yHtpQNv0khYf4j_aHjcEri9ykvpz1mtdacbrKK25N4V1HHRwDqZiJzOCCISXDuqB6wguY_v4" +
                    "n0l1XtrEs7iCyfRFwNSKNrLqr23tR1CscmLfbH6ZLg5CYJTD-1uPSx0HMOB4Wv51PbWw\"}]}")
                    .getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            Assert.fail();
        }
        Mockito.when(response.getEntity()).thenReturn(httpEntity);
        try {
            Mockito.when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        javax.security.cert.Certificate javaxCert = Mockito.mock(javax.security.cert.Certificate.class);
        RSAPublicKey rsaPublicKey = Mockito.mock(RSAPublicKey.class);
        Mockito.when(javaxCert.getPublicKey()).thenReturn(rsaPublicKey);

        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto("https://localhost:9444/services");
        JWKSConfigurationDTO jwksConfigurationDTO = new JWKSConfigurationDTO();
        tokenIssuerDto.setJwksConfigurationDTO(jwksConfigurationDTO);
        tokenIssuerDto.setCertificate(javaxCert);
        jwksConfigurationDTO.setEnabled(false);
        JWTValidatorConfiguration jwtValidatorConfiguration = PowerMockito.mock(JWTValidatorConfiguration.class);
        Mockito.when(jwtValidatorConfiguration.getJwtIssuer()).thenReturn(tokenIssuerDto);

        JWTValidator jwtValidator = new JWTValidatorImpl();
        jwtValidator.loadValidatorConfiguration(jwtValidatorConfiguration);
        JWTValidationInfo jwtValidationInfo = null;
        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertFalse(jwtValidationInfo.isValid());
        Assert.assertEquals(900901, jwtValidationInfo.getValidationCode());

        try {
            Mockito.when(signedJWT.verify(any(JWSVerifier.class))).thenReturn(true);
        } catch (JOSEException e) {
            Assert.fail();
        }

        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertTrue(jwtValidationInfo.isValid());

        Mockito.when(jwtValidatorConfiguration.isEnableCertificateBoundAccessToken()).thenReturn(true);
        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertTrue(jwtValidationInfo.isValid());

        signedJWTInfo.setClientCertificate(trustStore.getCertificate("wso2carbon"));
        Assert.assertEquals(signedJWTInfo.getCertificateThumbprint(), signedJWTInfo.getClientCertificateHash());
        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertTrue(jwtValidationInfo.isValid());

        signedJWTInfo.setClientCertificate(trustStore.getCertificate("comodorsaca"));
        Assert.assertNotNull(signedJWTInfo.getCertificateThumbprint(), signedJWTInfo.getClientCertificateHash());
        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertFalse(jwtValidationInfo.isValid());
        Assert.assertEquals(900901, jwtValidationInfo.getValidationCode());

        try {
            // Dummy payload. JWKS response's n value is not considered for validation.
            Mockito.when(httpEntity.getContent()).thenReturn(
                    new ByteArrayInputStream(("{\"keys\":[{" +
                            "  \"kty\" : \"EC\"," +
                            "  \"crv\" : \"P-256\"," +
                            "  \"x\"   : \"SVqB4JcUD6lsfvqMr-OKUNUphdNn64Eay60978ZlL74\"," +
                            "  \"y\"   : \"lf0u0pMj4lGAzZix5u4Cm5CMQIgMNpkwy163wtKYVKI\"," +
                            "  \"d\"   : \"0g5vAEKzugrXaRbgKG0Tj2qJ5lMP4Bezds1_sTybkfk\"" +
                            "}]}")
                            .getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            Assert.fail();
        }
        Mockito.when(response.getEntity()).thenReturn(httpEntity);
        try {
            Mockito.when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            jwtValidationInfo = jwtValidator.validateToken(signedJWTInfo);
        } catch (CommonGatewayException e) {
            Assert.fail();
        }
        Assert.assertNotNull(jwtValidationInfo);
        Assert.assertFalse(jwtValidationInfo.isValid());
        Assert.assertEquals(900901, jwtValidationInfo.getValidationCode());
    }
}
