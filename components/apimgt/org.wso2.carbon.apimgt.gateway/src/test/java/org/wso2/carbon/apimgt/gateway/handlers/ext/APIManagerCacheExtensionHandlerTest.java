/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.gateway.handlers.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

public class APIManagerCacheExtensionHandlerTest {
    private final String EXT_SEQUENCE_PREFIX = "WSO2AM--Ext--";
    private final String DIRECTION_OUT = "Out";
    private final String API_NAME = "_WSO2AMTokenAPI_";
    
    private final String SUPER_TENANT_REVOKED_TOKEN = UUID.randomUUID().toString();
    private final String WSO2_TENANT_REVOKED_TOKEN = UUID.randomUUID().toString();
    private final String NOT_CACHED_REVOKED_TOKEN = UUID.randomUUID().toString();
    private final String SUPER_TENANT_DEACTIVATED_TOKEN = UUID.randomUUID().toString();
    private final String WSO2_TENANT_DEACTIVATED_TOKEN = UUID.randomUUID().toString();
    private final String NOT_CACHED_DEACTIVATED_TOKEN = UUID.randomUUID().toString();
    
    private final String WSO2_TENANT_DOMAIN = "wso2.com";
    private final String SUPER_TENANT_DOMAIN = "carbon.super";
    
    private Map<String, String> cache = new HashMap<String, String>();


    @Test
    public void testSuperTenantBothTokenCacheWithGlobalAndCustomSeq(){
    	//populate cache
    	cache.put(SUPER_TENANT_REVOKED_TOKEN, SUPER_TENANT_DOMAIN);
    	cache.put(SUPER_TENANT_DEACTIVATED_TOKEN, SUPER_TENANT_DOMAIN);
       	cache.put(WSO2_TENANT_REVOKED_TOKEN, WSO2_TENANT_DOMAIN);
       	cache.put(WSO2_TENANT_DEACTIVATED_TOKEN, WSO2_TENANT_DOMAIN);    	
    	
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);
		
		//mock tokens
		TreeMap transportHeaders = Mockito.mock(TreeMap.class);
		Mockito.when((TreeMap) axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
				.thenReturn(transportHeaders);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.REVOKED_ACCESS_TOKEN))
				.thenReturn(SUPER_TENANT_REVOKED_TOKEN);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.DEACTIVATED_ACCESS_TOKEN))
		.thenReturn(SUPER_TENANT_DEACTIVATED_TOKEN);
		
		
		//custom sequences related configurations
		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);		

		SequenceMediator outSeq = Mockito.mock(SequenceMediator.class);
		SequenceMediator globalOutSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_OUT)).thenReturn(outSeq);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_OUT)).thenReturn(globalOutSeq);
		Mockito.when(((Mediator) outSeq).mediate(messageContext)).thenReturn(true);		
		//Global mediation returns a false to prevent any further mediation
		Mockito.when(((Mediator) globalOutSeq).mediate(messageContext)).thenReturn(false);
		
		

		APIManagerCacheExtensionHandler handler = new APIManagerCacheExtensionHandlerWrapper(cache);
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		//Test sequences related code
		// check whether custom out sequnce is not executed
		Mockito.verify(outSeq, Mockito.never()).mediate(messageContext);
		// check whether global out sequnce is executed once 
		Mockito.verify(globalOutSeq, Mockito.times(1)).mediate(messageContext);
		
		//Test cache entries
		Assert.assertNull("Revoked token exists in the cache", cache.get(SUPER_TENANT_REVOKED_TOKEN));
		Assert.assertNull("Deactivated token exists in the cache", cache.get(SUPER_TENANT_DEACTIVATED_TOKEN));
		Assert.assertFalse("Super tenant tenant flow is started  ",
				((APIManagerCacheExtensionHandlerWrapper) handler).isTenantFlowStarted());
		Assert.assertFalse("Super tenant tenant flow ended  ",
				((APIManagerCacheExtensionHandlerWrapper) handler).isTenantFlowFinished());
    	
    }
    
    @Test
    public void testTenantBothTokenCacheWithGlobalAndCustomSeq(){
    	//populate cache
    	cache.put(SUPER_TENANT_REVOKED_TOKEN, SUPER_TENANT_DOMAIN);
    	cache.put(SUPER_TENANT_DEACTIVATED_TOKEN, SUPER_TENANT_DOMAIN);
       	cache.put(WSO2_TENANT_REVOKED_TOKEN, WSO2_TENANT_DOMAIN);
       	cache.put(WSO2_TENANT_DEACTIVATED_TOKEN, WSO2_TENANT_DOMAIN); 
       	
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);
		
		//mock tokens
		TreeMap transportHeaders = Mockito.mock(TreeMap.class);
		Mockito.when((TreeMap) axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
				.thenReturn(transportHeaders);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.REVOKED_ACCESS_TOKEN))
				.thenReturn(WSO2_TENANT_REVOKED_TOKEN);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.DEACTIVATED_ACCESS_TOKEN))
		.thenReturn(WSO2_TENANT_DEACTIVATED_TOKEN);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);
		

		SequenceMediator outSeq = Mockito.mock(SequenceMediator.class);
		SequenceMediator globalOutSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_OUT)).thenReturn(outSeq);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_OUT)).thenReturn(globalOutSeq);
		Mockito.when(((Mediator) outSeq).mediate(messageContext)).thenReturn(true);
		
		//Global mediation returns a true to continue further sequence
		Mockito.when(((Mediator) globalOutSeq).mediate(messageContext)).thenReturn(true);

		APIManagerCacheExtensionHandler handler = new APIManagerCacheExtensionHandlerWrapper(cache);
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom out sequnce is executed
		Mockito.verify(outSeq, Mockito.times(1)).mediate(messageContext);
		// check whether global out sequnce is executed once 
		Mockito.verify(globalOutSeq, Mockito.times(1)).mediate(messageContext);
		
		//Test cache entries
		Assert.assertNull("Revoked token exists in the cache", cache.get(WSO2_TENANT_REVOKED_TOKEN));
		Assert.assertNull("Deactivated token exists in the cache", cache.get(WSO2_TENANT_DEACTIVATED_TOKEN));
		Assert.assertTrue("Tenant flow is not started  ",
				((APIManagerCacheExtensionHandlerWrapper) handler).isTenantFlowStarted());
		Assert.assertTrue("Tenant flow is not ended  ",
				((APIManagerCacheExtensionHandlerWrapper) handler).isTenantFlowFinished());
		
    }
    @Test
    public void testSuperTenantBothTokenCacheWithoutSeq(){
    	//populate cache
    	cache.put(SUPER_TENANT_REVOKED_TOKEN, SUPER_TENANT_DOMAIN);
    	cache.put(SUPER_TENANT_DEACTIVATED_TOKEN, SUPER_TENANT_DOMAIN);
       	cache.put(WSO2_TENANT_REVOKED_TOKEN, WSO2_TENANT_DOMAIN);
       	cache.put(WSO2_TENANT_DEACTIVATED_TOKEN, WSO2_TENANT_DOMAIN);    	
    	
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);
		
		//mock tokens
		TreeMap transportHeaders = Mockito.mock(TreeMap.class);
		Mockito.when((TreeMap) axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
				.thenReturn(transportHeaders);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.REVOKED_ACCESS_TOKEN))
				.thenReturn(SUPER_TENANT_REVOKED_TOKEN);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.DEACTIVATED_ACCESS_TOKEN))
		.thenReturn(SUPER_TENANT_DEACTIVATED_TOKEN);


		APIManagerCacheExtensionHandler handler = new APIManagerCacheExtensionHandlerWrapper(cache);
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		//Test cache entries
		Assert.assertNull("Revoked token exists in the cache", cache.get(SUPER_TENANT_REVOKED_TOKEN));
		Assert.assertNull("Deactivated token exists in the cache", cache.get(SUPER_TENANT_DEACTIVATED_TOKEN));
		Assert.assertFalse("Super tenant tenant flow is started  ",
				((APIManagerCacheExtensionHandlerWrapper) handler).isTenantFlowStarted());
		Assert.assertFalse("Super tenant tenant flow ended  ",
				((APIManagerCacheExtensionHandlerWrapper) handler).isTenantFlowFinished());
    	
    }
    @Test
    public void testTenantBothTokenCacheWithoutSeq(){
    	//populate cache
    	cache.put(SUPER_TENANT_REVOKED_TOKEN, SUPER_TENANT_DOMAIN);
    	cache.put(SUPER_TENANT_DEACTIVATED_TOKEN, SUPER_TENANT_DOMAIN);
       	cache.put(WSO2_TENANT_REVOKED_TOKEN, WSO2_TENANT_DOMAIN);
       	cache.put(WSO2_TENANT_DEACTIVATED_TOKEN, WSO2_TENANT_DOMAIN); 
       	
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);
		
		//mock tokens
		TreeMap transportHeaders = Mockito.mock(TreeMap.class);
		Mockito.when((TreeMap) axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
				.thenReturn(transportHeaders);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.REVOKED_ACCESS_TOKEN))
				.thenReturn(WSO2_TENANT_REVOKED_TOKEN);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.DEACTIVATED_ACCESS_TOKEN))
		.thenReturn(WSO2_TENANT_DEACTIVATED_TOKEN);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);
		



		APIManagerCacheExtensionHandler handler = new APIManagerCacheExtensionHandlerWrapper(cache);
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		
		//Test cache entries
		Assert.assertNull("Revoked token exists in the cache", cache.get(WSO2_TENANT_REVOKED_TOKEN));
		Assert.assertNull("Deactivated token exists in the cache", cache.get(WSO2_TENANT_DEACTIVATED_TOKEN));
		Assert.assertTrue("tenant tenant flow is not started  ",
				((APIManagerCacheExtensionHandlerWrapper) handler).isTenantFlowStarted());
		Assert.assertTrue("Super tenant tenant flow is not ended  ",
				((APIManagerCacheExtensionHandlerWrapper) handler).isTenantFlowFinished());
		
		Assert.assertTrue("Invalid response handling from cache handler ", handler.handleResponse(messageContext));
    	
    }
    
    @Test
    public void testTokenNotInCache(){

		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);
		
		//mock tokens
		TreeMap transportHeaders = Mockito.mock(TreeMap.class);
		Mockito.when((TreeMap) axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
				.thenReturn(transportHeaders);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.REVOKED_ACCESS_TOKEN))
				.thenReturn(NOT_CACHED_REVOKED_TOKEN);
		Mockito.when((String) transportHeaders.get(APIMgtGatewayConstants.DEACTIVATED_ACCESS_TOKEN))
		.thenReturn(NOT_CACHED_DEACTIVATED_TOKEN);

		APIManagerCacheExtensionHandler handler = new APIManagerCacheExtensionHandlerWrapper(cache);
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		
		//Test cache entries
		Assert.assertNull("Revoked token exists in the cache", cache.get(NOT_CACHED_REVOKED_TOKEN));
		Assert.assertNull("Deactivated token exists in the cache", cache.get(NOT_CACHED_DEACTIVATED_TOKEN));
		
    }
    
    @Test
    public void testTokenNotInTransportHeader(){  	
    	
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);
		
		//mock tokens
		TreeMap transportHeaders = Mockito.mock(TreeMap.class);
		Mockito.when((TreeMap) axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
				.thenReturn(transportHeaders);

		APIManagerCacheExtensionHandler handler = new APIManagerCacheExtensionHandlerWrapper(cache);
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		Assert.assertTrue(handler.handleResponse(messageContext));
		
    }
}
