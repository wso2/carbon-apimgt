/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.commons.codegen;

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.rest.api.common.codegen.DynamicHtmlGen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DynamicHtmlGenTestCase {
    @Test
    public void testPreprocessSwagger() throws Exception {
        DynamicHtmlGen htmlGen = new DynamicHtmlGen();
        Swagger swagger = new Swagger();
        Path path = new Path();
        Operation operation = new Operation();
        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");
        tags.add("tag3");
        tags.add("tag4");
        operation.setTags(tags);
        path.setGet(operation);
        swagger.path("get_sample", path);
        htmlGen.preprocessSwagger(swagger);
        List<String> processedTags = swagger.getPath("get_sample").getGet().getTags();
        Assert.assertEquals(processedTags.size(), 1);
        Assert.assertEquals(processedTags.get(0), "tag1");
    }

    @Test
    public void testToApiName() throws Exception {
        DynamicHtmlGen htmlGen = new DynamicHtmlGen();
        final String originalTag = "API (Collection)";
        String sanitised = htmlGen.sanitizeTag(originalTag);
        Assert.assertEquals(sanitised, "APICollection");
        String retrievedTag = htmlGen.toApiName(sanitised);
        Assert.assertEquals(retrievedTag, originalTag);
        sanitised = htmlGen.toApiFilename(originalTag);
        Assert.assertEquals(sanitised, "APICollection");
    }

    @Test
    public void testFromOperation() throws Exception {
        Operation operation = new Operation();
        final String summary = "Sample operation summary";
        final String description = "Sample operation description";
        operation.setDescription(description);
        operation.setSummary(summary);
        DynamicHtmlGen htmlGen = new DynamicHtmlGen();
        CodegenOperation modified = htmlGen.fromOperation("/apis", "GET", operation, null, null);
        Assert.assertEquals(modified.summary, summary);
        Assert.assertEquals(modified.notes, description);
    }

    @Test
    public void testFromParameter() throws Exception {
        Parameter parameter = new QueryParameter();
        parameter.setName("query");
        final String description = "Sample parameter description";
        parameter.setDescription(description);
        DynamicHtmlGen htmlGen = new DynamicHtmlGen();
        CodegenParameter modified = htmlGen.fromParameter(parameter, new HashSet<>());
        Assert.assertEquals(modified.description, description);
    }

    @Test
    public void testEscapeUnsafeCharacters() throws Exception {
        DynamicHtmlGen htmlGen = new DynamicHtmlGen();
        final String stringToEscape = "hello\nworld";
        String result = htmlGen.escapeUnsafeCharacters(stringToEscape);
        Assert.assertEquals(result, stringToEscape); //Shouldn't be escaped
    }

    @Test
    public void testEscapeQuotationMark() throws Exception {
        DynamicHtmlGen htmlGen = new DynamicHtmlGen();
        final String stringToEscape = "\"";
        String result = htmlGen.escapeQuotationMark(stringToEscape);
        Assert.assertEquals(result, stringToEscape); //Shouldn't be escaped
    }
}
