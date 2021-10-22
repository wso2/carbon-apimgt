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
package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.common.gateway.graphql.FieldComplexityCalculatorImpl;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * This Class can be used to calculate fields complexity values of GraphQL Query.
 */
public class GraphQLComplexityCalculator extends FieldComplexityCalculatorImpl {
    private static final Log log = LogFactory.getLog(GraphQLComplexityCalculator.class);

    public GraphQLComplexityCalculator(MessageContext messageContext) {
        try {
            String graphQLAccessControlPolicy = (String) messageContext
                    .getProperty(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY);
            if (graphQLAccessControlPolicy == null) {
                policyDefinition = new JSONObject();
            } else {
                JSONObject jsonObject = (JSONObject) jsonParser.parse(graphQLAccessControlPolicy);
                policyDefinition = (JSONObject) jsonObject.get(APIConstants.QUERY_ANALYSIS_COMPLEXITY);
            }

        } catch (ParseException e) {
            String errorMessage = "Policy definition parsing failed. ";
            handleFailure(messageContext, errorMessage, errorMessage);
        }
    }

    /**
     * This method handle the failure
     *
     * @param messageContext   message context of the request
     * @param errorMessage     error message of the failure
     * @param errorDescription error description of the failure
     */
    private void handleFailure(MessageContext messageContext, String errorMessage, String errorDescription) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, GraphQLConstants.GRAPHQL_INVALID_QUERY);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION, errorDescription);
        Mediator sequence = messageContext.getSequence(GraphQLConstants.GRAPHQL_API_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, HttpStatus.SC_BAD_REQUEST);
    }


}