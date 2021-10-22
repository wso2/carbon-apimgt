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

 * * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.common.gateway.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.schema.GraphQLSchema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class QueryAnalyzer {

    private static final Log log = LogFactory.getLog(QueryAnalyzer.class);
    private GraphQLSchema schema;

    public QueryAnalyzer(GraphQLSchema schema) {
        this.schema = schema;
    }

    /**
     * This method analyses the query depth
     *
     * @param payload        payload of the request
     * @return true, if the query depth does not exceed the maximum value or false, if query depth exceeds the maximum
     */
    public boolean analyseQueryDepth(int maxQueryDepth, String payload, List<String> errorList) {

        if (maxQueryDepth > 0) {
            MaxQueryDepthInstrumentation maxQueryDepthInstrumentation =
                    new MaxQueryDepthInstrumentation(maxQueryDepth);
            GraphQL runtime = GraphQL.newGraphQL(schema).instrumentation(maxQueryDepthInstrumentation).build();

            try {
                ExecutionResult executionResult = runtime.execute(payload);
                List<GraphQLError> errors = executionResult.getErrors();
                if (errors.size() > 0) {
                    for (GraphQLError error : errors) {
                        errorList.add(error.getMessage());
                    }
                    // TODO: https://github.com/wso2/carbon-apimgt/issues/8147
                    errorList.removeIf(s -> s.contains("non-nullable"));
                    if (errorList.size() == 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Maximum query depth of " + maxQueryDepth + " was not exceeded");
                        }
                        return true;
                    }
                    log.error(errorList.toString());
                    return false;
                }
                return true;
            } catch (Throwable e) {
                log.error(e);
            }
        } else {
            return true; // No depth limitation check
        }
        return false;
    }

}
