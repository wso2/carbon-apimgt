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

package org.wso2.carbon.apimgt.spec.parser.definitions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.swagger.models.properties.AbstractProperty;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.BackendOperation;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

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
                Assert.assertFalse(operation.getVendorExtensions().containsKey(APISpecParserConstants.SWAGGER_X_SCOPE));
            }
        }

        // check updated scopes in security definition
        Operation itemGet = swaggerObj.getPath("/items").getGet();
        Assert.assertTrue(itemGet.getSecurity().get(0).get("default").contains("newScope"));

        // check available scopes in security definition
        OAuth2Definition oAuth2Definition = (OAuth2Definition) swaggerObj.getSecurityDefinitions().get("default");
        Assert.assertTrue(oAuth2Definition.getScopes().containsKey("newScope"));
        Assert.assertEquals("newScopeDescription", oAuth2Definition.getScopes().get("newScope"));

        Assert.assertTrue(oAuth2Definition.getVendorExtensions().containsKey(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS));
        Map<String, String> scopeBinding = (Map<String, String>) oAuth2Definition.getVendorExtensions()
                .get(APISpecParserConstants.SWAGGER_X_SCOPES_BINDINGS);
        Assert.assertTrue(scopeBinding.containsKey("newScope"));
        Assert.assertEquals("admin", scopeBinding.get("newScope"));
    }

    @Test
    public void testGenerateAPIDefinitionWithoutInfoTag() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2Resources.json";
        String oas2Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        SwaggerParser swaggerParser = new SwaggerParser();

        String definition = testGenerateAPIDefinitionWithoutInfoTag(oas2Parser, oas2Resources);
        Swagger swaggerObj = swaggerParser.parse(definition);

        Assert.assertNotNull(swaggerObj.getInfo());
        Assert.assertEquals("simple", swaggerObj.getInfo().getTitle());
        Assert.assertEquals("1.0.0", swaggerObj.getInfo().getVersion());
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
    public void testSwaggerValidatorWithValidationLevel2() throws Exception {
        String faultySwagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "oas_util_test_faulty_swagger.json"),
                String.valueOf(StandardCharsets.UTF_8));
        APIDefinitionValidationResponse response = OASParserUtil.validateAPIDefinition(faultySwagger, true, null);
        Assert.assertFalse(response.isValid());
        Assert.assertEquals(3, response.getErrorItems().size());
        Assert.assertEquals(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode(),
                response.getErrorItems().get(0).getErrorCode());
        Assert.assertEquals(ExceptionCodes.INVALID_OAS2_FOUND.getErrorCode(),
                response.getErrorItems().get(1).getErrorCode());
    }

    @Test
    public void testOpenAPIValidatorWithMultiplePathsHavingSameNameWithAndWithoutTrailingSlash() throws Exception {
        String faultySwagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "oas2_paths_with_trailing_slash.json"),
                "UTF-8");

        APIDefinitionValidationResponse response = OASParserUtil.validateAPIDefinition(faultySwagger, true, null);
        Assert.assertFalse(response.isValid());
        Assert.assertEquals(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode(),
                response.getErrorItems().get(0).getErrorCode());
        Assert.assertEquals("Multiple GET operations with the same resource path /test found in " +
                "the swagger definition", response.getErrorItems().get(0).getErrorDescription());
    }

    @Test
    public void testSwaggerValidatorWithRelaxValidationEnabledAndWithoutInfoTag() throws Exception {
        System.setProperty(APISpecParserConstants.SWAGGER_RELAXED_VALIDATION, "true");
        String withoutInfoTagSwagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                + File.separator + "oas2_without_info_swagger.json"),
                "UTF-8");
        APIDefinitionValidationResponse response = OASParserUtil.validateAPIDefinition(withoutInfoTagSwagger, true,
                null);
        Assert.assertTrue(response.isValid());
        Assert.assertTrue(response.getInfo().getName().startsWith("API-Title-"));
        Assert.assertEquals("attribute info is missing",
                response.getErrorItems().get(0).getErrorDescription());
        System.clearProperty(APISpecParserConstants.SWAGGER_RELAXED_VALIDATION);
    }

    @Test
    public void testRootLevelApplicationSecurity() throws Exception {
        String apiSecurity = "oauth2,oauth_basic_auth_api_key_mandatory,api_key";
        String oasDefinition = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "oas2_app_security.json"),
                "UTF-8");
        String oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "oas2_app_security_key.json"),
                "UTF-8");
        API api = Mockito.mock(API.class);
        when(api.getApiSecurity()).thenReturn(apiSecurity);
        APIDefinition parser = OASParserUtil.getOASParser(oasDefinition);
        String response = parser.getOASDefinitionForPublisher(api, oasDefinition);
        Assert.assertEquals(oasDefinitionEdited, response);
    }

    @Test
    public void testGetOASSecurityDefinitionForPublisher() throws Exception {

        // Testing API with migrated swagger coming from APIM version 2.x without any x-wso2-security or x-scopes.
        String swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2" + File.separator
                        + "publisher" + File.separator + "oas2_mig_without_sec_extensions.json"),
                String.valueOf(StandardCharsets.UTF_8));
        API api = Mockito.mock(API.class);
        String apiSecurity = "oauth_basic_auth_api_key_mandatory,oauth2";
        when(api.getApiSecurity()).thenReturn(apiSecurity);
        APIDefinition parser = OASParserUtil.getOASParser(swagger);
        String response = parser.getOASDefinitionForPublisher(api, swagger);
        String oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2" + File.separator
                        + "publisher" + File.separator + "oas2_mig_without_sec_extensions_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionEdited, response);

        // Testing API with migrated swagger coming from APIM version 2.x with x-wso2-security and x-scopes.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2" + File.separator
                        + "publisher" + File.separator + "oas2_mig_with_sec_extensions.json"),
                String.valueOf(StandardCharsets.UTF_8));
        response = parser.getOASDefinitionForPublisher(api, swagger);
        oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "publisher" + File.separator + "oas2_mig_with_sec_extensions_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionEdited, response);

        // Testing API with swagger generated after APIM 2.x versions with oauth security definitions and x-wso2
        // extensions. API configured with all security.
        apiSecurity = "oauth_basic_auth_api_key_mandatory,api_key,basic_auth,oauth2";
        when(api.getApiSecurity()).thenReturn(apiSecurity);
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "publisher" + File.separator + "oas2_with_default_oauth.json"),
                String.valueOf(StandardCharsets.UTF_8));
        response = parser.getOASDefinitionForPublisher(api, swagger);
        oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "publisher" + File.separator + "oas2_with_default_oauth_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionEdited, response);

        // Testing if the different default implicit authorizationUrl is replaced with the default value.
        // This is a test for the fix 9620. Earlier value was replaced with the default 'https;//test.com value.
        // Now it should not be the case.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2" + File.separator
                        + "publisher" + File.separator + "oas2_with_default_implicit_authorization_url.json"),
                "UTF-8");
        APIIdentifier identifier = new APIIdentifier("admin", "simple", "1.0.0");
        API api2 = new API(identifier);
        response = parser.generateAPIDefinition(new SwaggerData(api2), swagger);
        oasDefinitionEdited= IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2" + File.separator
                        + "publisher" + File.separator + "oas2_with_default_implicit_authorization_url_response.json"),
                "UTF-8");
        Assert.assertEquals(oasDefinitionEdited, response);
    }

    @Test
    public void testGetOASSecurityDefinitionForStore() throws  Exception {

        // Testing API with migrated swagger coming from APIM version 2.x without any x-wso2-security or x-scopes.
        String swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "publisher" + File.separator + "oas2_mig_without_sec_extensions.json"),
                String.valueOf(StandardCharsets.UTF_8));
        APIIdentifier apiIdentifier = new APIIdentifier("admin", "OldAPI", "1.0.0");
        Map<String, String> hostWithSchemes = new HashMap<>();
        hostWithSchemes.put(APISpecParserConstants.HTTPS_PROTOCOL, "https://localhost");
        API api = new API(apiIdentifier);
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,oauth2");
        api.setTransports("https");
        api.setContext("/oldapi");
        api.setScopes(new HashSet<>());
        String response = oas2Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        String oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator
                        + "oas2_mig_without_sec_extensions_response.json"), String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);

        // Testing API with migrated swagger coming from APIM version 2.x with x-wso2-security and x-scopes.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "publisher" + File.separator + "oas2_mig_with_sec_extensions.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setScopes(getAPITestScopes());
        response = oas2Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator + "oas2_mig_with_sec_extensions_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);

        // Testing API with swagger generated after APIM 2.x versions with oauth security definitions and x-wso2
        // extensions. API configured with all security.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator + "oas2_with_default_allsecurity.json"),
                String.valueOf(StandardCharsets.UTF_8));
        apiIdentifier = new APIIdentifier("admin", "SwaggerPetstore", "1.0.6");
        api = new API(apiIdentifier);
        api.setTransports("https");
        api.setContext("/v2");
        api.setScopes(getAPITestScopes());
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,api_key,basic_auth,oauth2");
        response = oas2Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator
                        + "oas2_with_default_allsecurity_response.json"), String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // Testing API with swagger generated after APIM 2.x version, but with basic_auth and api_key security in
        // the scheme which went with as an u2 update for 4.1, then later reverted. API configured with all security.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2" + File.separator
                        + "devportal" + File.separator + "oas2_with_apikey_basic_oauth_security_u2.json"),
                String.valueOf(StandardCharsets.UTF_8));
        response = oas2Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2" + File.separator
                        + "devportal" + File.separator + "oas2_with_apikey_basic_oauth_security_u2_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // Testing API with swagger generated after APIM 2.x versions with oauth security definitions and x-wso2
        // extensions. API configured with basic auth and api key.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator + "oas2_with_basic_apisec.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,api_key,basic_auth");
        response = oas2Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator + "oas2_with_basic_apisec_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // API configured with basic auth only.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator + "oas2_with_basic.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,basic_auth");
        response = oas2Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator + "oas2_with_basic_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // API Configured with api key only.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator + "oas2_with_apikey.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,api_key");
        response = oas2Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "devportal" + File.separator + "oas2_with_apikey_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
    }

    private Set<Scope> getAPITestScopes() {
        Scope petLocalScope = new Scope();
        petLocalScope.setKey("PetLocalScope");
        petLocalScope.setName("PetLocalScope");
        petLocalScope.setRoles("admin");
        petLocalScope.setDescription("");
        Scope globalScope = new Scope();
        globalScope.setName("GlobalScope");
        globalScope.setKey("GlobalScope");
        globalScope.setDescription("desc");
        globalScope.setRoles("");
        Set<Scope> apiScopes = new LinkedHashSet<>();
        apiScopes.add(globalScope);
        apiScopes.add(petLocalScope);
        return apiScopes;
    }

    // -------------------------------------------------------------------------
    // Circular $ref handling in MCP tool schema generation.
    //
    // resolveModel and resolveProperty are mutually recursive. Each used to start a fresh
    // visitedRefs set instead of passing on the one it was given, so a cycle running through a
    // model's properties lost the path and recursed until the stack overflowed. These cover the
    // shapes a self-referential model actually takes, and the case a naive fix breaks: a model
    // referenced twice from unrelated places must still be expanded at both.
    //
    // Every test carries a timeout so a regression fails rather than hanging the build.
    // -------------------------------------------------------------------------

    private static final int CIRCULAR_TIMEOUT_MS = 30000;
    private static final String CIRCULAR_BACKEND_ID = "forum-backend";
    private static final APIIdentifier CIRCULAR_REF_API_ID =
            new APIIdentifier("admin", "ForumAPI", "1.0.0");
    private final ObjectMapper circularMapper = new ObjectMapper();

    private static String swagger20(String path, String bodyRef, String definitions) {
        return "{\n"
                + "  \"swagger\": \"2.0\",\n"
                + "  \"info\": { \"title\": \"Forum\", \"version\": \"1.0\" },\n"
                + "  \"paths\": {\n"
                + "    \"" + path + "\": {\n"
                + "      \"post\": {\n"
                + "        \"parameters\": [\n"
                + "          { \"name\": \"body\", \"in\": \"body\",\n"
                + "            \"schema\": { \"$ref\": \"#/definitions/" + bodyRef + "\" } }\n"
                + "        ],\n"
                + "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n"
                + "      }\n"
                + "    }\n"
                + "  },\n"
                + "  \"definitions\": {\n" + definitions + "\n  }\n"
                + "}";
    }

    private URITemplate mcpTool(String name, String target, String verb) {
        URITemplate template = new URITemplate();
        template.setUriTemplate(name);
        template.setHTTPVerb(APISpecParserConstants.HTTP_VERB_TOOL);
        BackendOperationMapping mapping = new BackendOperationMapping();
        mapping.setBackendId(CIRCULAR_BACKEND_ID);
        BackendOperation op = new BackendOperation();
        op.setTarget(target);
        op.setVerb(APIConstants.SupportedHTTPVerbs.valueOf(verb));
        mapping.setBackendOperation(op);
        template.setBackendOperationMapping(mapping);
        return template;
    }

    private JsonNode generateToolSchema(String definition, String target) throws Exception {
        Set<URITemplate> templates = new LinkedHashSet<>();
        templates.add(mcpTool("theTool", target, "POST"));
        Set<URITemplate> result = oas2Parser.generateMCPTools(definition, CIRCULAR_REF_API_ID,
                CIRCULAR_BACKEND_ID, APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);
        Assert.assertEquals("Expected exactly one generated tool", 1, result.size());
        String schema = result.iterator().next().getSchemaDefinition();
        Assert.assertNotNull("A schema should have been generated", schema);
        return circularMapper.readTree(schema);
    }

    private JsonNode requestBodyProperties(JsonNode schema) {
        JsonNode requestBody = schema.path("properties").path("requestBody");
        Assert.assertTrue("Schema should carry a requestBody property",
                schema.path("properties").has("requestBody"));
        Assert.assertTrue("requestBody should carry its resolved properties",
                requestBody.has("properties"));
        return requestBody.path("properties");
    }

    /**
     * Walks the whole tree looking for a leftover reference. The generated schema carries no
     * definitions section, so any surviving $ref would point at nothing.
     */
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
        String definition = swagger20("/comments", "Comment",
                "    \"Comment\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"required\": [\"text\"],\n"
                + "      \"properties\": {\n"
                + "        \"text\": { \"type\": \"string\" },\n"
                + "        \"parent\": { \"$ref\": \"#/definitions/Comment\" }\n"
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
        String definition = swagger20("/comments", "Comment",
                "    \"Comment\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"text\": { \"type\": \"string\" },\n"
                + "        \"replies\": {\n"
                + "          \"type\": \"array\",\n"
                + "          \"items\": { \"$ref\": \"#/definitions/Comment\" }\n"
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
        String definition = swagger20("/threads", "Thread",
                "    \"Thread\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"title\": { \"type\": \"string\" },\n"
                + "        \"author\": { \"$ref\": \"#/definitions/User\" }\n"
                + "      }\n"
                + "    },\n"
                + "    \"User\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"name\": { \"type\": \"string\" },\n"
                + "        \"lastThread\": { \"$ref\": \"#/definitions/Thread\" }\n"
                + "      }\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/threads"));
        JsonNode author = props.path("author");

        Assert.assertEquals("The intermediate model should expand",
                "string", author.path("properties").path("name").path("type").asText());
        Assert.assertEquals("The back-reference should still be described",
                "object", author.path("properties").path("lastThread").path("type").asText());
    }

    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularRefThroughNestedObjectTerminates() throws Exception {
        String definition = swagger20("/comments", "Comment",
                "    \"Comment\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"text\": { \"type\": \"string\" },\n"
                + "        \"meta\": {\n"
                + "          \"type\": \"object\",\n"
                + "          \"properties\": {\n"
                + "            \"origin\": { \"$ref\": \"#/definitions/Comment\" }\n"
                + "          }\n"
                + "        }\n"
                + "      }\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/comments"));

        Assert.assertEquals("The inline object should survive",
                "object", props.path("meta").path("type").asText());
        Assert.assertEquals("The cycle inside the inline object should be described",
                "object", props.path("meta").path("properties").path("origin").path("type").asText());
    }

    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularRefThroughAllOfTerminates() throws Exception {
        String definition = swagger20("/nodes", "Node",
                "    \"Audit\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": { \"createdBy\": { \"type\": \"string\" } }\n"
                + "    },\n"
                + "    \"Node\": {\n"
                + "      \"allOf\": [\n"
                + "        { \"$ref\": \"#/definitions/Audit\" },\n"
                + "        { \"type\": \"object\",\n"
                + "          \"properties\": {\n"
                + "            \"label\": { \"type\": \"string\" },\n"
                + "            \"child\": { \"$ref\": \"#/definitions/Node\" }\n"
                + "          }\n"
                + "        }\n"
                + "      ]\n"
                + "    }");

        JsonNode schema = generateToolSchema(definition, "/nodes");
        JsonNode props = requestBodyProperties(schema);

        Assert.assertTrue("Properties merged from the composed model should be present",
                props.has("createdBy"));
        Assert.assertTrue("Properties declared inline in the allOf should be present",
                props.has("label"));
        Assert.assertEquals("The circular field should be described, not left as a raw $ref",
                "object", props.path("child").path("type").asText());
        assertNoUnresolvedReference(schema, "");
    }

    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testGeneratedToolSchemaHasNoUnresolvedReference() throws Exception {
        String definition = swagger20("/comments", "Comment",
                "    \"Comment\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"text\": { \"type\": \"string\" },\n"
                + "        \"parent\": { \"$ref\": \"#/definitions/Comment\" },\n"
                + "        \"replies\": {\n"
                + "          \"type\": \"array\",\n"
                + "          \"items\": { \"$ref\": \"#/definitions/Comment\" }\n"
                + "        }\n"
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
        String definition = swagger20("/orders", "Order",
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
                + "        \"billingAddress\": { \"$ref\": \"#/definitions/Address\" },\n"
                + "        \"shippingAddress\": { \"$ref\": \"#/definitions/Address\" }\n"
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
     * Nested allOf where two branches compose the same non-circular model. Resolving the left branch
     * must not leave Common on the visited path and make the right branch skip it.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testAllOfBranchesSharingAModelBothContribute() throws Exception {
        String definition = swagger20("/records", "Wrapper",
                "    \"Common\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": { \"id\": { \"type\": \"string\" } }\n"
                + "    },\n"
                + "    \"Left\": {\n"
                + "      \"allOf\": [\n"
                + "        { \"$ref\": \"#/definitions/Common\" },\n"
                + "        { \"type\": \"object\", \"properties\": { \"leftField\": { \"type\": \"string\" } } }\n"
                + "      ]\n"
                + "    },\n"
                + "    \"Right\": {\n"
                + "      \"allOf\": [\n"
                + "        { \"$ref\": \"#/definitions/Common\" },\n"
                + "        { \"type\": \"object\", \"properties\": { \"rightField\": { \"type\": \"string\" } } }\n"
                + "      ]\n"
                + "    },\n"
                + "    \"Wrapper\": {\n"
                + "      \"allOf\": [\n"
                + "        { \"$ref\": \"#/definitions/Left\" },\n"
                + "        { \"$ref\": \"#/definitions/Right\" }\n"
                + "      ]\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/records"));

        Assert.assertTrue("Left branch should contribute its own field", props.has("leftField"));
        Assert.assertTrue("Right branch should contribute its own field", props.has("rightField"));
        Assert.assertTrue("The shared model must survive both branches", props.has("id"));
    }

    /**
     * Two tools built from one parsed definition. Resolution must not write its results back onto
     * the definition, or the first tool changes what the second one sees.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testResolvingOneToolDoesNotDegradeAnother() throws Exception {
        String definition = "{\n"
                + "  \"swagger\": \"2.0\",\n"
                + "  \"info\": { \"title\": \"Graph\", \"version\": \"1.0\" },\n"
                + "  \"paths\": {\n"
                + "    \"/roots\": { \"post\": {\n"
                + "      \"parameters\": [ { \"name\": \"body\", \"in\": \"body\",\n"
                + "        \"schema\": { \"$ref\": \"#/definitions/Root\" } } ],\n"
                + "      \"responses\": { \"200\": { \"description\": \"OK\" } } } },\n"
                + "    \"/ms\": { \"post\": {\n"
                + "      \"parameters\": [ { \"name\": \"body\", \"in\": \"body\",\n"
                + "        \"schema\": { \"$ref\": \"#/definitions/M\" } } ],\n"
                + "      \"responses\": { \"200\": { \"description\": \"OK\" } } } }\n"
                + "  },\n"
                + "  \"definitions\": {\n"
                + "    \"Root\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"tag\": { \"type\": \"string\" },\n"
                + "        \"first\": { \"$ref\": \"#/definitions/M\" },\n"
                + "        \"second\": { \"$ref\": \"#/definitions/M\" }\n"
                + "      }\n"
                + "    },\n"
                + "    \"M\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"label\": { \"type\": \"string\" },\n"
                + "        \"backRef\": { \"$ref\": \"#/definitions/Root\" }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}";

        Set<URITemplate> templates = new LinkedHashSet<>();
        templates.add(mcpTool("createRoot", "/roots", "POST"));
        templates.add(mcpTool("createM", "/ms", "POST"));

        Set<URITemplate> result = oas2Parser.generateMCPTools(definition, CIRCULAR_REF_API_ID,
                CIRCULAR_BACKEND_ID, APISpecParserConstants.API_SUBTYPE_DIRECT_BACKEND, templates);
        Assert.assertEquals(2, result.size());

        JsonNode mSchema = null;
        for (URITemplate template : result) {
            if ("createM".equals(template.getUriTemplate())) {
                mSchema = circularMapper.readTree(template.getSchemaDefinition());
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
     * An acyclic definition must come through resolution with every attribute intact. Resolution
     * rebuilds properties rather than editing them in place, so anything the rebuild forgets to
     * carry would be lost silently, on definitions that have no cycle at all.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testAcyclicDefinitionKeepsPropertyAttributes() throws Exception {
        String definition = swagger20("/carts", "Cart",
                "    \"Item\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": { \"sku\": { \"type\": \"string\" } }\n"
                + "    },\n"
                + "    \"Cart\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"items\": {\n"
                + "          \"type\": \"array\",\n"
                + "          \"description\": \"Lines in the cart\",\n"
                + "          \"uniqueItems\": true,\n"
                + "          \"minItems\": 1,\n"
                + "          \"maxItems\": 50,\n"
                + "          \"items\": { \"$ref\": \"#/definitions/Item\" }\n"
                + "        },\n"
                + "        \"audit\": {\n"
                + "          \"type\": \"object\",\n"
                + "          \"description\": \"Audit block\",\n"
                + "          \"readOnly\": true,\n"
                + "          \"properties\": { \"createdBy\": { \"$ref\": \"#/definitions/Item\" } }\n"
                + "        }\n"
                + "      }\n"
                + "    }");

        JsonNode props = requestBodyProperties(generateToolSchema(definition, "/carts"));

        JsonNode items = props.path("items");
        Assert.assertEquals("array", items.path("type").asText());
        Assert.assertEquals("Lines in the cart", items.path("description").asText());
        Assert.assertTrue("uniqueItems should survive resolution", items.path("uniqueItems").asBoolean());
        Assert.assertEquals(1, items.path("minItems").asInt());
        Assert.assertEquals(50, items.path("maxItems").asInt());
        Assert.assertEquals("The referenced item type should be expanded", "string",
                items.path("items").path("properties").path("sku").path("type").asText());

        JsonNode audit = props.path("audit");
        Assert.assertEquals("Audit block", audit.path("description").asText());
        Assert.assertTrue("readOnly should survive resolution", audit.path("readOnly").asBoolean());
        Assert.assertEquals("The nested reference should be expanded", "string",
                audit.path("properties").path("createdBy").path("properties").path("sku")
                        .path("type").asText());
    }

    /**
     * Guards the field list copied by OAS2Parser.copyCommonPropertyFields against changes in
     * swagger-models. Resolution rebuilds properties, so a field added to these classes by a library
     * upgrade would be dropped silently from every generated schema. If this fails, review
     * copyCommonPropertyFields before updating the expected names here.
     */
    @Test
    public void testCopiedPropertyFieldsAreStillComplete() {
        assertDeclaredFields(AbstractProperty.class, "name", "type", "format", "example", "xml",
                "required", "position", "description", "title", "readOnly", "allowEmptyValue",
                "access", "vendorExtensions", "booleanValue");
        assertDeclaredFields(ArrayProperty.class, "TYPE", "uniqueItems", "items", "maxItems", "minItems");
        assertDeclaredFields(ObjectProperty.class, "TYPE", "properties");
        assertDeclaredFields(MapProperty.class, "property", "minProperties", "maxProperties");
    }

    private void assertDeclaredFields(Class<?> type, String... expected) {
        Set<String> actual = Arrays.stream(type.getDeclaredFields())
                .filter(f -> !f.isSynthetic())
                .map(Field::getName)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> want = new TreeSet<>(Arrays.asList(expected));
        Assert.assertEquals("swagger-models has changed the fields of " + type.getSimpleName()
                + ". Review OAS2Parser.copyCommonPropertyFields before updating this list.",
                want, actual);
    }

    /**
     * A cycle that runs only through allOf, never through a property. Nothing on that path
     * registers the reference unless resolveModel holds it across the traversal of the resolved
     * model's body, so this recursed until the stack overflowed.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularRefThroughAllOfChainTerminates() throws Exception {
        String definition = swagger20("/nodes", "Alpha",
                "    \"Alpha\": { \"allOf\": [ { \"$ref\": \"#/definitions/Beta\" } ] },\n"
                + "    \"Beta\":  { \"allOf\": [ { \"$ref\": \"#/definitions/Alpha\" } ] }");

        JsonNode schema = generateToolSchema(definition, "/nodes");

        Assert.assertTrue("A requestBody should still be emitted",
                schema.path("properties").has("requestBody"));
        assertNoUnresolvedReference(schema, "");
    }

    /**
     * The cycle runs through a map's value type. resolveProperty handled Ref, Array and Object
     * properties but not Map, so additionalProperties was never traversed.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testCircularRefThroughMapValueTerminates() throws Exception {
        String definition = swagger20("/trees", "Tree",
                "    \"Tree\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"name\": { \"type\": \"string\" },\n"
                + "        \"children\": {\n"
                + "          \"type\": \"object\",\n"
                + "          \"additionalProperties\": { \"$ref\": \"#/definitions/Tree\" }\n"
                + "        }\n"
                + "      }\n"
                + "    }");

        JsonNode schema = generateToolSchema(definition, "/trees");
        JsonNode props = requestBodyProperties(schema);

        Assert.assertEquals("string", props.path("name").path("type").asText());
        Assert.assertTrue("The recursive map field should still be declared", props.has("children"));
        assertNoUnresolvedReference(schema, "");
    }

    /** A map whose value type is not circular must still be expanded. */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testNonCircularMapValueIsExpanded() throws Exception {
        String definition = swagger20("/carts", "Cart",
                "    \"Item\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": { \"sku\": { \"type\": \"string\" } }\n"
                + "    },\n"
                + "    \"Cart\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"entries\": {\n"
                + "          \"type\": \"object\",\n"
                + "          \"additionalProperties\": { \"$ref\": \"#/definitions/Item\" }\n"
                + "        }\n"
                + "      }\n"
                + "    }");

        JsonNode schema = generateToolSchema(definition, "/carts");
        JsonNode props = requestBodyProperties(schema);

        Assert.assertEquals("The map value type should be expanded", "string",
                props.path("entries").path("additionalProperties").path("properties")
                        .path("sku").path("type").asText());
        assertNoUnresolvedReference(schema, "");
    }

    /**
     * A property referencing an allOf model. resolveModel merges composed models, but resolveProperty
     * used to expand a reference only when its target was a plain model, so this came back as an
     * unresolved $ref pointing at a definitions section the generated schema does not carry.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testPropertyReferenceToComposedModelIsExpanded() throws Exception {
        String definition = swagger20("/records", "Record",
                "    \"Common\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": { \"id\": { \"type\": \"string\" } }\n"
                + "    },\n"
                + "    \"Left\": {\n"
                + "      \"allOf\": [\n"
                + "        { \"$ref\": \"#/definitions/Common\" },\n"
                + "        { \"type\": \"object\", \"properties\": { \"leftField\": { \"type\": \"string\" } } }\n"
                + "      ]\n"
                + "    },\n"
                + "    \"Record\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": { \"left\": { \"$ref\": \"#/definitions/Left\" } }\n"
                + "    }");

        JsonNode schema = generateToolSchema(definition, "/records");
        JsonNode left = requestBodyProperties(schema).path("left");

        Assert.assertEquals("The composed model should expand to an object",
                "object", left.path("type").asText());
        Assert.assertEquals("Properties inherited through allOf should be present",
                "string", left.path("properties").path("id").path("type").asText());
        Assert.assertEquals("Properties declared inline in the allOf should be present",
                "string", left.path("properties").path("leftField").path("type").asText());
        assertNoUnresolvedReference(schema, "");
    }

    /**
     * Non-body parameters sit alongside the request body in the generated schema, keyed by location
     * so a query parameter and a header of the same name cannot collide. The $ref parameter is
     * resolved through resolveComponentRef, which now releases its reference on the way out.
     */
    @Test(timeout = CIRCULAR_TIMEOUT_MS)
    public void testParametersAreKeyedByLocationAlongsideCircularBody() throws Exception {
        String definition = "{\n"
                + "  \"swagger\": \"2.0\",\n"
                + "  \"info\": { \"title\": \"Forum\", \"version\": \"1.0\" },\n"
                + "  \"paths\": {\n"
                + "    \"/comments/{tenant}\": {\n"
                + "      \"post\": {\n"
                + "        \"parameters\": [\n"
                + "          { \"$ref\": \"#/parameters/TenantId\" },\n"
                + "          { \"name\": \"limit\", \"in\": \"query\", \"type\": \"integer\" },\n"
                + "          { \"name\": \"body\", \"in\": \"body\",\n"
                + "            \"schema\": { \"$ref\": \"#/definitions/Comment\" } }\n"
                + "        ],\n"
                + "        \"responses\": { \"200\": { \"description\": \"OK\" } }\n"
                + "      }\n"
                + "    }\n"
                + "  },\n"
                + "  \"parameters\": {\n"
                + "    \"TenantId\": { \"name\": \"tenant\", \"in\": \"path\",\n"
                + "      \"required\": true, \"type\": \"string\" }\n"
                + "  },\n"
                + "  \"definitions\": {\n"
                + "    \"Comment\": {\n"
                + "      \"type\": \"object\",\n"
                + "      \"properties\": {\n"
                + "        \"text\": { \"type\": \"string\" },\n"
                + "        \"parent\": { \"$ref\": \"#/definitions/Comment\" }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}";

        JsonNode schema = generateToolSchema(definition, "/comments/{tenant}");
        JsonNode props = schema.path("properties");

        Assert.assertEquals("A $ref parameter should resolve and be keyed by its location",
                "string", props.path("path_tenant").path("type").asText());
        Assert.assertEquals("An inline parameter should be keyed by its location",
                "integer", props.path("query_limit").path("type").asText());
        Assert.assertEquals("The circular body should still resolve alongside the parameters",
                "string", props.path("requestBody").path("properties").path("text").path("type").asText());
        assertNoUnresolvedReference(schema, "");
    }
}
