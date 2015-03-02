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
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.synapse.MessageContext;
import org.apache.synapse.TestMessageContextBuilder;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.xml.ValidateMediatorFactory;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.TestMediateHandler;
import org.apache.synapse.mediators.TestMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ValidateMediatorTest extends TestCase {

    private static final String SCHEMA_FULL_CHECKING_FEATURE_ID =
        "http://apache.org/xml/features/validation/schema-full-checking";

    private static final String HONOUR_ALL_SCHEMA_LOCATIONS_FEATURE_ID =
        "http://apache.org/xml/features/honour-all-schemaLocations";

    public static final String FEATURE_SECURE_PROCESSING =
         "http://javax.xml.XMLConstants/feature/secure-processing";

    private static final String VALID_ENVELOPE_TWO_SCHEMAS =
            "<Outer xmlns=\"http://services.samples/xsd2\">" +
            "<m0:CheckPriceRequest xmlns:m0=\"http://services.samples/xsd\">\n" +
            "<m0:Code>String</m0:Code>\n" +
            "</m0:CheckPriceRequest>\n" +
            "<m1:CheckPriceRequest2 xmlns:m1=\"http://services.samples/xsd2\">\n" +
            "<m1:Code2>String</m1:Code2>\n" +
            "</m1:CheckPriceRequest2>\n" +
            "</Outer>";

    private static final String INVALID_ENVELOPE_TWO_SCHEMAS =
            "<Outer xmlns=\"http://services.samples/xsd2\">" +
            "<m1:CheckPriceRequest2 xmlns:m1=\"http://services.samples/xsd2\">\n" +
            "<m1:Code2>String</m1:Code2>\n" +
            "</m1:CheckPriceRequest2>\n" +
            "<m0:CheckPriceRequest xmlns:m0=\"http://services.samples/xsd\">\n" +
            "<m0:Code>String</m0:Code>\n" +
            "</m0:CheckPriceRequest>\n" +
            "</Outer>";

    private static final String VALID_ENVELOPE =
            "<m0:CheckPriceRequest xmlns:m0=\"http://services.samples/xsd\">\n" +
            "\t<m0:Code>String</m0:Code>\n" +
            "</m0:CheckPriceRequest>\n";

    private static final String IN_VALID_ENVELOPE =
            "<m0:CheckPriceRequest xmlns:m0=\"http://services.samples/xsd\">\n" +
            "\t<m0:Codes>String</m0:Codes>\n" +
            "</m0:CheckPriceRequest>\n";

    private static final String VALID_ENVELOPE_NO_NS =
            "<CheckPriceRequest xmlns=\"http://services.samples/xsd\">\n" +
            "<Code>String</Code>\n" +
            "</CheckPriceRequest>\n";

    private static final String IN_VALID_ENVELOPE_NO_NS =
            "<CheckPriceRequest xmlns=\"http://services.samples/xsd\">\n" +
            "<Codes>String</Codes>\n" +
            "</CheckPriceRequest>\n";

    private static final String DEFAULT_FEATURES_MEDIATOR_CONFIG =
            "<validate xmlns=\"http://ws.apache.org/ns/synapse\">" +
            "   <schema key=\"file:synapse_repository/conf/sample/validate.xsd\"/>" +
            "   <on-fail>" +
            "       <makefault>" +
            "           <code value=\"tns:Receiver\" xmlns:tns=\"http://www.w3.org/2003/05/soap-envelope\"/>" +
            "           <reason value=\"Invalid request\"/>" +
            "       </makefault>" +
            "   </on-fail>" +
            "</validate>";

    private static final String CUSTOM_FEATURES_MEDIATOR_CONFIG =
            "<validate xmlns=\"http://ws.apache.org/ns/synapse\">" +
            "   <schema key=\"file:synapse_repository/conf/sample/validate.xsd\"/>" +
            "   <feature name=\"" + SCHEMA_FULL_CHECKING_FEATURE_ID + "\" value=\"false\"/>" +
            "   <feature name=\"" + HONOUR_ALL_SCHEMA_LOCATIONS_FEATURE_ID + "\" value=\"true\"/>" +
            "   <on-fail>" +
            "       <makefault>" +
            "           <code value=\"tns:Receiver\" xmlns:tns=\"http://www.w3.org/2003/05/soap-envelope\"/>" +
            "           <reason value=\"Invalid request\"/>" +
            "       </makefault>" +
            "   </on-fail>" +
            "</validate>";

    private static final String REG_KEY =
            "<validate xmlns=\"http://ws.apache.org/ns/synapse\">" +
            "   <schema key=\"file:synapse_repository/conf/sample/validate.xsd\"/>" +
            "   <on-fail>" +
            "       <makefault>" +
            "           <code value=\"tns:Receiver\" xmlns:tns=\"http://www.w3.org/2003/05/soap-envelope\"/>" +
            "           <reason value=\"Invalid request\"/>" +
            "       </makefault>" +
            "   </on-fail>" +
            "</validate>";

    private static final String DYNAMIC_KEY_ENVELOPE =
        "<m0:CheckPriceRequest xmlns:m0=\"http://services.samples/xsd\">\n" +
        "<m0:DynamicXsdKey>DynamicXsdKey</m0:DynamicXsdKey>\n" +
        "</m0:CheckPriceRequest>\n" ;

    private SynapseXPath createXPath(String expression) throws JaxenException {
        SynapseXPath xpath = new SynapseXPath(expression);
        xpath.addNamespace("m0", "http://services.samples/xsd");
        xpath.addNamespace("m1", "http://services.samples/xsd2");
        return xpath;
    }

    private void test(ValidateMediator validate, MessageContext synCtx, boolean expectFail) {
        final MutableInt onFailInvoked = new MutableInt();
        TestMediator testMediator = new TestMediator();
        testMediator.setHandler(
                new TestMediateHandler() {
                    public void handle(MessageContext synCtx) {
                        onFailInvoked.setValue(1);
                    }
                });
        // set dummy mediator to be called on fail
        validate.addChild(testMediator);
        validate.mediate(synCtx);
        if (expectFail) {
            assertTrue("Expected ValidateMediator to trigger fail sequence",
                    onFailInvoked.intValue() == 1);
        } else {
            assertTrue("ValidateMediator unexpectedly triggered fail sequence",
                    onFailInvoked.intValue() == 0);
        }
    }

    public void testValidateMediatorValidCase() throws Exception {
        // create a validate mediator
        ValidateMediator validate = new ValidateMediator();

        // set the schema url, source xpath and any name spaces
        validate.setSchemaKeys(createKeyListFromStaticKey("xsd-key"));
        validate.setSource(createXPath("//m0:CheckPriceRequest"));

        MessageContext synCtx = new TestMessageContextBuilder()
                .addFileEntry("xsd-key", "./../../repository/conf/sample/resources/validate/validate.xsd")
                .setBodyFromString(VALID_ENVELOPE).build();

        // test validate mediator, with static enveope
        test(validate, synCtx, false);
    }

    public void testValidateMediatorValidCaseTwoSchemas() throws Exception {
        // create a validate mediator
        ValidateMediator validate = new ValidateMediator();

        // set the schema url, source xpath and any name spaces
        validate.setSchemaKeys(createKeyListFromMoreKeys("xsd-key-1", "xsd-key-2"));
        validate.setSource(createXPath("//m1:Outer"));

        MessageContext synCtx = new TestMessageContextBuilder()
                .addFileEntry("xsd-key-1", "./../../repository/conf/sample/resources/validate/validate.xsd")
                .addFileEntry("xsd-key-2", "./../../repository/conf/sample/resources/validate/validate2.xsd")
                .setBodyFromString(VALID_ENVELOPE_TWO_SCHEMAS).build();

        // test validate mediator, with static enveope
        test(validate, synCtx, false);
    }

    public void testValidateMediatorInvalidCaseTwoSchemas() throws Exception {
        // create a validate mediator
        ValidateMediator validate = new ValidateMediator();

        // set the schema url, source xpath and any name spaces
        validate.setSchemaKeys(createKeyListFromMoreKeys("xsd-key-1", "xsd-key-2"));
        validate.setSource(createXPath("//m1:Outer"));

        MessageContext synCtx = new TestMessageContextBuilder()
                .addFileEntry("xsd-key-1", "./../../repository/conf/sample/resources/validate/validate.xsd")
                .addFileEntry("xsd-key-2", "./../../repository/conf/sample/resources/validate/validate2.xsd")
                .setBodyFromString(INVALID_ENVELOPE_TWO_SCHEMAS).build();

        // test validate mediator, with static enveope
        test(validate, synCtx, true);
    }

    public void testValidateMediatorInvalidCase() throws Exception {
        // create a validate mediator
        ValidateMediator validate = new ValidateMediator();

        // set the schema url, source xpath and any name spaces
        validate.setSchemaKeys(createKeyListFromStaticKey("xsd-key-1"));
        validate.setSource(createXPath("//m0:CheckPriceRequest"));

        MessageContext synCtx = new TestMessageContextBuilder()
                .addFileEntry("xsd-key-1", "./../../repository/conf/sample/resources/validate/validate.xsd")
                .setBodyFromString(IN_VALID_ENVELOPE).build();

        // test validate mediator, with static enveope
        test(validate, synCtx, true);
    }

    public void testValidateMediatorValidCaseNoNS() throws Exception {
        // create a validate mediator
        ValidateMediator validate = new ValidateMediator();

        // set the schema url, source xpath and any name spaces
        validate.setSchemaKeys(createKeyListFromStaticKey("xsd-key-1"));
        validate.setSource(createXPath("//m0:CheckPriceRequest"));

        MessageContext synCtx = new TestMessageContextBuilder()
                .addFileEntry("xsd-key-1", "./../../repository/conf/sample/resources/validate/validate.xsd")
                .setBodyFromString(VALID_ENVELOPE_NO_NS).build();

        // test validate mediator, with static enveope
        test(validate, synCtx, false);
    }

    public void testValidateMediatorInvalidCaseNoNS() throws Exception {
        // create a validate mediator
        ValidateMediator validate = new ValidateMediator();

        // set the schema url, source xpath and any name spaces
        validate.setSchemaKeys(createKeyListFromStaticKey("xsd-key-1"));
        validate.setSource(createXPath("//m0:CheckPriceRequest"));

        MessageContext synCtx = new TestMessageContextBuilder()
                .addFileEntry("xsd-key-1", "./../../repository/conf/sample/resources/validate/validate.xsd")
                .setBodyFromString(IN_VALID_ENVELOPE_NO_NS).build();

        // test validate mediator, with static enveope
        test(validate, synCtx, true);
    }

    public void testValidateMediatorDefaultFeatures() throws Exception {

        ValidateMediatorFactory mf = new ValidateMediatorFactory();
        ValidateMediator validate = (ValidateMediator) mf.createMediator(
                SynapseConfigUtils.stringToOM(DEFAULT_FEATURES_MEDIATOR_CONFIG), new Properties());

        assertNull(validate.getFeature(SCHEMA_FULL_CHECKING_FEATURE_ID));
        assertNull(validate.getFeature(HONOUR_ALL_SCHEMA_LOCATIONS_FEATURE_ID));

        makeValidInvocation(validate);
    }

    public void testValidateMediatorCustomFeatures() throws Exception {
        ValidateMediatorFactory mf = new ValidateMediatorFactory();
        ValidateMediator validate = (ValidateMediator) mf.createMediator(
                SynapseConfigUtils.stringToOM(CUSTOM_FEATURES_MEDIATOR_CONFIG), new Properties());

        assertNotNull(validate.getFeature(SCHEMA_FULL_CHECKING_FEATURE_ID));
        assertFalse("true".equals(validate.getFeature(SCHEMA_FULL_CHECKING_FEATURE_ID)));
        assertNotNull(validate.getFeature(HONOUR_ALL_SCHEMA_LOCATIONS_FEATURE_ID));
        assertTrue("true".equals(validate.getFeature(HONOUR_ALL_SCHEMA_LOCATIONS_FEATURE_ID)));

        makeValidInvocation(validate);
    }

    private void makeValidInvocation(ValidateMediator validate) throws Exception {
        // set the schema url, source xpath and any name spaces
        validate.setSchemaKeys(createKeyListFromStaticKey("xsd-key-1"));
        validate.setSource(createXPath("//m0:CheckPriceRequest"));

        MessageContext synCtx = new TestMessageContextBuilder()
                .addFileEntry("xsd-key-1", "./../../repository/conf/sample/resources/validate/validate.xsd")
                .setBodyFromString(VALID_ENVELOPE).build();

        // test validate mediator, with static enveope
        test(validate, synCtx, false);
    }

    /**
     * Test that the Validator mediator is able to handle static and dynamic keys
     * Xpath expression can be used to generate real key dynamically
     *
     * @throws Exception Exception in case of an error in tests
     */
    public void testWithStaticDynamicKeys() throws Exception {
        for (int i = 0; i < 2; i++) {
            testMultipleKeys(i);
        }
    }

    /**
     * Test with multiple keys including static and dynamic keys
     *
     * @param num number from 0 to 1
     * @throws Exception Exception in case of an error in tests
     */
    private void testMultipleKeys(int num) throws Exception {

        String xsdKeyValue = null;

        String path;

        SynapseXPath xpath;

        // create a validate mediator
        ValidateMediator validate = new ValidateMediator();

        //default source, xsdFile, and state of key (dynamic or static)
        String source = "";
        String xsdFile = "";
        boolean isDynamicKey = true;

        // based on source, different xsdFiles can be used
        if (num == 0) {
            source = VALID_ENVELOPE;
            xsdKeyValue = "xsd-key";
            isDynamicKey = false;
            xsdFile = "validate";

        } else if (num == 1) {
            source = DYNAMIC_KEY_ENVELOPE;
            // xsdFile = "dynamic_key1.xsd";
            xsdKeyValue = "DynamicXsdKey";
            isDynamicKey = true;
            xsdFile = "validate3";
        }

        if (isDynamicKey) {
            // set the schema url using dynamic key (Xpath)
            path = "//m0:CheckPriceRequest/m0:" + xsdKeyValue;
            xpath = new SynapseXPath(path);
            xpath.addNamespace("m0", "http://services.samples/xsd");
            validate.setSchemaKeys(createKeyListFromDynamicKey(xpath));
        } else {
            // set the schema url using static key
            validate.setSchemaKeys(createKeyListFromStaticKey(xsdKeyValue));
        }

        MessageContext synCtx = new TestMessageContextBuilder()
                .addFileEntry(xsdKeyValue, "./../../repository/conf/sample/resources/validate/" + xsdFile + ".xsd")
                .setBodyFromString(source).build();

        test(validate, synCtx, false);
    }

    /**
     * Create a Value list which consists with one static element
     *
     * @param keyName String key value (static key) to create Value object
     * @return immutable Value list with one Value element
     */
    private List<Value> createKeyListFromStaticKey(String keyName) {
        // create static key using given string key name
        Value xsdKey = new Value(keyName);
        return Collections.singletonList(xsdKey);
    }

    /**
     * Create a Value list which consists with one dynamic element
     *
     * @param xpath String key value (static key) to create Value object
     * @return immutable Value list with one Value element
     */
    private List<Value> createKeyListFromDynamicKey(SynapseXPath xpath) {
        // create static key using given string key name
        Value xsdKey = new Value(xpath);
        return Collections.singletonList(xsdKey);
    }


    /**
     * Create a Value list with given set of static keys
     *
     * @param keyNames Set of static keys to create list
     * @return Key list
     */
    private List<Value> createKeyListFromMoreKeys(String... keyNames) {
        List<Value> keyList = new ArrayList<Value>();
        for (String keyName : keyNames) {
            // create static key using given string key name
            Value xsdKey = new Value(keyName);
            keyList.add(xsdKey);

        }
        return keyList;
    }


}
