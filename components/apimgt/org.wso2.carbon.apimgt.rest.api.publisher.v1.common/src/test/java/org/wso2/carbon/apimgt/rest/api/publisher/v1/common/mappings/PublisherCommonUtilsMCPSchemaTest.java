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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for MCP server schema definition preservation logic in PublisherCommonUtils.
 *
 * Covers:
 * - populateExistingSchemaDefinitions: existing tool schemas are preserved, user edits ignored
 * - New tools (not in originalAPI) get empty schema so OAS3Parser can regenerate
 * - Behavior is consistent for both DIRECT_BACKEND and EXISTING_API subtypes
 */
public class PublisherCommonUtilsMCPSchemaTest {

    private static final Log log = LogFactory.getLog(PublisherCommonUtilsMCPSchemaTest.class);

    private static final String OLD_SCHEMA =
            "{\"type\":\"object\",\"properties\":{\"query_city\":{\"type\":\"string\"}}}";
    private static final String USER_MODIFIED_SCHEMA =
            "{\"type\":\"object\",\"properties\":{\"query_city\":{\"type\":\"string\"},"
                    + "\"query_fake\":{\"type\":\"string\"}}}";

    private Method populateExistingSchemaDefinitions;

    @Before
    public void init() throws Exception {
        populateExistingSchemaDefinitions = PublisherCommonUtils.class.getDeclaredMethod(
                "populateExistingSchemaDefinitions", API.class, Set.class);
        populateExistingSchemaDefinitions.setAccessible(true);
        log.debug("Successfully initialized populateExistingSchemaDefinitions method for testing");
    }

    private URITemplate createToolTemplate(String name, String schema) {
        URITemplate template = new URITemplate();
        template.setUriTemplate(name);
        template.setHTTPVerb(APIConstants.MCP.MCP_DEFAULT_FEATURE_TYPE);
        if (schema != null) {
            template.setSchemaDefinition(schema);
        }
        return template;
    }

    private API createApiWithTemplates(Set<URITemplate> templates) {
        APIIdentifier apiId = new APIIdentifier("admin", "TestMCP", "1.0.0");
        API api = new API(apiId);
        api.setUriTemplates(templates);
        return api;
    }

    // -------------------------------------------------------------------------
    // populateExistingSchemaDefinitions — unit tests
    // -------------------------------------------------------------------------

    @Test
    public void testExistingToolSchemaIsPreserved() throws Exception {
        Set<URITemplate> existingTemplates = new HashSet<>();
        existingTemplates.add(createToolTemplate("getWeather", OLD_SCHEMA));

        Set<URITemplate> importedTemplates = new HashSet<>();
        importedTemplates.add(createToolTemplate("getWeather", null));

        API apiToUpdate = createApiWithTemplates(importedTemplates);

        populateExistingSchemaDefinitions.invoke(null, apiToUpdate, existingTemplates);
        log.info("Populated existing schema definitions for API: " + apiToUpdate.getId().getApiName()
                + " with " + existingTemplates.size() + " existing templates");

        URITemplate result = apiToUpdate.getUriTemplates().iterator().next();
        Assert.assertEquals("Existing tool schema should be preserved from originalAPI",
                OLD_SCHEMA, result.getSchemaDefinition());
    }

    @Test
    public void testUserModifiedSchemaIsOverwrittenByExisting() throws Exception {
        Set<URITemplate> existingTemplates = new HashSet<>();
        existingTemplates.add(createToolTemplate("getWeather", OLD_SCHEMA));

        Set<URITemplate> importedTemplates = new HashSet<>();
        importedTemplates.add(createToolTemplate("getWeather", USER_MODIFIED_SCHEMA));

        API apiToUpdate = createApiWithTemplates(importedTemplates);

        populateExistingSchemaDefinitions.invoke(null, apiToUpdate, existingTemplates);
        log.info("Populated existing schema definitions for API: " + apiToUpdate.getId().getApiName()
                + " with " + existingTemplates.size() + " existing templates");

        URITemplate result = apiToUpdate.getUriTemplates().iterator().next();
        Assert.assertEquals("User-modified schema should be overwritten by existing schema",
                OLD_SCHEMA, result.getSchemaDefinition());
    }

    @Test
    public void testNewToolGetsEmptySchema() throws Exception {
        Set<URITemplate> existingTemplates = new HashSet<>();
        existingTemplates.add(createToolTemplate("getWeather", OLD_SCHEMA));

        Set<URITemplate> importedTemplates = new HashSet<>();
        importedTemplates.add(createToolTemplate("getWeather", null));
        importedTemplates.add(createToolTemplate("getTime", null));

        API apiToUpdate = createApiWithTemplates(importedTemplates);

        populateExistingSchemaDefinitions.invoke(null, apiToUpdate, existingTemplates);
        log.info("Populated existing schema definitions for API: " + apiToUpdate.getId().getApiName()
                + " with " + existingTemplates.size() + " existing templates");

        for (URITemplate t : apiToUpdate.getUriTemplates()) {
            if ("getWeather".equals(t.getUriTemplate())) {
                Assert.assertEquals("Existing tool should keep old schema",
                        OLD_SCHEMA, t.getSchemaDefinition());
            } else if ("getTime".equals(t.getUriTemplate())) {
                Assert.assertEquals("New tool should get empty schema for regeneration",
                        "", t.getSchemaDefinition());
            }
        }
    }

    @Test
    public void testDeletedToolNotPropagated() throws Exception {
        Set<URITemplate> existingTemplates = new HashSet<>();
        existingTemplates.add(createToolTemplate("getWeather", OLD_SCHEMA));
        existingTemplates.add(createToolTemplate("getTime", "{\"type\":\"object\"}"));

        Set<URITemplate> importedTemplates = new HashSet<>();
        importedTemplates.add(createToolTemplate("getWeather", null));

        API apiToUpdate = createApiWithTemplates(importedTemplates);

        populateExistingSchemaDefinitions.invoke(null, apiToUpdate, existingTemplates);
        log.info("Populated existing schema definitions for API: " + apiToUpdate.getId().getApiName()
                + " with " + existingTemplates.size() + " existing templates");

        Assert.assertEquals("Only imported tools should remain", 1, apiToUpdate.getUriTemplates().size());
        URITemplate result = apiToUpdate.getUriTemplates().iterator().next();
        Assert.assertEquals("getWeather", result.getUriTemplate());
        Assert.assertEquals(OLD_SCHEMA, result.getSchemaDefinition());
    }

    @Test
    public void testNonToolVerbsAreSkipped() throws Exception {
        Set<URITemplate> existingTemplates = new HashSet<>();
        existingTemplates.add(createToolTemplate("getWeather", OLD_SCHEMA));

        URITemplate nonToolTemplate = new URITemplate();
        nonToolTemplate.setUriTemplate("/menu");
        nonToolTemplate.setHTTPVerb("GET");
        nonToolTemplate.setSchemaDefinition("user-supplied");

        Set<URITemplate> importedTemplates = new HashSet<>();
        importedTemplates.add(nonToolTemplate);

        API apiToUpdate = createApiWithTemplates(importedTemplates);

        populateExistingSchemaDefinitions.invoke(null, apiToUpdate, existingTemplates);
        log.info("Populated existing schema definitions for API: " + apiToUpdate.getId().getApiName()
                + " with " + existingTemplates.size() + " existing templates");

        URITemplate result = apiToUpdate.getUriTemplates().iterator().next();
        Assert.assertEquals("Non-TOOL verb templates should not be modified",
                "user-supplied", result.getSchemaDefinition());
    }

    @Test
    public void testEmptyExistingTemplates() throws Exception {
        Set<URITemplate> existingTemplates = new HashSet<>();

        Set<URITemplate> importedTemplates = new HashSet<>();
        importedTemplates.add(createToolTemplate("getWeather", USER_MODIFIED_SCHEMA));

        API apiToUpdate = createApiWithTemplates(importedTemplates);

        populateExistingSchemaDefinitions.invoke(null, apiToUpdate, existingTemplates);
        log.info("Populated existing schema definitions for API: " + apiToUpdate.getId().getApiName()
                + " with " + existingTemplates.size() + " existing templates");

        URITemplate result = apiToUpdate.getUriTemplates().iterator().next();
        Assert.assertEquals("With no existing templates, schema should be empty for regeneration",
                "", result.getSchemaDefinition());
    }

    @Test
    public void testMultipleToolsPreservedCorrectly() throws Exception {
        String schemaA = "{\"type\":\"object\",\"properties\":{\"a\":{\"type\":\"string\"}}}";
        String schemaB = "{\"type\":\"object\",\"properties\":{\"b\":{\"type\":\"number\"}}}";

        Set<URITemplate> existingTemplates = new HashSet<>();
        existingTemplates.add(createToolTemplate("toolA", schemaA));
        existingTemplates.add(createToolTemplate("toolB", schemaB));

        Set<URITemplate> importedTemplates = new HashSet<>();
        importedTemplates.add(createToolTemplate("toolA", USER_MODIFIED_SCHEMA));
        importedTemplates.add(createToolTemplate("toolB", USER_MODIFIED_SCHEMA));
        importedTemplates.add(createToolTemplate("toolC", "user-new-schema"));

        API apiToUpdate = createApiWithTemplates(importedTemplates);

        populateExistingSchemaDefinitions.invoke(null, apiToUpdate, existingTemplates);
        log.info("Populated existing schema definitions for API: " + apiToUpdate.getId().getApiName()
                + " with " + existingTemplates.size() + " existing templates");

        for (URITemplate t : apiToUpdate.getUriTemplates()) {
            switch (t.getUriTemplate()) {
                case "toolA":
                    Assert.assertEquals("toolA should have original schema",
                            schemaA, t.getSchemaDefinition());
                    break;
                case "toolB":
                    Assert.assertEquals("toolB should have original schema",
                            schemaB, t.getSchemaDefinition());
                    break;
                case "toolC":
                    Assert.assertEquals("toolC (new) should have empty schema",
                            "", t.getSchemaDefinition());
                    break;
                default:
                    Assert.fail("Unexpected template: " + t.getUriTemplate());
            }
        }
    }
}
