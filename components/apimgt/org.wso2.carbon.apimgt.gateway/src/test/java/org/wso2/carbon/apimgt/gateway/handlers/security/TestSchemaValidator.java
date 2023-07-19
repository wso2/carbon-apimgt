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

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.Handler;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

public class TestSchemaValidator {
    private static final Log log = LogFactory.getLog(TestSchemaValidator.class);
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_LOWERCASE = "content-type";
    private static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    private static final String RESOURCE_TAG = "api.ut.resource";
    private MessageContext messageContext;
    private org.apache.axis2.context.MessageContext axis2MsgContext;
    private Handler schemaValidator = new SchemaValidator();
    SynapseConfiguration synapseConfiguration = Mockito.mock(SynapseConfiguration.class);
    Map map = Mockito.mock(Map.class);
    Entry entry = Mockito.mock(Entry.class);

    @BeforeClass
    public static void init() {

    }

    @Before
    public void before() {
        messageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MsgContext = Mockito.mock(org.apache.axis2.context.MessageContext.class);
    }

    @Test
    public void testValidRequestPostPet() throws IOException, XMLStreamException {
        // Happy Path: Valid Pet
        setMockedRequest("POST", "/pet", "/pet", "<jsonObject>" +
                "<id>123</id><name>Doggie</name>" +
                "<photoUrls>https://mydog_1.jpg</photoUrls><photoUrls>https://mydog_2.jpg</photoUrls>" +
                "<category><id>2</id><name>dog</name></category>" +
                "<tags><id>12</id><name>Black</name></tags><tags><id>43</id><name>German Shepherd</name></tags>" +
                "<status>available</status>" +
                "</jsonObject>");
        assertValidRequest();
    }

    @Test
    public void testValidRequestPostStoreOrder() throws IOException, XMLStreamException {
        // Happy Path: Valid Store Order: Valid date
        setMockedRequest("POST", "/store/order", "/store/order", "<jsonObject>" +
                "<id>123</id><petId>22</petId><quantity>8</quantity><shipDate>2020-05-14T10:29:24.160Z</shipDate>" +
                "<status>placed</status><complete>false</complete>" +
                "</jsonObject>");
        assertValidRequest();
    }

    @Test
    public void testValidRequestPostArrayOfUsers() throws IOException, XMLStreamException {
        // Happy Path: Valid Array of Users
        setMockedRequest("POST", "/user/createWithArray", "/user/createWithArray",
                "<jsonArray><jsonElement>" +
                "<id>1234</id><username>andy</username><firstName>Andy</firstName>" +
                "<lastName>Fernando</lastName><email>andy@abc.com</email><password>pw1234</password>" +
                "<phone>01234567890</phone><userStatus>3</userStatus></jsonElement>" +
                "<jsonElement><id>2345</id><username>ann</username><firstName>Ann</firstName>" +
                "<lastName>Fernando</lastName><email>ann@abc.com</email><password>pw2233</password>" +
                "<phone>01234567890</phone><userStatus>5</userStatus>" +
                "</jsonElement></jsonArray>");
        assertValidRequest();
    }

    @Test
    public void testBadRequestInvalidEnum() throws IOException, XMLStreamException {
        // Invalid Enum
        setMockedRequest("POST", "/pet", "/pet", "<jsonObject>" +
                "<id>123</id><name>Doggie</name>" +
                "<photoUrls>https://mydog_1.jpg</photoUrls><photoUrls>https://mydog_2.jpg</photoUrls>" +
                "<category><id>2</id><name>dog</name></category>" +
                "<tags><id>12</id><name>Black</name></tags><tags><id>43</id><name>German Shepherd</name></tags>" +
                "<status>INVALID-ENUM</status>" +
                "</jsonObject>");
        assertBadRequest();
    }

    @Test
    public void testBadRequestInvalidInt() throws IOException, XMLStreamException {
        // Invalid Int
        setMockedRequest("POST", "/pet", "/pet", "<jsonObject>" +
                "<id>INVALID-INT</id><name>Doggie</name>" +
                "<photoUrls>https://mydog_1.jpg</photoUrls><photoUrls>https://mydog_2.jpg</photoUrls>" +
                "<category><id>2</id><name>dog</name></category>" +
                "<tags><id>12</id><name>Black</name></tags><tags><id>43</id><name>German Shepherd</name></tags>" +
                "<status>available</status>" +
                "</jsonObject>");
        assertBadRequest();
    }

    @Test
    public void testBadRequestInvalidString() throws IOException, XMLStreamException {
        // Invalid String: name of pet
        setMockedRequest("POST", "/pet", "/pet", "<jsonObject>" +
                "<id>123</id><name>1234</name>" +
                "<photoUrls>https://mydog_1.jpg</photoUrls><photoUrls>https://mydog_2.jpg</photoUrls>" +
                "<category><id>2</id><name>dog</name></category>" +
                "<tags><id>12</id><name>Black</name></tags><tags><id>43</id><name>German Shepherd</name></tags>" +
                "<status>available</status>" +
                "</jsonObject>");
        assertBadRequest();
    }

    @Test
    public void testBadRequestInvalidDate() throws IOException, XMLStreamException {
        // Invalid date
        setMockedRequest("POST", "/store/order", "/store/order", "<jsonObject>" +
                "<id>123</id><petId>22</petId><quantity>8</quantity><shipDate>2020-05-14</shipDate>" +
                "<status>placed</status><complete>false</complete>" +
                "</jsonObject>");
        assertBadRequest();
    }

    @Test
    public void testBadRequestMissRequiredField() throws IOException, XMLStreamException {
        // Missing required field - Name of Pet
        setMockedRequest("POST", "/pet", "/pet", "<jsonObject>" +
                "<id>123</id>" +
                "<photoUrls>https://mydog_1.jpg</photoUrls><photoUrls>https://mydog_2.jpg</photoUrls>" +
                "<category><id>2</id><name>dog</name></category>" +
                "<tags><id>12</id><name>Black</name></tags><tags><id>43</id><name>German Shepherd</name></tags>" +
                "<status>available</status>" +
                "</jsonObject>");
        assertBadRequest();
    }

    @Test
    public void testLowercaseContentType() throws IOException, XMLStreamException {
        // lowercase content-type header
        Map<String, String> headers = new HashMap<>();
        String contentType = "application/json";
        headers.put(CONTENT_TYPE_HEADER_LOWERCASE, contentType);
        Mockito.when(axis2MsgContext.getProperty(TRANSPORT_HEADERS)).thenReturn(headers);
        setMockedRequest("POST", "/pet", "/pet", "<jsonObject>" +
                "<id>1</id><name>Doggie</name>" +
                "<photoUrls>https://mydog_1.jpg</photoUrls><photoUrls>https://mydog_2.jpg</photoUrls>" +
                "<category><id>2</id><name>dog</name></category>" +
                "<tags><id>12</id><name>Black</name></tags><tags><id>43</id><name>German Shepherd</name></tags>" +
                "<status>available</status>" +
                "</jsonObject>");

        assertValidRequest();
    }

    @Test
    public void testValidOneOfSchema1() throws IOException, XMLStreamException {
        Map<String, String> headers = new HashMap<>();
        String contentType = "application/json";
        headers.put(CONTENT_TYPE_HEADER_LOWERCASE, contentType);
        Mockito.when(axis2MsgContext.getProperty(TRANSPORT_HEADERS)).thenReturn(headers);
        setMockedRequestWithOpenApiYaml("POST", "/100/002/107066", "/100/002/107066",
                "<jsonObject>" +
                "<firstName>John</firstName><lastName>Doe</lastName><sport>Football</sport>" +
                "</jsonObject>");
        assertValidRequest();
    }

    @Test
    public void testValidOneOfSchema2() throws IOException, XMLStreamException {
        Map<String, String> headers = new HashMap<>();
        String contentType = "application/json";
        headers.put(CONTENT_TYPE_HEADER_LOWERCASE, contentType);
        Mockito.when(axis2MsgContext.getProperty(TRANSPORT_HEADERS)).thenReturn(headers);
        setMockedRequestWithOpenApiYaml("POST", "/100/002/107066", "/100/002/107066",
                "<jsonObject>" +
                "<vehicle>Car</vehicle><price>10</price>" +
                "</jsonObject>");
        assertValidRequest();
    }

    @Test
    public void testInvalidOneOfSchema() throws IOException, XMLStreamException {
        Map<String, String> headers = new HashMap<>();
        String contentType = "application/json";
        headers.put(CONTENT_TYPE_HEADER_LOWERCASE, contentType);
        Mockito.when(axis2MsgContext.getProperty(TRANSPORT_HEADERS)).thenReturn(headers);
        setMockedRequestWithOpenApiYaml("POST", "/100/002/107066", "/100/002/107066",
                "<jsonObject>" +
                "<type>Computer</type><model>Desktop</model>" +
                "</jsonObject>");
        assertBadRequest();
    }

    @Test
    public void testNonAllowedAdditionalProperties() throws IOException, XMLStreamException {
        Map<String, String> headers = new HashMap<>();
        String contentType = "application/json";
        headers.put(CONTENT_TYPE_HEADER_LOWERCASE, contentType);
        Mockito.when(axis2MsgContext.getProperty(TRANSPORT_HEADERS)).thenReturn(headers);
        setMockedRequestWithOpenApiYaml("POST", "/100/002/107066", "/100/002/107066",
                "<jsonObject>" +
                "<firstName>John</firstName><lastName>Doe</lastName><sport>Football</sport><age>Twenty</age>" +
                "</jsonObject>");
        assertBadRequest();
    }

    @Test
    public void testAllowedAdditionalProperties() throws IOException, XMLStreamException {
        Map<String, String> headers = new HashMap<>();
        String contentType = "application/json";
        headers.put(CONTENT_TYPE_HEADER_LOWERCASE, contentType);
        Mockito.when(axis2MsgContext.getProperty(TRANSPORT_HEADERS)).thenReturn(headers);
        setMockedRequestWithOpenApiYaml("POST", "/100/002/107066", "/100/002/107066",
                "<jsonObject>" +
                "<vehicle>Car</vehicle><price>10</price><color>Red</color>" +
                "</jsonObject>");
        assertValidRequest();
    }

    @Test
    public void testValidRequestResourceWithOnlySlash() throws IOException, XMLStreamException {
        // Happy path - testing resource with only slash
        setMockedRequest("POST", "/", "/","<jsonObject>" +
                "<pet>dog</pet>" +
                "</jsonObject>");
        assertValidRequest();
    }

    @Test
    public void testBadRequestResourceWithOnlySlash() throws IOException, XMLStreamException {
        // Missing required field - pet tag
        setMockedRequest("POST", "/", "/", "<jsonObject>" +
                "<pets>dog</pets>" +
                "</jsonObject>");
        assertBadRequest();
    }

    @Test
    public void testValidRequestResourceWithTrailingSlash() throws IOException, XMLStreamException {
        // Happy path - testing resource with trailing slash
        setMockedRequest("POST", "/pet/sample", "/pet/sample/", "<jsonObject>" +
                "<pet>dog</pet>" +
                "</jsonObject>");
        assertValidRequest();
    }

    @Test
    public void testBadRequestResourceWithTrailingSlash() throws IOException, XMLStreamException {
        // Missing required field - pet tag
        setMockedRequest("POST", "/pet/sample", "/pet/sample/", "<jsonObject>" +
                "<pets>dog</pets>" +
                "</jsonObject>");
        assertBadRequest();
    }

    private void assertValidRequest() {
        Assert.assertTrue(schemaValidator.handleRequest(messageContext));
        Mockito.verify(messageContext, Mockito.times(0))
                .setProperty(APIMgtGatewayConstants.THREAT_FOUND, true);
    }

    private void assertBadRequest() {
        schemaValidator.handleRequest(messageContext);
        Mockito.verify(messageContext).setProperty(APIMgtGatewayConstants.THREAT_FOUND, true);
    }

    private void setMockedRequestWithOpenApiYaml(String httpMethod, String resourcePath, String subPath,
                                                 String xmlMessage) throws IOException, XMLStreamException {
        File openApiYamlFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("swaggerEntry/openapi.yaml").getFile());
        String swaggerValue = FileUtils.readFileToString(openApiYamlFile);
        setMockedRequest(httpMethod, resourcePath, subPath, xmlMessage, swaggerValue);
    }

    private void setMockedRequest(String httpMethod, String resourcePath, String subPath, String xmlMessage)
            throws XMLStreamException, IOException {
        File swaggerJsonFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("swaggerEntry/swagger.json").getFile());
        String swaggerValue = FileUtils.readFileToString(swaggerJsonFile);
        setMockedRequest(httpMethod, resourcePath, subPath, xmlMessage, swaggerValue);
    }

    private void setMockedRequest(String httpMethod, String resourcePath, String subPath, String xmlMessage, String swaggerValue)
            throws XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        OMElement messageStore = AXIOMUtil.stringToOM(xmlMessage);
        env.getBody().addChild(messageStore);
        log.info(" Running the test case to validate the request content against the defined schemas.");
        String contentType = "application/json";
        String ApiId = "admin-SwaggerPetstore-1.0.0";

        OpenAPIParser parser = new OpenAPIParser();
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolveFully(true);
        OpenAPI openAPI = parser.readContents(swaggerValue, null, parseOptions).getOpenAPI();

        Mockito.doReturn(env).when(messageContext).getEnvelope();
        // Mockito.when()

        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgContext);
        Mockito.when((String) axis2MsgContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE))
                .thenReturn(contentType);
        Mockito.when((String) axis2MsgContext.getProperty(APIMgtGatewayConstants.HTTP_REQUEST_METHOD)).
                thenReturn(httpMethod);
        Mockito.when(messageContext.getConfiguration()).thenReturn(synapseConfiguration);
        Mockito.when(synapseConfiguration.getLocalRegistry()).thenReturn(map);

        Mockito.when(messageContext.getConfiguration()).thenReturn(synapseConfiguration);
        Mockito.when((String) messageContext.getProperty((APIMgtGatewayConstants.API_ELECTED_RESOURCE))).
                thenReturn(resourcePath);
        Mockito.when((String) messageContext.getProperty(RESTConstants.REST_SUB_REQUEST_PATH)).
                thenReturn(subPath);
        Mockito.when(synapseConfiguration.getLocalRegistry()).thenReturn(map);
        Mockito.when(map.get(ApiId)).thenReturn(entry);
        Mockito.when((String) entry.getValue()).thenReturn(swaggerValue);
        Mockito.when((String) messageContext.getProperty(APIMgtGatewayConstants.ELECTED_REQUEST_METHOD)).
                thenReturn(httpMethod);
        Mockito.when((String) axis2MsgContext.getProperty(APIMgtGatewayConstants.HTTP_REQUEST_METHOD)).
                thenReturn(httpMethod);
        Mockito.when((String) messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_STRING))
                .thenReturn(swaggerValue);
        Mockito.when((OpenAPI) messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT))
                .thenReturn(openAPI);
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE_HEADER, contentType);
        Mockito.when(axis2MsgContext.getProperty(TRANSPORT_HEADERS)).thenReturn(headers);
        Mockito.when(messageContext.getProperty(RESOURCE_TAG)).thenReturn(resourcePath);
        Mockito.when((String) messageContext.getProperty((RESTConstants.REST_FULL_REQUEST_PATH))).
                thenReturn(resourcePath);
    }
}
