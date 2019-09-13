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
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
        System.setProperty(APIDefinition.KEEP_LEGACY_EXTENSION_PROP, Boolean.toString(false));
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

        // check preserve vendor extensions
        System.setProperty(APIDefinition.KEEP_LEGACY_EXTENSION_PROP, Boolean.toString(true));
        definition = testGenerateAPIDefinitionWithExtension(oas3Parser, oas3Resources);
        parseAttemptForV3 = openAPIV3Parser.readContents(definition, null, null);
        openAPI = parseAttemptForV3.getOpenAPI();
        Assert.assertTrue(openAPI.getExtensions().containsKey(APIConstants.SWAGGER_X_WSO2_SECURITY));
        Assert.assertEquals(2, openAPI.getPaths().size());

        itr = openAPI.getPaths().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, PathItem> pathEntry = itr.next();
            PathItem path = pathEntry.getValue();
            for (Operation operation : path.readOperations()) {
                Assert.assertTrue(operation.getExtensions().containsKey(APIConstants.SWAGGER_X_SCOPE));
            }
        }

        // check updated scopes in both extension and security definition
        Operation itemGet = openAPI.getPaths().get("/items").getGet();
        Assert.assertEquals("newScope", itemGet.getExtensions().get(APIConstants.SWAGGER_X_SCOPE));
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

        // check available scopes in extensions
        Map<String, LinkedHashMap<String, ArrayList<LinkedHashMap<String, String>>>> scopesInEx =
                (Map<String, LinkedHashMap<String, ArrayList<LinkedHashMap<String, String>>>>) openAPI.getExtensions()
                        .get(APIConstants.SWAGGER_X_WSO2_SECURITY);
        ArrayList<LinkedHashMap<String, String>> scopeList =
                scopesInEx.get(APIConstants.SWAGGER_OBJECT_NAME_APIM).get(APIConstants.SWAGGER_X_WSO2_SCOPES);
        Assert.assertEquals(3, scopeList.size());
        boolean found = false;
        for (LinkedHashMap<String, String> map : scopeList) {
            if ("newScope".equals(map.get("name"))) {
                Assert.assertEquals("admin", map.get("roles"));
                Assert.assertEquals("newScope", map.get("key"));
                Assert.assertEquals("newScopeDescription", map.get("description"));
                found = true;
            }
        }
        Assert.assertTrue("Newly added scope not found in the updated definition", found);
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

}