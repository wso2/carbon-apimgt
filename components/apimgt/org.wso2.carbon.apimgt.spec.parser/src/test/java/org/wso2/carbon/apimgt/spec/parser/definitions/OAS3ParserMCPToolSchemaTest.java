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
package org.wso2.carbon.apimgt.spec.parser.definitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIOperationMapping;
import org.wso2.carbon.apimgt.api.model.BackendOperation;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tests for MCP tool schema generation and preservation in OAS3Parser.updateMCPTools.
 *
 * Covers:
 * - Schema is generated for tools with empty/null schemaDefinition
 * - Schema is NOT regenerated for tools with existing schemaDefinition (guard at line 2818)
 * - Delete + re-add flow: removed tool gets fresh schema from updated backend definition
 * - Consistent behavior for both DIRECT_BACKEND and EXISTING_API subtypes
 */
public class OAS3ParserMCPToolSchemaTest {

    private static final String BACKEND_ID = "test-backend-123";
    private static final APIIdentifier REF_API_ID = new APIIdentifier("admin", "RefAPI", "1.0.0");

    private static final String BACKEND_DEFINITION_V1 =
            "{\n"
            + "  \"openapi\": \"3.0.1\",\n"
            + "  \"info\": { \"title\": \"TestBackend\", \"version\": \"1.0\" },\n"
            + "  \"paths\": {\n"
            + "    \"/get\": {\n"
            + "      \"get\": {\n"
            + "        \"description\": \"Get weather\",\n"
            + "        \"parameters\": [\n"
            + "          { \"name\": \"city\", \"in\": \"query\", \"description\": \"City name\","
            + "            \"schema\": { \"type\": \"string\" } }\n"
            + "        ],\n"
            + "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n"
            + "      }\n"
            + "    },\n"
            + "    \"/post\": {\n"
            + "      \"post\": {\n"
            + "        \"description\": \"Create item\",\n"
            + "        \"requestBody\": {\n"
            + "          \"content\": {\n"
            + "            \"application/json\": {\n"
            + "              \"schema\": {\n"
            + "                \"type\": \"object\",\n"
            + "                \"properties\": {\n"
            + "                  \"name\": { \"type\": \"string\" }\n"
            + "                },\n"
            + "                \"required\": [\"name\"]\n"
            + "              }\n"
            + "            }\n"
            + "          }\n"
            + "        },\n"
            + "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}";

    private static final String BACKEND_DEFINITION_V2 =
            "{\n"
            + "  \"openapi\": \"3.0.1\",\n"
            + "  \"info\": { \"title\": \"TestBackend\", \"version\": \"2.0\" },\n"
            + "  \"paths\": {\n"
            + "    \"/get\": {\n"
            + "      \"get\": {\n"
            + "        \"description\": \"Get weather\",\n"
            + "        \"parameters\": [\n"
            + "          { \"name\": \"city\", \"in\": \"query\", \"description\": \"City name\","
            + "            \"schema\": { \"type\": \"string\" } },\n"
            + "          { \"name\": \"units\", \"in\": \"query\", \"description\": \"Units\","
            + "            \"schema\": { \"type\": \"string\" } }\n"
            + "        ],\n"
            + "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}";

    private static final String OLD_SCHEMA =
            "{\"type\":\"object\",\"properties\":{\"query_city\":{\"type\":\"string\","
            + "\"description\":\"City name\"}},\"required\":[\"query_city\"]}";

    private final OAS3Parser parser = new OAS3Parser();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private URITemplate createDirectBackendTool(String name, String target, String verb, String schema) {
        URITemplate template = new URITemplate();
        template.setUriTemplate(name);
        template.setHTTPVerb(APISpecParserConstants.HTTP_VERB_TOOL);
        if (schema != null) {
            template.setSchemaDefinition(schema);
        }
        BackendOperationMapping mapping = new BackendOperationMapping();
        mapping.setBackendId(BACKEND_ID);
        BackendOperation op = new BackendOperation();
        op.setTarget(target);
        op.setVerb(APIConstants.SupportedHTTPVerbs.valueOf(verb));
        mapping.setBackendOperation(op);
        template.setBackendOperationMapping(mapping);
        return template;
    }

    private URITemplate createExistingApiTool(String name, String target, String verb, String schema) {
        URITemplate template = new URITemplate();
        template.setUriTemplate(name);
        template.setHTTPVerb(APISpecParserConstants.HTTP_VERB_TOOL);
        if (schema != null) {
            template.setSchemaDefinition(schema);
        }
        APIOperationMapping mapping = new APIOperationMapping();
        mapping.setApiUuid("ref-api-uuid");
        mapping.setApiName("RefAPI");
        mapping.setApiVersion("1.0.0");
        BackendOperation op = new BackendOperation();
        op.setTarget(target);
        op.setVerb(APIConstants.SupportedHTTPVerbs.valueOf(verb));
        mapping.setBackendOperation(op);
        template.setAPIOperationMapping(mapping);
        return template;
    }

    // -------------------------------------------------------------------------
    // DIRECT_BACKEND: schema generation tests
    // -------------------------------------------------------------------------

    @Test
    public void testDirectBackend_emptySchemaTriggersGeneration() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createDirectBackendTool("getWeather", "/get", "GET", null));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V1, null, BACKEND_ID,
                APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);

        Assert.assertEquals(1, result.size());
        URITemplate tool = result.iterator().next();
        Assert.assertNotNull("Schema should be generated for empty schemaDefinition",
                tool.getSchemaDefinition());
        Assert.assertFalse("Schema should not be empty",
                tool.getSchemaDefinition().isEmpty());

        JsonNode schema = objectMapper.readTree(tool.getSchemaDefinition());
        Assert.assertTrue("Generated schema should have query_city property",
                schema.has("properties") && schema.get("properties").has("query_city"));
    }

    @Test
    public void testDirectBackend_existingSchemaPreserved() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createDirectBackendTool("getWeather", "/get", "GET", OLD_SCHEMA));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V2, null, BACKEND_ID,
                APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);

        Assert.assertEquals(1, result.size());
        URITemplate tool = result.iterator().next();

        JsonNode schema = objectMapper.readTree(tool.getSchemaDefinition());
        Assert.assertTrue("Existing schema should still have query_city",
                schema.has("properties") && schema.get("properties").has("query_city"));
        Assert.assertFalse("Existing schema should NOT have query_units (not regenerated)",
                schema.has("properties") && schema.get("properties").has("query_units"));
    }

    @Test
    public void testDirectBackend_reAddedToolGetsNewSchema() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createDirectBackendTool("getWeather", "/get", "GET", null));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V2, null, BACKEND_ID,
                APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);

        Assert.assertEquals(1, result.size());
        URITemplate tool = result.iterator().next();

        JsonNode schema = objectMapper.readTree(tool.getSchemaDefinition());
        Assert.assertTrue("Re-added tool should have query_city",
                schema.has("properties") && schema.get("properties").has("query_city"));
        Assert.assertTrue("Re-added tool should have query_units from updated definition",
                schema.has("properties") && schema.get("properties").has("query_units"));
    }

    @Test
    public void testDirectBackend_postToolSchemaGeneration() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createDirectBackendTool("createItem", "/post", "POST", null));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V1, null, BACKEND_ID,
                APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);

        Assert.assertEquals(1, result.size());
        URITemplate tool = result.iterator().next();
        Assert.assertNotNull(tool.getSchemaDefinition());

        JsonNode schema = objectMapper.readTree(tool.getSchemaDefinition());
        Assert.assertTrue("POST tool should have requestBody property",
                schema.has("properties") && schema.get("properties").has("requestBody"));
    }

    // -------------------------------------------------------------------------
    // EXISTING_API: schema generation tests
    // -------------------------------------------------------------------------

    @Test
    public void testExistingApi_emptySchemaTriggersGeneration() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createExistingApiTool("getWeather", "/get", "GET", null));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V1, REF_API_ID, null,
                APISpecParserConstants.API_SUBTYPE_EXISTING_API, templates);

        Assert.assertEquals(1, result.size());
        URITemplate tool = result.iterator().next();
        Assert.assertNotNull("Schema should be generated",
                tool.getSchemaDefinition());

        JsonNode schema = objectMapper.readTree(tool.getSchemaDefinition());
        Assert.assertTrue("Generated schema should have query_city",
                schema.has("properties") && schema.get("properties").has("query_city"));
    }

    @Test
    public void testExistingApi_existingSchemaPreserved() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createExistingApiTool("getWeather", "/get", "GET", OLD_SCHEMA));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V2, REF_API_ID, null,
                APISpecParserConstants.API_SUBTYPE_EXISTING_API, templates);

        Assert.assertEquals(1, result.size());
        URITemplate tool = result.iterator().next();

        JsonNode schema = objectMapper.readTree(tool.getSchemaDefinition());
        Assert.assertTrue("Existing schema should keep query_city",
                schema.has("properties") && schema.get("properties").has("query_city"));
        Assert.assertFalse("Existing schema should NOT have query_units",
                schema.has("properties") && schema.get("properties").has("query_units"));
    }

    @Test
    public void testExistingApi_reAddedToolGetsNewSchema() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createExistingApiTool("getWeather", "/get", "GET", null));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V2, REF_API_ID, null,
                APISpecParserConstants.API_SUBTYPE_EXISTING_API, templates);

        Assert.assertEquals(1, result.size());
        URITemplate tool = result.iterator().next();

        JsonNode schema = objectMapper.readTree(tool.getSchemaDefinition());
        Assert.assertTrue("Re-added tool should have query_city",
                schema.has("properties") && schema.get("properties").has("query_city"));
        Assert.assertTrue("Re-added tool should have query_units",
                schema.has("properties") && schema.get("properties").has("query_units"));
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    public void testEmptyStringSchemaTriggersRegeneration() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createDirectBackendTool("getWeather", "/get", "GET", ""));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V1, null, BACKEND_ID,
                APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);

        Assert.assertEquals(1, result.size());
        URITemplate tool = result.iterator().next();
        Assert.assertNotNull(tool.getSchemaDefinition());
        Assert.assertFalse("Empty string schema should trigger regeneration",
                tool.getSchemaDefinition().isEmpty());
    }

    @Test
    public void testMultipleToolsMixedSchemaState() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createDirectBackendTool("getWeather", "/get", "GET", OLD_SCHEMA));
        templates.add(createDirectBackendTool("createItem", "/post", "POST", null));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V1, null, BACKEND_ID,
                APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);

        Assert.assertEquals(2, result.size());
        for (URITemplate tool : result) {
            Assert.assertNotNull("All tools should have schemas", tool.getSchemaDefinition());
            Assert.assertFalse(tool.getSchemaDefinition().isEmpty());
            if ("getWeather".equals(tool.getUriTemplate())) {
                Assert.assertEquals("Existing tool should keep old schema",
                        OLD_SCHEMA, tool.getSchemaDefinition());
            }
        }
    }

    @Test
    public void testUnmatchedToolPassedThroughWithoutSchema() throws Exception {
        Set<URITemplate> templates = new HashSet<>();
        templates.add(createDirectBackendTool("nonExistentTool", "/nonexistent", "GET", null));

        Set<URITemplate> result = parser.updateMCPTools(
                BACKEND_DEFINITION_V1, null, BACKEND_ID,
                APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);

        Assert.assertEquals(1, result.size());
        URITemplate tool = result.iterator().next();
        Assert.assertNull("Unmatched tool should have no schema generated",
                tool.getSchemaDefinition());
    }

    // -------------------------------------------------------------------------
    // Circular $ref handling.
    //
    // Each of resolveSchema's recursive branches used to start a fresh visitedRefs set instead of
    // passing on the one it was given, so a cycle running through a schema's properties lost the
    // path and recursed until the stack overflowed. A reference that closes a cycle is now replaced
    // with a plain object, so the field stays declared rather than vanishing.
    //
    // Every test carries a timeout so a regression fails rather than hanging the build.
    // -------------------------------------------------------------------------

    private static final int CIRCULAR_TIMEOUT_MS = 30000;

    private static String openApi30(String path, String bodyRef, String schemas) {
        return "{\n"
                + "  \"openapi\": \"3.0.1\",\n"
                + "  \"info\": { \"title\": \"Forum\", \"version\": \"1.0\" },\n"
                + "  \"paths\": {\n"
                + "    \"" + path + "\": {\n"
                + "      \"post\": {\n"
                + "        \"requestBody\": { \"content\": { \"application/json\": {\n"
                + "          \"schema\": { \"$ref\": \"#/components/schemas/" + bodyRef + "\" } } } },\n"
                + "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n"
                + "      }\n"
                + "    }\n"
                + "  },\n"
                + "  \"components\": { \"schemas\": {\n" + schemas + "\n  } }\n"
                + "}";
    }

    private JsonNode generateToolSchema(String definition, String target) throws Exception {
        Set<URITemplate> templates = new LinkedHashSet<>();
        templates.add(createDirectBackendTool("theTool", target, "POST", null));
        Set<URITemplate> result = parser.generateMCPTools(definition, REF_API_ID, BACKEND_ID,
                APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);
        Assert.assertEquals("Expected exactly one generated tool", 1, result.size());
        String schema = result.iterator().next().getSchemaDefinition();
        Assert.assertNotNull("A schema should have been generated", schema);
        return objectMapper.readTree(schema);
    }

    private JsonNode requestBodyProperties(JsonNode schema) {
        Assert.assertTrue("Schema should carry a requestBody property",
                schema.path("properties").has("requestBody"));
        JsonNode requestBody = schema.path("properties").path("requestBody");
        Assert.assertTrue("requestBody should carry its resolved properties",
                requestBody.has("properties"));
        return requestBody.path("properties");
    }

    private void assertNoUnresolvedReference(JsonNode node, String path) {
        if (node.isObject()) {
            Iterator<String> names = node.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                Assert.assertNotEquals("Unresolved reference left at " + path + "/" + name,
                        "$ref", name);
                assertNoUnresolvedReference(node.get(name), path + "/" + name);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                assertNoUnresolvedReference(node.get(i), path + "/" + i);
            }
        }
    }

    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularRefThroughFieldTerminates() throws Exception {
        String definition = openApi30("/comments", "Comment",
                "    \"Comment\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"required\": [\"text\"],\n"
                + "      \"properties\": {\n"
                + "        \"text\": { \"type\": \"string\" },\n"
                + "        \"parent\": { \"$ref\": \"#/components/schemas/Comment\" }\n"
                + "      }\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/comments"));

        Assert.assertEquals("A non-circular sibling should still resolve",
                "string", props.path("text").path("type").asText());
        Assert.assertTrue("The circular field should still be declared", props.has("parent"));
        Assert.assertEquals("An unexpanded cycle should render as a plain object",
                "object", props.path("parent").path("type").asText());
    }

    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularRefThroughListTerminates() throws Exception {
        String definition = openApi30("/comments", "Comment",
                "    \"Comment\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"text\": { \"type\": \"string\" },\n"
                + "        \"replies\": {\n"
                + "          \"type\": \"array\",\n"
                + "          \"items\": { \"$ref\": \"#/components/schemas/Comment\" }\n"
                + "        }\n"
                + "      }\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/comments"));

        Assert.assertEquals("array", props.path("replies").path("type").asText());
        Assert.assertEquals("The recursive item type should still be described",
                "object", props.path("replies").path("items").path("type").asText());
    }

    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testIndirectCircularRefTerminates() throws Exception {
        String definition = openApi30("/threads", "Thread",
                "    \"Thread\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"title\": { \"type\": \"string\" },\n"
                + "        \"author\": { \"$ref\": \"#/components/schemas/User\" }\n"
                + "      }\n"
                + "    },\n"
                + "    \"User\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"name\": { \"type\": \"string\" },\n"
                + "        \"lastThread\": { \"$ref\": \"#/components/schemas/Thread\" }\n"
                + "      }\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/threads"));
        JsonNode author = props.path("author");

        Assert.assertEquals("The intermediate schema should expand",
                "string", author.path("properties").path("name").path("type").asText());
        Assert.assertEquals("The back-reference should still be described",
                "object", author.path("properties").path("lastThread").path("type").asText());
    }

    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularRefThroughAllOfTerminates() throws Exception {
        String definition = openApi30("/nodes", "Node",
                "    \"Audit\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": { \"createdBy\": { \"type\": \"string\" } }\n"
                + "    },\n"
                + "    \"Node\": {\n"
                + "      \"allOf\": [\n"
                + "        { \"$ref\": \"#/components/schemas/Audit\" },\n"
                + "        { \"type\": \"object\",\n"
                + "          \"properties\": {\n"
                + "            \"label\": { \"type\": \"string\" },\n"
                + "            \"child\": { \"$ref\": \"#/components/schemas/Node\" }\n"
                + "          }\n"
                + "        }\n"
                + "      ]\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/nodes"));

        Assert.assertTrue("Properties merged from the composed schema should be present",
                props.has("createdBy"));
        Assert.assertTrue("Properties declared inline in the allOf should be present",
                props.has("label"));
    }

    /** oneOf has no Swagger 2.0 equivalent, so it is only covered here. */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularRefThroughOneOfTerminates() throws Exception {
        String definition = openApi30("/shapes", "Shape",
                "    \"Shape\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"label\": { \"type\": \"string\" },\n"
                + "        \"variant\": {\n"
                + "          \"oneOf\": [\n"
                + "            { \"type\": \"string\" },\n"
                + "            { \"$ref\": \"#/components/schemas/Shape\" }\n"
                + "          ]\n"
                + "        }\n"
                + "      }\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/shapes"));

        Assert.assertEquals("string", props.path("label").path("type").asText());
        Assert.assertTrue("The oneOf field should still be declared", props.has("variant"));
    }

    /** additionalProperties is a separate recursion branch from properties and items. */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularRefThroughAdditionalPropertiesTerminates() throws Exception {
        String definition = openApi30("/trees", "Tree",
                "    \"Tree\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"name\": { \"type\": \"string\" },\n"
                + "        \"children\": {\n"
                + "          \"type\": \"object\",\n"
                + "          \"additionalProperties\": { \"$ref\": \"#/components/schemas/Tree\" }\n"
                + "        }\n"
                + "      }\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/trees"));

        Assert.assertEquals("string", props.path("name").path("type").asText());
        Assert.assertTrue("The recursive map field should still be declared", props.has("children"));
    }

    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testGeneratedToolSchemaHasNoUnresolvedReference() throws Exception {
        String definition = openApi30("/comments", "Comment",
                "    \"Comment\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"text\": { \"type\": \"string\" },\n"
                + "        \"parent\": { \"$ref\": \"#/components/schemas/Comment\" }\n"
                + "      }\n"
                + "    }");

        assertNoUnresolvedReference(generateToolSchema(definition, "/comments"), "");
    }

    /**
     * No cycle at all. Order refers to Address twice, and both occurrences must stay fully
     * expanded - the visited set tracks the current path, not everything seen so far.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testSharedNonCircularRefExpandedAtEveryOccurrence() throws Exception {
        String definition = openApi30("/orders", "Order",
                "    \"Address\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"street\": { \"type\": \"string\" },\n"
                + "        \"city\": { \"type\": \"string\" }\n"
                + "      }\n"
                + "    },\n"
                + "    \"Order\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"billingAddress\": { \"$ref\": \"#/components/schemas/Address\" },\n"
                + "        \"shippingAddress\": { \"$ref\": \"#/components/schemas/Address\" }\n"
                + "      }\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/orders"));

        for (String field : new String[] { "billingAddress", "shippingAddress" }) {
            Assert.assertEquals(field + " should keep its street field", "string",
                    props.path(field).path("properties").path("street").path("type").asText());
            Assert.assertEquals(field + " should keep its city field", "string",
                    props.path(field).path("properties").path("city").path("type").asText());
        }
    }

    /**
     * Root refers to M twice; M refers back to Root. One tool is generated for a Root body and one
     * for an M body. Resolving a schema writes its expanded content back onto the components, so
     * each tool must be resolved against its own parse - otherwise the Root tool's truncation of
     * M.backRef is what the M tool sees, and which tool is degraded depends on iteration order.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testResolvingOneToolDoesNotDegradeAnother() throws Exception {
        String definition = "{\n"
                + "  \"openapi\": \"3.0.1\",\n"
                + "  \"info\": { \"title\": \"Graph\", \"version\": \"1.0\" },\n"
                + "  \"paths\": {\n"
                + "    \"/roots\": { \"post\": {\n"
                + "      \"requestBody\": { \"content\": { \"application/json\": {\n"
                + "        \"schema\": { \"$ref\": \"#/components/schemas/Root\" } } } },\n"
                + "      \"responses\": { \"200\": { \"description\": \"OK\" } } } },\n"
                + "    \"/ms\": { \"post\": {\n"
                + "      \"requestBody\": { \"content\": { \"application/json\": {\n"
                + "        \"schema\": { \"$ref\": \"#/components/schemas/M\" } } } },\n"
                + "      \"responses\": { \"200\": { \"description\": \"OK\" } } } }\n"
                + "  },\n"
                + "  \"components\": { \"schemas\": {\n"
                + "    \"Root\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"tag\": { \"type\": \"string\" },\n"
                + "        \"first\": { \"$ref\": \"#/components/schemas/M\" },\n"
                + "        \"second\": { \"$ref\": \"#/components/schemas/M\" }\n"
                + "      }\n"
                + "    },\n"
                + "    \"M\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"label\": { \"type\": \"string\" },\n"
                + "        \"backRef\": { \"$ref\": \"#/components/schemas/Root\" }\n"
                + "      }\n"
                + "    }\n"
                + "  } }\n"
                + "}";

        Set<URITemplate> templates = new LinkedHashSet<>();
        templates.add(createDirectBackendTool("createRoot", "/roots", "POST", null));
        templates.add(createDirectBackendTool("createM", "/ms", "POST", null));

        Set<URITemplate> result = parser.generateMCPTools(definition, REF_API_ID, BACKEND_ID,
                APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);
        Assert.assertEquals(2, result.size());

        JsonNode mSchema = null;
        for (URITemplate template : result) {
            if ("createM".equals(template.getUriTemplate())) {
                mSchema = objectMapper.readTree(template.getSchemaDefinition());
            }
        }
        Assert.assertNotNull("The M tool should have been generated", mSchema);

        JsonNode backRef = mSchema.path("properties").path("requestBody").path("properties")
                .path("backRef");
        Assert.assertEquals("Root should be expanded inside the M tool regardless of which tool "
                        + "resolved first", "string",
                backRef.path("properties").path("tag").path("type").asText());
    }

    /**
     * A parameter's schema can itself reference a circular component. That is a second entry point
     * into resolution, separate from the request body, and it recursed just as far before the fix.
     *
     * buildUnifiedInputSchema reads only type, format, enum and default off a resolved parameter
     * schema - never its properties - so a circular object parameter is expected to come through as
     * a bare object. Asserted here so the shape is not later "fixed" back into recursion.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularParameterSchemaTerminates() throws Exception {
        String definition = "{\n"
                + "  \"openapi\": \"3.0.1\",\n"
                + "  \"info\": { \"title\": \"Forum\", \"version\": \"1.0\" },\n"
                + "  \"paths\": {\n"
                + "    \"/nodes/{tenant}\": {\n"
                + "      \"post\": {\n"
                + "        \"parameters\": [\n"
                + "          { \"name\": \"tenant\", \"in\": \"path\", \"required\": true,\n"
                + "            \"schema\": { \"type\": \"string\" } },\n"
                + "          { \"name\": \"filter\", \"in\": \"query\",\n"
                + "            \"schema\": { \"$ref\": \"#/components/schemas/Node\" } }\n"
                + "        ],\n"
                + "        \"requestBody\": { \"content\": { \"application/json\": {\n"
                + "          \"schema\": { \"$ref\": \"#/components/schemas/Node\" } } } },\n"
                + "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n"
                + "      }\n"
                + "    }\n"
                + "  },\n"
                + "  \"components\": { \"schemas\": {\n"
                + "    \"Node\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"label\": { \"type\": \"string\" },\n"
                + "        \"child\": { \"$ref\": \"#/components/schemas/Node\" }\n"
                + "      }\n"
                + "    }\n"
                + "  } }\n"
                + "}";

        JsonNode schema = generateToolSchema(definition, "/nodes/{tenant}");
        JsonNode props = schema.path("properties");

        Assert.assertEquals("Parameters should be keyed by their location",
                "string", props.path("path_tenant").path("type").asText());
        Assert.assertEquals("A circular parameter schema should resolve to a bare object",
                "object", props.path("query_filter").path("type").asText());
        Assert.assertEquals("The body should still expand alongside the parameters",
                "string", props.path("requestBody").path("properties").path("label").path("type").asText());
        assertNoUnresolvedReference(schema, "");
    }
}
