/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.Test;
import org.testng.Assert;

import javax.xml.stream.XMLStreamException;

import java.util.Map;

public class APIManagerConfigurationTest {

    @Test
    public void testPublisherProperties() throws XMLStreamException {
        String publisherConfig = "<EventPublisherConfiguration>"
                                 + "<Properties>\n"
                                 + "<Property name=\"testProp\">testVal</Property>\n"
                                 + "</Properties>\n"
                                 + "</EventPublisherConfiguration>";
        OMElement element = AXIOMUtil.stringToOM(publisherConfig);
        APIManagerConfiguration config = new APIManagerConfiguration();
        Map<String, String> extractedProperties = config.extractPublisherProperties(element);
        Assert.assertTrue(extractedProperties.containsKey("testProp"));
    }

    @Test
    public void testPublisherPropertiesUndefined() throws XMLStreamException {
        String publisherConfig = "<EventPublisherConfiguration>"
                                 + "<Properties>\n"
                                 + "</Properties>\n"
                                 + "</EventPublisherConfiguration>";
        OMElement element = AXIOMUtil.stringToOM(publisherConfig);
        APIManagerConfiguration config = new APIManagerConfiguration();
        Map<String, String> extractedProperties = config.extractPublisherProperties(element);
        Assert.assertTrue(extractedProperties.isEmpty());

    }
}
