package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import graphql.analysis.FieldComplexityCalculator;
import graphql.analysis.FieldComplexityEnvironment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.impl.APIConstants;

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
            log.error("Policy definition parsing failed. " + e.getMessage(), e);
        }
    }

    @Override
    public int calculate(FieldComplexityEnvironment fieldComplexityEnvironment, int childComplexity) {
        String fieldName = fieldComplexityEnvironment.getField().getName();
        String parentType = fieldComplexityEnvironment.getParentType().getName();
        int customFieldComplexity = getCustomComplexity(fieldName, parentType, policyDefinition);
        return (customFieldComplexity + childComplexity);
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
}