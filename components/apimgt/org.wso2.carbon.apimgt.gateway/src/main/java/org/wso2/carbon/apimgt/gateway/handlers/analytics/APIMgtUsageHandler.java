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
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;

import java.util.Map;
import java.util.regex.Pattern;

public class APIMgtUsageHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIMgtUsageHandler.class);
    public static final Pattern resourcePattern = Pattern.compile("^/.+?/.+?([/?].+)$");

    protected volatile APIMgtUsageDataPublisher publisher;

    @MethodStats
    public boolean handleRequest(MessageContext mc) {

        TracingSpan span = null;

        // reset HTTP_METHOD, to rest api values before send them to analytics.
        // (only for graphQL APIs)
        if (mc.getProperty(APIConstants.API_TYPE) != null &&
                APIConstants.GRAPHQL_API.equals(mc.getProperty(APIConstants.API_TYPE).toString())) {
            ((Axis2MessageContext) mc).getAxis2MessageContext().
                    setProperty(Constants.Configuration.HTTP_METHOD, mc.getProperty(APIConstants.HTTP_VERB));
        }

        if (Util.tracingEnabled()) {
            TracingSpan responseLatencySpan = (TracingSpan) mc.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
            TracingTracer tracer = Util.getGlobalTracer();
            span = Util.startSpan(APIMgtGatewayConstants.API_MGT_USAGE_HANDLER, responseLatencySpan, tracer);
        }
        boolean enabled = getApiManagerAnalyticsConfiguration().isAnalyticsEnabled();
        if (Util.tracingEnabled()) {
            Util.finishSpan(span);
        }
        if (!enabled) {
            return true;
        }
        /*setting global analytic enabled status. Which use at by the by bam mediator in
        synapse to enable or disable destination based stat publishing*/
        mc.setProperty("isStatEnabled", Boolean.toString(enabled));
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) mc).getAxis2MessageContext();
        Map headers = (Map) (axis2MsgContext)
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String userAgent = (String) headers.get(APIConstants.USER_AGENT);
        String clientIp = DataPublisherUtil.getClientIp(axis2MsgContext);
        mc.setProperty(APIMgtGatewayConstants.CLIENT_USER_AGENT, userAgent);
        mc.setProperty(APIMgtGatewayConstants.CLIENT_IP, clientIp);
        return true;
    }

    protected APIManagerAnalyticsConfiguration getApiManagerAnalyticsConfiguration() {
        return DataPublisherUtil.getApiManagerAnalyticsConfiguration();
    }

    @MethodStats
    public boolean handleResponse(MessageContext mc) {
        return true;
    }

}
