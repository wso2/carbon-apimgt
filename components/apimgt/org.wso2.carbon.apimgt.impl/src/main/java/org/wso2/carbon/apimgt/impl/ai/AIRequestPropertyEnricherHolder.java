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
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.Collections;
import java.util.Map;

/**
 * Loads and holds the single {@link AIRequestPropertyEnricher} instance used by all AI assistance features, and
 * resolves the additional properties for a given {@link AIRequestContext}.
 * <p>
 * The implementation class is read from the {@code propertyEnricherImpl} configuration under {@code [apim.ai]}. When it
 * is not configured, or cannot be loaded, {@link DefaultAIRequestPropertyEnricher} is used, which adds no properties.
 * <p>
 * Resolving the properties is best effort: a failing or misbehaving implementation is logged and the request is
 * dispatched without the additional properties, so a faulty extension can never fail an AI request.
 */
public class AIRequestPropertyEnricherHolder {

    private static final Log log = LogFactory.getLog(AIRequestPropertyEnricherHolder.class);

    private static volatile AIRequestPropertyEnricherHolder instance;

    /**
     * Resolved enricher. Left {@code null} while the API Manager configuration is not yet available so that resolution
     * is retried on a later request instead of permanently falling back to the default.
     */
    private volatile AIRequestPropertyEnricher enricher;

    private AIRequestPropertyEnricherHolder() {

    }

    public static AIRequestPropertyEnricherHolder getInstance() {

        if (instance == null) {
            synchronized (AIRequestPropertyEnricherHolder.class) {
                if (instance == null) {
                    instance = new AIRequestPropertyEnricherHolder();
                }
            }
        }
        return instance;
    }

    /**
     * Resolves the additional properties to be added to an outbound AI service request payload.
     *
     * @param context details of the request being dispatched
     * @return properties to add to the payload, never {@code null}. Empty when no enricher is configured or when the
     * configured enricher fails.
     */
    public Map<String, Object> getAdditionalProperties(AIRequestContext context) {

        if (context == null) {
            return Collections.emptyMap();
        }
        AIRequestPropertyEnricher propertyEnricher = getEnricher();
        if (propertyEnricher == null) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> additionalProperties = propertyEnricher.getAdditionalProperties(context);
            return additionalProperties == null
                    ? Collections.<String, Object>emptyMap()
                    : additionalProperties;
        } catch (Throwable t) {
            // Deliberately catching Throwable. A custom enricher must never be able to fail an AI request, so any
            // failure, including errors such as NoClassDefFoundError raised by a partially deployed extension,
            // degrades to no additional properties.
            log.error("Error while resolving the additional properties of the AI service request for resource: "
                    + context.getResource() + ". Proceeding without the additional properties.", t);
            return Collections.emptyMap();
        }
    }

    /**
     * @return the enricher instance, or {@code null} when the API Manager configuration is not yet available and
     * resolution has to be retried later.
     */
    private AIRequestPropertyEnricher getEnricher() {

        AIRequestPropertyEnricher resolved = enricher;
        if (resolved == null) {
            synchronized (this) {
                resolved = enricher;
                if (resolved == null) {
                    resolved = loadEnricher();
                    enricher = resolved;
                }
            }
        }
        return resolved;
    }

    private AIRequestPropertyEnricher loadEnricher() {

        APIManagerConfigurationService configurationService =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
        APIManagerConfiguration configuration =
                configurationService == null ? null : configurationService.getAPIManagerConfiguration();
        if (configuration == null) {
            log.debug("API Manager configuration is not available yet. Deferring AI request property enricher "
                    + "initialization.");
            return null;
        }

        String implClass = configuration.getAIRequestPropertyEnricherImpl();
        if (StringUtils.isBlank(implClass)) {
            if (log.isDebugEnabled()) {
                log.debug("No AI request property enricher configured. Using "
                        + DefaultAIRequestPropertyEnricher.class.getName());
            }
            return new DefaultAIRequestPropertyEnricher();
        }

        try {
            AIRequestPropertyEnricher loaded = (AIRequestPropertyEnricher) Class.forName(implClass.trim())
                    .getDeclaredConstructor().newInstance();
            log.info("Initialized AI request property enricher: " + implClass);
            return loaded;
        } catch (ClassCastException e) {
            log.error("Configured AI request property enricher " + implClass + " does not implement "
                    + AIRequestPropertyEnricher.class.getName() + ". Falling back to "
                    + DefaultAIRequestPropertyEnricher.class.getName(), e);
        } catch (Exception e) {
            log.error("Error while initializing the AI request property enricher: " + implClass + ". Falling back to "
                    + DefaultAIRequestPropertyEnricher.class.getName(), e);
        }
        return new DefaultAIRequestPropertyEnricher();
    }
}
