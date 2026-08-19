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

package org.wso2.carbon.apimgt.impl;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link MCPInitializerAndToolFetcher#buildToolMetadata(JSONObject)} — the helper that,
 * for a proxied MCP server, retains the complete tool definition (every field except {@code name} and
 * {@code description}, which are served from their own columns) so metadata such as {@code annotations},
 * {@code _meta} and {@code outputSchema} is not lost.
 */
public class MCPInitializerAndToolFetcherTest {

    /**
     * A tool carrying metadata beyond the basic three fields keeps every field EXCEPT name/description —
     * this is the core of the metadata-preservation fix (annotations, _meta, outputSchema, title, execution
     * survive).
     */
    @Test
    public void testBuildToolMetadataRetainsAllFieldsExceptNameAndDescription() {

        JSONObject inputSchema = new JSONObject()
                .put("type", "object")
                .put("properties", new JSONObject().put("message", new JSONObject().put("type", "string")));
        JSONObject annotations = new JSONObject().put("readOnlyHint", true).put("title", "Echo");
        JSONObject meta = new JSONObject().put("vendor", "wso2").put("weight", 5);
        JSONObject outputSchema = new JSONObject()
                .put("type", "object")
                .put("properties", new JSONObject().put("result", new JSONObject().put("type", "string")));
        JSONObject execution = new JSONObject().put("mode", "async").put("timeout", 30);

        JSONObject toolJson = new JSONObject()
                .put("name", "echo")
                .put("description", "Echoes the provided message")
                .put("title", "Echo Tool")
                .put("inputSchema", inputSchema)
                .put("annotations", annotations)
                .put("_meta", meta)
                .put("outputSchema", outputSchema)
                .put("execution", execution);

        JSONObject result = MCPInitializerAndToolFetcher.buildToolMetadata(toolJson);

        Assert.assertFalse("name must not be retained in the schema definition", result.has("name"));
        Assert.assertFalse("description must not be retained in the schema definition", result.has("description"));

        Assert.assertTrue("inputSchema must be retained", result.has("inputSchema"));
        Assert.assertTrue("annotations must be retained", result.has("annotations"));
        Assert.assertTrue("_meta must be retained", result.has("_meta"));
        Assert.assertTrue("outputSchema must be retained", result.has("outputSchema"));
        Assert.assertTrue("title must be retained", result.has("title"));
        Assert.assertTrue("execution must be retained", result.has("execution"));

        Assert.assertEquals("Echo Tool", result.getString("title"));
        Assert.assertTrue("annotations value must be preserved verbatim",
                annotations.similar(result.getJSONObject("annotations")));
        Assert.assertTrue("_meta value must be preserved verbatim",
                meta.similar(result.getJSONObject("_meta")));
        Assert.assertTrue("outputSchema value must be preserved verbatim",
                outputSchema.similar(result.getJSONObject("outputSchema")));
        Assert.assertTrue("inputSchema value must be preserved verbatim",
                inputSchema.similar(result.getJSONObject("inputSchema")));
        Assert.assertTrue("execution value must be preserved verbatim",
                execution.similar(result.getJSONObject("execution")));
    }

    /**
     * A minimal tool with only name and description yields an empty metadata object (nothing to retain).
     */
    @Test
    public void testBuildToolMetadataWithOnlyNameAndDescriptionYieldsEmptyObject() {

        JSONObject toolJson = new JSONObject()
                .put("name", "ping")
                .put("description", "No-op tool");

        JSONObject result = MCPInitializerAndToolFetcher.buildToolMetadata(toolJson);

        Assert.assertEquals("no fields other than name/description should remain", 0, result.length());
    }

    /**
     * The common legacy shape (name/description/inputSchema, no extra metadata) yields exactly the input
     * schema under the {@code inputSchema} key — proving backward compatibility of the stored definition.
     */
    @Test
    public void testBuildToolMetadataWithInputSchemaOnlyRetainsInputSchema() {

        JSONObject inputSchema = new JSONObject().put("type", "object").put("properties", new JSONObject());
        JSONObject toolJson = new JSONObject()
                .put("name", "get_pets")
                .put("description", "Returns the list of pets")
                .put("inputSchema", inputSchema);

        JSONObject result = MCPInitializerAndToolFetcher.buildToolMetadata(toolJson);

        Assert.assertEquals("only inputSchema should remain", 1, result.length());
        Assert.assertTrue("inputSchema must be retained", result.has("inputSchema"));
        Assert.assertTrue(inputSchema.similar(result.getJSONObject("inputSchema")));
    }
}
