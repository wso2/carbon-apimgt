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
}
