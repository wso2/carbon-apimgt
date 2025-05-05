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
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.SwaggerData;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

}