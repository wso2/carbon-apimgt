package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import graphql.analysis.FieldComplexityCalculator;
import graphql.analysis.FieldComplexityEnvironment;
import graphql.language.Argument;
import graphql.language.IntValue;
import graphql.language.Value;
import graphql.schema.CoercingParseLiteralException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This Class can be used to calculate fields complexity values of GraphQL Query.
 */
public class FieldComplexityCalculatorImpl implements FieldComplexityCalculator {
    private static final Log log = LogFactory.getLog(FieldComplexityCalculatorImpl.class);
    JSONParser jsonParser = new JSONParser();
    JSONObject policyDefinition;

    public FieldComplexityCalculatorImpl(MessageContext messageContext) {
        try {
            String graphQLAccessControlPolicy = (String) messageContext.getProperty(APIConstants.GRAPHQL_ACCESS_CONTROL_POLICY);
            JSONObject jsonObject = (JSONObject) jsonParser.parse(graphQLAccessControlPolicy);
            policyDefinition = (JSONObject) jsonObject.get(APIConstants.QUERY_ANALYSIS_COMPLEXITY);
        } catch (ParseException e) {
            String errorMessage = "Policy definition parsing failed. ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
    }

    @Override
    public int calculate(FieldComplexityEnvironment fieldComplexityEnvironment, int childComplexity) {
        String fieldName = fieldComplexityEnvironment.getField().getName();
        String parentType = fieldComplexityEnvironment.getParentType().getName();
        List<Argument> ArgumentList = fieldComplexityEnvironment.getField().getArguments();

        int argumentsValue = getArgumentsValue(ArgumentList);
        int customFieldComplexity = getCustomComplexity(fieldName, parentType, policyDefinition);
        return (argumentsValue * (customFieldComplexity + childComplexity));
    }

    private int getCustomComplexity(String fieldName, String parentType, JSONObject policyDefinition) {
        JSONObject customComplexity = (JSONObject) policyDefinition.get(parentType);
        if (customComplexity != null && customComplexity.get(fieldName) != null) {
            //TODO:chnge as interger
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
                BigInteger value = ((IntValue) object.getValue()).getValue();
                int val = value.intValue();
                argumentValue = argumentValue + val;
            }
        } else {
            argumentValue = 1;
        }
        return argumentValue;
    }
}