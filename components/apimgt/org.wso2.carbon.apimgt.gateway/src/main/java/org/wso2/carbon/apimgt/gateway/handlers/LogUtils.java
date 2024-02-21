/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.axis2.Constants;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.API;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.api.Resource;
import org.apache.synapse.api.dispatch.DispatcherHelper;
import org.apache.synapse.api.dispatch.RESTDispatcher;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Provides util methods for the LogsHandler
 */
class LogUtils {

    protected static String getAuthorizationHeader(Map headers) {
        return (String) headers.get(HttpHeaders.AUTHORIZATION);
    }

    protected static String getCorrelationHeader(Map headers) {
        return (String) headers.get(APIConstants.AM_ACTIVITY_ID);
    }

    protected static String getOrganizationIdHeader(Map headers) {
        return (String) headers.get("organization-id");
    }

    protected static String getSourceIdHeader(Map headers) {
        return (String) headers.get("source-id");
    }

    protected static String getApplicationIdHeader(Map headers) {
        return (String) headers.get("application-id");
    }

    protected static String getUuidHeader(Map headers) {
        return (String) headers.get("uuid");
    }

    protected static String getAPIName(org.apache.synapse.MessageContext messageContext) {
        return (String) messageContext.getProperty("SYNAPSE_REST_API");
    }

    protected static String getAPICtx(org.apache.synapse.MessageContext messageContext) {
        return (String) messageContext.getProperty("REST_API_CONTEXT");
    }

    protected static String getRestMethod(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        return (String) axis2MsgContext.getProperty("HTTP_METHOD");
    }

    protected static String getRestHttpResponseStatusCode(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        return String.valueOf(axis2MsgContext.getProperty("HTTP_SC"));
    }

    protected static String getURLPrx(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        return (String) axis2MsgContext.getProperty("REST_URL_PREFIX");
    }

    protected static String getTo(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        return (String) axis2MsgContext.getProperty("REST_URL_POSTFIX");
    }

    protected static String getElectedResource(org.apache.synapse.MessageContext messageContext) {
        return (String) messageContext.getProperty("API_ELECTED_RESOURCE");
    }

    protected static String getResourceCacheKey(org.apache.synapse.MessageContext messageContext) {
        return (String) messageContext.getProperty("API_RESOURCE_CACHE_KEY");
    }

    protected static String getRestReqFullPath(org.apache.synapse.MessageContext messageContext) {
        return (String) messageContext.getProperty("REST_FULL_REQUEST_PATH");
    }

    protected static Map getTransportHeaders(org.apache.synapse.MessageContext messageContext) {
        return (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    protected static String getApplicationName(org.apache.synapse.MessageContext messageContext){
        return (String) messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME);
    }

    protected static String getConsumerKey(org.apache.synapse.MessageContext messageContext){
        return (String) messageContext.getProperty(APIMgtGatewayConstants.CONSUMER_KEY);
    }

    protected static String getTransportInURL(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        String transportInURL = (String) axis2MsgContext.getProperty("TransportInURL");
        return transportInURL.substring(1);
    }

    protected static String getMatchingLogLevel(MessageContext messageContext,
                                          Map<Map<String, String>, String> logProperties) {
        //initializing variables to store resource level logging
        String apiLogLevel = null;
        String resourceLogLevel = null;
        String resourcePath = null;
        String resourceMethod = null;
        Resource selectedResource = null;
        //obtain the selected API by context and path
        API selectedApi = ApiUtils.getSelectedAPI(messageContext);
        String apiContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty("TransportInURL").toString();
        String httpMethod = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(Constants.Configuration.HTTP_METHOD);

        if (selectedApi != null) {
            Utils.setSubRequestPath(selectedApi, messageContext);
            //iterating through all the existing resources to match with the requesting method
            Map<String, Resource> resourcesMap = selectedApi.getResourcesMap();
            Set<Resource> acceptableResources = ApiUtils
                    .getAcceptableResources(resourcesMap, messageContext);
            if (!acceptableResources.isEmpty()) {
                for (RESTDispatcher dispatcher : ApiUtils.getDispatchers()) {
                    selectedResource = dispatcher.findResource(messageContext, acceptableResources);
                    if (selectedResource != null) {
                        DispatcherHelper helper = selectedResource.getDispatcherHelper();
                        for (Map.Entry<Map<String, String>, String> entry : logProperties.entrySet()) {
                            Map<String, String> key = entry.getKey();
                            //if resource path is empty, proceeding with API level logs
                            if (selectedApi.getContext().equals(key.get(APIConstants.API_CONTEXT_FOR_RESOURCE))) {
                                if (key.get(APIConstants.PATH_FOR_RESOURCE) == null && key.get(
                                        APIConstants.METHOD_FOR_RESOURCE) == null) {
                                    apiLogLevel = entry.getValue();
                                    //matching the methods first and then the resource path
                                } else if (httpMethod.equals(key.get(APIConstants.METHOD_FOR_RESOURCE))) {
                                    if (helper.getString().equals(key.get(APIConstants.PATH_FOR_RESOURCE))) {
                                        resourceLogLevel = entry.getValue();
                                        resourcePath = key.get(APIConstants.PATH_FOR_RESOURCE);
                                        resourceMethod = key.get(APIConstants.METHOD_FOR_RESOURCE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        boolean isResourceLevelHasHighPriority = false;
        if (resourceLogLevel != null) {
            switch (resourceLogLevel) {
                case APIConstants.LOG_LEVEL_FULL:
                    isResourceLevelHasHighPriority = true;
                    break;
                case APIConstants.LOG_LEVEL_STANDARD:
                    if (apiLogLevel != null && (apiLogLevel.equals(APIConstants.LOG_LEVEL_BASIC)
                            || apiLogLevel.equals(APIConstants.LOG_LEVEL_OFF))) {
                        isResourceLevelHasHighPriority = true;
                        break;
                    } else {
                        break;
                    }
                case APIConstants.LOG_LEVEL_BASIC:
                    if (apiLogLevel == null || apiLogLevel.equals(APIConstants.LOG_LEVEL_OFF)) {
                        isResourceLevelHasHighPriority = true;
                    } else {
                        break;
                    }
            }
            if (isResourceLevelHasHighPriority || apiLogLevel == null) {
                messageContext.setProperty(LogsHandler.LOG_LEVEL, resourceLogLevel);
                messageContext.setProperty(LogsHandler.RESOURCE_PATH, resourcePath);
                messageContext.setProperty(LogsHandler.RESOURCE_METHOD, resourceMethod);
                messageContext.setProperty("API_TO", apiContext);
                return resourceLogLevel;
            } else {
                messageContext.setProperty(LogsHandler.LOG_LEVEL, apiLogLevel);
                messageContext.setProperty("API_TO", apiContext);
                return apiLogLevel;
            }
        } else if (apiLogLevel != null) {
            messageContext.setProperty(LogsHandler.LOG_LEVEL, apiLogLevel);
            messageContext.setProperty("API_TO", apiContext);
            return apiLogLevel;
        } else {
            return null;
        }
    }

}