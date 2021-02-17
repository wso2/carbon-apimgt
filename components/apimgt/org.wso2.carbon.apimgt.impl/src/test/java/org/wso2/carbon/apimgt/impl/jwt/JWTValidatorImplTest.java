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
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.junit.Assert;
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
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.utils.JWTUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JWTUtil.class, GatewayUtils.class, APIManagerConfiguration.class, ServiceReferenceHolder.class,
        APIManagerConfigurationService.class})
public class JWTValidatorImplTest {

    private final JWSAlgorithm jwsAlgorithm = JWSAlgorithm.RS256;
    private final String KeyId = "keyId";
    private Log log = LogFactory.getLog(JWTValidatorImplTest.class);

    @Test
    public void testValidateToken() {

        JWSHeader jwsHeader = new JWSHeader(this.jwsAlgorithm);
        jwsHeader = new JWSHeader.Builder(jwsHeader).keyID(KeyId).build();
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo();
        SignedJWT signedJWT =
                Mockito.mock(SignedJWT.class);
        signedJWTInfo.setSignedJWT(signedJWT);
        JWTValidationInfo jwtValidationInfo = Mockito.mock(JWTValidationInfo.class);

        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto("https://localhost:9444/services");
        Mockito.when(signedJWT.getHeader()).thenReturn(jwsHeader);
        PowerMockito.mockStatic(JWTUtil.class);
        byte[] encodedCertificate = "sdnjnkjdsn".getBytes();
        try {
            PowerMockito.when(JWTUtil.verifyTokenSignature(signedJWT, KeyId)).thenReturn(true);
        } catch (APIManagementException e) {
            log.info("Exception while signature verification. " + e);
            Assert.fail();
        }

        JWTValidatorImpl jwtValidator = new JWTValidatorImpl();
        JWKSConfigurationDTO jwksConfigurationDTO = new JWKSConfigurationDTO();
        tokenIssuerDto.setJwksConfigurationDTO(jwksConfigurationDTO);
        jwksConfigurationDTO.setEnabled(false);
        jwtValidator.loadTokenIssuerConfiguration(tokenIssuerDto);

        try {
            JWTValidationInfo validatedInfo = jwtValidator.validateToken(signedJWTInfo);
            assertFalse(validatedInfo.isValid(), "JWT certificate bound access token validation failed due certificate is not found.");
        } catch (APIManagementException e) {
            Assert.fail();
        }

        // test when certificate is found in the trust store but cnf thumbprint is not matching with the certificate
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        X509Certificate x509Certificate = Mockito.mock(X509Certificate.class);
        X509Certificate[] sslCertObject = new X509Certificate[]{x509Certificate};
        Mockito.when(axis2MsgCntxt.getProperty(NhttpConstants.SSL_CLIENT_AUTH_CERT_X509)).thenReturn(sslCertObject);
        try {
            Mockito.when(x509Certificate.getEncoded()).thenReturn(encodedCertificate);
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);

        signedJWTInfo.setX509ClientCertificate(x509Certificate);
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(APIManagerConfiguration.class);
        PowerMockito.mockStatic(APIManagerConfigurationService.class);
        // Create a mock APIManagerConfiguration Object for retrieving properties from the deployment.toml
        APIManagerConfiguration apiManagerConfiguration = PowerMockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder serviceReferenceHolder = PowerMockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = PowerMockito.mock(APIManagerConfigurationService.class);
        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(oAuthServerConfiguration.getTimeStampSkewInSeconds()).thenReturn(300L);
        Mockito.when(serviceReferenceHolder.getOauthServerConfiguration()).thenReturn(oAuthServerConfiguration);
        // Mock the properties read from the deployment.toml
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.ENABLE_CERTIFICATE_BOUND_ACCESS_TOKEN))
                .thenReturn("true");
        try {
            JWTValidationInfo validatedInfo = jwtValidator.validateToken(signedJWTInfo);
            assertFalse(validatedInfo.isValid(),
                    "JWT certificate bound access token validation failed due certificate is not found.");
        } catch (APIManagementException e) {
            Assert.fail();
        }

        // Test when certificate bound access token validation is enabled and cnf thumbprint validation is successful
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, 1);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .expirationTime(now.getTime())
                .claim(APIConstants.CNF, "{\n" +
                        "         \"x5t#S256\": \"9a0c3570ac7392bee14a408ecb38978852a86d38cbc087feeeeaab2c9a07b9f1\"\n" +
                        "       }")
                .build();
        signedJWTInfo.setJwtClaimsSet(jwtClaimsSet);
        try {
            JWTValidationInfo validatedInfo = jwtValidator.validateToken(signedJWTInfo);
            assertTrue(validatedInfo.isValid(),
                    "JWT certificate bound access token validation failed. But it should be successful.");
        } catch (APIManagementException e) {
            Assert.fail();
        }
        //Test when certificate is found in the trust store but certificate bound access token is not enabled from the
        // config
        // when the config is not enabled validation should be successful.
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.ENABLE_CERTIFICATE_BOUND_ACCESS_TOKEN))
                .thenReturn("false");
        try {
            JWTValidationInfo validatedInfo = jwtValidator.validateToken(signedJWTInfo);
            assertTrue(validatedInfo.isValid(),
                    "JWT certificate bound access token validation failed. But it should be successful" +
                            "when the config is not set.");
        } catch (APIManagementException e) {
            Assert.fail();
        }
    }

}
