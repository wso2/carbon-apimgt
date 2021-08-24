/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.juddi.v3.error.RegistryException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.EndpointSecurity;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ConditionDto;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.eq;
import static org.wso2.carbon.apimgt.impl.utils.APIUtil.DISABLE_ROLE_VALIDATION_AT_SCOPE_CREATION;
import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getOAuthConfigurationFromAPIMConfig;
import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getOAuthConfigurationFromTenantRegistry;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {LogFactory.class, ServiceReferenceHolder.class, SSLSocketFactory.class, CarbonUtils.class,
        GovernanceUtils.class, AuthorizationManager.class, MultitenantUtils.class, GenericArtifactManager.class,
        APIUtil.class, KeyManagerHolder.class,ApiMgtDAO.class})
@PowerMockIgnore("javax.net.ssl.*")
public class APIUtilTest {

    @Test
    public void testGetAPINamefromRESTAPI() throws Exception {
        String restAPI = "admin--map";
        String apiName = APIUtil.getAPINamefromRESTAPI(restAPI);

        Assert.assertEquals(apiName, "map");
    }

    @Test
    public void testGetAPIProviderFromRESTAPI() throws Exception {
        String restAPI = "admin--map";
        String providerName = APIUtil.getAPIProviderFromRESTAPI(restAPI, null);

        Assert.assertEquals(providerName, "admin@carbon.super");

        restAPI = "user@test.com--map";
        providerName = APIUtil.getAPIProviderFromRESTAPI(restAPI, "test.com");
        Assert.assertEquals(providerName, "user@test.com");

        restAPI = "user-AT-test.com--map";
        providerName = APIUtil.getAPIProviderFromRESTAPI(restAPI, "test.com");
        Assert.assertEquals(providerName, "user@test.com");

    }

    @Test
    public void testIsValidURL() throws Exception {
        String validURL = "http://fsdfsfd.sda";

        Assert.assertTrue(APIUtil.isValidURL(validURL));

        String invalidURL = "sadafvsdfwef";

        Assert.assertFalse(APIUtil.isValidURL(invalidURL));
        Assert.assertFalse(APIUtil.isValidURL(null));
    }

    @Test
    public void testgGetUserNameWithTenantSuffix() throws Exception {
        String plainUserName = "john";

        String userNameWithTenantSuffix = APIUtil.getUserNameWithTenantSuffix(plainUserName);

        Assert.assertEquals("john@carbon.super", userNameWithTenantSuffix);

        String userNameWithDomain = "john@smith.com";

        userNameWithTenantSuffix = APIUtil.getUserNameWithTenantSuffix(userNameWithDomain);

        Assert.assertEquals("john@smith.com", userNameWithTenantSuffix);
    }

    @Test
    public void testGetRESTAPIScopesFromConfig() throws Exception {
        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());

        String tenantConfValue = FileUtils.readFileToString(siteConfFile);

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(tenantConfValue);
        JSONObject restapiScopes = (JSONObject) json.get("RESTAPIScopes");

        Map<String, String> expectedScopes = new HashMap<String, String>();
        JSONArray scopes = (JSONArray) restapiScopes.get("Scope");
        JSONObject roleMappings = (JSONObject) restapiScopes.get("RoleMappings");

        for (Object scopeObj : scopes) {
            JSONObject scope = (JSONObject) scopeObj;
            String name = (String) scope.get("Name");
            String roles = (String) scope.get("Roles");
            expectedScopes.put(name, roles);
        }

        Map<String, String> restapiScopesFromConfig = APIUtil.getRESTAPIScopesFromConfig(restapiScopes, roleMappings);

        Assert.assertEquals(expectedScopes, restapiScopesFromConfig);
    }

    @Test
    public void testGetRESTAPIScopesFromConfigWithRoleMappings() throws Exception {
        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());

        String tenantConfValue = FileUtils.readFileToString(siteConfFile);

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(tenantConfValue);
        JSONObject restapiScopes = (JSONObject) json.get("RESTAPIScopes");

        Map<String, String> expectedScopes = new HashMap<String, String>();
        JSONArray scopes = (JSONArray) restapiScopes.get("Scope");
        JSONObject roleMappings = new JSONObject();
        roleMappings.put("Internal/publisher", "publisher");

        for (Object scopeObj : scopes) {
            JSONObject scope = (JSONObject) scopeObj;
            String name = (String) scope.get("Name");
            String roles = (String) scope.get("Roles");
            //replace Internal/publisher role for publisher role and remove white spaces
            roles = roles.replace("Internal/publisher", "publisher");
            roles = roles.replace(" ", "");
            expectedScopes.put(name, roles);
        }

        Map<String, String> restapiScopesFromConfig = APIUtil.getRESTAPIScopesFromConfig(restapiScopes, roleMappings);

        Assert.assertEquals(expectedScopes, restapiScopesFromConfig);
    }

    @Test
    public void testIsSandboxEndpointsExists() throws Exception {
        API api = Mockito.mock(API.class);

        JSONObject sandboxEndpoints = new JSONObject();
        sandboxEndpoints.put("url", "https:\\/\\/localhost:9443\\/am\\/sample\\/pizzashack\\/v1\\/api\\/");
        sandboxEndpoints.put("config", null);

        JSONObject root = new JSONObject();
        root.put("sandbox_endpoints", sandboxEndpoints);
        root.put("endpoint_type", "http");

        Mockito.when(api.getEndpointConfig()).thenReturn(root.toJSONString());

        Assert.assertTrue("Cannot find sandbox endpoint", APIUtil.isSandboxEndpointsExists(root.toJSONString()));
    }

    @Test
    public void testIsSandboxEndpointsNotExists() throws Exception {
        API api = Mockito.mock(API.class);

        JSONObject productionEndpoints = new JSONObject();
        productionEndpoints.put("url", "https:\\/\\/localhost:9443\\/am\\/sample\\/pizzashack\\/v1\\/api\\/");
        productionEndpoints.put("config", null);

        JSONObject root = new JSONObject();
        root.put("production_endpoints", productionEndpoints);
        root.put("endpoint_type", "http");

        Mockito.when(api.getEndpointConfig()).thenReturn(root.toJSONString());

        Assert.assertFalse("Unexpected sandbox endpoint found", APIUtil.isSandboxEndpointsExists(root.toJSONString()));
    }

    @Test
    public void testIsProductionEndpointsExists() throws Exception {
        API api = Mockito.mock(API.class);

        JSONObject productionEndpoints = new JSONObject();
        productionEndpoints.put("url", "https:\\/\\/localhost:9443\\/am\\/sample\\/pizzashack\\/v1\\/api\\/");
        productionEndpoints.put("config", null);

        JSONObject root = new JSONObject();
        root.put("production_endpoints", productionEndpoints);
        root.put("endpoint_type", "http");

        Mockito.when(api.getEndpointConfig()).thenReturn(root.toJSONString());

        Assert.assertTrue("Cannot find production endpoint", APIUtil.isProductionEndpointsExists(root.toJSONString()));
    }

    @Test
    public void testIsProductionEndpointsNotExists() throws Exception {
        API api = Mockito.mock(API.class);

        JSONObject sandboxEndpoints = new JSONObject();
        sandboxEndpoints.put("url", "https:\\/\\/localhost:9443\\/am\\/sample\\/pizzashack\\/v1\\/api\\/");
        sandboxEndpoints.put("config", null);

        JSONObject root = new JSONObject();
        root.put("sandbox_endpoints", sandboxEndpoints);
        root.put("endpoint_type", "http");

        Mockito.when(api.getEndpointConfig()).thenReturn(root.toJSONString());

        Assert.assertFalse("Unexpected production endpoint found", APIUtil.isProductionEndpointsExists(root.toJSONString()));
    }

    @Test
    public void testIsProductionSandboxEndpointsExists() throws Exception {
        API api = Mockito.mock(API.class);

        JSONObject productionEndpoints = new JSONObject();
        productionEndpoints.put("url", "https:\\/\\/localhost:9443\\/am\\/sample\\/pizzashack\\/v1\\/api\\/");
        productionEndpoints.put("config", null);

        JSONObject sandboxEndpoints = new JSONObject();
        sandboxEndpoints.put("url", "https:\\/\\/localhost:9443\\/am\\/sample\\/pizzashack\\/v1\\/api\\/");
        sandboxEndpoints.put("config", null);

        JSONObject root = new JSONObject();
        root.put("production_endpoints", productionEndpoints);
        root.put("sandbox_endpoints", sandboxEndpoints);
        root.put("endpoint_type", "http");

        Mockito.when(api.getEndpointConfig()).thenReturn(root.toJSONString());

        Assert.assertTrue("Cannot find production endpoint", APIUtil.isProductionEndpointsExists(root.toJSONString()));
        Assert.assertTrue("Cannot find sandbox endpoint", APIUtil.isSandboxEndpointsExists(root.toJSONString()));
    }

    @Test
    public void testIsProductionEndpointsInvalidJSON() throws Exception {
        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        API api = Mockito.mock(API.class);

        Mockito.when(api.getEndpointConfig()).thenReturn("</SomeXML>");

        Assert.assertFalse("Unexpected production endpoint found", APIUtil.isProductionEndpointsExists("</SomeXML>"));

        JSONObject productionEndpoints = new JSONObject();
        productionEndpoints.put("url", "https:\\/\\/localhost:9443\\/am\\/sample\\/pizzashack\\/v1\\/api\\/");
        productionEndpoints.put("config", null);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(productionEndpoints);

        Mockito.when(api.getEndpointConfig()).thenReturn(jsonArray.toJSONString());

        Assert.assertFalse("Unexpected production endpoint found", APIUtil.isProductionEndpointsExists(jsonArray.toJSONString()));
    }

    @Test
    public void testIsSandboxEndpointsInvalidJSON() throws Exception {
        Log log = Mockito.mock(Log.class);
        PowerMockito.mockStatic(LogFactory.class);
        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(log);

        API api = Mockito.mock(API.class);

        Mockito.when(api.getEndpointConfig()).thenReturn("</SomeXML>");

        Assert.assertFalse("Unexpected sandbox endpoint found", APIUtil.isSandboxEndpointsExists("</SomeXML>"));

        JSONObject sandboxEndpoints = new JSONObject();
        sandboxEndpoints.put("url", "https:\\/\\/localhost:9443\\/am\\/sample\\/pizzashack\\/v1\\/api\\/");
        sandboxEndpoints.put("config", null);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(sandboxEndpoints);

        Mockito.when(api.getEndpointConfig()).thenReturn(jsonArray.toJSONString());

        Assert.assertFalse("Unexpected sandbox endpoint found", APIUtil.isSandboxEndpointsExists(jsonArray.toJSONString()));
    }

    @Test
    public void testGetAPIInformation() throws Exception {
        GovernanceArtifact artifact = Mockito.mock(GovernanceArtifact.class);
        Registry registry = Mockito.mock(Registry.class);
        Resource resource = Mockito.mock(Resource.class);

        API expectedAPI = getUniqueAPI();

        String artifactPath = "";
        PowerMockito.mockStatic(GovernanceUtils.class);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, expectedAPI.getUUID())).thenReturn(artifactPath);
        Mockito.when(registry.get(artifactPath)).thenReturn(resource);
        Mockito.when(resource.getLastModified()).thenReturn(expectedAPI.getLastUpdated());

        DateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date createdTime = df.parse(expectedAPI.getCreatedTime());
        Mockito.when(resource.getCreatedTime()).thenReturn(createdTime);

        ServiceReferenceHolderMockCreator holderMockCreator = new ServiceReferenceHolderMockCreator(1);
        APIManagerConfiguration apimConfiguration = holderMockCreator.getConfigurationServiceMockCreator().
                getConfigurationMockCreator().getMock();

        CORSConfiguration corsConfiguration = expectedAPI.getCorsConfiguration();

        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS)).
                thenReturn(corsConfiguration.getAccessControlAllowHeaders().toString());
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS)).
                thenReturn(corsConfiguration.getAccessControlAllowMethods().toString());
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN)).
                thenReturn(corsConfiguration.getAccessControlAllowOrigins().toString());


        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).
                thenReturn(expectedAPI.getId().getProviderName());
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME)).
                thenReturn(expectedAPI.getId().getApiName());
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION)).
                thenReturn(expectedAPI.getId().getVersion());
        Mockito.when(artifact.getId()).thenReturn(expectedAPI.getUUID());

        API api = APIUtil.getAPIInformation(artifact, registry);

        Assert.assertEquals(expectedAPI.getId(), api.getId());
        Assert.assertEquals(expectedAPI.getUUID(), api.getUUID());

        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_NAME);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_VERSION);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getLifecycleState();
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_CONTEXT);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_OWNER);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY);
        Mockito.verify(artifact, Mockito.atLeastOnce()).getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
    }


    @Test
    public void testGetMediationSequenceUuidInSequence() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String path = APIConstants.API_CUSTOM_SEQUENCE_LOCATION + File.separator + APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN;
        Mockito.when(registry.get(eq(path))).thenReturn(collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        String expectedUUID = UUID.randomUUID().toString();

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);
        Mockito.when(resource.getUUID()).thenReturn(expectedUUID);


        String actualUUID = APIUtil.getMediationSequenceUuid("sample", 1, "in", apiIdentifier);

        Assert.assertEquals(expectedUUID, actualUUID);
        sampleSequence.close();
    }

    @Test
    public void testGetMediationSequenceUuidOutSequence() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String path = APIConstants.API_CUSTOM_SEQUENCE_LOCATION + File.separator + APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT;
        Mockito.when(registry.get(eq(path))).thenReturn(collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        String expectedUUID = UUID.randomUUID().toString();

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);
        Mockito.when(resource.getUUID()).thenReturn(expectedUUID);


        String actualUUID = APIUtil.getMediationSequenceUuid("sample", 1, "out", apiIdentifier);

        Assert.assertEquals(expectedUUID, actualUUID);
        sampleSequence.close();
    }

    @Test
    public void testGetMediationSequenceUuidFaultSequence() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String path = APIConstants.API_CUSTOM_SEQUENCE_LOCATION + File.separator + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT;
        Mockito.when(registry.get(eq(path))).thenReturn(collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        String expectedUUID = UUID.randomUUID().toString();

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);
        Mockito.when(resource.getUUID()).thenReturn(expectedUUID);


        String actualUUID = APIUtil.getMediationSequenceUuid("sample", 1, "fault", apiIdentifier);

        Assert.assertEquals(expectedUUID, actualUUID);
        sampleSequence.close();
    }


    @Test
    public void testGetMediationSequenceUuidCustomSequence() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "custom" + RegistryConstants.PATH_SEPARATOR;

        Mockito.when(registry.get(eq(path))).thenReturn(collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        String expectedUUID = UUID.randomUUID().toString();

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);
        Mockito.when(resource.getUUID()).thenReturn(expectedUUID);


        String actualUUID = APIUtil.getMediationSequenceUuid("sample", 1, "custom", apiIdentifier);

        Assert.assertEquals(expectedUUID, actualUUID);
        sampleSequence.close();
    }


    @Test
    public void testGetMediationSequenceUuidCustomSequenceNotFound() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "custom" + RegistryConstants.PATH_SEPARATOR;

        Mockito.when(registry.get(eq(path))).thenReturn(null, collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        String expectedUUID = UUID.randomUUID().toString();

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);
        Mockito.when(resource.getUUID()).thenReturn(expectedUUID);


        String actualUUID = APIUtil.getMediationSequenceUuid("sample", 1, "custom", apiIdentifier);

        Assert.assertEquals(expectedUUID, actualUUID);
        sampleSequence.close();
    }

    @Test
    public void testIsPerAPISequence() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "in" + RegistryConstants.PATH_SEPARATOR;

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);
        Mockito.when(registry.resourceExists(eq(path))).thenReturn(true);

        Collection collection = Mockito.mock(Collection.class);
        Mockito.when(registry.get(eq(path))).thenReturn(collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                        getResource("sampleSequence.xml").getFile());
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);

        boolean isPerAPiSequence = APIUtil.isPerAPISequence("sample", 1, apiIdentifier, "in");

        Assert.assertTrue(isPerAPiSequence);
    }

    @Test
    public void testIsPerAPISequenceResourceMissing() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "in" + RegistryConstants.PATH_SEPARATOR;

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);
        Mockito.when(registry.resourceExists(eq(path))).thenReturn(false);

        boolean isPerAPiSequence = APIUtil.isPerAPISequence("sample", 1, apiIdentifier, "in");

        Assert.assertFalse(isPerAPiSequence);
    }

    @Test
    public void testIsPerAPISequenceSequenceMissing() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "in" + RegistryConstants.PATH_SEPARATOR;

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);
        Mockito.when(registry.resourceExists(eq(path))).thenReturn(true);
        Mockito.when(registry.get(eq(path))).thenReturn(null);

        boolean isPerAPiSequence = APIUtil.isPerAPISequence("sample", 1, apiIdentifier, "in");

        Assert.assertFalse(isPerAPiSequence);
    }

    @Test
    public void testIsPerAPISequenceNoPathsInCollection() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "in" + RegistryConstants.PATH_SEPARATOR;

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);
        Mockito.when(registry.resourceExists(eq(path))).thenReturn(false);

        Collection collection = Mockito.mock(Collection.class);
        Mockito.when(registry.get(eq(path))).thenReturn(collection);

        String[] childPaths = {};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        boolean isPerAPiSequence = APIUtil.isPerAPISequence("sample", 1, apiIdentifier, "in");

        Assert.assertFalse(isPerAPiSequence);
    }


    @Test
    public void testGetCustomInSequence() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "in" + RegistryConstants.PATH_SEPARATOR;

        Mockito.when(registry.get(eq(path))).thenReturn(collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);


        OMElement customSequence = APIUtil.getCustomSequence("sample", 1, "in", apiIdentifier);

        Assert.assertNotNull(customSequence);
        sampleSequence.close();
    }

    @Test
    public void testGetCustomOutSequence() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "out" + RegistryConstants.PATH_SEPARATOR;

        Mockito.when(registry.get(eq(path))).thenReturn(collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);


        OMElement customSequence = APIUtil.getCustomSequence("sample", 1, "out", apiIdentifier);

        Assert.assertNotNull(customSequence);
        sampleSequence.close();
    }

    @Test
    public void testGetCustomFaultSequence() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "fault" + RegistryConstants.PATH_SEPARATOR;

        Mockito.when(registry.get(eq(path))).thenReturn(collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);


        OMElement customSequence = APIUtil.getCustomSequence("sample", 1, "fault", apiIdentifier);

        Assert.assertNotNull(customSequence);
        sampleSequence.close();
    }

    @Test
    public void testGetCustomSequenceNotFound() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "custom" + RegistryConstants.PATH_SEPARATOR;

        Mockito.when(registry.get(eq(path))).thenReturn(null, collection);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        String expectedUUID = UUID.randomUUID().toString();

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);
        Mockito.when(resource.getUUID()).thenReturn(expectedUUID);


        OMElement customSequence = APIUtil.getCustomSequence("sample", 1, "custom", apiIdentifier);

        Assert.assertNotNull(customSequence);
        sampleSequence.close();
    }

    @Test
    public void testGetCustomSequenceNull() throws Exception {
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry registry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getGovernanceSystemRegistry(eq(1))).thenReturn(registry);

        Collection collection = Mockito.mock(Collection.class);
        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiIdentifier.getApiName() + RegistryConstants.PATH_SEPARATOR + apiIdentifier.getVersion();
        String path = artifactPath + RegistryConstants.PATH_SEPARATOR + "custom" + RegistryConstants.PATH_SEPARATOR;

        Mockito.when(registry.get(eq(path))).thenReturn(null, null);

        String[] childPaths = {"test"};
        Mockito.when(collection.getChildren()).thenReturn(childPaths);

        String expectedUUID = UUID.randomUUID().toString();

        InputStream sampleSequence = new FileInputStream(Thread.currentThread().getContextClassLoader().
                getResource("sampleSequence.xml").getFile());

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registry.get(eq("test"))).thenReturn(resource);
        Mockito.when(resource.getContentStream()).thenReturn(sampleSequence);
        Mockito.when(resource.getUUID()).thenReturn(expectedUUID);


        OMElement customSequence = APIUtil.getCustomSequence("sample", 1, "custom", apiIdentifier);

        Assert.assertNull(customSequence);
        sampleSequence.close();
    }

    @Test
    public void testCreateSwaggerJSONContent() throws Exception {
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Environment environment = Mockito.mock(Environment.class);
        Map<String, Environment> environmentMap = new HashMap<String, Environment>();
        environmentMap.put("Production", environment);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getApiGatewayEnvironments()).thenReturn(environmentMap);
        Mockito.when(environment.getApiGatewayEndpoint()).thenReturn("");

        String swaggerJSONContent = APIUtil.createSwaggerJSONContent(getUniqueAPI());

        Assert.assertNotNull(swaggerJSONContent);
    }

    @Test
    public void testIsRoleNameExist() throws Exception {
        String userName = "John";
        String roleName = "developer";

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.isExistingRole(roleName)).thenReturn(true);

        Mockito.when(userStoreManager.isExistingRole("NonExistingDomain/role")).thenThrow(UserStoreException.class);
        Mockito.when(userStoreManager.isExistingRole("NonExistingDomain/")).thenThrow(UserStoreException.class);
        
        Assert.assertTrue(APIUtil.isRoleNameExist(userName, roleName));
        Assert.assertFalse(APIUtil.isRoleNameExist(userName, "NonExistingDomain/role"));
        Assert.assertFalse(APIUtil.isRoleNameExist(userName, "NonExistingDomain/"));
        Assert.assertTrue(APIUtil.isRoleNameExist(userName, ""));//allow adding empty role
    }

    @Test
    public void testIsRoleNameNotExist() throws Exception {
        String userName = "John";
        String roleName = "developer";

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.isExistingRole(roleName)).thenReturn(false);

        Assert.assertFalse(APIUtil.isRoleNameExist(userName, roleName));
    }

    @Test
    public void testIsRoleNameExistDisableRoleValidation() throws Exception {
        String userName = "John";
        String roleName = "developer";

        System.setProperty(DISABLE_ROLE_VALIDATION_AT_SCOPE_CREATION, "true");

        Assert.assertTrue(APIUtil.isRoleNameExist(userName, roleName));

        Assert.assertTrue(APIUtil.isRoleNameExist(userName, null));

        Assert.assertTrue(APIUtil.isRoleNameExist(userName, ""));

        System.clearProperty(DISABLE_ROLE_VALIDATION_AT_SCOPE_CREATION);
    }

    @Test
    public void testGetRoleNamesSuperTenant() throws Exception {
        String userName = "John";

        String[] roleNames = {"role1", "role2"};

        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);

        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(AuthorizationManager.class);
        Mockito.when(MultitenantUtils.getTenantDomain(userName)).
                thenReturn(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.when(AuthorizationManager.getInstance()).thenReturn(authorizationManager);
        Mockito.when(authorizationManager.getRoleNames()).thenReturn(roleNames);


        Assert.assertEquals(roleNames, APIUtil.getRoleNames(userName));
    }

    @Test
    public void testCreateAPIArtifactContent() throws Exception {
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
            API api = getUniqueAPI();
            Mockito.when(genericArtifact.getAttributeKeys()).thenReturn(new String[] {"URITemplate"}).thenThrow
                    (GovernanceException.class);
            ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
            PowerMockito.mockStatic(ApiMgtDAO.class);
            Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
            Mockito.when(apiMgtDAO.getAllLabels(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))
                    .thenReturn(new ArrayList<Label>());
            APIUtil.createAPIArtifactContent(genericArtifact, api);
            Assert.assertTrue(true);
            APIUtil.createAPIArtifactContent(genericArtifact, api);
            Assert.fail();
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Failed to create API for :"));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    @Test
    public void testGetDocumentation() throws GovernanceException, APIManagementException {
        PowerMockito.mockStatic(CarbonUtils.class);
        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/abc").thenReturn("/");
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_TYPE)).thenReturn(DocumentationType.HOWTO.getType
                ()).thenReturn(DocumentationType.PUBLIC_FORUM.getType()).thenReturn(DocumentationType.SUPPORT_FORUM
                .getType()).thenReturn(DocumentationType.API_MESSAGE_FORMAT.getType()).thenReturn(DocumentationType
                .SAMPLES.getType()).thenReturn(DocumentationType.OTHER.getType());
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_NAME)).thenReturn("Docname");
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_VISIBILITY)).thenReturn(null).thenReturn
                (Documentation.DocumentVisibility.API_LEVEL.name()).thenReturn(Documentation.DocumentVisibility
                .PRIVATE.name()).thenReturn(Documentation.DocumentVisibility.OWNER_ONLY.name());
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_SOURCE_TYPE)).thenReturn(Documentation
                .DocumentSourceType.URL.name()).thenReturn(Documentation.DocumentSourceType.FILE.name());
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_SOURCE_URL)).thenReturn("https://localhost");
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_FILE_PATH)).thenReturn("file://abc");
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME)).thenReturn("abc");
        APIUtil.getDocumentation(genericArtifact);
        APIUtil.getDocumentation(genericArtifact);
        APIUtil.getDocumentation(genericArtifact);
        APIUtil.getDocumentation(genericArtifact);
        APIUtil.getDocumentation(genericArtifact);
        APIUtil.getDocumentation(genericArtifact);

    }

    @Test
    public void testGetDocumentationByDocCreator() throws Exception {
        PowerMockito.mockStatic(CarbonUtils.class);
        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/abc").thenReturn("/");
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_TYPE)).thenReturn(DocumentationType.HOWTO.getType
                ()).thenReturn(DocumentationType.PUBLIC_FORUM.getType()).thenReturn(DocumentationType.SUPPORT_FORUM
                .getType()).thenReturn(DocumentationType.API_MESSAGE_FORMAT.getType()).thenReturn(DocumentationType
                .SAMPLES.getType()).thenReturn(DocumentationType.OTHER.getType());
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_NAME)).thenReturn("Docname");
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_VISIBILITY)).thenReturn(null).thenReturn
                (Documentation.DocumentVisibility.API_LEVEL.name()).thenReturn(Documentation.DocumentVisibility
                .PRIVATE.name()).thenReturn(Documentation.DocumentVisibility.OWNER_ONLY.name());
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_SOURCE_TYPE)).thenReturn(Documentation
                .DocumentSourceType.URL.name()).thenReturn(Documentation.DocumentSourceType.FILE.name());
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_SOURCE_URL)).thenReturn("https://localhost");
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_FILE_PATH)).thenReturn("file://abc");
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME)).thenReturn("abc");
        APIUtil.getDocumentation(genericArtifact, "admin");
        APIUtil.getDocumentation(genericArtifact, "admin");
        APIUtil.getDocumentation(genericArtifact, "admin");
        APIUtil.getDocumentation(genericArtifact, "admin");
        APIUtil.getDocumentation(genericArtifact, "admin");
        APIUtil.getDocumentation(genericArtifact, "admin");
        APIUtil.getDocumentation(genericArtifact, "admin@wso2.com");
    }

    @Test
    public void testVisibilityOfDoc() throws Exception {
        PowerMockito.mockStatic(CarbonUtils.class);
        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/abc").thenReturn("/");
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_TYPE)).thenReturn(DocumentationType.HOWTO.getType
                ()).thenReturn(DocumentationType.PUBLIC_FORUM.getType()).thenReturn(DocumentationType.SUPPORT_FORUM
                .getType()).thenReturn(DocumentationType.API_MESSAGE_FORMAT.getType()).thenReturn(DocumentationType
                .SAMPLES.getType()).thenReturn(DocumentationType.OTHER.getType());
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_NAME)).thenReturn("Docname");
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_SOURCE_TYPE)).thenReturn(Documentation
                .DocumentSourceType.URL.name()).thenReturn(Documentation.DocumentSourceType.FILE.name());
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_SOURCE_URL)).thenReturn("https://localhost");
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_FILE_PATH)).thenReturn("file://abc");
        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME)).thenReturn("abc");

        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_VISIBILITY)).thenReturn(null);
        Assert.assertEquals(APIUtil.getDocumentation(genericArtifact, "admin@wso2.com").getVisibility()
                .name(), Documentation.DocumentVisibility.API_LEVEL.name());

        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_VISIBILITY)).
                thenReturn(Documentation.DocumentVisibility.API_LEVEL.name());
        Assert.assertEquals(APIUtil.getDocumentation(genericArtifact, "admin@wso2.com").getVisibility().
                name(), Documentation.DocumentVisibility.API_LEVEL.name());

        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_VISIBILITY)).
                thenReturn(Documentation.DocumentVisibility.PRIVATE.name());
        Assert.assertEquals(APIUtil.getDocumentation(genericArtifact, "admin@wso2.com").getVisibility().
                name(), Documentation.DocumentVisibility.PRIVATE.name());

        Mockito.when(genericArtifact.getAttribute(APIConstants.DOC_VISIBILITY)).
                thenReturn(Documentation.DocumentVisibility.OWNER_ONLY.name());
        Assert.assertEquals(APIUtil.getDocumentation(genericArtifact, "admin@wso2.com").getVisibility().
                name(), Documentation.DocumentVisibility.OWNER_ONLY.name());
    }

    @Test
    public void testCreateDocArtifactContent() throws GovernanceException, APIManagementException {
        API api = getUniqueAPI();
        PowerMockito.mockStatic(CarbonUtils.class);
        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/abc").thenReturn("/");
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
        Documentation documentation = new Documentation(DocumentationType.HOWTO, "this is a doc");
        documentation.setSourceType(Documentation.DocumentSourceType.FILE);
        documentation.setCreatedDate(new Date(System.currentTimeMillis()));
        documentation.setSummary("abcde");
        documentation.setVisibility(Documentation.DocumentVisibility.API_LEVEL);
        documentation.setSourceUrl("/abcd/def");
        documentation.setOtherTypeName("aa");
        APIUtil.createDocArtifactContent(genericArtifact, api.getId(), documentation);
        documentation.setSourceType(Documentation.DocumentSourceType.INLINE);
        APIUtil.createDocArtifactContent(genericArtifact, api.getId(), documentation);
        documentation.setSourceType(Documentation.DocumentSourceType.URL);
        APIUtil.createDocArtifactContent(genericArtifact, api.getId(), documentation);

        try {
            documentation.setSourceType(Documentation.DocumentSourceType.URL);
            Mockito.doThrow(GovernanceException.class).when(genericArtifact).setAttribute(APIConstants
                    .DOC_SOURCE_URL, documentation.getSourceUrl());
            APIUtil.createDocArtifactContent(genericArtifact, api.getId(), documentation);
            Assert.fail();
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Failed to create doc artifact content from :"));
        }
    }

    @Test
    public void testGetArtifactManager()  {
        PowerMockito.mockStatic(GenericArtifactManager.class);
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        Registry registry = Mockito.mock(UserRegistry.class);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PowerMockito.mockStatic(GovernanceUtils.class);
            PowerMockito.doNothing().when(GovernanceUtils.class, "loadGovernanceArtifacts",(UserRegistry)registry);
            Mockito.when(GovernanceUtils.findGovernanceArtifactConfiguration(APIConstants.API_KEY, registry))
                    .thenReturn(Mockito.mock(GovernanceArtifactConfiguration.class)).thenReturn(null).thenThrow
                    (RegistryException.class);
            GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
            PowerMockito.whenNew(GenericArtifactManager.class).withArguments(registry, APIConstants.API_KEY)
                    .thenReturn(genericArtifactManager);
            GenericArtifactManager retrievedGenericArtifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            Assert.assertEquals(genericArtifactManager, retrievedGenericArtifactManager);
            APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            Assert.fail();
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Failed to initialize GenericArtifactManager"));
        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }


    @Test
    public void testGetRoleNamesNonSuperTenant() throws Exception {
        String userName = "John";

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);

        String[] roleNames = {"role1", "role2"};

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        Mockito.when(MultitenantUtils.getTenantDomain(userName)).
                thenReturn("test.com");
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.getRoleNames()).thenReturn(roleNames);

        Assert.assertEquals(roleNames, APIUtil.getRoleNames(userName));
    }


    @Test
    public void testGetAPI() throws Exception {
        API expectedAPI = getUniqueAPI();

        final String provider = expectedAPI.getId().getProviderName();
        final String tenantDomain = org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        final int tenantId = -1234;

        GovernanceArtifact artifact = Mockito.mock(GovernanceArtifact.class);
        Registry registry = Mockito.mock(Registry.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Resource resource = Mockito.mock(Resource.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
        SubscriptionPolicy policy = Mockito.mock(SubscriptionPolicy.class);
        SubscriptionPolicy[] policies = new SubscriptionPolicy[]{policy};
        QuotaPolicy quotaPolicy = Mockito.mock(QuotaPolicy.class);
        RequestCountLimit limit = Mockito.mock(RequestCountLimit.class);

        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.getAPIID(Mockito.any(APIIdentifier.class), eq((Connection) null))).thenReturn(123);
        Mockito.when(artifact.getId()).thenReturn("");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn(provider);
        Mockito.when(MultitenantUtils.getTenantDomain(provider)).
                thenReturn(tenantDomain);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantId);

        String artifactPath = "";
        PowerMockito.mockStatic(GovernanceUtils.class);
        Mockito.when(GovernanceUtils.getArtifactPath(registry, "")).thenReturn(artifactPath);
        Mockito.when(registry.get(artifactPath)).thenReturn(resource);
        Mockito.when(resource.getLastModified()).thenReturn(expectedAPI.getLastUpdated());
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(tenantId)).thenReturn(policies);
        Mockito.when(policy.getPolicyName()).thenReturn("policy");
        Mockito.when(policy.getDefaultQuotaPolicy()).thenReturn(quotaPolicy);
        Mockito.when(quotaPolicy.getLimit()).thenReturn(limit);
        Mockito.when(registry.getTags(artifactPath)).thenReturn(getTagsFromSet(expectedAPI.getTags()));

        HashMap<String, String> urlPatterns = getURLTemplatePattern(expectedAPI.getUriTemplates());
        Mockito.when(apiMgtDAO.getURITemplatesPerAPIAsString(Mockito.any(APIIdentifier.class))).thenReturn(urlPatterns);

        CORSConfiguration corsConfiguration = expectedAPI.getCorsConfiguration();

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS)).
                thenReturn(corsConfiguration.getAccessControlAllowHeaders().toString());
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS)).
                thenReturn(corsConfiguration.getAccessControlAllowMethods().toString());
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN)).
                thenReturn(corsConfiguration.getAccessControlAllowOrigins().toString());

        API api = APIUtil.getAPI(artifact, registry);

        Assert.assertNotNull(api);
    }

    @Test
    public void testGetAPIForPublishing() throws Exception {
        API expectedAPI = getUniqueAPI();

        final String provider = expectedAPI.getId().getProviderName();
        final String tenantDomain = org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        final int tenantId = -1234;

        GovernanceArtifact artifact = Mockito.mock(GovernanceArtifact.class);
        Registry registry = Mockito.mock(Registry.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Resource resource = Mockito.mock(Resource.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
        SubscriptionPolicy policy = Mockito.mock(SubscriptionPolicy.class);
        SubscriptionPolicy[] policies = new SubscriptionPolicy[]{policy};
        QuotaPolicy quotaPolicy = Mockito.mock(QuotaPolicy.class);
        RequestCountLimit limit = Mockito.mock(RequestCountLimit.class);

        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);

        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.getAPIID(Mockito.any(APIIdentifier.class), eq((Connection) null))).thenReturn(123);
        Mockito.when(artifact.getId()).thenReturn("");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn(provider);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT)).thenReturn("15");
        Mockito.when(MultitenantUtils.getTenantDomain(provider)).thenReturn(tenantDomain);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantId);

        String artifactPath = "";
        Mockito.when(GovernanceUtils.getArtifactPath(registry, "")).thenReturn(artifactPath);
        Mockito.when(registry.get(artifactPath)).thenReturn(resource);
        Mockito.when(resource.getLastModified()).thenReturn(expectedAPI.getLastUpdated());
        Mockito.when(resource.getCreatedTime()).thenReturn(expectedAPI.getLastUpdated());
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(tenantId)).thenReturn(policies);
        Mockito.when(policy.getPolicyName()).thenReturn("policy");
        Mockito.when(policy.getDefaultQuotaPolicy()).thenReturn(quotaPolicy);
        Mockito.when(quotaPolicy.getLimit()).thenReturn(limit);
        Mockito.when(registry.getTags(artifactPath)).thenReturn(getTagsFromSet(expectedAPI.getTags()));

        HashMap<String, String> urlPatterns = getURLTemplatePattern(expectedAPI.getUriTemplates());
        Mockito.when(apiMgtDAO.getURITemplatesPerAPIAsString(Mockito.any(APIIdentifier.class))).thenReturn(urlPatterns);

        CORSConfiguration corsConfiguration = expectedAPI.getCorsConfiguration();

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS)).
                thenReturn(corsConfiguration.getAccessControlAllowHeaders().toString());
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS)).
                thenReturn(corsConfiguration.getAccessControlAllowMethods().toString());
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN)).
                thenReturn(corsConfiguration.getAccessControlAllowOrigins().toString());

        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG))
                .thenReturn("{\"production_endpoints\":{\"url\":\"http://www.mocky.io/v2/5b21fe0f2e00002a00e313fe\"," +
                        "\"config\":null,\"template_not_supported\":false}," +
                        "\"sandbox_endpoints\":{\"url\":\"http://www.mocky.io/v2/5b21fe0f2e00002a00e313fe\"," +
                        "\"config\":null,\"template_not_supported\":false},\"endpoint_type\":\"http\"}");

        API api = APIUtil.getAPIForPublishing(artifact, registry);

        Assert.assertNotNull(api);

        Set<String> testEnvironmentList = new HashSet<String>();
        testEnvironmentList.add("PRODUCTION");
        testEnvironmentList.add("SANDBOX");
        Assert.assertThat(SetUtils.isEqualSet(api.getEnvironmentList(), testEnvironmentList), is(true));
    }

    @Test
    public void testGetAPIWithGovernanceArtifact() throws Exception {
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            API expectedAPI = getUniqueAPI();

            final String provider = expectedAPI.getId().getProviderName();
            final String tenantDomain = org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

            final int tenantId = -1234;

            System.setProperty("carbon.home", "");

            File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                    getResource("tenant-conf.json").getFile());

            String tenantConfValue = FileUtils.readFileToString(siteConfFile);

            GovernanceArtifact artifact = Mockito.mock(GovernanceArtifact.class);
            Registry registry = Mockito.mock(Registry.class);
            ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
            Resource resource = Mockito.mock(Resource.class);
            ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
            RealmService realmService = Mockito.mock(RealmService.class);
            TenantManager tenantManager = Mockito.mock(TenantManager.class);
            APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
            APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
            ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
            SubscriptionPolicy policy = Mockito.mock(SubscriptionPolicy.class);
            SubscriptionPolicy[] policies = new SubscriptionPolicy[] {policy};
            QuotaPolicy quotaPolicy = Mockito.mock(QuotaPolicy.class);
            RequestCountLimit limit = Mockito.mock(RequestCountLimit.class);
            PrivilegedCarbonContext carbonContext = Mockito.mock(PrivilegedCarbonContext.class);
            RegistryService registryService = Mockito.mock(RegistryService.class);
            UserRegistry userRegistry = Mockito.mock(UserRegistry.class);

            PowerMockito.mockStatic(ApiMgtDAO.class);
            PowerMockito.mockStatic(GovernanceUtils.class);
            PowerMockito.mockStatic(MultitenantUtils.class);
            PowerMockito.mockStatic(ServiceReferenceHolder.class);

            Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
            Mockito.when(apiMgtDAO.getAPIID(Mockito.any(APIIdentifier.class), eq((Connection) null))).thenReturn(123);
            Mockito.when(apiMgtDAO.getPolicyNames(PolicyConstants.POLICY_LEVEL_SUB, provider)).thenReturn(new String[] {"Unlimited"});
            Mockito.when(artifact.getId()).thenReturn("");
            Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn(provider);
            Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT)).thenReturn("15");
            Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_TIER)).thenReturn("Unlimited");
            Mockito.when(MultitenantUtils.getTenantDomain(provider)).thenReturn(tenantDomain);
            Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
            Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
            Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
            Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
            Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantId);
            Mockito.when(registryService.getConfigSystemRegistry(tenantId)).thenReturn(userRegistry);
            Mockito.when(userRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
            Mockito.when(userRegistry.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(resource);

            String artifactPath = "";
            Mockito.when(GovernanceUtils.getArtifactPath(registry, "")).thenReturn(artifactPath);
            Mockito.when(registry.get(artifactPath)).thenReturn(resource);
            Mockito.when(resource.getLastModified()).thenReturn(expectedAPI.getLastUpdated());
            Mockito.when(resource.getCreatedTime()).thenReturn(expectedAPI.getLastUpdated());
            Mockito.when(resource.getContent()).thenReturn(tenantConfValue.getBytes());
            Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
            Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
            Mockito.when(apiManagerConfiguration.getThrottleProperties()).thenReturn(throttleProperties);
            Mockito.when(apiMgtDAO.getSubscriptionPolicies(tenantId)).thenReturn(policies);
            Mockito.when(policy.getDefaultQuotaPolicy()).thenReturn(quotaPolicy);
            Mockito.when(quotaPolicy.getLimit()).thenReturn(limit);
            Mockito.when(registry.getTags(artifactPath)).thenReturn(getTagsFromSet(expectedAPI.getTags()));

            ArrayList<URITemplate> urlList = getURLTemplateList(expectedAPI.getUriTemplates());
            Mockito.when(apiMgtDAO.getAllURITemplates(Mockito.anyString(), Mockito.anyString())).thenReturn(urlList);

            CORSConfiguration corsConfiguration = expectedAPI.getCorsConfiguration();

            Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS)).
                    thenReturn(corsConfiguration.getAccessControlAllowHeaders().toString());
            Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS)).
                    thenReturn(corsConfiguration.getAccessControlAllowMethods().toString());
            Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN)).
                    thenReturn(corsConfiguration.getAccessControlAllowOrigins().toString());


            API api = APIUtil.getAPI(artifact);

            Assert.assertNotNull(api);
        }finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test
    public void testGetAPIWithGovernanceArtifactAdvancedThrottlingDisabled() throws Exception {
        System.setProperty("carbon.home", APIUtilTest.class.getResource("/").getFile());
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        API expectedAPI = getUniqueAPI();

        final String provider = expectedAPI.getId().getProviderName();
        final String tenantDomain = org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        final int tenantId = -1234;

        System.setProperty("carbon.home", "");

        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());

        String tenantConfValue = FileUtils.readFileToString(siteConfFile);

        GovernanceArtifact artifact = Mockito.mock(GovernanceArtifact.class);
        Registry registry = Mockito.mock(Registry.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Resource resource = Mockito.mock(Resource.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
        SubscriptionPolicy policy = Mockito.mock(SubscriptionPolicy.class);
        SubscriptionPolicy[] policies = new SubscriptionPolicy[]{policy};
        QuotaPolicy quotaPolicy = Mockito.mock(QuotaPolicy.class);
        RequestCountLimit limit = Mockito.mock(RequestCountLimit.class);
            RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);

        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Mockito.when(apiMgtDAO.getAPIID(Mockito.any(APIIdentifier.class), eq((Connection) null))).thenReturn(123);
        Mockito.when(apiMgtDAO.getPolicyNames(PolicyConstants.POLICY_LEVEL_SUB, provider)).thenReturn(new String[]{"Unlimited"});
        Mockito.when(artifact.getId()).thenReturn("");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER)).thenReturn(provider);
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT)).thenReturn("15");
        Mockito.when(artifact.getAttribute(APIConstants.API_OVERVIEW_TIER)).thenReturn("Unlimited");
        Mockito.when(MultitenantUtils.getTenantDomain(provider)).thenReturn(tenantDomain);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantId);
        Mockito.when(registryService.getConfigSystemRegistry(tenantId)).thenReturn(userRegistry);
        Mockito.when(userRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
        Mockito.when(userRegistry.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(resource);

        String artifactPath = "";
        Mockito.when(GovernanceUtils.getArtifactPath(registry, "")).thenReturn(artifactPath);
        Mockito.when(registry.get(artifactPath)).thenReturn(resource);
        Mockito.when(resource.getLastModified()).thenReturn(expectedAPI.getLastUpdated());
        Mockito.when(resource.getCreatedTime()).thenReturn(expectedAPI.getLastUpdated());
        Mockito.when(resource.getContent()).thenReturn(tenantConfValue.getBytes());
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(apiMgtDAO.getSubscriptionPolicies(tenantId)).thenReturn(policies);
        Mockito.when(policy.getDefaultQuotaPolicy()).thenReturn(quotaPolicy);
        Mockito.when(quotaPolicy.getLimit()).thenReturn(limit);
        Mockito.when(registry.getTags(artifactPath)).thenReturn(getTagsFromSet(expectedAPI.getTags()));

        ArrayList<URITemplate> urlList = getURLTemplateList(expectedAPI.getUriTemplates());
        Mockito.when(apiMgtDAO.getAllURITemplates(Mockito.anyString(), Mockito.anyString())).thenReturn(urlList);

        CORSConfiguration corsConfiguration = expectedAPI.getCorsConfiguration();

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS)).
                thenReturn(corsConfiguration.getAccessControlAllowHeaders().toString());
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS)).
                thenReturn(corsConfiguration.getAccessControlAllowMethods().toString());
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN)).
                thenReturn(corsConfiguration.getAccessControlAllowOrigins().toString());

        API api = APIUtil.getAPI(artifact);

        Assert.assertNotNull(api);
    }finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private API getUniqueAPI() {
        APIIdentifier apiIdentifier = new APIIdentifier(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        API api = new API(apiIdentifier);
        api.setStatus(APIConstants.CREATED);
        api.setContext(UUID.randomUUID().toString());

        Set<String> environments = new HashSet<String>();
        environments.add(UUID.randomUUID().toString());

        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setAuthType("None");
        uriTemplate.setHTTPVerb("GET");
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setUriTemplate("/*");
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        uriTemplates.add(uriTemplate);

        uriTemplate = new URITemplate();
        uriTemplate.setAuthType("None");
        uriTemplate.setHTTPVerb("GET");
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setUriTemplate("/get");
        uriTemplates.add(uriTemplate);

        uriTemplate = new URITemplate();
        uriTemplate.setAuthType("None");
        uriTemplate.setHTTPVerb("POST");
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setUriTemplate("/*");
        uriTemplates.add(uriTemplate);

        uriTemplate = new URITemplate();
        uriTemplate.setAuthType("None");
        uriTemplate.setHTTPVerb("POST");
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setUriTemplate("/post");
        uriTemplates.add(uriTemplate);

        uriTemplate = new URITemplate();
        uriTemplate.setAuthType("None");
        uriTemplate.setHTTPVerb("DELETE");
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setUriTemplate("/*");
        uriTemplates.add(uriTemplate);

        uriTemplate = new URITemplate();
        uriTemplate.setAuthType("None");
        uriTemplate.setHTTPVerb("PUT");
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setUriTemplate("/*");
        uriTemplates.add(uriTemplate);

        uriTemplate = new URITemplate();
        uriTemplate.setAuthType("None");
        uriTemplate.setHTTPVerb("PUT");
        uriTemplate.setThrottlingTier("Unlimited");
        uriTemplate.setUriTemplate("/put");
        uriTemplates.add(uriTemplate);

        api.setUriTemplates(uriTemplates);

        api.setEnvironments(environments);
        api.setUUID(UUID.randomUUID().toString());
        api.setThumbnailUrl(UUID.randomUUID().toString());
        api.setVisibility(UUID.randomUUID().toString());
        api.setVisibleRoles(UUID.randomUUID().toString());
        api.setVisibleTenants(UUID.randomUUID().toString());
        api.setTransports(UUID.randomUUID().toString());
        api.setInSequence(UUID.randomUUID().toString());
        api.setOutSequence(UUID.randomUUID().toString());
        api.setFaultSequence(UUID.randomUUID().toString());
        api.setDescription(UUID.randomUUID().toString());
        api.setRedirectURL(UUID.randomUUID().toString());
        api.setBusinessOwner(UUID.randomUUID().toString());
        api.setApiOwner(UUID.randomUUID().toString());
        api.setAdvertiseOnly(true);

        CORSConfiguration corsConfiguration = new CORSConfiguration(true, Arrays.asList("*"),
                true, Arrays.asList("*"), Arrays.asList("*"));

        api.setCorsConfiguration(corsConfiguration);
        api.setLastUpdated(new Date());
        api.setCreatedTime(new Date().toString());

        Set<Tier> tierSet = new HashSet<Tier>();
        tierSet.add(new Tier("Unlimited"));
        tierSet.add(new Tier("Gold"));
        api.addAvailableTiers(tierSet);
        Set<String> tags = new HashSet<String>();
        tags.add("stuff");
        api.addTags(tags);

        return api;
    }

    private Tag[] getTagsFromSet(Set<String> tagSet) {
        String[] tagNames = tagSet.toArray(new String[tagSet.size()]);

        Tag[] tags = new Tag[tagNames.length];

        for (int i = 0; i < tagNames.length; i++) {
            Tag tag = new Tag();
            tag.setTagName(tagNames[i]);
            tags[i] = tag;
        }

        return tags;
    }

    private HashMap<String, String> getURLTemplatePattern(Set<URITemplate> uriTemplates) {
        HashMap<String, String> pattern = new HashMap<String, String>();

        for (URITemplate uriTemplate : uriTemplates) {
            String key = uriTemplate.getUriTemplate() + "::" + uriTemplate.getHTTPVerb() + "::" +
                    uriTemplate.getAuthType() + "::" + uriTemplate.getThrottlingTier();
            pattern.put(key, uriTemplate.getHTTPVerb());
        }

        return pattern;
    }

    private ArrayList<URITemplate> getURLTemplateList(Set<URITemplate> uriTemplates) {
        ArrayList<URITemplate> list = new ArrayList<URITemplate>();
        list.addAll(uriTemplates);

        return list;

    }

    @Test
    public void testWsdlDefinitionFilePath () {
        Assert.assertEquals(APIUtil.getWSDLDefinitionFilePath("test", "1.0.0", "publisher1")
                , APIConstants.API_WSDL_RESOURCE_LOCATION + "publisher1" + "--" + "test" + "1.0.0" + ".wsdl");
    }

    @Test
    public void testGetOAuthConfigurationFromTenantRegistry () throws Exception{
        final int tenantId = -1234;
        final String property = "AuthorizationHeader";

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(tenantId)).thenReturn(userRegistry);
        Mockito.when(userRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
        Mockito.when(userRegistry.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(resource);
        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());
        String tenantConfValue = FileUtils.readFileToString(siteConfFile);
        Mockito.when(resource.getContent()).thenReturn(tenantConfValue.getBytes());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(tenantConfValue);
        String authorizationHeader = (String)json.get(property);
        String authHeader = getOAuthConfigurationFromTenantRegistry(tenantId,property);
        Assert.assertEquals(authorizationHeader, authHeader);
    }

    @Test
    public void testGetOAuthConfigurationFromAPIMConfig () throws Exception {
        String property = "AuthorizationHeader";
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService =
                Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.OAUTH_CONFIGS + property))
                .thenReturn("APIM_AUTH");

        String authHeader = getOAuthConfigurationFromAPIMConfig(property);
        Assert.assertEquals("APIM_AUTH", authHeader);
    }

    @Test
    public void testGetGatewayEndpoint () throws Exception {
        Environment environment = new Environment();
        environment.setType("Production");
        environment.setName("Production");
        environment.setApiGatewayEndpoint("http://localhost:8280,https://localhost:8243");
        Map<String, Environment> environmentMap = new HashMap<String, Environment>();
        environmentMap.put("Production", environment);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService =
                Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getApiGatewayEnvironments()).thenReturn(environmentMap);
        String gatewayEndpoint = APIUtil.getGatewayEndpoint("http,https", "Production", "Production");
        Assert.assertEquals("https://localhost:8243", gatewayEndpoint);
    }
    @Test
    public void testGetConditionDtoListWithHavingIPSpecificConditionOnly() throws ParseException {
        String base64EncodedString = "W3siaXBzcGVjaWZpYyI6eyJzcGVjaWZpY0lwIjoxNjg0MzAwOTAsImludmVydCI6ZmFsc2V9fV0=";
        List<ConditionDto> conditionDtoList = APIUtil.extractConditionDto(base64EncodedString);
        Assert.assertEquals(conditionDtoList.size(),1);
        ConditionDto conditionDto = conditionDtoList.get(0);
        Assert.assertNotNull(conditionDto.getIpCondition());
    }
    @Test
    public void testGetConditionDtoListWithHavingIPRangeConditionOnly() throws ParseException {
        String base64EncodedString =
                "W3siaXByYW5nZSI6eyJzdGFydGluZ0lwIjoxNjg0MzAwOTAsImVuZGluZ0lwIjoxNjg0MzAwOTEsImludmVydCI6dHJ1ZX19XQ==";
        List<ConditionDto> conditionDtoList = APIUtil.extractConditionDto(base64EncodedString);
        Assert.assertEquals(conditionDtoList.size(),1);
        ConditionDto conditionDto = conditionDtoList.get(0);
        Assert.assertNotNull(conditionDto.getIpRangeCondition());
        Assert.assertTrue(conditionDto.getIpRangeCondition().isInvert());
    }
    @Test
    public void testGetConditionDtoListWithHavingHeaderConditionOnly() throws ParseException {
        String base64EncodedString = "W3siaGVhZGVyIjp7ImludmVydCI6ZmFsc2UsInZhbHVlcyI6eyJhYmMiOiJkZWYifX19XQo=";
        List<ConditionDto> conditionDtoList = APIUtil.extractConditionDto(base64EncodedString);
        Assert.assertEquals(conditionDtoList.size(),1);
        ConditionDto conditionDto = conditionDtoList.get(0);
        Assert.assertNotNull(conditionDto.getHeaderConditions());
        Assert.assertEquals(conditionDto.getHeaderConditions().getValues().size(),1);
    }
    @Test
    public void testGetConditionDtoListWithHavingJWTClaimConditionOnly() throws ParseException {
        String base64EncodedString = "W3siand0Y2xhaW1zIjp7ImludmVydCI6ZmFsc2UsInZhbHVlcyI6eyJhYmMiOiJkZWYifX19XQo=";
        List<ConditionDto> conditionDtoList = APIUtil.extractConditionDto(base64EncodedString);
        Assert.assertEquals(conditionDtoList.size(),1);
        ConditionDto conditionDto = conditionDtoList.get(0);
        Assert.assertNotNull(conditionDto.getJwtClaimConditions());
        Assert.assertEquals(conditionDto.getJwtClaimConditions().getValues().size(),1);
    }
    @Test
    public void testGetConditionDtoListWithHavingQueryParamConditionOnly() throws ParseException {
        String base64EncodedString =
                "W3sicXVlcnlwYXJhbWV0ZXJ0eXBlIjp7ImludmVydCI6ZmFsc2UsInZhbHVlcyI6eyJhYmMiOiJkZWYifX19XQo=";
        List<ConditionDto> conditionDtoList = APIUtil.extractConditionDto(base64EncodedString);
        Assert.assertEquals(conditionDtoList.size(),1);
        ConditionDto conditionDto = conditionDtoList.get(0);
        Assert.assertNotNull(conditionDto.getQueryParameterConditions());
        Assert.assertEquals(conditionDto.getQueryParameterConditions().getValues().size(),1);
    }
    @Test
    public void testGetConditionDtoListWithHavingMultipleConditionTypes() throws ParseException {
        String base64EncodedString = "W3siaXBzcGVjaWZpYyI6eyJzcGVjaWZpY0lwIjoxNzQzMjcxODksImludmVydCI6ZmFsc2V9LCJoZW" +
                "FkZXIiOnsiaW52ZXJ0IjpmYWxzZSwidmFsdWVzIjp7ImFiYyI6ImRlZiJ9fSwiand0Y2xhaW1zIjp7ImludmVydCI6ZmFsc2UsI" +
                "nZhbHVlcyI6eyJhYmMiOiJkZWYifX19XQo=";
        List<ConditionDto> conditionDtoList = APIUtil.extractConditionDto(base64EncodedString);
        Assert.assertEquals(conditionDtoList.size(),1);
        ConditionDto conditionDto = conditionDtoList.get(0);
        Assert.assertNotNull(conditionDto.getIpCondition());
        Assert.assertNotNull(conditionDto.getHeaderConditions());
        Assert.assertNotNull(conditionDto.getJwtClaimConditions());
        Assert.assertEquals(conditionDto.getHeaderConditions().getValues().size(),1);
        Assert.assertEquals(conditionDto.getJwtClaimConditions().getValues().size(),1);
    }
    @Test
    public void testGetConditionDtoListWithHavingMultiplePipelines() throws ParseException {
        String base64EncodedString = "W3siaGVhZGVyIjp7ImludmVydCI6ZmFsc2UsInZhbHVlcyI6eyJhYmMiOiJkZWYifX19LHsiaXBzcG" +
                "VjaWZpYyI6eyJzcGVjaWZpY0lwIjoxNjg0MzAwOTAsImludmVydCI6ZmFsc2V9fV0=";
        List<ConditionDto> conditionDtoList = APIUtil.extractConditionDto(base64EncodedString);
        Assert.assertEquals(conditionDtoList.size(),2);
        Assert.assertNotNull(conditionDtoList.get(0).getIpCondition());
        Assert.assertNotNull(conditionDtoList.get(1).getHeaderConditions());

    }
    @Test
    public void testGetConditionDtoListWithHavingMultiplePipelinesWithVariousConditions() throws ParseException {
        String base64EncodedString = "W3siaGVhZGVyIjp7ImludmVydCI6ZmFsc2UsInZhbHVlcyI6eyJhYmMiOiJkZWYifX19LHsiaXBzcGV" +
                "jaWZpYyI6eyJzcGVjaWZpY0lwIjoxNzQzMjcxODksImludmVydCI6ZmFsc2V9LCJoZWFkZXIiOnsiaW52ZXJ0IjpmYWxzZSwidmF" +
                "sdWVzIjp7ImFiYyI6ImRlZiJ9fX0seyJqd3RjbGFpbXMiOnsiaW52ZXJ0IjpmYWxzZSwidmFsdWVzIjp7ImFiYyI6ImRlZiJ9fX0" +
                "seyJpcHNwZWNpZmljIjp7InNwZWNpZmljSXAiOjE3NDMyNzE4OSwiaW52ZXJ0IjpmYWxzZX0sImp3dGNsYWltcyI6eyJpbnZlcnQ" +
                "iOmZhbHNlLCJ2YWx1ZXMiOnsiYWJjIjoiZGVmIn19fSx7ImlwcmFuZ2UiOnsic3RhcnRpbmdJcCI6MTc0MzI3MTg5LCJlbmRpbmd" +
                "JcCI6MTc0MzI3MjAwLCJpbnZlcnQiOmZhbHNlfX1d";
        List<ConditionDto> conditionDtoList = APIUtil.extractConditionDto(base64EncodedString);
        Assert.assertEquals(conditionDtoList.size(),5);
        Assert.assertNotNull(conditionDtoList.get(0).getIpCondition());
        Assert.assertNotNull(conditionDtoList.get(0).getHeaderConditions());
        Assert.assertNotNull(conditionDtoList.get(1).getIpCondition());
        Assert.assertNotNull(conditionDtoList.get(1).getJwtClaimConditions());
        Assert.assertNotNull(conditionDtoList.get(2).getIpRangeCondition());
        Assert.assertNotNull(conditionDtoList.get(3).getHeaderConditions());
        Assert.assertNotNull(conditionDtoList.get(4).getJwtClaimConditions());
    }

    @Test
    public void testGetAppAttributeKeysFromRegistry() throws Exception {
        final int tenantId = -1234;
        final String property = APIConstants.ApplicationAttributes.APPLICATION_CONFIGURATIONS;

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        Mockito.when(registryService.getConfigSystemRegistry(tenantId)).thenReturn(userRegistry);
        Mockito.when(userRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
        Mockito.when(userRegistry.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(resource);
        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());
        String tenantConfValue = FileUtils.readFileToString(siteConfFile);
        Mockito.when(resource.getContent()).thenReturn(tenantConfValue.getBytes());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(tenantConfValue);
        JSONObject applicationAttributes = (JSONObject) json.get(property);
        JSONObject appAttributes = APIUtil.getAppAttributeKeysFromRegistry(tenantId);
        Assert.assertEquals(applicationAttributes, appAttributes);
    }

    @Test
    public void testSanitizeUserRole() throws Exception {
        Assert.assertEquals("Test%26123", APIUtil.sanitizeUserRole("Test&123"));
        Assert.assertEquals("Test%26123%26test", APIUtil.sanitizeUserRole("Test&123&test"));
        Assert.assertEquals("Test123", APIUtil.sanitizeUserRole("Test123"));
    }
    
    @Test
    public void testIsRoleExistForUser() throws Exception {
        /*
        String userName = "user1";
        String[] userRoleList = {"role1", "role2"};
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(APIUtil.getListOfRoles(userName)).thenReturn(userRoleList);
        Assert.assertFalse(APIUtil.isRoleExistForUser(userName, "roleA,roleB"));
        Assert.assertTrue(APIUtil.isRoleExistForUser(userName, "role1,roleB"));
        //Assert.assertTrue(APIUtil.isRoleExistForUser(userName, "role1,role2"));
        Assert.assertFalse(APIUtil.isRoleExistForUser(userName, ""));
        Assert.assertFalse(APIUtil.isRoleExistForUser(userName, null));
        Assert.assertFalse(APIUtil.isRoleExistForUser(userName, "test"));
        */
    }

    @Test
    public void testGetTokenEndpointsByType () throws Exception {
        Environment environment = new Environment();
        environment.setType("production");
        environment.setName("Production");
        environment.setDefault(true);
        environment.setApiGatewayEndpoint("http://localhost:8280,https://localhost:8243");
        Map<String, Environment> environmentMap = new HashMap<String, Environment>();
        environmentMap.put("Production", environment);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService =
                Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getApiGatewayEnvironments()).thenReturn(environmentMap);
        String tokenEndpointType = APIUtil.getTokenEndpointsByType("production");
        Assert.assertEquals("https://localhost:8243", tokenEndpointType);
    }
    
    @Test
    public void testSetEndpointSecurityForAPIProduct() throws Exception {
        API api = new API(new APIIdentifier("admin", "test", "1.0"));
        api.setEndpointUTPassword("testpassword");
        api.setEndpointUTUsername("testuser");
        api.setEndpointSecured(true);
        Map<String, EndpointSecurity> map = APIUtil.setEndpointSecurityForAPIProduct(api);
        Assert.assertEquals("Username mismatch for production endpoint", "testuser",
                map.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION).getUsername());
        Assert.assertEquals("Password mismatch for production endpoint", "testpassword",
                map.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION).getPassword());
        Assert.assertEquals("Username mismatch for sandbox endpoint", "testuser",
                map.get(APIConstants.ENDPOINT_SECURITY_SANDBOX).getUsername());
        Assert.assertEquals("Password mismatch for sandbox endpoint", "testpassword",
                map.get(APIConstants.ENDPOINT_SECURITY_SANDBOX).getPassword());
        
    }
}
