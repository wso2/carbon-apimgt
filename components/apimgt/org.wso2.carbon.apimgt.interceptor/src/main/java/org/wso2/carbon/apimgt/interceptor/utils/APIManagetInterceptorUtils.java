/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.interceptor.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.APIManagerErrorConstants;
import org.wso2.carbon.apimgt.interceptor.valve.APIFaultException;

/**
 * Util class
 * 
 */
public class APIManagetInterceptorUtils {

	private static final Log log = LogFactory.getLog(APIManagetInterceptorUtils.class);

	/**
	 * Generate the Error Payload
	 * 
	 * @param exception
	 *            APIFaultException
	 * @param FaultNS
	 * @param FaultNSPrefix
	 * @return
	 */
	public static OMElement getFaultPayload(APIFaultException exception, String FaultNS,
	                                        String FaultNSPrefix) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(FaultNS, FaultNSPrefix);
		OMElement payload = fac.createOMElement("fault", ns);

		OMElement errorCode = fac.createOMElement("code", ns);
		errorCode.setText(String.valueOf(exception.getErrorCode()));
		OMElement errorMessage = fac.createOMElement("message", ns);
		errorMessage.setText(APIManagerErrorConstants.getFailureMessage(exception.getErrorCode()));
		OMElement errorDetail = fac.createOMElement("description", ns);
		errorDetail.setText(exception.getMessage());

		payload.addChild(errorCode);
		payload.addChild(errorMessage);
		payload.addChild(errorDetail);
		return payload;
	}

	/**
	 * Get the version of the API
	 * 
	 * @param request
	 *            -Httpservelet request
	 * @return
	 */
	public static String getAPIVersion(HttpServletRequest request) {
		int contextStartsIndex = (request.getRequestURI()).indexOf(request.getContextPath()) + 1;
		int length = request.getContextPath().length();
		String afterContext = (request.getRequestURI()).substring(contextStartsIndex + length);
		int SlashIndex = afterContext.indexOf(("/"));

		if (SlashIndex != -1) {
			return afterContext.substring(0, SlashIndex);
		} else {
			return afterContext;
		}
	}

	/**
	 * Send an Error Response in application/xml content type
	 * 
	 * @param response
	 * @param payload
	 */
	public static void handleRestFailure(Response response, String payload) {
		response.setStatus(403);
		response.setContentType("application/xml");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().write(payload);
		} catch (IOException e) {
			log.error("Error in sending fault response", e);
		}
	}

	/**
	 * Send an Error Response
	 * 
	 * @param msgContext
	 * @param payload
	 */
	public static MessageContext getFaultMessagecontext(MessageContext msgContext, String payload) {
		try {
			MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(msgContext, new AxisFault(payload));
			faultContext.setProperty("ContentType","application/xml");
			return faultContext;
        } catch (AxisFault e) {
        	log.error("Error in sending fault response", e);
        } 
		return msgContext;		
	}
	
	/**
	 * Handle APIfaults for axis services
	 * @param e
	 * @param namespace
	 * @param namespaceprefix
	 * @param msgContext
	 * @return
	 * @throws AxisFault
	 */
	public static void handleAPIFaultForAxisservice(APIFaultException e, String namespace,
	                                          String namespaceprefix, MessageContext msgContext)throws AxisFault {

		String faultPayload = getFaultPayload(e, namespace, namespaceprefix).toString();
		MessageContext faultContext = getFaultMessagecontext(msgContext,faultPayload);
		AxisEngine.sendFault(faultContext);
		
	}
	
	/**
	 * Handle APIfaults for rest services
	 * @param e
	 * @param namespace
	 * @param namespaceprefix
	 * @param response
	 * @throws AxisFault
	 */
	public static void handleAPIFaultForRestService(APIFaultException e, String namespace,
	  	                                          String namespaceprefix, Response response) {
		String faultPayload = getFaultPayload(e,namespace,namespaceprefix).toString();
		handleRestFailure(response, faultPayload);	  		
	 }
	  
	/**
	 * Handle NomatchedAuthScheme for axis services. In the APImanager it is
	 * handled at synapse level.
	 * We need to introduce this at interceptor layer for our APImanagement
	 * capability in the platform.
	 * eg: HTTP verb blocks.
	 * 
	 * @param msgContext
	 * @param httpVerb
	 * @param reqUri
	 * @param version
	 * @param context
	 * @throws AxisFault
	 */
	public static void handleNoMatchAuthSchemeCallForAxisservice(MessageContext msgContext,
	                                                             String httpVerb, String reqUri,
	                                                             String version, String context) throws AxisFault {
		String errrMsg = "Resource is not matched for HTTP Verb " + httpVerb + ". API context " +
		                         context + ",version " + version + ", request " + reqUri;
		APIFaultException e = new APIFaultException( APIManagerErrorConstants.API_AUTH_INCORRECT_API_RESOURCE,
		                                            errrMsg);
		String faultPayload = getFaultPayload(e, APIManagerErrorConstants.API_SECURITY_NS,
		                                      APIManagerErrorConstants.API_SECURITY_NS_PREFIX).toString();
		MessageContext faultContext = getFaultMessagecontext(msgContext, faultPayload);
		AxisEngine.sendFault(faultContext);
	}

	/**
	 * Handle NomatchedAuthScheme for rest services
	 * @param response : Response
	 * @param httpVerb
	 * @param reqUri
	 * @param version
	 * @param context
	 * 
	 */
	public static void handleNoMatchAuthSchemeCallForRestService(Response response,String httpVerb, String reqUri,
		                                                             String version, String context ) {
		String errrMsg = "Resource is not matched for HTTP Verb " + httpVerb + ". API context " +
                context + ",version " + version + ", request " + reqUri;
		APIFaultException e = new APIFaultException( APIManagerErrorConstants.API_AUTH_INCORRECT_API_RESOURCE,
                                   errrMsg);
		String faultPayload = getFaultPayload(e, APIManagerErrorConstants.API_SECURITY_NS,
		                                      APIManagerErrorConstants.API_SECURITY_NS_PREFIX).toString();
		handleRestFailure(response, faultPayload);	  		
	 }
	/**
	 * Get bearer token form the HTTP header
	 * @param bearerToken
	 */
	public static String getBearerToken(String bearerToken) {
		String accessToken = null;
		String[] token = bearerToken.split("Bearer");
		if (token.length > 1 && token[1] != null) {
			accessToken = token[1].trim();
		}
		return accessToken;
	}

}
