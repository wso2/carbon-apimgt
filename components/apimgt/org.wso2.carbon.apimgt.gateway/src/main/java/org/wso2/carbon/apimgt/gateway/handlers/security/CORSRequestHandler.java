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

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.*;
import org.apache.synapse.rest.dispatch.RESTDispatcher;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CORSRequestHandler extends AbstractHandler implements ManagedLifecycle {

	private static final Log log = LogFactory.getLog(CORSRequestHandler.class);
	private String apiImplementationType;
	private String allowHeaders;
	private String allowCredentials;
	private Set<String> allowedOrigins;
	private boolean initializeHeaderValues;
	private String allowedMethods;
	private boolean allowCredentialsEnabled;
	public void init(SynapseEnvironment synapseEnvironment) {
		if (log.isDebugEnabled()) {
			log.debug("Initializing CORSRequest Handler instance");
		}
		if (ServiceReferenceHolder.getInstance().getApiManagerConfigurationService() != null) {
			 initializeHeaders();
		}
	}

	/**
	 * This method used to Initialize  header values
	 *
	 * @return true after Initialize the values
	 */
	void initializeHeaders() {
		if (allowHeaders == null) {
			allowHeaders = APIUtil.getAllowedHeaders();
		}
		if (allowedOrigins == null) {
			String allowedOriginsList = APIUtil.getAllowedOrigins();
			if (!allowedOriginsList.isEmpty()) {
				allowedOrigins = new HashSet<String>(Arrays.asList(allowedOriginsList.split(",")));
			}
		}
		if (allowCredentials == null) {
			allowCredentialsEnabled = APIUtil.isAllowCredentials();
		}
		if (allowedMethods == null) {
			allowedMethods = APIUtil.getAllowedMethods();
		}

		initializeHeaderValues =  true;
	}

	public void destroy() {
		if (log.isDebugEnabled()) {
			log.debug("Destroying CORSRequest Handler instance");
		}
	}

	public boolean handleRequest(MessageContext messageContext) {

		long executionStartTime = System.currentTimeMillis();
		Timer timer = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName()));
        Timer.Context context = timer.start();

        boolean status;
        try {
            if (!initializeHeaderValues) {
                initializeHeaders();
            }
            String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
			String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
			String httpMethod = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                    getProperty(Constants.Configuration.HTTP_METHOD);
			API selectedApi = messageContext.getConfiguration().getAPI(apiName);
			Resource selectedResourceWithVerb = null;
            Resource selectedResource = null;
			String subPath = null;
            String path = RESTUtils.getFullRequestPath(messageContext);
			if(selectedApi != null) {
				if (VersionStrategyFactory.TYPE_URL.equals(selectedApi.getVersionStrategy().getVersionType())) {
					subPath = path.substring(
							selectedApi.getContext().length() + selectedApi.getVersionStrategy().getVersion().length() + 1);
				} else {
					subPath = path.substring(selectedApi.getContext().length());
				}
			}
            if ("".equals(subPath)) {
                subPath = "/";
            }
            messageContext.setProperty(RESTConstants.REST_SUB_REQUEST_PATH, subPath);

            if(selectedApi != null){
                Resource[] selectedAPIResources = selectedApi.getResources();
                if (selectedAPIResources.length > 0) {
                    for (RESTDispatcher dispatcher : RESTUtils.getDispatchers()) {
                        Resource resource = dispatcher.findResource(messageContext, Arrays.asList(selectedAPIResources));
                        if (resource != null) {
                            selectedResource = resource;
                            if (Arrays.asList(resource.getMethods()).contains(httpMethod)) {
                                selectedResourceWithVerb = resource;
                                break;
                            }
                        }
                    }
                }
            }

            String resourceString =
                    selectedResourceWithVerb != null ? selectedResourceWithVerb.getDispatcherHelper().getString() : null;
            String resourceCacheKey = APIUtil
                    .getResourceInfoDTOCacheKey(apiContext, apiVersion, resourceString, httpMethod);
            messageContext.setProperty(APIConstants.API_ELECTED_RESOURCE, resourceString);
            messageContext.setProperty(APIConstants.API_RESOURCE_CACHE_KEY, resourceCacheKey);
            setCORSHeaders(messageContext, selectedResourceWithVerb);
            if (selectedResource != null && selectedResourceWithVerb != null) {
                if (APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(apiImplementationType)) {
                    messageContext.getSequence(APIConstants.CORS_SEQUENCE_NAME).mediate(messageContext);
                }
                status = true;
            } else if (selectedResource != null) {
                if (APIConstants.SupportedHTTPVerbs.OPTIONS.name().equalsIgnoreCase(httpMethod)) {
	                Mediator corsSequence = messageContext.getSequence(APIConstants.CORS_SEQUENCE_NAME);
	                if (corsSequence != null) {
		                corsSequence.mediate(messageContext);
	                }
	                Utils.send(messageContext, HttpStatus.SC_OK);
                    status = false;
                } else {
					messageContext.setProperty(APIConstants.CUSTOM_HTTP_STATUS_CODE, HttpStatus.SC_METHOD_NOT_ALLOWED);
					messageContext.setProperty(APIConstants.CUSTOM_ERROR_CODE, HttpStatus.SC_METHOD_NOT_ALLOWED);
					messageContext.setProperty(APIConstants.CUSTOM_ERROR_MESSAGE, "Method not allowed for given API resource");
					Mediator resourceMisMatchedSequence = messageContext.getSequence(RESTConstants.NO_MATCHING_RESOURCE_HANDLER);
					if (resourceMisMatchedSequence != null) {
						resourceMisMatchedSequence.mediate(messageContext);
					}
					status = false;
				}
			} else {
                messageContext.setProperty(APIConstants.CUSTOM_HTTP_STATUS_CODE, HttpStatus.SC_NOT_FOUND);
                messageContext.setProperty(APIConstants.CUSTOM_ERROR_CODE, HttpStatus.SC_NOT_FOUND);
                messageContext.setProperty(APIConstants.CUSTOM_ERROR_MESSAGE, "No matching resource found for given API Request");
                Mediator resourceMisMatchedSequence = messageContext.getSequence(RESTConstants.NO_MATCHING_RESOURCE_HANDLER);
                if (resourceMisMatchedSequence != null) {
					resourceMisMatchedSequence.mediate(messageContext);
				}
				status = false;
			}
		} finally {
            context.stop();
        }
        return status;
    }

	public boolean handleResponse(MessageContext messageContext) {
		Mediator corsSequence = messageContext.getSequence(APIConstants.CORS_SEQUENCE_NAME);
		if (corsSequence != null) {
			corsSequence.mediate(messageContext);
		}
		return true;
	}

	/**
	 * This method used to set CORS headers into message context
	 *
	 * @param messageContext   message context for set cors headers as properties
	 * @param selectedResource resource according to the request
	 */
	public void setCORSHeaders(MessageContext messageContext, Resource selectedResource) {
		org.apache.axis2.context.MessageContext axis2MC =
				((Axis2MessageContext) messageContext).getAxis2MessageContext();
		Map<String, String> headers =
				(Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
		String requestOrigin = headers.get("Origin");
		String allowedOrigin = getAllowedOrigins(requestOrigin);

		//Set the access-Control-Allow-Credentials header in the response only if it is specified to true in the api-manager configuration
		//and the allowed origin is not the wildcard (*)
        if (allowCredentialsEnabled && !"*".equals(allowedOrigin)) {
            messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE);
        }

		messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
		String allowedMethods = "";
		StringBuffer allowedMethodsBuffer = new StringBuffer();
		if (selectedResource != null) {
			String[] methods = selectedResource.getMethods();
			for (String method : methods) {
				allowedMethodsBuffer.append(method).append(',');
				}
			allowedMethods = allowedMethodsBuffer.toString();
			if (methods.length != 0) {
				allowedMethods = allowedMethods.substring(0, allowedMethods.length() - 1);
			}
		} else {
			allowedMethods = this.allowedMethods;
		}
		if ("*".equals(allowHeaders)) {
			allowHeaders = headers.get("Access-Control-Request-Headers");

		}
		messageContext.setProperty(APIConstants.CORS_CONFIGURATION_ENABLED, APIUtil.isCORSEnabled());
		messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, allowedMethods);
		messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
	}


	public String getAllowHeaders() {
		return allowHeaders;
	}

	public void setAllowHeaders(String allowHeaders) {
		this.allowHeaders = allowHeaders;
	}

	public String getAllowedOrigins(String origin) {
		if (allowedOrigins.contains("*")) {
			return "*";
		} else if (allowedOrigins.contains(origin)) {
			return origin;
		} else {
			return null;
		}
	}

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = new HashSet<String>(Arrays.asList(allowedOrigins.split(",")));
    }

	public String getApiImplementationType() {
		return apiImplementationType;
	}

	public void setApiImplementationType(String apiImplementationType) {
		this.apiImplementationType = apiImplementationType;
	}

	// For backward compatibility with 1.9.0 since the property name is inline
	public String getInline() { return getApiImplementationType(); }

	// For backward compatibility with 1.9.0 since the property name is inline
	public void setInline(String inlineType) {
		setApiImplementationType(inlineType);
	}

	public String isAllowCredentials() {
		return allowCredentials;
	}

	public void setAllowCredentials(String allowCredentials) {
		this.allowCredentialsEnabled = Boolean.parseBoolean(allowCredentials);
		this.allowCredentials = allowCredentials;
	}

	public String getAllowedMethods() {
		return allowedMethods;
	}

	public void setAllowedMethods(String allowedMethods) {
		this.allowedMethods = allowedMethods;
	}
}
