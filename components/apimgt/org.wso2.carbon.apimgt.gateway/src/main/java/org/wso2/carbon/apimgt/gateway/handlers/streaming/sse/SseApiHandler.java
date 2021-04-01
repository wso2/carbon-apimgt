/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.analytics.SseResponseEventDataProvider;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.throttling.ThrottleInfo;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.utils.SseUtils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import static org.apache.axis2.Constants.Configuration.HTTP_METHOD;
import static org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.SseApiConstants.SSE_ANALYTICS_INFO;
import static org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.SseApiConstants.SSE_CONTENT_TYPE;
import static org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.SseApiConstants.SSE_THROTTLE_DTO;
import static org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.SseApiConstants.THROTTLED_MESSAGE;
import static org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.SseApiConstants.THROTTLED_OUT_ERROR_MESSAGE;
import static org.wso2.carbon.apimgt.impl.APIConstants.AsyncApi.ASYNC_MESSAGE_TYPE;
import static org.wso2.carbon.apimgt.impl.APIConstants.AsyncApi.ASYNC_MESSAGE_TYPE_SUBSCRIBE;

/**
 * Wraps the authentication handler for the purpose of changing the http method before calling it.
 */
@SuppressWarnings("unused")
public class SseApiHandler extends APIAuthenticationHandler {

    private static final Log log = LogFactory.getLog(SseApiHandler.class);
    private static final QName TEXT_ELEMENT = new QName("http://ws.apache.org/commons/ns/payload", "text");

    @Override
    public boolean handleRequest(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axisCtx.setProperty(PassThroughConstants.SYNAPSE_ARTIFACT_TYPE, APIConstants.API_TYPE_SSE);
        synCtx.setProperty(org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.SKIP_DEFAULT_METRICS_PUBLISHING,
                           true);
        synCtx.setProperty(ASYNC_MESSAGE_TYPE, ASYNC_MESSAGE_TYPE_SUBSCRIBE);
        GatewayUtils.setRequestDestination(synCtx);

        // set http verb for authentication
        Object httpVerb = axisCtx.getProperty(HTTP_METHOD);
        axisCtx.setProperty(HTTP_METHOD, APIConstants.SubscriptionCreatedStatus.SUBSCRIBE);
        boolean isAuthenticated = super.handleRequest(synCtx);
        axisCtx.setProperty(Constants.Configuration.HTTP_METHOD, httpVerb);

        if (isAuthenticated && isThrottled(axisCtx, synCtx)) {
            return false;
        } else {
            return isAuthenticated;
        }
    }

    private boolean isThrottled(org.apache.axis2.context.MessageContext axisCtx, MessageContext synCtx) {

        AuthenticationContext authenticationContext = APISecurityUtils.getAuthenticationContext(synCtx);
        ThrottleInfo throttleInfo = getThrottlingInfo(authenticationContext, synCtx);
        boolean isThrottled = SseUtils.isRequestBlocked(authenticationContext, throttleInfo.getApiContext(),
                                                        throttleInfo.getApiVersion(), throttleInfo.getAuthorizedUser(),
                                                        throttleInfo.getRemoteIp(),
                                                        throttleInfo.getSubscriberTenantDomain());
        if (!isThrottled) {
            // do throttling if request is not blocked by global conditions only
            isThrottled = SseUtils.isThrottled(throttleInfo.getSubscriberTenantDomain(),
                                               throttleInfo.getResourceLevelThrottleKey(),
                                               throttleInfo.getSubscriptionLevelThrottleKey(),
                                               throttleInfo.getApplicationLevelThrottleKey());
        }
        if (isThrottled) {
            handleThrottledOut(synCtx);
            return true;
        }
        if (APIUtil.isAnalyticsEnabled()) {
            AnalyticsDataProvider provider = new SseResponseEventDataProvider(synCtx);
            axisCtx.setProperty(SSE_ANALYTICS_INFO, provider);
        }
        return false;
    }

    private ThrottleInfo getThrottlingInfo(AuthenticationContext authenticationContext, MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) synCtx).
                getAxis2MessageContext();
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        List<VerbInfoDTO> verbInfoList = (List<VerbInfoDTO>) synCtx.getProperty(APIConstants.VERB_INFO_DTO);
        String resourceLevelThrottleKey = null;
        String resourceLevelTier = null;
        if (verbInfoList != null) {
            // for sse, there will be only one verb info list
            VerbInfoDTO verbInfoDTO = verbInfoList.get(0);
            resourceLevelThrottleKey = verbInfoDTO.getRequestKey();
            resourceLevelTier = verbInfoDTO.getThrottling();
        }
        String remoteIP = GatewayUtils.getIp(axis2MC);
        ThrottleInfo throttleInfo = new ThrottleInfo(authenticationContext, apiContext, apiVersion,
                                                     resourceLevelThrottleKey, resourceLevelTier, remoteIP);
        axis2MC.setProperty(SSE_THROTTLE_DTO, throttleInfo);
        return throttleInfo;
    }

    private void handleThrottledOut(MessageContext synCtx) {

        log.warn("Request is throttled out");
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement payload = factory.createOMElement(TEXT_ELEMENT);
        payload.setText(THROTTLED_MESSAGE);
        synCtx.getEnvelope().getBody().addChild(payload);
        synCtx.setProperty(SynapseConstants.ERROR_CODE, APIThrottleConstants.APPLICATION_THROTTLE_OUT_ERROR_CODE);
        synCtx.setProperty(SynapseConstants.ERROR_MESSAGE, THROTTLED_OUT_ERROR_MESSAGE);
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, SSE_CONTENT_TYPE);
        axis2MC.setProperty(Constants.Configuration.CONTENT_TYPE, SSE_CONTENT_TYPE);
        axis2MC.setProperty(NhttpConstants.HTTP_SC, APIThrottleConstants.SC_TOO_MANY_REQUESTS);
        synCtx.setResponse(true);
        synCtx.setProperty(SynapseConstants.RESPONSE, "true");
        synCtx.setTo(null);
        axis2MC.removeProperty(NhttpConstants.NO_ENTITY_BODY);
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            headers.remove(HttpHeaders.AUTHORIZATION);
            headers.remove(HttpHeaders.HOST);
        }
        Axis2Sender.sendBack(synCtx);
    }

}
