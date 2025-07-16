/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.ext;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.API;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.api.Resource;
import org.apache.synapse.api.dispatch.RESTDispatcher;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class handles WebSocket requests in the inbound flow of the API Gateway. It identifies the API and resource,
 * sets the sub-request path, and performs request mediation for handshake requests.
 */
public class WebSocketExtensionHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(WebSocketExtensionHandler.class);
    private static final String DIRECTION_IN = "In";

    @MethodStats
    public boolean handleRequest(MessageContext messageContext) {
        try {
            API selectedApi = Utils.getSelectedAPI(messageContext);
            Resource selectedResource = null;
            Utils.setSubRequestPath(selectedApi, messageContext);

            if (selectedApi != null) {
                Resource[] allAPIResources = selectedApi.getResources();
                Set<Resource> acceptableResources = new LinkedHashSet<>(Arrays.asList(allAPIResources));
                if (!acceptableResources.isEmpty()) {
                    for (RESTDispatcher dispatcher : RESTUtils.getDispatchers()) {
                        Resource resource = dispatcher.findResource(messageContext, acceptableResources);
                        if (resource != null) {
                            selectedResource = resource;
                            if (selectedResource.getDispatcherHelper()
                                    .getString() != null && !selectedResource.getDispatcherHelper().getString()
                                    .contains("/*")) {
                                break;
                            }
                        }
                    }
                    if (selectedResource == null) {
                        handleResourceNotFound(messageContext, Arrays.asList(allAPIResources));
                        return false;
                    }
                } else {
                    handleResourceNotFound(messageContext, Arrays.asList(allAPIResources));
                    return false;
                }
            }

            String resourceString = selectedResource.getDispatcherHelper().getString();
            messageContext.setProperty(APIConstants.API_ELECTED_RESOURCE, resourceString);
            if (!Boolean.TRUE.equals(messageContext.getProperty(WebSocketApiConstants.SOURCE_HANDSHAKE_PRESENT))) {
                // For websocket frames, set the 'To header' to be used in dynamic endpoint scenarios and pass without mediation
                Axis2MessageContext axis2MsgCtx = (Axis2MessageContext) messageContext;
                org.apache.axis2.context.MessageContext axis2Ctx = axis2MsgCtx.getAxis2MessageContext();
                axis2Ctx.getOptions().setTo(new EndpointReference(
                        (String) axis2MsgCtx.getProperty(WebSocketApiConstants.TARGET_ENDPOINT_ADDRESS)));
                return true;
            } else {
                // For WebSocket handshakes, handle the request mediation
                return mediate(messageContext);
            }
        } catch (Exception e) {
            log.error("Error occurred while identifying the resource in WebSocketTopicDispatcher", e);
            throw e;
        }
    }

    @MethodStats
    public boolean mediate(MessageContext messageContext) {
        try {
            Map localRegistry = messageContext.getConfiguration().getLocalRegistry();
            String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
            Object sequence = localRegistry.get(apiName + "--" + DIRECTION_IN);
            if (sequence instanceof Mediator) {
                return ((Mediator) sequence).mediate(messageContext);
            }
            return true;
        } catch (Exception e) {
            log.error("Error during post-request processing", e);
            throw e;
        }
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    private void handleResourceNotFound(MessageContext messageContext, List<Resource> allAPIResources) {
        Resource uriMatchingResource = null;
        for (RESTDispatcher dispatcher : RESTUtils.getDispatchers()) {
            uriMatchingResource = dispatcher.findResource(messageContext, allAPIResources);
            if (uriMatchingResource != null) {
                onResourceNotFoundError(messageContext, HttpStatus.SC_METHOD_NOT_ALLOWED,
                        APIMgtGatewayConstants.METHOD_NOT_FOUND_ERROR_MSG);
                return;
            }
        }
        onResourceNotFoundError(messageContext, HttpStatus.SC_NOT_FOUND,
                APIMgtGatewayConstants.RESOURCE_NOT_FOUND_ERROR_MSG);
    }

    private void onResourceNotFoundError(MessageContext messageContext, int statusCode, String errorMessage) {
        messageContext.setProperty(APIConstants.CUSTOM_HTTP_STATUS_CODE, statusCode);
        messageContext.setProperty(APIConstants.CUSTOM_ERROR_CODE, statusCode);
        messageContext.setProperty(APIConstants.CUSTOM_ERROR_MESSAGE, errorMessage);
        Mediator resourceMisMatchedSequence = messageContext.getSequence(RESTConstants.NO_MATCHING_RESOURCE_HANDLER);
        if (resourceMisMatchedSequence != null) {
            resourceMisMatchedSequence.mediate(messageContext);
        }
    }

}
