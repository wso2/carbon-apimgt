/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.synapse.MessageContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper for product features (such as AI token usage) that contribute a named section to the
 * {@code PRODUCT_ANALYTICS_PROPS_TO_PUBLISH} message-context property, which several features may share.
 */
public final class AnalyticsMessageContextUtil {

    private AnalyticsMessageContextUtil() {

    }

    /**
     * Adds or replaces this contributor's own named section in the shared {@code property} map, leaving the other
     * sections untouched. Calling it again (e.g. on a retried backend attempt) replaces the earlier section. The map
     * is copied rather than changed in place, as a cloned message context may share it.
     *
     * @param mc       Message context.
     * @param property Shared message-context property name (e.g. {@code PRODUCT_ANALYTICS_PROPS_TO_PUBLISH}).
     * @param section  This contributor's own section name within that property (e.g. {@code "aiTokenUsage"}).
     * @param value    This contributor's section value.
     */
    @SuppressWarnings("unchecked")
    public static void mergeSection(MessageContext mc, String property, String section, Map<String, Object> value) {

        Object existing = mc.getProperty(property);
        Map<String, Object> sections = existing instanceof Map
                ? new LinkedHashMap<>((Map<String, Object>) existing)
                : new LinkedHashMap<>();
        sections.put(section, value);
        mc.setProperty(property, sections);
    }
}
