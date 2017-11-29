/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the test case for {@link RegularExpressionProtector}
 */
@RunWith(PowerMockRunner.class)
public class RegularExpressionProtectorTest {

    private static final Log log = LogFactory.getLog(RegularExpressionProtectorTest.class);
    private RegularExpressionProtector regularExpressionProtector;
    private MessageContext messageContext;
    private org.apache.axis2.context.MessageContext axis2MsgContext;
    private String enabledStatus = "true";


    @Before
    public void init() {
        messageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MsgContext = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.REGEX_PATTERN)).thenReturn
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
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.THREAT_TYPE)).thenReturn
                (String.valueOf("SQL-Injection"));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_PATHPARAM)).thenReturn
                (String.valueOf(enabledStatus));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_HEADERS)).thenReturn
                (String.valueOf("false"));
        Mockito.when(axis2MsgContext.getProperty(APIMgtGatewayConstants.REST_URL_POSTFIX)).thenReturn
                (String.valueOf("/drop"));
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgContext);
        regularExpressionProtector = new RegularExpressionProtector();
        regularExpressionProtector.mediate(messageContext);
    }

    /**
     * This is the test case to validate the request header against sql injection attack.
     */
    @Test
    public void testSqlInjectionInHeaders() {
        log.info("Running the test case to validate the request header from sql injection attacks.");
        Map<String, String> transportHeaders = new HashMap<>();
        String acceptHeader = "application/json";
        String acceptLanguage = "en-US,en;q=drop";
        String userAgent = "aaa' or 1/*";
        transportHeaders.put(HttpHeaders.ACCEPT, acceptHeader);
        transportHeaders.put(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage);
        transportHeaders.put(HttpHeaders.USER_AGENT, userAgent);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_HEADERS)).thenReturn
                (String.valueOf(enabledStatus));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_PATHPARAM)).thenReturn
                (String.valueOf("false"));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_BODY)).thenReturn
                (String.valueOf("false"));
        Mockito.when(axis2MsgContext.getProperty(APIMgtGatewayConstants.TRANSPORT_HEADERS)).thenReturn
                (transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgContext);
        regularExpressionProtector = new RegularExpressionProtector();
        regularExpressionProtector.mediate(messageContext);
    }

    /**
     * This is the test case to validate the request body against sql injection attack.
     */
    @Test
    public void testSqlInjectionInBody() throws AxisFault {
        log.info(" Running the test case to validate the request body from sql injection attacks.");
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        env.getBody().addChild(fac.createOMElement("test", "Drop database", "testBody"));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_BODY)).thenReturn
                (String.valueOf(enabledStatus));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_HEADERS)).thenReturn
                (String.valueOf("false"));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_PATHPARAM)).thenReturn
                (String.valueOf("false"));
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgContext);
        Mockito.doReturn(env).when(axis2MsgContext).getEnvelope();
        regularExpressionProtector = new RegularExpressionProtector();
        regularExpressionProtector.mediate(messageContext);
    }

    /**
     * This is the test case to check the return value of the isContentAware method.
     */
    @Test
    public void testIsContentAware() {
        log.info("Running the test case to check the return status of the isContentAware method.");
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        env.getBody().addChild(fac.createOMElement("test", "Content aware", "MessageBody"));
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgContext);
        Mockito.doReturn(env).when(axis2MsgContext).getEnvelope();
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_BODY)).thenReturn
                (String.valueOf(enabledStatus));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_HEADERS)).thenReturn
                (String.valueOf("false"));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_PATHPARAM)).thenReturn
                (String.valueOf("false"));
        regularExpressionProtector = new RegularExpressionProtector();
        regularExpressionProtector.mediate(messageContext);
        String enabledBuild = String.valueOf(regularExpressionProtector.isContentAware());
        Assert.assertEquals(enabledStatus, enabledBuild);
        log.info("Successfully completed testIsContentAware test case.");
    }
}
