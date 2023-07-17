/*
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
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.axis2.Constants;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.api.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.api.Resource;
import org.apache.synapse.api.dispatch.DispatcherHelper;
import org.apache.synapse.api.dispatch.URLMappingHelper;
import org.apache.synapse.api.version.VersionStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.metrics.manager.Timer;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Test class for CORSRequestHandler
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, Utils.class, ServiceReferenceHolder.class})
public class CORSRequestHandlerTestCase {
    private APIManagerConfiguration apiManagerConfiguration;

    @Before
    public void setup() {
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(APIUtil.getAllowedMethods()).thenReturn("GET,PUT,POST,DELETE,PATCH,OPTIONS");
        PowerMockito.when(APIUtil.isAllowCredentials()).thenReturn(false);
        PowerMockito.when(APIUtil.getAllowedOrigins()).thenReturn("*");
        PowerMockito.when(APIUtil.getAllowedHeaders()).thenReturn("authorization,Access-Control-Allow-Origin,Content-Type,SOAPAction");
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
    }

    @Test
    public void testHandleRequest() throws Exception {
        PowerMockito.mockStatic(Utils.class);
        SynapseEnvironment synapseEnvironment = Mockito.mock(SynapseEnvironment.class);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/ishara");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin-AT-wso2.com--PizzaShackAPI");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        SynapseConfiguration synapseConfiguration = Mockito.mock(SynapseConfiguration.class);
        API api = Mockito.mock(API.class);
        Mockito.when(api.getContext()).thenReturn("/ishara");
        Resource resource = Mockito.mock(Resource.class);
        Resource[] resources = new Resource[1];
        resources[0] = resource;
        Mockito.when(api.getResources()).thenReturn(resources);
        VersionStrategy versionStrategy = Mockito.mock(VersionStrategy.class);
        Mockito.when(versionStrategy.getVersionType()).thenReturn("url");
        Mockito.when(versionStrategy.getVersion()).thenReturn("1.0");
        Mockito.when(api.getVersionStrategy()).thenReturn(versionStrategy);
        Mockito.when(Utils.getSelectedAPI(messageContext)).thenReturn(api);
        Mockito.when(synapseConfiguration.getAPI("admin-AT-wso2.com--PizzaShackAPI")).thenReturn(api);
        Mockito.when(messageContext.getConfiguration()).thenReturn(synapseConfiguration);

        CORSRequestHandler corsRequestHandler = createCORSRequestHandler();

        corsRequestHandler.init(synapseEnvironment);
        //test ResourceNotFound path
        Assert.assertFalse(corsRequestHandler.handleRequest(messageContext));

        //test for resource that is found
        String[] methods = {"GET", "POST"};
        Mockito.when(resource.getMethods()).thenReturn(methods);
        DispatcherHelper dispatcherHelper = new URLMappingHelper("/ishara/1.0") {
            @Override
            public String getString() {
                return "/xx";
            }
        };
        Mockito.when(resource.getDispatcherHelper()).thenReturn(dispatcherHelper);
        Mockito.when(messageContext.getProperty("REST_SUB_REQUEST_PATH")).thenReturn("/ishara/1.0");

        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put("Origin", "");

        Set<Resource> acceptableResources = new LinkedHashSet<>();
        acceptableResources.add(resource);
        String httpMethod = "GET";
        Mockito.when(Utils.getAcceptableResources(resources, httpMethod, null)).thenReturn(acceptableResources);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Assert.assertTrue(corsRequestHandler.handleRequest(messageContext));


        DispatcherHelper dispatcherHelper1 = new URLMappingHelper("/ishara/1.0") {
            @Override
            public String getString() {
                return "/xx";
            }
        };
        Mockito.when(resource.getDispatcherHelper()).thenReturn(dispatcherHelper1);

        httpMethod = "OPTIONS";
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn(httpMethod);
        //test for OPTIONS request when OPTIONS is not supported by SupportedHTTPVerbs
        Assert.assertFalse(corsRequestHandler.handleRequest(messageContext));
        //test for OPTIONS request when OPTIONS is supported by SupportedHTTPVerbs
        String[] methodsWithOptions = {"GET", "POST", "OPTIONS"};
        Mockito.when(Utils.getAcceptableResources(resources, httpMethod, null)).thenReturn(acceptableResources);
        Mockito.when(resource.getMethods()).thenReturn(methodsWithOptions);
        Assert.assertTrue(corsRequestHandler.handleRequest(messageContext));

    }

    @Test
    public void testHandleResponse() {
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        CORSRequestHandler corsRequestHandler = new CORSRequestHandler();
        Mediator mediator = Mockito.mock(Mediator.class);
        Mockito.when(messageContext.getSequence(APIConstants.CORS_SEQUENCE_NAME)).thenReturn(mediator);
        corsRequestHandler.handleResponse(messageContext);
    }

    @Test
    public void testGettersAndSetters() {
        CORSRequestHandler corsRequestHandler = new CORSRequestHandler();
        corsRequestHandler.setAllowCredentials("true");
        corsRequestHandler.setAllowedMethods("");
        corsRequestHandler.setAllowedMethods("");
        corsRequestHandler.setInline("");
        corsRequestHandler.setApiImplementationType("");
        corsRequestHandler.setAllowedOrigins("");
        corsRequestHandler.setAllowHeaders("");
        corsRequestHandler.getInline();
        corsRequestHandler.getInline();
        corsRequestHandler.getApiImplementationType();
        corsRequestHandler.destroy();

    }

    private CORSRequestHandler createCORSRequestHandler() {
        return new CORSRequestHandler() {

            @Override
            protected APIManagerConfigurationService getApiManagerConfigurationService() {
                return Mockito.mock(APIManagerConfigurationService.class);
            }

            @Override
            protected String getFullRequestPath(MessageContext messageContext) {
                return "/ishara/1.0/xx";
            }

            @Override
            protected Timer.Context startMetricTimer() {
                return null;
            }

            @Override
            protected void stopMetricTimer(Timer.Context context) {

            }

            @Override
            protected boolean isCorsEnabled() {
                return true;
            }

        };
    }

    /**
     * This method will test request flow when "isGraphqlSubscriptionRequest" property is set in axis2 message context
     * when incoming transport is websocket. This occurs during Graphql Subscription request flow.
     */
    @Test
    public void testHandleRequestForGraphQLSubscriptions() {
        Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(messageContext.getAxis2MessageContext()).thenReturn(axis2MessageContext);
        Mockito.when(axis2MessageContext.getIncomingTransportName()).thenReturn("ws");
        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(true);
        CORSRequestHandler corsRequestHandler = createCORSRequestHandler();
        Assert.assertTrue(corsRequestHandler.handleRequest(messageContext));

        Mockito.when(axis2MessageContext.getIncomingTransportName()).thenReturn("wss");
        Assert.assertTrue(corsRequestHandler.handleRequest(messageContext));
    }
}
