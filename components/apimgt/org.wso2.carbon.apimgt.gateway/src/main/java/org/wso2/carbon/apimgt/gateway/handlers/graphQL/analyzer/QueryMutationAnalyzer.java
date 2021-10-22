/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.graphQL.analyzer;

import graphql.schema.GraphQLSchema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.apimgt.common.gateway.graphql.QueryAnalyzer;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.graphQL.GraphQLConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.List;

public class QueryMutationAnalyzer extends QueryAnalyzer {

    private static final Log log = LogFactory.getLog(QueryMutationAnalyzer.class);

    public QueryMutationAnalyzer(GraphQLSchema schema) {
        super(schema);
    }

    /**
     * This method analyses the query depth
     *
     * @param messageContext message context of the request
     * @param payload        payload of the request
     * @return true, if the query depth does not exceed the maximum value or false, if query depth exceeds the maximum
     */
    public boolean analyseQueryDepth(MessageContext messageContext, String payload) {

        List<String> errorList = new ArrayList<>();
        int maxQueryDepth = getMaxQueryDepth(messageContext);

        if (!super.analyseQueryDepth(maxQueryDepth, payload, errorList) && !errorList.isEmpty()) {
            handleFailure(GraphQLConstants.GRAPHQL_QUERY_TOO_DEEP, messageContext,
                    GraphQLConstants.GRAPHQL_QUERY_TOO_DEEP_MESSAGE, errorList.toString());
            log.error(errorList.toString());
            return false;
        }
        return true;
    }

    /**
     * This method returns the maximum query complexity value
     *
     * @param messageContext message context of the request
     * @return maximum query depth value if exists, or -1 to denote no complexity limitation
     */
    private int getMaxQueryDepth(MessageContext messageContext) {
        Object maxQueryDepth = messageContext.getProperty(APIConstants.MAXIMUM_QUERY_DEPTH);
        if (maxQueryDepth != null) {
            int maxDepth = ((Integer) maxQueryDepth).intValue();
            if (maxDepth > 0) {
                return maxDepth;
            } else {
                log.debug("Maximum query depth value is 0");
                return -1;
            }
        } else {
            log.debug("Maximum query depth not applicable");
            return -1;
        }
    }

    /**
     * This method handle the failure
     *
     * @param errorCodeValue   error code of the failure
     * @param messageContext   message context of the request
     * @param errorMessage     error message of the failure
     * @param errorDescription error description of the failure
     */
    private void handleFailure(int errorCodeValue, MessageContext messageContext, String errorMessage,
                               String errorDescription) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCodeValue);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDescription);
        Mediator sequence = messageContext.getSequence(GraphQLConstants.GRAPHQL_API_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, HttpStatus.SC_BAD_REQUEST);
    }
}
