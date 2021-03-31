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
package org.wso2.carbon.apimgt.gateway.handlers.streaming.webhook;

import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Target;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.AsyncAnalyticsDataProvider;
import org.wso2.carbon.apimgt.impl.APIConstants;

public class WebhooksAnalyticsDataProvider extends AsyncAnalyticsDataProvider {

    private MessageContext messageContext;

    public WebhooksAnalyticsDataProvider(MessageContext messageContext) {
        super(messageContext);
        this.messageContext = messageContext;
    }

    @Override
    public int getTargetResponseCode() {
        String responseCode = (String) messageContext.getProperty(Constants.BACKEND_RESPONSE_CODE);
        return Integer.parseInt(responseCode);
    }

    @Override
    public Operation getOperation() {
        String httpMethod = (String) messageContext.getProperty(RESTConstants.REST_METHOD);
        String apiResourceTemplate = (String) messageContext.getProperty(APIConstants.Webhooks.
                SUBSCRIBER_TOPIC_PROPERTY);
        Operation operation = new Operation();
        operation.setApiMethod(httpMethod);
        operation.setApiResourceTemplate(apiResourceTemplate);
        return operation;
    }

    @Override
    public Target getTarget() {
        Target target = new Target();
        boolean isCacheHit = messageContext.getPropertyKeySet().contains(Constants.CACHED_RESPONSE_KEY);
        String endpointAddress = (String) messageContext.getProperty(APIConstants.Webhooks.
                SUBSCRIBER_CALLBACK_PROPERTY);
        int targetResponseCode = getTargetResponseCode();
        target.setResponseCacheHit(isCacheHit);
        target.setDestination(endpointAddress);
        target.setTargetResponseCode(targetResponseCode);
        return target;
    }

}
