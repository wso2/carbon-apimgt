/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.carbon.apimgt.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hubspot.jinjava.Jinjava;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders the api-manager.xml.j2 template of the core feature with Jinjava, the template engine used by the config
 * mapper at server startup, and loads the result through APIManagerConfiguration. Errors inside a conditional block
 * of the template (e.g. a malformed closing tag) only surface when that block is rendered, so each test enables a
 * different combination of the event hub JMS task manager (thread pool) configurations.
 */
public class APIManagerConfigTemplateTest {

    private static final String TEMPLATES_DIR_PROPERTY = "apim.conf.templates.dir";
    private static final String API_MANAGER_TEMPLATE = "templates/repository/conf/api-manager.xml.j2";
    private static final String DEFAULT_CONFIGS = "org.wso2.carbon.apimgt.core.default.json";
    private static final String UNIT_RESOLVE_CONFIGS = "org.wso2.carbon.apimgt.core.unit-resolve.json";
    private static final String EVENT_HUB_LISTENER = "apim.event_hub.listener.";
    private static final String THROTTLING_JMS = "apim.throttling.jms.";
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$ref\\{(.+)}");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String template;
    private Map<String, Object> defaultConfigs;

    @Before
    public void setUp() throws IOException {

        File templatesDir = new File(System.getProperty(TEMPLATES_DIR_PROPERTY));
        template = FileUtils.readFileToString(new File(templatesDir, API_MANAGER_TEMPLATE), StandardCharsets.UTF_8);
        String defaults = FileUtils.readFileToString(new File(templatesDir, DEFAULT_CONFIGS), StandardCharsets.UTF_8);
        defaultConfigs = new Gson().fromJson(defaults, new TypeToken<Map<String, Object>>() {
        }.getType());

        // The config mapper converts values such as "200s" to the unit given in unit-resolve.json before rendering.
        // Defaults are already in that unit, so dropping the suffix is enough.
        String unitResolveConfigs = FileUtils.readFileToString(new File(templatesDir, UNIT_RESOLVE_CONFIGS),
                StandardCharsets.UTF_8);
        Map<String, Map<String, String>> units = new Gson().fromJson(unitResolveConfigs,
                new TypeToken<Map<String, Map<String, String>>>() {
                }.getType());
        for (Map.Entry<String, String> timeUnit : units.get("time").entrySet()) {
            Object value = defaultConfigs.get(timeUnit.getKey());
            if (value instanceof String && ((String) value).matches("\\d+" + timeUnit.getValue())) {
                defaultConfigs.put(timeUnit.getKey(), ((String) value).replaceFirst(timeUnit.getValue() + "$", ""));
            }
        }
    }

    @Test
    public void testThreadPoolNotConfigured() throws Exception {

        String renderedConfig = render(new HashMap<String, Object>());
        Assert.assertFalse(renderedConfig.contains("JMSTaskManager"));
        assertTaskManagerProperties(load(renderedConfig), 20, 100, 1000, 10);
    }

    @Test
    public void testEventHubListenerThreadPoolConfigs() throws Exception {

        Map<String, Object> configs = threadPoolConfigs(EVENT_HUB_LISTENER, 25, 150, 2000, 30);
        assertTaskManagerProperties(load(render(configs)), 25, 150, 2000, 30);
    }

    @Test
    public void testThrottlingJmsThreadPoolConfigs() throws Exception {

        Map<String, Object> configs = threadPoolConfigs(THROTTLING_JMS, 15, 80, 3000, 40);
        assertTaskManagerProperties(load(render(configs)), 15, 80, 3000, 40);
    }

    @Test
    public void testEventHubListenerConfigsTakePrecedence() throws Exception {

        Map<String, Object> configs = threadPoolConfigs(EVENT_HUB_LISTENER, 25, 150, 2000, 30);
        configs.putAll(threadPoolConfigs(THROTTLING_JMS, 15, 80, 3000, 40));
        assertTaskManagerProperties(load(render(configs)), 25, 150, 2000, 30);
    }

    @Test
    public void testFallbackToThrottlingJmsConfigsPerProperty() throws Exception {

        Map<String, Object> configs = new HashMap<>();
        configs.put(EVENT_HUB_LISTENER + "min_thread_pool_size", 25);
        configs.put(THROTTLING_JMS + "job_queue_size", 40);
        assertTaskManagerProperties(load(render(configs)), 25, 100, 1000, 40);
    }

    @Test
    public void testKeepAliveTimeWithoutJobQueueSize() throws Exception {

        Map<String, Object> configs = new HashMap<>();
        configs.put(EVENT_HUB_LISTENER + "keep_alive_time_in_millis", 2000);
        assertTaskManagerProperties(load(render(configs)), 20, 100, 2000, 10);
    }

    private Map<String, Object> threadPoolConfigs(String prefix, int minThreadPoolSize, int maxThreadPoolSize,
                                                  int keepAliveTimeInMillis, int jobQueueSize) {

        Map<String, Object> configs = new HashMap<>();
        configs.put(prefix + "min_thread_pool_size", minThreadPoolSize);
        configs.put(prefix + "max_thread_pool_size", maxThreadPoolSize);
        configs.put(prefix + "keep_alive_time_in_millis", keepAliveTimeInMillis);
        configs.put(prefix + "job_queue_size", jobQueueSize);
        return configs;
    }

    /**
     * Renders the template with the given deployment.toml style configs applied on top of the feature defaults.
     */
    private String render(Map<String, Object> deploymentConfigs) {

        Map<String, Object> configs = new HashMap<>(defaultConfigs);
        // Mandatory deployment.toml configs referred by the feature defaults
        configs.put("super_admin.username", "admin");
        configs.put("super_admin.password", "admin");
        configs.putAll(deploymentConfigs);
        Map<String, Object> context = new HashMap<>();
        for (Map.Entry<String, Object> config : configs.entrySet()) {
            addToContext(context, config.getKey(), resolveReference(configs, config.getValue()));
        }
        return new Jinjava().render(template, context);
    }

    /**
     * Resolves references such as $ref{super_admin.username} the same way the config mapper does before rendering.
     */
    private Object resolveReference(Map<String, Object> configs, Object value) {

        if (value instanceof String) {
            Matcher matcher = REFERENCE_PATTERN.matcher((String) value);
            if (matcher.matches()) {
                return resolveReference(configs, configs.get(matcher.group(1)));
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private void addToContext(Map<String, Object> context, String key, Object value) {

        String[] keyParts = key.split("\\.");
        Map<String, Object> current = context;
        for (int i = 0; i < keyParts.length - 1; i++) {
            Object child = current.get(keyParts[i]);
            if (child == null) {
                child = new HashMap<String, Object>();
                current.put(keyParts[i], child);
            }
            current = (Map<String, Object>) child;
        }
        current.put(keyParts[keyParts.length - 1], value);
    }

    private ThrottleProperties.JMSConnectionProperties.JMSTaskManagerProperties load(String renderedConfig)
            throws IOException, APIManagementException {

        File configFile = temporaryFolder.newFile("api-manager.xml");
        FileUtils.writeStringToFile(configFile, renderedConfig, StandardCharsets.UTF_8);
        APIManagerConfiguration config = new APIManagerConfiguration();
        config.load(configFile.getAbsolutePath());
        return config.getThrottleProperties().getJmsConnectionProperties().getJmsTaskManagerProperties();
    }

    private void assertTaskManagerProperties(
            ThrottleProperties.JMSConnectionProperties.JMSTaskManagerProperties taskManagerProperties,
            int minThreadPoolSize, int maxThreadPoolSize, int keepAliveTimeInMillis, int jobQueueSize) {

        Assert.assertEquals(minThreadPoolSize, taskManagerProperties.getMinThreadPoolSize());
        Assert.assertEquals(maxThreadPoolSize, taskManagerProperties.getMaxThreadPoolSize());
        Assert.assertEquals(keepAliveTimeInMillis, taskManagerProperties.getKeepAliveTimeInMillis());
        Assert.assertEquals(jobQueueSize, taskManagerProperties.getJobQueueSize());
    }
}
