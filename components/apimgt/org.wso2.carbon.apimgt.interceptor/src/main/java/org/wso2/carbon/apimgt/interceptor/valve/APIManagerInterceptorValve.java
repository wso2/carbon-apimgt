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

package org.wso2.carbon.apimgt.interceptor.valve;

import org.apache.axis2.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.core.APIManagerErrorConstants;
import org.wso2.carbon.apimgt.core.gateway.APITokenAuthenticator;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.interceptor.APIManagerInterceptorOps;
import org.wso2.carbon.apimgt.interceptor.UsageStatConfiguration;
import org.wso2.carbon.apimgt.interceptor.utils.APIManagetInterceptorUtils;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;

import java.util.Enumeration;

/**
 * APIManagerInterceptorValve is exposed as a CarbonTomatValve and it filters 
 * all the requests to published APIs and perform API Management for those APIs.
 *
 */
public class APIManagerInterceptorValve extends CarbonTomcatValve {
	
	private static final Log log = LogFactory.getLog(APIManagerInterceptorValve.class);
    //Cache contextCache = null;

    APITokenAuthenticator authenticator;

    public APIManagerInterceptorValve () {            
            //contextCache = APIUtil.getAPIContextCache();
            authenticator = new APITokenAuthenticator();     
    }

    public void invoke(Request request, Response response, CompositeValve compositeValve) {
        
        String context = request.getContextPath();
        if (context == null || context.equals("")) {
            //Invoke next valve in pipe.
            getNext().invoke(request, response, compositeValve);
            return;
        }

        boolean contextExist;
        Boolean contextValueInCache = null;
        if (APIUtil.getAPIContextCache().get(context) != null) {
            contextValueInCache = Boolean.parseBoolean(APIUtil.getAPIContextCache().get(context).toString());
        }

        if (contextValueInCache != null) {
            contextExist = contextValueInCache;
        } else {
            contextExist = ApiMgtDAO.getInstance().isContextExist(context);
            APIUtil.getAPIContextCache().put(context, contextExist);
        }

        if (!contextExist) {
            getNext().invoke(request, response, compositeValve);
            return;
        }

        handleWSDLGetRequest( request,  response,  compositeValve, context );
        
        long requestTime = System.currentTimeMillis();
        APIManagerInterceptorOps interceptorOps = new APIManagerInterceptorOps(); 
        UsageStatConfiguration statConfiguration = new UsageStatConfiguration();
        if (contextExist) {
        	//Use embedded API Management
			if (log.isDebugEnabled()) {
				log.debug("API Manager Interceptor Valve Got invoked!!");
			}
        	
            String bearerToken = request.getHeader(APIConstants.OperationParameter.AUTH_PARAM_NAME);
            String accessToken = null;

            /* Authenticate*/
            try {
				if (bearerToken != null) {
					accessToken = APIManagetInterceptorUtils.getBearerToken(bearerToken);
				} else {
					// There can be some API published with None Auth Type
					/*
					 * throw new
					 * APIFaultException(APIConstants.KeyValidationStatus
					 * .API_AUTH_INVALID_CREDENTIALS,
					 * "Invalid format for Authorization header. Expected 'Bearer <token>'"
					 * );
					 */
				}
                
            	String apiVersion = APIManagetInterceptorUtils.getAPIVersion(request);
				String authLevel = authenticator.getResourceAuthenticationScheme(context,
                                                               apiVersion,
                                                               request.getRequestURI(),
                                                               request.getMethod());
				if(authLevel == APIConstants.NO_MATCHING_AUTH_SCHEME){
					APIManagetInterceptorUtils.handleNoMatchAuthSchemeCallForRestService(response, 
					                               request.getMethod(), request.getRequestURI(),
					                               apiVersion, context);
					return;
				}
				else{
					interceptorOps.doAuthenticate(context, apiVersion, accessToken, authLevel);
				}
            } catch (APIManagementException e) {
                    //ignore
            } catch (APIFaultException e) {/* If !isAuthorized APIFaultException is thrown*/
            	APIManagetInterceptorUtils.handleAPIFaultForRestService(e, APIManagerErrorConstants.API_SECURITY_NS, 
            	                                                        APIManagerErrorConstants.API_SECURITY_NS_PREFIX,response);            	
		        return;
			}
            /* Throttle*/
            try {
            	interceptorOps.doThrottle(request, accessToken);
			} catch (APIFaultException e) {
				APIManagetInterceptorUtils.handleAPIFaultForRestService(e,
				                                                        APIManagerErrorConstants.API_THROTTLE_NS, 
				                                                        APIManagerErrorConstants.API_THROTTLE_NS_PREFIX, response);				
			    return;
			}
            /* Publish Statistic if enabled*/
            if (statConfiguration.isStatsPublishingEnabled()) {
            	try {
	                interceptorOps.publishStatistics(request, requestTime,false);
                }catch (APIManagementException e) {
                	 log.error("Error occured when publishing stats",e);
                }
            }
        }

        //Invoke next valve in pipe.
        getNext().invoke(request, response, compositeValve);

        //Handle Responses
        if (contextExist && statConfiguration.isStatsPublishingEnabled()) {
        	try {
	            interceptorOps.publishStatistics(request, requestTime, true);
            } catch (APIManagementException e) {
            	log.error("Error occured when publishing stats",e);
           }
        }
    }
 
	/**
	 * When we do GET call for WSDL/WADL, we do not want to
	 * authenticate/throttle the request.
	 * 
	 *TODO check logic
	 * @param request
	 * @param response
	 * @param compositeValve
	 * @param context
	 */
	private void handleWSDLGetRequest(Request request, Response response,
	                                  CompositeValve compositeValve, String context) {
		if (request.getMethod().equals(Constants.Configuration.HTTP_METHOD_GET)) {
			// TODO:Need to get these paths from a config file.
			if (request.getRequestURI().matches(context + "/[^/]*/services")) {
				getNext().invoke(request, response, compositeValve);
				return;
			}
			Enumeration<String> params = request.getParameterNames();
			String paramName = null;
			while (params.hasMoreElements()) {
				paramName = params.nextElement();
				if (paramName.endsWith("wsdl") || paramName.endsWith("wadl")) {
					getNext().invoke(request, response, compositeValve);
					return;
				}
			}
		}
	}
}
