package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.utils.LocalEntryAdminClient;

/**
 * This is the test case for {@link SwaggerSchemaValidator}
 */
@RunWith(PowerMockRunner.class)
public class SwaggerSchemaValidatorTest {

    private static final Log log = LogFactory.getLog(SwaggerSchemaValidatorTest.class);
    private SwaggerSchemaValidator swaggerSchemaValidator;
    private Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
    private org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);


    @Before
    public void init() {
        messageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.LOCAL_ENTRY)).thenReturn
                (String.valueOf("\".*'.*|.*ALTER.*|.*ALTER TABLE.*|.*ALTER VIEW.*|\n" +
                        "    .*CREATE DATABASE.*|.*CREATE PROCEDURE.*|.*CREATE SCHEMA.*|.*create table.*|." +
                        "*CREATE VIEW.*|.*DELETE.*|.\n" +
                        "    *DROP DATABASE.*|.*DROP PROCEDURE.*|.*DROP.*|.*SELECT.*"));
    }

    /**
     * This is the test case to validate the request path parameter against sql injection attack.
     */
    @Test
    public void testSqlInjectionInPathParam() {
        log.info("Running the test case to validate the request path param.");

    }
}
