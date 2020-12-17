package org.wso2.carbon.apimgt.impl.definitions;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;

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
        api.setEndpointConfig("{'endpoint_type':'http','sandbox_endpoints':{'url':'wss://echo.websocket.org:443'},'production_endpoints':{'url':'wss://echo.websocket.org:443'}}");
        AsyncApiParser asyncApiParser = new AsyncApiParser();
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
        api.setEndpointConfig("{'endpoint_type':'http','sandbox_endpoints':{'url':'wss://echo.websocket.org:443'},'production_endpoints':{'url':'wss://echo.websocket.org:443'}}");
        AsyncApiParser asyncApiParser = new AsyncApiParser();
        String asyncAPIDefinition = asyncApiParser.generateAsyncAPIDefinition(api);
        String definitionForStore = asyncApiParser.getAsyncApiDefinitionForStore(api, asyncAPIDefinition, hostsWithSchemes);
        Assert.assertNotNull(definitionForStore);
    }
}