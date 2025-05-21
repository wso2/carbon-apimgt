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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.model.APIResourceMediationPolicy;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

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
        APIDefinitionValidationResponse response = OASParserUtil.validateAPIDefinition(faultySwagger, true);
        Assert.assertFalse(response.isValid());
        Assert.assertEquals(3, response.getErrorItems().size());
        Assert.assertEquals(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode(),
                response.getErrorItems().get(0).getErrorCode());
        Assert.assertEquals(ExceptionCodes.INVALID_OAS2_FOUND.getErrorCode(),
                response.getErrorItems().get(1).getErrorCode());
    }

    @Test
    public void getGeneratedExamples() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_uri_template.json";
        String openApi = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath),
                StandardCharsets.UTF_8);
        Map<String, Object> responseMap = oas2Parser.generateExample(openApi);
        String swaggerString = (String) responseMap.get(APIConstants.SWAGGER);
        Map<String, Object> responseMap1 = oas2Parser.getGeneratedExamples(swaggerString);
        Assert.assertNotNull(responseMap1);
        Assert.assertTrue(responseMap1.containsKey(APIConstants.MOCK_GEN_POLICY_LIST));
        List<APIResourceMediationPolicy> apiResourceMediationPolicyList = (List<APIResourceMediationPolicy>) responseMap1.get(
                APIConstants.MOCK_GEN_POLICY_LIST);
        Assert.assertFalse(apiResourceMediationPolicyList.isEmpty());
        APIResourceMediationPolicy apiResourceMediationPolicy = apiResourceMediationPolicyList.get(4);
        Assert.assertEquals("/abc", apiResourceMediationPolicy.getPath());
        String content = apiResourceMediationPolicy.getContent();
        Assert.assertTrue(content.contains("responses[200][\"application/json\"] = \"\";"));
    }

    @Test
    public void testAddScriptsAndMockDataset() throws Exception {
        String relativePath = "definitions" + File.separator + "oas2" + File.separator + "oas2_uri_template.json";
        String openApi = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath),
                StandardCharsets.UTF_8);
        Map<String, Object> responseMap = oas2Parser.generateExample(openApi);
        String swaggerString = (String) responseMap.get(APIConstants.SWAGGER);

        //Test for adding scripts and mockDataset Completely
        //Sample LLM Response
        String llmResponseFull = "{" + "\"mockDataset\": \"{\\\"mockResponses\\\":[" + "{\\\"id\\\":200,\\\"mockResponse\\\":\\\"mockResponse200\\\"}," + "{\\\"id\\\":404,\\\"mockResponse\\\":\\\"mockResponse404\\\"}," + "{\\\"id\\\":\\\"4XX\\\",\\\"mockResponse\\\":\\\"mockResponse4XX\\\"}" + "]}\"," + "\"paths\": {" + "  \"/*\": {" + "    \"get\": \"var a=mc.getProperty('AcceptHeader')||'application/json';\\n" + "if(!a||a=='*/*')a='application/json';\\n" + "mc.setProperty('CONTENT_TYPE',a);\\n" + "var db=JSON.parse(mc.getProperty('mockDataset')||'{\\\\\\\"mockResponses\\\\\\\":[]}');\\n" + "mc.setProperty('HTTP_SC','200');\\n" + "mc.setPayloadJSON(db.mockResponses);\"," + "    \"post\": \"var a=mc.getProperty('AcceptHeader')||'application/json';\\n" + "if(!a||a=='*/*')a='application/json';\\n" + "mc.setProperty('CONTENT_TYPE',a);\\n" + "var db=JSON.parse(mc.getProperty('mockDataset')||'{\\\\\\\"mockResponses\\\\\\\":[]}');\\n" + "mc.setProperty('HTTP_SC','200');\\n" + "mc.setPayloadJSON(db.mockResponses);\"," + "    \"put\": \"var a=mc.getProperty('AcceptHeader')||'application/json';\\n" + "if(!a||a=='*/*')a='application/json';\\n" + "mc.setProperty('CONTENT_TYPE',a);\\n" + "var db=JSON.parse(mc.getProperty('mockDataset')||'{\\\\\\\"mockResponses\\\\\\\":[]}');\\n" + "mc.setProperty('HTTP_SC','200');\\n" + "mc.setPayloadJSON(db.mockResponses);\"," + "    \"delete\": \"var a=mc.getProperty('AcceptHeader')||'application/json';\\n" + "if(!a||a=='*/*')a='application/json';\\n" + "mc.setProperty('CONTENT_TYPE',a);\\n" + "var db=JSON.parse(mc.getProperty('mockDataset')||'{\\\\\\\"mockResponses\\\\\\\":[]}');\\n" + "mc.setProperty('HTTP_SC','200');\\n" + "mc.setPayloadJSON(db.mockResponses);\"" + "  }," + "  \"/abc\": {" + "    \"get\": \"var a=mc.getProperty('AcceptHeader')||'application/json';\\n" + "if(!a||a=='*/*')a='application/json';\\n" + "mc.setProperty('CONTENT_TYPE',a);\\n" + "var db=JSON.parse(mc.getProperty('mockDataset')||'{\\\\\\\"mockResponses\\\\\\\":[]}');\\n" + "mc.setProperty('HTTP_SC','200');\\n" + "mc.setPayloadJSON(db.mockResponses);\"" + "  }" + "}" + "}";

        Map mockConfig1 = Map.of();
        JsonObject llmResponseJson = JsonParser.parseString(llmResponseFull).getAsJsonObject();

        Map<String, Object> responseMap1 = oas2Parser.addScriptsAndMockDataset(swaggerString, mockConfig1,
                llmResponseJson);
        Assert.assertNotNull(responseMap1);
        Assert.assertTrue(responseMap1.containsKey(APIConstants.MOCK_GEN_POLICY_LIST));
        Assert.assertTrue(responseMap1.containsKey(APIConstants.SWAGGER));
        String swaggerString1 = (String) responseMap1.get(APIConstants.SWAGGER);
        Assert.assertTrue(swaggerString1.contains(APIConstants.X_WSO2_MOCK_DATASET));
        List<APIResourceMediationPolicy> apiResourceMediationPolicyList = (List<APIResourceMediationPolicy>) responseMap1.get(
                APIConstants.MOCK_GEN_POLICY_LIST);
        Assert.assertFalse(apiResourceMediationPolicyList.isEmpty());
        APIResourceMediationPolicy apiResourceMediationPolicy = apiResourceMediationPolicyList.get(4);
        Assert.assertEquals("/abc", apiResourceMediationPolicy.getPath());
        String content = apiResourceMediationPolicy.getContent();
        Assert.assertTrue(content.contains("var db=JSON.parse(mc.getProperty('mockDataset')"));

        //Test for modifying a method
        String llmResponseModify = "{" + "\"modified_script\": \"var accept=mc.getProperty('AcceptHeader')||'application/json';\\n" + "if(!accept||accept=='*/*')accept='application/json';\\n" + "mc.setProperty('CONTENT_TYPE',accept);\\n" + "var db=JSON.parse(mc.getProperty('mockDataset')||'{\\\\\\\"mockResponses\\\\\\\":" + "[{\\\"id\\\":200,\\\"mockResponse\\\":\\\"mockResponse200ToTestIfModified\\\"}]}');\\n" + "mc.setProperty('HTTP_SC','200');\\n" + "mc.setPayloadJSON(db.mockResponses);\"" + "}";
        Map mockConfig2 = Map.of("modify", Map.of("path", "/abc", "method", "get"));
        JsonObject llmResponseJsonModify = JsonParser.parseString(llmResponseModify).getAsJsonObject();
        Map<String, Object> responseMap2 = oas2Parser.addScriptsAndMockDataset(swaggerString, mockConfig2,
                llmResponseJsonModify);
        Assert.assertNotNull(responseMap2);
        Assert.assertTrue(responseMap2.containsKey(APIConstants.MOCK_GEN_POLICY_LIST));
        List<APIResourceMediationPolicy> apiResourceMediationPolicyList2 = (List<APIResourceMediationPolicy>) responseMap2.get(
                APIConstants.MOCK_GEN_POLICY_LIST);
        Assert.assertFalse(apiResourceMediationPolicyList2.isEmpty());
        APIResourceMediationPolicy apiResourceMediationPolicy2 = apiResourceMediationPolicyList2.get(4);
        Assert.assertEquals("/abc", apiResourceMediationPolicy2.getPath());
        String content2 = apiResourceMediationPolicy2.getContent();
        Assert.assertTrue(content2.contains("mockResponse200ToTestIfModified"));
    }

    @Test
    public void testOpenAPIValidatorWithMultiplePathsHavingSameNameWithAndWithoutTrailingSlash() throws Exception {
        String faultySwagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "oas2_paths_with_trailing_slash.json"),
                "UTF-8");

        APIDefinitionValidationResponse response = OASParserUtil.validateAPIDefinition(faultySwagger, true);
        Assert.assertFalse(response.isValid());
        Assert.assertEquals(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode(),
                response.getErrorItems().get(0).getErrorCode());
        Assert.assertEquals("Multiple GET operations with the same resource path /test found in " +
                "the swagger definition", response.getErrorItems().get(0).getErrorDescription());
    }

    @Test
    public void testSwaggerValidatorWithRelaxValidationEnabledAndWithoutInfoTag() throws Exception {
        System.setProperty(APIConstants.SWAGGER_RELAXED_VALIDATION, "true");
        String withoutInfoTagSwagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas2"
                        + File.separator + "oas2_without_info_swagger.json"),
                "UTF-8");
        APIDefinitionValidationResponse response = OASParserUtil.validateAPIDefinition(withoutInfoTagSwagger, true);
        Assert.assertTrue(response.isValid());
        Assert.assertTrue(response.getInfo().getName().startsWith("API-Title-"));
        Assert.assertEquals("attribute info is missing",
                response.getErrorItems().get(0).getErrorDescription());
        System.clearProperty(APIConstants.SWAGGER_RELAXED_VALIDATION);
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
        hostWithSchemes.put(APIConstants.HTTPS_PROTOCOL, "https://localhost");
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

}