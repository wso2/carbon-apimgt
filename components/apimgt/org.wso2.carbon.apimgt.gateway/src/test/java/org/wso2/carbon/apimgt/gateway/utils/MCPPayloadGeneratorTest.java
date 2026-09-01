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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link MCPPayloadGenerator#generateToolListPayload(Object, List, boolean)} — the gateway
 * builder for the {@code tools/list} response. Pins the metadata-preservation fix: a stored tool definition
 * may be a bare input schema OR a wrapper additionally carrying {@code title}, {@code annotations},
 * {@code _meta}, {@code outputSchema} and {@code execution}; the builder must emit ALL of those fields, while
 * still sanitizing the input schema for non third-party (OpenAPI/API-generated) servers.
 */
public class MCPPayloadGeneratorTest {

    private static final Gson GSON = new Gson();

    private JsonArray toolsOf(String payload) {
        JsonObject root = GSON.fromJson(payload, JsonObject.class);
        Assert.assertTrue("payload must carry a result object", root.has("result"));
        JsonObject result = root.getAsJsonObject("result");
        Assert.assertTrue("result must carry a tools array", result.has("tools"));
        return result.getAsJsonArray("tools");
    }

    private URLMapping tool(String name, String description, String schemaDefinition) {
        URLMapping mapping = new URLMapping();
        mapping.setUrlPattern(name);
        mapping.setDescription(description);
        mapping.setSchemaDefinition(schemaDefinition);
        return mapping;
    }

    /**
     * Regression core — a proxied (third-party) tool whose stored definition wraps the input schema together
     * with annotations, _meta and outputSchema must have EVERY one of those fields emitted in tools/list, and
     * the input schema passed through UNCHANGED (proxy schemas are not sanitized). Before the fix only
     * name/description/inputSchema were emitted.
     */
    @Test
    public void testProxyToolPreservesMetadataInToolsList() {

        String schema = "{"
                + "\"inputSchema\":{\"type\":\"object\",\"properties\":{\"query_city\":{\"type\":\"string\"}},"
                + "  \"required\":[\"query_city\"]},"
                + "\"title\":\"Weather Lookup\","
                + "\"annotations\":{\"readOnlyHint\":true,\"title\":\"Weather\"},"
                + "\"_meta\":{\"vendor\":\"wso2\"},"
                + "\"outputSchema\":{\"type\":\"object\",\"properties\":{\"temp\":{\"type\":\"number\"}},"
                + "  \"required\":[\"temp\"]},"
                + "\"execution\":{\"mode\":\"async\",\"timeout\":30}"
                + "}";
        List<URLMapping> ops = Collections.singletonList(tool("get_weather", "Returns the weather", schema));

        JsonArray tools = toolsOf(MCPPayloadGenerator.generateToolListPayload(1, ops, true));
        Assert.assertEquals(1, tools.size());
        JsonObject t = tools.get(0).getAsJsonObject();

        Assert.assertEquals("get_weather", t.get("name").getAsString());
        Assert.assertEquals("Returns the weather", t.get("description").getAsString());

        Assert.assertEquals("top-level title must be preserved", "Weather Lookup", t.get("title").getAsString());
        Assert.assertTrue("execution must be preserved", t.has("execution"));
        Assert.assertEquals("execution.mode value must be preserved", "async",
                t.getAsJsonObject("execution").get("mode").getAsString());
        Assert.assertEquals("execution.timeout value must be preserved", 30,
                t.getAsJsonObject("execution").get("timeout").getAsInt());

        Assert.assertTrue("annotations must be preserved", t.has("annotations"));
        Assert.assertTrue("annotations.readOnlyHint value must be preserved",
                t.getAsJsonObject("annotations").get("readOnlyHint").getAsBoolean());
        Assert.assertEquals("annotations.title value must be preserved", "Weather",
                t.getAsJsonObject("annotations").get("title").getAsString());

        Assert.assertTrue("_meta must be preserved", t.has("_meta"));
        Assert.assertEquals("wso2", t.getAsJsonObject("_meta").get("vendor").getAsString());

        Assert.assertTrue("outputSchema must be preserved", t.has("outputSchema"));
        Assert.assertEquals("outputSchema contents must be preserved verbatim", "number",
                t.getAsJsonObject("outputSchema").getAsJsonObject("properties")
                        .getAsJsonObject("temp").get("type").getAsString());
        Assert.assertEquals("outputSchema.required must be preserved verbatim", "temp",
                t.getAsJsonObject("outputSchema").getAsJsonArray("required").get(0).getAsString());

        // Proxy input schema is passed through verbatim (not sanitized).
        JsonObject inputSchema = t.getAsJsonObject("inputSchema");
        Assert.assertTrue("proxy inputSchema must be passed through unchanged",
                inputSchema.getAsJsonObject("properties").has("query_city"));
    }

    /**
     * Backward compatibility — a proxied tool stored in the LEGACY shape (the definition is the bare input
     * schema, with no {@code inputSchema} wrapper key) is still emitted with its input schema, unchanged.
     */
    @Test
    public void testProxyToolWithLegacyBareSchemaIsPassedThrough() {

        // Prefixed key: the third-party path must pass it through unchanged.
        String bareSchema = "{\"type\":\"object\",\"properties\":{\"query_message\":{\"type\":\"string\"}},"
                + "\"required\":[\"query_message\"]}";
        List<URLMapping> ops = Collections.singletonList(tool("echo", "Echoes the message", bareSchema));

        JsonArray tools = toolsOf(MCPPayloadGenerator.generateToolListPayload(1, ops, true));
        JsonObject t = tools.get(0).getAsJsonObject();

        Assert.assertEquals("echo", t.get("name").getAsString());
        Assert.assertTrue("inputSchema must be emitted", t.has("inputSchema"));
        JsonObject properties = t.getAsJsonObject("inputSchema").getAsJsonObject("properties");
        Assert.assertTrue("bare proxy schema must be passed through unchanged (prefix retained)",
                properties.has("query_message"));
        Assert.assertFalse("prefix must NOT be stripped on the third-party path", properties.has("message"));
        Assert.assertEquals("required must retain the prefixed key", "query_message",
                t.getAsJsonObject("inputSchema").getAsJsonArray("required").get(0).getAsString());
        Assert.assertFalse("no metadata should be invented for a bare schema", t.has("annotations"));
    }

    /**
     * For a NON third-party (OpenAPI/API-generated) server the input schema is SANITIZED (header/query/path
     * prefixes stripped, {@code contentType} removed) — while any sibling metadata is still emitted.
     */
    @Test
    public void testNonProxyToolSanitizesInputSchemaButKeepsMetadata() {

        String schema = "{"
                + "\"inputSchema\":{\"type\":\"object\","
                + "  \"properties\":{\"query_city\":{\"type\":\"string\"},\"contentType\":{\"type\":\"string\"}},"
                + "  \"required\":[\"query_city\"]},"
                + "\"annotations\":{\"readOnlyHint\":true}"
                + "}";
        List<URLMapping> ops = Collections.singletonList(tool("get_weather", "Returns the weather", schema));

        JsonArray tools = toolsOf(MCPPayloadGenerator.generateToolListPayload(1, ops, false));
        JsonObject t = tools.get(0).getAsJsonObject();

        JsonObject properties = t.getAsJsonObject("inputSchema").getAsJsonObject("properties");
        Assert.assertTrue("query_ prefix must be stripped for non-proxy", properties.has("city"));
        Assert.assertFalse("prefixed key must not remain", properties.has("query_city"));
        Assert.assertFalse("contentType must be removed", properties.has("contentType"));
        Assert.assertTrue("required prefix must be stripped",
                t.getAsJsonObject("inputSchema").getAsJsonArray("required").get(0).getAsString().equals("city"));
        Assert.assertTrue("sibling metadata must still be emitted", t.has("annotations"));
        Assert.assertTrue("annotations.readOnlyHint value must be preserved on the non-proxy path",
                t.getAsJsonObject("annotations").get("readOnlyHint").getAsBoolean());
    }

    /**
     * A null description is omitted from the emitted tool object (never serialized as a null/empty field).
     */
    @Test
    public void testNullDescriptionIsOmitted() {

        List<URLMapping> ops = Collections.singletonList(tool("echo", null, null));

        JsonArray tools = toolsOf(MCPPayloadGenerator.generateToolListPayload(1, ops, true));
        JsonObject t = tools.get(0).getAsJsonObject();

        Assert.assertEquals("echo", t.get("name").getAsString());
        Assert.assertFalse("null description must be omitted", t.has("description"));
    }
}
