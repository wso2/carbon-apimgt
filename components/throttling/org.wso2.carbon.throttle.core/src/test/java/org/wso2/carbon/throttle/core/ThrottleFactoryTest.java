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
package org.wso2.carbon.throttle.core;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.throttle.core.Throttle;
import org.wso2.carbon.throttle.core.ThrottleConstants;
import org.wso2.carbon.throttle.core.ThrottleFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 *
 */
public class ThrottleFactoryTest extends TestCase {

    private String modulePolicy = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "\n" +
            "<throttle:ModuleThrottleAssertion>\n" +
            "    <throttle:MaximumConcurrentAccess>5 </throttle:MaximumConcurrentAccess>\n" +
            "    <wsp:Policy>\n" +
            "        <throttle:ID throttle:type=\"IP\"> other </throttle:ID>\n" +
            "        <wsp:Policy>\n" +
            "            <throttle:Control>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:MaximumCount>5 </throttle:MaximumCount>\n" +
            "                    <throttle:UnitTime>2000</throttle:UnitTime>\n" +
            "                    <throttle:ProhibitTimePeriod wsp:Optional=\"true\">5</throttle:ProhibitTimePeriod>\n" +
            "                </wsp:Policy>\n" +
            "            </throttle:Control>\n" +
            "        </wsp:Policy>\n" +
            "    </wsp:Policy>\n" +
            "    <wsp:Policy>\n" +
            "        <throttle:ID throttle:type=\"IP\">10.100.1.160 - 10.100.1.165 </throttle:ID>\n" +
            "        <wsp:Policy>\n" +
            "            <throttle:Control>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:MaximumCount>5</throttle:MaximumCount>\n" +
            "                    <throttle:UnitTime>2000</throttle:UnitTime>\n" +
            "                    <throttle:ProhibitTimePeriod wsp:Optional=\"true\">5</throttle:ProhibitTimePeriod>\n" +
            "                </wsp:Policy>\n" +
            "            </throttle:Control>\n" +
            "        </wsp:Policy>\n" +
            "    </wsp:Policy>\n" +
            "    <wsp:Policy>\n" +
            "        <throttle:ID throttle:type=\"DOMAIN\"> test.com </throttle:ID>\n" +
            "        <wsp:Policy>\n" +
            "            <throttle:Control>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:MaximumCount> 5 </throttle:MaximumCount>\n" +
            "                    <throttle:UnitTime> 2000 </throttle:UnitTime>\n" +
            "                    <throttle:ProhibitTimePeriod wsp:Optional=\"true\"> 5 </throttle:ProhibitTimePeriod>\n" +
            "                </wsp:Policy>\n" +
            "            </throttle:Control>\n" +
            "        </wsp:Policy>\n" +
            "    </wsp:Policy>\n" +
            "</throttle:ModuleThrottleAssertion>\n" +
            "</wsp:Policy>";

    private String defaultMudulePolicy = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "    <throttle:ModuleThrottleAssertion>\n" +             
            "        <wsp:Policy>\n" +
            "            <throttle:ID throttle:type=\"IP\"> other </throttle:ID>\n" +
            "            <wsp:Policy>\n" +
            "                <throttle:Allow/>\n" +
            "            </wsp:Policy>\n" +
            "        </wsp:Policy>\n" +
            "        <wsp:Policy>\n" +
            "            <throttle:ID throttle:type=\"DOMAIN\"> other </throttle:ID>\n" +
            "            <wsp:Policy>\n" +
            "                <throttle:Allow/>\n" +
            "            </wsp:Policy>\n" +
            "        </wsp:Policy>\n" +
            "    </throttle:ModuleThrottleAssertion>\n" +
            "</wsp:Policy>";

    private String oldPolicy = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "    <throttle:ThrottleAssertion>\n" +
            "        <throttle:MaximumConcurrentAccess>10</throttle:MaximumConcurrentAccess>\n" +
            "        <wsp:All>\n" +
            "            <throttle:ID throttle:type=\"IP\">other</throttle:ID>\n" +
            "            <wsp:ExactlyOne>\n" +
            "                <wsp:All>\n" +
            "                    <throttle:MaximumCount>4</throttle:MaximumCount>\n" +
            "                    <throttle:UnitTime>800000</throttle:UnitTime>\n" +
            "                    <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000</throttle:ProhibitTimePeriod>\n" +
            "                </wsp:All>\n" +
            "                <throttle:IsAllow>true</throttle:IsAllow>\n" +
            "            </wsp:ExactlyOne>\n" +
            "        </wsp:All> \n" +
            "        <wsp:All>\n" +
            "            <throttle:ID throttle:type=\"IP\"> 127.1.1.127 </throttle:ID>\n" +
            "            <wsp:ExactlyOne>\n" +
            "                <wsp:All>\n" +
            "                    <throttle:MaximumCount>4</throttle:MaximumCount>\n" +
            "                    <throttle:UnitTime>800000</throttle:UnitTime>\n" +
            "                    <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000</throttle:ProhibitTimePeriod>\n" +
            "                </wsp:All>\n" +
            "                <throttle:IsAllow>true</throttle:IsAllow>\n" +
            "            </wsp:ExactlyOne>\n" +
            "        </wsp:All>\n" +
            "        <wsp:All>\n" +
            "            <throttle:ID throttle:type=\"DOMAIN\">test.com</throttle:ID>\n" +
            "            <wsp:ExactlyOne>\n" +
            "                <throttle:IsAllow>true</throttle:IsAllow>              \n" +
            "            </wsp:ExactlyOne>\n" +
            "        </wsp:All>      \n" +
            "    </throttle:ThrottleAssertion>\n" +
            "</wsp:Policy>";

    private String servicePolicy = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "                xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "        <throttle:ServiceThrottleAssertion>\n" +
            "            <throttle:MaximumConcurrentAccess>5</throttle:MaximumConcurrentAccess>\n" +
            "            <wsp:Policy>\n" +
            "                <throttle:ID throttle:type=\"IP\">  other</throttle:ID>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:Allow/>\n" +
            "                </wsp:Policy>\n" +
            "            </wsp:Policy>\n" +
            "            <wsp:Policy>\n" +
            "                <throttle:ID throttle:type=\"IP\"> 127.1.1.127 </throttle:ID>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:Deny/>\n" +
            "                </wsp:Policy>\n" +
            "            </wsp:Policy>\n" +
            "            <wsp:Policy>\n" +
            "                <throttle:ID throttle:type=\"DOMAIN\"> test.com</throttle:ID>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:Control>\n" +
            "                        <wsp:Policy>\n" +
            "                            <throttle:MaximumCount>5</throttle:MaximumCount>\n" +
            "                            <throttle:UnitTime>2000</throttle:UnitTime>\n" +
            "                            <throttle:ProhibitTimePeriod wsp:Optional=\"true\">5</throttle:ProhibitTimePeriod>\n" +
            "                        </wsp:Policy>\n" +
            "                    </throttle:Control>\n" +
            "                </wsp:Policy>\n" +
            "            </wsp:Policy>\n" +
            "        </throttle:ServiceThrottleAssertion>\n" +
            "    </wsp:Policy>";

    private String opPolicy = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "                xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "        <throttle:OperationThrottleAssertion>\n" +
            "            <throttle:MaximumConcurrentAccess> 5 </throttle:MaximumConcurrentAccess>\n" +
            "            <wsp:Policy>\n" +
            "                <throttle:ID throttle:type=\"IP\">other</throttle:ID>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:Allow/>\n" +
            "                </wsp:Policy>\n" +
            "            </wsp:Policy>\n" +
            "            <wsp:Policy>\n" +
            "                <throttle:ID throttle:type=\"IP\">127.1.1.127</throttle:ID>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:Deny/>\n" +
            "                </wsp:Policy>\n" +
            "            </wsp:Policy>\n" +
            "            <wsp:Policy>\n" +
            "                <throttle:ID throttle:type=\"DOMAIN\"> test.com</throttle:ID>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:Control>\n" +
            "                        <wsp:Policy>\n" +
            "                            <throttle:MaximumCount>5</throttle:MaximumCount>\n" +
            "                            <throttle:UnitTime>2000</throttle:UnitTime>\n" +
            "                            <throttle:ProhibitTimePeriod wsp:Optional=\"true\">5</throttle:ProhibitTimePeriod>\n" +
            "                        </wsp:Policy>\n" +
            "                    </throttle:Control>\n" +
            "                </wsp:Policy>\n" +
            "            </wsp:Policy>\n" +
            "        </throttle:OperationThrottleAssertion>\n" +
            "    </wsp:Policy>";

    private String testXMl = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "    <wsp:Policy>\n" +
            "        <wsp:ExactlyOne>\n" +
            "            <wsp:All>\n" +
            "                <throttle:ServiceThrottleAssertion>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:ID throttle:type=\"IP\">other</throttle:ID>\n" +
            "                        <wsp:Policy>\n" +
            "                            <throttle:Deny/>\n" +
            "                        </wsp:Policy>\n" +
            "                    </wsp:Policy>\n" +
            "                </throttle:ServiceThrottleAssertion>\n" +
            "            </wsp:All>\n" +
            "        </wsp:ExactlyOne>\n" +
            "    </wsp:Policy>\n" +
            "</wsp:Policy>";

    public void testModuleThrottleAssertBuilder() throws Exception {

        OMElement policyOM = createOMElement(modulePolicy);
        Policy policy = PolicyEngine.getPolicy(policyOM);
        Throttle throttle = ThrottleFactory.createModuleThrottle(policy);

        assertNotNull(throttle);
        assertNotNull(throttle.getConcurrentAccessController());
        assertNotNull(throttle.getThrottleContext(ThrottleConstants.IP_BASED_THROTTLE_KEY));
        assertNotNull(throttle.getThrottleConfiguration(ThrottleConstants.IP_BASED_THROTTLE_KEY));
        assertNotNull(throttle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY).getConfigurationKeyOfCaller("other"));
        assertNotNull(throttle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("127.1.1.127"));
        assertNotNull(throttle.getThrottleConfiguration(
                ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("test.com"));

        OMElement oldElement = createOMElement(oldPolicy);
        Policy oldPolicy = PolicyEngine.getPolicy(oldElement);
        Throttle oldThrottle = ThrottleFactory.createModuleThrottle(oldPolicy);

        assertNotNull(oldThrottle);
        assertNotNull(oldThrottle.getConcurrentAccessController());
        assertNotNull(oldThrottle.getThrottleContext(ThrottleConstants.IP_BASED_THROTTLE_KEY));
        assertNotNull(oldThrottle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY));
        assertNotNull(oldThrottle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY).getConfigurationKeyOfCaller("other"));
        assertNotNull(oldThrottle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("127.1.1.127"));
        assertNotNull(oldThrottle.getThrottleConfiguration(
                ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("test.com"));

        OMElement allOM = createOMElement(servicePolicy);
        Policy allPolicy = PolicyEngine.getPolicy(allOM);
        Throttle allThrottle = ThrottleFactory.createServiceThrottle(allPolicy);

        assertNotNull(allThrottle);
        assertNotNull(allThrottle.getConcurrentAccessController());
        assertNotNull(allThrottle.getThrottleContext(ThrottleConstants.IP_BASED_THROTTLE_KEY));
        assertNotNull(allThrottle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY));
        assertNotNull(allThrottle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY).getConfigurationKeyOfCaller("other"));
        assertNotNull(allThrottle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("127.1.1.127"));
        assertNotNull(allThrottle.getThrottleConfiguration(
                ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("test.com"));

        OMElement opElement = createOMElement(opPolicy);
        Policy opPolicy = PolicyEngine.getPolicy(opElement);
        Throttle opThrottle = ThrottleFactory.createOperationThrottle(opPolicy);

        assertNotNull(opThrottle);
        assertNotNull(opThrottle.getConcurrentAccessController());
        assertNotNull(opThrottle.getThrottleContext(ThrottleConstants.IP_BASED_THROTTLE_KEY));
        assertNotNull(opThrottle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY));
        assertNotNull(opThrottle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY).getConfigurationKeyOfCaller("other"));
        assertNotNull(opThrottle.getThrottleConfiguration(
                ThrottleConstants.IP_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("127.1.1.127"));
        assertNotNull(opThrottle.getThrottleConfiguration(
                ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("test.com"));
        assertNotNull(opThrottle.getConcurrentAccessController());

        OMElement defaultElement = createOMElement(defaultMudulePolicy);
        Throttle defaultThrottle = ThrottleFactory.createModuleThrottle(
                PolicyEngine.getPolicy(defaultElement));

        assertNotNull(defaultThrottle);
        assertNotNull(defaultThrottle.
                getThrottleConfiguration(ThrottleConstants.IP_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("other"));
        assertNotNull(defaultThrottle.
                getThrottleConfiguration(ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("other"));
        assertNull(defaultThrottle.getConcurrentAccessController());

        OMElement testXMLElement = createOMElement(testXMl);
        Throttle testThrottle = ThrottleFactory.createServiceThrottle(
                PolicyEngine.getPolicy(testXMLElement));

        assertNotNull(testThrottle);
        assertNotNull(testThrottle.
                getThrottleConfiguration(ThrottleConstants.IP_BASED_THROTTLE_KEY).
                getConfigurationKeyOfCaller("other"));
        assertNull(testThrottle.getConcurrentAccessController());


    }


    private OMElement createOMElement(String xml) {
        try {
            XMLStreamReader reader = XMLInputFactory
                    .newInstance().createXMLStreamReader(new StringReader(xml));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}

