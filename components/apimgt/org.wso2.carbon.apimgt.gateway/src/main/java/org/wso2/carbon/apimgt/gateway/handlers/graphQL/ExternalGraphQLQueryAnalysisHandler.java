package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.common.gateway.dto.ExternalQueryAnalyzerResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.graphql.ExternalQueryAnalyzer;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Handler to analyze incoming GraphQL queries using an external security analyzer.
 */
public class ExternalGraphQLQueryAnalysisHandler extends AbstractHandler
{
    private static final Log log = LogFactory.getLog(ExternalGraphQLQueryAnalysisHandler.class);

    @Override
    public boolean handleRequest(MessageContext messageContext)
    {
        if (Utils.isGraphQLSubscriptionRequest(messageContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping GraphQL subscription handshake request.");
            }
            return true;
        }

        Object queryObject = messageContext.getProperty(APIConstants.GRAPHQL_PAYLOAD);
        if (queryObject == null) {
            log.warn("GraphQL query is missing in the message context.");
            return true;
        }
        String query = queryObject.toString();

        ExternalQueryAnalyzer externalQueryAnalyzer = new ExternalQueryAnalyzer();
        ExternalQueryAnalyzerResponseDTO response = externalQueryAnalyzer.analyseQuery(query);

        if (response.isVulnerable()) {
            log.warn("Vulnerable GraphQL query detected by external analyzer.");
            log.warn("Identified vulnerabilities: " + response.getVulList().toString());
        }

        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext)
    {
        return true;
    }
}
