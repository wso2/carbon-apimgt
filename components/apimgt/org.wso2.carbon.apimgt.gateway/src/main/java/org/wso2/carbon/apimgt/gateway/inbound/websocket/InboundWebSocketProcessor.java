/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.inbound.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.api.API;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.api.Resource;
import org.apache.synapse.api.dispatch.RESTDispatcher;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.ResourceNotFoundException;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketAnalyticsMetricsHandler;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiException;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.handshake.HandshakeProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.request.GraphQLRequestProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.request.RequestProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.response.GraphQLResponseProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.response.ResponseProcessor;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.Authentication.ApiKeyAuthenticator;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.Authentication.OAuthAuthenticator;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This class intercepts the inbound websocket handler execution during handshake, request messaging, response
 * messaging phases. This processor depends on netty inbound websocket channel pipeline.
 */
public class InboundWebSocketProcessor implements WebSocketProcessor {

    private static final Log log = LogFactory.getLog(InboundWebSocketProcessor.class);
    private WebSocketAnalyticsMetricsHandler metricsHandler;

    public InboundWebSocketProcessor() {

        if (APIUtil.isAnalyticsEnabled()) {
            metricsHandler = new WebSocketAnalyticsMetricsHandler();
        }
    }

    /**
     * This method process websocket handshake and extract necessary API information from the channel context and
     * request. Finally, hand over the processing to relevant handshake processor for authentication etc.
     *
     * @param req                   Handshake request
     * @param ctx                   Channel pipeline context
     * @param inboundMessageContext InboundMessageContext
     * @return InboundProcessorResponseDTO with handshake processing response
     */
    @Override
    public InboundProcessorResponseDTO handleHandshake(FullHttpRequest req, ChannelHandlerContext ctx,
                                                       InboundMessageContext inboundMessageContext) {

        InboundProcessorResponseDTO inboundProcessorResponseDTO;
        try {
            HandshakeProcessor handshakeProcessor = new HandshakeProcessor();
            setUris(req, inboundMessageContext);
            InboundWebsocketProcessorUtil.setTenantDomainToContext(inboundMessageContext);
            setMatchingResource(ctx, req, inboundMessageContext);
            // This needs to be called after setMatchingResource() to correctly set the fullRequestPath when invoking
            // with authorization as a query parameter
            setUris(req, inboundMessageContext);
            String userAgent = req.headers().get(HttpHeaders.USER_AGENT);

            // '-' is used for empty values to avoid possible errors in DAS side.
            // Required headers are stored one by one as validateOAuthHeader()
            // removes some headers from the request
            userAgent = userAgent != null ? userAgent : "-";
            inboundMessageContext.getRequestHeaders().put(HttpHeaders.USER_AGENT, userAgent);

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    inboundMessageContext.getTenantDomain(), true);
            if (isOauthAuthentication(req, inboundMessageContext)) {
                inboundMessageContext.setAuthenticator(new OAuthAuthenticator());
                setRequestHeaders(req, inboundMessageContext);
                setOrReplaceRequestHeaderIgnoreCase(WebsocketUtil.authorizationHeader, req, inboundMessageContext);
                inboundProcessorResponseDTO =
                        handshakeProcessor.processHandshake(inboundMessageContext);
                setRequestHeaders(req, inboundMessageContext);
            } else if (isAPIKeyAuthentication(req, inboundMessageContext)) {
                inboundMessageContext.setAuthenticator(new ApiKeyAuthenticator());
                setRequestHeaders(req, inboundMessageContext);
                setOrReplaceRequestHeaderIgnoreCase(APIConstants.API_KEY_HEADER_QUERY_PARAM, req, inboundMessageContext);
                inboundProcessorResponseDTO =
                        handshakeProcessor.processHandshake(inboundMessageContext);
                setRequestHeaders(req, inboundMessageContext);
            } else {
                String errorMessage = "No Authorization header, access_token query parameter, apikey header or query " +
                        "parameter present";
                log.error(errorMessage + " in request for the websocket context "
                        + inboundMessageContext.getApiContext());
                inboundProcessorResponseDTO = InboundWebsocketProcessorUtil.getHandshakeErrorDTO(
                        WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR, errorMessage);
            }
            if (inboundProcessorResponseDTO.isError()) {
                publishHandshakeAuthErrorEvent(ctx, inboundProcessorResponseDTO.getErrorMessage());
            }
            return inboundProcessorResponseDTO;
        } catch (APISecurityException e) {
            log.error("Authentication Failure for the websocket context: " + inboundMessageContext.getApiContext()
                    + e.getMessage());
            inboundProcessorResponseDTO = InboundWebsocketProcessorUtil.getHandshakeErrorDTO(
                    WebSocketApiConstants.HandshakeErrorConstants.API_AUTH_ERROR, e.getMessage());
            publishHandshakeAuthErrorEvent(ctx, e.getMessage());
        } catch (WebSocketApiException e) {
            log.error(e.getMessage());
            inboundProcessorResponseDTO = InboundWebsocketProcessorUtil.getHandshakeErrorDTO(
                    WebSocketApiConstants.HandshakeErrorConstants.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage());
            inboundProcessorResponseDTO = InboundWebsocketProcessorUtil.getHandshakeErrorDTO(
                    WebSocketApiConstants.HandshakeErrorConstants.RESOURCE_NOT_FOUND_ERROR, e.getMessage());

            Object errorCode = WebSocketUtils.getPropertyFromChannel(SynapseConstants.ERROR_CODE, ctx);
            if (errorCode != null && (int) errorCode ==
                    org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.RESOURCE_NOT_FOUND_ERROR_CODE) {
                publishResourceNotFoundEvent(ctx);
            }
        }
        return inboundProcessorResponseDTO;
    }

    /**
     * This method process websocket request messages (publish messages) and
     * hand over the processing to relevant request intercepting processor for authentication, scope validation,
     * throttling etc.
     *
     * @param msg                   Websocket request message frame
     * @param inboundMessageContext InboundMessageContext
     * @return InboundProcessorResponseDTO with handshake processing response
     */
    @Override
    public InboundProcessorResponseDTO handleRequest(WebSocketFrame msg, InboundMessageContext inboundMessageContext)
            throws APISecurityException {

        RequestProcessor requestProcessor;
        String msgText = null;
        if (APIConstants.GRAPHQL_API.equals(inboundMessageContext.getElectedAPI().getApiType())
                && msg instanceof TextWebSocketFrame) {
            requestProcessor = new GraphQLRequestProcessor();
            msgText = ((TextWebSocketFrame) msg).text();
        } else {
            requestProcessor = new RequestProcessor();
        }
        return requestProcessor.handleRequest(msg.content().capacity(), msgText, inboundMessageContext);
    }

    /**
     * This method process websocket response messages (subscribe messages) and
     * hand over the processing to relevant response intercepting processor for authentication, scope validation,
     * throttling etc.
     *
     * @param msg                   Websocket request message frame
     * @param inboundMessageContext InboundMessageContext
     * @return InboundProcessorResponseDTO with handshake processing response
     */
    @Override
    public InboundProcessorResponseDTO handleResponse(WebSocketFrame msg, InboundMessageContext inboundMessageContext)
            throws Exception {

        ResponseProcessor responseProcessor;
        String msgText = null;
        if (APIConstants.GRAPHQL_API.equals(inboundMessageContext.getElectedAPI().getApiType())
                && msg instanceof TextWebSocketFrame) {
            responseProcessor = new GraphQLResponseProcessor();
            msgText = ((TextWebSocketFrame) msg).text();
        } else {
            responseProcessor = new ResponseProcessor();
        }
        return responseProcessor.handleResponse(msg.content().capacity(), msgText, inboundMessageContext);

    }

    /**
     * Validates access_token query param and reset OAuth header in the handshake request.
     *
     * @param req                   Handshake request
     * @param inboundMessageContext InboundMessageContext
     * @return if validation success
     * @throws APISecurityException if an error occurs
     */
    protected boolean isOauthAuthentication(FullHttpRequest req, InboundMessageContext inboundMessageContext)
            throws APISecurityException {

        boolean containsAuthorizationHeader = inboundMessageContext.getRequestHeaders()
                .keySet()
                .stream()
                .anyMatch(key -> key.equalsIgnoreCase(WebsocketUtil.authorizationHeader));

        if (!containsAuthorizationHeader) {
            QueryStringDecoder decoder = new QueryStringDecoder(inboundMessageContext.getFullRequestPath());
            Map<String, List<String>> requestMap = decoder.parameters();
            if (requestMap.containsKey(APIConstants.AUTHORIZATION_QUERY_PARAM_DEFAULT) && requestMap.get(APIConstants.
                    AUTHORIZATION_QUERY_PARAM_DEFAULT) != null) {
                inboundMessageContext.getHeadersToAdd().put(WebsocketUtil.authorizationHeader,
                        APIConstants.CONSUMER_KEY_SEGMENT + StringUtils.SPACE + requestMap.get(APIConstants.
                                AUTHORIZATION_QUERY_PARAM_DEFAULT).get(0));
                InboundWebsocketProcessorUtil.removeTokenFromQuery(requestMap, inboundMessageContext, APIConstants.
                        AUTHORIZATION_QUERY_PARAM_DEFAULT);
                req.setUri(inboundMessageContext.getFullRequestPath());
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates apikey query param and reset apikey header in the handshake request.
     *
     * @param req                   Handshake request
     * @param inboundMessageContext InboundMessageContext
     * @return true if validation success
     * @throws APISecurityException if an error occurs
     */
    protected boolean isAPIKeyAuthentication(FullHttpRequest req, InboundMessageContext inboundMessageContext)
            throws APISecurityException {

        boolean containsAuthorizationHeader = inboundMessageContext.getRequestHeaders()
                .keySet()
                .stream()
                .anyMatch(key -> key.equalsIgnoreCase(APIConstants.API_KEY_HEADER_QUERY_PARAM));

        if (!containsAuthorizationHeader) {
            QueryStringDecoder decoder = new QueryStringDecoder(inboundMessageContext.getFullRequestPath());
            Map<String, List<String>> requestMap = decoder.parameters();
            if (requestMap.containsKey(APIConstants.API_KEY_HEADER_QUERY_PARAM) && requestMap.get(APIConstants.
                    API_KEY_HEADER_QUERY_PARAM) != null) {
                inboundMessageContext.getHeadersToAdd().put(APIConstants.API_KEY_HEADER_QUERY_PARAM, requestMap.
                        get(APIConstants.API_KEY_HEADER_QUERY_PARAM).get(0));
                InboundWebsocketProcessorUtil.removeTokenFromQuery(requestMap, inboundMessageContext,
                        APIConstants.API_KEY_HEADER_QUERY_PARAM);
                req.setUri(inboundMessageContext.getFullRequestPath());
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Extract full request path from the request and update InboundMessageContext.
     *
     * @param req                   Request object
     * @param inboundMessageContext InboundMessageContext
     */
    protected void setUris(FullHttpRequest req, InboundMessageContext inboundMessageContext)
            throws WebSocketApiException {

        try {
            String fullRequestPath = req.uri();
            inboundMessageContext.setFullRequestPath(req.uri());
            URI uriTemp;
            uriTemp = new URI(fullRequestPath);
            String requestPath = new URI(uriTemp.getScheme(), uriTemp.getAuthority(), uriTemp.getPath(), null,
                    uriTemp.getFragment()).toString();
            if (requestPath.endsWith(WebSocketApiConstants.URL_SEPARATOR)) {
                requestPath = requestPath.substring(0, requestPath.length() - 1);
            }
            inboundMessageContext.setRequestPath(requestPath);
            if (log.isDebugEnabled()) {
                log.debug("Websocket API fullRequestPath = " + inboundMessageContext.getRequestPath());
            }
        } catch (URISyntaxException e) {
            throw new WebSocketApiException("Error while parsing uri: " + e.getMessage());
        }
    }

    /**
     * Get matching resource for invoking handshake request.
     *
     * @param ctx                   Channel context
     * @param req                   Handshake request
     * @param inboundMessageContext InboundMessageContext
     * @throws WebSocketApiException     If an error occurs
     * @throws ResourceNotFoundException If no matching API or resource found
     */
    protected void setMatchingResource(ChannelHandlerContext ctx, FullHttpRequest req,
                                     InboundMessageContext inboundMessageContext) throws WebSocketApiException,
            ResourceNotFoundException {

        String matchingResource;
        try {
            MessageContext synCtx = getMessageContext(inboundMessageContext);
            API api = InboundWebsocketProcessorUtil.getApi(synCtx, inboundMessageContext);
            if (api == null) {
                throw new ResourceNotFoundException("No matching API was found to dispatch the request for path : "
                        + inboundMessageContext.getRequestPath());
            }
            inboundMessageContext.setApi(api);
            reConstructFullUriWithVersion(req, synCtx, inboundMessageContext);
            inboundMessageContext.setApiContext(api.getContext());
            Resource selectedResource = null;
            Utils.setSubRequestPath(api, synCtx);
            Set<Resource> acceptableResources = new LinkedHashSet<>(Arrays.asList(api.getResources()));
            if (!acceptableResources.isEmpty()) {
                for (RESTDispatcher dispatcher : ApiUtils.getDispatchers()) {
                    Resource resource = dispatcher.findResource(synCtx, acceptableResources);
                    if (resource != null) {
                        selectedResource = resource;
                        if (selectedResource.getDispatcherHelper()
                                .getString() != null && !selectedResource.getDispatcherHelper().getString()
                                .contains("/*")) {
                            break;
                        }
                    }
                }
                if (selectedResource != null && APIUtil.isAnalyticsEnabled()) {
                    WebSocketUtils.setApiPropertyToChannel(ctx, APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS,
                            WebSocketUtils.getEndpointUrl(selectedResource, synCtx));
                }
            }
            setApiPropertiesToChannel(ctx, inboundMessageContext);
            if (selectedResource == null) {
                WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_CODE,
                        org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.RESOURCE_NOT_FOUND_ERROR_CODE);
                throw new ResourceNotFoundException("No matching resource was found to dispatch the request for path : "
                        + inboundMessageContext.getRequestPath());
            }
            if (APIConstants.GRAPHQL_API.equals(inboundMessageContext.getElectedAPI().getApiType())) {
                inboundMessageContext.setGraphQLSchemaDTO(DataHolder.getInstance()
                        .getGraphQLSchemaDTOForAPI(inboundMessageContext.getElectedAPI().getUuid()));
            }
            matchingResource = selectedResource.getDispatcherHelper().getString();
            if (log.isDebugEnabled()) {
                log.info("Selected resource for API dispatch : " + matchingResource);
            }
        } catch (AxisFault | URISyntaxException e) {
            throw new WebSocketApiException("Error while getting matching resource for Websocket API");
        }
        inboundMessageContext.setMatchingResource(matchingResource);
    }

    /**
     * Get synapse message context from tenant domain.
     *
     * @param inboundMessageContext InboundMessageContext
     * @return MessageContext
     * @throws AxisFault          if an error occurs getting context
     * @throws URISyntaxException if an error occurs getting transport scheme
     */
    protected MessageContext getMessageContext(InboundMessageContext inboundMessageContext)
            throws AxisFault, URISyntaxException {

        String tenantDomain = inboundMessageContext.getTenantDomain();
        MessageContext synCtx = WebsocketUtil.getSynapseMessageContext(tenantDomain);
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        msgCtx.setIncomingTransportName(new URI(inboundMessageContext.getFullRequestPath()).getScheme());
        msgCtx.setProperty(Constants.Configuration.TRANSPORT_IN_URL, inboundMessageContext.getFullRequestPath());
        //Sets axis2 message context in InboundMessageContext for later use
        inboundMessageContext.setAxis2MessageContext(msgCtx);
        return synCtx;
    }

    /**
     * Set API properties to netty channel context.
     *
     * @param ctx                   ChannelHandlerContext
     * @param inboundMessageContext InboundMessageContext
     */
    protected void setApiPropertiesToChannel(ChannelHandlerContext ctx, InboundMessageContext inboundMessageContext) {
        Map<String, Object> apiPropertiesMap = WebSocketUtils.getApiProperties(ctx);
        apiPropertiesMap.put(RESTConstants.SYNAPSE_REST_API, inboundMessageContext.getApiName());
        apiPropertiesMap.put(RESTConstants.PROCESSED_API, inboundMessageContext.getApi());
        apiPropertiesMap.put(RESTConstants.REST_API_CONTEXT, inboundMessageContext.getApiContext());
        apiPropertiesMap.put(RESTConstants.SYNAPSE_REST_API_VERSION, inboundMessageContext.getVersion());
        if (inboundMessageContext.getElectedAPI().getApiType().equals(APIConstants.GRAPHQL_API)) {
            apiPropertiesMap.put(APIMgtGatewayConstants.API_OBJECT, inboundMessageContext.getElectedAPI());
            apiPropertiesMap.put(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST, true);
        }
        ctx.channel().attr(WebSocketUtils.WSO2_PROPERTIES).set(apiPropertiesMap);
    }

    /**
     * Reconstruct WS full request uri with version for default API requests.
     *
     * @param req                   Http Handshake request
     * @param synCtx                Synapse request
     * @param inboundMessageContext InboundMessageContext
     */
    protected void reConstructFullUriWithVersion(FullHttpRequest req, MessageContext synCtx,
                                               InboundMessageContext inboundMessageContext) {

        String fullRequestPath = inboundMessageContext.getFullRequestPath().replace(
                inboundMessageContext.getElectedRoute(), inboundMessageContext.getElectedAPI().getContext());
        req.setUri(fullRequestPath);
        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axis2MsgCtx.setProperty(Constants.Configuration.TRANSPORT_IN_URL, fullRequestPath);
        inboundMessageContext.getAxis2MessageContext().setProperty(Constants.Configuration.TRANSPORT_IN_URL,
                fullRequestPath);
        if (log.isDebugEnabled()) {
            log.debug("Updated full request uri : " + fullRequestPath);
        }
    }

    /**
     * Publish resource not found event if analytics enabled.
     *
     * @param ctx Channel context
     */
    protected void publishResourceNotFoundEvent(ChannelHandlerContext ctx) {

        if (APIUtil.isAnalyticsEnabled()) {
            WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_MESSAGE,
                    "No matching resource found to dispatch the request");
            metricsHandler.handleHandshake(ctx);
            removeErrorPropertiesFromChannel(ctx);
        }
    }

    /**
     * Publish handshake auth error event if analytics enabled.
     *
     * @param ctx Channel context
     */
    protected void publishHandshakeAuthErrorEvent(ChannelHandlerContext ctx, String errorMessage) {

        if (APIUtil.isAnalyticsEnabled()) {
            WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_CODE,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
            WebSocketUtils.setApiPropertyToChannel(ctx, SynapseConstants.ERROR_MESSAGE, errorMessage);
            metricsHandler.handleHandshake(ctx);
            removeErrorPropertiesFromChannel(ctx);
        }
    }

    /**
     * Remove error properties from channel properties.
     *
     * @param ctx Channel context
     */
    protected void removeErrorPropertiesFromChannel(ChannelHandlerContext ctx) {
        WebSocketUtils.removeApiPropertyFromChannel(ctx, SynapseConstants.ERROR_CODE);
        WebSocketUtils.removeApiPropertyFromChannel(ctx, SynapseConstants.ERROR_MESSAGE);
    }

    /**
     * Update and remove request headers using headersToAdd and headersToRemove set in InboundMessageContext.
     *
     * @param request               Handshake request
     * @param inboundMessageContext InboundMessageContext
     */
    protected void setRequestHeaders(FullHttpRequest request, InboundMessageContext inboundMessageContext) {
        Map<String, String> headersToAdd = inboundMessageContext.getHeadersToAdd();
        List<String> headersToRemove = inboundMessageContext.getHeadersToRemove();
        for (Map.Entry<String, String> header : headersToAdd.entrySet()) {
            request.headers().add(header.getKey(), header.getValue());
        }
        for (String header : headersToRemove) {
            request.headers().remove(header);
        }
        inboundMessageContext.setHeadersToRemove(new ArrayList<>());
    }

    /**
     * Sets or replaces a request header in InboundMessageContext matching the header key case-insensitively.
     *
     * @param headerKey             Header key to set or replace
     * @param request               Handshake request
     * @param inboundMessageContext InboundMessageContext
     */
    private static void setOrReplaceRequestHeaderIgnoreCase(String headerKey, FullHttpRequest request,
                                                            InboundMessageContext inboundMessageContext) {
        Optional<String> existingKey = inboundMessageContext.getRequestHeaders().keySet()
                .stream()
                .filter(key -> key.equalsIgnoreCase(headerKey))
                .findFirst();

        existingKey.ifPresent(inboundMessageContext.getRequestHeaders()::remove);
        inboundMessageContext.getRequestHeaders().put(headerKey, request.headers().get(headerKey));
    }
}
