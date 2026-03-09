/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.definitions;

import org.junit.Assert;
import org.junit.Test;

public class AsyncAPIDefinitionProcessorTest {

    private final AsyncAPIDefinitionProcessor processor = new AsyncAPIDefinitionProcessor();

    @Test
    public void extractEndpointUrlShouldSupportJsonDefinitions() {
        String definition = "{\n"
                + "  \"asyncapi\": \"2.6.0\",\n"
                + "  \"servers\": {\n"
                + "    \"production\": {\n"
                + "      \"url\": \"broker.example.com/socket\",\n"
                + "      \"protocol\": \"wss\"\n"
                + "    }\n"
                + "  }\n"
                + "}";

        Assert.assertEquals("wss://broker.example.com/socket", processor.extractEndpointUrl(definition));
    }

    @Test
    public void extractEndpointUrlShouldSupportYamlDefinitions() {
        String definition = "asyncapi: 2.6.0\n"
                + "servers:\n"
                + "  production:\n"
                + "    url: broker.example.com/socket\n"
                + "    protocol: ws\n";

        Assert.assertEquals("ws://broker.example.com/socket", processor.extractEndpointUrl(definition));
    }

    @Test
    public void extractEndpointUrlShouldNotDuplicateExistingScheme() {
        String definition = "{\n"
                + "  \"asyncapi\": \"2.6.0\",\n"
                + "  \"servers\": {\n"
                + "    \"production\": {\n"
                + "      \"url\": \"wss://broker.example.com/socket\",\n"
                + "      \"protocol\": \"wss\"\n"
                + "    }\n"
                + "  }\n"
                + "}";

        Assert.assertEquals("wss://broker.example.com/socket", processor.extractEndpointUrl(definition));
    }

    @Test
    public void extractEndpointUrlShouldReturnNullForInvalidDefinitions() {
        Assert.assertNull(processor.extractEndpointUrl("not: [valid"));
    }
}
