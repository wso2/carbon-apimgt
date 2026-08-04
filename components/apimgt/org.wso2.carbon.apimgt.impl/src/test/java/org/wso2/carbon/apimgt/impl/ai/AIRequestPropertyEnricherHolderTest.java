/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.impl.ai;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.AIRequestContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.AbstractAIRequestPropertyEnricher;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests {@link AIRequestPropertyEnricherHolder}, which loads the configured
 * {@link org.wso2.carbon.apimgt.api.AIRequestPropertyEnricher} and resolves the additional properties of an outbound AI
 * service request.
 * <p>
 * The behaviour that matters most here is fail open: no configuration, a missing class, a broken class and an enricher
 * that misbehaves at request time must all degrade to no additional properties rather than fail the AI request.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class AIRequestPropertyEnricherHolderTest {

    private static final String USERNAME_KEY = "username";
    private static final String USERNAME = "john@acme.com";

    private APIManagerConfiguration configuration;

    @Before
    public void setUp() throws Exception {

        // The holder is a singleton that caches the resolved enricher, so both are reset to keep tests independent.
        resetHolder();
        TrackingEnricher.instantiations = 0;

        configuration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configurationService);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
    }

    @Test
    public void testGetInstanceReturnsTheSameHolder() {

        Assert.assertSame("getInstance() must return a single shared holder",
                AIRequestPropertyEnricherHolder.getInstance(), AIRequestPropertyEnricherHolder.getInstance());
    }

    @Test
    public void testNullContextResolvesNoProperties() {

        configureEnricher(TrackingEnricher.class);

        Map<String, Object> properties = AIRequestPropertyEnricherHolder.getInstance()
                .resolveProperties(null, enricher -> enricher.enrichMarketplaceAssistantChatProperties(null));

        Assert.assertTrue("A null context must resolve no properties", properties.isEmpty());
        Assert.assertEquals("The enricher must not be consulted without a context", 0,
                TrackingEnricher.instantiations);
    }

    @Test
    public void testNullResolverResolvesNoProperties() {

        configureEnricher(TrackingEnricher.class);

        Map<String, Object> properties =
                AIRequestPropertyEnricherHolder.getInstance().resolveProperties(context(), null);

        Assert.assertTrue("A null resolver must resolve no properties", properties.isEmpty());
    }

    @Test
    public void testConfiguredEnricherPropertiesAreResolved() {

        configureEnricher(TrackingEnricher.class);

        Map<String, Object> properties = resolveMarketplaceChat();

        Assert.assertEquals("The configured enricher must contribute its properties",
                Collections.singletonMap(USERNAME_KEY, USERNAME), properties);
    }

    /**
     * The point of the extension point: an enricher contributes only to the operations whose methods it overrides.
     * {@link TrackingEnricher} overrides Marketplace Assistant chat and nothing else, so every other operation must
     * resolve no properties and leave its payload untouched.
     */
    @Test
    public void testOnlyOverriddenOperationContributesProperties() {

        configureEnricher(TrackingEnricher.class);
        AIRequestPropertyEnricherHolder holder = AIRequestPropertyEnricherHolder.getInstance();
        AIRequestContext context = context();

        Assert.assertFalse("The overridden operation must contribute properties",
                holder.resolveProperties(context,
                        enricher -> enricher.enrichMarketplaceAssistantChatProperties(context)).isEmpty());

        Assert.assertTrue("API publish is not overridden and must contribute nothing",
                holder.resolveProperties(context,
                        enricher -> enricher.enrichMarketplaceAssistantApiPublishProperties(context)).isEmpty());
        Assert.assertTrue("API Chat prepare is not overridden and must contribute nothing",
                holder.resolveProperties(context,
                        enricher -> enricher.enrichApiChatPrepareProperties(context)).isEmpty());
        Assert.assertTrue("API Chat execute is not overridden and must contribute nothing",
                holder.resolveProperties(context,
                        enricher -> enricher.enrichApiChatExecuteProperties(context)).isEmpty());
        Assert.assertTrue("Design Assistant chat is not overridden and must contribute nothing",
                holder.resolveProperties(context,
                        enricher -> enricher.enrichDesignAssistantChatProperties(context)).isEmpty());
        Assert.assertTrue("Design Assistant payload generation is not overridden and must contribute nothing",
                holder.resolveProperties(context,
                        enricher -> enricher.enrichDesignAssistantPayloadGenProperties(context)).isEmpty());
    }

    @Test
    public void testEnricherIsLoadedOnlyOnce() {

        configureEnricher(TrackingEnricher.class);

        resolveMarketplaceChat();
        resolveMarketplaceChat();
        resolveMarketplaceChat();

        Assert.assertEquals("The enricher must be instantiated once and reused", 1, TrackingEnricher.instantiations);
    }

    @Test
    public void testUnavailableConfigurationResolvesNoPropertiesAndIsRetriedLater() throws Exception {

        Mockito.when(ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()).thenReturn(null);

        Assert.assertTrue("Without configuration no properties can be resolved", resolveMarketplaceChat().isEmpty());
        Assert.assertNull("No enricher must be cached, so that resolution is retried on a later request",
                cachedEnricher());

        // Configuration becomes available, as it does once the configuration service is registered at startup.
        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);
        Mockito.when(ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService())
                .thenReturn(configurationService);
        configureEnricher(TrackingEnricher.class);

        Assert.assertEquals("Resolution must succeed once the configuration is available",
                Collections.singletonMap(USERNAME_KEY, USERNAME), resolveMarketplaceChat());
    }

    @Test
    public void testUnconfiguredEnricherFallsBackToDefault() throws Exception {

        Mockito.when(configuration.getAIRequestPropertyEnricherImpl()).thenReturn(null);

        Assert.assertTrue("An unconfigured enricher must resolve no properties", resolveMarketplaceChat().isEmpty());
        Assert.assertTrue("The no-op default enricher must be used",
                cachedEnricher() instanceof DefaultAIRequestPropertyEnricher);
    }

    @Test
    public void testBlankEnricherConfigurationFallsBackToDefault() throws Exception {

        Mockito.when(configuration.getAIRequestPropertyEnricherImpl()).thenReturn("   ");

        Assert.assertTrue("A blank configuration must resolve no properties", resolveMarketplaceChat().isEmpty());
        Assert.assertTrue("The no-op default enricher must be used",
                cachedEnricher() instanceof DefaultAIRequestPropertyEnricher);
    }

    @Test
    public void testConfiguredClassNameIsTrimmed() {

        Mockito.when(configuration.getAIRequestPropertyEnricherImpl())
                .thenReturn("  " + TrackingEnricher.class.getName() + "  ");

        Assert.assertEquals("A padded class name must still resolve the enricher",
                Collections.singletonMap(USERNAME_KEY, USERNAME), resolveMarketplaceChat());
    }

    @Test
    public void testMissingEnricherClassFallsBackToDefault() throws Exception {

        Mockito.when(configuration.getAIRequestPropertyEnricherImpl()).thenReturn("com.acme.NoSuchEnricher");

        Assert.assertTrue("A missing class must resolve no properties", resolveMarketplaceChat().isEmpty());
        Assert.assertTrue("A missing class must fall back to the no-op default enricher",
                cachedEnricher() instanceof DefaultAIRequestPropertyEnricher);
    }

    @Test
    public void testClassNotImplementingTheInterfaceFallsBackToDefault() throws Exception {

        Mockito.when(configuration.getAIRequestPropertyEnricherImpl()).thenReturn(NotAnEnricher.class.getName());

        Assert.assertTrue("A class of the wrong type must resolve no properties", resolveMarketplaceChat().isEmpty());
        Assert.assertTrue("A class of the wrong type must fall back to the no-op default enricher",
                cachedEnricher() instanceof DefaultAIRequestPropertyEnricher);
    }

    @Test
    public void testEnricherWithoutDefaultConstructorFallsBackToDefault() throws Exception {

        Mockito.when(configuration.getAIRequestPropertyEnricherImpl())
                .thenReturn(NoDefaultConstructorEnricher.class.getName());

        Assert.assertTrue("A class without a no-argument constructor must resolve no properties",
                resolveMarketplaceChat().isEmpty());
        Assert.assertTrue("A class without a no-argument constructor must fall back to the no-op default enricher",
                cachedEnricher() instanceof DefaultAIRequestPropertyEnricher);
    }

    @Test
    public void testEnricherThrowingFromConstructorFallsBackToDefault() throws Exception {

        Mockito.when(configuration.getAIRequestPropertyEnricherImpl())
                .thenReturn(ConstructorFailingEnricher.class.getName());

        Assert.assertTrue("A constructor failure must resolve no properties", resolveMarketplaceChat().isEmpty());
        Assert.assertTrue("A constructor failure must fall back to the no-op default enricher",
                cachedEnricher() instanceof DefaultAIRequestPropertyEnricher);
    }

    /**
     * A failing static initializer surfaces as {@code ExceptionInInitializerError}, which is a {@link LinkageError} and
     * therefore not caught by {@code catch (Exception)}. It must still be absorbed, because it is raised while loading
     * the class rather than while resolving properties, and so is outside the failure handling of
     * {@code resolveProperties}.
     */
    @Test
    public void testEnricherWithFailingStaticInitializerFallsBackToDefault() throws Exception {

        Mockito.when(configuration.getAIRequestPropertyEnricherImpl())
                .thenReturn(StaticInitializerFailingEnricher.class.getName());

        Assert.assertTrue("A failing static initializer must resolve no properties",
                resolveMarketplaceChat().isEmpty());
        Assert.assertTrue("A failing static initializer must fall back to the no-op default enricher",
                cachedEnricher() instanceof DefaultAIRequestPropertyEnricher);
    }

    @Test
    public void testEnricherReturningNullResolvesNoProperties() {

        configureEnricher(NullReturningEnricher.class);

        Assert.assertTrue("A null return must be normalised to no properties", resolveMarketplaceChat().isEmpty());
    }

    @Test
    public void testEnricherThrowingCheckedExceptionResolvesNoProperties() {

        configureEnricher(CheckedExceptionEnricher.class);

        Assert.assertTrue("A checked exception must degrade to no properties", resolveMarketplaceChat().isEmpty());
    }

    @Test
    public void testEnricherThrowingRuntimeExceptionResolvesNoProperties() {

        configureEnricher(RuntimeExceptionEnricher.class);

        Assert.assertTrue("A runtime exception must degrade to no properties", resolveMarketplaceChat().isEmpty());
    }

    /**
     * An {@link Error} raised by a partially deployed extension, such as {@code NoClassDefFoundError}, must be absorbed
     * as well, which is why the failure handling catches {@link Throwable} rather than {@link Exception}.
     */
    @Test
    public void testEnricherThrowingErrorResolvesNoProperties() {

        configureEnricher(ErrorThrowingEnricher.class);

        Assert.assertTrue("An error must degrade to no properties", resolveMarketplaceChat().isEmpty());
    }

    @Test
    public void testFailingEnricherIsRetainedSoLaterOperationsStillWork() {

        configureEnricher(PartiallyFailingEnricher.class);
        AIRequestPropertyEnricherHolder holder = AIRequestPropertyEnricherHolder.getInstance();
        AIRequestContext context = context();

        Assert.assertTrue("The failing operation must degrade to no properties",
                holder.resolveProperties(context,
                        enricher -> enricher.enrichMarketplaceAssistantChatProperties(context)).isEmpty());
        Assert.assertEquals("A failure in one operation must not stop another from contributing",
                Collections.singletonMap(USERNAME_KEY, USERNAME), holder.resolveProperties(context,
                        enricher -> enricher.enrichDesignAssistantChatProperties(context)));
    }

    @Test
    public void testResolvedPropertiesArePassedThroughUnchanged() {

        configureEnricher(MultiPropertyEnricher.class);

        Map<String, Object> expected = new HashMap<>();
        expected.put(USERNAME_KEY, USERNAME);
        expected.put("count", 7);
        expected.put("enabled", Boolean.TRUE);

        Assert.assertEquals("The holder must not alter what the enricher returns", expected,
                resolveMarketplaceChat());
    }

    // Helpers

    private void configureEnricher(Class<?> enricherClass) {

        Mockito.when(configuration.getAIRequestPropertyEnricherImpl()).thenReturn(enricherClass.getName());
    }

    private Map<String, Object> resolveMarketplaceChat() {

        AIRequestContext context = context();
        return AIRequestPropertyEnricherHolder.getInstance().resolveProperties(context,
                enricher -> enricher.enrichMarketplaceAssistantChatProperties(context));
    }

    private static AIRequestContext context() {

        AIRequestContext context = new AIRequestContext();
        context.setUsername(USERNAME);
        context.setOrganization("acme.com");
        context.setResource("/chat");
        return context;
    }

    private static void resetHolder() throws Exception {

        Field instance = AIRequestPropertyEnricherHolder.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    /**
     * @return the enricher the holder cached, read reflectively because the holder deliberately does not expose it
     */
    private static Object cachedEnricher() throws Exception {

        Field enricher = AIRequestPropertyEnricherHolder.class.getDeclaredField("enricher");
        enricher.setAccessible(true);
        return enricher.get(AIRequestPropertyEnricherHolder.getInstance());
    }

    // Enricher fixtures. Loaded by name through Class.forName, so they must be public with a no-argument constructor
    // unless the test is about that requirement not being met.

    /**
     * Overrides Marketplace Assistant chat only, so it also serves as the fixture for operation selectivity.
     */
    public static class TrackingEnricher extends AbstractAIRequestPropertyEnricher {

        private static int instantiations;

        public TrackingEnricher() {

            instantiations++;
        }

        @Override
        public Map<String, Object> enrichMarketplaceAssistantChatProperties(AIRequestContext context) {

            return Collections.singletonMap(USERNAME_KEY, context.getUsername());
        }
    }

    public static class MultiPropertyEnricher extends AbstractAIRequestPropertyEnricher {

        @Override
        public Map<String, Object> enrichMarketplaceAssistantChatProperties(AIRequestContext context) {

            Map<String, Object> properties = new HashMap<>();
            properties.put(USERNAME_KEY, context.getUsername());
            properties.put("count", 7);
            properties.put("enabled", Boolean.TRUE);
            return properties;
        }
    }

    public static class NullReturningEnricher extends AbstractAIRequestPropertyEnricher {

        @Override
        public Map<String, Object> enrichMarketplaceAssistantChatProperties(AIRequestContext context) {

            return null;
        }
    }

    public static class CheckedExceptionEnricher extends AbstractAIRequestPropertyEnricher {

        @Override
        public Map<String, Object> enrichMarketplaceAssistantChatProperties(AIRequestContext context)
                throws APIManagementException {

            throw new APIManagementException("Cannot resolve the additional properties");
        }
    }

    public static class RuntimeExceptionEnricher extends AbstractAIRequestPropertyEnricher {

        @Override
        public Map<String, Object> enrichMarketplaceAssistantChatProperties(AIRequestContext context) {

            throw new IllegalStateException("Faulty extension");
        }
    }

    public static class ErrorThrowingEnricher extends AbstractAIRequestPropertyEnricher {

        @Override
        public Map<String, Object> enrichMarketplaceAssistantChatProperties(AIRequestContext context) {

            throw new NoClassDefFoundError("com/acme/Missing");
        }
    }

    /**
     * Fails on one operation and succeeds on another, to show that a failure is contained to the operation that raised
     * it.
     */
    public static class PartiallyFailingEnricher extends AbstractAIRequestPropertyEnricher {

        @Override
        public Map<String, Object> enrichMarketplaceAssistantChatProperties(AIRequestContext context) {

            throw new IllegalStateException("Faulty extension");
        }

        @Override
        public Map<String, Object> enrichDesignAssistantChatProperties(AIRequestContext context) {

            return Collections.singletonMap(USERNAME_KEY, context.getUsername());
        }
    }

    public static class NoDefaultConstructorEnricher extends AbstractAIRequestPropertyEnricher {

        public NoDefaultConstructorEnricher(String required) {

            // Deliberately has no no-argument constructor.
        }
    }

    public static class ConstructorFailingEnricher extends AbstractAIRequestPropertyEnricher {

        public ConstructorFailingEnricher() {

            throw new IllegalStateException("Cannot initialize the enricher");
        }
    }

    /**
     * Does not implement the enricher interface, so the cast in the holder fails.
     */
    public static class NotAnEnricher {

    }
}
