/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.output.adapter.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * .
 */
public class ExtendedHTTPEventAdaptorTestCase {
    private static final Log logger = LogFactory.getLog(ExtendedHTTPEventAdaptorTestCase.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    @Before
    public void setupCarbonConfig() {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "tenant.name");
    }

    public ExtendedHTTPEventAdapter getHttpAdaptor() {
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestHttpAdaptor");
        eventAdapterConfiguration.setType("http-extended");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticPropertes = new HashMap<>();
        staticPropertes.put("http.client.method", "HttpPost");
        staticPropertes.put("http.proxy.port", "9090");
        staticPropertes.put("http.proxy.host", "localhost");
        staticPropertes.put("oauth.url", "localhost:9090/token");
        staticPropertes.put("oauth.consumer.key", "key");
        staticPropertes.put("oauth.consumer.secret", "secret");
        eventAdapterConfiguration.setStaticProperties(staticPropertes);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        return new ExtendedHTTPEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    @Test
    public void testHTTPPublisherInit() throws OutputEventAdapterException {
        ExtendedHTTPEventAdapter httpEventAdapter = getHttpAdaptor();
        httpEventAdapter.init();
        httpEventAdapter.isPolled();
        httpEventAdapter.destroy();
    }

    @Test
    public void testHttpPublisherConnect() throws OutputEventAdapterException {
        logger.info("Test case for connection of Extended HTTP output adaptor.");
        ExtendedHTTPEventAdapter httpEventAdapter = getHttpAdaptor();
        httpEventAdapter.init();
        httpEventAdapter.connect();
        httpEventAdapter.disconnect();
        httpEventAdapter.destroy();
    }

    @Test
    public void testHttpPublisherConnect2() throws OutputEventAdapterException {
        logger.info("Test case for connection of Extended HTTP output adaptor.");
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestHttpAdaptor");
        eventAdapterConfiguration.setType("http-extended");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticPropertes = new HashMap<>();
        staticPropertes.put("http.client.method", "HttpPost");
        staticPropertes.put("http.proxy.port", null);
        staticPropertes.put("http.proxy.host", null);
        staticPropertes.put("oauth.url", null);
        staticPropertes.put("oauth.consumer.key", null);
        staticPropertes.put("oauth.consumer.secret", null);
        eventAdapterConfiguration.setStaticProperties(staticPropertes);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        ExtendedHTTPEventAdapter httpEventAdapter = new ExtendedHTTPEventAdapter(eventAdapterConfiguration, globalProperties);
        httpEventAdapter.init();
        httpEventAdapter.connect();
        httpEventAdapter.disconnect();
        httpEventAdapter.destroy();
    }

    @Test
    public void testHttpPublisherPublish() throws InterruptedException {
        logger.info("Test case for publishing http basic oauth request from output adaptor.");
        ExtendedHTTPEventAdapter httpEventAdapter = getHttpAdaptor();
        try {
            httpEventAdapter.init();
            httpEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("http.password", "pwd123");
            dynamicProperties.put("http.headers", null);
            dynamicProperties.put("http.url", "http://localhost:8080/service");
            dynamicProperties.put("http.username", "user123");
            httpEventAdapter.publish("hi", dynamicProperties);
            Thread.sleep(1000);
        } catch (OutputEventAdapterException e) {
            logger.error(e.getMessage());
        } finally {
            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }
    }

    @Test
    public void testHttpPublisherPublish2() {
        logger.info("Test case for publishing http basic oauth request from output adaptor with headers");
        ExtendedHTTPEventAdapter httpEventAdapter = getHttpAdaptor();
        try {
            httpEventAdapter.init();
            httpEventAdapter.connect();
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("http.password", "pwd123");
            dynamicProperties.put("http.headers", "Content-Type:application/json");
            dynamicProperties.put("http.url", "http://localhost:8080/service");
            dynamicProperties.put("http.username", "user123");
            httpEventAdapter.publish("hi", dynamicProperties);
        } catch (OutputEventAdapterException e) {
            logger.error(e.getMessage());
        } finally {
            httpEventAdapter.disconnect();
            httpEventAdapter.destroy();
        }

    }

    @Test
    public void testHttpPublisherFactory() {
        logger.info("Test case for factory properties of Extended HTTP output adaptor.");
        ExtendedHTTPEventAdapterFactory adapterFactory = new ExtendedHTTPEventAdapterFactory();
        List<Property> dyPropertyList = adapterFactory.getDynamicPropertyList();
        List<Property> propertyList = new ArrayList<>();

        Property property = new Property("http.url");
        property.setRequired(true);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("URL");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("The target HTTP/HTTPS URL, e.g. \"http://yourhost:8080/service\"");
        propertyList.add(property);

        property = new Property("http.username");
        property.setRequired(false);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Username");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("HTTP BasicAuth username");
        propertyList.add(property);

        property = new Property("http.password");
        property.setRequired(false);
        property.setSecured(true);
        property.setEncrypted(true);
        property.setDisplayName("Password");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("HTTP BasicAuth password");
        propertyList.add(property);

        property = new Property("http.headers");
        property.setRequired(false);
        property.setSecured(false);
        property.setEncrypted(false);
        property.setDisplayName("Headers");
        property.setDefaultValue(null);
        property.setOptions(null);
        property.setHint("Custom HTTP headers, e.g. \"header1: value1, header2: value2\"");
        propertyList.add(property);

        Assert.assertEquals(4, dyPropertyList.size());
        int i = 0;
        for (Property prop : propertyList) {
            Assert.assertEquals(prop.getPropertyName(), dyPropertyList.get(i).getPropertyName());
            Assert.assertEquals(prop.getDefaultValue(), dyPropertyList.get(i).getDefaultValue());
            Assert.assertEquals(prop.getDisplayName(), dyPropertyList.get(i).getDisplayName());
            Assert.assertEquals(prop.getHint(), dyPropertyList.get(i).getHint());
            i++;
        }
        List<String> types = new ArrayList<>();
        types.add("text");
        types.add("xml");
        types.add("json");
        List<String> supportedTypes = adapterFactory.getSupportedMessageFormats();
        Assert.assertEquals(supportedTypes.toString(), types.toString());
        Assert.assertEquals("http-extended", adapterFactory.getType());

        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        OutputEventAdapterConfiguration eventAdapterConfiguration = new OutputEventAdapterConfiguration();
        eventAdapterConfiguration.setName("TestHttpAdaptor");
        eventAdapterConfiguration.setType("http-extended");
        eventAdapterConfiguration.setMessageFormat("text");
        Map<String, String> staticPropertes = new HashMap<>();
        staticPropertes.put("http.client.method", "HttpPost");
        staticPropertes.put("http.proxy.port", "9090");
        staticPropertes.put("http.proxy.host", "localhost");
        staticPropertes.put("oauth.url", "localhost:9090/token");
        staticPropertes.put("oauth.consumer.key", "key");
        staticPropertes.put("oauth.consumer.secret", "secret");
        eventAdapterConfiguration.setStaticProperties(staticPropertes);
        Map<String, String> globalProperties = new HashMap<>();
        globalProperties.put("jobQueueSize", "10000");
        globalProperties.put("keepAliveTimeInMillis", "20000");
        globalProperties.put("maxThread", "100");
        globalProperties.put("minThread", "8");
        globalProperties.put("defaultMaxConnectionsPerHost", "50");
        globalProperties.put("maxTotalConnections", "1000");
        adapterFactory.createEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
