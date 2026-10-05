
/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.spec.parser.definitions;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParseOptions;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParserFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AsyncApiParserTest {

    @Test
    public void testGenerateAsyncAPIDefinition() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin", "HelloServer", "1.0");
        API api = new API(identifier);
        api.setType("WS");
        api.setContext("/hello/1.0");
        api.setTransports("ws,wss");
        api.setEndpointConfig("{'endpoint_type':'http','sandbox_endpoints':{'url':'wss://echo.websocket.org:443'}," +
                "'production_endpoints':{'url':'wss://echo.websocket.org:443'}}");
        AsyncApiParseOptions options =  new AsyncApiParseOptions();
        options.setPreserveLegacyAsyncApiParser(true);
        AbstractAsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(
                AsyncApiTestUtils.ASYNCAPI_V20, options);
        String asyncAPIDefinition = asyncApiParser.generateAsyncAPIDefinition(api);
        Assert.assertNotNull(asyncAPIDefinition);
    }

    @Test
    public void testGetAsyncAPIDefinitionForStore() throws Exception {
        Map<String, String> hostsWithSchemes = new HashMap<>();
        hostsWithSchemes.put("ws", "ws://localhost:9099");
        hostsWithSchemes.put("wss", "wss://localhost:8099");
        APIIdentifier identifier = new APIIdentifier("admin", "HelloServer2", "1.0");
        API api = new API(identifier);
        api.setType("WS");
        api.setContext("/hello2/1.0");
        api.setTransports("ws,wss");
        api.setEndpointConfig("{'endpoint_type':'http','sandbox_endpoints':{'url':'wss://echo.websocket.org:443'}," +
                "'production_endpoints':{'url':'wss://echo.websocket.org:443'}}");
        AsyncApiParseOptions options =  new AsyncApiParseOptions();
        options.setPreserveLegacyAsyncApiParser(true);
        AbstractAsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(
                AsyncApiTestUtils.ASYNCAPI_V20, options);
        String asyncAPIDefinition = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "asyncAPI" +
                        File.separator + "sampleWebSocket.json"),
                "UTF-8");
        api.setAsyncApiDefinition(asyncAPIDefinition);
        String definitionForStore = asyncApiParser.getAsyncApiDefinitionForStore(api, asyncAPIDefinition, hostsWithSchemes);
        Assert.assertNotNull(definitionForStore);
    }

    @Test
    public void testGenerateAsyncAPIDefinitionFail() throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin", "HelloServer", "1.0");
        API api = new API(identifier);
        AsyncApiParseOptions options =  new AsyncApiParseOptions();
        options.setPreserveLegacyAsyncApiParser(true);
        AbstractAsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(
                AsyncApiTestUtils.ASYNCAPI_V20, options);
        try {
            asyncApiParser.generateAsyncAPIDefinition(api);
            Assert.fail("Expected exception was not thrown");
        } catch (IllegalStateException | APIManagementException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetAsyncAPIDefinitionForStoreFail() throws Exception {
        Map<String, String> hostsWithSchemes = new HashMap<>();
        hostsWithSchemes.put("ws", "ws://localhost:9099");
        hostsWithSchemes.put("wss", "wss://localhost:8099");
        APIIdentifier identifier = new APIIdentifier("admin", "HelloServer2", "1.0");
        API api = new API(identifier);
        AsyncApiParseOptions options =  new AsyncApiParseOptions();
        options.setPreserveLegacyAsyncApiParser(true);
        AbstractAsyncApiParser asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(
                AsyncApiTestUtils.ASYNCAPI_V20, options);
        String asyncAPIDefinition = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "asyncAPI" +
                        File.separator + "incorrectWebSocket.yml"),
                "UTF-8");
        api.setAsyncApiDefinition(asyncAPIDefinition);
        try {
            asyncApiParser.getAsyncApiDefinitionForStore(api, asyncAPIDefinition, hostsWithSchemes);
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }
    }
}
