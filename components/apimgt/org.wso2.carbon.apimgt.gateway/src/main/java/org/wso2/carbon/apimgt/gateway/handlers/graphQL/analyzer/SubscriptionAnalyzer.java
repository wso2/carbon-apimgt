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
package org.wso2.carbon.apimgt.gateway.handlers.graphQL.analyzer;

import graphql.analysis.FieldComplexityCalculator;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.QueryAnalyzerResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.graphql.FieldComplexityCalculatorImpl;
import org.wso2.carbon.apimgt.common.gateway.graphql.QueryAnalyzer;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Base64;
import java.util.Set;

/**
 * QueryAnalyzer class extension for GraphQL subscription operations.
 */
public class SubscriptionAnalyzer extends QueryAnalyzer {

    private static final Log log = LogFactory.getLog(SubscriptionAnalyzer.class);

    public SubscriptionAnalyzer(GraphQLSchema schema) {
        super(schema);
    }

    /**
     * This method analyses the query depth.
     *
     * @param payload       payload of the request
     * @param maxQueryDepth Maximum query depth
     * @return true, if the query depth does not exceed the maximum value or false, if query depth exceeds the maximum
     */
    public QueryAnalyzerResponseDTO analyseSubscriptionQueryDepth(int maxQueryDepth, String payload) {

        int updatedMaxQueryDepth = getMaxQueryDepth(maxQueryDepth);
        return analyseQueryDepth(updatedMaxQueryDepth, payload);
    }

    /**
     * This method returns the maximum query complexity value.
     *
     * @param maxQueryDepth max query depth property from context.
     * @return maximum query depth value if exists, or -1 to denote no complexity limitation
     */
    private int getMaxQueryDepth(int maxQueryDepth) {

        if (maxQueryDepth > 0) {
            return maxQueryDepth;
        } else {
            log.debug("Maximum query depth value is 0");
            return 0;
        }
    }

    /**
     * This method analyses the query complexity.
     *
     * @param payload            Payload of the request
     * @param maxQueryComplexity Maximum query complexity
     * @return true, if query complexity does not exceed the maximum or false, if query complexity exceeds the maximum
     */
    public QueryAnalyzerResponseDTO analyseSubscriptionQueryComplexity(String payload, int maxQueryComplexity)
            throws APIManagementException {

        FieldComplexityCalculator fieldComplexityCalculator;
        try {
            //get access control policy
            String accessControlInfo = getGraphQLAccessControlInfo();
            fieldComplexityCalculator = new FieldComplexityCalculatorImpl(accessControlInfo);
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing policy definition.", e);
        }
        int updatedMaxQueryComplexity = getMaxQueryComplexity(maxQueryComplexity);
        return analyseQueryComplexity(updatedMaxQueryComplexity, payload,
                fieldComplexityCalculator);
    }

    /**
     * This method returns the maximum query complexity value.
     *
     * @param maxComplexity Max complexity
     * @return maximum query complexity value if exists, or -1 to denote no complexity limitation
     */
    private int getMaxQueryComplexity(int maxComplexity) {
        if (maxComplexity > 0) {
            return maxComplexity;
        } else {
            log.debug("Maximum query complexity value is 0");
            return 0;
        }
    }

    /**
     * Get GraphQL complexity access control information from schema.
     *
     * @return Access Control policy
     * @throws APIManagementException if an error occurs
     */
    private String getGraphQLAccessControlInfo() throws APIManagementException {
        String graphQLAccessControlPolicy;
        Set<GraphQLType> additionalTypes = getSchema().getAdditionalTypes();
        for (GraphQLType additionalType : additionalTypes) {
            String additionalTypeName = ((GraphQLObjectType) additionalType).getName();
            if (additionalTypeName.startsWith(APIConstants.GRAPHQL_ADDITIONAL_TYPE_PREFIX) &&
                    additionalTypeName.contains(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY)) {
                for (GraphQLFieldDefinition type : ((GraphQLObjectType) additionalType).getFieldDefinitions()) {
                    graphQLAccessControlPolicy = new String(Base64.getUrlDecoder().decode(type.getName()));
                    return graphQLAccessControlPolicy;
                }
            }
        }
        throw new APIManagementException(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY + " not found in schema");
    }
}
