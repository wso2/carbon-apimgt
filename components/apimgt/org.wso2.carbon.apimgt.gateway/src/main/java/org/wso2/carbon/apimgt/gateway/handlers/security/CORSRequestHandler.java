/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;

/**
 * This handler functions as an enabler for the Store API Console
 * Sets the Access-Control-Allow-Origin header
 *
 */
public class CORSRequestHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(CORSRequestHandler.class);

    public void init(SynapseEnvironment synapseEnvironment) {
        log.debug("Initializing API authentication handler instance");
    }

    public void destroy() {
        log.debug("Destroying API authentication handler instance");
    }

    public boolean handleRequest(MessageContext messageContext) {

        if (Utils.isCORSEnabled()) {
	    	/* For CORS support adding required headers to the response */
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext();
            Map<String, String> headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            String requestOrigin = (String) headers.get("Origin");

            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Utils.getAllowedOrigin(requestOrigin));
            //headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils.getAllowedMethods());
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils.getAllowedHeaders());
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }

        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {

    	if (Utils.isCORSEnabled()) {
	    	/* For CORS support adding required headers to the response */
	    	org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
	                getAxis2MessageContext();
	    	Map<String, String> headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            String requestOrigin = (String) headers.get("Origin");
	    		    	    	
	    	headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Utils.getAllowedOrigin(requestOrigin));
	        //headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils.getAllowedMethods());
	        headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils.getAllowedHeaders());
	        axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
    	}
 
        return true;
    }

}
