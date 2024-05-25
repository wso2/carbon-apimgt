/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.gateway.handlers.graphQL.analytics;

import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.common.analytics.collectors.impl.GenericRequestDataCollector;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.SynapseAnalyticsDataProvider;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;

import org.apache.commons.logging.Log;

/**
 * handles engaging graphql data provider and data collector and
 * publish graphql analytics event through the data publisher
 */
public class GraphQLAnalyticsMetricsHandler {
    private static final Log log = LogFactory.getLog(GraphQLAnalyticsMetricsHandler.class);

    /**
     * @param messageContext
     */
    public void handleQuery(MessageContext messageContext) {
        collectData(messageContext);
    }

    /**
     * @param messageContext
     */
    public void handleMutation(MessageContext messageContext) {
        collectData(messageContext);
    }

    /**
     * @param messageContext
     */
    public void handleSubscribe(MessageContext messageContext) {
        collectData(messageContext);
    }

    private void collectData(MessageContext messageContext) {
        AnalyticsDataProvider provider;
        RequestDataCollector dataCollector;
        provider = new SynapseAnalyticsDataProvider(messageContext,
                ServiceReferenceHolder.getInstance().getAnalyticsCustomDataProvider());
        dataCollector = new GenericRequestDataCollector(provider);
        try {
            dataCollector.collectData();
        } catch (Exception e) {
            log.error("Error Occurred when collecting data", e);
        }
    }
}
