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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.impl;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class APIDefinitionFromSwagger20TestCase {

    @Test()
    public void testApiResourceParseFromSwagger() throws IOException, APIManagementException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = SampleTestObjectCreator.getSampleApiSwagger();
        API.APIBuilder apiBuilder = apiDefinitionFromSwagger20
                .generateApiFromSwaggerResource("testProvider", sampleApi);
        API api = apiBuilder.build();
        Assert.assertNotNull(api);
    }

    @Test()
    public void testApiScopeParseSwagger() throws IOException, APIManagementException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = SampleTestObjectCreator.getSampleApiSwagger();
        Map<String, String> scopeMap = apiDefinitionFromSwagger20.getScopesFromSecurityDefinition(sampleApi);
        for (Map.Entry<String, String> scopeEntry : scopeMap.entrySet()) {
            if ("apim:api_view".equals(scopeEntry.getKey())) {
                Assert.assertEquals("View API", scopeEntry.getValue());
            }
            if ("apim:api_create".equals(scopeEntry.getKey())) {
                Assert.assertEquals("Create API", scopeEntry.getValue());
            }
            if ("apim:api_delete".equals(scopeEntry.getKey())) {
                Assert.assertEquals("Delete API", scopeEntry.getValue());
            }
        }

        if (scopeMap.isEmpty()) {
            Assert.fail("Scopes didn't ");
        }
    }

    @Test()
    public void testRemoveScopeFromSwagger() throws IOException, APIManagementException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = SampleTestObjectCreator.getSampleApiSwagger();
        String scopeRemovedSwagger = apiDefinitionFromSwagger20.removeScopeFromSwaggerDefinition(sampleApi,
                "apim:api_view");
        Map<String, String> scopeMap = apiDefinitionFromSwagger20.getScopesFromSecurityDefinition(scopeRemovedSwagger);
        Assert.assertFalse(scopeMap.containsKey("apim:api_view"));
    }

    @Test()
    public void testAddNewScopeToExistingOauthSecurity() throws IOException, APIManagementException {

        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String filePath = "src" + File.separator + "test" + File.separator + "resources" + File
                .separator + "swagger" + File.separator + "swaggerWithAuthorization.yaml";
        File file = Paths.get(filePath).toFile();
        String sampleApi = IOUtils.toString(new FileInputStream(file));
        Scope scope = new Scope();
        scope.setName("apim:api_delete");
        scope.setDescription("Delete API");
        String scopeAddedSwagger = apiDefinitionFromSwagger20.addScopeToSwaggerDefinition(sampleApi, scope);
        Map<String, String> scopes = apiDefinitionFromSwagger20.getScopesFromSecurityDefinition(scopeAddedSwagger);
        Assert.assertTrue(scopes.containsKey("apim:api_delete"));
    }

    @Test()
    public void testAddNewScopeToNonExistingSecurityDefinition() throws IOException, APIManagementException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithOutAuthorization.yaml"));
        Scope scope = new Scope();
        scope.setName("apim:api_delete");
        scope.setDescription("Delete API");
        String scopeAddedSwagger = apiDefinitionFromSwagger20.addScopeToSwaggerDefinition(sampleApi, scope);
        Map<String, String> scopes = apiDefinitionFromSwagger20.getScopesFromSecurityDefinition(scopeAddedSwagger);
        Assert.assertTrue(scopes.containsKey("apim:api_delete"));
    }

    @Test()
    public void testAddNewScopeToSecurityDefinitionExistingSwaggerNonExisting() throws IOException,
            APIManagementException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithAuthorizationApiKey.yaml"));
        Scope scope = new Scope();
        scope.setName("apim:api_delete");
        scope.setDescription("Delete API");
        String scopeAddedSwagger = apiDefinitionFromSwagger20.addScopeToSwaggerDefinition(sampleApi, scope);
        Map<String, String> scopes = apiDefinitionFromSwagger20.getScopesFromSecurityDefinition(scopeAddedSwagger);
        Assert.assertTrue(scopes.containsKey("apim:api_delete"));
    }

    @Test()
    public void testUpdateScope() throws IOException, APIManagementException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithAuthorization.yaml"));
        Scope scope = new Scope();
        scope.setName("apim:api_create");
        scope.setDescription("Delete API");
        String scopeAddedSwagger = apiDefinitionFromSwagger20.updateScopesOnSwaggerDefinition(sampleApi, scope);
        Map<String, String> scopes = apiDefinitionFromSwagger20.getScopesFromSecurityDefinition(scopeAddedSwagger);
        Assert.assertTrue(scopes.containsKey("apim:api_create"));
        //commented due to parallel test run
        //Assert.assertEquals(scopes.get("apim:api_delete").getDescription(),"Delete API");
    }

    @Test
    public void testGetGlobalAssignedScopes() throws IOException, APIManagementException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithAuthorization.yaml"));
        List<String> scopes = apiDefinitionFromSwagger20.getGlobalAssignedScopes(sampleApi);
        Assert.assertEquals(scopes.size(), 1);
    }

    @Test
    public void testGetGlobalAssignedScopesFromApiKeyAuthorization() throws IOException, APIManagementException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithAuthorizationApiKey.yaml"));
        List<String> scopes = apiDefinitionFromSwagger20.getGlobalAssignedScopes(sampleApi);
        Assert.assertEquals(scopes.size(), 0);
    }

    @Test
    public void testGetGlobalAssignedScopesFromNonExisting() throws IOException, APIManagementException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithOutAuthorization.yaml"));
        List<String> scopes = apiDefinitionFromSwagger20.getGlobalAssignedScopes(sampleApi);
        Assert.assertEquals(scopes.size(), 0);
    }

    @Test
    public void testGenerateMergedResourceDefinition() throws IOException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithAuthorization.yaml"));
        UriTemplate uriTemplate1 = new UriTemplate.UriTemplateBuilder().uriTemplate("/apis").httpVerb("post").scopes
                (Arrays.asList("apim:api_create")).build();
        UriTemplate uriTemplate2 = new UriTemplate.UriTemplateBuilder().uriTemplate("/endpoints").httpVerb("post")
                .scopes(Arrays.asList("apim:api_create")).build();
        Map<String, UriTemplate> hasTemplateMap = new HashMap<>();
        hasTemplateMap.put(APIUtils.generateOperationIdFromPath(uriTemplate1.getUriTemplate(),
                uriTemplate1.getHttpVerb()), uriTemplate1);
        hasTemplateMap.put(APIUtils.generateOperationIdFromPath(uriTemplate2.getUriTemplate(),
                uriTemplate2.getHttpVerb()), uriTemplate2);
        API api = new API.APIBuilder("admin", "admin", "1.0.0").uriTemplates(hasTemplateMap).id(UUID.randomUUID()
                .toString()).build();
        apiDefinitionFromSwagger20.generateMergedResourceDefinition(sampleApi, api);
    }

    @Test
    public void testGenerateMergedResourceDefinitionWhileAddingRootLevelSecurity() throws IOException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithoutRootLevelSecurity.yaml"));
        UriTemplate uriTemplate1 = new UriTemplate.UriTemplateBuilder().uriTemplate("/apis").httpVerb("post").scopes
                (Arrays.asList("apim:api_create")).build();
        UriTemplate uriTemplate2 = new UriTemplate.UriTemplateBuilder().uriTemplate("/endpoints").httpVerb("post")
                .scopes(Arrays.asList("apim:api_create")).build();
        Map<String, UriTemplate> hasTemplateMap = new HashMap<>();
        hasTemplateMap.put(APIUtils.generateOperationIdFromPath(uriTemplate1.getUriTemplate(),
                uriTemplate1.getHttpVerb()), uriTemplate1);
        hasTemplateMap.put(APIUtils.generateOperationIdFromPath(uriTemplate2.getUriTemplate(),
                uriTemplate2.getHttpVerb()), uriTemplate2);
        API api = new API.APIBuilder("admin", "admin", "1.0.0").uriTemplates(hasTemplateMap).id(UUID.randomUUID()
                .toString()).scopes(Arrays.asList("apim:api_create")).build();
        apiDefinitionFromSwagger20.generateMergedResourceDefinition(sampleApi, api);
    }

    @Test
    public void testGenerateMergedResourceDefinitionWhileAddingRootLevelHavingDifferentLevelSecurity() throws
            IOException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithAuthorizationApiKeyInRoot.yaml"));
        UriTemplate uriTemplate1 = new UriTemplate.UriTemplateBuilder().uriTemplate("/apis").httpVerb("post").scopes
                (Arrays.asList("apim:api_create")).build();
        UriTemplate uriTemplate2 = new UriTemplate.UriTemplateBuilder().uriTemplate("/endpoints").httpVerb("post")
                .scopes(Arrays.asList("apim:api_create")).build();
        Map<String, UriTemplate> hasTemplateMap = new HashMap<>();
        hasTemplateMap.put(APIUtils.generateOperationIdFromPath(uriTemplate1.getUriTemplate(),
                uriTemplate1.getHttpVerb()), uriTemplate1);
        hasTemplateMap.put(APIUtils.generateOperationIdFromPath(uriTemplate2.getUriTemplate(),
                uriTemplate2.getHttpVerb()), uriTemplate2);
        API api = new API.APIBuilder("admin", "admin", "1.0.0").uriTemplates(hasTemplateMap).id(UUID.randomUUID()
                .toString()).scopes(Arrays.asList("apim:api_create")).build();
        apiDefinitionFromSwagger20.generateMergedResourceDefinition(sampleApi, api);
    }
    @Test
    public void testGenerateMergedResourceDefinitionUpdatingExistingresource() throws IOException {
        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = IOUtils.toString(this.getClass().getResourceAsStream
                (File.separator + "swagger" + File.separator + "swaggerWithAuthorization.yaml"));
        UriTemplate uriTemplate1 = new UriTemplate.UriTemplateBuilder().uriTemplate("/apis").httpVerb("get").scopes
                (Arrays.asList("apim:api_create")).build();
        UriTemplate uriTemplate2 = new UriTemplate.UriTemplateBuilder().uriTemplate("/endpoints").httpVerb("post")
                .scopes(Arrays.asList("apim:api_create")).build();
        Map<String, UriTemplate> hasTemplateMap = new HashMap<>();
        hasTemplateMap.put(APIUtils.generateOperationIdFromPath(uriTemplate1.getUriTemplate(),
                uriTemplate1.getHttpVerb()), uriTemplate1);
        hasTemplateMap.put(APIUtils.generateOperationIdFromPath(uriTemplate2.getUriTemplate(),
                uriTemplate2.getHttpVerb()), uriTemplate2);
        API api = new API.APIBuilder("admin", "admin", "1.0.0").uriTemplates(hasTemplateMap).id(UUID.randomUUID()
                .toString()).build();
        apiDefinitionFromSwagger20.generateMergedResourceDefinition(sampleApi, api);
    }
}

