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
import org.apache.synapse.api.dispatch.URITemplateBasedDispatcher;
import org.apache.synapse.api.dispatch.URITemplateHelper;
import org.apache.synapse.api.version.DefaultStrategy;
import org.apache.synapse.api.version.VersionStrategy;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.*;

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

    protected static String getResourceCacheKey(org.apache.synapse.MessageContext messageContext){
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

    protected static String fetchLogLevel(MessageContext messageContext,
                                        Map<Map<String, String>, String> logProperties) {
        //initializing variables to store resource level logging
        String subPath;
        API selectedApi = null;
        String apiLogLevel = null;
        String resourceLogLevel = null;
        String resourcePath = null;
        String resourceMethod = null;
        //getting the API collection from the synapse configuration to find the invoked API
        Collection<API> apiSet = messageContext.getEnvironment().getSynapseConfiguration().getAPIs();
        List<API> duplicateApiSet = new ArrayList<API>(apiSet);
        //obtaining required parameters to execute findResource method
        String apiContext = ((Axis2MessageContext)messageContext).getAxis2MessageContext().
                getProperty("TransportInURL").toString();
        String httpMethod = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String corsRequestMethod = (String) headers.get(APIConstants.CORSHeaders.ACCESS_CONTROL_REQUEST_METHOD);

        for (API api : duplicateApiSet) {
            if (apiContext.contains(api.getContext())) {
                selectedApi = api;
                break;
            }
        }

        if (selectedApi != null) {
            //obtaining the subpath vua full request path (ex:{resource}/{id}) to invoke the findResource method
            String path = ApiUtils.getFullRequestPath(messageContext);
            VersionStrategy versionStrategy = new DefaultStrategy(selectedApi);
            if (versionStrategy.getVersionType().equals(VersionStrategyFactory.TYPE_URL)) {
                subPath = path.substring(selectedApi.getContext().length() + versionStrategy.getVersion().length() + 1);
            } else {
                subPath = path.substring(selectedApi.getContext().length());
            }
            if ("".equals(subPath)) {
                subPath = "/";
            }
            messageContext.setProperty(RESTConstants.REST_SUB_REQUEST_PATH, subPath);
            //iterating through all the existing resources to match with the requesting method
            Resource[] allAPIResources = selectedApi.getResources();
            Set<Resource> acceptableResources = new LinkedHashSet<>();
            for (Resource resource : allAPIResources) {
                //If the requesting method is OPTIONS or if the Resource contains the requesting method
                if ((RESTConstants.METHOD_OPTIONS.equals(httpMethod) && resource.getMethods() != null &&
                        Arrays.asList(resource.getMethods()).contains(corsRequestMethod)) ||
                        (resource.getMethods() != null && Arrays.asList(resource.getMethods()).contains(httpMethod))) {
                    acceptableResources.add(resource);
                }
            }
            if (!acceptableResources.isEmpty()) {
                for (RESTDispatcher dispatcher : ApiUtils.getDispatchers()) {
                    if (dispatcher instanceof URITemplateBasedDispatcher){
                        //matching the available resources with the message context
                        Resource selectedResource = dispatcher.findResource(messageContext, acceptableResources);
                        if (selectedResource != null) {
                            messageContext.setProperty(LogsHandler.SELECTED_RESOURCE, selectedResource);
                            DispatcherHelper helper = selectedResource.getDispatcherHelper();
                            if (helper instanceof URITemplateHelper) {
                                URITemplateHelper templateHelper = (URITemplateHelper) helper;
                                for (Map.Entry<Map<String, String>, String> entry : logProperties.entrySet()) {
                                    Map<String, String> key = entry.getKey();
                                    //if resource path is empty, proceeding with API level logs
                                    if (key.get("resourcePath") == null && key.get("resourceMethod") == null) {
                                        apiLogLevel = entry.getValue();
                                    //matching the methods first and then the resource path
                                    } else if (httpMethod.equals(key.get("resourceMethod"))) {
                                        if(templateHelper.getString().equals(key.get("resourcePath"))) {
                                            resourceLogLevel = entry.getValue();
                                            resourcePath = key.get("resourcePath");
                                            resourceMethod = key.get("resourceMethod");
                                        }
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
                    if (apiLogLevel != null && (apiLogLevel.equals(APIConstants.LOG_LEVEL_BASIC) ||
                            apiLogLevel.equals(APIConstants.LOG_LEVEL_OFF))) {
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

    protected static String getMatchingLogLevel(MessageContext ctx,
                                                Map<Map<String, String>, String> logProperties) {
        String apiCtx = LogUtils.getTransportInURL(ctx);
        String apiHttpMethod = (String) ((Axis2MessageContext) ctx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String apiLogLevel = null;
        String resourceLogLevel = null;
        String resourcePath = null;
        String resourceMethod = null;
        String logResourcePath, resourcePathRegexPattern = null;
        for (Map.Entry<Map<String, String>, String> entry : logProperties.entrySet()) {
            Map<String, String> key = entry.getKey();
            String apiResourcePath;
            if (apiCtx.split("/", 3).length > 2) {
                apiResourcePath = apiCtx.split("/", 3)[2];
            } else {
                apiResourcePath = "";
            }
            if (key.containsKey("resourcePath") && key.get("resourcePath") != null) {
                logResourcePath = key.get("resourcePath");
                resourcePathRegexPattern = logResourcePath.replace("/", "\\/");
                resourcePathRegexPattern = resourcePathRegexPattern.replaceAll("\\{.*?\\}",
                        "\\\\w{0,}([-,_]?\\\\w{1,})+");
            }
            if (key.get("context").startsWith(key.get("context") + "/") ||
                    key.get("context").equals(key.get("context"))) {
                if (key.get("resourcePath") == null && key.get("resourceMethod") == null) {
                    apiLogLevel = entry.getValue();
                } else if (("/" + apiResourcePath).matches(resourcePathRegexPattern)
                        && apiHttpMethod.equals(key.get("resourceMethod"))) {
                    resourceLogLevel = entry.getValue();
                    resourcePath = key.get("resourcePath");
                    resourceMethod = key.get("resourceMethod");
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
                    if (apiLogLevel != null && (apiLogLevel.equals(APIConstants.LOG_LEVEL_BASIC) ||
                            apiLogLevel.equals(APIConstants.LOG_LEVEL_OFF))) {
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
                ctx.setProperty(LogsHandler.LOG_LEVEL, resourceLogLevel);
                ctx.setProperty(LogsHandler.RESOURCE_PATH, resourcePath);
                ctx.setProperty(LogsHandler.RESOURCE_METHOD, resourceMethod);
                ctx.setProperty("API_TO", apiCtx);
                return resourceLogLevel;
            } else {
                ctx.setProperty(LogsHandler.LOG_LEVEL, apiLogLevel);
                ctx.setProperty("API_TO", apiCtx);
                return apiLogLevel;
            }
        } else if (apiLogLevel != null) {
            ctx.setProperty(LogsHandler.LOG_LEVEL, apiLogLevel);
            ctx.setProperty("API_TO", apiCtx);
            return apiLogLevel;
        } else {
            return null;
        }
    }
}