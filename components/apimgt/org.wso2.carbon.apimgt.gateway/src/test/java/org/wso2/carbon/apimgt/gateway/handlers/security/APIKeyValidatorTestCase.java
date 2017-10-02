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
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

import javax.cache.Cache;
import java.util.ArrayList;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test class for APIKeyValidator
 */
public class APIKeyValidatorTestCase {

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

        Assert.assertEquals("", verbDTO, apiKeyValidator.getVerbInfoDTOFromAPIData(context, apiVersion, requestPath, httpMethod));

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
//                VerbInfoDTO verbInfoDTO = Mockito.mock(VerbInfoDTO.class);
                    if (cacheName.equals("resourceCache")) {
                        Mockito.when(cache.get("abc")).thenReturn(verbInfoDTO);
                    }
                    return cache;
                }

            }

            @Override
            protected ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion) throws APISecurityException {
                ArrayList<URITemplate> urlTemplates = new ArrayList<>();
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

            @Override
            protected String getTenantDomain() {
                return "carbon.super";
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
    }
}
