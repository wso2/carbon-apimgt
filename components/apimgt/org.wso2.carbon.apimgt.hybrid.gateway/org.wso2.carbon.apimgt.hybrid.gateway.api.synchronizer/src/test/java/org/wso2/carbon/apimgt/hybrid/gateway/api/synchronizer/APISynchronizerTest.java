/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.constants.Constants;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.util.APIMappingUtil;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.internal.ServiceDataHolder;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dao.OnPremiseGatewayDAO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.AccessTokenDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.OAuthApplicationInfoDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.HttpRequestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.TokenUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Matchers.any;

/**
 * APISynchronizer Test Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonUtils.class, APIManagerConfiguration.class,
        ServiceReferenceHolder.class, APIManagerConfigurationService.class, TokenUtil.class, HttpClients.class,
        HttpRequestUtil.class, APIMappingUtil.class, ServiceDataHolder.class, RealmService.class,
        PrivilegedCarbonContext.class, TenantAxisUtils.class, APIUtil.class, ConfigManager.class
})
public class APISynchronizerTest {
    public static final String updatedApis = "updated-apis";
    public static final String mediationPolicyInfo = "mediation-policy-info";
    public static final String mediationPolicies = "mediation-policies";
    public static final String weatherApiInfo = "api_yahoo-weather";
    public static final String phoneVerificationApiInfo = "api_phone-verification";
    public static final String allApis = "all-apis";

    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test
    public void synchronizeApis() throws Exception {
        mockCommonCases();
        registerClient();
        generateAccessToken();
        executeHTTPMethodWithRetry();
        APISynchronizer synchronizer = new APISynchronizer();
        PowerMockito.mockStatic(ConfigManager.class);
        ConfigManager configManager = Mockito.mock(ConfigManager.class);
        PowerMockito.when(ConfigManager.getConfigManager()).thenReturn(configManager);
        PowerMockito.mockStatic(APIUtil.class);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        PowerMockito.when(APIUtil.getHttpClient(Mockito.anyInt(), Mockito.anyString())).thenReturn(httpClient);
        synchronizer.synchronizeApis(null);
    }


    @Test(expected = Exception.class)
    public void synchronizeApis_throwsExceptionDuringClientRegistration() throws Exception {
        mockCommonCases();
        registerClient_throwsException();
        generateAccessToken();
        executeHTTPMethodWithRetry();
        APISynchronizer synchronizer = new APISynchronizer();
        synchronizer.synchronizeApis(null);
    }


    @Test(expected = Exception.class)
    public void synchronizeApis_throwsExceptionDuringAccessTokenGeneration() throws Exception {
        mockCommonCases();
        registerClient();
        generateAccessToken_ThrowsException();
        executeHTTPMethodWithRetry();
        APISynchronizer synchronizer = new APISynchronizer();
        synchronizer.synchronizeApis(null);
    }

    @Test
    public void updateApis() throws Exception {
        mockCommonCases();
        registerClient();
        generateAccessToken();
        Map<String, String> testData = getTestData();
        PowerMockito.mockStatic(HttpClients.class);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        PowerMockito.mockStatic(ConfigManager.class);
        ConfigManager configManager = Mockito.mock(ConfigManager.class);
        PowerMockito.when(ConfigManager.getConfigManager()).thenReturn(configManager);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getHttpClient(Mockito.anyInt(), Mockito.anyString())).thenReturn(httpClient);
        PowerMockito.mockStatic(HttpRequestUtil.class);
        PowerMockito.when(HttpRequestUtil.executeHTTPMethodWithRetry(any(HttpClient.class), any(HttpGet.class),
                any(Integer.class))).thenReturn(testData.get(updatedApis),
                testData.get(weatherApiInfo), testData.get(mediationPolicies),
                testData.get(mediationPolicyInfo));
        APISynchronizer synchronizer = new APISynchronizer();
        synchronizer.updateApis();
    }

    public void mockCommonCases() throws UserStoreException {
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.mockStatic(TenantAxisUtils.class);

        String carbonConfigPath = System.getProperty(Constants.CARBON_HOME) + Constants.CARBON_CONFIGS_PATH;
        String tenantDirPath = System.getProperty(Constants.CARBON_HOME) + Constants.CARBON_TENANT_CONFIGS_PATH;
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonTenantsDirPath()).thenReturn(tenantDirPath);
        PowerMockito.when(CarbonUtils.getCarbonConfigDirPath()).thenReturn(carbonConfigPath);

        PowerMockito.mockStatic(TokenUtil.class);
        PowerMockito.mockStatic(APIMappingUtil.class);
        Mockito.mock(OnPremiseGatewayDAO.class);

        PowerMockito.mockStatic(RealmService.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        ServiceDataHolder serviceDataHolder = Mockito.mock(ServiceDataHolder.class);

        PowerMockito.mockStatic(ServiceDataHolder.class);
        PowerMockito.when(ServiceDataHolder.getInstance()).thenReturn(serviceDataHolder);
        PowerMockito.when(serviceDataHolder.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.when(tenantManager.getTenantId(any(String.class))).thenReturn(1);

        ConfigurationContextService contextService = Mockito.mock(ConfigurationContextService.class);
        ConfigurationContext context = Mockito.mock(ConfigurationContext.class);
        PowerMockito.when(serviceDataHolder.getConfigurationContextService()).thenReturn(contextService);
        PowerMockito.when(contextService.getServerConfigContext()).thenReturn(context);

        Properties properties = Mockito.mock(Properties.class);
        Mockito.when(properties.getProperty(Constants.API_PUBLISHER_URL_PROPERTY))
                .thenReturn(Constants.DEFAULT_API_PUBLISHER_URL);
        Mockito.when(properties.getProperty(Constants.DEFAULT_API_UPDATE_URL_PROPERTY))
                .thenReturn(Constants.DEFAULT_API_UPDATE_SERVICE_URL);
        APIManagerConfigurationService apimConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apimConfig = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceDataHolder.getInstance()).thenReturn(serviceDataHolder);
        Mockito.when(serviceDataHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigService);
        Mockito.when(apimConfigService.getAPIManagerConfiguration()).thenReturn(apimConfig);
        Mockito.when(apimConfig.getFirstProperty(Constants.KEY_VALIDATOR_USERNAME))
                .thenReturn(Constants.USERNAME);
        Mockito.when(apimConfig.getFirstProperty(Constants.KEY_VALIDATOR_PASSWORD))
                .thenReturn(Constants.PASSWORD);
    }

    public void registerClient() throws Exception {
        OAuthApplicationInfoDTO oAuthDto = Mockito.mock(OAuthApplicationInfoDTO.class);
        PowerMockito.when(TokenUtil.registerClient()).thenReturn(oAuthDto);
        Mockito.doReturn(Constants.CLIENT_ID).when(oAuthDto).getClientId();
        Mockito.doReturn(Constants.CLIENT_SECRET).when(oAuthDto).getClientSecret();
    }

    public void registerClient_throwsException() throws Exception {
        OAuthApplicationInfoDTO oAuthDto = Mockito.mock(OAuthApplicationInfoDTO.class);
        PowerMockito.when(TokenUtil.registerClient()).thenThrow(OnPremiseGatewayException.class);
        Mockito.doReturn(Constants.CLIENT_ID).when(oAuthDto).getClientId();
        Mockito.doReturn(Constants.CLIENT_SECRET).when(oAuthDto).getClientSecret();
    }

    public void generateAccessToken() throws Exception {
        AccessTokenDTO accessTknDTO = Mockito.mock(AccessTokenDTO.class);
        PowerMockito.when(TokenUtil.generateAccessToken(any(String.class), any(char[].class), any(String.class)))
                .thenReturn(accessTknDTO);
        Mockito.doReturn(Constants.ACCESS_TOKEN).when(accessTknDTO).getAccessToken();
    }

    public void generateAccessToken_ThrowsException() throws Exception {
        AccessTokenDTO accessTknDTO = Mockito.mock(AccessTokenDTO.class);
        PowerMockito.when(TokenUtil.generateAccessToken(any(String.class), any(char[].class), any(String.class)))
                .thenThrow(OnPremiseGatewayException.class);
        Mockito.doReturn(Constants.ACCESS_TOKEN).when(accessTknDTO).getAccessToken();
    }

    public void executeHTTPMethodWithRetry() throws Exception {
        Map<String, String> testData = getTestData();
        PowerMockito.mockStatic(HttpClients.class);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        PowerMockito.mockStatic(HttpRequestUtil.class);
        PowerMockito.when(HttpRequestUtil.executeHTTPMethodWithRetry(any(HttpClient.class), any(HttpGet.class),
                any(Integer.class))).thenReturn(testData.get(allApis), testData.get(phoneVerificationApiInfo),
                testData.get(weatherApiInfo), testData.get(mediationPolicies),
                testData.get(mediationPolicyInfo));
    }

    public Map<String, String> getTestData() {
        Map<String, String> testData = new HashMap<>();

        String allAPIs = "{\"count\":2,\"next\":\"\",\"previous\":\"\"," +
                "\"list\":[{\"id\":\"23cd9301-d50b-449b-b247-ff4752f3b195\"," +
                "\"name\":\"PhoneVerification\",\"description\":null,\"context\":\"/t/ccc2222/phoneverify\"," +
                "\"version\":\"1.0.0\",\"provider\":\"8jona1j@googl.igg.biz@ccc2222\",\"status\":\"PUBLISHED\"," +
                "\"thumbnailUri\":null},{\"id\":\"7dfe9e1c-bbfd-47ef-8630-ea03129cb801\",\"name\":\"YahooWeather\"," +
                "\"description\":null,\"context\":\"/t/ccc2222/weather\",\"version\":\"1.0\"," +
                "\"provider\":\"8jona1j@googl.igg.biz@ccc2222\",\"status\":\"PUBLISHED\",\"thumbnailUri\":null}]," +
                "\"pagination\":{\"total\":2,\"offset\":0,\"limit\":25}}";

        String apiInfo1 = "{\"id\":\"23cd9301-d50b-449b-b247-ff4752f3b195\",\"name\":\"PhoneVerification\"," +
                "\"description\":null,\"context\":\"/t/ccc2222/phoneverify\",\"version\":\"1.0.0\"," +
                "\"provider\":\"8jona1j@googl.igg.biz@ccc2222\",\"apiDefinition\":\"{\\\"swagger\\\":\\\"2.0\\\"," +
                "\\\"paths\\\":{\\\"/CheckPhoneNumber\\\":{\\\"get\\\":{\\\"responses\\\":{\\\"200\\\":" +
                "{\\\"description\\\":\\\"\\\"}},\\\"parameters\\\":[{\\\"name\\\":\\\"PhoneNumber\\\"," +
                "\\\"in\\\":\\\"query\\\",\\\"required\\\":true,\\\"type\\\":\\\"string\\\"},{\\\"name\\\":" +
                "\\\"LicenseKey\\\",\\\"in\\\":\\\"query\\\",\\\"required\\\":true,\\\"type\\\":\\\"string\\\"}]," +
                "\\\"x-auth-type\\\":\\\"Application & Application User\\\",\\\"x-throttling-tier\\\":" +
                "\\\"Unlimited\\\"},\\\"post\\\":{\\\"responses\\\":{\\\"200\\\":{\\\"description\\\":\\\"\\\"}}," +
                "\\\"parameters\\\":[{\\\"name\\\":\\\"Payload\\\",\\\"description\\\":\\\"Request Body\\\"," +
                "\\\"required\\\":false,\\\"in\\\":\\\"body\\\",\\\"schema\\\":{\\\"type\\\":\\\"object\\\"," +
                "\\\"properties\\\":{\\\"payload\\\":{\\\"type\\\":\\\"string\\\"}}}}],\\\"x-auth-type\\\":\\\"" +
                "Application & Application User\\\",\\\"x-throttling-tier\\\":\\\"Unlimited\\\"}}},\\\"info\\\":" +
                "{\\\"title\\\":\\\"PhoneVerification\\\",\\\"version\\\":\\\"1.0.0\\\"}}\",\"wsdlUri\":null," +
                "\"status\":\"PUBLISHED\",\"responseCaching\":\"Disabled\",\"cacheTimeout\":300," +
                "\"destinationStatsEnabled\":null,\"isDefaultVersion\":false,\"type\":\"HTTP\",\"transport\":" +
                "[\"http\",\"https\"],\"tags\":[],\"tiers\":[\"Gold\",\"Silver\",\"Unlimited\"]," +
                "\"maxTps\":null,\"thumbnailUri\":null,\"visibility\":\"PRIVATE\",\"visibleRoles\":[]," +
                "\"accessControl\":\"NONE\",\"accessControlRoles\":[],\"visibleTenants\":[],\"endpointConfig\":" +
                "\"{\\\"production_endpoints\\\":[{\\\"url\\\":\\\"" +
                "http://ws.cdyne.com/phoneverify/phoneverify.asmx\\\",\\\"config\\\":null," +
                "\\\"template_not_supported\\\":false}],\\\"endpoint_type\\\":\\\"load_balance\\\",\\\"algoCombo\\\":" +
                "\\\"org.apache.synapse.endpoints.algorithms.RoundRobin\\\",\\\"algoClassName\\\":" +
                "\\\"org.apache.synapse.endpoints.algorithms.RoundRobin\\\",\\\"sessionManagement\\\":\\\"\\\"," +
                "\\\"sessionTimeOut\\\":\\\"100\\\"}\",\"endpointSecurity\":{\"username\":" +
                "\"8jona1j@googl.igg.biz@ccc2222\",\"type\":\"basic\",\"password\":\"Amanda12\"}," +
                "\"gatewayEnvironments\":\"Production and Sandbox\",\"sequences\":[]," +
                "\"subscriptionAvailability\":null,\"subscriptionAvailableTenants\":[]," +
                "\"businessInformation\":{\"businessOwnerEmail\":\"amandaj@wso2.com\"," +
                "\"technicalOwnerEmail\":\"amanda.12@cse.mrt.ac.lk\",\"technicalOwner\":\"Amanda\"," +
                "\"businessOwner\":\"Amanda\"},\"corsConfiguration\":{\"accessControlAllowOrigins\":[]," +
                "\"accessControlAllowHeaders\":[\"authorization\",\"Access-Control-Allow-Origin\",\"Content-Type\"," +
                "\"SOAPAction\",\"X-Authorization\"],\"accessControlAllowMethods\":[\"DELETE\",\"PATCH\"," +
                "\"OPTIONS\"],\"accessControlAllowCredentials\":true,\"corsConfigurationEnabled\":true}}";

        String apiInfo2 = "{\"id\":\"7dfe9e1c-bbfd-47ef-8630-ea03129cb801\",\"name\":\"YahooWeather\"," +
                "\"description\":null,\"context\":\"/t/ccc2222/weather\",\"version\":\"1.0\"," +
                "\"provider\":\"8jona1j@googl.igg.biz@ccc2222\",\"apiDefinition\":\"{\\\"swagger\\\":\\\"2.0\\\"," +
                "\\\"paths\\\":{\\\"/current/{country}/{zipcode}\\\":{\\\"get\\\":{\\\"responses\\\":" +
                "{\\\"200\\\":{\\\"description\\\":\\\"\\\"}},\\\"parameters\\\":[{\\\"name\\\":" +
                "\\\"country\\\",\\\"in\\\":\\\"path\\\",\\\"allowMultiple\\\":false,\\\"required\\\":true," +
                "\\\"type\\\":\\\"string\\\"},{\\\"name\\\":\\\"zipcode\\\",\\\"in\\\":\\\"path\\\"," +
                "\\\"allowMultiple\\\":false,\\\"required\\\":true,\\\"type\\\":\\\"string\\\"}]," +
                "\\\"x-auth-type\\\":\\\"Application & Application User\\\",\\\"x-throttling-tier\\\":" +
                "\\\"Unlimited\\\"}}},\\\"info\\\":{\\\"title\\\":\\\"YahooWeather\\\"," +
                "\\\"version\\\":\\\"1.0\\\"}}\",\"wsdlUri\":null,\"status\":\"PUBLISHED\"," +
                "\"responseCaching\":\"Disabled\",\"cacheTimeout\":300,\"destinationStatsEnabled\":null," +
                "\"isDefaultVersion\":false,\"type\":\"HTTP\",\"transport\":[\"http\",\"https\"],\"tags\":[]," +
                "\"tiers\":[\"Gold\",\"Silver\"],\"maxTps\":null,\"thumbnailUri\":null,\"visibility\":\"PUBLIC\"," +
                "\"visibleRoles\":[],\"accessControl\":\"NONE\",\"accessControlRoles\":[],\"visibleTenants\":[]," +
                "\"endpointConfig\":\"{\\\"production_endpoints\\\":{\\\"url\\\":" +
                "\\\"https://query.yahooapis.com/v1/public/yql\\\",\\\"config\\\":null," +
                "\\\"template_not_supported\\\":false},\\\"sandbox_endpoints\\\":{\\\"url\\\":" +
                "\\\"https://query.yahooapis.com/v1/public/yql\\\",\\\"config\\\":null," +
                "\\\"template_not_supported\\\":false},\\\"endpoint_type\\\":\\\"http\\\"," +
                "\\\"sandbox_endpoint\\\":{\\\"url\\\":\\\"null\\\",\\\"config\\\":null}}\"," +
                "\"endpointSecurity\":null,\"gatewayEnvironments\":\"Production and Sandbox\"," +
                "\"sequences\":[{\"name\":\"YahooWeatherSequence\",\"type\":\"in\"," +
                "\"id\":\"60ca1788-d866-4f87-908a-f19c1f5d2510\",\"shared\":false}]," +
                "\"subscriptionAvailability\":\"current_tenant\",\"subscriptionAvailableTenants\":[]," +
                "\"businessInformation\":{\"businessOwnerEmail\":null,\"technicalOwnerEmail\":null," +
                "\"technicalOwner\":null,\"businessOwner\":null},\"corsConfiguration\":" +
                "{\"accessControlAllowOrigins\":[\"*\"],\"accessControlAllowHeaders\":[\"authorization\"," +
                "\"Access-Control-Allow-Origin\",\"Content-Type\",\"SOAPAction\",\"X-Authorization\"]," +
                "\"accessControlAllowMethods\":[\"GET\",\"PUT\",\"POST\",\"DELETE\",\"PATCH\",\"OPTIONS\"]," +
                "\"accessControlAllowCredentials\":false,\"corsConfigurationEnabled\":false}}";

        String mediationPoliciesList = "{\"count\":1,\"next\":null,\"previous\":null,\"list\":" +
                "[{\"name\":\"YahooWeatherSequence\",\"id\":\"eef4daa5-2369-4fcb-b1ae-ae0f9ffdd0a8\"," +
                "\"type\":\"in\"}]}";

        String mediationPolicyInfoList = "{\"id\":\"eef4daa5-2369-4fcb-b1ae-ae0f9ffdd0a8\"," +
                "\"name\":\"YahooWeatherSequence\",\"type\":\"in\",\"config\":\"<?xml version=\\\"1.0\\\" " +
                "encoding=\\\"UTF-8\\\"?>\\n<sequence name=\\\"YahooWeatherSequence\\\" trace=\\\"disable\\\" " +
                "xmlns=\\\"http://ws.apache.org/ns/synapse\\\">\\n    " +
                "<property expression=\\\"" +
                "concat('?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20" +
                "(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22',syn:get-property('uri.var.zipcode')," +
                "',',syn:get-property('uri.var.country'),'%22)&amp;format=json')\\\" name=\\\"YQL\\\"" +
                " scope=\\\"default\\\" type=\\\"STRING\\\"/>\\n    <property expression=\\\"get-property('YQL')\\\" " +
                "name=\\\"REST_URL_POSTFIX\\\" scope=\\\"axis2\\\" type=\\\"STRING\\\"/>\\n</sequence>\\n\"}";

        String updatedAPIs = "{\"API_IDs\":[\"jane-AT-wso2.com-AT-onpremgw-YahooWeather-1.0\"]}";

        testData.put(allApis, allAPIs);
        testData.put(phoneVerificationApiInfo, apiInfo1);
        testData.put(weatherApiInfo, apiInfo2);
        testData.put(mediationPolicies, mediationPoliciesList);
        testData.put(mediationPolicyInfo, mediationPolicyInfoList);
        testData.put(updatedApis, updatedAPIs);
        return testData;
    }
}
