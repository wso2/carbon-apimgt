/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.io.FileUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

import java.io.File;
import java.util.Map;

public class TestSchemaValidator {
    private static final Log log = LogFactory.getLog(TestSchemaValidator.class);
    private MessageContext messageContext;
    private org.apache.axis2.context.MessageContext axis2MsgContext;
    private SchemaValidator schemaValidator;
    private String uuid;
    SynapseConfiguration synapseConfiguration = Mockito.mock(SynapseConfiguration.class);
    Map map = Mockito.mock(Map.class);
    Entry entry = Mockito.mock(Entry.class);

    @Before
    public void init() {
        messageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MsgContext = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
    }

    @Test
    public void testValidRequest() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        OMElement messageStore = AXIOMUtil.stringToOM(
                "<jsonObject><id>0</id><category><id>0</id><name>string</name></category>" +
                        "<name>doggie</name><photoUrls>string</photoUrls><tags><id>0</id><name>string</name>" +
                        "</tags><status>available</status></jsonObject>" );
        env.getBody().addChild(messageStore);
        log.info(" Running the test case to validate the request content against the defined schemas.");
        String contentType = "application/json";
        File swaggerJsonFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("swaggerEntry/swagger.json").getFile());
        String swaggerValue = FileUtils.readFileToString(swaggerJsonFile);
        Mockito.doReturn(env).when(messageContext).getEnvelope();
        // Mockito.when()

        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgContext);
        Mockito.when((String) axis2MsgContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE))
                .thenReturn(contentType);
        Mockito.when((String) axis2MsgContext.getProperty(APIMgtGatewayConstants.HTTP_REQUEST_METHOD)).
                thenReturn("POST");
        Mockito.when((String)messageContext.getProperty((APIMgtGatewayConstants.API_ELECTED_RESOURCE))).
                thenReturn("/pet");
        Mockito.when((String)messageContext.getProperty(APIMgtGatewayConstants.ELECTED_REQUEST_METHOD)).
                thenReturn("POST");
        Mockito.when((String) axis2MsgContext.getProperty(APIMgtGatewayConstants.HTTP_REQUEST_METHOD)).
                thenReturn("POST");

        Mockito.when(messageContext.getConfiguration()).thenReturn(synapseConfiguration);
        Mockito.when(synapseConfiguration.getLocalRegistry()).thenReturn(map);
        Mockito.when(map.get(uuid)).thenReturn(entry);
        Mockito.when((String)messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_NAME)).thenReturn(swaggerValue);
    }

    @Test
    public void testBadRequest() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        OMElement messageStore = AXIOMUtil.stringToOM("<jsonObject><id>0</id><category>" +
                "<id>dededededededed</id><name>string</name></category><name>doggie</name><photoUrls>" +
                "string</photoUrls><tags><id>0</id><name>string</name></tags><status>available</status></jsonObject>");
        env.getBody().addChild(messageStore);
        log.info(" Running the test case to validate the request content against the defined schemas.");
        String contentType = "application/json";
        String ApiId = "admin-SwaggerPetstore-1.0.0";
        File swaggerJsonFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("swaggerEntry/swagger.json").getFile());
        String swaggerValue = FileUtils.readFileToString(swaggerJsonFile);


        Mockito.doReturn(env).when(messageContext).getEnvelope();
        // Mockito.when()

        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgContext);
        Mockito.when((String) axis2MsgContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE))
                .thenReturn(contentType);
        Mockito.when((String) axis2MsgContext.getProperty(APIMgtGatewayConstants.HTTP_REQUEST_METHOD)).
                thenReturn("POST");
        Mockito.when(messageContext.getConfiguration()).thenReturn(synapseConfiguration);
        Mockito.when(synapseConfiguration.getLocalRegistry()).thenReturn(map);
        Mockito.when(map.get(uuid)).thenReturn(entry);

        Mockito.when(messageContext.getConfiguration()).thenReturn(synapseConfiguration);
        Mockito.when((String) messageContext.getProperty((APIMgtGatewayConstants.API_ELECTED_RESOURCE))).
                thenReturn("/pet");
        Mockito.when(synapseConfiguration.getLocalRegistry()).thenReturn(map);
        Mockito.when(map.get(ApiId)).thenReturn(entry);
        Mockito.when((String) entry.getValue()).thenReturn(swaggerValue);
        Mockito.when((String) messageContext.getProperty(APIMgtGatewayConstants.ELECTED_REQUEST_METHOD)).
                thenReturn("POST");
        Mockito.when((String) axis2MsgContext.getProperty(APIMgtGatewayConstants.HTTP_REQUEST_METHOD)).
                thenReturn("POST");
        Mockito.when((String)messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_NAME)).thenReturn(swaggerValue);
    }

}
