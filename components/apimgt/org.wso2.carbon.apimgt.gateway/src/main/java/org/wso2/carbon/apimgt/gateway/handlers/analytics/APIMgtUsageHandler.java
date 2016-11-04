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

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIMgtUsageHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIMgtUsageHandler.class);
    public static final Pattern resourcePattern = Pattern.compile("^/.+?/.+?([/?].+)$");

    private volatile APIMgtUsageDataPublisher publisher;

    public boolean handleRequest(MessageContext mc) {

        boolean enabled = APIUtil.isAnalyticsEnabled();
        boolean skipEventReceiverConnection = DataPublisherUtil.getApiManagerAnalyticsConfiguration().
                isSkipEventReceiverConnection();

        if (!enabled || skipEventReceiverConnection) {
            return true;
        }

        /*setting global analytic enabled status. Which use at by the by bam mediator in
        synapse to enable or disable destination based stat publishing*/
        mc.setProperty("isStatEnabled", Boolean.toString(enabled));

        String publisherClass = UsageComponent.getAmConfigService().getAPIAnalyticsConfiguration().getPublisherClass();
        try {
            long currentTime = System.currentTimeMillis();
            if (publisher == null) {
                // The publisher initializes in the first request only
                synchronized (this) {
                    if (publisher == null) {
                        try {
                            log.debug("Instantiating Data Publisher");

                            APIMgtUsageDataPublisher tempPublisher =
                                    (APIMgtUsageDataPublisher) APIUtil.getClassForName(publisherClass).newInstance();
                            tempPublisher.init();
                            publisher = tempPublisher;
                        } catch (ClassNotFoundException e) {
                            log.error("Class not found " + publisherClass, e);
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + publisherClass, e);
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + publisherClass, e);
                        }
                    }
                }
            }

            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(mc);
            String consumerKey = "";
            String username = "";
            String applicationName = "";
            String applicationId = "";
            String applicationOwner = "";
            String tier = "";
            if (authContext != null) {
                consumerKey = authContext.getConsumerKey();
                username = authContext.getUsername();
                applicationName = authContext.getApplicationName();
                applicationId = authContext.getApplicationId();
                tier = authContext.getTier();
                applicationOwner = authContext.getSubscriber();
            }
            String hostName = DataPublisherUtil.getHostAddress();
            org.apache.axis2.context.MessageContext axis2MsgContext =
                    ((Axis2MessageContext) mc).getAxis2MessageContext();
            Map headers =
                    (Map) (axis2MsgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            String userAgent = (String) headers.get(APIConstants.USER_AGENT);
            String context = (String) mc.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) mc.getProperty(RESTConstants.SYNAPSE_REST_API);
            String fullRequestPath = (String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
            String apiPublisher = (String) mc.getProperty(APIMgtGatewayConstants.API_PUBLISHER);

            String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(fullRequestPath);
            if (apiPublisher == null) {
                apiPublisher = APIUtil.getAPIProviderFromRESTAPI(apiVersion, tenantDomain);
            }

            String api = APIUtil.getAPINamefromRESTAPI(apiVersion);
            String version = (String) mc.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            String resource = extractResource(mc);
            String resourceTemplate = (String) mc.getProperty(APIConstants.API_ELECTED_RESOURCE);

            String method = (String) (axis2MsgContext.getProperty(Constants.Configuration.HTTP_METHOD));

            Object throttleOutProperty = mc.getProperty(APIConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY);
            boolean throttleOutHappened = false;
            if (throttleOutProperty instanceof Boolean) {
                throttleOutHappened = (Boolean) throttleOutProperty;
            }
            String clientIp = DataPublisherUtil.getClientIp(axis2MsgContext);
            RequestPublisherDTO requestPublisherDTO = new RequestPublisherDTO();
            requestPublisherDTO.setConsumerKey(consumerKey);
            requestPublisherDTO.setContext(context);
            requestPublisherDTO.setApiVersion(apiVersion);
            requestPublisherDTO.setApi(api);
            requestPublisherDTO.setVersion(version);
            requestPublisherDTO.setResourcePath(resource);
            requestPublisherDTO.setResourceTemplate(resourceTemplate);
            requestPublisherDTO.setMethod(method);
            requestPublisherDTO.setRequestTime(currentTime);
            requestPublisherDTO.setUsername(username);
            requestPublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(apiPublisher));
            requestPublisherDTO.setHostName(hostName);
            requestPublisherDTO.setApiPublisher(apiPublisher);
            requestPublisherDTO.setApplicationName(applicationName);
            requestPublisherDTO.setApplicationId(applicationId);
            requestPublisherDTO.setUserAgent(userAgent);
            requestPublisherDTO.setTier(tier);
            requestPublisherDTO.setContinuedOnThrottleOut(throttleOutHappened);
            requestPublisherDTO.setClientIp(clientIp);
            requestPublisherDTO.setApplicationOwner(applicationOwner);
            publisher.publishEvent(requestPublisherDTO);
        } catch (Exception e) {
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
        return true;
    }

//moving to APIUTil

    public boolean handleResponse(MessageContext mc) {
        return true;

    }

    private String extractResource(MessageContext mc) {
        String resource = "/";
        Matcher matcher = resourcePattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
        if (matcher.find()) {
            resource = matcher.group(1);
        }
        return resource;
    }

}
