/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, ApiMgtDAO.class, APIUtil.class, LogFactory.class})
public class APIAdminImplTest {

    ServiceReferenceHolder serviceReferenceHolder;
    APIMConfigService apimConfigService;

    @Before
    public void setup() {

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        apimConfigService = Mockito.mock(APIMConfigService.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getApimConfigService()).thenReturn(apimConfigService);
    }

    @Test
    public void getTenantConfig() throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.when(apimConfigService.getTenantConfig("abc.com")).thenReturn("abcde");
        Assert.assertEquals(apiAdmin.getTenantConfig("abc.com"), "abcde");
        Mockito.verify(apimConfigService, Mockito.times(1)).getTenantConfig("abc.com");
    }

    @Test
    public void getTenantConfigException() throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.when(apimConfigService.getTenantConfig("abc.com")).thenThrow(APIManagementException.class);
        try {
            apiAdmin.getTenantConfig("abc.com");
            Assert.fail("Method successfully invoked");
        } catch (APIManagementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void updateTenantConfig() throws Exception {

        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());
        String tenantConf = FileUtils.readFileToString(siteConfFile);
        PowerMockito.doNothing().when(APIUtil.class, "validateRestAPIScopes", tenantConf);
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(schema);
        Mockito.doNothing().when(schema).validate(Mockito.any());
        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.doNothing().when(apimConfigService).updateTenantConfig("abc.com", tenantConf);
        apiAdmin.updateTenantConfig("abc.com", tenantConf);
    }

    // Schema not present
    @Test
    public void updateTenantConfigNegative1() throws Exception {

        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());
        String tenantConf = FileUtils.readFileToString(siteConfFile);
        PowerMockito.doNothing().when(APIUtil.class, "validateRestAPIScopes", tenantConf);
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(null);
        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.doNothing().when(apimConfigService).updateTenantConfig("abc.com", tenantConf);
        try {
            apiAdmin.updateTenantConfig("abc.com", tenantConf);
            Assert.fail("Method successfully invoked");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "tenant-config validation failure");
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.INTERNAL_ERROR);
        }
    }

    // invalid json
    @Test
    public void updateTenantConfigNegative2() throws Exception {

        String tenantConf = "{\"hello\"";
        PowerMockito.doNothing().when(APIUtil.class, "validateRestAPIScopes", tenantConf);
        APIAdmin apiAdmin = new APIAdminImpl();
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(schema);

        Mockito.doNothing().when(apimConfigService).updateTenantConfig("abc.com", tenantConf);
        try {
            apiAdmin.updateTenantConfig("abc.com", tenantConf);
            Assert.fail("Method successfully invoked");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "tenant-config validation failure");
        }
    }

    // valid json element missing
    @Test
    public void updateTenantConfigNegative3() throws Exception {

        String tenantConf = "{\"hello\":\"world\"}";
        PowerMockito.doNothing().when(APIUtil.class, "validateRestAPIScopes", tenantConf);
        APIAdmin apiAdmin = new APIAdminImpl();
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(schema);
        Mockito.doThrow(ValidationException.class).when(schema).validate(Mockito.any());
        Mockito.doNothing().when(apimConfigService).updateTenantConfig("abc.com", tenantConf);
        try {
            apiAdmin.updateTenantConfig("abc.com", tenantConf);
            Assert.fail("Method successfully invoked");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "tenant-config validation failure");
        }
    }

    @Test
    public void getTenantConfigSchema() throws Exception {

        APIAdmin apiAdmin = new APIAdminImpl();
        Schema schema = Mockito.mock(Schema.class);
        PowerMockito.when(APIUtil.class, "retrieveTenantConfigJsonSchema").thenReturn(schema);
        Assert.assertEquals(apiAdmin.getTenantConfigSchema("abc.com"), schema.toString());
    }

    @Test
    public void testAddKeyManagerConfiguration() throws Exception {

        ApiMgtDAOMockCreator daoMockHolder = new ApiMgtDAOMockCreator(1);
        ApiMgtDAO apiMgtDAO = daoMockHolder.getMock();

        APIAdmin apiAdmin = new APIAdminImpl();
        Map<String, Object> additionalProperties = new HashMap<>();
        Map<String, String> endpoints = new HashMap<>();

        endpoints.put("token_endpoint", "https://test.com/token");
        endpoints.put("revoke_endpoint", "https://test.com/revoke");
        endpoints.put("authorize_endpoint", "https://test.com/authorize");

        additionalProperties.put("issuer", "https://test.com");
        additionalProperties.put("consumer_key_claim", "client-id");
        additionalProperties.put("scopes_claim", "scp");
        additionalProperties.put("certificate_type", "JWKS");
        additionalProperties.put("certificate_value", "https://test.com/jwks");

        KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
        keyManagerConfigurationDTO.setEnabled(true);
        keyManagerConfigurationDTO.setTokenType("EXTERNAL");
        keyManagerConfigurationDTO.setType("Auth0");
        keyManagerConfigurationDTO.setName("TestAuth0");
        keyManagerConfigurationDTO.setAdditionalProperties(additionalProperties);
        keyManagerConfigurationDTO.setOrganization("testOrg");
        keyManagerConfigurationDTO.setEndpoints(endpoints);

        PowerMockito.when(APIUtil.isValidURL(Mockito.anyString())).thenReturn(true);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        KeyManagerConfigurationService keyManagerConfigurationService =
                Mockito.mock(KeyManagerConfigurationService.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getKeyManagerConfigurationService())
                .thenReturn(keyManagerConfigurationService);
        APIManagerConfigurationService apiManagerConfigurationService =
                Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        EventHubConfigurationDto eventHubConfigurationDto = Mockito.mock(EventHubConfigurationDto.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getEventHubConfigurationDto()).thenReturn(eventHubConfigurationDto);
        Mockito.when(eventHubConfigurationDto.isEnabled()).thenReturn(false);
        KeyManagerConfigurationDTO  generatedKMConfigDTO =
                apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
        Assert.assertNotNull(generatedKMConfigDTO);
        Assert.assertEquals(generatedKMConfigDTO.getName(), "TestAuth0");
        Assert.assertEquals(generatedKMConfigDTO.getType(), "Auth0");
        Assert.assertEquals(generatedKMConfigDTO.getTokenType(), "EXTERNAL");
        Assert.assertEquals(generatedKMConfigDTO.getOrganization(), "testOrg");
        Assert.assertEquals(generatedKMConfigDTO.getAdditionalProperties().get("issuer"), "https://test.com");
        Assert.assertEquals(generatedKMConfigDTO.getAdditionalProperties().get("consumer_key_claim"), "client-id");
        Assert.assertEquals(generatedKMConfigDTO.getAdditionalProperties().get("scopes_claim"), "scp");
        Assert.assertEquals(generatedKMConfigDTO.getAdditionalProperties().get("certificate_type"), "JWKS");
        Assert.assertEquals(generatedKMConfigDTO.getAdditionalProperties().get("certificate_value"),
                "https://test.com/jwks");
        Assert.assertEquals(generatedKMConfigDTO.getEndpoints().get("token_endpoint"), "https://test.com/token");
        Assert.assertEquals(generatedKMConfigDTO.getEndpoints().get("revoke_endpoint"), "https://test.com/revoke");
        Assert.assertEquals(generatedKMConfigDTO.getEndpoints().get("authorize_endpoint"),
                "https://test.com/authorize");

        // Issuer is the first URL validated
        PowerMockito.when(APIUtil.isValidURL(Mockito.anyString())).thenReturn(false);
        try {
            apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            Assert.fail("Add key managers is supposed to be failed but it passed");
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Invalid JWKS URL is provided. Only valid URLs are supported");
        }
        PowerMockito.when(APIUtil.isValidURL(Mockito.anyString())).thenReturn(true);

        // Test for error message when additional properties are not present
        keyManagerConfigurationDTO.setAdditionalProperties(new HashMap<>());
        try {
            apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            Assert.fail("Add key managers is supposed to be failed but it passed due to missing properties");
        } catch (APIManagementException e) {
            String commonRegex = "issuer|consumer_key_claim|scopes_claim|certificate_type|certificate_value";
            Assert.assertTrue(e.getMessage().matches(String.format("Key Manager Configuration value for " +
                    "((%s),){4}(%s) is/are required", commonRegex, commonRegex)));
        }
        keyManagerConfigurationDTO.setAdditionalProperties(additionalProperties);

        // Test for error message when endpoints are not present
        keyManagerConfigurationDTO.setEndpoints(new HashMap<>());
        try {
            apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            Assert.fail("Add key managers is supposed to be failed but it passed due to missing endpoints");
        } catch (APIManagementException e) {
            String commonRegex = "token_endpoint|revoke_endpoint|authorize_endpoint";
            Assert.assertTrue(e.getMessage().matches(String.format("Key Manager Endpoint Configuration value for " +
                    "((%s),){2}(%s) is/are required", commonRegex, commonRegex)));
        }
        keyManagerConfigurationDTO.setEndpoints(endpoints);

        ArrayList<KeyManagerConfigurationDTO> keyManagerList3 = new ArrayList<>();
        KeyManagerConfigurationDTO existingKeyManagerConfigurationDTOBySameIssuer = new KeyManagerConfigurationDTO();
        existingKeyManagerConfigurationDTOBySameIssuer.setAdditionalProperties(additionalProperties);
        existingKeyManagerConfigurationDTOBySameIssuer.setEnabled(true);
        existingKeyManagerConfigurationDTOBySameIssuer.setTokenType("EXTERNAL");
        existingKeyManagerConfigurationDTOBySameIssuer.setType("Auth0");
        existingKeyManagerConfigurationDTOBySameIssuer.setName("TestAuth02");
        existingKeyManagerConfigurationDTOBySameIssuer.setAdditionalProperties(additionalProperties);
        existingKeyManagerConfigurationDTOBySameIssuer.setOrganization("testOrg");
        existingKeyManagerConfigurationDTOBySameIssuer.setEndpoints(endpoints);
        keyManagerList3.add(existingKeyManagerConfigurationDTOBySameIssuer);
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationsByOrganization(Mockito.anyString())).thenReturn(keyManagerList3);
        try {
            apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            Assert.fail("Add key managers is supposed to be failed but it passed as issuer is already added" );
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Issuer already exists for the organization testOrg");
        }
        Mockito.when(apiMgtDAO.getKeyManagerConfigurationsByOrganization(Mockito.anyString())).thenReturn(null);
    }
}
