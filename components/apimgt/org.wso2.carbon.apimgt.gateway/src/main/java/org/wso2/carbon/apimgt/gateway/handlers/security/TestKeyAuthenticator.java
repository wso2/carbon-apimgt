package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

public class TestKeyAuthenticator extends AbstractHandler  {

    private static final Log logger = LogFactory.getLog(SchemaValidator.class);
    private String testKey;

    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        logger.debug("Validating the API request header for the test console request authentication");
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        axis2MC.getProperty((APIMgtGatewayConstants.TRANSPORT_HEADERS));
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }
}
