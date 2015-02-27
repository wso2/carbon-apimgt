/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.synapse.transport.passthru.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.wso2.caching.CacheConfiguration;
import org.wso2.caching.CachingConstants;

public class CacheMessageBuilderDispatchandler extends AbstractDispatcher {

	private static final Log log = LogFactory.getLog(CacheMessageBuilderDispatchandler.class);
	
	public static final String NAME = "CacheMessageBuilderDispatchandler";

	@Override
	public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
		InvocationResponse invocationResponse = super.invoke(messageContext);

		EndpointReference toEPR = messageContext.getTo();

		Pipe pipe = (Pipe) messageContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);

		if (pipe != null && messageContext.getAxisMessage() != null) {
			CacheConfiguration cacheCfg = null;
			Parameter ccfgParam = messageContext.getAxisMessage().getParameter(CachingConstants.CACHE_CONFIGURATION);
			
	        
			if (ccfgParam != null && ccfgParam.getValue() instanceof CacheConfiguration) {

				cacheCfg = (CacheConfiguration) ccfgParam.getValue();
				 // even though we found a cache config, if the timeout is <= 0, caching is disabled
		        if (cacheCfg.getTimeout() <= 0) {
		        	return invocationResponse;
		        }
				try {
	                RelayUtils.buildMessage(messageContext, false);
                } catch (Exception e) {
                	log.error("Error while executing the message at cache message builder handler", e);
                }
			} 

		}

		return invocationResponse;
	}

	
	
	@Override
	public AxisOperation findOperation(AxisService arg0, MessageContext arg1) throws AxisFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AxisService findService(MessageContext arg0) throws AxisFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initDispatcher() {
		init(new HandlerDescription(NAME));
	}

}
