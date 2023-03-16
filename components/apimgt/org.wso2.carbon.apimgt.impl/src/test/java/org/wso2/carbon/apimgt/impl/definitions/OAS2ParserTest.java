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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.ThrottlingLimit;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class OAS2ParserTest extends OASTestBase {
    private OAS2Parser oas2Parser = new OAS2Parser();

    @Test
    public void testGetURITemplates() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_scopes.json";
        String oas2Scope = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        testGetURITemplates(oas2Parser, oas2Scope);
    }

    @Test
    public void testGetScopes() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_scopes.json";
        String oas2Scope = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        testGetScopes(oas2Parser, oas2Scope);
    }

    @Test
    public void testThrottlingLimitExtension() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_throttling.json";
        String swaggerContent = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath),
                "UTF-8");
        testThrottlingLimitParsing(oas2Parser, swaggerContent);
    }

    @Test
    public void testGenerateAPIDefinition() throws Exception {
        testGenerateAPIDefinition(oas2Parser);
    }

    @Test
    public void testUpdateAPIDefinition() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2Resources.json";
        String oas2Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        OASParserEvaluator evaluator = (definition -> {
            SwaggerParser swaggerParser = new SwaggerParser();
            Swagger swagger = swaggerParser.parse(definition);
            Assert.assertNotNull(swagger);
            Assert.assertEquals(1, swagger.getPaths().size());
            Assert.assertFalse(swagger.getPaths().containsKey("/noresource/{resid}"));
        });
        testGenerateAPIDefinition2(oas2Parser, oas2Resources, evaluator);
    }

    @Test
    public void testUpdateAPIDefinitionWithExtensions() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2Resources.json";
        String oas2Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        SwaggerParser swaggerParser = new SwaggerParser();

        // check remove vendor extensions
        String definition = testGenerateAPIDefinitionWithExtension(oas2Parser, oas2Resources);
        Swagger swaggerObj = swaggerParser.parse(definition);
        boolean isExtensionNotFound =
                swaggerObj.getVendorExtensions() == null || swaggerObj.getVendorExtensions().isEmpty();
        Assert.assertTrue(isExtensionNotFound);
        Assert.assertEquals(2, swaggerObj.getPaths().size());

        Iterator<Map.Entry<String, Path>> itr = swaggerObj.getPaths().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Path> pathEntry = itr.next();
            Path path = pathEntry.getValue();
            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                Operation operation = operationEntry.getValue();
                Assert.assertFalse(operation.getVendorExtensions().containsKey(APIConstants.SWAGGER_X_SCOPE));
            }
        }

        // check updated scopes in security definition
        Operation itemGet = swaggerObj.getPath("/items").getGet();
        Assert.assertTrue(itemGet.getSecurity().get(0).get("default").contains("newScope"));

        // check available scopes in security definition
        OAuth2Definition oAuth2Definition = (OAuth2Definition) swaggerObj.getSecurityDefinitions().get("default");
        Assert.assertTrue(oAuth2Definition.getScopes().containsKey("newScope"));
        Assert.assertEquals("newScopeDescription", oAuth2Definition.getScopes().get("newScope"));

        Assert.assertTrue(oAuth2Definition.getVendorExtensions().containsKey(APIConstants.SWAGGER_X_SCOPES_BINDINGS));
        Map<String, String> scopeBinding = (Map<String, String>) oAuth2Definition.getVendorExtensions()
                .get(APIConstants.SWAGGER_X_SCOPES_BINDINGS);
        Assert.assertTrue(scopeBinding.containsKey("newScope"));
        Assert.assertEquals("admin", scopeBinding.get("newScope"));
    }

    @Test
    public void testGetURITemplatesOfOpenAPI20Spec() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_uri_template.json";
        String swagger = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();
        uriTemplates.add(getUriTemplate("POST", "Application User", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Application", "/*"));
        uriTemplates.add(getUriTemplate("PUT", "None", "/*"));
        uriTemplates.add(getUriTemplate("DELETE", "Any", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Application & Application User", "/abc"));
        Set<URITemplate> uriTemplateSet = oas2Parser.getURITemplates(swagger);
        Assert.assertEquals(uriTemplateSet, uriTemplates);
    }

    @Test
    public void testRemoveResponsesObjectFromOpenAPI20Spec() throws Exception {
        String relativePathSwagger1 = "definitions" + File.separator + "oas2" + File.separator +
                "oas2_uri_template.json";
        String relativePathSwagger2 = "definitions" + File.separator + "oas2" + File.separator +
                "oas2_uri_template_with_responsesObject.json";
        String swaggerWithoutResponsesObject = IOUtils.toString(getClass().getClassLoader().
                getResourceAsStream(relativePathSwagger1), "UTF-8");
        String swaggerWithResponsesObject = IOUtils.toString(getClass().getClassLoader().
                getResourceAsStream(relativePathSwagger2), "UTF-8");
        Swagger swagger = oas2Parser.getSwagger(swaggerWithResponsesObject);
        Assert.assertEquals(oas2Parser.removeResponsesObject(swagger,swaggerWithoutResponsesObject),
                oas2Parser.removeResponsesObject(swagger,swaggerWithResponsesObject));
    }

    @Test
    public void getOASDefinitionForPublisher() throws Exception {
        String relativePath = "definitions" + File.separator + "petstore_v2_throttle_limit.yaml";
        String swagger = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath),
                "UTF-8");
        API api = Mockito.mock(API.class);
        Mockito.when(api.getContext()).thenReturn("/test");
        String swaggerContent = null;
        try {
            swaggerContent = oas2Parser.getOASDefinitionForPublisher(api, swagger);
        } catch (APIManagementException e) {
            Assert.fail();
        }
        Swagger parsedSwagger = new SwaggerParser().parse(swaggerContent);
        Assert.assertNotNull(parsedSwagger);
        // Tests if the default throttling limit is added if no throttling limit is mentioned in the
        // swagger definition.
        Assert.assertNotNull(parsedSwagger.getPaths().get("/pets/{petId}").getGet());
        Assert.assertNotNull(parsedSwagger.getPaths().get("/pets/{petId}").getGet().getVendorExtensions());
        Assert.assertNotNull(parsedSwagger.getPaths().get("/pets/{petId}").getGet().getVendorExtensions()
                .get(APIConstants.SWAGGER_X_THROTTLING_LIMIT));
        Object limitObject = parsedSwagger.getPaths().get("/pets/{petId}").getGet().getVendorExtensions()
                .get(APIConstants.SWAGGER_X_THROTTLING_LIMIT);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.toJsonTree(limitObject).getAsJsonObject();
        Assert.assertEquals("requestCount Mismatched", -1, jsonObject.get("requestCount").getAsInt());

        Assert.assertNotNull(parsedSwagger.getPaths().get("/pets").getGet());
        Assert.assertNotNull(parsedSwagger.getPaths().get("/pets").getGet().getVendorExtensions());
        Assert.assertNotNull(parsedSwagger.getPaths().get("/pets").getGet().getVendorExtensions()
                .get(APIConstants.SWAGGER_X_THROTTLING_LIMIT));
        limitObject = parsedSwagger.getPaths().get("/pets").getGet().getVendorExtensions()
                .get(APIConstants.SWAGGER_X_THROTTLING_LIMIT);
        jsonObject = gson.toJsonTree(limitObject).getAsJsonObject();
        Assert.assertEquals("requestCount Mismatched", 10000, jsonObject.get("requestCount").getAsInt());
        Assert.assertEquals("timeUnit Mismatched", "MINUTE", jsonObject.get("unit").getAsString());

        // Swagger Parser for v2 does not keep the null valued extensions. Still added the test
        // to make sure that the behavior does not change
        Assert.assertNotNull(parsedSwagger.getPaths().get("/pets").getPost());
        Assert.assertNotNull(parsedSwagger.getPaths().get("/pets").getPost().getVendorExtensions());
        Assert.assertNotNull(parsedSwagger.getPaths().get("/pets").getPost().getVendorExtensions()
                .get(APIConstants.SWAGGER_X_THROTTLING_LIMIT));
        limitObject = parsedSwagger.getPaths().get("/pets").getPost().getVendorExtensions()
                .get(APIConstants.SWAGGER_X_THROTTLING_LIMIT);
        jsonObject = gson.toJsonTree(limitObject).getAsJsonObject();
        Assert.assertEquals("requestCount Mismatched", -1, jsonObject.get("requestCount").getAsInt());
        Assert.assertEquals("timeUnit Mismatched", "MINUTE", jsonObject.get("unit").getAsString());
    }
}
