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


import graphql.language.AstPrinter;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.common.gateway.graphql.GraphQLOperationAnalyzer;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * analyze each query and mutation
 * then engages metric handler to handle metric publishing
 */
public class GraphQLOperationInfoAnalyzer {

    private GraphQLAnalyticsMetricsHandler graphQLMetricsHandler;

    public void analyzePayload(MessageContext messageContext){

        Parser parser = new Parser();
        String payload = (String) messageContext.getProperty(APIConstants.GRAPHQL_PAYLOAD);
        Document document = parser.parseDocument(payload);
        GraphQLSchema graphQLSchema = (GraphQLSchema) messageContext.getProperty(APIConstants.GRAPHQL_SCHEMA);
        TypeDefinitionRegistry typeDefinition = (TypeDefinitionRegistry) messageContext.getProperty(APIConstants.TYPE_DEFINITION);
        Map<String, Object> variablesMap = (Map<String, Object>) messageContext.getProperty(APIConstants.VARIABLE_MAP);
        String complexityInfoJson = (String) messageContext
                .getProperty(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY);
        String type = null;

        for (Definition definition : document.getDefinitions()) {
            OperationDefinition operation = (OperationDefinition) definition;
            type = operation.getOperation().toString().toLowerCase(Locale.ROOT);
            for (Selection selection : operation.getSelectionSet().getSelections()) {
                String result = AstPrinter.printAst(selection);
                Document subDocument = parser.parseDocument( type + " { \n " + result + "\n }");

                Map<String, Object> operationInfo = GraphQLOperationAnalyzer
                        .getOperationInfo(type, selection, subDocument,graphQLSchema, typeDefinition, complexityInfoJson, variablesMap);
                messageContext.setProperty(APIConstants.OPERATION_INFO, operationInfo);

                List<Map> fieldUsage = GraphQLOperationAnalyzer.getUsedFields(selection);
                messageContext.setProperty(APIConstants.ACCESSED_FIELDS, fieldUsage);

                graphQLMetricsHandler = new GraphQLAnalyticsMetricsHandler();
                if (type.equals(APIConstants.QUERY)){
                    graphQLMetricsHandler.handleQuery(messageContext);
                } else if (type.equals(APIConstants.MUTATION)) {
                    String operationName = ((Field) selection).getName();
                    List<Map<String, Object>> mutatedFields = GraphQLOperationAnalyzer.getMutatedFields(type, selection,
                            graphQLSchema, operationName, variablesMap);
                    messageContext.setProperty(APIConstants.MUTATED_FIELDS, mutatedFields);
                    graphQLMetricsHandler.handleMutation(messageContext);
                } else if (type.equals(APIConstants.SUBSCRIPTION)) {
                    graphQLMetricsHandler.handleSubscribe(messageContext);
                }
            }
        }
    }
}
