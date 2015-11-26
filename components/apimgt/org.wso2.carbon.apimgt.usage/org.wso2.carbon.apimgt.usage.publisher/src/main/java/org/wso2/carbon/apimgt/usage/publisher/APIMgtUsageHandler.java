/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.usage.publisher;

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
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIMgtUsageHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIMgtUsageHandler.class);

    private volatile APIMgtUsageDataPublisher publisher;

    public boolean handleRequest(MessageContext mc) {

        boolean enabled = DataPublisherUtil.getApiManagerAnalyticsConfiguration().isAnalyticsEnabled();

        /*setting global analytic enabled status. Which use at by the by bam mediator in
        synapse to enable or disable destination based stat publishing*/
        mc.setProperty("isStatEnabled", Boolean.toString(enabled));

        boolean skipEventReceiverConnection = DataPublisherUtil.getApiManagerAnalyticsConfiguration().
                isSkipEventReceiverConnection();

        String publisherClass = UsageComponent.getAmConfigService().
                getAPIAnalyticsConfiguration().getPublisherClass();
        try {
            long currentTime = System.currentTimeMillis();

            if (!enabled || skipEventReceiverConnection) {
                return true;
            }

            if (publisher == null) {
                // The publisher initializes in the first request only
                synchronized (this) {
                    if (publisher == null) {
                        try {
                            log.debug("Instantiating Data Publisher");
                            publisher = (APIMgtUsageDataPublisher) APIUtil.getClassForName(publisherClass).newInstance();
                            publisher.init();
                        } catch (ClassNotFoundException e) {
                            log.error("Class not found " + publisherClass);
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + publisherClass);
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + publisherClass);
                        }
                    }
                }
            }

            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(mc);
            String consumerKey = "";
            String username = "";
            String applicationName = "";
            String applicationId = "";
            String tier = "";
            if (authContext != null) {
                consumerKey = authContext.getConsumerKey();
                username = authContext.getUsername();
                applicationName = authContext.getApplicationName();
                applicationId = authContext.getApplicationId();
                tier = authContext.getTier();
            }
            String hostName = DataPublisherUtil.getHostAddress();
            org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) mc).getAxis2MessageContext();
            Map headers = (Map) (axis2MsgContext).
                    getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            String userAgent = (String) headers.get(APIConstants.USER_AGENT);
            String context = (String) mc.getProperty(RESTConstants.REST_API_CONTEXT);
            String api_version = (String) mc.getProperty(RESTConstants.SYNAPSE_REST_API);
            String fullRequestPath = (String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
            int tenantDomainIndex = fullRequestPath.indexOf("/t/");
            String apiPublisher = (String) mc.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
            String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            if (tenantDomainIndex != -1) {
                String temp = fullRequestPath.substring(tenantDomainIndex + 3, fullRequestPath.length());
                tenantDomain = temp.substring(0, temp.indexOf("/"));
            }

            if (apiPublisher == null) {
                apiPublisher = getAPIProviderFromRESTAPI(api_version);
            }

            if (apiPublisher != null && !apiPublisher.endsWith(tenantDomain)) {
                apiPublisher = apiPublisher + "@" + tenantDomain;
            }

            int index = api_version.indexOf("--");

            if (index != -1) {
                api_version = api_version.substring(index + 2);
            }

            String api = api_version.split(":")[0];
            index = api.indexOf("--");
            if (index != -1) {
                api = api.substring(index + 2);
            }
            String version = (String) mc.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            String resource = extractResource(mc);
            String method = (String) (axis2MsgContext.getProperty(
                    Constants.Configuration.HTTP_METHOD));
            String userTenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(userTenantDomain);

            Object throttleOutProperty = mc.getProperty(APIConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY);
            boolean throttleOutHappened = false;
            if (throttleOutProperty != null && throttleOutProperty instanceof Boolean) {
                throttleOutHappened = (Boolean) throttleOutProperty;
            }

            RequestPublisherDTO requestPublisherDTO = new RequestPublisherDTO();
            requestPublisherDTO.setConsumerKey(consumerKey);
            requestPublisherDTO.setContext(context);
            requestPublisherDTO.setApi_version(api_version);
            requestPublisherDTO.setApi(api);
            requestPublisherDTO.setVersion(version);
            requestPublisherDTO.setResourcePath(resource);
            requestPublisherDTO.setMethod(method);
            requestPublisherDTO.setRequestTime(currentTime);
            requestPublisherDTO.setUsername(username);
            requestPublisherDTO.setTenantDomain((MultitenantUtils.getTenantDomain(apiPublisher)));
            requestPublisherDTO.setHostName(hostName);
            requestPublisherDTO.setApiPublisher(apiPublisher);
            requestPublisherDTO.setApplicationName(applicationName);
            requestPublisherDTO.setApplicationId(applicationId);
            requestPublisherDTO.setUserAgent(userAgent);
            requestPublisherDTO.setTier(tier);
            requestPublisherDTO.setContinuedOnThrottleOut(throttleOutHappened);

            publisher.publishEvent(requestPublisherDTO);

            //Metering related publishing is no longer used.
            /*//We check if usage metering is enabled for billing purpose
            if (DataPublisherUtil.isEnabledMetering()) {
                //If usage metering enabled create new usage stat object and publish to bam
                APIManagerRequestStats stats = new APIManagerRequestStats();
                stats.setRequestCount(1);
                stats.setTenantId(tenantId);
                try {
                    //Publish stat to bam
                    PublisherUtils.publish(stats, tenantId);
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug(e);
                    }
                    log.error("Error occurred while publishing request statistics. Full stacktrace available in debug logs. " + e.getMessage());
                }
            }*/

        } catch (Throwable e) {
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
        return true;
    }

    private String getAPIProviderFromRESTAPI(String api_version) {
        int index = api_version.indexOf("--");
        if (index != -1) {
            String apiProvider = api_version.substring(0, index);
            if (apiProvider.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
                apiProvider = apiProvider.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT,
                                      APIConstants.EMAIL_DOMAIN_SEPARATOR);
            }
            return apiProvider;
        }
        return null;
    }

    public boolean handleResponse(MessageContext mc) {
        return true;

    }

    private String extractResource(MessageContext mc) {
        String resource = "/";
        Pattern pattern = Pattern.compile("^/.+?/.+?([/?].+)$");
        Matcher matcher = pattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
        if (matcher.find()) {
            resource = matcher.group(1);
        }
        return resource;
    }

}
