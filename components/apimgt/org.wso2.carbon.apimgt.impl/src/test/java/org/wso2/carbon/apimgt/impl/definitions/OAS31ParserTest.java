package org.wso2.carbon.apimgt.impl.definitions;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.mockito.Mockito.when;

public class OAS31ParserTest extends OASTestBase {
    private OAS3Parser oas31Parser = new OAS3Parser(APIConstants.OAS_V31);

    @Test
    public void testGetURITemplates() throws Exception {
        String relativePath = "definitions" + File.separator + "oas31" + File.separator + "oas31_scopes.json";
        String oas31Definition = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        JSONObject jsonObject = new JSONObject(oas31Definition);

        URITemplate exUriTemplate = new URITemplate();
        exUriTemplate.setUriTemplate("/pets");
        exUriTemplate.setAuthType("Application & Application User");
        exUriTemplate.setAuthTypes("Application & Application User");
        exUriTemplate.setHTTPVerb("GET");
        exUriTemplate.setHttpVerbs("GET");
        exUriTemplate.setThrottlingTier("Unlimited");
        exUriTemplate.setThrottlingTiers("Unlimited");
        exUriTemplate.setScope(getSampleScope());
        exUriTemplate.setScopes(getSampleScope());

        Set<URITemplate> uriTemplates = oas31Parser.getURITemplates(oas31Definition);
        Assert.assertEquals(1, uriTemplates.size());
        Assert.assertTrue(uriTemplates.contains(exUriTemplate));
    }

    @Test
    public void testGetScopes() throws Exception {
        String relativePath = "definitions" + File.separator + "oas31" + File.separator + "oas31_scopes.json";
        String oas3Scope = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        Set<Scope> scopes = oas31Parser.getScopes(oas3Scope);
        Assert.assertEquals(2, scopes.size());
        scopes.toArray()[0].equals(getSampleScope());
        Assert.assertTrue(scopes.contains(getSampleScope()));
        Assert.assertTrue(scopes.contains(getExtensionScope()));
    }

    @Test
    public void testGenerateAPIDefinition() throws Exception {
        testGenerateAPIDefinition(oas31Parser);
    }

    @Test
    public void testUpdateAPIDefinition() throws Exception {
        String relativePath = "definitions" + File.separator + "oas31" + File.separator + "oas31Resources.json";
        String oas2Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");

        OASParserEvaluator evaluator = (definition -> {
            OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
            SwaggerParseResult parseAttemptForV3 = openAPIV3Parser.readContents(definition, null, null);
            OpenAPI openAPI = parseAttemptForV3.getOpenAPI();
            Assert.assertNotNull(openAPI);
            Assert.assertEquals("3.1.0", openAPI.getOpenapi());
            Assert.assertEquals(1, openAPI.getPaths().size());
            Assert.assertFalse(openAPI.getPaths().containsKey("/noresource/{resid}"));
        });
        testGenerateAPIDefinition2(oas31Parser, oas2Resources, evaluator);
    }

    @Test
    public void testUpdateAPIDefinitionWithExtensions() throws Exception {
        String relativePath = "definitions" + File.separator + "oas31" + File.separator + "oas31Resources.json";
        String oas3Resources = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();

        // check remove vendor extensions
        String definition = testGenerateAPIDefinitionWithExtension(oas31Parser, oas3Resources);
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
        String relativePath = "definitions" + File.separator + "oas31" + File.separator + "oas31_uri_template.json";
        String openAPISpec300 =
                IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();
        uriTemplates.add(getUriTemplate("POST", "Application User", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Application", "/*"));
        uriTemplates.add(getUriTemplate("PUT", "None", "/*"));
        uriTemplates.add(getUriTemplate("DELETE", "Any", "/*"));
        uriTemplates.add(getUriTemplate("GET", "Any", "/abc"));
        Set<URITemplate> uriTemplateSet = oas31Parser.getURITemplates(openAPISpec300);
        Assert.assertEquals(uriTemplateSet, uriTemplates);
    }

    @Test
    public void testValidateOpenAPIDefinitionWithoutLicenceIdentifier() throws Exception {
        String relativePath = "definitions" + File.separator + "oas31" + File.separator + "oas31_with_no_licese_identifier.yaml";
        String openApi = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath), "UTF-8");
        APIDefinitionValidationResponse response = oas31Parser.validateAPIDefinition(openApi, false);
        Assert.assertFalse(response.isValid());
        Assert.assertTrue(response.getErrorItems().size() > 0);
    }

    @Test
    public void testOpenAPIValidatorWithValidationLevel1() throws Exception {
        String faultySwagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31"
                        + File.separator + "openApi31_validation.json"),
                String.valueOf(StandardCharsets.UTF_8));
        APIDefinitionValidationResponse response = OASParserUtil.validateAPIDefinition(faultySwagger, true);

        Assert.assertFalse(response.isValid());
        Assert.assertEquals(1, response.getErrorItems().size());
        Assert.assertEquals(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode(),
                response.getErrorItems().get(0).getErrorCode());
        Assert.assertEquals("attribute extraInfo is unexpected",
                response.getErrorItems().get(0).getErrorDescription());
    }

    // Test case for an API with clientCredentials security scheme
    @Test
    public void testProcessOtherSchemeScopesWithClientCredentialsScheme() throws Exception {
        String OPENAPI_SECURITY_SCHEMA_KEY = "default";

        //Read the API definition file
        String relativePath = "definitions" + File.separator + "oas31" + File.separator
                + "oas31_client_credential_security_scheme.yaml";
        String swaggerContent = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath),
                "UTF-8");
        swaggerContent = oas31Parser.processOtherSchemeScopes(swaggerContent);
        OpenAPI openAPI = oas31Parser.getOpenAPI(swaggerContent);
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

        // Testing API with swagger generated after APIM 2.x versions with oauth security definitions and x-wso2
        // extensions. API configured with all security.
        API api = Mockito.mock(API.class);
        String apiSecurity = "oauth_basic_auth_api_key_mandatory,api_key,basic_auth,oauth2";
        when(api.getApiSecurity()).thenReturn(apiSecurity);
        String swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31" + File.separator
                        + "publisher" + File.separator + "oas31_with_default_oauth.json"),
                String.valueOf(StandardCharsets.UTF_8));
        APIDefinition parser = OASParserUtil.getOASParser(swagger);
        String response = parser.getOASDefinitionForPublisher(api, swagger);
        String oasDefinitionEdited = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31" + File.separator
                        + "publisher" + File.separator + "oas31_with_default_oauth_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionEdited, response);
    }


    @Test
    public void testGetOASSecurityDefinitionForStore() throws  Exception {

        // Testing API with swagger generated after APIM 2.x versions with oauth security definitions and x-wso2
        // extensions. API configured with all security.
        String swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31" + File.separator
                        + "devportal" + File.separator + "oas31_with_default_allsecurity.json"),
                String.valueOf(StandardCharsets.UTF_8));
        APIIdentifier apiIdentifier = new APIIdentifier("admin", "PizzaShackAPI", "1.0.0");
        Map<String, String> hostWithSchemes = new HashMap<>();
        hostWithSchemes.put(APIConstants.HTTPS_PROTOCOL, "https://localhost");
        API api = new API(apiIdentifier);
        api.setTransports("https");
        api.setContext("/");
        api.setScopes(new HashSet<>());
        api.setScopes(getAPITestScopes());
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,api_key,basic_auth,oauth2");
        String response = oas31Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes);
        String oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31" + File.separator
                        + "devportal" + File.separator + "oas31_with_default_allsecurity_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // Testing API with swagger generated after APIM 2.x version, but with basic_auth and api_key security in
        // the scheme which went with as an u2 update for 4.1, then later reverted. API configured with all security.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31" + File.separator
                        + "publisher" + File.separator + "oas31_with_apikey_basic_oauth_security_u2.json"),
                String.valueOf(StandardCharsets.UTF_8));
        response = oas31Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31" + File.separator
                        + "devportal" + File.separator + "oas31_with_apikey_basic_oauth_security_u2_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // Testing API with swagger generated after APIM 2.x versions with oauth security definitions and x-wso2
        // extensions. API configured with basic auth and api key.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31"
                        + File.separator + "devportal" + File.separator + "oas31_with_basic_apisec.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,api_key,basic_auth");
        response = oas31Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31" + File.separator
                        + "devportal" + File.separator + "oas31_with_basic_apisec_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // API configured with basic auth only.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31" + File.separator
                        + "devportal" + File.separator + "oas31_with_basic.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,basic_auth");
        response = oas31Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31" + File.separator
                        + "devportal" + File.separator + "oas31_with_basic_response.json"),
                String.valueOf(StandardCharsets.UTF_8));
        Assert.assertEquals(oasDefinitionExpected, response);
        // API Configured with api key only.
        swagger = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31"
                        + File.separator + "devportal" + File.separator + "oas31_with_apikey.json"),
                String.valueOf(StandardCharsets.UTF_8));
        api.setApiSecurity("oauth_basic_auth_api_key_mandatory,api_key");
        response = oas31Parser.getOASDefinitionForStore(api, swagger, hostWithSchemes);
        oasDefinitionExpected = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("definitions" + File.separator + "oas31"
                        + File.separator + "devportal" + File.separator + "oas31_with_apikey_response.json"),
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
