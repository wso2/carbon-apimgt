package org.wso2.carbon.apimgt.gateway.handlers.graphQL.analytics;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.common.gateway.graphql.GraphQLOperationAnalyzer;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.AnalyticsMetricsHandler;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import graphql.parser.Parser;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

public class GraphQLAnalyticsMetricsHandler {
    private static final Log log = LogFactory.getLog(GraphQLAnalyticsMetricsHandler.class);

    public void handleQuery(MessageContext messageContext) {
        collectData(messageContext);
    }

    public void handleMutation(MessageContext messageContext) {
        collectData(messageContext);
    }

    public void handleSubscribe(MessageContext messageContext) {
        collectData(messageContext);
    }

    private void collectData(MessageContext messageContext) {
        AnalyticsDataProvider provider;
        RequestDataCollector dataCollector;
        provider = new GraphQLAnalyticsDataProvider(messageContext,
                ServiceReferenceHolder.getInstance().getAnalyticsCustomDataProvider());
        dataCollector = new GraphQLOperationInfoCollector(provider);
        try {
            dataCollector.collectData();
        } catch (Exception e) {
            log.error("Error Occurred when collecting data", e);
        }
    }
}
