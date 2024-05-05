package org.wso2.carbon.apimgt.gateway.handlers.graphQL.analytics;

import graphql.language.*;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.common.gateway.graphql.GraphQLOperationAnalyzer;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.*;

public class GraphQLOperationInfoAnalyzer {

    private GraphQLAnalyticsMetricsHandler graphQLMetricsHandler;

    public void analyzePayload(MessageContext messageContext){

        Parser parser = new Parser();
        String payload = (String) messageContext.getProperty(APIConstants.GRAPHQL_PAYLOAD);
        Document document = parser.parseDocument(payload);
        GraphQLSchema graphQLSchema = (GraphQLSchema) messageContext.getProperty(APIConstants.GRAPHQL_SCHEMA);
        TypeDefinitionRegistry typeDefinition = (TypeDefinitionRegistry) messageContext.getProperty(APIConstants.TYPE_DEFINITION);
        HashMap<String, Object> variablesMap = (HashMap<String, Object>) messageContext.getProperty("VARIABLE_MAP");
        String complexityInfoJson = (String) messageContext
                .getProperty(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY);
        String type = null;

        for (Definition definition : document.getDefinitions()) {
            OperationDefinition operation = (OperationDefinition) definition;
            type = operation.getOperation().toString().toLowerCase(Locale.ROOT);
            for (Selection selection : operation.getSelectionSet().getSelections()) {
                String result = AstPrinter.printAst(selection);
                Document subDocument = parser.parseDocument( type + " Name { \n " + result + "\n }");

                Map<String, Object> operationInfo = GraphQLOperationAnalyzer
                        .getOperationInfo(type, selection, subDocument,graphQLSchema, typeDefinition, complexityInfoJson, variablesMap);
                messageContext.setProperty("FIELD_USAGE", operationInfo);

                List<Map> fieldUsage = GraphQLOperationAnalyzer.getUsedFields(selection);
                messageContext.setProperty("ACCESSED_FIELDS", fieldUsage);

                graphQLMetricsHandler = new GraphQLAnalyticsMetricsHandler();
                if (type.equals("query")){
                    graphQLMetricsHandler.handleQuery(messageContext);
                } else if (type.equals("mutation")) {
                    String operationName = ((Field) selection).getName();
                    List<Map<String, Object>> mutatedFields = GraphQLOperationAnalyzer.getMutatedFields(type, selection,
                            graphQLSchema, operationName, variablesMap);
                    messageContext.setProperty("MUTATED_FIELDS", mutatedFields);
                    graphQLMetricsHandler.handleMutation(messageContext);
                } else if (type.equals("subscription")) {
                    graphQLMetricsHandler.handleSubscribe(messageContext);
                }
            }
        }
    }
}
