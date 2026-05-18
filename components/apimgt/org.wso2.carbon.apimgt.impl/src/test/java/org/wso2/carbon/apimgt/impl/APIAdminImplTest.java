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
import org.powermock.reflect.Whitebox;
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
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;

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

        // Test scenario 5: Original is not default, new is non value (should be allowed)
        try {
            apiAdmin.validateKeyManagerConfiguration(
                    createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(),
                    createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(azureADAlternateTokenVersion)
            );
        } catch (APIManagementException e) {
            Assert.fail("Validation should succeed when original is non-default and new is not defined");
        }

        // Test scenario 6: Changing a modifiable field (should be allowed)
        try {
            KeyManagerConfigurationDTO updated = createAzureADKeyManagerConfigWithRequestedAccessTokenVersion();
            updated.getAdditionalProperties().put("azure_ad_client_id", "new-client-id");
            apiAdmin.validateKeyManagerConfiguration(
                    createAzureADKeyManagerConfigWithRequestedAccessTokenVersion(),
                    updated
            );
        } catch (APIManagementException e) {
            Assert.fail("Validation should succeed when modifiable field is updated");
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

    // ---------- Issue #4990: PEM cert was dropped on the IdP record (private createIdp / updatedIDP) ----------

    private static final String SAMPLE_PEM =
            "-----BEGIN CERTIFICATE-----\n" +
                    "MIIDazCCAlOgAwIBAgIUJ+abcDEFghIJklMNopQRsTUVwxYwDQYJKoZIhvcNAQEL\n" +
                    "BQAwRTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoM\n" +
                    "GEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDAeFw0yNjA1MDUwMDAwMDBaFw0yNzA1\n" +
                    "MDUwMDAwMDBaMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEw\n" +
                    "HwYDVQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwggEiMA0GCSqGSIb3DQEB\n" +
                    "AQUAA4IBDwAwggEKAoIBAQDtESTcert==\n" +
                    "-----END CERTIFICATE-----";

    private static final String SAMPLE_JWKS_URL = "https://idp.example.com/oauth2/jwks";

    private KeyManagerConfigurationDTO buildExchangedKM(String certType, String certValue) {
        KeyManagerConfigurationDTO config = new KeyManagerConfigurationDTO();
        config.setName("TestKM");
        config.setDisplayName("Test KM");
        config.setDescription("");
        config.setOrganization("carbon.super");
        config.setType("WSO2-IS");
        config.setEnabled(true);
        config.setTokenType("EXCHANGED");
        config.setAlias("https://localhost:9443/oauth2/token");
        config.setUuid("11111111-2222-3333-4444-555555555555");

        Map<String, Object> additionalProps = new HashMap<>();
        if (certType != null) {
            additionalProps.put(APIConstants.KeyManager.CERTIFICATE_TYPE, certType);
        }
        if (certValue != null) {
            additionalProps.put(APIConstants.KeyManager.CERTIFICATE_VALUE, certValue);
        }
        config.setAdditionalProperties(additionalProps);
        return config;
    }

    /**
     * #4990 (create path): a KM with PEM cert must populate the IdP's {@code certificate}
     * field verbatim — pre-fix the inverted {@code String.join(certificate, "")} dropped it
     * to an empty string.
     */
    @Test
    public void testCreateIdpSetsCertificateForPemType() throws Exception {

        APIAdminImpl apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO km = buildExchangedKM("PEM", SAMPLE_PEM);

        IdentityProvider idp = Whitebox.invokeMethod(apiAdmin, "createIdp", km);

        Assert.assertNotNull("IdP should be created", idp);
        Assert.assertEquals("PEM cert must be set verbatim on the IdP (issue #4990)",
                SAMPLE_PEM, idp.getCertificate());
        // PEM branch must NOT add a JWKS_URI property
        IdentityProviderProperty[] props = idp.getIdpProperties();
        if (props != null) {
            for (IdentityProviderProperty p : props) {
                Assert.assertNotEquals("PEM branch must not set JWKS_URI", "jwksUri", p.getName());
            }
        }
    }

    /**
     * #4990 (update path): same expectation as create — the PEM cert must round-trip
     * onto the IdP record.
     */
    @Test
    public void testUpdatedIDPSetsCertificateForPemType() throws Exception {

        APIAdminImpl apiAdmin = new APIAdminImpl();
        IdentityProvider retrieved = new IdentityProvider();
        retrieved.setIdentityProviderName("existing");
        retrieved.setCertificate(""); // simulate the prior empty placeholder
        KeyManagerConfigurationDTO km = buildExchangedKM("PEM", SAMPLE_PEM);

        IdentityProvider idp = Whitebox.invokeMethod(apiAdmin, "updatedIDP", retrieved, km);

        Assert.assertNotNull("IdP should be returned from update", idp);
        Assert.assertEquals("PEM cert must be set verbatim on update (issue #4990)",
                SAMPLE_PEM, idp.getCertificate());
    }

    /**
     * #4990 regression guard: a PEM containing newlines, leading/trailing whitespace,
     * and embedded blank lines must round-trip without truncation or modification.
     * This is what the inverted {@code String.join} would silently mangle.
     */
    @Test
    public void testCreateIdpPreservesPemWithNewlinesAndWhitespace() throws Exception {

        APIAdminImpl apiAdmin = new APIAdminImpl();
        String multilinePem = "  \n-----BEGIN CERTIFICATE-----\nLINE1\n\nLINE2\n   \n-----END CERTIFICATE-----\n";
        KeyManagerConfigurationDTO km = buildExchangedKM("PEM", multilinePem);

        IdentityProvider idp = Whitebox.invokeMethod(apiAdmin, "createIdp", km);

        Assert.assertEquals("Multi-line PEM must round-trip verbatim",
                multilinePem, idp.getCertificate());
    }

    /**
     * JWKS branch: with {@code certificate_type=JWKS}, the cert value goes into the
     * {@code jwksUri} IdP property and {@code IdentityProvider.certificate} must remain unset.
     * Sanity check that the fix is scoped to the PEM arm only.
     */
    @Test
    public void testCreateIdpJwksTypeDoesNotSetCertificate() throws Exception {

        APIAdminImpl apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO km = buildExchangedKM("JWKS", SAMPLE_JWKS_URL);

        IdentityProvider idp = Whitebox.invokeMethod(apiAdmin, "createIdp", km);

        Assert.assertNull("JWKS branch must not populate IdP.certificate", idp.getCertificate());
        IdentityProviderProperty[] props = idp.getIdpProperties();
        Assert.assertNotNull("JWKS branch should add a " + APIConstants.JWKS_URI + " property", props);
        boolean jwksFound = false;
        for (IdentityProviderProperty p : props) {
            if (APIConstants.JWKS_URI.equals(p.getName())) {
                Assert.assertEquals(SAMPLE_JWKS_URL, p.getValue());
                jwksFound = true;
            }
        }
        Assert.assertTrue("Expected " + APIConstants.JWKS_URI + " property on JWKS branch", jwksFound);
    }

    /**
     * Edge case: an empty {@code certificate_value} must NOT cause
     * {@code setCertificate} to be invoked — the existing {@code StringUtils.isNotEmpty}
     * guard should short-circuit. The IdP's certificate field remains at its
     * default (null).
     */
    @Test
    public void testCreateIdpEmptyCertificateValueIsSkipped() throws Exception {

        APIAdminImpl apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO km = buildExchangedKM("PEM", "");

        IdentityProvider idp = Whitebox.invokeMethod(apiAdmin, "createIdp", km);

        Assert.assertNull("Empty PEM must not be set on the IdP", idp.getCertificate());
    }

    /**
     * Edge case: a missing {@code certificate_type} (with a non-empty value) must also
     * skip {@code setCertificate} — the outer guard checks both type and value.
     */
    @Test
    public void testCreateIdpMissingCertificateTypeIsSkipped() throws Exception {

        APIAdminImpl apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO km = buildExchangedKM(null, SAMPLE_PEM);

        IdentityProvider idp = Whitebox.invokeMethod(apiAdmin, "createIdp", km);

        Assert.assertNull("PEM without " + APIConstants.KeyManager.CERTIFICATE_TYPE + " must not be set",
                idp.getCertificate());
    }

    /**
     * Update path JWKS sanity: confirm the update path also preserves the PEM-only
     * scope of the fix — JWKS path stays unchanged.
     */
    @Test
    public void testUpdatedIDPJwksTypeDoesNotSetCertificate() throws Exception {

        APIAdminImpl apiAdmin = new APIAdminImpl();
        IdentityProvider retrieved = new IdentityProvider();
        retrieved.setIdentityProviderName("existing");
        KeyManagerConfigurationDTO km = buildExchangedKM("JWKS", SAMPLE_JWKS_URL);

        IdentityProvider idp = Whitebox.invokeMethod(apiAdmin, "updatedIDP", retrieved, km);

        Assert.assertNull("JWKS update branch must not populate IdP.certificate",
                idp.getCertificate());
        IdentityProviderProperty[] props = idp.getIdpProperties();
        Assert.assertNotNull(props);
        boolean jwksFound = false;
        for (IdentityProviderProperty p : props) {
            if (APIConstants.JWKS_URI.equals(p.getName())) {
                Assert.assertEquals(SAMPLE_JWKS_URL, p.getValue());
                jwksFound = true;
            }
        }
        Assert.assertTrue("Expected " + APIConstants.JWKS_URI + " property on JWKS update", jwksFound);
    }
}
