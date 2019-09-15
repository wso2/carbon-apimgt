/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.definitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.parser.ObjectMapperFactory;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.File;
import java.io.IOException;

public class OASParserUtilTest {

    @Test
    public void testGetOASParser() throws Exception {
        String oas3 = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_v3.yaml"),
                "UTF-8");
        APIDefinition definition = OASParserUtil.getOASParser(oas3);
        Assert.assertNotNull(definition);
        Assert.assertTrue(definition instanceof OAS3Parser);

        String oas2 = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_v2.yaml"),
                "UTF-8");
        definition = OASParserUtil.getOASParser(oas2);
        Assert.assertNotNull(definition);
        Assert.assertTrue(definition instanceof OAS2Parser);

        String oasError = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_error.json"),
                "UTF-8");
        try {
            definition = OASParserUtil.getOASParser(oasError);
            Assert.fail("Exception expected");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getCause() instanceof IOException);
        }

        String oasInvalid = IOUtils.toString(getClass().getClassLoader()
                .getResourceAsStream("definitions" + File.separator + "petstore_invalid.yaml"), "UTF-8");
        try {
            definition = OASParserUtil.getOASParser(oasInvalid);
            Assert.fail("Exception expected");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid OAS definition provided."));
        }
    }

    @Test
    public void testValidateAPIDefinition() {
    }

    @Test
    public void testUpdateValidationResponseAsSuccess() {
    }

    @Test
    public void testAddErrorToValidationResponse() {
    }

    @Test
    public void testGetSwaggerJsonString() {
    }

    @Test
    public void testValidateAPIDefinitionByURL() {
    }

    @Test
    public void test() throws Exception {
        String oas3 = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_v3.yaml"),
                        "UTF-8");
        String oas2 = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "petstore_v2.yaml"),
                        "UTF-8");

        ObjectMapper mapper;
        mapper = ObjectMapperFactory.createYaml();
        JsonNode rootNode = mapper.readTree(oas3.getBytes());
        ObjectNode node = (ObjectNode)rootNode;
        JsonNode v = node.get("openapi");
        System.out.println(v!=null && v.asText().startsWith("3."));
    }
}