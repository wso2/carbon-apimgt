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

package org.wso2.carbon.apimgt.interceptor.handler;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.handlers.AbstractHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.core.APIManagerErrorConstants;
import org.wso2.carbon.apimgt.core.authenticate.APITokenValidator;
import org.wso2.carbon.apimgt.core.gateway.APITokenAuthenticator;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.interceptor.APIManagerInterceptorOps;
import org.wso2.carbon.apimgt.interceptor.UsageStatConfiguration;
import org.wso2.carbon.apimgt.interceptor.utils.APIManagerInterceptorConstant;
import org.wso2.carbon.apimgt.interceptor.utils.APIManagetInterceptorUtils;
import org.wso2.carbon.apimgt.interceptor.valve.APIFaultException;

/**
 * Axis2 handler to intercept all incoming requests for axis services and do
 * APIManagement. The required services should have a service level parameter
 * defined in it.
 * <parameter name="apiService" locked="true">true</parameter>
 * 
 */
public class APIManagerInterceptorHandler extends AbstractHandler {

	private static final Log log = LogFactory.getLog(APIManagerInterceptorHandler.class);
	
	public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
		boolean apiService = false;
		AxisService axisService = msgContext.getAxisService();

		if (axisService != null) {
			Parameter apiParam = axisService.getParameter(APIManagerInterceptorConstant.API_SERVICE_PARAMETER);
			if (apiParam != null && "true".equalsIgnoreCase(apiParam.getValue().toString())) {
				apiService = true;
			}

			if (apiService) {
				HttpServletRequest request =  (HttpServletRequest) msgContext.getProperty(APIManagerInterceptorConstant.HTTP_SERVLET_REQUEST);

				if (request != null) {
					String context = request.getContextPath();					
					if (context == null || context.equals("")) {
						return InvocationResponse.CONTINUE;
					}

					boolean contextExist;
					Boolean contextValueInCache = null;
					if (APIUtil.getAPIContextCache().get(context) != null) {
						contextValueInCache = Boolean.parseBoolean(APIUtil.getAPIContextCache()
						                                                  .get(context).toString());
					}

					if (contextValueInCache != null) {
						contextExist = contextValueInCache;
					} else {
						contextExist = ApiMgtDAO.isContextExist(context);
						APIUtil.getAPIContextCache().put(context, contextExist);
					}

					if (!contextExist) {
						return InvocationResponse.CONTINUE;
					}

					if (request.getMethod().equals(Constants.Configuration.HTTP_METHOD_GET)) {
						InvocationResponse res = handleWSDLGetRequest(request, context);
						if (res != null) {
							return res;
						}
					}

					long requestTime = System.currentTimeMillis();
					APIManagerInterceptorOps interceptorOps = new APIManagerInterceptorOps();
					if (contextExist) {						
						if (log.isDebugEnabled()) {
							log.debug("API Manager Interceptor Valve Got invoked!!");
						}
						String bearerToken = request.getHeader(APIConstants.OperationParameter.AUTH_PARAM_NAME);
						String accessToken = null;
					
						try {
							if (bearerToken != null) {
								accessToken =  APIManagetInterceptorUtils.getBearerToken(bearerToken);
							} else {
								// There can be some API published with None Auth Type
								/* throw new APIFaultException(APIConstants.KeyValidationStatus
								 * .API_AUTH_INVALID_CREDENTIALS, "Invalid format for Authorization header. Expected 'Bearer <token>'"
								 * );
								 */
							}
							APITokenAuthenticator authenticator = new APITokenAuthenticator();

							String apiVersion = APIManagetInterceptorUtils.getAPIVersion(request);							
							String domain = request.getHeader(APITokenValidator.getAPIManagerClientDomainHeader());
							String authLevel = authenticator.getResourceAuthenticationScheme(context,
                                                                           apiVersion,
                                                                           request.getRequestURI(),
                                                                           request.getMethod());
							if(authLevel == APIConstants.NO_MATCHING_AUTH_SCHEME){
								APIManagetInterceptorUtils.handleNoMatchAuthSchemeCallForAxisservice(msgContext, 
								                                request.getMethod(), request.getRequestURI(), apiVersion, context);
								return InvocationResponse.ABORT;
							}
							else{
								interceptorOps.doAuthenticate(context, apiVersion,accessToken, authLevel,domain);
							}
						} catch (APIManagementException e) {
							// ignore
						} catch (APIFaultException e) {
							APIManagetInterceptorUtils.handleAPIFaultForAxisservice(e, APIManagerErrorConstants.API_SECURITY_NS,
							               APIManagerErrorConstants.API_SECURITY_NS_PREFIX, msgContext);
							return InvocationResponse.ABORT;
						}
						
						try {
							interceptorOps.doThrottle(request, accessToken);
						} catch (APIFaultException e) {
							APIManagetInterceptorUtils.handleAPIFaultForAxisservice(e, APIManagerErrorConstants.API_THROTTLE_NS,
							               APIManagerErrorConstants.API_THROTTLE_NS_PREFIX, msgContext);						
							return InvocationResponse.ABORT;
						}
						UsageStatConfiguration statConfiguration = new UsageStatConfiguration();
						if (statConfiguration.isStatsPublishingEnabled()) {
							try {
								interceptorOps.publishStatistics(request, requestTime, false);
							}catch (APIManagementException e) {
								log.error("Error occured when publishing stats", e);
                            }
						}
					}					
				}
			}
		}
		return InvocationResponse.CONTINUE;
	}

	/**
	 * When we do GET call for WSDL/WADL, we do not want to
	 * authenticate/throttle the request.
	 * 
	 * @param request
	 * @param response
	 * @param compositeValve
	 * @param context
	 * @return
	 */
	private InvocationResponse handleWSDLGetRequest(HttpServletRequest request, String context) {
	
			// TODO:Need to get these paths from a config file.
			if (request.getRequestURI().matches(context + "/[^/]*/services")) {
				return InvocationResponse.CONTINUE;
			}
			Enumeration<String> params = request.getParameterNames();
			String paramName = null;
			while (params.hasMoreElements()) {
				paramName = params.nextElement();
				if (paramName.endsWith("wsdl") || paramName.endsWith("wadl")) {
					return InvocationResponse.CONTINUE;
				}
			}		
		return null;
	}	
	
}
