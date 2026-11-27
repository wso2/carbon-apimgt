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

package org.wso2.carbon.apimgt.impl.importexport.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.error.YAMLException;

public class CommonUtilTest {

    @Test
    public void testYamlToJsonResolvesAnchorAlias() throws Exception {
        String yaml =
                "defaults:\n" +
                "  required: &req\n" +
                "    - name\n" +
                "    - type\n" +
                "schema:\n" +
                "  required: *req\n";

        String json = CommonUtil.yamlToJson(yaml);
        JsonNode root = new ObjectMapper().readTree(json);

        JsonNode required = root.path("schema").path("required");
        Assert.assertTrue("'required' must be an array after alias resolution", required.isArray());
        Assert.assertEquals(2, required.size());
        Assert.assertEquals("name", required.get(0).asText());
        Assert.assertEquals("type", required.get(1).asText());
    }

    @Test
    public void testYamlToJsonKeepsDateScalarsAsStrings() throws Exception {

        String yaml = "version: 2024-01-01\n" +
                "createdTime: 2024-01-01T10:00:00Z\n" +
                "count: 5\n" +
                "enabled: true\n";

        JsonNode node = new ObjectMapper().readTree(CommonUtil.yamlToJson(yaml));
        Assert.assertTrue(node.get("version").isTextual());
        Assert.assertEquals("2024-01-01", node.get("version").asText());
        Assert.assertTrue(node.get("createdTime").isTextual());
        Assert.assertEquals("2024-01-01T10:00:00Z", node.get("createdTime").asText());
        Assert.assertTrue(node.get("count").isInt());
        Assert.assertTrue(node.get("enabled").isBoolean());
    }

    @Test
    public void testYamlToJsonKeepsScalarTypes() throws Exception {

        String yaml = "data:\n" +
                "  lastUpdatedTime: 2024-01-01 10:00:00.0\n" +
                "  defaultValue: 2024-01-01\n" +
                "  taggedDate: !!timestamp 2024-01-01\n" +
                "  quotedDate: \"2024-01-01\"\n" +
                "  dates: [2024-01-01, 2024-12-31]\n" +
                "  version: 1.0.0\n" +
                "  ratio: 1.5\n" +
                "  disabled: false\n" +
                "  empty: null\n" +
                "  tilde: ~\n";

        JsonNode data = new ObjectMapper().readTree(CommonUtil.yamlToJson(yaml)).get("data");
        Assert.assertEquals("2024-01-01 10:00:00.0", data.get("lastUpdatedTime").textValue());
        Assert.assertEquals("2024-01-01", data.get("defaultValue").textValue());
        Assert.assertEquals("2024-01-01", data.get("taggedDate").textValue());
        Assert.assertEquals("2024-01-01", data.get("quotedDate").textValue());
        Assert.assertEquals("2024-01-01", data.get("dates").get(0).textValue());
        Assert.assertEquals("2024-12-31", data.get("dates").get(1).textValue());
        Assert.assertEquals("1.0.0", data.get("version").textValue());
        Assert.assertTrue(data.get("ratio").isDouble());
        Assert.assertEquals(1.5, data.get("ratio").doubleValue(), 0.0);
        Assert.assertTrue(data.get("disabled").isBoolean());
        Assert.assertFalse(data.get("disabled").booleanValue());
        Assert.assertTrue(data.get("empty").isNull());
        Assert.assertTrue(data.get("tilde").isNull());
    }

    @Test
    public void testYamlToJsonRejectsYamlAboveDefaultCodePointLimit() throws Exception {

        // The default SnakeYAML code point limit is 3 MB.
        int limit = new LoaderOptions().getCodePointLimit();
        StringBuilder yaml = new StringBuilder(limit + 64);
        int i = 0;
        while (yaml.length() <= limit) {
            yaml.append("date").append(i++).append(": 2024-01-01\n");
        }
        try {
            CommonUtil.yamlToJson(yaml.toString());
            Assert.fail("YAML above the code point limit should be rejected");
        } catch (YAMLException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("exceeds the limit"));
        }
    }

    @Test(expected = YAMLException.class)
    public void testYamlToJsonRejectsMalformedYaml() throws Exception {

        CommonUtil.yamlToJson("version: 2024-01-01\n  - broken: [\n");
    }
}
