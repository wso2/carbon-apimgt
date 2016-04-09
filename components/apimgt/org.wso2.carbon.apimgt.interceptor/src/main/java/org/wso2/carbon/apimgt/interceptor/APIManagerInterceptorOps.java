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
package org.wso2.carbon.apimgt.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.connector.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.core.APIManagerErrorConstants;
import org.wso2.carbon.apimgt.core.authenticate.APITokenValidator;
import org.wso2.carbon.apimgt.core.usage.APIStatsPublisher;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.interceptor.utils.APIManagetInterceptorUtils;
import org.wso2.carbon.apimgt.interceptor.valve.APIFaultException;
import org.wso2.carbon.apimgt.interceptor.valve.APIThrottleHandler;
import org.wso2.carbon.apimgt.interceptor.valve.internal.DataHolder;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

/**
 * APIManagement operations
 *
 */

public class APIManagerInterceptorOps {

	private APIKeyValidationInfoDTO apiKeyValidationDTO;
	
	
	private static final Log log = LogFactory.getLog(APIManagerInterceptorOps.class);

	public APIManagerInterceptorOps() {			
	}

	/**
	 * Authenticate the request
	 * 
	 * @param context
	 * @param version
	 * @param accessToken
	 * @param requiredAuthenticationLevel
	 * @return
	 * @throws APIManagementException
	 * @throws org.wso2.carbon.apimgt.interceptor.valve.APIFaultException
	 */
	public boolean doAuthenticate(String context, String version, String accessToken,
	                              String requiredAuthenticationLevel)
	                                                                                      throws APIManagementException,
	                                                                                      APIFaultException {

		if (APIConstants.AUTH_NO_AUTHENTICATION.equals(requiredAuthenticationLevel)) {
			return true;
		}
		APITokenValidator tokenValidator = new APITokenValidator();
		apiKeyValidationDTO = tokenValidator.validateKey(context, version, accessToken,
		                                                 requiredAuthenticationLevel);
		if (apiKeyValidationDTO.isAuthorized()) {
			String userName = apiKeyValidationDTO.getEndUserName();
			PrivilegedCarbonContext.getThreadLocalCarbonContext()
			                       .setUsername(apiKeyValidationDTO.getEndUserName());
			PrivilegedCarbonContext.getThreadLocalCarbonContext()
					.setTenantId(IdentityTenantUtil.getTenantIdOfUser(userName));

			return true;
		} else {
			throw new APIFaultException(apiKeyValidationDTO.getValidationStatus(),
					"Access failure for API: " + context + ", version: ");
		}
	}

	/**
	 * Throttle out the request
	 * 
	 * @param request
	 * @param accessToken
	 * @return
	 * @throws org.wso2.carbon.apimgt.interceptor.valve.APIFaultException
	 */
	public boolean doThrottle(Request request, String accessToken) throws APIFaultException {

		String apiName = request.getContextPath();
		String apiVersion = APIManagetInterceptorUtils.getAPIVersion(request);
		String apiIdentifier = apiName + "-" + apiVersion;

		APIThrottleHandler throttleHandler = null;
		ConfigurationContext cc = DataHolder.getServerConfigContext();

		if (cc.getProperty(apiIdentifier) == null) {
			throttleHandler = new APIThrottleHandler();
			/* Add the Throttle handler to ConfigContext against API Identifier */
			cc.setProperty(apiIdentifier, throttleHandler);
		} else {
			throttleHandler = (APIThrottleHandler) cc.getProperty(apiIdentifier);
		}

		if (throttleHandler.doThrottle(request, apiKeyValidationDTO, accessToken)) {
			return true;
		} else {
			throw new APIFaultException(APIManagerErrorConstants.API_THROTTLE_OUT,
			                            "You have exceeded your quota");
		}
	}

	/**
	 * 
	 * @param request -Httpservlet request
	 * @param accessToken
	 * @return
	 * @throws org.wso2.carbon.apimgt.interceptor.valve.APIFaultException
	 */
	public boolean doThrottle(HttpServletRequest request, String accessToken) throws APIFaultException {

		String apiName = request.getContextPath();
		String apiVersion = APIManagetInterceptorUtils.getAPIVersion(request);
		String apiIdentifier = apiName + "-" + apiVersion;

		APIThrottleHandler throttleHandler = null;
		ConfigurationContext cc = DataHolder.getServerConfigContext();

		if (cc.getProperty(apiIdentifier) == null) {
			throttleHandler = new APIThrottleHandler();
			/* Add the Throttle handler to ConfigContext against API Identifier */
			cc.setProperty(apiIdentifier, throttleHandler);
		} else {
			throttleHandler = (APIThrottleHandler) cc.getProperty(apiIdentifier);
		}

		if (throttleHandler.doThrottle(request, apiKeyValidationDTO, accessToken)) {
			return true;
		} else {
			throw new APIFaultException(APIManagerErrorConstants.API_THROTTLE_OUT,
			                            "You have exceeded your quota");
		}
	}

	/**
	 * Publish the request/response statistics
	 * 
	 * @param request
	 * @param requestTime
	 * @param response
	 *            : boolean
	 * @return
	 * @throws org.wso2.carbon.apimgt.interceptor.valve.APIFaultException
	 * @throws APIManagementException 
	 */
	public boolean publishStatistics(HttpServletRequest request, long requestTime, boolean response) throws APIManagementException {	
			
		UsageStatConfiguration statConf= new UsageStatConfiguration();
		APIMgtUsageDataPublisher publisher = statConf.getPublisher() ;
		if (publisher != null) {
			publisher.init();
			APIStatsPublisher statsPublisher =  new APIStatsPublisher(publisher,
			                                                         statConf.getHostName());
			if (response) {
				statsPublisher.publishResponseStatistics(apiKeyValidationDTO,
				                                         request.getRequestURI(),
				                                         request.getContextPath(),
				                                         request.getPathInfo(),
				                                         request.getMethod(), requestTime);
			} else {
				statsPublisher.publishRequestStatistics(apiKeyValidationDTO,
				                                        request.getRequestURI(),
				                                        request.getContextPath(),
				                                        request.getPathInfo(), request.getMethod(),
				                                        requestTime);
			}
			return true;
		}
		return false;
	}
}
