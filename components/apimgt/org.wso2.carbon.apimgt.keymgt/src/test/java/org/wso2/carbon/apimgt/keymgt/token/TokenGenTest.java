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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {CarbonUtils.class, ServiceReferenceHolder.class, AbstractJWTGenerator.class, APIUtil.class,
        KeyStoreManager.class, System.class, OAuth2Util.class, IdentityUtil.class, OAuthServerConfiguration.class,
        SubscriptionDataHolder.class, KeyManagerHolder.class})
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
        OAuthServerConfiguration oauthServerConfigurationMock = Mockito
                .mock(OAuthServerConfiguration.class);
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.when(OAuthServerConfiguration.getInstance()).thenReturn(oauthServerConfigurationMock);
        PowerMockito.mockStatic(OAuth2Util.class);
        OAuth2Util oAuth2Util = Mockito.mock(OAuth2Util.class);
        OAuthAppDO oAuthAppDO = Mockito.mock(OAuthAppDO.class);
        String[] audiences = {"aud1", "aud2"};
        PowerMockito.when(OAuth2Util.getAppInformationByClientId(Mockito.anyString())).thenReturn(oAuthAppDO);
        PowerMockito.when(oAuthAppDO.getAudiences()).thenReturn(audiences);
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
    @Ignore
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
    @Ignore
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
        MessageDigest digestValue = MessageDigest.getInstance("SHA-256");
        digestValue.update(der);
        byte[] digestInBytes = digestValue.digest();
        String publicCertThumbprint = hexify(digestInBytes);
        String encodedThumbprint = new String(new Base64(0, null, true).encode(
                publicCertThumbprint.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
        //Check if the encoded thumbprint get matched with JWT header's x5t
        Assert.assertTrue(header.contains(encodedThumbprint));
    }

    @Test public void testJTI() throws Exception {

        AbstractJWTGenerator jwtGen = new JWTGenerator();
        APIKeyValidationInfoDTO dto=new APIKeyValidationInfoDTO();
        TokenValidationContext validationContext = new TokenValidationContext();
        validationContext.setContext("testAPI");
        validationContext.setVersion("1.0.0");
        validationContext.setAccessToken("DUMMY_TOKEN_STRING");
        validationContext.setTenantDomain("carbon.super");
        dto.setSubscriber("admin");
        dto.setApplicationName("application");
        dto.setApplicationId("1");
        dto.setApplicationTier("UNLIMITED");
        dto.setEndUserName("subscriber");
        dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
        dto.setSubscriberTenantDomain("carbon.super");
        dto.setKeyManager("km1");
        validationContext.setValidationInfoDTO(dto);
        System.setProperty("carbon.home", "");
        APIManagerConfiguration config = Mockito.mock(APIManagerConfiguration.class);
        RealmService realmService = Mockito.mock(RealmService.class);

        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.doReturn(tenantManager).when(realmService).getTenantManager();
        Mockito.doReturn(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID)
                .when(tenantManager).getTenantId(Mockito.anyString());
        JWTConfigurationDto jWTConfigurationDto = Mockito.mock(JWTConfigurationDto.class);
        Mockito.when(config.getJwtConfigurationDto()).thenReturn(jWTConfigurationDto);
        Mockito.when(config.getJwtConfigurationDto().getConsumerDialectUri()).thenReturn("http://wso2.org/claims");
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl(config);
        org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance()
                .setAPIManagerConfigurationService(apiManagerConfigurationService);

        KeyManager keyManager = Mockito.mock(KeyManager.class);
        PowerMockito.mockStatic(KeyManagerHolder.class);
        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "km1"))
                .thenReturn(keyManager);

        String token = jwtGen.buildBody(validationContext);
        Assert.assertTrue("Contains JTI value in access token", token.contains("jti"));
    }

    /**
     * Helper method to hexify a byte array.
     * TODO:need to verify the logic
     *
     * @param bytes - The input byte array
     * @return hexadecimal representation
     */
    private String hexify(byte[] bytes) {

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
