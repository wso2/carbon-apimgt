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
package org.wso2.carbon.apimgt.common.gateway.graphql;

import graphql.analysis.FieldComplexityCalculator;
import graphql.analysis.FieldComplexityEnvironment;
import graphql.language.Argument;
import graphql.language.IntValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

/**
 * Implementation for GraphQL query field complexity calculator.
 */
public class FieldComplexityCalculatorImpl implements FieldComplexityCalculator {

    private static final Log log = LogFactory.getLog(FieldComplexityCalculatorImpl.class);
    protected JSONParser jsonParser = new JSONParser();
    protected JSONObject policyDefinition;

    public FieldComplexityCalculatorImpl(String accessControlPolicy) throws ParseException {
        if (accessControlPolicy == null) {
            policyDefinition = new JSONObject();
        } else {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(accessControlPolicy);
            policyDefinition = (JSONObject) jsonObject.get(GraphQLConstants.QUERY_ANALYSIS_COMPLEXITY);
        }
    }

    /**
     * Calculate complexity.
     *
     * @param fieldComplexityEnvironment FieldComplexityEnvironment
     * @param childComplexity            Child Complexity value
     */
    @Override
    public int calculate(FieldComplexityEnvironment fieldComplexityEnvironment, int childComplexity) {

        String fieldName = fieldComplexityEnvironment.getField().getName();
        String parentType = fieldComplexityEnvironment.getParentType().getName();
        List<Argument> argumentList = fieldComplexityEnvironment.getField().getArguments();

        int argumentsValue = getArgumentsValue(argumentList);
        int customFieldComplexity = getCustomComplexity(fieldName, parentType, policyDefinition);
        return (argumentsValue * (customFieldComplexity + childComplexity));
    }

    private int getCustomComplexity(String fieldName, String parentType, JSONObject policyDefinition) {

        JSONObject customComplexity = (JSONObject) policyDefinition.get(parentType);
        if (customComplexity != null && customComplexity.get(fieldName) != null) {
            return ((Long) customComplexity.get(fieldName)).intValue(); // Returns custom complexity value
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No custom complexity value was assigned for " + fieldName + " under type " + parentType);
            }
            return 1; // Returns default complexity value
        }
    }

    private int getArgumentsValue(List<Argument> argumentList) {

        int argumentValue = 0;
        if (argumentList.size() > 0) {
            for (Argument object : argumentList) {
                String argumentName = object.getName();
                // The below list of slicing arguments (keywords) effect query complexity to multiply by the factor
                // given as the value of the argument.
                List<String> slicingArguments = GraphQLConstants.QUERY_COMPLEXITY_SLICING_ARGS;
                if (slicingArguments.contains(argumentName.toLowerCase(Locale.ROOT))) {
                    BigInteger value = null;
                    if (object.getValue() instanceof IntValue) {
                        value = ((IntValue) object.getValue()).getValue();
                    }
                    int val = 0;
                    if (value != null) {
                        val = value.intValue();
                    }
                    argumentValue = argumentValue + val;
                } else {
                    argumentValue += 1;
                }
            }
        } else {
            argumentValue = 1;
        }
        return argumentValue;
    }
}
