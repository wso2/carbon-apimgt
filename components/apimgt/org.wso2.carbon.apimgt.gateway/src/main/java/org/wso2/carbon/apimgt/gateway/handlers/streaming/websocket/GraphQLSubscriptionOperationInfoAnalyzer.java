package org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket;

import graphql.language.*;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.common.gateway.graphql.GraphQLOperationAnalyzer;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.analytics.GraphQLAnalyticsMetricsHandler;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.analytics.GraphQLOperationInfoCollector;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GraphQLSubscriptionOperationInfoAnalyzer {
    private Object APIConstants;
    private GraphQLAnalyticsMetricsHandler graphQLMetricsHandler;
    private static final Log log = LogFactory.getLog(GraphQLSubscriptionOperationInfoAnalyzer.class);

    public void analyzePayload(ChannelHandlerContext ctx) {
        Parser parser = new Parser();
        String payload = (String) WebSocketUtils.getPropertyFromChannel("GRAPHQL_PAYLOAD", ctx);
        Document document = parser.parseDocument(payload);
        GraphQLSchema graphQLSchema = (GraphQLSchema) WebSocketUtils.getPropertyFromChannel("GRAPHQL_SCHEMA", ctx);
        TypeDefinitionRegistry typeDefinition = (TypeDefinitionRegistry) WebSocketUtils.getPropertyFromChannel("TYPE_DEFINITION", ctx);
        //HashMap<String, Object> variablesMap = (HashMap<String, Object>) messageContext.getProperty("VARIABLE_MAP");
        String complexityInfoJson = (String) WebSocketUtils.getPropertyFromChannel("CONTROL_INFO", ctx);
        String type = null;

        for (Definition definition : document.getDefinitions()) {
            OperationDefinition operation = (OperationDefinition) definition;
            type = operation.getOperation().toString().toLowerCase(Locale.ROOT);
            for (Selection selection : operation.getSelectionSet().getSelections()) {
                String result = AstPrinter.printAst(selection);
                Document subDocument = parser.parseDocument(type + " Name { \n " + result + "\n }");
                HashMap<String, Object> variablesMap = null;
                Map<String, Object> operationInfo = GraphQLOperationAnalyzer
                        .getOperationInfo(type, selection, subDocument, graphQLSchema, typeDefinition, complexityInfoJson, variablesMap);
                WebSocketUtils.setApiPropertyToChannel(ctx,
                        "FIELD_USAGE", operationInfo);

                RequestDataCollector dataCollector;
                AnalyticsDataProvider provider = new WebSocketAnalyticsDataProvider(ctx,
                        ServiceReferenceHolder.getInstance().getAnalyticsCustomDataProvider());
                dataCollector = new GraphQLOperationInfoCollector(provider);
                try {
                    dataCollector.collectData();
                } catch (Exception e) {
                    log.error("Error Occurred when collecting data", e);
                }

            }
        }
    }
}
