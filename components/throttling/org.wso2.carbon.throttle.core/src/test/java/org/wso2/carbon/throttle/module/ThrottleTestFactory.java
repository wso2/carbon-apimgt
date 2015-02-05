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
package org.wso2.carbon.throttle.module;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.throttle.core.Throttle;
import org.wso2.carbon.throttle.core.ThrottleFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 *
 */
public class ThrottleTestFactory {

    public static String modulePolicy = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "\n" +
            "<throttle:ModuleThrottleAssertion>\n" +

            "    <wsp:Policy>\n" +
            "        <throttle:ID throttle:type=\"IP\"> other </throttle:ID>\n" +
            "        <wsp:Policy>\n" +
            "            <throttle:Deny/>\n" +
            "        </wsp:Policy>\n" +
            "    </wsp:Policy>\n" +
            "    <wsp:Policy>\n" +
            "        <throttle:ID throttle:type=\"IP\">10.100.1.160 - 10.100.1.165</throttle:ID>\n" +
            "        <wsp:Policy>\n" +
            "            <throttle:Control>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:MaximumCount>10</throttle:MaximumCount>\n" +
            "                    <throttle:UnitTime>10000</throttle:UnitTime>\n" +
//            "                    <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000</throttle:ProhibitTimePeriod>\n" +
            "                </wsp:Policy>\n" +
            "            </throttle:Control>\n" +
            "        </wsp:Policy>\n" +
            "    </wsp:Policy>\n" +
             "    <wsp:Policy>\n" +
            "        <throttle:ID throttle:type=\"IP\">10.100.2.160 - 10.100.2.165</throttle:ID>\n" +
            "        <wsp:Policy>\n" +
            "            <throttle:Control>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:MaximumCount>10</throttle:MaximumCount>\n" +
            "                    <throttle:UnitTime>10000</throttle:UnitTime>\n" +
//            "                    <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000</throttle:ProhibitTimePeriod>\n" +
            "                </wsp:Policy>\n" +
            "            </throttle:Control>\n" +
            "        </wsp:Policy>\n" +
            "    </wsp:Policy>\n" +
            "</throttle:ModuleThrottleAssertion>\n" +
            "</wsp:Policy>";

    public static String roleBaseOnlyPolicy = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "<throttle:ModuleThrottleAssertion>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">silver,platinum</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Control>\n" +
            "                            <wsp:Policy>\n" +
            "                                <throttle:MaximumCount>10</throttle:MaximumCount>\n" +
            "                                <throttle:UnitTime>10000</throttle:UnitTime>\n" +
//            "                                <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000\n" +
//            "                                </throttle:ProhibitTimePeriod>\n" +
            "                            </wsp:Policy>\n" +
            "                        </throttle:Control>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "                <wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">bronze</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Deny/>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "</throttle:ModuleThrottleAssertion>\n" +
            "</wsp:Policy>";

    public static String roleBaseOnlyPolicy_2 = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "<throttle:ModuleThrottleAssertion>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">silver</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Control>\n" +
            "                            <wsp:Policy>\n" +
            "                                <throttle:MaximumCount>5</throttle:MaximumCount>\n" +
            "                                <throttle:UnitTime>10000</throttle:UnitTime>\n" +
//            "                                <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000\n" +
//            "                                </throttle:ProhibitTimePeriod>\n" +
            "                            </wsp:Policy>\n" +
            "                        </throttle:Control>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "<wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">platinum</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Control>\n" +
            "                            <wsp:Policy>\n" +
            "                                <throttle:MaximumCount>10</throttle:MaximumCount>\n" +
            "                                <throttle:UnitTime>20000</throttle:UnitTime>\n" +
//            "                                <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000\n" +
//            "                                </throttle:ProhibitTimePeriod>\n" +
            "                            </wsp:Policy>\n" +
            "                        </throttle:Control>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "                <wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">bronze</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Deny/>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "</throttle:ModuleThrottleAssertion>\n" +
            "</wsp:Policy>";

    public static String roleBasePolicy_WithIP = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "<throttle:ModuleThrottleAssertion>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">silver</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Control>\n" +
            "                            <wsp:Policy>\n" +
            "                                <throttle:MaximumCount>5</throttle:MaximumCount>\n" +
            "                                <throttle:UnitTime>10000</throttle:UnitTime>\n" +
//            "                                <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000\n" +
//            "                                </throttle:ProhibitTimePeriod>\n" +
            "                            </wsp:Policy>\n" +
            "                        </throttle:Control>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "<wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">platinum</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Control>\n" +
            "                            <wsp:Policy>\n" +
            "                                <throttle:MaximumCount>10</throttle:MaximumCount>\n" +
            "                                <throttle:UnitTime>20000</throttle:UnitTime>\n" +
//            "                                <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000\n" +
//            "                                </throttle:ProhibitTimePeriod>\n" +
            "                            </wsp:Policy>\n" +
            "                        </throttle:Control>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "                <wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">bronze</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Deny/>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "    <wsp:Policy>\n" +
            "        <throttle:ID throttle:type=\"IP\"> other </throttle:ID>\n" +
            "        <wsp:Policy>\n" +
            "            <throttle:Deny/>\n" +
            "        </wsp:Policy>\n" +
            "    </wsp:Policy>\n" +
            "    <wsp:Policy>\n" +
            "        <throttle:ID throttle:type=\"IP\">10.100.1.160 - 10.100.1.165</throttle:ID>\n" +
            "        <wsp:Policy>\n" +
            "            <throttle:Control>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:MaximumCount>10</throttle:MaximumCount>\n" +
            "                    <throttle:UnitTime>10000</throttle:UnitTime>\n" +
//            "                    <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000</throttle:ProhibitTimePeriod>\n" +
            "                </wsp:Policy>\n" +
            "            </throttle:Control>\n" +
            "        </wsp:Policy>\n" +
            "    </wsp:Policy>\n" +
            "</throttle:ModuleThrottleAssertion>\n" +
            "</wsp:Policy>";

    public static String roleBasePolicy_WithGlobalCLimit = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
            "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "<throttle:ModuleThrottleAssertion>\n" +
            "    <throttle:MaximumConcurrentAccess>5 </throttle:MaximumConcurrentAccess>\n" +
            "                <wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">silver</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Control>\n" +
            "                            <wsp:Policy>\n" +
            "                                <throttle:MaximumCount>5</throttle:MaximumCount>\n" +
            "                                <throttle:UnitTime>10000</throttle:UnitTime>\n" +
//            "                                <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000\n" +
//            "                                </throttle:ProhibitTimePeriod>\n" +
            "                            </wsp:Policy>\n" +
            "                        </throttle:Control>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "<wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">platinum</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Control>\n" +
            "                            <wsp:Policy>\n" +
            "                                <throttle:MaximumCount>10</throttle:MaximumCount>\n" +
            "                                <throttle:UnitTime>20000</throttle:UnitTime>\n" +
//            "                                <throttle:ProhibitTimePeriod wsp:Optional=\"true\">1000\n" +
//            "                                </throttle:ProhibitTimePeriod>\n" +
            "                            </wsp:Policy>\n" +
            "                        </throttle:Control>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "                <wsp:Policy>\n" +
            "                    <throttle:ID throttle:type=\"ROLE\">bronze</throttle:ID>\n" +
            "                    <wsp:Policy>\n" +
            "                        <throttle:Deny/>\n" +
            "                    </wsp:Policy>\n" +
            "                </wsp:Policy>" +
            "</throttle:ModuleThrottleAssertion>\n" +
            "</wsp:Policy>";



    public static Throttle getThrottle(String policyStr) throws Exception {
        OMElement policyOM = createOMElement(policyStr);
        Policy policy = PolicyEngine.getPolicy(policyOM);
        Throttle throttle = ThrottleFactory.createModuleThrottle(policy);

        return throttle;
    }


    private static OMElement createOMElement(String xml) {
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

    public static void main(String[] args) throws Exception {
        Throttle throttle = ThrottleTestFactory.getThrottle(roleBaseOnlyPolicy);
        System.out.println(roleBaseOnlyPolicy);
        System.out.println("ok");
    }
}

