package org.wso2.carbon.apimgt.gateway.handlers.security.keys;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.axis2.context.MessageContext;
import org.apache.http.conn.ssl.SSLContexts;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;


public class APIKeyValidatorClientTest {
    @Rule
    public WireMockRule wireMockRule;
    public static WireMockConfiguration wireMockConfiguration = new WireMockConfiguration();
    private static final String KEYSTORE_FILE_PATH =
            "src" + File.separator + "test" + File.separator + "resources" + File.separator + "jks"
                    + File.separator + "wso2carbon.jks";
    private static final String TRUSTSTORE_FILE_PATH =
            "src" + File.separator + "test" + File.separator + "resources" + File.separator + "jks"
                    + File.separator + "client-truststore.jks";

    @BeforeClass
    public static void setTrustManager() throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        wireMockConfiguration.trustStoreType("JKS").keystoreType("JKS").keystorePath(KEYSTORE_FILE_PATH)
                .trustStorePath(TRUSTSTORE_FILE_PATH).httpsPort(8082).trustStorePassword("wso2carbon")
                .keystorePassword("wso2carbon");
        ;
        InputStream inputStream = new FileInputStream("src/test/resources/jks/wso2carbon.jks");
        KeyStore keystore = KeyStore.getInstance("JKS");
        char[] pwd = "wso2carbon".toCharArray();
        keystore.load(inputStream, pwd);
        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(keystore).useSSL().build();
        SSLContext.setDefault(sslcontext);
    }

    @Test
    public void getAPIKeyData() throws Exception {
        wireMockRule = new WireMockRule(wireMockConfiguration);
        wireMockRule.stubFor(WireMock.post(urlEqualTo("/services/APIKeyValidationService")).withBasicAuth("admin",
                "admin").willReturn(aResponse().withBody("<soapenv:Envelope " +
                "xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "   <soapenv:Body>\n" +
                "      <ns:validateKeyResponse xmlns:ns=\"http://org.apache.axis2/xsd\">\n" +
                "         <ns:return xsi:type=\"ax2129:APIKeyValidationInfoDTO\" xmlns:ax2125=\"http://keymgt.apimgt" +
                ".carbon.wso2.org/xsd\" xmlns:ax2127=\"http://api.apimgt.carbon.wso2.org/xsd\" " +
                "xmlns:ax2129=\"http://dto.impl.apimgt.carbon.wso2.org/xsd\" xmlns:ax2131=\"http://model.api.apimgt" +
                ".carbon.wso2.org/xsd\" xmlns:ax2132=\"http://dto.api.apimgt.carbon.wso2.org/xsd\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "            <ax2129:apiName>PizzaShackAPI</ax2129:apiName>\n" +
                "            <ax2129:apiPublisher>admin</ax2129:apiPublisher>\n" +
                "            <ax2129:apiTier xsi:nil=\"true\"/>\n" +
                "            <ax2129:applicationId>1</ax2129:applicationId>\n" +
                "            <ax2129:applicationName>DefaultApplication</ax2129:applicationName>\n" +
                "            <ax2129:applicationTier>Unlimited</ax2129:applicationTier>\n" +
                "            <ax2129:authorized>true</ax2129:authorized>\n" +
                "            <ax2129:authorizedDomains xsi:nil=\"true\"/>\n" +
                "            <ax2129:consumerKey>mKdqktRsbjxpfgovkPAviyffWC0a</ax2129:consumerKey>\n" +
                "            <ax2129:contentAware>false</ax2129:contentAware>\n" +
                "            <ax2129:endUserName>admin@carbon.super</ax2129:endUserName>\n" +
                "            <ax2129:endUserToken xsi:nil=\"true\"/>\n" +
                "            <ax2129:issuedTime>1507717162497</ax2129:issuedTime>\n" +
                "            <ax2129:scopes>default</ax2129:scopes>\n" +
                "            <ax2129:scopes>am_application_scope</ax2129:scopes>\n" +
                "            <ax2129:spikeArrestLimit>0</ax2129:spikeArrestLimit>\n" +
                "            <ax2129:spikeArrestUnit xsi:nil=\"true\"/>\n" +
                "            <ax2129:stopOnQuotaReach>true</ax2129:stopOnQuotaReach>\n" +
                "            <ax2129:subscriber>admin</ax2129:subscriber>\n" +
                "            <ax2129:subscriberTenantDomain>carbon.super</ax2129:subscriberTenantDomain>\n" +
                "            <ax2129:throttlingDataList>api_level_throttling_key</ax2129:throttlingDataList>\n" +
                "            <ax2129:tier>Unlimited</ax2129:tier>\n" +
                "            <ax2129:type>PRODUCTION</ax2129:type>\n" +
                "            <ax2129:userType>APPLICATION</ax2129:userType>\n" +
                "            <ax2129:validationStatus>0</ax2129:validationStatus>\n" +
                "            <ax2129:validityPeriod>2059000</ax2129:validityPeriod>\n" +
                "         </ns:return>\n" +
                "      </ns:validateKeyResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>").withStatus(200)));
        wireMockRule.start();
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (apiManagerConfiguration));
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn
                ("https://localhost:" + 8082 + "/services/");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn
                ("admin");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn
                ("admin");
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Map headers = new HashMap();
        Mockito.when(messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        MessageContext.setCurrentMessageContext(messageContext);
        APIKeyValidatorClient apiKeyValidatorClient = new APIKeyValidatorClientWrapper();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = apiKeyValidatorClient.getAPIKeyData("/pizzashack/1.0.0",
                "1.0.0", "eaa0e467-36f7-367b-ba8c-87ab9849456f",
                "ANY", "http://localhost", "/menu", "GET");
        wireMockRule.resetAll();
        wireMockRule.stop();
        Assert.assertNotNull(apiKeyValidationInfoDTO);
        Assert.assertEquals(apiKeyValidationInfoDTO.getApiName(), "PizzaShackAPI");
        Assert.assertEquals(apiKeyValidationInfoDTO.getApiPublisher(), "admin");
        Assert.assertEquals(apiKeyValidationInfoDTO.getApplicationId(), "1");
        Assert.assertEquals(apiKeyValidationInfoDTO.getApplicationName(), "DefaultApplication");
        Assert.assertEquals(apiKeyValidationInfoDTO.getApplicationTier(), "Unlimited");
        Assert.assertEquals(apiKeyValidationInfoDTO.getType(), "PRODUCTION");
        Assert.assertEquals(apiKeyValidationInfoDTO.getEndUserName(), "admin@carbon.super");
    }

    @Test
    public void getAPIKeyDataWhileKeyManagerCallFailed() throws Exception {
        wireMockRule = new WireMockRule(wireMockConfiguration);
        wireMockRule.stubFor(WireMock.post(urlEqualTo("/services/APIKeyValidationService")).withBasicAuth("admin",
                "admin").willReturn(aResponse().withBody("<soapenv:Envelope " +
                "xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "   <soapenv:Body>\n" +
                "      <ns:validateKeyResponse xmlns:ns=\"http://org.apache.axis2/xsd\">\n" +
                "         <ns:return xsi:type=\"ax2129:APIKeyValidationInfoDTO\" xmlns:ax2125=\"http://keymgt.apimgt" +
                ".carbon.wso2.org/xsd\" xmlns:ax2127=\"http://api.apimgt.carbon.wso2.org/xsd\" " +
                "xmlns:ax2129=\"http://dto.impl.apimgt.carbon.wso2.org/xsd\" xmlns:ax2131=\"http://model.api.apimgt" +
                ".carbon.wso2.org/xsd\" xmlns:ax2132=\"http://dto.api.apimgt.carbon.wso2.org/xsd\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "            <ax2129:apiName>PizzaShackAPI</ax2129:apiName>\n" +
                "            <ax2129:apiPublisher>admin</ax2129:apiPublisher>\n" +
                "            <ax2129:apiTier xsi:nil=\"true\"/>\n" +
                "            <ax2129:applicationId>1</ax2129:applicationId>\n" +
                "            <ax2129:applicationName>DefaultApplication</ax2129:applicationName>\n" +
                "            <ax2129:applicationTier>Unlimited</ax2129:applicationTier>\n" +
                "            <ax2129:authorized>true</ax2129:authorized>\n" +
                "            <ax2129:authorizedDomains xsi:nil=\"true\"/>\n" +
                "            <ax2129:consumerKey>mKdqktRsbjxpfgovkPAviyffWC0a</ax2129:consumerKey>\n" +
                "            <ax2129:contentAware>false</ax2129:contentAware>\n" +
                "            <ax2129:endUserName>admin@carbon.super</ax2129:endUserName>\n" +
                "            <ax2129:endUserToken xsi:nil=\"true\"/>\n" +
                "            <ax2129:issuedTime>1507717162497</ax2129:issuedTime>\n" +
                "            <ax2129:scopes>default</ax2129:scopes>\n" +
                "            <ax2129:scopes>am_application_scope</ax2129:scopes>\n" +
                "            <ax2129:spikeArrestLimit>0</ax2129:spikeArrestLimit>\n" +
                "            <ax2129:spikeArrestUnit xsi:nil=\"true\"/>\n" +
                "            <ax2129:stopOnQuotaReach>true</ax2129:stopOnQuotaReach>\n" +
                "            <ax2129:subscriber>admin</ax2129:subscriber>\n" +
                "            <ax2129:subscriberTenantDomain>carbon.super</ax2129:subscriberTenantDomain>\n" +
                "            <ax2129:throttlingDataList>api_level_throttling_key</ax2129:throttlingDataList>\n" +
                "            <ax2129:tier>Unlimited</ax2129:tier>\n" +
                "            <ax2129:type>PRODUCTION</ax2129:type>\n" +
                "            <ax2129:userType>APPLICATION</ax2129:userType>\n" +
                "            <ax2129:validationStatus>0</ax2129:validationStatus>\n" +
                "            <ax2129:validityPeriod>2059000</ax2129:validityPeriod>\n" +
                "         </ns:return>\n" +
                "      </ns:validateKeyResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>").withStatus(200)));
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (apiManagerConfiguration));
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn
                ("https://localhost:" + 8082 + "/services/");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn
                ("admin");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn
                ("admin");
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Map headers = new HashMap();
        Mockito.when(messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        MessageContext.setCurrentMessageContext(messageContext);
        APIKeyValidatorClient apiKeyValidatorClient = new APIKeyValidatorClientWrapper();
        try {
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = apiKeyValidatorClient.getAPIKeyData("/pizzashack/1.0.0",
                    "1.0.0", "eaa0e467-36f7-367b-ba8c-87ab9849456f",
                    "ANY", "http://localhost", "/menu", "GET");
        } catch (APISecurityException e) {
            if (e.getMessage().contains("Error while accessing backend services for API key validation")) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }
        }

    }

    @Test
    public void testGetUriTemplates() throws Exception {
        wireMockRule = new WireMockRule(wireMockConfiguration);
        wireMockRule.stubFor(WireMock.post(urlEqualTo("/services/APIKeyValidationService")).withBasicAuth("admin",
                "admin").willReturn(aResponse().withBody("<soapenv:Envelope xmlns:soapenv=\"http://www" +
                ".w3.org/2003/05/soap-envelope\">\n" +
                "   <soapenv:Body>\n" +
                "      <ns:getAllURITemplatesResponse xmlns:ns=\"http://org.apache.axis2/xsd\" " +
                "xmlns:ax2125=\"http://keymgt.apimgt.carbon.wso2.org/xsd\" xmlns:ax2127=\"http://api.apimgt.carbon" +
                ".wso2.org/xsd\" xmlns:ax2131=\"http://model.api.apimgt.carbon.wso2.org/xsd\" " +
                "xmlns:ax2129=\"http://dto.impl.apimgt.carbon.wso2.org/xsd\" xmlns:ax2132=\"http://dto.api.apimgt" +
                ".carbon.wso2.org/xsd\">\n" +
                "         <ns:return xsi:type=\"ax2131:URITemplate\" xmlns:xsi=\"http://www" +
                ".w3.org/2001/XMLSchema-instance\">\n" +
                "            <ax2131:HTTPVerb>GET</ax2131:HTTPVerb>\n" +
                "            <ax2131:aggregatedMediationScript>null</ax2131:aggregatedMediationScript>\n" +
                "            <ax2131:applicableLevel>apiLevel</ax2131:applicableLevel>\n" +
                "            <ax2131:authType>Any</ax2131:authType>\n" +
                "            <ax2131:authTypeAsString/>\n" +
                "            <ax2131:authTypes>Any</ax2131:authTypes>\n" +
                "            <ax2131:conditionGroups xsi:type=\"ax2132:ConditionGroupDTO\">\n" +
                "               <ax2132:conditionGroupId>_default</ax2132:conditionGroupId>\n" +
                "               <ax2132:conditions xsi:nil=\"true\"/>\n" +
                "            </ax2131:conditionGroups>\n" +
                "            <ax2131:httpVerbs>GET</ax2131:httpVerbs>\n" +
                "            <ax2131:mediationScript xsi:nil=\"true\"/>\n" +
                "            <ax2131:methodsAsString/>\n" +
                "            <ax2131:resourceMap>{}</ax2131:resourceMap>\n" +
                "            <ax2131:resourceSandboxURI xsi:nil=\"true\"/>\n" +
                "            <ax2131:resourceSandboxURIExist>false</ax2131:resourceSandboxURIExist>\n" +
                "            <ax2131:resourceURI xsi:nil=\"true\"/>\n" +
                "            <ax2131:resourceURIExist>false</ax2131:resourceURIExist>\n" +
                "            <ax2131:scope xsi:nil=\"true\"/>\n" +
                "            <ax2131:scopes xsi:nil=\"true\"/>\n" +
                "            <ax2131:throttlingConditions>_default</ax2131:throttlingConditions>\n" +
                "            <ax2131:throttlingConditionsAsString>_default</ax2131:throttlingConditionsAsString>\n" +
                "            <ax2131:throttlingTier>Unlimited</ax2131:throttlingTier>\n" +
                "            <ax2131:throttlingTiers>Unlimited</ax2131:throttlingTiers>\n" +
                "            <ax2131:throttlingTiersAsString/>\n" +
                "            <ax2131:uriTemplate>/menu</ax2131:uriTemplate>\n" +
                "         </ns:return>\n" +
                "      </ns:getAllURITemplatesResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>").withStatus(200)));
        wireMockRule.start();
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (apiManagerConfiguration));
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn
                ("https://localhost:" + 8082 + "/services/");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn
                ("admin");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn
                ("admin");
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Map headers = new HashMap();
        Mockito.when(messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        MessageContext.setCurrentMessageContext(messageContext);
        APIKeyValidatorClient apiKeyValidatorClient = new APIKeyValidatorClientWrapper();
        List<URITemplate> uriTemplates = apiKeyValidatorClient.getAllURITemplates("/pizzashack/1.0.0", "1.0.0");
        wireMockRule.resetAll();
        wireMockRule.stop();
        Assert.assertEquals(uriTemplates.size(), 1);
        URITemplate uriTemplate = uriTemplates.get(0);
        Assert.assertNotNull(uriTemplate);
        Assert.assertEquals(uriTemplate.getUriTemplate(),"/menu");
        Assert.assertEquals(uriTemplate.getHTTPVerb(),"GET");
        Assert.assertEquals(uriTemplate.getThrottlingTier(),"Unlimited");
        Assert.assertEquals(uriTemplate.getAuthType(),"Any");
    }
    @Test
    public void testGetUriTemplatesWhileKeyValidationCallgetFailed() throws Exception {
        wireMockRule = new WireMockRule(wireMockConfiguration);
        wireMockRule.stubFor(WireMock.post(urlEqualTo("/services/APIKeyValidationService")).withBasicAuth("admin",
                "admin").willReturn(aResponse().withBody("<soapenv:Envelope xmlns:soapenv=\"http://www" +
                ".w3.org/2003/05/soap-envelope\">\n" +
                "   <soapenv:Body>\n" +
                "      <ns:getAllURITemplatesResponse xmlns:ns=\"http://org.apache.axis2/xsd\" " +
                "xmlns:ax2125=\"http://keymgt.apimgt.carbon.wso2.org/xsd\" xmlns:ax2127=\"http://api.apimgt.carbon" +
                ".wso2.org/xsd\" xmlns:ax2131=\"http://model.api.apimgt.carbon.wso2.org/xsd\" " +
                "xmlns:ax2129=\"http://dto.impl.apimgt.carbon.wso2.org/xsd\" xmlns:ax2132=\"http://dto.api.apimgt" +
                ".carbon.wso2.org/xsd\">\n" +
                "         <ns:return xsi:type=\"ax2131:URITemplate\" xmlns:xsi=\"http://www" +
                ".w3.org/2001/XMLSchema-instance\">\n" +
                "            <ax2131:HTTPVerb>GET</ax2131:HTTPVerb>\n" +
                "            <ax2131:aggregatedMediationScript>null</ax2131:aggregatedMediationScript>\n" +
                "            <ax2131:applicableLevel>apiLevel</ax2131:applicableLevel>\n" +
                "            <ax2131:authType>Any</ax2131:authType>\n" +
                "            <ax2131:authTypeAsString/>\n" +
                "            <ax2131:authTypes>Any</ax2131:authTypes>\n" +
                "            <ax2131:conditionGroups xsi:type=\"ax2132:ConditionGroupDTO\">\n" +
                "               <ax2132:conditionGroupId>_default</ax2132:conditionGroupId>\n" +
                "               <ax2132:conditions xsi:nil=\"true\"/>\n" +
                "            </ax2131:conditionGroups>\n" +
                "            <ax2131:httpVerbs>GET</ax2131:httpVerbs>\n" +
                "            <ax2131:mediationScript xsi:nil=\"true\"/>\n" +
                "            <ax2131:methodsAsString/>\n" +
                "            <ax2131:resourceMap>{}</ax2131:resourceMap>\n" +
                "            <ax2131:resourceSandboxURI xsi:nil=\"true\"/>\n" +
                "            <ax2131:resourceSandboxURIExist>false</ax2131:resourceSandboxURIExist>\n" +
                "            <ax2131:resourceURI xsi:nil=\"true\"/>\n" +
                "            <ax2131:resourceURIExist>false</ax2131:resourceURIExist>\n" +
                "            <ax2131:scope xsi:nil=\"true\"/>\n" +
                "            <ax2131:scopes xsi:nil=\"true\"/>\n" +
                "            <ax2131:throttlingConditions>_default</ax2131:throttlingConditions>\n" +
                "            <ax2131:throttlingConditionsAsString>_default</ax2131:throttlingConditionsAsString>\n" +
                "            <ax2131:throttlingTier>Unlimited</ax2131:throttlingTier>\n" +
                "            <ax2131:throttlingTiers>Unlimited</ax2131:throttlingTiers>\n" +
                "            <ax2131:throttlingTiersAsString/>\n" +
                "            <ax2131:uriTemplate>/menu</ax2131:uriTemplate>\n" +
                "         </ns:return>\n" +
                "      </ns:getAllURITemplatesResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>").withStatus(200)));
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (apiManagerConfiguration));
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn
                ("https://localhost:" + 8082 + "/services/");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn
                ("admin");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn
                ("admin");
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Map headers = new HashMap();
        Mockito.when(messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        MessageContext.setCurrentMessageContext(messageContext);
        APIKeyValidatorClient apiKeyValidatorClient = new APIKeyValidatorClientWrapper();
        try {
            List<URITemplate> uriTemplates = apiKeyValidatorClient.getAllURITemplates("/pizzashack/1.0.0", "1.0.0");
        } catch (APISecurityException e) {
            if (e.getMessage().contains("Error while accessing backend services for API key validation")) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }
        }
    }
    @Test
    public void testConfigurationValuesNotGiven() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (apiManagerConfiguration));
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Map headers = new HashMap();
        Mockito.when(messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        MessageContext.setCurrentMessageContext(messageContext);
        try {
            APIKeyValidatorClient apiKeyValidatorClient = new APIKeyValidatorClientWrapper();
        } catch (APISecurityException e) {
            if (e.getMessage().contains("Required connection details for the key management server not provided")) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }
        }
    }

}