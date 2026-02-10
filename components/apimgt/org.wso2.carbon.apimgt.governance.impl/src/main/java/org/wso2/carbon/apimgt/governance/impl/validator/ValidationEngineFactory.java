/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class to provide the appropriate ValidationEngine based on Ruleset category.
 * This factory pattern allows different validation engines to handle different rule categories:
 * - GENERIC: Handled by GatekeeperValidationEngine (for deduplication/similarity checks)
 * - SPECTRAL: Handled by SpectralValidationEngine (for OpenAPI/Spectral rule validation)
 * - AI: Can be extended for AI-based validation engines
 */
public class ValidationEngineFactory {

    private static final Log log = LogFactory.getLog(ValidationEngineFactory.class);

    // Cache for registered validation engines by category
    private static final Map<RuleCategory, ValidationEngine> registeredEngines = new ConcurrentHashMap<>();

    // Default engine (Spectral) used when no specific engine is registered for a category
    private static ValidationEngine defaultEngine;

    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationEngineFactory() {
    }

    /**
     * Gets the appropriate ValidationEngine for the given Ruleset based on its RuleCategory.
     *
     * @param ruleset The ruleset to get a validation engine for
     * @return The appropriate ValidationEngine, or the default engine if no specific one is registered
     */
    public static ValidationEngine getValidationEngine(Ruleset ruleset) {
        if (ruleset == null) {
            log.warn("Ruleset is null, returning default validation engine");
            return getDefaultEngine();
        }

        RuleCategory category = ruleset.getRuleCategory();
        if (category == null) {
            if (log.isDebugEnabled()) {
                log.debug("RuleCategory is null for ruleset: " + ruleset.getName() + 
                        ", using default validation engine");
            }
            return getDefaultEngine();
        }

        // Check if a specific engine is registered for this category
        ValidationEngine engine = registeredEngines.get(category);
        if (engine != null) {
            if (log.isDebugEnabled()) {
                log.debug("Using registered validation engine for category " + category + 
                        ": " + engine.getClass().getSimpleName());
            }
            return engine;
        }

        // Fallback to default engine
        if (log.isDebugEnabled()) {
            log.debug("No specific engine registered for category " + category + 
                    ", using default validation engine");
        }
        return getDefaultEngine();
    }

    /**
     * Gets the appropriate ValidationEngine for the given RuleCategory.
     *
     * @param category The rule category
     * @return The appropriate ValidationEngine, or the default engine if no specific one is registered
     */
    public static ValidationEngine getValidationEngine(RuleCategory category) {
        if (category == null) {
            return getDefaultEngine();
        }

        ValidationEngine engine = registeredEngines.get(category);
        return engine != null ? engine : getDefaultEngine();
    }

    /**
     * Registers a ValidationEngine for a specific RuleCategory.
     *
     * @param category The rule category to register the engine for
     * @param engine   The validation engine to register
     */
    public static void registerValidationEngine(RuleCategory category, ValidationEngine engine) {
        if (category == null || engine == null) {
            log.warn("Cannot register null category or engine");
            return;
        }
        registeredEngines.put(category, engine);
        log.info("Registered validation engine " + engine.getClass().getSimpleName() + 
                " for category: " + category);
    }

    /**
     * Unregisters a ValidationEngine for a specific RuleCategory.
     *
     * @param category The rule category to unregister
     */
    public static void unregisterValidationEngine(RuleCategory category) {
        if (category != null) {
            ValidationEngine removed = registeredEngines.remove(category);
            if (removed != null) {
                log.info("Unregistered validation engine for category: " + category);
            }
        }
    }

    /**
     * Sets the default validation engine to use when no specific engine is registered.
     *
     * @param engine The default validation engine
     */
    public static void setDefaultEngine(ValidationEngine engine) {
        defaultEngine = engine;
        log.info("Set default validation engine: " + 
                (engine != null ? engine.getClass().getSimpleName() : "null"));
    }

    /**
     * Gets the default validation engine.
     * Falls back to ServiceReferenceHolder if no default is set.
     *
     * @return The default validation engine
     */
    public static ValidationEngine getDefaultEngine() {
        if (defaultEngine != null) {
            return defaultEngine;
        }

        // Fallback to the engine from ServiceReferenceHolder
        ValidationEngineService service = ServiceReferenceHolder.getInstance().getValidationEngineService();
        if (service != null) {
            return service.getValidationEngine();
        }

        log.warn("No default validation engine available");
        return null;
    }

    /**
     * Clears all registered engines and the default engine.
     * Primarily used for testing purposes.
     */
    public static void clearRegistrations() {
        registeredEngines.clear();
        defaultEngine = null;
        log.info("Cleared all validation engine registrations");
    }

    /**
     * Checks if a specific engine is registered for a category.
     *
     * @param category The rule category to check
     * @return true if an engine is registered for this category
     */
    public static boolean hasRegisteredEngine(RuleCategory category) {
        return category != null && registeredEngines.containsKey(category);
    }
}
