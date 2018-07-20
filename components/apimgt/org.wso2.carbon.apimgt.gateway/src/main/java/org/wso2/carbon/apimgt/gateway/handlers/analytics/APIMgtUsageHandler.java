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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;

import java.util.Map;
import java.util.regex.Pattern;

public class APIMgtUsageHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIMgtUsageHandler.class);
    public static final Pattern resourcePattern = Pattern.compile("^/.+?/.+?([/?].+)$");

    protected volatile APIMgtUsageDataPublisher publisher;

    public boolean handleRequest(MessageContext mc) {

        boolean enabled = getApiManagerAnalyticsConfiguration().isAnalyticsEnabled();
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

    public boolean handleResponse(MessageContext mc) {
        return true;
    }

}
