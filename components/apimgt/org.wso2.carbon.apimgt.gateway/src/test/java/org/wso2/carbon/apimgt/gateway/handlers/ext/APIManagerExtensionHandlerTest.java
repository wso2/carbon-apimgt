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

import java.util.Map;

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.metrics.manager.Timer;

public class APIManagerExtensionHandlerTest {
	private final String API_NAME = "TestAPI";
	private static final String EXT_SEQUENCE_PREFIX = "WSO2AM--Ext--";
	private static final String DIRECTION_IN = "In";
	private static final String DIRECTION_OUT = "Out";

	@Test
	public void testHandleRequestWithCustomInSeq() {

		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);

		SequenceMediator inSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_IN)).thenReturn(inSeq);
		Mockito.when(((Mediator) inSeq).mediate(messageContext)).thenReturn(true);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom in sequnce is only executed once
		Mockito.verify(inSeq, Mockito.times(1)).mediate(messageContext);
	}

	@Test
	public void testHandleRequestWithCustomOutSeq() {
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);

		SequenceMediator outSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_OUT)).thenReturn(outSeq);
		Mockito.when(((Mediator) outSeq).mediate(messageContext)).thenReturn(true);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom out sequnce is only executed once
		Mockito.verify(outSeq, Mockito.times(1)).mediate(messageContext);
	}

	@Test
	public void testHandleRequestWithCustomInOutSeq() {
		// Test for both in and out sequences
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);

		SequenceMediator outSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_OUT)).thenReturn(outSeq);
		SequenceMediator inSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_IN)).thenReturn(inSeq);
		Mockito.when(((Mediator) outSeq).mediate(messageContext)).thenReturn(true);
		Mockito.when(((Mediator) inSeq).mediate(messageContext)).thenReturn(true);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom out sequnce is only executed once
		Mockito.verify(outSeq, Mockito.times(1)).mediate(messageContext);
		// check whether custom in sequnce is only executed once
		Mockito.verify(inSeq, Mockito.times(1)).mediate(messageContext);
	}

	@Test
	public void testHandleRequestWithoutCustomSeq() {

		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_OUT)).thenReturn(null);


		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();

		Assert.assertTrue("Invalid request handling from extension handler ", handler.handleRequest(messageContext));
		Assert.assertTrue("Invalid response handling from extension handler ", handler.handleResponse(messageContext));

	}
	@Test
	public void testHandleRequestWithGlobalInSeq() {

		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);

		SequenceMediator globalInSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_IN)).thenReturn(globalInSeq);
		Mockito.when(((Mediator) globalInSeq).mediate(messageContext)).thenReturn(true);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom in sequnce is only executed once
		Mockito.verify(globalInSeq, Mockito.times(1)).mediate(messageContext);
	}

	@Test
	public void testHandleRequestWithGlobalOutSeq() {
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);


		SequenceMediator globalOutSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_OUT)).thenReturn(globalOutSeq);
		Mockito.when(((Mediator) globalOutSeq).mediate(messageContext)).thenReturn(true);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom out sequnce is only executed once
		Mockito.verify(globalOutSeq, Mockito.times(1)).mediate(messageContext);
	}

	@Test
	public void testHandleRequestWithGlobalInOutSeq() {
		// Test for both in and out sequences
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);

		SequenceMediator globalOutSeq = Mockito.mock(SequenceMediator.class);
		SequenceMediator globalInSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_IN)).thenReturn(globalInSeq);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_OUT)).thenReturn(globalOutSeq);
		Mockito.when(((Mediator) globalOutSeq).mediate(messageContext)).thenReturn(true);
		Mockito.when(((Mediator) globalInSeq).mediate(messageContext)).thenReturn(true);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom out sequnce is only executed once
		Mockito.verify(globalOutSeq, Mockito.times(1)).mediate(messageContext);
		// check whether custom in sequnce is only executed once
		Mockito.verify(globalInSeq, Mockito.times(1)).mediate(messageContext);
	}


	// Test when both global and custom out sequences are there and global sequence instructs to hault with the
	// the custom mediation execution. (global mediation seq returns a false after mediation)
	@Test
	public void testHandleRequestWithGlobalOutSeqWithoutFurtherMediationAndCustomOutSeq() {
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);
		

		SequenceMediator outSeq = Mockito.mock(SequenceMediator.class);
		SequenceMediator globalOutSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_OUT)).thenReturn(outSeq);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_OUT)).thenReturn(globalOutSeq);
		Mockito.when(((Mediator) outSeq).mediate(messageContext)).thenReturn(true);
		
		//Global mediation returns a false to prevent any further mediation
		Mockito.when(((Mediator) globalOutSeq).mediate(messageContext)).thenReturn(false);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom out sequnce is not executed
		Mockito.verify(outSeq, Mockito.never()).mediate(messageContext);
		// check whether global out sequnce is executed once 
		Mockito.verify(globalOutSeq, Mockito.times(1)).mediate(messageContext);

	}
	
	// Test when both global and custom out sequences are there and global sequence instructs to continue with the
	// the custom mediation as well. (global mediation seq returns a true after mediation)
	@Test
	public void testHandleRequestWithGlobalOutSeqWithFurtherMediationAndCustomOutSeq() {
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);
		

		SequenceMediator outSeq = Mockito.mock(SequenceMediator.class);
		SequenceMediator globalOutSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_OUT)).thenReturn(outSeq);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_OUT)).thenReturn(globalOutSeq);
		Mockito.when(((Mediator) outSeq).mediate(messageContext)).thenReturn(true);
		
		//Global mediation returns a true to continue further mediation
		Mockito.when(((Mediator) globalOutSeq).mediate(messageContext)).thenReturn(true);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom out sequnce is  executed once
		Mockito.verify(outSeq, Mockito.times(1)).mediate(messageContext);
		// check whether global out sequnce is executed once 
		Mockito.verify(globalOutSeq, Mockito.times(1)).mediate(messageContext);

	}	
	
	// Test when both global and custom in sequences are there and global sequence instructs to hault with the
	// the custom mediation execution. (global mediation seq returns a false after mediation)
	@Test
	public void testHandleRequestWithGlobalInSeqWithoutFurtherMediationAndCustomInSeq() {
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);
		

		SequenceMediator inSeq = Mockito.mock(SequenceMediator.class);
		SequenceMediator globalInSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_IN)).thenReturn(inSeq);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_IN)).thenReturn(globalInSeq);
		Mockito.when(((Mediator) inSeq).mediate(messageContext)).thenReturn(true);
		
		//Global mediation returns a false to prevent any further mediation
		Mockito.when(((Mediator) globalInSeq).mediate(messageContext)).thenReturn(false);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom in sequnce is not executed
		Mockito.verify(inSeq, Mockito.never()).mediate(messageContext);
		// check whether global in sequnce is executed once 
		Mockito.verify(globalInSeq, Mockito.times(1)).mediate(messageContext);

	}
	
	// Test when both global and custom in sequences are there and global sequence instructs to continue with the
	// the custom mediation as well. (global mediation seq returns a true after mediation)
	@Test
	public void testHandleRequestWithGlobalInSeqWithFurtherMediationAndCustomInSeq() {
		MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
		SynapseConfiguration synapseConfig = Mockito.mock(SynapseConfiguration.class);
		org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito
				.mock(org.apache.axis2.context.MessageContext.class);

		Map localRegistry = Mockito.mock(Map.class);

		Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
		Mockito.when(((Axis2MessageContext) messageContext).getConfiguration()).thenReturn(synapseConfig);
		Mockito.when(synapseConfig.getLocalRegistry()).thenReturn(localRegistry);

		Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn(API_NAME);
		

		SequenceMediator inSeq = Mockito.mock(SequenceMediator.class);
		SequenceMediator globalInSeq = Mockito.mock(SequenceMediator.class);
		Mockito.when(localRegistry.get(API_NAME + "--" + DIRECTION_IN)).thenReturn(inSeq);
		Mockito.when(localRegistry.get(EXT_SEQUENCE_PREFIX + DIRECTION_IN)).thenReturn(globalInSeq);
		Mockito.when(((Mediator) inSeq).mediate(messageContext)).thenReturn(true);		

		//Global mediation returns a true to continue further mediation
		Mockito.when(((Mediator) globalInSeq).mediate(messageContext)).thenReturn(true);

		APIManagerExtensionHandler handler = createAPIManagerExtensionHandler();
		// both methods are executed during a full request path
		handler.handleRequest(messageContext);
		handler.handleResponse(messageContext);

		// check whether custom in sequnce is executed once
		Mockito.verify(inSeq, Mockito.times(1)).mediate(messageContext);
		// check whether global in sequnce is executed once 
		Mockito.verify(globalInSeq, Mockito.times(1)).mediate(messageContext);

	}	




	private APIManagerExtensionHandler createAPIManagerExtensionHandler() {
		return new APIManagerExtensionHandler() {
			@Override
			protected void stopMetricTimer(Timer.Context context) {

			}

			@Override
			protected Timer.Context startMetricTimer(String direction) {
				return null;
			}

		};
	}

}
