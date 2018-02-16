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
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.JSONConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;

import static org.junit.Assert.assertEquals;

/**
 * This is the test case for {@link JsonSchemaValidator}
 */
@RunWith(PowerMockRunner.class)
public class JsonSchemaValidatorTest {
    private static final Log log = LogFactory.getLog(JsonSchemaValidatorTest.class);
    private MessageContext messageContext;

    @Before
    public void init() {
        messageContext = Mockito.mock(Axis2MessageContext.class);
    }

    /**
     * Test Json configure properties method.
     */
    @Test
    public void testConfigureSchemaProperties() {
        log.info("Running the test case to Configure the schema properties.");
        JSONConfig testJsonConfig = new JSONConfig();
        testJsonConfig.setMaxPropertyCount(Integer.valueOf("5"));
        testJsonConfig.setMaxStringLength(Integer.valueOf("5"));
        testJsonConfig.setMaxArrayElementCount(Integer.valueOf("5"));
        testJsonConfig.setMaxKeyLength(Integer.valueOf("5"));
        testJsonConfig.setMaxJsonDepth(Integer.valueOf("5"));
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_PROPERTY_COUNT)).thenReturn
                ("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_STRING_LENGTH)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_ARRAY_ELEMENT_COUNT)).thenReturn("5");
        Mockito.when(messageContext.getProperty(ThreatProtectorConstants.MAX_KEY_LENGTH)).thenReturn("5");
        Mockito.when(messageContext.getProperty( ThreatProtectorConstants.MAX_JSON_DEPTH)).thenReturn("5");
        JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator();
        JSONConfig jsonConfig = jsonSchemaValidator.configureSchemaProperties(messageContext);
        assertEquals(jsonConfig.getMaxPropertyCount(), testJsonConfig.getMaxPropertyCount());
        assertEquals(jsonConfig.getMaxStringLength(), testJsonConfig.getMaxStringLength());
        assertEquals(jsonConfig.getMaxArrayElementCount(), testJsonConfig.getMaxArrayElementCount());
        assertEquals(jsonConfig.getMaxKeyLength(), testJsonConfig.getMaxKeyLength());
        assertEquals(jsonConfig.getMaxJsonDepth(), testJsonConfig.getMaxJsonDepth());
        log.info("Successfully completed testConfigureSchemaProperties test case.");
    }

}
