/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.threatprotection.analyzer.APIMThreatAnalyzer;
import org.wso2.carbon.apimgt.gateway.threatprotection.analyzer.XMLAnalyzer;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.XMLConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;

import static org.junit.Assert.assertEquals;

/**
 * This is the test case for {@link XMLSchemaValidator}
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ThreatProtectorConstants.class, XMLAnalyzer.class, ServiceReferenceHolder.class})
public class XMLSchemaValidatorTest {

    private static final Log log = LogFactory.getLog(XMLSchemaValidatorTest.class);
    private Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
    private org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(
            org.apache.axis2.context.MessageContext.class);
    private XMLConfig xmlConfig;
    APIManagerConfiguration apiManagerConfiguration;

    @Before
    public void init() {
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.mock(APIMThreatAnalyzer.class);
        Mockito.mock(ThreatProtectorConstants.class);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration())
                .thenReturn(apiManagerConfiguration);

        xmlConfig = new XMLConfig();
        xmlConfig.setMaxAttributeCount(1);
        xmlConfig.setMaxChildrenPerElement(5);
        xmlConfig.setEntityExpansionLimit(5);
        xmlConfig.setMaxAttributeLength(1);
        xmlConfig.setMaxElementCount(5);
        xmlConfig.setMaxDepth(5);
        xmlConfig.setDtdEnabled(false);
        xmlConfig.setExternalEntitiesEnabled(false);
    }

    /**
     * Test XML configure schema properties
     */
    @Test
    public void testConfigureSchemaProperties() {
        log.info("Running the test case to Configure the schema properties.");
        XMLConfig xmlConfig = new XMLConfig();
        xmlConfig.setDtdEnabled(false);
        xmlConfig.setExternalEntitiesEnabled(false);
        xmlConfig.setMaxAttributeLength(5);
        xmlConfig.setMaxAttributeCount(5);
        xmlConfig.setMaxChildrenPerElement(5);
        xmlConfig.setMaxDepth(5);
        xmlConfig.setMaxElementCount(5);
        xmlConfig.setMaxChildrenPerElement(5);
        xmlConfig.setMaxElementCount(5);
        xmlConfig.setMaxDepth(5);
        xmlConfig.setEntityExpansionLimit(5);

        XMLConfig testConfig;
        Mockito.when(apiManagerConfiguration.isEnableSecureXMLProcessing()).thenReturn(true);
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.DTD_ENABLED)).thenReturn("false");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.EXTERNAL_ENTITIES_ENABLED)).thenReturn("true");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_ELEMENT_COUNT)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_ATTRIBUTE_LENGTH)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_XML_DEPTH)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_ATTRIBUTE_COUNT)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.CHILDREN_PER_ELEMENT)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.ENTITY_EXPANSION_LIMIT)).thenReturn("5");
        Mockito.when((messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        XMLSchemaValidator xmlSchemaValidator = new XMLSchemaValidator();
        testConfig = xmlSchemaValidator.configureSchemaProperties(messageContext);
        assertEquals(xmlConfig.getEntityExpansionLimit(), testConfig.getEntityExpansionLimit());
        assertEquals(xmlConfig.getMaxAttributeCount(), testConfig.getMaxAttributeCount());
        assertEquals(xmlConfig.getMaxAttributeLength(), testConfig.getMaxAttributeLength());
        assertEquals(xmlConfig.getMaxChildrenPerElement(), testConfig.getMaxChildrenPerElement());
        assertEquals(xmlConfig.getMaxDepth(), testConfig.getMaxDepth());
        assertEquals(xmlConfig.isDtdEnabled(), testConfig.isDtdEnabled());
        assertEquals(xmlConfig.isExternalEntitiesEnabled(), testConfig.isExternalEntitiesEnabled());
        assertEquals(xmlConfig.getMaxElementCount(), testConfig.getMaxElementCount());
        log.info("Successfully completed testConfigureSchemaProperties test case.");
    }

    /**
     * Test XML configure schema properties when secure XML processing is enabled.
     * When secure XML processing is enabled, DTD and external entities should be disabled
     * regardless of the message context properties.
     */
    @Test
    public void testConfigureSchemaPropertiesWithSecureXMLProcessingEnabled() {
        log.info("Running the test case to verify secure XML processing overrides DTD and external entities.");
        // Secure XML processing is enabled
        Mockito.when(apiManagerConfiguration.isEnableSecureXMLProcessing()).thenReturn(true);
        // Message context properties try to enable DTD and external entities
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.DTD_ENABLED)).thenReturn("true");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.EXTERNAL_ENTITIES_ENABLED)).thenReturn("true");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_ELEMENT_COUNT)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_ATTRIBUTE_LENGTH)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_XML_DEPTH)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_ATTRIBUTE_COUNT)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.CHILDREN_PER_ELEMENT)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.ENTITY_EXPANSION_LIMIT)).thenReturn("5");
        Mockito.when((messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);

        XMLSchemaValidator xmlSchemaValidator = new XMLSchemaValidator();
        XMLConfig testConfig = xmlSchemaValidator.configureSchemaProperties(messageContext);

    // Verify that DTD and external entities are forced disabled
    assertEquals("DTD must be disabled when secure XML processing is enabled", false, testConfig.isDtdEnabled());
    assertEquals("External entities must be disabled when secure XML processing is enabled",
        false, testConfig.isExternalEntitiesEnabled());
        // Verify other properties are set correctly
        assertEquals(5, testConfig.getMaxElementCount());
        assertEquals(5, testConfig.getMaxAttributeLength());
        assertEquals(5, testConfig.getMaxDepth());
        assertEquals(5, testConfig.getMaxAttributeCount());
        assertEquals(5, testConfig.getMaxChildrenPerElement());
        assertEquals(5, testConfig.getEntityExpansionLimit());

        log.info("Successfully completed testConfigureSchemaPropertiesWithSecureXMLProcessingEnabled test case.");
    }
}
