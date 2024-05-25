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
package org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket;

import graphql.language.AstPrinter;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.common.analytics.collectors.impl.GenericRequestDataCollector;
import org.wso2.carbon.apimgt.common.gateway.graphql.GraphQLOperationAnalyzerUtil;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * analyze graphql subscription payload
 * then engages metric handler to handle metric publishing
 */
public class GraphQLSubscriptionOperationHandler {
    private static final Log log = LogFactory.getLog(GraphQLSubscriptionOperationHandler.class);

    /**
     * @param ctx Channel Handler Context
     */
    public void analyzePayload(ChannelHandlerContext ctx) {
        Parser parser = new Parser();
        GraphQLSchema graphQLSchema = (GraphQLSchema) WebSocketUtils.getPropertyFromChannel(APIConstants.GRAPHQL_SCHEMA, ctx);
        TypeDefinitionRegistry typeDefinition = (TypeDefinitionRegistry) WebSocketUtils.getPropertyFromChannel(APIConstants.TYPE_DEFINITION, ctx);
        String complexityInfoJson = (String) WebSocketUtils.getPropertyFromChannel(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY, ctx);
        OperationDefinition operation = (OperationDefinition) WebSocketUtils.getPropertyFromChannel(APIConstants.GRAPHQL_OPERATION, ctx);
        String type = operation.getOperation().toString().toLowerCase(Locale.ROOT);

        for (Selection selection : operation.getSelectionSet().getSelections()) {
            String result = AstPrinter.printAst(selection);
            Document subDocument = parser.parseDocument(type + " Name { \n " + result + "\n }");
            HashMap<String, Object> variablesMap = null;
            Map<String, Object> operationInfo = GraphQLOperationAnalyzerUtil
                    .getOperationInfo(type, selection, subDocument, graphQLSchema, typeDefinition, complexityInfoJson, variablesMap);
            WebSocketUtils.setApiPropertyToChannel(ctx,
                    APIConstants.OPERATION_INFO, operationInfo);

            List<Map> fieldUsage = GraphQLOperationAnalyzerUtil.getUsedFields(selection);
            WebSocketUtils.setApiPropertyToChannel(ctx,
                    APIConstants.ACCESSED_FIELDS, fieldUsage);

            RequestDataCollector dataCollector;
            AnalyticsDataProvider provider = new WebSocketAnalyticsDataProvider(ctx,
                    ServiceReferenceHolder.getInstance().getAnalyticsCustomDataProvider());
            dataCollector = new GenericRequestDataCollector(provider);
            try {
                dataCollector.collectData();
            } catch (Exception e) {
                log.error("Error Occurred when collecting data", e);
            }

        }
    }
}
