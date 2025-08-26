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
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.api.model.WorkflowTaskService;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.DefaultWorkflowTaskService;
import org.wso2.carbon.context.CarbonContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, ApiMgtDAO.class, APIUtil.class, CarbonContext.class})
public class APIAdminImplTest {

    private static final String azureADKeyManagerType = "AzureAD";
    private static final String azureADKeyManagerName = "Azure AD Key Manager";
    private static final String directKeyManagerTokenType = "DIRECT";
    private static final String azureADDefaultTokenVersion = "v1.0";
    private static final String azureADAlternateTokenVersion = "v2.0";

    ServiceReferenceHolder serviceReferenceHolder;
    APIMConfigService apimConfigService;

    ApiMgtDAO apiMgtDAO;

    @Before
    public void setup() {

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(CarbonContext.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
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
    public void getPolicyByNameAndType() throws Exception {
        APIPolicy apiPolicy = Mockito.mock(APIPolicy.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
        APIAdmin apiAdmin = new APIAdminImpl();
        Mockito.when(apiMgtDAO.getAPIPolicy(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenReturn(apiPolicy);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(throttleProperties.isEnableUnlimitedTier()).thenReturn(true);

        try {
            Policy policy = apiAdmin.getPolicyByNameAndType(ArgumentMatchers.anyInt(), "api", ArgumentMatchers.anyString());
            Assert.assertNotNull(policy);
        } catch (APIManagementException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testWorkflowDefaultPendingList() throws Exception {
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        WorkflowProperties workflowProperties = Mockito.mock(WorkflowProperties.class);
        Mockito.when(apiManagerConfiguration.getWorkflowProperties()).thenReturn(workflowProperties);
        Mockito.when(workflowProperties.isListTasks()).thenReturn(true);

        WorkflowTaskService task = new DefaultWorkflowTaskService();
        Mockito.when(serviceReferenceHolder.getWorkflowTaskService()).thenReturn(task);
        String workflowType = "AM_APPLICATION_CREATION";
        String status = "CREATED";
        String tenantDomain = "carbon.super";
        String reference = "1234";

        Workflow[] pendingTasks = new Workflow[1];
        Workflow pendingTask = new Workflow();
        pendingTask.setExternalWorkflowReference(reference);
        pendingTasks[0] = pendingTask;
        Mockito.when(apiMgtDAO.getworkflows(workflowType, status, tenantDomain)).thenReturn(pendingTasks);


        CarbonContext context = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(context);

        APIAdmin apiAdmin = new APIAdminImpl();
        Workflow[] returnedWorkflows = apiAdmin.getworkflows(workflowType, status, tenantDomain);
        Assert.assertNotNull("Workflow array null", returnedWorkflows);
        Assert.assertEquals("Workflow array length mismatch", 1, returnedWorkflows.length);
        Assert.assertEquals("Invalid workflow", reference, returnedWorkflows[0].getExternalWorkflowReference());

    }
    
    @Test
    public void testWorkflowPendingListWithListingDisabled() throws Exception {
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        WorkflowProperties workflowProperties = Mockito.mock(WorkflowProperties.class);
        Mockito.when(apiManagerConfiguration.getWorkflowProperties()).thenReturn(workflowProperties);
        Mockito.when(workflowProperties.isListTasks()).thenReturn(false);

        String workflowType = "AM_APPLICATION_CREATION";
        String status = "CREATED";
        String tenantDomain = "carbon.super";

        APIAdmin apiAdmin = new APIAdminImpl();
        Workflow[] returnedWorkflows = apiAdmin.getworkflows(workflowType, status, tenantDomain);
        Assert.assertNotNull("Workflow array null", returnedWorkflows);
        Assert.assertEquals("Workflow array length mismatch", 0, returnedWorkflows.length);


    }

    @Test
    public void testKeyManagerConnectorConfigurationUpdateDisabled() throws APIManagementException {
        // Setup test data
        String configKey = "azure_ad_requested_access_token_version";

        // Prepare the ConfigurationDto list
        Map<String, Integer> map = new HashMap<>();
        map.put(azureADDefaultTokenVersion, 1);
        map.put(azureADAlternateTokenVersion, 2);
        List<ConfigurationDto> configurationDtoList = new ArrayList<>();
        configurationDtoList.add(new ConfigurationDto(configKey,
                "Requested Access Token Version", "options",
                "Select the requested access token version", azureADDefaultTokenVersion,
                false, false,
                new ArrayList<>(Collections.unmodifiableMap(map).keySet()),
                false, true));

        // Mock the chain: serviceReferenceHolder.getKeyManagerConnectorConfiguration(...)
        KeyManagerConnectorConfiguration keyManagerConnectorConfiguration =
                Mockito.mock(KeyManagerConnectorConfiguration.class);
        Mockito.when(serviceReferenceHolder.getKeyManagerConnectorConfiguration(azureADKeyManagerType))
                .thenReturn(keyManagerConnectorConfiguration);
        Mockito.when(keyManagerConnectorConfiguration.getConnectionConfigurations())
                .thenReturn(configurationDtoList);

        // Initialize API admin implementation
        APIAdminImpl apiAdmin = new APIAdminImpl();

        // Test scenario 1: No changes to configuration non-modifiable field
        try {
            apiAdmin.validateKeyManagerConfiguration(
                // Both do not have the field
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(),
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion()
            );
            apiAdmin.validateKeyManagerConfiguration(
                // Both have the default version
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADDefaultTokenVersion),
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADDefaultTokenVersion)
            );
            apiAdmin.validateKeyManagerConfiguration(
                // Both have the alternative version
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADAlternateTokenVersion),
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADAlternateTokenVersion)
            );
        } catch (APIManagementException e) {
            Assert.fail("Validation should succeed when configuration is unchanged");
        }

        // Test scenario 2: Attempt to modify a non-modifiable field
        try {
            apiAdmin.validateKeyManagerConfiguration(
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADDefaultTokenVersion),
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADAlternateTokenVersion)
            );
            Assert.fail("Expected APIManagementException for changing configuration");
        } catch (APIManagementException e) {
            assertUpdateDisabledKeyManagerConfigurationModification(e);
        }

        try {
            apiAdmin.validateKeyManagerConfiguration(
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADAlternateTokenVersion),
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADDefaultTokenVersion)
            );
            Assert.fail("Expected APIManagementException for changing configuration");
        } catch (APIManagementException e) {
            assertUpdateDisabledKeyManagerConfigurationModification(e);
        }

        try {
            apiAdmin.validateKeyManagerConfiguration(
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion("v3.0"),
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion("v4.0")
            );
            Assert.fail("Expected APIManagementException for changing configuration");
        } catch (APIManagementException e) {
            assertUpdateDisabledKeyManagerConfigurationModification(e);
        }

        // Test scenario 3: Original is null, new is default value (should be allowed)
        try {
            apiAdmin.validateKeyManagerConfiguration(
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADDefaultTokenVersion),
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion()
            );
        } catch (APIManagementException e) {
            Assert.fail("Validation should succeed when setting to default value");
        }

        // Test scenario 4: Original is null, new is non-default value (should be rejected)
        try {
            apiAdmin.validateKeyManagerConfiguration(
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADAlternateTokenVersion),
                createAzureADKeyManagerConfigWithRequestedAccessTokenVersion()
            );
            Assert.fail("Expected APIManagementException for setting non-default value");
        } catch (APIManagementException e) {
            assertUpdateDisabledKeyManagerConfigurationModification(e);
        }
    }

    /**
     * Creates a base key manager configuration with standard settings
     */
    private KeyManagerConfigurationDTO createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(String tokenVersion)
    {
        KeyManagerConfigurationDTO config = new KeyManagerConfigurationDTO();
        config.setName(azureADKeyManagerType);
        config.setDisplayName(azureADKeyManagerName);
        config.setDescription("");
        config.setOrganization("carbon.super");
        config.setType(azureADKeyManagerType);
        config.setEnabled(true);
        config.setTokenType(directKeyManagerTokenType);
        config.setAlias("");

        Map<String, Object> additionalProps = new HashMap<>();
        if (tokenVersion != null) {
            additionalProps.put("azure_ad_requested_access_token_version", tokenVersion);
        }
        config.setAdditionalProperties(additionalProps);

        return config;
    }

    /**
     * Creates a base key manager configuration with standard settings
     */
    private KeyManagerConfigurationDTO createAzureADKeyManagerConfigWithRequestedAccessTokenVersion()
    {
        KeyManagerConfigurationDTO config = new KeyManagerConfigurationDTO();
        config.setName(azureADKeyManagerType);
        config.setDisplayName(azureADKeyManagerName);
        config.setDescription("");
        config.setOrganization("carbon.super");
        config.setType(azureADKeyManagerType);
        config.setEnabled(true);
        config.setTokenType(directKeyManagerTokenType);
        config.setAlias("");

        Map<String, Object> additionalProps = new HashMap<>();
        config.setAdditionalProperties(additionalProps);

        return config;
    }

    /**
     * Asserts that the provided exception contains the expected message and error handler for
     * a required Key Manager configuration missing scenario.
     *
     * @param exception The exception to check.
     */
    private void assertUpdateDisabledKeyManagerConfigurationModification(APIManagementException exception) {
        Assert.assertTrue(exception.getMessage().contains("Modification of the Key Manager configuration"));
        Assert.assertEquals(ExceptionCodes.KEY_MANAGER_UPDATE_VIOLATION, exception.getErrorHandler());
    }
}
