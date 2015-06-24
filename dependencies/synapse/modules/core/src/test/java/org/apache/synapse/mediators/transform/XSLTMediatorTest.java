/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.mediators.transform;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.axiom.om.*;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.TestMessageContextBuilder;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.jaxp.*;
import org.apache.synapse.util.xpath.SynapseXPath;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;

public class XSLTMediatorTest extends TestCase {
    private static final Class[] sourceBuilderFactories = {
        DOOMSourceBuilderFactory.class,
        StreamSourceBuilderFactory.class,
        AXIOMSourceBuilderFactory.class };

    private static final Class[] resultBuilderFactories = {
        DOOMResultBuilderFactory.class,
        StreamResultBuilderFactory.class,
        AXIOMResultBuilderFactory.class };

    private static final String SOURCE =
        "<m0:CheckPriceRequest xmlns:m0=\"http://services.samples/xsd\">\n" +
        "<m0:Code>String</m0:Code>\n" +
        "</m0:CheckPriceRequest>";

    private static final String ENCLOSING_SOURCE =
        "<m:someOtherElement xmlns:m=\"http://someother\">" +
        SOURCE +
        "</m:someOtherElement>";

    private static final String SOURCE_DYNAMIC_KEY1 =
        "<m0:CheckPriceRequest xmlns:m0=\"http://services.samples/xsd\">\n" +
        "<m0:DynamicXsltKey1>DynamicXsltKey1</m0:DynamicXsltKey1>\n" +
        "</m0:CheckPriceRequest>\n" ;

    private static final String SOURCE_DYNAMIC_KEY2 =
        "<m0:CheckPriceRequest xmlns:m0=\"http://services.samples/xsd\">\n" +
        "<m0:DynamicXsltKey2>DynamicXsltKey2</m0:DynamicXsltKey2>\n" +
        "</m0:CheckPriceRequest>\n" ;

    private static final String SOURCE_STATIC_KEY =
        "<m0:CheckPriceRequest xmlns:m0=\"http://services.samples/xsd\">\n" +
        "<m0:StaticXsltKey>StaticXsltKey</m0:StaticXsltKey>\n" +
        "</m0:CheckPriceRequest>\n" ;

    // Create the test cases for the various transformer factories, source builders and
    // result builders dynamically:
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(XSLTMediatorTest.class);
        addGenericTests(suite, "Xalan", org.apache.xalan.processor.TransformerFactoryImpl.class);
        addGenericTests(suite, "Saxon", net.sf.saxon.TransformerFactoryImpl.class);
        return suite;
    }

    private static void addGenericTests(TestSuite suite, final String processorName,
            final Class<? extends TransformerFactory> transformerFactoryClass) {

        for (final Class sbf : sourceBuilderFactories) {
            for (final Class rbf : resultBuilderFactories) {
                String testName = "test" + processorName + shortName(sbf) + shortName(rbf);
                suite.addTest(new TestCase(testName) {
                    @Override
                    public void runTest() throws Throwable {
                        String oldTransformerFactory =
                            TransformerFactory.newInstance().getClass().getName();
                        System.setProperty(TransformerFactory.class.getName(),
                                transformerFactoryClass.getName());
                        test(sbf, rbf);
                        System.setProperty(TransformerFactory.class.getName(),
                                oldTransformerFactory);
                    }
                });
            }
        }
    }

    private static String shortName(Class clazz) {
        String name = clazz.getName();
        name = name.substring(name.lastIndexOf('.')+1);
        if (name.endsWith("BuilderFactory")) {
            name = name.substring(0, name.length()-14);
        }
        return name;
    }
    
    /**
     * Check that the provided element is the result of the XSL transformation of
     * SOURCE by the stylesheet transform_unittest.xslt.
     *
     * @param node result of the XSLT to be matched
     */
    private void assertQuoteElement(OMNode node) {
        assertTrue(node instanceof OMElement);
        OMElement element = (OMElement)node;

        assertTrue("GetQuote".equals(element.getLocalName()));
        assertTrue("http://www.webserviceX.NET/".equals(element.getNamespace().getNamespaceURI()));

        OMElement symbolElem = element.getFirstElement();
        assertTrue("symbol".equals(symbolElem.getLocalName()));
        assertTrue("http://www.webserviceX.NET/".equals(
                symbolElem.getNamespace().getNamespaceURI()));

        assertTrue("String".equals(symbolElem.getText()));
    }

    public void testTransformXSLTCustomSource() throws Exception {

        // create a new XSLT mediator
        XSLTMediator transformMediator = new XSLTMediator();

        // set xpath condition to select source
        SynapseXPath xpath = new SynapseXPath("//m0:CheckPriceRequest");
        xpath.addNamespace("m0", "http://services.samples/xsd");
        transformMediator.setSource(xpath);

        // set XSLT transformation URL
        setXsltTransformationURL(transformMediator, "xslt-key");

        MessageContext synCtx = new TestMessageContextBuilder().addFileEntry("xslt-key",
                "../../repository/conf/sample/resources/transform/transform_unittest.xslt")
                .setBodyFromString(SOURCE).addTextAroundBody().build();
        transformMediator.mediate(synCtx);

        // validate result
        assertQuoteElement(synCtx.getEnvelope().getBody().getFirstOMChild().getNextOMSibling());
    }

    /**
     * If a source element for transformation is not found, default to soap body
     * @throws Exception if there is an error in test
     */
    public void testTransformXSLTDefaultSource() throws Exception {

        // create a new xslt mediator
        XSLTMediator transformMediator = new XSLTMediator();

        // set XSLT transformation URL
        setXsltTransformationURL(transformMediator, "xslt-key");

        MessageContext synCtx = new TestMessageContextBuilder().addFileEntry("xslt-key",
                "../../repository/conf/sample/resources/transform/transform_unittest.xslt")
                .setBodyFromString(SOURCE).addTextAroundBody().build();
        transformMediator.mediate(synCtx);

        // validate result
        assertQuoteElement(synCtx.getEnvelope().getBody().getFirstOMChild().getNextOMSibling());
    }

    public void testTransformXSLTLargeMessagesCSV() throws Exception {

        // create a new switch mediator
        XSLTMediator transformMediator = new XSLTMediator();
        // set XSLT transformation URL
        setXsltTransformationURL(transformMediator, "xslt-key");

        for (int i=0; i<2; i++) {

            // invoke transformation, with static enveope
            MessageContext synCtx = new TestMessageContextBuilder().addFileEntry("xslt-key",
                    "../../repository/conf/sample/resources/transform/transform_load.xml")
                    .setBodyFromFile("../../repository/conf/sample/resources/transform/message.xml")
                    .addTextAroundBody().build();
            //MessageContext synCtx = TestUtils.getTestContextForXSLTMediator(SOURCE, props);
            transformMediator.mediate(synCtx);
//            synCtx.getEnvelope().serializeAndConsume(new FileOutputStream("/tmp/out.xml"));
//            System.gc();
//            System.out.println("done : " + i + " :: " + Runtime.getRuntime().freeMemory());
        }
    }

    public void testTransformXSLTLargeMessagesXML() throws Exception {

        // create a new switch mediator
        XSLTMediator transformMediator = new XSLTMediator();
        // set XSLT transformation URL
        setXsltTransformationURL(transformMediator, "xslt-key");
        for (int i=0; i<2; i++) {

            // invoke transformation, with static enveope
            MessageContext synCtx = new TestMessageContextBuilder().addFileEntry("xslt-key",
                    "../../repository/conf/sample/resources/transform/transform_load_3.xml")
                    .setBodyFromFile("../../repository/conf/sample/resources/transform/message.xml")
                    .addTextAroundBody().build();
            //MessageContext synCtx = TestUtils.getTestContextForXSLTMediator(SOURCE, props);
            transformMediator.mediate(synCtx);
//            System.gc();
//            System.out.println("done : " + i + " :: " + Runtime.getRuntime().freeMemory());
        }
    }

     public void testSynapse242() throws Exception {

        // create a new switch mediator
         XSLTMediator transformMediator = new XSLTMediator();
        // set XSLT transformation URL
        setXsltTransformationURL(transformMediator, "xslt-key");

         // invoke transformation, with static enveope
         MessageContext synCtx = new TestMessageContextBuilder().addFileEntry("xslt-key",
                 "../../repository/conf/sample/resources/transform/transform_load_2.xml")
                 .setBodyFromFile("../../repository/conf/sample/resources/transform/med_message.xml")
                 .addTextAroundBody().build();
         transformMediator.mediate(synCtx);

         // validate result
         OMContainer body = synCtx.getEnvelope().getBody();
         assertTrue(body.getFirstOMChild().getNextOMSibling() instanceof OMElement);
         assertTrue(((OMElement) body.getFirstOMChild().getNextOMSibling()).getText().length() > 0);
    }


    public void testTransformXSLTSmallMessages() throws Exception {

        // create a new switch mediator
        XSLTMediator transformMediator = new XSLTMediator();
        // set XSLT transformation URL
        setXsltTransformationURL(transformMediator, "xslt-key");

        for (int i=0; i<5; i++) {
            // invoke transformation, with static enveope
            MessageContext synCtx = new TestMessageContextBuilder().addFileEntry("xslt-key",
                    "../../repository/conf/sample/resources/transform/transform_load_2.xml")
                    .setBodyFromFile("../../repository/conf/sample/resources/transform/small_message.xml")
                    .addTextAroundBody().build();
            //MessageContext synCtx = TestUtils.getTestContextForXSLTMediator(SOURCE, props);
            transformMediator.mediate(synCtx);
            //System.out.println("done : " + i + " :: " + Runtime.getRuntime().freeMemory());
        }
    }

    public void testTransformXSLTCustomSourceNonMainElement() throws Exception {

        // create a new switch mediator
        XSLTMediator transformMediator = new XSLTMediator();

        // set xpath condition to select source
        SynapseXPath xpath = new SynapseXPath("//m0:CheckPriceRequest");
        xpath.addNamespace("m0", "http://services.samples/xsd");
        transformMediator.setSource(xpath);

        // set XSLT transformation URL
        setXsltTransformationURL(transformMediator, "xslt-key");

        // invoke transformation, with static enveope
        MessageContext synCtx = new TestMessageContextBuilder().addFileEntry("xslt-key",
                "../../repository/conf/sample/resources/transform/transform_unittest.xslt")
                .setBodyFromString(ENCLOSING_SOURCE)
                .addTextAroundBody().build();
        transformMediator.mediate(synCtx);

        // validate result
        OMContainer body = synCtx.getEnvelope().getBody();
        if (body.getFirstOMChild().getNextOMSibling() instanceof OMElement) {

            OMElement someOtherElem = (OMElement) body.getFirstOMChild().getNextOMSibling();
            assertTrue("someOtherElement".equals(someOtherElem.getLocalName()));
            assertTrue("http://someother".equals(someOtherElem.getNamespace().getNamespaceURI()));

            assertQuoteElement(someOtherElem.getFirstOMChild());
        } else {
            fail("Unexpected element found in SOAP body");
        }
    }

    public void testTextEncoding() throws Exception {
        XSLTMediator transformMediator = new XSLTMediator();
        setXsltTransformationURL(transformMediator, "xslt-key");

        MessageContext mc = new TestMessageContextBuilder().addFileEntry("xslt-key",
                "../../repository/conf/sample/resources/transform/encoding_test.xslt")
                .setEnvelopeFromFile("../../repository/conf/sample/resources/transform" +
                        "/encoding_test.xml").build();

        transformMediator.mediate(mc);

        OMElement resultElement = mc.getEnvelope().getBody().getFirstElement();
        assertEquals(BaseConstants.DEFAULT_TEXT_WRAPPER, resultElement.getQName());
        assertEquals("\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre",
                resultElement.getText());
    }
    
    // Test for SYNAPSE-307
    public void testInvalidStylesheet() throws Exception {
        XSLTMediator transformMediator = new XSLTMediator();
        setXsltTransformationURL(transformMediator, "xslt-key");
        MessageContext mc = new TestMessageContextBuilder()
            .addEntry("xslt-key", getClass().getResource("invalid.xslt"))
            .setBodyFromString("<root/>")
            .build();

        try {
            transformMediator.mediate(mc);
            fail("Expected a SynapseException to be thrown");
        } catch (SynapseException ex) {
            // this is what is expected
        }
    }

    /**
     * Test that the XSLT mediator is able to handle CDATA sections in the
     * source AXIOM tree.
     * This tests for regression against WSCOMMONS-338. It should work with
     * AXIOM versions above 1.2.7.
     *
     * @throws Exception in case of an error in tests
     */
    public void testWithCDATA() throws Exception {
        XSLTMediator transformMediator = new XSLTMediator();
        setXsltTransformationURL(transformMediator, "xslt-key");

        MessageContext mc = new TestMessageContextBuilder()
            .addEntry("xslt-key", getClass().getResource("cdata.xslt"))
            .build();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement in = factory.createOMElement(new QName(null, "in"));
        factory.createOMText(in, "test", OMNode.CDATA_SECTION_NODE);
        mc.getEnvelope().getBody().addChild(in);

        transformMediator.mediate(mc);

        OMElement out = mc.getEnvelope().getBody().getFirstElement();
        assertEquals("out", out.getLocalName());
        assertEquals("test", out.getText());
    }

    /**
     * Test that the XSLT mediator is able to handle dynamic keys
     * Xpath expression can be used to generate real key dynamically
     * Multiple xslt files can be loaded
     *
     * @throws Exception Exception in case of an error in tests
     */
    public void testWithStaticDynamicKeys() throws Exception {
        for (int i = 0; i < 3; i++) {
            testMultipleKeys(i);
        }
    }

    /**
     * Test with multiple keys including static and dynamic keys
     *
     * @param num number from 0 to 2
     * @throws Exception Exception in case of an error in tests
     */
    private void testMultipleKeys(int num) throws Exception {

        String xsltKeyValue = null;

        String path;
        SynapseXPath xpath;
        Value xsltKey;
        XSLTMediator transformMediator = new XSLTMediator();

        //default source, xsltFile, and state of key (dynamic or static)
        String source = "";
        String xsltFile = "";
        boolean isDynamicKey = true;

        // based on source, different XSLTFiles can be used
        if (num == 0) {
            source = SOURCE_STATIC_KEY;
            xsltFile = "static_key.xslt";
            xsltKeyValue = "StaticXsltKey";
            isDynamicKey = false;

        } else if (num == 1) {
            source = SOURCE_DYNAMIC_KEY1;
            xsltFile = "dynamic_key_1.xslt";
            xsltKeyValue = "DynamicXsltKey1";
            isDynamicKey = true;
        } else if (num == 2) {
            source = SOURCE_DYNAMIC_KEY2;
            xsltFile = "dynamic_key_2.xslt";
            xsltKeyValue = "DynamicXsltKey2";
            isDynamicKey = true;
        }

        if (isDynamicKey) {
            path = "//m0:CheckPriceRequest/m0:" + xsltKeyValue;
            xpath = new SynapseXPath(path);
            xpath.addNamespace("m0", "http://services.samples/xsd");

            // Create key from dynamic key (xpath)
            xsltKey = new Value(xpath);
            // set XSLT transformation URL (Xpath)
            transformMediator.setXsltKey(xsltKey);
        } else {
            //static key
            path = xsltKeyValue;
            // set XSLT transformation URL (static)
            setXsltTransformationURL(transformMediator, path);
        }
        // Mediate twice for synCtx
        MessageContext synCtx = new TestMessageContextBuilder().addEntry(
                xsltKeyValue, getClass().getResource(xsltFile)).setBodyFromString(
                source).addTextAroundBody().build();
        transformMediator.mediate(synCtx);

        synCtx = new TestMessageContextBuilder().addEntry(
                xsltKeyValue, getClass().getResource(xsltFile)).setBodyFromString(
                source).addTextAroundBody().build();
        transformMediator.mediate(synCtx);
    }

    protected static void test(Class sbf, Class rbf) throws Exception {

        XSLTMediator transformMediator = new XSLTMediator();
        Value xsltKey = new Value("xslt-key");
        transformMediator.setXsltKey(xsltKey);

        MessageContext mc = new TestMessageContextBuilder()
            .addEntry("xslt-key", XSLTMediator.class.getResource("identity.xslt"))
            .build();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement orgRoot = factory.createOMElement(new QName("root"));
        OMElement orgElement = factory.createOMElement(new QName("urn:mynamespace", "element1"));
        orgElement.setText("test");
        OMAttribute orgAttribute = orgElement.addAttribute("att", "testValue", null);
        orgRoot.addChild(orgElement);

        mc.getEnvelope().getBody().addChild(orgRoot);

        transformMediator.addAttribute(XSLTMediator.SOURCE_BUILDER_FACTORY, sbf.getName());
        transformMediator.addAttribute(XSLTMediator.RESULT_BUILDER_FACTORY, rbf.getName());

        transformMediator.mediate(mc);

        OMElement root = mc.getEnvelope().getBody().getFirstElement();
        assertEquals(orgRoot.getQName(), root.getQName());
        OMElement element = (OMElement)root.getFirstOMChild();
        assertEquals(orgElement.getQName(), element.getQName());
        assertEquals(orgElement.getText(), element.getText());
        assertEquals(orgAttribute, orgElement.getAttribute(orgAttribute.getQName()));
        assertNull(element.getNextOMSibling());
    }

    /**
     * Set XSLT transformation URL
     *
     * @param transformMediator Mediator which need to set key
     * @param xsltKeyValue String key value (static key) to set as a key
     */
    private void setXsltTransformationURL(XSLTMediator transformMediator, String xsltKeyValue) {
        Value xsltKey = new Value(xsltKeyValue);
        transformMediator.setXsltKey(xsltKey);
    }
}
