/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.gateway.mediators.APIMgtCommonExecutionPublisher;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestResponseStreamDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * This mediator is to publish events upon success API invocations
 */

public class APIMgtResponseHandler extends APIMgtCommonExecutionPublisher {

    public APIMgtResponseHandler() {
        super();
    }

    public boolean mediate(MessageContext mc) {
        TracingSpan span = null;
        if (Util.tracingEnabled()) {
            TracingSpan responseLatencySpan = (TracingSpan) mc.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
            TracingTracer tracer = Util.getGlobalTracer();
            span = Util.startSpan(APIMgtGatewayConstants.API_MGT_RESPONSE_HANDLER, responseLatencySpan, tracer);
        }
        if (publisher == null) {
            this.initializeDataPublisher();
        }
        if (Util.tracingEnabled()) {
            Util.finishSpan(span);
        }
        try {
            if (!enabled) {
                return true;
            }
            long responseSize = 0;
            long responseTime = 0;
            long serviceTime = 0;
            long backendTime = 0;
            long endTime = System.currentTimeMillis();
            boolean cacheHit = false;

            Object startTimeProperty = mc.getProperty(APIMgtGatewayConstants.REQUEST_START_TIME);
            long startTime = (startTimeProperty == null ? 0 :  Long.parseLong((String) startTimeProperty));

            Object beStartTimeProperty = mc.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME);
            long backendStartTime = (beStartTimeProperty == null ? 0 : Long.parseLong((String) beStartTimeProperty));

            Object beEndTimeProperty = mc.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME);
            long backendEndTime = (beEndTimeProperty == null ? 0 : ((Number) beEndTimeProperty).longValue());

            //Check the config property is set to true to build the response message in-order
            //to get the response message size
            boolean isBuildMsg = getApiAnalyticsConfiguration().isBuildMsg();
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) mc).
                    getAxis2MessageContext();
            if (isBuildMsg) {
                Map headers = (Map) axis2MC.getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                String contentLength = (String) headers.get(HttpHeaders.CONTENT_LENGTH);
                if (contentLength != null) {
                    responseSize = Integer.parseInt(contentLength);
                } else {  //When chunking is enabled
                    try {
                        RelayUtils.buildMessage(axis2MC);
                    } catch (IOException ex) {
                        //In case of an exception, it won't be propagated up,and set response size to 0
                        log.error("Error occurred while building the message to" +
                                  " calculate the response body size", ex);
                    } catch (XMLStreamException ex) {
                        log.error("Error occurred while building the message to calculate the response" +
                                  " body size", ex);
                    }
                    
                    SOAPEnvelope env = mc.getEnvelope();
                    if (env != null) {
                        SOAPBody soapbody = env.getBody();
                        if (soapbody != null) {
                            byte[] size = soapbody.toString().getBytes(Charset.defaultCharset());
                            responseSize = size.length;
                        }
                    }
                }
            }
            //When start time not properly set
            if (startTime == 0) {
                responseTime = 0;
                backendTime = 0;
                serviceTime = 0;
            } else if (endTime != 0 && backendStartTime != 0 && backendEndTime != 0) { //When
                // response caching is disabled
                responseTime = endTime - startTime;
                backendTime = backendEndTime - backendStartTime;
                serviceTime = responseTime - backendTime;

            } else if (endTime != 0 && backendStartTime == 0) {//When response caching enabled
                responseTime = endTime - startTime;
                serviceTime = responseTime;
                backendTime = 0;
                cacheHit = true;
            }
            String keyType = (String) mc.getProperty(APIConstants.API_KEY_TYPE);
            String correlationID = GatewayUtils.getAndSetCorrelationID(mc);
            JSONObject obj = new JSONObject();
            obj.put("keyType", keyType);
            obj.put("correlationID", correlationID);
            String metaClientType = obj.toJSONString();
            String fullRequestPath = (String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
            String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(fullRequestPath);
            String apiVersion = (String) mc.getProperty(RESTConstants.SYNAPSE_REST_API);
            String creator = (String) mc.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
            if (creator == null) {
                creator = APIUtil.getAPIProviderFromRESTAPI(apiVersion, tenantDomain);
            }
            //get the version
            apiVersion = apiVersion.split(":v")[1];
            String url = (String) mc.getProperty(RESTConstants.REST_URL_PREFIX);

            if (url == null) {
                url = (String) axis2MC.getProperty(APIMgtGatewayConstants.SERVICE_PREFIX);
            }

            URL apiurl = new URL(url);
            int port = apiurl.getPort();
            String protocol = mc.getProperty(SynapseConstants.TRANSPORT_IN_NAME) + "-" + port;

            //taken from request added new
            Object throttleOutProperty = mc.getProperty(APIConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY);
            boolean throttleOutHappened = false;
            if (throttleOutProperty instanceof Boolean) {
                throttleOutHappened = (Boolean) throttleOutProperty;
            }
            
            String consumerKey = "";
            String username = "";
            String applicationName = "";
            String applicationId = "";
            String applicationOwner = "";
            String tier = "";
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(mc);
            if (authContext != null) {
                consumerKey = authContext.getConsumerKey();
                username = authContext.getUsername();
                applicationName = authContext.getApplicationName();
                applicationId = authContext.getApplicationId();
                if (applicationName == null || "".equals(applicationName)) {
                    applicationName = "None";
                    applicationId = "0";
                }
                tier = authContext.getTier();
                applicationOwner = authContext.getSubscriber();
                if (applicationOwner == null || "".equals(applicationOwner)) {
                    applicationOwner = "None";
                }
            }

            RequestResponseStreamDTO stream = new RequestResponseStreamDTO();
            stream.setApiContext((String) mc.getProperty(APIMgtGatewayConstants.CONTEXT));
            stream.setApiHostname(GatewayUtils.getHostName(mc));
            stream.setApiMethod((String) mc.getProperty(APIMgtGatewayConstants.HTTP_METHOD));
            stream.setApiName((String) mc.getProperty(APIMgtGatewayConstants.API));
            stream.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(creator));
            stream.setApiCreator(creator);
            stream.setApiResourcePath(GatewayUtils.extractResource(mc));
            stream.setApiResourceTemplate((String) mc.getProperty(APIConstants.API_ELECTED_RESOURCE));
            stream.setApiTier(tier);
            stream.setApiVersion(apiVersion);
            consumerKey = (String) mc.getProperty(APIMgtGatewayConstants.CONSUMER_KEY);
            if (consumerKey == null || "".equals(consumerKey)) {
                consumerKey = "None";
            }
            stream.setApplicationConsumerKey(consumerKey);
            stream.setApplicationId(applicationId);
            stream.setApplicationName(applicationName);
            stream.setApplicationOwner(applicationOwner);
            stream.setBackendTime(backendTime);
            stream.setDestination(GatewayUtils.extractAddressBasePath(mc));
            stream.setExecutionTime(GatewayUtils.getExecutionTime(mc));
            stream.setMetaClientType(metaClientType); // check meta type
            stream.setProtocol(protocol);
            stream.setRequestTimestamp(
                    Long.parseLong((String) mc.getProperty(APIMgtGatewayConstants.REQUEST_START_TIME)));
            stream.setResponseCacheHit(cacheHit);
            int responseCode;
            if (axis2MC.getProperty(SynapseConstants.HTTP_SC) instanceof String) {
                responseCode = Integer.parseInt((String) axis2MC.getProperty(SynapseConstants.HTTP_SC));
            } else {
                responseCode = (Integer) axis2MC.getProperty(SynapseConstants.HTTP_SC);
            }
            stream.setResponseCode(responseCode);
            stream.setResponseSize(responseSize);
            stream.setServiceTime(serviceTime);
            stream.setThrottledOut(throttleOutHappened);
            stream.setUserAgent((String) mc.getProperty(APIMgtGatewayConstants.CLIENT_USER_AGENT));
            stream.setUserIp((String) mc.getProperty(APIMgtGatewayConstants.CLIENT_IP));
            stream.setUsername(username);
            stream.setUserTenantDomain(MultitenantUtils.getTenantDomain(username));
            stream.setResponseTime(responseTime);
            stream.setCorrelationID(correlationID);
            stream.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
            stream.setLabel(APIMgtGatewayConstants.SYNAPDE_GW_LABEL);
            Map<String, String> properties = Utils.getCustomAnalyticsProperties(mc);
            stream.setProperties(properties);
            if (log.isDebugEnabled()) {
                log.debug("Publishing success API invocation event from gateway to analytics for: "
                        + mc.getProperty(APIMgtGatewayConstants.CONTEXT) + " with ID: " +
                        mc.getMessageID() + " started" + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
            publisher.publishEvent(stream);
            if (log.isDebugEnabled()) {
                log.debug("Publishing success API invocation event from gateway to analytics for: "
                        + mc.getProperty(APIMgtGatewayConstants.CONTEXT) + " with ID: " +
                        mc.getMessageID() + " ended" + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
        } catch (Exception e) {
            log.error("Cannot publish response event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    protected APIManagerAnalyticsConfiguration getApiAnalyticsConfiguration() {
        return UsageComponent.getAmConfigService().getAPIAnalyticsConfiguration();
    }

    public boolean isContentAware() {
        return false;
    }
}

