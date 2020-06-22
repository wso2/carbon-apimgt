/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.keymgt.token;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {AbstractJWTGenerator.class,APIUtil.class,KeyStoreManager.class,CarbonUtils.class,
        SubscriptionDataHolder.class})
public class TokenGenTest {
    private static final Log log = LogFactory.getLog(TokenGenTest.class);

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.mockStatic(SubscriptionDataHolder.class);
        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        Mockito.when(serverConfiguration.getFirstProperty(APIConstants.PORT_OFFSET_CONFIG)).thenReturn("2");
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(
                new APIManagerConfigurationServiceImpl(config));
        SubscriptionDataStore subscriptionDataStore = Mockito.mock(SubscriptionDataStore.class);
        SubscriptionDataHolder subscriptionDataHolder = Mockito.mock(SubscriptionDataHolder.class);
        PowerMockito.when(SubscriptionDataHolder.getInstance()).thenReturn(subscriptionDataHolder);
        PowerMockito.when(SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))
                .thenReturn(subscriptionDataStore);
        Application application = new Application();
        application.setId(1);
        application.setName("app2");
        application.setUUID(UUID.randomUUID().toString());
        application.addAttribute("abc","cde");
        Mockito.when(subscriptionDataStore.getApplicationById(1)).thenReturn(application);
    }

    @Test
    public void testAbstractJWTGenerator() throws Exception {
        JWTGenerator jwtGen = new JWTGenerator() {
            @Override
            protected Map<String, String> convertClaimMap(Map<ClaimMapping, String> userAttributes, String username) {
                return new HashMap<>();
            }
        };
        APIKeyValidationInfoDTO dto=new APIKeyValidationInfoDTO();

        TokenValidationContext validationContext = new TokenValidationContext();
        validationContext.setValidationInfoDTO(dto);
        validationContext.setContext("testAPI");
        validationContext.setVersion("1.5.0");
        validationContext.setAccessToken("DUMMY_TOKEN_STRING");

        dto.setSubscriber("sanjeewa");
        dto.setApplicationName("sanjeewa-app");
        dto.setApplicationId("1");
        dto.setApplicationTier("UNLIMITED");
        dto.setEndUserName("malalgoda");
        dto.setSubscriberTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
        //Here we will call generate token method with 4 argument.
        String token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        String header = token.split("\\.")[0];
        String decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        String body = token.split("\\.")[1];
        String decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);
        // With end user name not included
        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);
        dto.setUserType(APIConstants.SUBSCRIPTION_USER_TYPE);
        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);

        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);
    }

    //    TODO: Have to convert to work with new JWT generation and signing
    @Test
    public void testJWTGeneration() throws Exception {
        JWTGenerator jwtGen = new JWTGenerator() {
            @Override
            public Map<String, String> convertClaimMap(Map<ClaimMapping, String> userAttributes, String username) {
                return new HashMap<>();
            }
        };
        APIKeyValidationInfoDTO dto=new APIKeyValidationInfoDTO();
        dto.setSubscriber("sastry");
        dto.setApplicationName("hubapp");
        dto.setApplicationId("1");
        dto.setApplicationTier("UNLIMITED");
        dto.setEndUserName("denis");
        dto.setSubscriberTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
        TokenValidationContext validationContext = new TokenValidationContext();
        validationContext.setValidationInfoDTO(dto);
        validationContext.setContext("cricScore");
        validationContext.setVersion("1.9.0");
        String token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        String header = token.split("\\.")[0];
        String decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        String body = token.split("\\.")[1];
        String decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);


        // With end user name not included
        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);


        dto.setUserType(APIConstants.SUBSCRIPTION_USER_TYPE);
        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);

        token = jwtGen.generateToken(validationContext);
        System.out.println("Generated Token: " + token);
        header = token.split("\\.")[0];
        decodedHeader = new String(Base64Utils.decode(header));
        System.out.println("Header: "+decodedHeader);
        body = token.split("\\.")[1];
        decodedBody = new String(Base64Utils.decode(body));
        System.out.println("Body: " + decodedBody);


        //we can not do assert eaquals because body includes expiration time.

        /*String expectedHeader = "{\"typ\":\"JWT\"}";
        String expectedBody = "{\"iss\":\"wso2.org/products/am\", \"exp\":1349270811075, " +
                              "\"http://wso2.org/claims/subscriber\":\"sastry\", " +
                              "\"http://wso2.org/claims/applicationname\":\"hubapp\", " +
                              "\"http://wso2.org/claims/apicontext\":\"cricScore\", " +
                              "\"http://wso2.org/claims/version\":\"1.9.0\", " +
                              "\"http://wso2.org/claims/tier\":\"Bronze\", " +
                              "\"http://wso2.org/claims/enduser\":\"denis\"}";

        Assert.assertEquals(expectedHeader, decodedHeader);
        Assert.assertEquals(expectedBody, decodedBody);*/
        //String decodedToken = new String(Base64Utils.decode(token));
        //log.info(decodedToken);
        //assertNotNull(decodedToken);


    }

    @Test
    public void testJWTx5tEncoding() throws Exception {
        //Read public certificat
        InputStream inputStream = new FileInputStream("src/test/resources/wso2carbon.jks");
        KeyStore keystore = KeyStore.getInstance("JKS");
        char[] pwd = "wso2carbon".toCharArray();
        keystore.load(inputStream, pwd);
        Certificate cert = keystore.getCertificate("wso2carbon");

        //Generate JWT header using the above certificate
        String header = APIUtil.generateHeader(cert, "SHA256withRSA");

        //Get the public certificate's thumbprint and base64url encode it
        byte[] der = cert.getEncoded();
        MessageDigest digestValue = MessageDigest.getInstance("SHA-1");
        digestValue.update(der);
        byte[] digestInBytes = digestValue.digest();
        String publicCertThumbprint = hexify(digestInBytes);
        String encodedThumbprint = java.util.Base64.getUrlEncoder()
                .encodeToString(publicCertThumbprint.getBytes("UTF-8"));
        //Check if the encoded thumbprint get matched with JWT header's x5t
        Assert.assertTrue(header.contains(encodedThumbprint));
    }

    /**
     * Helper method to hexify a byte array.
     * TODO:need to verify the logic
     *
     * @param bytes - The input byte array
     * @return hexadecimal representation
     */
    private String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (byte aByte : bytes) {
            buf.append(hexDigits[(aByte & 0xf0) >> 4]);
            buf.append(hexDigits[aByte & 0x0f]);
        }

        return buf.toString();
    }
}
