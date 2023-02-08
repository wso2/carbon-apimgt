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

import org.json.JSONObject;
import org.junit.Assert;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.ThrottlingLimit;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class OASTestBase {

    private Scope sampleScope;
    private Scope extensionScope;
    private URITemplate petGet;
    private URITemplate petPost;
    private URITemplate itemPost;
    private URITemplate itemGet;
    private URITemplate bookGet;
    private ThrottlingLimit throttlingLimit = new ThrottlingLimit();

    public OASTestBase() {
        sampleScope = new Scope();
        sampleScope.setName("sample");
        sampleScope.setKey("sample");
        sampleScope.setRoles("admin");
        sampleScope.setDescription("sample description");

        extensionScope = new Scope();
        extensionScope.setName("extensionScope");
        extensionScope.setKey("extensionScope");
        extensionScope.setRoles("admin");
        extensionScope.setDescription("extensionScope description");

        petGet = new URITemplate();
        petGet.setUriTemplate("/pets");
        petGet.setAuthType("Application & Application User");
        petGet.setAuthTypes("Application & Application User");
        petGet.setHTTPVerb("GET");
        petGet.setHttpVerbs("GET");
        petGet.setThrottlingTier("Unlimited");
        petGet.setThrottlingTiers("Unlimited");
        petGet.setThrottlingLimit("Unlimited");
        petGet.setScope(sampleScope);
        petGet.setScopes(sampleScope);

        petPost = new URITemplate();
        petPost.setUriTemplate("/pets");
        petPost.setAuthType("Application & Application User");
        petPost.setAuthTypes("Application & Application User");
        petPost.setHTTPVerb("POST");
        petPost.setHttpVerbs("POST");
        petPost.setThrottlingTier("Unlimited");
        petPost.setThrottlingTiers("Unlimited");
        petPost.setThrottlingLimit("Unlimited");
        petPost.setScope(sampleScope);
        petPost.setScopes(sampleScope);

        itemPost = new URITemplate();
        itemPost.setUriTemplate("/items");
        itemPost.setAuthType("Application & Application User");
        itemPost.setAuthTypes("Application & Application User");
        itemPost.setHTTPVerb("POST");
        itemPost.setHttpVerbs("POST");
        itemPost.setThrottlingTier("Unlimited");
        itemPost.setThrottlingTiers("Unlimited");
        itemPost.setThrottlingLimit("Unlimited");
        itemPost.setScope(sampleScope);
        itemPost.setScopes(sampleScope);

        itemGet = new URITemplate();
        itemGet.setUriTemplate("/items");
        itemGet.setAuthType("Application & Application User");
        itemGet.setAuthTypes("Application & Application User");
        itemGet.setHTTPVerb("GET");
        itemGet.setHttpVerbs("GET");
        itemGet.setThrottlingTier("Unlimited");
        itemGet.setThrottlingTiers("Unlimited");
        itemGet.setThrottlingLimit("Unlimited");
        itemGet.setScope(sampleScope);
        itemGet.setScopes(sampleScope);

        bookGet = new URITemplate();
        bookGet.setUriTemplate("/books");
        bookGet.setAuthType("Application & Application User");
        bookGet.setAuthTypes("Application & Application User");
        bookGet.setHTTPVerb("GET");
        bookGet.setHttpVerbs("GET");
        bookGet.setThrottlingTier("12310PerMin");
        bookGet.setThrottlingTiers("12310PerMin");
        throttlingLimit.setRequestCount(12310);
        throttlingLimit.setUnit("MINUTE");
        bookGet.setThrottlingLimit(throttlingLimit);
        bookGet.setScope(sampleScope);
        bookGet.setScopes(sampleScope);
    }

    public void testGetURITemplates(APIDefinition parser, String content) throws Exception {
        JSONObject jsonObject = new JSONObject(content);

        URITemplate exUriTemplate = new URITemplate();
        exUriTemplate.setUriTemplate("/pets");
        exUriTemplate.setAuthType("Application & Application User");
        exUriTemplate.setAuthTypes("Application & Application User");
        exUriTemplate.setHTTPVerb("GET");
        exUriTemplate.setHttpVerbs("GET");
        exUriTemplate.setThrottlingTier("Unlimited");
        exUriTemplate.setThrottlingTiers("Unlimited");
        exUriTemplate.setThrottlingLimit("Unlimited");
        exUriTemplate.setScope(extensionScope);
        exUriTemplate.setScopes(extensionScope);

        String scopesOnlyInSecurity = jsonObject.getJSONObject("scopesOnlyInSecurity").toString();
        Set<URITemplate> uriTemplates = parser.getURITemplates(scopesOnlyInSecurity);
        Assert.assertEquals(1, uriTemplates.size());
        Assert.assertTrue(uriTemplates.contains(petGet));

        String scopesOnlyInExtension = jsonObject.getJSONObject("scopesOnlyInExtension").toString();
        uriTemplates = parser.getURITemplates(scopesOnlyInExtension);
        Assert.assertEquals(1, uriTemplates.size());
        Assert.assertTrue(uriTemplates.contains(exUriTemplate));

        String scopesInExtensionAndSec = jsonObject.getJSONObject("scopesInExtensionAndSec").toString();
        uriTemplates = parser.getURITemplates(scopesInExtensionAndSec);
        Assert.assertEquals(1, uriTemplates.size());
        Assert.assertTrue(uriTemplates.contains(petGet));

        String throttlingLimitExtensionUriTemplate = jsonObject.getJSONObject("throttlingLimitInExtension").toString();
        uriTemplates = parser.getURITemplates(throttlingLimitExtensionUriTemplate);
        Assert.assertEquals(1, uriTemplates.size());
        Assert.assertTrue(uriTemplates.contains(bookGet));
    }

    public void testGetScopes(APIDefinition parser, String content) throws Exception {
        JSONObject jsonObject = new JSONObject(content);

        String scopesOnlyInSecurity = jsonObject.getJSONObject("scopesOnlyInSecurity").toString();
        Set<Scope> scopes = parser.getScopes(scopesOnlyInSecurity);
        Assert.assertEquals(2, scopes.size());
        scopes.toArray()[0].equals(sampleScope);
        Assert.assertTrue(scopes.contains(sampleScope));
        Assert.assertTrue(scopes.contains(extensionScope));

        String scopesOnlyInExtension = jsonObject.getJSONObject("scopesOnlyInExtension").toString();
        scopes = parser.getScopes(scopesOnlyInExtension);
        Assert.assertEquals(2, scopes.size());
        Assert.assertTrue(scopes.contains(sampleScope));
        Assert.assertTrue(scopes.contains(extensionScope));

        String scopesInExtensionAndSec = jsonObject.getJSONObject("scopesInExtensionAndSec").toString();
        scopes = parser.getScopes(scopesInExtensionAndSec);
        Assert.assertEquals(2, scopes.size());
        Assert.assertTrue(scopes.contains(sampleScope));
        Assert.assertTrue(scopes.contains(extensionScope));
    }

    public void testGenerateAPIDefinition(APIDefinition parser) throws Exception {
        APIIdentifier identifier = new APIIdentifier("admin", "simple", "1.0.0");
        API api = new API(identifier);
        api.setScopes(new HashSet<>(Arrays.asList(sampleScope, extensionScope)));
        api.setUriTemplates(new HashSet<>(Arrays.asList(petGet)));

        String definition = parser.generateAPIDefinition(new SwaggerData(api));
        APIDefinitionValidationResponse response = parser.validateAPIDefinition(definition, false);
        Assert.assertTrue(response.isValid());
        Assert.assertTrue(response.getParser().getClass().equals(parser.getClass()));

        Set<URITemplate> uriTemplates = parser.getURITemplates(definition);
        Assert.assertEquals(1, uriTemplates.size());
        Assert.assertTrue(uriTemplates.contains(petGet));

        Set<Scope> scopes = parser.getScopes(definition);
        Assert.assertEquals(2, scopes.size());
        Assert.assertTrue(scopes.contains(sampleScope));
        Assert.assertTrue(scopes.contains(extensionScope));
    }

    public void testGenerateAPIDefinition2(APIDefinition parser, String content, OASParserEvaluator evaluator) throws Exception {
        JSONObject jsonObject = new JSONObject(content);
        String equalNoOfResources = jsonObject.getJSONObject("equalNoOfResources").toString();

        APIIdentifier identifier = new APIIdentifier("admin", "simple", "1.0.0");
        API api = new API(identifier);
        api.setScopes(new HashSet<>(Arrays.asList(sampleScope, extensionScope)));
        api.setUriTemplates(new HashSet<>(Arrays.asList(petGet, petPost, itemGet, itemPost)));

        String definition = parser.generateAPIDefinition(new SwaggerData(api), equalNoOfResources);
        APIDefinitionValidationResponse response = parser.validateAPIDefinition(definition, false);
        Assert.assertTrue(response.isValid());
        Assert.assertTrue(response.getParser().getClass().equals(parser.getClass()));

        Set<URITemplate> uriTemplates = parser.getURITemplates(definition);
        Assert.assertEquals(4, uriTemplates.size());
        Assert.assertTrue(uriTemplates.contains(petGet));
        Assert.assertTrue(uriTemplates.contains(petPost));
        Assert.assertTrue(uriTemplates.contains(itemGet));
        Assert.assertTrue(uriTemplates.contains(itemPost));

        Set<Scope> scopes = parser.getScopes(definition);
        Assert.assertEquals(2, scopes.size());
        Assert.assertTrue(scopes.contains(sampleScope));
        Assert.assertTrue(scopes.contains(extensionScope));

        // Remove operation and path from API object
        String extraResourcesInDefinition = jsonObject.getJSONObject("extraResourcesInDefinition").toString();
        api.setUriTemplates(new HashSet<>(Arrays.asList(itemGet, itemPost)));
        definition = parser.generateAPIDefinition(new SwaggerData(api), extraResourcesInDefinition);
        response = parser.validateAPIDefinition(definition, false);
        Assert.assertTrue(response.isValid());
        Assert.assertTrue(response.getParser().getClass().equals(parser.getClass()));
        uriTemplates = parser.getURITemplates(definition);
        Assert.assertEquals(2, uriTemplates.size());

        //assert generated paths
        if(evaluator != null) {
            evaluator.eval(definition);
        }

        Iterator iterator = uriTemplates.iterator();
        while (iterator.hasNext()) {
            URITemplate element = (URITemplate) iterator.next();
            if ("/pets".equalsIgnoreCase(element.getUriTemplate())) {
                Assert.fail("Removed paths from API operation should not present.");
            }
            if ("/items".equalsIgnoreCase(element.getUriTemplate()) && "PUT".equalsIgnoreCase(element.getHTTPVerb())) {
                Assert.fail("Removed item from API operation should not present.");
            }
        }
        Assert.assertTrue(uriTemplates.contains(itemGet));
        Assert.assertTrue(uriTemplates.contains(itemPost));

        // Add operation and path to API object
        String lessResourcesInDefinition = jsonObject.getJSONObject("lessResourcesInDefinition").toString();
        api.setUriTemplates(new HashSet<>(Arrays.asList(petGet, petPost, itemGet, itemPost, bookGet)));
        definition = parser.generateAPIDefinition(new SwaggerData(api), lessResourcesInDefinition);
        response = parser.validateAPIDefinition(definition, false);
        Assert.assertTrue(response.isValid());
        Assert.assertTrue(response.getParser().getClass().equals(parser.getClass()));
        uriTemplates = parser.getURITemplates(definition);
        Assert.assertEquals(5, uriTemplates.size());
        Assert.assertTrue(uriTemplates.contains(petGet));
        Assert.assertTrue(uriTemplates.contains(petPost));
        Assert.assertTrue(uriTemplates.contains(itemGet));
        Assert.assertTrue(uriTemplates.contains(itemPost));
        Assert.assertTrue(uriTemplates.contains(bookGet));
    }

    public String testGenerateAPIDefinitionWithExtension(APIDefinition parser, String content) throws Exception {
        JSONObject jsonObject = new JSONObject(content);
        String equalNoOfResourcesWithExtension = jsonObject.getJSONObject("equalNoOfResourcesWithExtension").toString();

        Scope newScope = new Scope();
        newScope.setName("newScope");
        newScope.setKey("newScope");
        newScope.setRoles("admin");
        newScope.setDescription("newScopeDescription");

        URITemplate updatedItemGet = new URITemplate();
        updatedItemGet.setUriTemplate("/items");
        updatedItemGet.setAuthType("Application & Application User");
        updatedItemGet.setAuthTypes("Application & Application User");
        updatedItemGet.setHTTPVerb("GET");
        updatedItemGet.setHttpVerbs("GET");
        updatedItemGet.setThrottlingTier("Unlimited");
        updatedItemGet.setThrottlingTiers("Unlimited");
        updatedItemGet.setThrottlingLimit("Unlimited");
        updatedItemGet.setScope(newScope);
        updatedItemGet.setScopes(newScope);

        APIIdentifier identifier = new APIIdentifier("admin", "simple", "1.0.0");
        API api = new API(identifier);
        api.setScopes(new HashSet<>(Arrays.asList(sampleScope, extensionScope, newScope)));
        api.setUriTemplates(new HashSet<>(Arrays.asList(petPost, updatedItemGet)));

        String definition = parser.generateAPIDefinition(new SwaggerData(api), equalNoOfResourcesWithExtension);
        APIDefinitionValidationResponse response = parser.validateAPIDefinition(definition, false);
        Assert.assertTrue(response.isValid());
        return definition;
    }

    protected URITemplate getUriTemplate(String httpVerb, String authType, String uriTemplateString) {
        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setAuthTypes(authType);
        uriTemplate.setAuthType(authType);
        uriTemplate.setHTTPVerb(httpVerb);
        uriTemplate.setHttpVerbs(httpVerb);
        uriTemplate.setUriTemplate(uriTemplateString);
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setThrottlingTiers("Unlimited");
        uriTemplate.setThrottlingLimit("Unlimited");
        uriTemplate.setScope(null);
        return uriTemplate;
    }

    interface OASParserEvaluator {
        void eval(String definition);
    }
}
