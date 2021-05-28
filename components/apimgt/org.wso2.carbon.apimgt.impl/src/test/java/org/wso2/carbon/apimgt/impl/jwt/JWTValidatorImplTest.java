/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.JWKSConfigurationDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImplTest;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.apimgt.impl.utils.JWTUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JWTUtil.class, APIManagerConfiguration.class, ServiceReferenceHolder.class,
        APIManagerConfigurationService.class, APIUtil.class, X509CertUtils.class})
public class JWTValidatorImplTest {

    private final JWSAlgorithm jwsAlgorithm = JWSAlgorithm.RS256;
    private final String KeyId = "keyId";
    private final String alias = "abcde";
    private static String PASSWORD = "wso2carbon";
    private static String BASE64_ENCODED_CERT = "MIIE+zCCAuOgAwIBAgIJAKvdGdaKIgxqMA0GCSqGSIb3DQEBCwUAMBQxEjAQBgNV\n" +
            "BAMMCWxvY2FsaG9zdDAeFw0xODA5MTAwNjA0MzBaFw0yODA5MDcwNjA0MzBaMBQx\n" +
            "EjAQBgNVBAMMCWxvY2FsaG9zdDCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoC\n" +
            "ggIBANemSwQgFmP/2U/EgXqmT0P06m6r8eqYjEC6YpbsbPOOm+r5CB+pjG4METby\n" +
            "Gsgzqz6U+kZSmQ32C+VHnNRMtS2QiMjZIPBvLaw+HGKD1B2jf3edQ5pKQvPWDl2u\n" +
            "kC2Xrvfx90FaSMoEhIJ549W9Tgie7taxx63Nru2O2ubWD8j896nGQs6FO9u80dNo\n" +
            "d4rGEitUIG1lWoUCcLVjgLw4Knt8fCBmaoeO1TB4++5ARBM2nKAD65DZGefz2yWM\n" +
            "I1dk0EhBWWF7OYzIUfgNXwk4A4L+2ANjKFUgk5GkPCCRRm8F//v2IMMl5Qg0l3Zv\n" +
            "yxWs5Zm60XRpVErsQjKiXC5JsR62Qs5AZfwAflzFZqCGXgLGfIQcYBKjtMEw/B7T\n" +
            "oxG6G59dwy41p4XymQ22C6HDGH/dLkVbtegWzGP7vQJ3Vufgtq+LQI6mgZ79vWg7\n" +
            "2GnnoHNX7ameYjA3peReMhJdcwM5rsjFnVICNrCguwKo7IhwdEuyBfezAYdDpOoT\n" +
            "O4Zxq132GxHE2phaV4KnjwhAWhiadB4BuPSaeWIkND/5o4jnvGgdtiKChLoyAE1/\n" +
            "CFjLIDidaxYgTZthLVS4LJtSAJ0pn439+j6ns2U0c0SeLmCWBA1PwEJeeDOqKKwB\n" +
            "4EZlSPDGo5t+WKgTWKECiw1cmI59RO6orG46imJ5Xo1/Dfa5AgMBAAGjUDBOMB0G\n" +
            "A1UdDgQWBBSQd7RzrqJuFuGo7YjZI8rk5i3tyDAfBgNVHSMEGDAWgBSQd7RzrqJu\n" +
            "FuGo7YjZI8rk5i3tyDAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4ICAQAI\n" +
            "SPGIT6QDD6U0lFgfImHOHix1bkxpSEBR/NmfZuc1M9Df4x8HSfSazZQP51nI7QdA\n" +
            "iIHUpxYsgE/o5Moa8H49iTqak41PNEN2FlhOg4X05hfr5iFJGAwrWh5poH99yYd0\n" +
            "/Mr0KlIx6/85wJHtr7k0MPvLX+LNNZqkdHCJYlXZ5ZE7vhKar9gOwU7qbqsPQZ1V\n" +
            "jxMAiK7TQrqZGwHZDwkYyW0sDOLe3+JejeXt7COx/cyKOggO8HhFGui1dv97AXaZ\n" +
            "saOZaLjysyHwhtbPLNwfjaVZ8HGtPEetSSTc6hAWZRO5FjrlpsQhcwzGend+SU5p\n" +
            "dhvDGFO1fDX92koT+T8U6dMqKowt776eKncVjhn8FCBWoHp9OCyDlc9xkFvsJfwV\n" +
            "mS0YRseEdFtOyk44Tst8BT/zPxGnOH+VtjY6+kl8U7baO+DQpBIErlwY2Ua0eWGW\n" +
            "EA1CcnJJd1dnkmHqIUgplu6G0O9D1G1rwDVn0D+EPnWq7vP1aN+xGz6iIQAKJswO\n" +
            "+lbz5BzW1wc0/b3WbYgXSJTynyDIWjDkShHyg+lxIQDRzD6YEeXYptb3DgkOhzW2\n" +
            "xjqrSvLzUtM00iIf1hAvYUCtSbB4yGGWis+JfJ18Seh7yMUFAo8h09UfoYZaG+XG\n" +
            "ztxBTi4NOuEOG6RHtbxnssFz76ShPIRB4ugMSLXO2Q==";
    public static final String BASE64_ENCODED_CLIENT_CERTIFICATE_HEADER = "X-WSO2-CLIENT-CERTIFICATE";
    public static final String CERTIFICATE = "-----BEGIN CERTIFICATE-----\n" +
            "MIIE+zCCAuOgAwIBAgIJAKvdGdaKIgxqMA0GCSqGSIb3DQEBCwUAMBQxEjAQBgNV\n" +
            "BAMMCWxvY2FsaG9zdDAeFw0xODA5MTAwNjA0MzBaFw0yODA5MDcwNjA0MzBaMBQx\n" +
            "EjAQBgNVBAMMCWxvY2FsaG9zdDCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoC\n" +
            "ggIBANemSwQgFmP/2U/EgXqmT0P06m6r8eqYjEC6YpbsbPOOm+r5CB+pjG4METby\n" +
            "Gsgzqz6U+kZSmQ32C+VHnNRMtS2QiMjZIPBvLaw+HGKD1B2jf3edQ5pKQvPWDl2u\n" +
            "kC2Xrvfx90FaSMoEhIJ549W9Tgie7taxx63Nru2O2ubWD8j896nGQs6FO9u80dNo\n" +
            "d4rGEitUIG1lWoUCcLVjgLw4Knt8fCBmaoeO1TB4++5ARBM2nKAD65DZGefz2yWM\n" +
            "I1dk0EhBWWF7OYzIUfgNXwk4A4L+2ANjKFUgk5GkPCCRRm8F//v2IMMl5Qg0l3Zv\n" +
            "yxWs5Zm60XRpVErsQjKiXC5JsR62Qs5AZfwAflzFZqCGXgLGfIQcYBKjtMEw/B7T\n" +
            "oxG6G59dwy41p4XymQ22C6HDGH/dLkVbtegWzGP7vQJ3Vufgtq+LQI6mgZ79vWg7\n" +
            "2GnnoHNX7ameYjA3peReMhJdcwM5rsjFnVICNrCguwKo7IhwdEuyBfezAYdDpOoT\n" +
            "O4Zxq132GxHE2phaV4KnjwhAWhiadB4BuPSaeWIkND/5o4jnvGgdtiKChLoyAE1/\n" +
            "CFjLIDidaxYgTZthLVS4LJtSAJ0pn439+j6ns2U0c0SeLmCWBA1PwEJeeDOqKKwB\n" +
            "4EZlSPDGo5t+WKgTWKECiw1cmI59RO6orG46imJ5Xo1/Dfa5AgMBAAGjUDBOMB0G\n" +
            "A1UdDgQWBBSQd7RzrqJuFuGo7YjZI8rk5i3tyDAfBgNVHSMEGDAWgBSQd7RzrqJu\n" +
            "FuGo7YjZI8rk5i3tyDAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4ICAQAI\n" +
            "SPGIT6QDD6U0lFgfImHOHix1bkxpSEBR/NmfZuc1M9Df4x8HSfSazZQP51nI7QdA\n" +
            "iIHUpxYsgE/o5Moa8H49iTqak41PNEN2FlhOg4X05hfr5iFJGAwrWh5poH99yYd0\n" +
            "/Mr0KlIx6/85wJHtr7k0MPvLX+LNNZqkdHCJYlXZ5ZE7vhKar9gOwU7qbqsPQZ1V\n" +
            "jxMAiK7TQrqZGwHZDwkYyW0sDOLe3+JejeXt7COx/cyKOggO8HhFGui1dv97AXaZ\n" +
            "saOZaLjysyHwhtbPLNwfjaVZ8HGtPEetSSTc6hAWZRO5FjrlpsQhcwzGend+SU5p\n" +
            "dhvDGFO1fDX92koT+T8U6dMqKowt776eKncVjhn8FCBWoHp9OCyDlc9xkFvsJfwV\n" +
            "mS0YRseEdFtOyk44Tst8BT/zPxGnOH+VtjY6+kl8U7baO+DQpBIErlwY2Ua0eWGW\n" +
            "EA1CcnJJd1dnkmHqIUgplu6G0O9D1G1rwDVn0D+EPnWq7vP1aN+xGz6iIQAKJswO\n" +
            "+lbz5BzW1wc0/b3WbYgXSJTynyDIWjDkShHyg+lxIQDRzD6YEeXYptb3DgkOhzW2\n" +
            "xjqrSvLzUtM00iIf1hAvYUCtSbB4yGGWis+JfJ18Seh7yMUFAo8h09UfoYZaG+XG\n" +
            "ztxBTi4NOuEOG6RHtbxnssFz76ShPIRB4ugMSLXO2Q==\n" +
            "-----END CERTIFICATE-----";


    private static final String CERT_HASH =  "9a0c3570ac7392bee14a408ecb38978852a86d38cbc087feeeeaab2c9a07b9f1";

    SignedJWT signedJWT;
    JWSHeader jwsHeader;
    SignedJWTInfo signedJWTInfo;
    private Log log = LogFactory.getLog(JWTValidatorImplTest.class);

    @Before
    public void setup() {

        jwsHeader = new JWSHeader(this.jwsAlgorithm);
        jwsHeader = new JWSHeader.Builder(jwsHeader).keyID(KeyId).build();
        signedJWTInfo = new SignedJWTInfo();
        signedJWT =
                Mockito.mock(SignedJWT.class);
        signedJWTInfo.setSignedJWT(signedJWT);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, 1);
        JSONObject transportCertHash = new JSONObject();
        transportCertHash.put("x5t#S256", CERT_HASH);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .expirationTime(now.getTime())
                .claim(APIConstants.CNF, transportCertHash)
                .build();
        signedJWTInfo.setJwtClaimsSet(jwtClaimsSet);
        System.setProperty("javax.net.ssl.trustStore", CertificateManagerImplTest.class.getClassLoader().getResource
                ("security/client-truststore.jks").getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", PASSWORD);
    }

    @Test
    @PrepareForTest({CertificateMgtUtils.class, JWTUtil.class, APIManagerConfiguration.class,
            ServiceReferenceHolder.class,
            APIManagerConfigurationService.class, APIUtil.class, X509CertUtils.class})
    public void testValidateToken() {

        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto("https://localhost:9444/services");
        Mockito.when(signedJWT.getHeader()).thenReturn(jwsHeader);
        PowerMockito.mockStatic(JWTUtil.class);
        byte[] encodedCertificateUnmatched = "aaaaaaaaaaaaaaaa".getBytes();
        try {
            PowerMockito.when(JWTUtil.verifyTokenSignature(signedJWT, KeyId)).thenReturn(true);
        } catch (APIManagementException e) {
            log.info("Exception while signature verification. " + e);
            Assert.fail();
        }
        // Create a mock APIManagerConfiguration Object for retrieving properties from the deployment.toml
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(APIManagerConfiguration.class);
        PowerMockito.mockStatic(APIManagerConfigurationService.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(CertificateMgtUtils.class);
        PowerMockito.mockStatic(X509CertUtils.class);
        APIManagerConfiguration apiManagerConfiguration = PowerMockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder serviceReferenceHolder = PowerMockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = PowerMockito.mock(APIManagerConfigurationService.class);
        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(oAuthServerConfiguration.getTimeStampSkewInSeconds()).thenReturn(300L);
        Mockito.when(serviceReferenceHolder.getOauthServerConfiguration()).thenReturn(oAuthServerConfiguration);

        JWTValidatorImpl jwtValidator = new JWTValidatorImpl();
        JWKSConfigurationDTO jwksConfigurationDTO = new JWKSConfigurationDTO();
        tokenIssuerDto.setJwksConfigurationDTO(jwksConfigurationDTO);
        jwksConfigurationDTO.setEnabled(false);
        jwtValidator.loadTokenIssuerConfiguration(tokenIssuerDto);

        try {
            JWTValidationInfo validatedInfo = jwtValidator.validateToken(signedJWTInfo);
            assertTrue(validatedInfo.isValid(), "JWT certificate bound access token validation failed even when the" +
                    " configuration is not enabled.");
        } catch (APIManagementException e) {
            Assert.fail();
        }

        // test when certificate is found in the trust store but cnf thumbprint is not matching with the certificate
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);

        X509Certificate x509Certificate = Mockito.mock(X509Certificate.class);
        java.security.cert.X509Certificate x509CertificateJava = Mockito.mock(java.security.cert.X509Certificate.class);
        PowerMockito.when(CertificateMgtUtils.convert(x509Certificate)).thenReturn(Optional.of(x509CertificateJava));

        X509Certificate[] sslCertObject = new X509Certificate[]{x509Certificate};
        Mockito.when(axis2MsgCntxt.getProperty(NhttpConstants.SSL_CLIENT_AUTH_CERT_X509)).thenReturn(sslCertObject);

        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        X509Certificate x509CertificateUnMatched = Mockito.mock(X509Certificate.class);
        java.security.cert.X509Certificate x509CertificateUnMatchedJava =
                Mockito.mock(java.security.cert.X509Certificate.class);
        PowerMockito.when(CertificateMgtUtils.convert(x509CertificateUnMatched))
                .thenReturn(Optional.of(x509CertificateUnMatchedJava));


        PowerMockito.when(X509CertUtils.computeSHA256Thumbprint(x509CertificateJava)).thenReturn(new Base64URL(CERT_HASH));
        PowerMockito.when(X509CertUtils.computeSHA256Thumbprint(x509CertificateUnMatchedJava))
                .thenReturn(new Base64URL(encodedCertificateUnmatched.toString()));
        signedJWTInfo.setX509ClientCertificate(x509CertificateUnMatched);

        // Mock the properties read from the deployment.toml
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.ENABLE_CERTIFICATE_BOUND_ACCESS_TOKEN))
                .thenReturn("true");
        try {
            JWTValidationInfo validatedInfo = jwtValidator.validateToken(signedJWTInfo);
            assertFalse(validatedInfo.isValid(),
                    "JWT certificate bound access token validation successful even if the certificate thumbprint" +
                            " is incorrect.");
        } catch (APIManagementException e) {
            Assert.fail();
        }
        //validate with correct certificate thumbprint
        signedJWTInfo.setX509ClientCertificate(x509Certificate);
        try {
            JWTValidationInfo validatedInfo = jwtValidator.validateToken(signedJWTInfo);
            assertTrue(validatedInfo.isValid(),
                    "JWT certificate bound access token validation failed with the correct certificate thumbprint.");
        } catch (APIManagementException e) {
            Assert.fail();
        }
        // Test when certificate bound access token validation is enabled and cnf thumbprint validation is successful
        // when client certificate is added in the trust store
        signedJWTInfo.setX509ClientCertificate(null);
        headers.put(BASE64_ENCODED_CLIENT_CERTIFICATE_HEADER, BASE64_ENCODED_CERT);

    }

    public X509Certificate getClientCertificate(org.apache.axis2.context.MessageContext axis2MessageContext)
            throws APIManagementException {

        Map headers =
                (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Object sslCertObject = axis2MessageContext.getProperty(NhttpConstants.SSL_CLIENT_AUTH_CERT_X509);
        X509Certificate certificateFromMessageContext = null;
        if (sslCertObject != null) {
            X509Certificate[] certs = (X509Certificate[]) sslCertObject;
            certificateFromMessageContext = certs[0];
        }
        if (headers.containsKey(getClientCertificateHeader())) {
            try {
                if (!isClientCertificateValidationEnabled() || APIUtil
                        .isCertificateExistsInListenerTrustStore(certificateFromMessageContext)) {
                    String certificate = (String) headers.get(getClientCertificateHeader());
                    X509Certificate x509Certificate = getCertificateFromBase64EncodedString(certificate);
                    if (APIUtil.isCertificateExistsInListenerTrustStore(x509Certificate)) {
                        return x509Certificate;
                    } else {
                        log.debug("Certificate is Header is not exist in truststore");
                        return null;
                    }
                }
            } catch (APIManagementException e) {
                String msg = "Error while validating into Certificate Existence";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        }
        return certificateFromMessageContext;
    }

    public String getClientCertificateHeader() {

        return BASE64_ENCODED_CLIENT_CERTIFICATE_HEADER;
    }

    private static boolean isClientCertificateValidationEnabled() {

        return true;
    }

    private static boolean isClientCertificateEncoded() {

        return false;
    }

    private X509Certificate getCertificateFromBase64EncodedString(String certificate) throws APIManagementException {

        byte[] bytes;
        if (certificate != null) {
            if (!isClientCertificateEncoded()) {
                certificate = APIUtil.getX509certificateContent(certificate);
                bytes = certificate.getBytes();
            } else {
                try {
                    certificate = URLDecoder.decode(certificate, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    String msg = "Error while URL decoding certificate";
                    throw new APIManagementException(msg, e);
                }

                certificate = APIUtil.getX509certificateContent(certificate);
                bytes = Base64.decodeBase64(certificate);
            }
            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                return X509Certificate.getInstance(inputStream);
            } catch (IOException | CertificateException e) {
                String msg = "Error while converting into X509Certificate";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        }
        return null;
    }
}
