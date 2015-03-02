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

package org.apache.synapse.mediators.builtin;

import junit.framework.TestCase;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.axiom.om.OMElement;

import java.util.Map;

public class PropertyMediatorTest extends TestCase {

    public void testSetAndReadContextProperty() throws Exception {

        PropertyMediator propMediator = new PropertyMediator();
        propMediator.setName("name");
        propMediator.setValue("value");

         // set a local property to the synapse context
        PropertyMediator propMediatorTwo = new PropertyMediator();
        propMediatorTwo.setName("nameTwo");
        propMediatorTwo.setValue("valueTwo");

        MessageContext synCtx = TestUtils.getTestContext("<empty/>");
        propMediator.mediate(synCtx);
        propMediatorTwo.mediate(synCtx);
        assertTrue(
            "value".equals((new SynapseXPath(
                "synapse:get-property('name')")).stringValueOf(synCtx)));
        assertTrue(
            "valueTwo".equals((new SynapseXPath(
                "synapse:get-property('nameTwo')")).stringValueOf(synCtx)));

        PropertyMediator propMediatorThree = new PropertyMediator();
        propMediatorThree.setName("name");
        propMediatorThree.setValue("value");
        propMediatorThree.setAction(PropertyMediator.ACTION_REMOVE);
        propMediatorThree.mediate(synCtx) ;
        assertNull((new SynapseXPath("synapse:get-property('name')")).stringValueOf(synCtx));
        assertTrue("valueTwo".equals((new SynapseXPath(
            "synapse:get-property('nameTwo')")).stringValueOf(synCtx)));
                
    }

    public void testTypeAwarePropertyHandling() throws Exception {
        PropertyMediator propMediatorOne = new PropertyMediator();
        propMediatorOne.setName("nameOne");
        propMediatorOne.setValue("valueOne", XMLConfigConstants.DATA_TYPES.STRING.name());

        PropertyMediator propMediatorTwo = new PropertyMediator();
        propMediatorTwo.setName("nameTwo");
        propMediatorTwo.setValue("25000", XMLConfigConstants.DATA_TYPES.INTEGER.name());
        propMediatorTwo.setScope(XMLConfigConstants.SCOPE_AXIS2);

        PropertyMediator propMediatorThree = new PropertyMediator();
        propMediatorThree.setName("nameThree");
        propMediatorThree.setValue("123.456", XMLConfigConstants.DATA_TYPES.DOUBLE.name());
        propMediatorThree.setScope(XMLConfigConstants.SCOPE_TRANSPORT);

        PropertyMediator propMediatorFour = new PropertyMediator();
        propMediatorFour.setName("nameFour");
        propMediatorFour.setValue("true", XMLConfigConstants.DATA_TYPES.BOOLEAN.name());

        PropertyMediator propMediatorFive = new PropertyMediator();
        propMediatorFive.setName("nameFive");
        propMediatorFive.setValue("123456", XMLConfigConstants.DATA_TYPES.LONG.name());
        propMediatorFive.setScope(XMLConfigConstants.SCOPE_AXIS2);

        PropertyMediator propMediatorSix = new PropertyMediator();
        propMediatorSix.setName("nameSix");
        propMediatorSix.setValue("12345", XMLConfigConstants.DATA_TYPES.SHORT.name());
        propMediatorSix.setScope(XMLConfigConstants.SCOPE_TRANSPORT);

        PropertyMediator propMediatorSeven = new PropertyMediator();
        propMediatorSeven.setName("nameSeven");
        propMediatorSeven.setValue("123.456", XMLConfigConstants.DATA_TYPES.FLOAT.name());

        MessageContext synCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>");
        propMediatorOne.mediate(synCtx);
        propMediatorTwo.mediate(synCtx);
        propMediatorThree.mediate(synCtx);
        propMediatorFour.mediate(synCtx);
        propMediatorFive.mediate(synCtx);
        propMediatorSix.mediate(synCtx);
        propMediatorSeven.mediate(synCtx);

        org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Map transportHeaders = (Map) axisCtx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        Object valueOne = synCtx.getProperty("nameOne");
        Object valueTwo = axisCtx.getProperty("nameTwo");
        Object valueThree = transportHeaders.get("nameThree");
        Object valueFour = synCtx.getProperty("nameFour");
        Object valueFive = axisCtx.getProperty("nameFive");
        Object valueSix = transportHeaders.get("nameSix");
        Object valueSeven = synCtx.getProperty("nameSeven");

        assertEquals("valueOne", valueOne);
        assertEquals(new Integer(25000), valueTwo);
        assertEquals(new Double(123.456), valueThree);
        assertEquals(Boolean.TRUE, valueFour);
        assertEquals(new Long(123456), valueFive);
        assertEquals(new Short("12345"), valueSix);
        assertEquals(new Float(123.456), valueSeven);
        System.out.println("All property values are correctly in place - Test SUCCESSFUL");
    }

    public void testXMLPropertyHandling() throws Exception {
        PropertyMediator propMediatorOne = new PropertyMediator();
        propMediatorOne.setName("nameOne");
        String xml = "<Project><name>Synapse</name></Project>";
        OMElement valueOne = TestUtils.createOMElement(xml);
        propMediatorOne.setValueElement(valueOne);

        // Test setting XML properties
        MessageContext synCtx = TestUtils.getTestContext("<getQuote><symbol>IBM</symbol></getQuote>");
        propMediatorOne.mediate(synCtx);
        Object prop = synCtx.getProperty("nameOne");
        assertEquals(valueOne, prop);

        // Test XML property retreival
        String exprValue = new SynapseXPath("synapse:get-property('nameOne')").stringValueOf(synCtx);
        assertEquals(xml, exprValue);

        // Test property removal
        propMediatorOne.setAction(PropertyMediator.ACTION_REMOVE);
        propMediatorOne.mediate(synCtx);
        assertNull(synCtx.getProperty("nameOne"));

        // Setting XML properties using expressions
        synCtx.setProperty("nameOne", xml);
        PropertyMediator propertyMediatorTwo = new PropertyMediator();
        propertyMediatorTwo.setName("nameTwo");
        propertyMediatorTwo.setExpression(new SynapseXPath("synapse:get-property('nameOne')"),
                XMLConfigConstants.DATA_TYPES.OM.name());
        propertyMediatorTwo.mediate(synCtx);
        Object exprProp = synCtx.getProperty("nameTwo");
        assertTrue(exprProp != null && exprProp instanceof OMElement);
    }

    /**
     * property being searched does not exist in context, and lookup should go up into the config
     * @throws Exception
     */
    /*TODO ACP public void testSetAndReadGlobalProperty() throws Exception {

        MessageContext synCtx = TestUtils.getTestContext("<empty/>");

        SynapseConfiguration synCfg = new SynapseConfiguration();
        Entry prop = new Entry();
        prop.setKey("name");
        prop.setType(Entry.VALUE_TYPE);
        prop.setValue("value");
        synCfg.addEntry("name", prop);
        synCtx.setConfiguration(synCfg);

        assertTrue(
            "value".equals(Axis2MessageContext.getStringValue(
                new SynapseXPath("synapse:get-property('name')"), synCtx)));
    }*/

    public void testMediatorPropertiesLiteral() throws Exception {

        MediatorProperty medProp = new MediatorProperty();
        medProp.setName("name");
        medProp.setValue("value");
        assertTrue("value".equals(medProp.getValue()));
    }

    public void testMediatorPropertiesExpression() throws Exception {

        // set a local property to the synapse context
        PropertyMediator propMediator = new PropertyMediator();
        propMediator.setName("name");
        propMediator.setValue("value");

        MessageContext synCtx = TestUtils.getTestContext("<empty/>");
        propMediator.mediate(synCtx);

        // read property through a mediator property
        MediatorProperty medProp = new MediatorProperty();
        medProp.setExpression(new SynapseXPath("synapse:get-property('name')"));

        assertTrue(
            "value".equals(medProp.getEvaluatedExpression(synCtx)));
    }

}


