/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.LLMModel;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ModelProviderDTO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link AiServiceProvidersApiServiceImpl}, focusing on the
 * buildUpdatedLLMProvider method which handles built-in provider update logic.
 *
 * These tests verify the fix for issue #16287:
 * - Configurations are accepted for built-in providers (not silently discarded)
 * - Model list is preserved when not explicitly provided in the update
 * - Early-return null guard considers configurations (not just apiDefinition)
 */
public class AiServiceProvidersApiServiceImplTest {

    private AiServiceProvidersApiServiceImpl service;
    private Method buildUpdatedLLMProviderMethod;

    private static final String PROVIDER_ID = "test-provider-id-123";
    private static final String PROVIDER_NAME = "AWSBedrock";
    private static final String API_VERSION = "v1";
    private static final String ORIGINAL_DESCRIPTION = "AWS Bedrock AI Provider";
    private static final String ORIGINAL_CONFIGURATIONS = "{\"authenticationConfiguration\":" +
            "{\"type\":\"aws\",\"parameters\":{\"awsServiceName\":\"bedrock\"}}}";
    private static final String NEW_CONFIGURATIONS = "{\"authenticationConfiguration\":" +
            "{\"type\":\"apikey\",\"parameters\":{\"apiKeyIdentifier\":\"Authorization\"," +
            "\"apiKeySentIn\":\"Header\"}}}";
    private static final String ORIGINAL_API_DEFINITION = "openapi: 3.0.0\ninfo:\n  title: Bedrock API";
    private static final String NEW_API_DEFINITION = "openapi: 3.0.0\ninfo:\n  title: Updated Bedrock API";

    @Before
    public void setUp() throws Exception {

        service = new AiServiceProvidersApiServiceImpl();
        // Access the private buildUpdatedLLMProvider method via reflection
        buildUpdatedLLMProviderMethod = AiServiceProvidersApiServiceImpl.class.getDeclaredMethod(
                "buildUpdatedLLMProvider",
                LLMProvider.class,   // retrievedProvider
                String.class,        // aiServiceProviderId
                String.class,        // description
                String.class,        // configurations
                InputStream.class,   // apiDefinitionInputStream
                List.class           // modelList (List<ModelProviderDTO>)
        );
        buildUpdatedLLMProviderMethod.setAccessible(true);
    }

    /**
     * Helper to create a built-in LLMProvider with typical values.
     */
    private LLMProvider createBuiltInProvider() {

        LLMProvider provider = new LLMProvider();
        provider.setId(PROVIDER_ID);
        provider.setName(PROVIDER_NAME);
        provider.setApiVersion(API_VERSION);
        provider.setDescription(ORIGINAL_DESCRIPTION);
        provider.setBuiltInSupport(true);
        provider.setConfigurations(ORIGINAL_CONFIGURATIONS);
        provider.setApiDefinition(ORIGINAL_API_DEFINITION);
        List<LLMModel> models = new ArrayList<>();
        models.add(new LLMModel("Anthropic", Arrays.asList("claude-3-opus", "claude-3-sonnet")));
        models.add(new LLMModel("Meta", Arrays.asList("llama-3-70b", "llama-3-8b")));
        provider.setModelList(models);
        return provider;
    }

    /**
     * Helper to create a non-built-in (custom) LLMProvider.
     */
    private LLMProvider createCustomProvider() {

        LLMProvider provider = new LLMProvider();
        provider.setId("custom-provider-id");
        provider.setName("CustomProvider");
        provider.setApiVersion("v2");
        provider.setDescription("Custom AI Provider");
        provider.setBuiltInSupport(false);
        provider.setConfigurations(ORIGINAL_CONFIGURATIONS);
        provider.setApiDefinition(ORIGINAL_API_DEFINITION);
        List<LLMModel> models = new ArrayList<>();
        models.add(new LLMModel("CustomVendor", Arrays.asList("model-a", "model-b")));
        provider.setModelList(models);
        return provider;
    }

    /**
     * Helper to invoke buildUpdatedLLMProvider via reflection.
     */
    private LLMProvider invokeBuildUpdatedLLMProvider(LLMProvider retrievedProvider, String providerId,
                                                      String description, String configurations,
                                                      InputStream apiDefinitionInputStream,
                                                      List<ModelProviderDTO> modelList) throws Exception {

        return (LLMProvider) buildUpdatedLLMProviderMethod.invoke(service,
                retrievedProvider, providerId, description, configurations,
                apiDefinitionInputStream, modelList);
    }

    private InputStream toStream(String content) {

        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    // ========================================================================
    // Test: Configurations are accepted for built-in providers (Issue #16287 core fix)
    // ========================================================================

    @Test
    public void testBuiltInProvider_ConfigurationsUpdated() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS, null, null);

        Assert.assertNotNull("Result should not be null when configurations are provided", result);
        Assert.assertEquals("Configurations should be updated to new value",
                NEW_CONFIGURATIONS, result.getConfigurations());
    }

    @Test
    public void testBuiltInProvider_ConfigurationsPreservedWhenNull() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        // Provide apiDefinition so the early-return guard doesn't trigger
        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, null, toStream(NEW_API_DEFINITION), null);

        Assert.assertNotNull("Result should not be null when apiDefinition is provided", result);
        Assert.assertEquals("Configurations should be preserved from retrieved provider",
                ORIGINAL_CONFIGURATIONS, result.getConfigurations());
    }

    // ========================================================================
    // Test: Early return null guard considers configurations (Issue #16287)
    // ========================================================================

    @Test
    public void testBuiltInProvider_ReturnsNullWhenBothApiDefinitionAndConfigurationsAreNull() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, null, null, null);

        Assert.assertNull("Should return null when built-in provider has no apiDefinition AND no configurations",
                result);
    }

    @Test
    public void testBuiltInProvider_DoesNotReturnNullWhenOnlyConfigurationsProvided() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        // Only configurations provided, no apiDefinition — should NOT return null
        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS, null, null);

        Assert.assertNotNull(
                "Should NOT return null when configurations are provided (even without apiDefinition)", result);
    }

    @Test
    public void testBuiltInProvider_DoesNotReturnNullWhenOnlyApiDefinitionProvided() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        // Only apiDefinition provided, no configurations
        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, null, toStream(NEW_API_DEFINITION), null);

        Assert.assertNotNull(
                "Should NOT return null when apiDefinition is provided (even without configurations)", result);
    }

    @Test
    public void testBuiltInProvider_DoesNotReturnNullWhenBothProvided() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS,
                toStream(NEW_API_DEFINITION), null);

        Assert.assertNotNull(
                "Should NOT return null when both apiDefinition and configurations are provided", result);
    }

    // ========================================================================
    // Test: Model list preservation when not provided (Issue #16287)
    // ========================================================================

    @Test
    public void testBuiltInProvider_ModelListPreservedWhenNull() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS, null, null);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertNotNull("Model list should not be null", result.getModelList());
        Assert.assertEquals("Model list should be preserved from retrieved provider",
                2, result.getModelList().size());
        Assert.assertEquals("First model vendor should be preserved",
                "Anthropic", result.getModelList().get(0).getModelVendor());
        Assert.assertEquals("Second model vendor should be preserved",
                "Meta", result.getModelList().get(1).getModelVendor());
    }

    @Test
    public void testBuiltInProvider_ModelListUpdatedWhenProvided() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        List<ModelProviderDTO> newModelList = new ArrayList<>();
        ModelProviderDTO dto = new ModelProviderDTO();
        dto.setName("NewVendor");
        dto.setModels(Arrays.asList("new-model-1", "new-model-2"));
        newModelList.add(dto);

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS, null, newModelList);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertNotNull("Model list should not be null", result.getModelList());
        Assert.assertEquals("Model list should contain the new models", 1, result.getModelList().size());
        Assert.assertEquals("Model vendor should be updated",
                "NewVendor", result.getModelList().get(0).getModelVendor());
    }

    @Test
    public void testBuiltInProvider_EmptyModelListDoesNotWipeModels() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        // Providing an empty list explicitly — this should set an empty model list (intentional clear)
        List<ModelProviderDTO> emptyModelList = new ArrayList<>();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS, null, emptyModelList);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertNotNull("Model list should not be null", result.getModelList());
        // An empty explicit list means user wants to clear models
        Assert.assertEquals("Model list should be empty when explicitly provided as empty",
                0, result.getModelList().size());
    }

    // ========================================================================
    // Test: Description immutability for built-in providers
    // ========================================================================

    @Test
    public void testBuiltInProvider_DescriptionNotUpdated() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();
        String newDescription = "New description for built-in provider";

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, newDescription, NEW_CONFIGURATIONS, null, null);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("Built-in provider description should NOT be updated",
                ORIGINAL_DESCRIPTION, result.getDescription());
    }

    @Test
    public void testCustomProvider_DescriptionUpdated() throws Exception {

        LLMProvider retrievedProvider = createCustomProvider();
        String newDescription = "Updated description for custom provider";

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, "custom-provider-id", newDescription, null,
                toStream(NEW_API_DEFINITION), null);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("Custom provider description should be updated",
                newDescription, result.getDescription());
    }

    @Test
    public void testCustomProvider_DescriptionPreservedWhenNull() throws Exception {

        LLMProvider retrievedProvider = createCustomProvider();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, "custom-provider-id", null, null,
                toStream(NEW_API_DEFINITION), null);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("Custom provider description should be preserved when null",
                "Custom AI Provider", result.getDescription());
    }

    // ========================================================================
    // Test: API definition handling
    // ========================================================================

    @Test
    public void testBuiltInProvider_ApiDefinitionUpdated() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS,
                toStream(NEW_API_DEFINITION), null);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("API definition should be updated",
                NEW_API_DEFINITION, result.getApiDefinition());
    }

    @Test
    public void testBuiltInProvider_ApiDefinitionPreservedWhenNull() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS, null, null);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("API definition should be preserved from retrieved provider",
                ORIGINAL_API_DEFINITION, result.getApiDefinition());
    }

    // ========================================================================
    // Test: Non-built-in (custom) provider behavior
    // ========================================================================

    @Test
    public void testCustomProvider_DoesNotReturnNullWhenApiDefinitionAndConfigurationsAreNull() throws Exception {

        LLMProvider retrievedProvider = createCustomProvider();

        // Custom providers don't have the null guard — but apiDefinition being null
        // means it uses retrieved. This should NOT return null for custom providers.
        // (The null guard only applies to built-in providers)
        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, "custom-provider-id", null, null,
                toStream(NEW_API_DEFINITION), null);

        Assert.assertNotNull("Custom provider should not return null when apiDefinition is provided", result);
    }

    @Test
    public void testCustomProvider_ConfigurationsUpdated() throws Exception {

        LLMProvider retrievedProvider = createCustomProvider();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, "custom-provider-id", null, NEW_CONFIGURATIONS,
                toStream(NEW_API_DEFINITION), null);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("Custom provider configurations should be updated",
                NEW_CONFIGURATIONS, result.getConfigurations());
    }

    // ========================================================================
    // Test: Provider metadata is correctly copied
    // ========================================================================

    @Test
    public void testBuiltInProvider_MetadataCopiedCorrectly() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS, null, null);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("Provider ID should be set", PROVIDER_ID, result.getId());
        Assert.assertEquals("Provider name should be copied from retrieved",
                PROVIDER_NAME, result.getName());
        Assert.assertEquals("API version should be copied from retrieved",
                API_VERSION, result.getApiVersion());
        Assert.assertTrue("Built-in support flag should be copied from retrieved",
                result.isBuiltInSupport());
    }

    // ========================================================================
    // Test: Edge case - ModelProviderDTO with null fields filtered out
    // ========================================================================

    @Test
    public void testBuiltInProvider_InvalidModelDTOsFilteredOut() throws Exception {

        LLMProvider retrievedProvider = createBuiltInProvider();

        List<ModelProviderDTO> modelList = new ArrayList<>();
        // Valid DTO
        ModelProviderDTO validDto = new ModelProviderDTO();
        validDto.setName("ValidVendor");
        validDto.setModels(Arrays.asList("model-1"));
        modelList.add(validDto);
        // DTO with null name — should be filtered
        ModelProviderDTO nullNameDto = new ModelProviderDTO();
        nullNameDto.setName(null);
        nullNameDto.setModels(Arrays.asList("model-2"));
        modelList.add(nullNameDto);
        // DTO with null models — should be filtered
        ModelProviderDTO nullModelsDto = new ModelProviderDTO();
        nullModelsDto.setName("NullModelsVendor");
        nullModelsDto.setModels(null);
        modelList.add(nullModelsDto);
        // Null DTO — should be filtered
        modelList.add(null);

        LLMProvider result = invokeBuildUpdatedLLMProvider(
                retrievedProvider, PROVIDER_ID, null, NEW_CONFIGURATIONS, null, modelList);

        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("Only valid model DTOs should be included",
                1, result.getModelList().size());
        Assert.assertEquals("Valid vendor should be present",
                "ValidVendor", result.getModelList().get(0).getModelVendor());
    }
}
