package org.wso2.carbon.apimgt.impl.definitions;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIResourceMediationPolicy;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.definitions.APIConstants;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.mockito.Mockito.when;

public class OAS3ParserTest extends OASTestBase {
    private OAS3Parser oas3Parser = new OAS3Parser();

    @Test
    public void testGetURITemplates() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_scopes.json";
        String oas3Scope = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        testGetURITemplates(oas3Parser, oas3Scope);
    }

    @Test
    public void testGetScopes() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_scopes.json";
        String oas3Scope = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        testGetScopes(oas3Parser, oas3Scope);
    }

    @Test
    public void testGenerateAPIDefinition() throws Exception {
        testGenerateAPIDefinition(oas3Parser);
    }

    @Test
    public void testUpdateAPIDefinition() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3Resources.json";
        String oas2Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");

        OASParserEvaluator evaluator = (definition -> {
            OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
            SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(definition, null, null);
            OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
            Assert.assertNotNull(openAPI);
            Assert.assertEquals(1, openAPI.getPaths().size());
            Assert.assertFalse(openAPI.getPaths().containsKey("/noresource/{resid}"));
        });
        testGenerateAPIDefinition2(oas3Parser, oas2Resources, evaluator);
    }

    @Test
    public void testUpdateAPIDefinitionWithExtensions() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3Resources.json";
        String oas3Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();

        // check remove vendor extensions
        String definition = testGenerateAPIDefinitionWithExtension(oas3Parser, oas3Resources);
        SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(definition, null, null);
        OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
        boolean isExtensionNotFound = openAPI.getExtensions() == null || !openAPI.getExtensions()
                .containsKey(APIConstants.SWAGGER_X_WSO2_SECURITY);
        Assert.assertTrue(isExtensionNotFound);
        Assert.assertEquals(2, openAPI.getPaths().size());

        Iterator<Map.Entry<String, PathItem>> itr = openAPI.getPaths().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, PathItem> pathEntry = itr.next();
            PathItem path = pathEntry.getValue();
            for (Operation operation : path.readOperations()) {
                Assert.assertFalse(operation.getExtensions().containsKey(APIConstants.SWAGGER_X_SCOPE));
            }
        }

        // check updated scopes in security definition
        Operation itemGet = openAPI.getPaths().get("/items").getGet();
        Assert.assertTrue(itemGet.getSecurity().get(0).get("default").contains("newScope"));

        // check available scopes in security definition
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("default");
        OAuthFlow implicityOauth = securityScheme.getFlows().getImplicit();
        Assert.assertTrue(implicityOauth.getScopes().containsKey("newScope"));
        Assert.assertEquals("newScopeDescription", implicityOauth.getScopes().get("newScope"));

        Assert.assertTrue(implicityOauth.getExtensions().containsKey(APIConstants.SWAGGER_X_SCOPES_BINDINGS));
        Map<String, String> scopeBinding =
                (Map<String, String>) implicityOauth.getExtensions().get(APIConstants.SWAGGER_X_SCOPES_BINDINGS);
        Assert.assertTrue(scopeBinding.containsKey("newScope"));
        Assert.assertEquals("admin", scopeBinding.get("newScope"));
    }

    @Test
    public void testGetURITemplatesOfOpenAPI300Spec() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_uri_template.json";
        String openAPISpec300 =
                IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();
        uriTemplates.add(getUriTemplate("POST", "Application User", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Application", "/*"));
        uriTemplates.add(getUriTemplate("PUT", "None", "/*"));
        uriTemplates.add(getUriTemplate("DELETE", "Any", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Any", "/abc"));
        Set<URITemplate> uriTemplateSet = oas3Parser.getURITemplates(openAPISpec300);
        Assert.assertEquals(uriTemplateSet, uriTemplates);

    }

    @Test
    public void testOpenApi3WithNonHttpVerbElementInPathItem() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_non_httpverb.json";
        String openApi = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        Set<URITemplate> expectedTemplates = new LinkedHashSet<>();
        expectedTemplates.add(getUriTemplate("GET", "Application", "/item"));
        Set<URITemplate> actualTemplates = oas3Parser.getURITemplates(openApi);
        Assert.assertEquals(actualTemplates, expectedTemplates);
    }

    @Test
    public void testValidateOpenAPIDefinitionWithBlankTitle() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_blank_title.yaml";
        String openApi = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        APIDefinitionValidationResponse response = oas3Parser.validateAPIDefinition(openApi, false);
        Assert.assertTrue(response.isValid());
        Assert.assertTrue(response.getParser().getClass().equals(oas3Parser.getClass()));
    }

    @Test
    public void testGenerateExample() throws Exception {
        String relativePath = "definitions" + File.separator + "oas3" + File.separator + "oas3_mock_response.yaml";
        String openApi = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        Map<String, Object> responseMap = oas3Parser.generateExample(openApi);
        Assert.assertNotNull(responseMap);
        Assert.assertTrue(responseMap.containsKey(APIConstants.SWAGGER) && responseMap.containsKey(APIConstants.MOCK_GEN_POLICY_LIST));

        String swaggerString = (String) responseMap.get(APIConstants.SWAGGER);
        Assert.assertTrue(Objects.nonNull(swaggerString) && !swaggerString.isEmpty());

        Assert.assertTrue(Objects.nonNull(responseMap.get(APIConstants.MOCK_GEN_POLICY_LIST)));
        List<APIResourceMediationPolicy> apiResourceMediationPolicyList = (List<APIResourceMediationPolicy>) responseMap.get(APIConstants.MOCK_GEN_POLICY_LIST);
        Assert.assertFalse(apiResourceMediationPolicyList.isEmpty());

        APIResourceMediationPolicy apiResourceMediationPolicy = apiResourceMediationPolicyList.get(0);
        Assert.assertEquals("/samplePath", apiResourceMediationPolicy.getPath());

        String content = apiResourceMediationPolicy.getContent();
        String expectedGeneratedCode200 = "if (!responses[200]) {\n"
                + " responses [200] = [];\n"
                + "}\n"
                + "responses[200][\"application/json\"] = [ {\n"
                + "  \"id\" : 200,\n"
                + "  \"mockResponse\" : \"mockResponse200\"\n"
                + "} ];";
        String expectedGeneratedCode4XX = "if (!responses[%1$d]) {\n"
                + " responses [%1$d] = [];\n"
                + "}\n"
                + "responses[%1$d][\"application/json\"] = [ {\n"
                + "  \"id\" : \"4XX\",\n"
                + "  \"mockResponse\" : \"mockResponse4XX\"\n"
                + "} ];\n";
        String expectedGeneratedCode404 = "if (!responses[404]) {\n"
                + " responses [404] = [];\n"
                + "}\n"
                + "responses[404][\"application/json\"] = [ {\n"
                + "  \"id\" : 404,\n"
                + "  \"mockResponse\" : \"mockResponse404\"\n"
                + "} ];";
        String expectedGeneratedCode501= "responses[501] = [];\n"
                + "responses[501][\"application/json\"] = {\n"
                + "\"code\" : 501,\n"
                + "\"description\" : \"Not Implemented\"}";

        String expectedGeneratedCodeDefault = "responses[500][\"application/json\"] = \"\";\n"
                + "responses[500][\"application/xml\"] = \"\";";

        Assert.assertTrue(content.contains(expectedGeneratedCode200));
        Assert.assertTrue(content.contains(expectedGeneratedCode404));
        Assert.assertTrue(content.contains(expectedGeneratedCode501));
        Assert.assertTrue(content.contains(expectedGeneratedCodeDefault));

        for (int responseCode = 400 ; responseCode < 500; responseCode++) {
            String expectedGeneratedCode = String.format(expectedGeneratedCode4XX, responseCode);

            if (responseCode == 404) {
                Assert.assertFalse(content.contains(expectedGeneratedCode));
            } else {
                Assert.assertTrue(content.contains(expectedGeneratedCode));
            }
        }
    }
    @Test
    public void testOpenAPIValidatorWithValidationLevel1() throws Exception {
        String faultySwagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3"
                        + File.separator + "openApi3_validation.json"),
                String.valueOf(StandardCharsets.UTF_8));
        APIDefinitionValidationResponse response = OASParserUtil.validateAPIDefinition(faultySwagger, true);

        Assert.assertFalse(response.isValid());
        Assert.assertEquals(1, response.getErrorItems().size());
        Assert.assertEquals(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode(),
                response.getErrorItems().get(0).getErrorCode());
        Assert.assertEquals("attribute extraInfo is unexpected",
                response.getErrorItems().get(0).getErrorDescription());
    }

    @Test
    public void testOpenAPIValidatorWithMultiplePathsHavingSameNameWithAndWithoutTrailingSlash() throws Exception {
        String faultySwagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3"
                        + File.separator + "oas3_paths_with_trailing_slash.json"),
                "UTF-8");

        APIDefinitionValidationResponse response = OASParserUtil.validateAPIDefinition(faultySwagger, true);
        Assert.assertFalse(response.isValid());
        Assert.assertEquals(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode(),
                response.getErrorItems().get(0).getErrorCode());
        Assert.assertEquals("Multiple GET operations with the same resource path /test found in " +
                "the openapi definition", response.getErrorItems().get(0).getErrorDescription());
    }

    @Test
    public void testRootLevelApplicationSecurity() throws Exception {
        String apiSecurity = "oauth_basic_auth_api_key_mandatory,oauth2,api_key";
        String oasDefinition = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3"
                        + File.separator + "oas3_app_security.json"),
                "UTF-8");
        String oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3"
                        + File.separator + "oas3_app_security_key.json"),
                "UTF-8");
        API api = Mockito.mock(API.class);
        when(api.getApiSecurity()).thenReturn(apiSecurity);
        APIDefinition parser = OASParserUtil.getOASParser(oasDefinition);
        String response = parser.getOASDefinitionForPublisher(api, oasDefinition);
        Assert.assertEquals(oasDefinitionEdited, response);
    }
    // Test case for an API with clientCredentials security scheme
    @Test
    public void testProcessOtherSchemeScopesWithClientCredentialsScheme() throws Exception {
        String OPENAPI_SECURITY_SCHEMA_KEY = "default";
        String OPENAPI_DEFAULT_AUTHORIZATION_URL = "https://test.com";

        //Read the API definition file
        String relativePath = "definitions" + File.separator + "oas3" + File.separator
                + "oas3_client_credential_security_scheme.yaml";
        String swaggerContent = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath),
                "UTF-8");
        swaggerContent = oas3Parser.processOtherSchemeScopes(swaggerContent);
        OpenAPI openAPI = oas3Parser.getOpenAPI(swaggerContent);
        //Take the default security schema, where only the token url is not null
        SecurityScheme defaultSecScheme = openAPI.getComponents().getSecuritySchemes()
                .get(OPENAPI_SECURITY_SCHEMA_KEY);
        //Check whether the default security schema is not null
        Assert.assertNotNull(defaultSecScheme);
        //Check whether the default security flows are not null
        Assert.assertNotNull(defaultSecScheme.getFlows());
        //Check whether the token url is available
        Assert.assertNotNull(defaultSecScheme.getFlows().getClientCredentials().getTokenUrl());
        //Check whether the authorization url is null
        Assert.assertNull(defaultSecScheme.getFlows().getClientCredentials().getAuthorizationUrl());

    }

    @Test
    public void testGetOASSecurityDefinitionForPublisher() throws Exception {

        // Testing API with migrated swagger coming from APIM version 2.x without any x-wso2-security or x-scopes.
        String swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3"
                        + File.separator + "publisher" + File.separator + "oas3_mig_without_sec_extensions.json"),
                String.valueOf(StandardCharsets.UTF_8));
        API api = Mockito.mock(API.class);
        String apiSecurity = "oauth_basic_auth_api_key_mandatory,oauth2";
        when(api.getApiSecurity()).thenReturn(apiSecurity);
        APIDefinition parser = OASParserUtil.getOASParser(swagger);
        String response = parser.getOASDefinitionForPublisher(api, swagger);
        String oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "publisher" + File.separator + "oas3_mig_without_sec_extensions_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionEdited, response);

        // Testing API with migrated swagger coming from APIM version 2.x with x-wso2-security and x-scopes.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3"
                        + File.separator + "publisher" + File.separator + "oas3_mig_with_sec_extensions.json"),
                String.valueOf(StandardCharsets.UTF_8));
        response = parser.getOASDefinitionForPublisher(api, swagger);
        oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "publisher" + File.separator + "oas3_mig_with_sec_extensions_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionEdited, response);

        // Testing API with swagger generated after APIM 2.x versions with oauth security definitions and x-wso2
        // extensions. API configured with all security.
        apiSecurity = "oauth_basic_auth_api_key_mandatory,api_key,basic_auth,oauth2";
        when(api.getApiSecurity()).thenReturn(apiSecurity);
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "publisher" + File.separator + "oas3_with_default_oauth.json"),
                String.valueOf(StandardCharsets.UTF_8));
        response = parser.getOASDefinitionForPublisher(api, swagger);
        oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "publisher" + File.separator + "oas3_with_default_oauth_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionEdited, response);

        // Testing if the different default implicit authorizationUrl is replaced with the default value.
        // This is a test for the fix 9620. Earlier value was replaced with the default 'https;//test.com value.
        // Now it should not be the case.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "publisher" + File.separator + "oas3_with_default_implicit_authorization_url.json"),
                "UTF-8");
        response = parser.getOASDefinitionForPublisher(api, swagger);
        oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "publisher" + File.separator + "oas3_with_default_implicit_authorization_url_response.json"),
                "UTF-8");
        Assert.assertEquals(oasDefinitionEdited, response);
    }


    @Test
    public void testGetOASSecurityDefinitionForStore() throws  Exception {

        // Testing API with migrated swagger coming from APIM version 2.x without any x-wso2-security or x-scopes.
        String swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "publisher" + File.separator + "oas3_mig_without_sec_extensions.json"),
                String.valueOf(StandardCharsets.UTF_8));
        APIIdentifier apiIdentifier = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0");
        Map<String, String> hostWithSchemes = new HashMap<>();
        hostWithSchemes.put(APIConstants.HTTPS_PROTOCOL, "https://localhost");
        API api = new API(apiIdentifier);
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,oauth2"); // oauth2 security only
        api.setTransports("https");
        api.setContext("/");
        api.setScopes(new HashSet<>());
        String response = oas3Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        String oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "devportal" + File.separator + "oas3_mig_without_sec_extensions_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);

        // Testing API with migrated swagger coming from APIM version 2.x with x-wso2-security and x-scopes.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "publisher" + File.separator + "oas3_mig_with_sec_extensions.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setScopes(getAPITestScopes());
        response = oas3Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "devportal" + File.separator + "oas3_mig_with_sec_extensions_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);

        // Testing API with swagger generated after APIM 2.x versions with oauth security definitions and x-wso2
        // extensions. API configured with all security.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "devportal" + File.separator + "oas3_with_default_allsecurity.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setScopes(getAPITestScopes());
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,api_key,basic_auth,oauth2");
        response = oas3Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "devportal" + File.separator + "oas3_with_default_allsecurity_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // Testing API with swagger generated after APIM 2.x version, but with basic_auth and api_key security in
        // the scheme which went with as an u2 update for 4.1, then later reverted. API configured with all security.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "publisher" + File.separator + "oas3_with_apikey_basic_oauth_security_u2.json"),
                String.valueOf(StandardCharsets.UTF_8));
        response = oas3Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "devportal" + File.separator + "oas3_with_apikey_basic_oauth_security_u2_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // Testing API with swagger generated after APIM 2.x versions with oauth security definitions and x-wso2
        // extensions. API configured with basic auth and api key.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3"
                        + File.separator + "devportal" + File.separator + "oas3_with_basic_apisec.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,api_key,basic_auth");
        response = oas3Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "devportal" + File.separator + "oas3_with_basic_apisec_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // API configured with basic auth only.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "devportal" + File.separator + "oas3_with_basic.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,basic_auth");
        response = oas3Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3" + File.separator
                        + "devportal" + File.separator + "oas3_with_basic_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // API Configured with api key only.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3"
                        + File.separator + "devportal" + File.separator + "oas3_with_apikey.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,api_key");
        response = oas3Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes, null);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas3"
                        + File.separator + "devportal" + File.separator + "oas3_with_apikey_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
    }

    private Set<Scope> getAPITestScopes() {
        Scope petLocalScope = new Scope();
        petLocalScope.setKey("OrderScope");
        petLocalScope.setName("OrderScope");
        petLocalScope.setRoles("admin");
        petLocalScope.setDescription("");
        Scope globalScope = new Scope();
        globalScope.setName("MenuScope");
        globalScope.setKey("MenuScope");
        globalScope.setDescription("description");
        globalScope.setRoles("");
        Set<Scope> apiScopes = new LinkedHashSet<>();
        apiScopes.add(globalScope);
        apiScopes.add(petLocalScope);
        return apiScopes;
    }
}
