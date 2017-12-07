/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.axis2.Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.RESTDispatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.APIKeyDataStore;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.WSAPIKeyDataStore;
import org.wso2.carbon.apimgt.gateway.handlers.security.thrift.ThriftAPIDataStore;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.caching.impl.Util;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test class for APIKeyValidator
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PrivilegedCarbonContext.class, APISecurityUtils.class, ServiceReferenceHolder.class,
		ServerConfiguration.class, APIUtil.class, Util.class, CarbonContext.class, CarbonConstants.class,
        Caching.class, APIKeyValidator.class, RESTUtils.class })
public class APIKeyValidatorTestCase {
    private APIManagerConfiguration apiManagerConfiguration;
    private ServerConfiguration serverConfiguration;
    private long defaultCacheTimeout = 54000;
    private PrivilegedCarbonContext privilegedCarbonContext;

    @Before
    public void setup() {
        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(CarbonConstants.class);
        PowerMockito.mockStatic(ServerConfiguration.class);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(CarbonContext.class);
        PowerMockito.mockStatic(Util.class);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Util.getTenantDomain()).thenReturn("carbon.super");
        serverConfiguration = Mockito.mock(ServerConfiguration.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServerConfiguration.getInstance()).thenReturn(serverConfiguration);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        PowerMockito.mockStatic(APISecurityUtils.class);
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Cache cache = Mockito.mock(Cache.class);
        PowerMockito.when(APIUtil.getCache(APIConstants.API_MANAGER_CACHE_MANAGER, APIConstants.GATEWAY_TOKEN_CACHE_NAME,
                defaultCacheTimeout, defaultCacheTimeout)).thenReturn(cache);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED)).thenReturn("true");
        Mockito.when(serverConfiguration.getFirstProperty(APIConstants.DEFAULT_CACHE_TIMEOUT)).thenReturn("900");

        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

    }

    /*
    *  This method will test for findMatchingVerb()
    * */
    @Test
    public void testFindMatchingVerb() {
        MessageContext synCtx = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY)).thenReturn(null);
        Mockito.when(synCtx.getProperty(APIConstants.API_RESOURCE_CACHE_KEY)).thenReturn("abc");
        Mockito.when(synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH)).thenReturn("abc");
        Mockito.when(synCtx.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("");
        Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("abc");
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("https");
        Mockito.when(((Axis2MessageContext) synCtx).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        SynapseConfiguration synapseConfiguration = Mockito.mock(SynapseConfiguration.class);
        Mockito.when(synapseConfiguration.getAPI("abc")).thenReturn(new API("abc", "/"));
        Mockito.when(synCtx.getConfiguration()).thenReturn(synapseConfiguration);
        Mockito.when(synCtx.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("https");
        APIKeyValidator apiKeyValidator = createAPIKeyValidator(true);

        try {
            //Test for ResourceNotFoundexception
            assertNotNull(apiKeyValidator.findMatchingVerb(synCtx));
//        todo    Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY)).thenReturn("url");

        } catch (ResourceNotFoundException e) {
            assert true;
        } catch (APISecurityException e) {
            fail("APISecurityException is thrown " + e);

        }
        APIKeyValidator apiKeyValidator1 = createAPIKeyValidator(false);

        Resource resource = Mockito.mock(Resource.class);
        API api = new API("abc", "/");
        Mockito.when(synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/menu");

        api.addResource(resource);
        Mockito.when(synapseConfiguration.getAPI("abc")).thenReturn((api));

        try {
            VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
            verbInfoDTO.setHttpVerb("get");
            //Test for matching verb is found path
            assertEquals("", verbInfoDTO, apiKeyValidator1.findMatchingVerb(synCtx));
        } catch (ResourceNotFoundException e) {
            fail("ResourceNotFoundException exception is thrown " + e);
        } catch (APISecurityException e) {
            fail("APISecurityException is thrown " + e);
        }

        try {
            //Test for matching verb is Not found path
            Mockito.when(synCtx.getProperty(APIConstants.API_RESOURCE_CACHE_KEY)).thenReturn("xyz");

            assertNull(apiKeyValidator.findMatchingVerb(synCtx));
        } catch (ResourceNotFoundException e) {
            fail("ResourceNotFoundException exception is thrown " + e);
        } catch (APISecurityException e) {
            fail("APISecurityException is thrown " + e);
        }
    }

    
  
    @Test
    public void testFindMatchingVerbWithValidResources() throws Exception {
        MessageContext synCtx = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY)).thenReturn(null);
        Mockito.when(synCtx.getProperty(APIConstants.API_RESOURCE_CACHE_KEY)).thenReturn("abc");
        Mockito.when(synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH)).thenReturn("");
        Mockito.when(synCtx.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("");
        Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("abc");
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(((Axis2MessageContext) synCtx).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Resource resource = Mockito.mock(Resource.class);
      
        SynapseConfiguration synapseConfiguration = Mockito.mock(SynapseConfiguration.class);
        Mockito.when(synCtx.getConfiguration()).thenReturn(synapseConfiguration);
        API api2 = Mockito.mock(API.class);
        PowerMockito.whenNew(API.class).withArguments("abc", "/").thenReturn(api2);        		
        Mockito.when(synapseConfiguration.getAPI("abc")).thenReturn(api2);
        Resource resource1 = Mockito.mock(Resource.class);
        
        Mockito.when(resource1.getMethods()).thenReturn(new String[]{"GET"});
        Resource[] resourceArray = new Resource[1];
        resourceArray[0] = resource1;
        //Mockito.when(resourceArray[0]).thenReturn(resource1);
        
		Mockito.when(api2.getResources()).thenReturn(resourceArray);

        Mockito.when(synCtx.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");

        DispatcherHelper helper = Mockito.mock(DispatcherHelper.class);
        Mockito.when(resource1.getDispatcherHelper()).thenReturn(helper);
        Mockito.when(helper.getString()).thenReturn("/test");
        


        APIKeyValidator apiKeyValidator = createAPIKeyValidator(true);

        try {
            //Test for ResourceNotFoundexception
            assertNotNull(apiKeyValidator.findMatchingVerb(synCtx));
//        todo    Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY)).thenReturn("url");

        } catch (ResourceNotFoundException e) {
            assert true;
        } catch (APISecurityException e) {
            fail("APISecurityException is thrown " + e);

        }
        APIKeyValidator apiKeyValidator1 = createAPIKeyValidator(false);

        
        API api = new API("abc", "/");
        Mockito.when(synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/menu");

        api.addResource(resource);
        Mockito.when(synapseConfiguration.getAPI("abc")).thenReturn((api));

        try {
            VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
            verbInfoDTO.setHttpVerb("get");
            //Test for matching verb is found path
            assertEquals("", verbInfoDTO, apiKeyValidator1.findMatchingVerb(synCtx));
        } catch (ResourceNotFoundException e) {
            fail("ResourceNotFoundException exception is thrown " + e);
        } catch (APISecurityException e) {
            fail("APISecurityException is thrown " + e);
        }

        try {
            //Test for matching verb is Not found path
            Mockito.when(synCtx.getProperty(APIConstants.API_RESOURCE_CACHE_KEY)).thenReturn("xyz");

            assertNull(apiKeyValidator.findMatchingVerb(synCtx));
        } catch (ResourceNotFoundException e) {
            fail("ResourceNotFoundException exception is thrown " + e);
        } catch (APISecurityException e) {
            fail("APISecurityException is thrown " + e);
        }
    }
    
    /*
    * Test method for getVerbInfoDTOFromAPIData()
    * */
    @Test
    public void testGetVerbInfoDTOFromAPIData() throws Exception {
        String context = "/";
        String apiVersion = "1.0";
        String requestPath = "/";
        String httpMethod = "https";

        VerbInfoDTO verbDTO = new VerbInfoDTO();
        verbDTO.setHttpVerb("https");
        verbDTO.setRequestKey("//1.0/:https");
        APIKeyValidator apiKeyValidator = createAPIKeyValidator(true);
        //If isAPIResourceValidationEnabled==true
        apiKeyValidator.setGatewayAPIResourceValidationEnabled(true);
        Assert.assertEquals("", verbDTO, apiKeyValidator.getVerbInfoDTOFromAPIData(context, apiVersion, requestPath, httpMethod));

    }
    
    @Test
    public void testGetVerbInfoDTOFromAPIDataWithRequestPath() throws Exception {
        String context = "/";
        String apiVersion = "1.0";
        String requestPath = "/test";
        String httpMethod = "https";

        VerbInfoDTO verbDTO = new VerbInfoDTO();
        verbDTO.setHttpVerb("https");
        verbDTO.setRequestKey("//1.0/:https");
        APIKeyValidator apiKeyValidator = createAPIKeyValidator(true);
        // If isAPIResourceValidationEnabled==true
        apiKeyValidator.setGatewayAPIResourceValidationEnabled(true);
        Assert.assertEquals("", verbDTO,
                apiKeyValidator.getVerbInfoDTOFromAPIData(context, apiVersion, requestPath, httpMethod));

    }

    @Test
    public void testGetVerbInfoDTOFromAPIDataWithInvalidRequestPath() throws Exception {
        String context = "/";
        String apiVersion = "1.0";
        String requestPath = "";
        String httpMethod = "https";
        APIKeyValidator apiKeyValidator = createAPIKeyValidator(true);
        // If isAPIResourceValidationEnabled==true
        apiKeyValidator.setGatewayAPIResourceValidationEnabled(true);
        Assert.assertEquals("", null,
                apiKeyValidator.getVerbInfoDTOFromAPIData(context, apiVersion, requestPath, httpMethod));

    }
    
    @Test
    public void testGetResourceAuthenticationScheme() {

        MessageContext synCtx = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY)).thenReturn(null);
        Mockito.when(synCtx.getProperty(APIConstants.API_RESOURCE_CACHE_KEY)).thenReturn("abc");
        Mockito.when(synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH)).thenReturn("abc");
        Mockito.when(synCtx.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("");
        Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("abc");
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("https");
        Mockito.when(((Axis2MessageContext) synCtx).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        SynapseConfiguration synapseConfiguration = Mockito.mock(SynapseConfiguration.class);
        Mockito.when(synapseConfiguration.getAPI("abc")).thenReturn(new API("abc", "/"));
        Mockito.when(synCtx.getConfiguration()).thenReturn(synapseConfiguration);
        Mockito.when(synCtx.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("https");
        APIKeyValidator apiKeyValidator = createAPIKeyValidator(true);
        //test for ResourceNotFoundException path
        try {
            String result = apiKeyValidator.getResourceAuthenticationScheme(synCtx);
            Assert.assertEquals("noMatchedAuthScheme", result);
        } catch (APISecurityException e) {
            e.printStackTrace();
        }

        APIKeyValidator apiKeyValidator1 = createAPIKeyValidator(false);

        Resource resource = Mockito.mock(Resource.class);
        API api = new API("abc", "/");
        Mockito.when(synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/menu");

        api.addResource(resource);
        Mockito.when(synapseConfiguration.getAPI("abc")).thenReturn((api));

        String result1 = null;
        try {
            result1 = apiKeyValidator1.getResourceAuthenticationScheme(synCtx);
        } catch (APISecurityException e) {
            e.printStackTrace();
        }
        Assert.assertEquals("None", result1);


    }

    /*
    * This method will create an instance of APIKeyValidator
    * */
    private APIKeyValidator createAPIKeyValidator(final boolean isWithEmptyCache) {
        AxisConfiguration axisConfig = Mockito.mock(AxisConfiguration.class);
        return new APIKeyValidator(axisConfig) {
            @Override
            protected String getKeyValidatorClientType() {
                return "thriftClient";
            }

            @Override
            protected APIManagerConfiguration getApiManagerConfiguration() {
                APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
                Mockito.when(configuration.getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY)).thenReturn("900");
                Mockito.when(configuration.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED)).thenReturn("true");
                return configuration;
            }

            @Override
            protected Cache getCache(String cacheManagerName, String cacheName, long modifiedExp, long accessExp) {
                return Mockito.mock(Cache.class);
            }

            @Override
            protected long getDefaultCacheTimeout() {
                return 900L;
            }

            @Override
            protected Cache getCacheFromCacheManager(String cacheName) {
                if (isWithEmptyCache) {
                    return Mockito.mock(Cache.class);
                } else {
                    Cache cache = Mockito.mock(Cache.class);
                    VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
                    verbInfoDTO.setHttpVerb("get");
                    verbInfoDTO.setAuthType("None");
                    if (cacheName.equals("resourceCache")) {
                        Mockito.when(cache.get("abc")).thenReturn(verbInfoDTO);
                    } else if (cacheName.equals("GATEWAY_TOKEN_CACHE")) {
                        Mockito.when(cache.get("abc")).thenReturn("token");
                    }
                    return cache;
                }

            }

            @Override
            protected ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion) throws APISecurityException {
                ArrayList<URITemplate> urlTemplates = new ArrayList<URITemplate>();
                URITemplate template = new URITemplate();
                template.setUriTemplate("/*");
                template.setHTTPVerb("https");
                urlTemplates.add(template);
                return urlTemplates;
            }

            @Override
            protected APIKeyValidationInfoDTO doGetKeyValidationInfo(String context, String apiVersion, String apiKey, String authenticationScheme, String clientDomain, String matchingResource, String httpVerb) throws APISecurityException {
                APIKeyValidationInfoDTO apiKeyValidationInfoDTO = Mockito.mock(APIKeyValidationInfoDTO.class);
                Mockito.when(apiKeyValidationInfoDTO.getApiName()).thenReturn(apiKey);
                return apiKeyValidationInfoDTO;
            }

        };
    }

    /*
    * Test method fpr getKeyValidationInfo()
    * */
    @Test
    public void testGetKeyValidationInfo() throws Exception {
        String context = "/";
        String apiKey = "abc";
        String apiVersion = "1.0";
        String authenticationScheme = "";
        String clientDomain = "abc.com";
        String matchingResource = "/menu";
        String httpVerb = "get";
        boolean defaultVersionInvoked = true;
        APIKeyValidator apiKeyValidator = createAPIKeyValidator(false);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName(apiKey);

        Assert.assertEquals(apiKeyValidationInfoDTO.getApiName(), apiKeyValidator.getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme,
                clientDomain, matchingResource, httpVerb, defaultVersionInvoked).getApiName());

        // Test for token cache is found in token cache
        AxisConfiguration axisConfig = Mockito.mock(AxisConfiguration.class);
        APIKeyValidator newApiKeyValidator = new APIKeyValidator(axisConfig) {
            @Override
            protected String getTenantDomain() {
                return "zyx";
            }

            @Override
            protected String getKeyValidatorClientType() {
                return "thriftClient";
            }

            @Override
            protected APIManagerConfiguration getApiManagerConfiguration() {
                APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
                Mockito.when(configuration.getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY)).thenReturn("900");
                Mockito.when(configuration.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED)).thenReturn("true");
                return configuration;
            }

            @Override
            protected Cache getCache(String cacheManagerName, String cacheName, long modifiedExp, long accessExp) {
                return Mockito.mock(Cache.class);
            }

            @Override
            protected Cache getCacheFromCacheManager(String cacheName) {
                Cache cache = Mockito.mock(Cache.class);
                VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
                verbInfoDTO.setHttpVerb("get");
                verbInfoDTO.setAuthType("None");
                if (cacheName.equals("resourceCache")) {
                    Mockito.when(cache.get("abc")).thenReturn(verbInfoDTO);
                } else if (cacheName.equals("GATEWAY_TOKEN_CACHE")) {
                    Mockito.when(cache.get("abc")).thenReturn("token");
                }
                return cache;
            }

            @Override
            protected APIKeyValidationInfoDTO doGetKeyValidationInfo(String context, String apiVersion, String apiKey, String authenticationScheme, String clientDomain, String matchingResource, String httpVerb) throws APISecurityException {
                APIKeyValidationInfoDTO apiKeyValidationInfoDTO = Mockito.mock(APIKeyValidationInfoDTO.class);
                Mockito.when(apiKeyValidationInfoDTO.getApiName()).thenReturn(apiKey);
                return apiKeyValidationInfoDTO;
            }
        };
        Assert.assertEquals(apiKeyValidationInfoDTO.getApiName(), newApiKeyValidator.getKeyValidationInfo(context, apiKey, apiVersion, authenticationScheme,
                clientDomain, matchingResource, httpVerb, defaultVersionInvoked).getApiName());

    }

    @Test
    public void testIsAPIResourceValidationEnabled() {
        // test for exception path
        APIKeyValidator apiKeyValidator = createAPIKeyValidator(true);
        Assert.assertFalse(apiKeyValidator.isAPIResourceValidationEnabled());

    }

    @Test
    public void testGetApiManagerConfiguration() {
        AxisConfiguration axisConfig = Mockito.mock(AxisConfiguration.class);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        APIKeyValidator apiKeyValidator = new APIKeyValidator(axisConfig) {
        };

        Assert.assertNotNull(apiKeyValidator.getApiManagerConfiguration());

    }

    @Test
    public void testGetKeyValidatorClientType() {
        AxisConfiguration axisConfig = Mockito.mock(AxisConfiguration.class);
        APIKeyValidator apiKeyValidator = new APIKeyValidator(axisConfig) {
        };
        apiKeyValidator.getKeyValidatorClientType();

    }
    
	@Test
	public void testDatasourceConfigurationAndCleanup() throws Exception {		
		 
        AxisConfiguration axisConfig = Mockito.mock(AxisConfiguration.class);
        WSAPIKeyDataStore wsDataStore = Mockito.mock(WSAPIKeyDataStore.class);
        ThriftAPIDataStore thriftDataStore = Mockito.mock(ThriftAPIDataStore.class);
        PowerMockito.whenNew(WSAPIKeyDataStore.class).withNoArguments().thenReturn(wsDataStore);
        PowerMockito.whenNew(ThriftAPIDataStore.class).withNoArguments().thenReturn(thriftDataStore);
        
        
        APIKeyValidator wsKeyValidator = new APIKeyValidator(axisConfig) {
            @Override
            protected String getKeyValidatorClientType() {
                return "WSClient";
            }
        };

        APIKeyValidator thriftKeyValidator = new APIKeyValidator(axisConfig) {
            @Override
            protected String getKeyValidatorClientType() {
                return "ThriftClient";
            }
        };

        // test cleanup for WSClient
        wsKeyValidator.cleanup();
        Mockito.verify(wsDataStore, Mockito.times(1)).cleanup();
        Mockito.verify(thriftDataStore, Mockito.times(0)).cleanup();
        
        //test cleanup for ThriftClient
        thriftKeyValidator.cleanup();
        Mockito.verify(thriftDataStore, Mockito.times(1)).cleanup();       
	}        
}
