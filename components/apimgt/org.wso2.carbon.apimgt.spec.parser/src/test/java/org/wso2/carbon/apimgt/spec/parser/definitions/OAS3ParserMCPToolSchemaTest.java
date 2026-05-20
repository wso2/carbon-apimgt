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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.BackendOperation;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.api.model.APIOperationMapping;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.APIConstants;

import java.util.HashSet;
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
}
