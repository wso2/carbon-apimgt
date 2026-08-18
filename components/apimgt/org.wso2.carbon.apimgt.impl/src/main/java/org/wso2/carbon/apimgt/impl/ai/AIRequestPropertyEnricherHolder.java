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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.AIRequestContext;
import org.wso2.carbon.apimgt.api.AIRequestPropertyEnricher;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.Collections;
import java.util.Map;

/**
 * Loads and holds the single {@link AIRequestPropertyEnricher} instance used by all AI assistance features, and
 * resolves the additional properties of an outbound AI service request through
 * {@link #resolveProperties(AIRequestContext, PropertyResolver)}.
 * <p>
 * The implementation class is read from the {@code property_enricher_impl} configuration under {@code [apim.ai]}. When
 * it is not configured, or cannot be loaded, no enricher is held at all and every payload is dispatched unchanged.
 * <p>
 * This class knows nothing about the individual AI assistance operations, so supporting a new one never requires
 * changing it. See {@link #resolveProperties(AIRequestContext, PropertyResolver)}.
 * <p>
 * Resolving the properties is best effort: a failing or misbehaving implementation is logged and the request is
 * dispatched without the additional properties, so a faulty extension can never fail an AI request.
 */
public class AIRequestPropertyEnricherHolder {

    private static final Log log = LogFactory.getLog(AIRequestPropertyEnricherHolder.class);

    private static final AIRequestPropertyEnricherHolder INSTANCE = new AIRequestPropertyEnricherHolder();

    /**
     * Resolved enricher, or {@code null} when none is configured or the configured one could not be loaded. Meaningful
     * only once {@link #enricherResolved} is set.
     */
    private volatile AIRequestPropertyEnricher enricher;

    /**
     * Set once the API Manager configuration has been read, whether or not that yielded an enricher. Left false while
     * the configuration is not yet available, so that resolution is retried on a later request rather than permanently
     * caching "no enricher"; and set even when no enricher is configured, so that the common case is resolved once
     * instead of on every request.
     */
    private volatile boolean enricherResolved;

    private AIRequestPropertyEnricherHolder() {

    }

    public static AIRequestPropertyEnricherHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Resolves the additional properties of one AI assistance operation, by invoking the enricher method the given
     * resolver selects.
     * <p>
     * This is the only entry point the payload construction sites use. It is deliberately generic: support for a new AI
     * assistance operation is added by declaring a method on {@link AIRequestPropertyEnricher}, giving it an empty
     * implementation in {@link org.wso2.carbon.apimgt.api.AbstractAIRequestPropertyEnricher} and calling this method
     * from the site that builds the payload. This class does not change.
     * <p>
     * Resolving is best effort. A missing enricher, a {@code null} return and any failure raised by the enricher all
     * degrade to no additional properties, so a faulty extension can never fail an AI request.
     *
     * @param context  details of the request being dispatched. When {@code null} no properties are resolved.
     * @param resolver invokes the {@link AIRequestPropertyEnricher} method of the operation being dispatched, typically
     *                 as a lambda, for example {@code enricher -> enricher.enrichApiChatExecuteProperties(context)}
     * @return properties to add to the payload, never {@code null}
     */
    public Map<String, Object> resolveProperties(AIRequestContext context, PropertyResolver resolver) {

        if (context == null || resolver == null) {
            return Collections.emptyMap();
        }
        AIRequestPropertyEnricher propertyEnricher = getEnricher();
        if (propertyEnricher == null) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> additionalProperties = resolver.resolve(propertyEnricher);
            return additionalProperties == null
                    ? Collections.<String, Object>emptyMap()
                    : additionalProperties;
        } catch (Throwable t) {
            // Deliberately catching Throwable. A custom enricher must never be able to fail an AI request, so any
            // failure, including errors such as NoClassDefFoundError raised by a partially deployed extension,
            // degrades to no additional properties. The stack trace identifies the enricher method that failed.
            log.error("Error while resolving the additional properties of an AI service request from "
                    + propertyEnricher.getClass().getName() + ". Proceeding without the additional properties.", t);
            return Collections.emptyMap();
        }
    }

    /**
     * Selects the {@link AIRequestPropertyEnricher} method to invoke for the operation being dispatched. Implemented at
     * the payload construction site, normally as a lambda passed to
     * {@link #resolveProperties(AIRequestContext, PropertyResolver)}.
     */
    @FunctionalInterface
    public interface PropertyResolver {

        /**
         * @param enricher the configured enricher
         * @return the properties the enricher returns for the operation being dispatched
         * @throws APIManagementException if the enricher cannot resolve the properties. The request is then dispatched
         *                                without the additional properties; it is not failed.
         */
        Map<String, Object> resolve(AIRequestPropertyEnricher enricher) throws APIManagementException;
    }

    /**
     * Not exposed on purpose. Callers go through {@link #resolveProperties(AIRequestContext, PropertyResolver)} so that
     * the {@code null} check and the failure handling cannot be forgotten at a call site.
     *
     * @return the enricher instance, or {@code null} when none is configured, the configured one could not be loaded,
     * or the API Manager configuration is not yet available and resolution has to be retried later.
     */
    private AIRequestPropertyEnricher getEnricher() {

        if (enricherResolved) {
            return enricher;
        }
        synchronized (this) {
            if (enricherResolved) {
                return enricher;
            }
            APIManagerConfigurationService configurationService =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
            APIManagerConfiguration configuration =
                    configurationService == null ? null : configurationService.getAPIManagerConfiguration();
            if (configuration == null) {
                // Deliberately leaves enricherResolved false so that a later request retries, rather than caching
                // "no enricher" for the lifetime of the server because one early request arrived before startup
                // registered the configuration service.
                log.debug("API Manager configuration is not available yet. Deferring AI request property enricher "
                        + "initialization.");
                return null;
            }
            enricher = loadEnricher(configuration.getAIRequestPropertyEnricherImpl());
            enricherResolved = true;
            return enricher;
        }
    }

    /**
     * @param implClass the configured implementation class name, which may be null or blank
     * @return the enricher instance, or {@code null} when none is configured or the configured one could not be loaded.
     * No no-op stand in is created: {@link #resolveProperties(AIRequestContext, PropertyResolver)} already treats a
     * missing enricher as "add no properties", so a null enricher and one that returns empty maps are indistinguishable
     * to a caller.
     */
    private AIRequestPropertyEnricher loadEnricher(String implClass) {

        if (StringUtils.isBlank(implClass)) {
            if (log.isDebugEnabled()) {
                log.debug("No AI request property enricher configured");
            }
            return null;
        }

        try {
            AIRequestPropertyEnricher loaded = (AIRequestPropertyEnricher) Class.forName(implClass.trim())
                    .getDeclaredConstructor().newInstance();
            log.info("Initialized AI request property enricher: " + implClass);
            return loaded;
        } catch (ClassCastException e) {
            log.error("Configured AI request property enricher " + implClass + " does not implement "
                    + AIRequestPropertyEnricher.class.getName()
                    + ". AI service request payloads are dispatched without additional properties.", e);
        } catch (Exception e) {
            log.error("Error while initializing the AI request property enricher: " + implClass
                    + ". AI service request payloads are dispatched without additional properties.", e);
        } catch (LinkageError e) {
            // Loading the class runs its static initializer and links it, which raises errors rather than exceptions,
            // for example ExceptionInInitializerError from a failing static initializer, NoClassDefFoundError from a
            // partially deployed extension or UnsupportedClassVersionError from one built for a newer Java version.
            // These are absorbed here so that a broken extension can never fail an AI request.
            log.error("Error while loading the AI request property enricher: " + implClass
                    + ". AI service request payloads are dispatched without additional properties.", e);
        }
        return null;
    }
}
