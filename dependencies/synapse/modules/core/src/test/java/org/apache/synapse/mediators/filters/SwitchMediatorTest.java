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

package org.apache.synapse.mediators.filters;

import junit.framework.TestCase;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.AnonymousListMediator;
import org.apache.synapse.config.xml.SwitchCase;
import org.apache.synapse.mediators.TestMediateHandler;
import org.apache.synapse.mediators.TestMediator;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.util.xpath.SynapseXPath;

import java.util.Arrays;
import java.util.regex.Pattern;

public class SwitchMediatorTest extends TestCase {

    private static final String IBM_REQ =
        "<m:GetQuote xmlns:m=\"http://www.webserviceX.NET/\">\n" +
        "\t<m:symbol>IBM</m:symbol>\n" +
        "</m:GetQuote>";

    private static final String MSFT_REQ =
        "<m:GetQuote xmlns:m=\"http://www.webserviceX.NET/\">\n" +
        "\t<m:symbol>MSFT</m:symbol>\n" +
        "</m:GetQuote>";

    private static final String DEFAULT_REQ =
        "<m:GetQuote xmlns:m=\"http://www.webserviceX.NET/\">\n" +
        "\t<m:symbol>SUN</m:symbol>\n" +
        "</m:GetQuote>";

    private String executedCase = null;
    TestMediator ibmMediator, msftMediator, defaultMediator;
    SwitchMediator switchMediator = null;

    public void setUp() throws Exception {

        ibmMediator = new TestMediator();
        ibmMediator.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    setExecutedCase("IBM");
                }
            });

        msftMediator = new TestMediator();
        msftMediator.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    setExecutedCase("MSFT");
                }
            });

        defaultMediator = new TestMediator();
        defaultMediator.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    setExecutedCase("DEFAULT");
                }
            });

        // create a new switch mediator
        switchMediator = new SwitchMediator();

        // set xpath condition to select symbol
        SynapseXPath xpath = new SynapseXPath("//wsx:symbol");
        xpath.addNamespace("wsx", "http://www.webserviceX.NET/");
        switchMediator.setSource(xpath);
        SwitchCase caseOne = new SwitchCase();
        caseOne.setRegex(Pattern.compile("IBM"));
        AnonymousListMediator mediatorOne = new AnonymousListMediator();
        mediatorOne.addAll(Arrays.asList(new Mediator[] {ibmMediator}));
        caseOne.setCaseMediator(mediatorOne);
        SwitchCase caseTwo = new SwitchCase();
        caseTwo.setRegex(Pattern.compile("MSFT"));
        AnonymousListMediator mediatorTwo = new AnonymousListMediator();
        mediatorTwo.addAll(Arrays.asList(new Mediator[] {msftMediator}));
        caseTwo.setCaseMediator(mediatorTwo);

        SwitchCase caseDefault = new SwitchCase();
        AnonymousListMediator mediatorDefault = new AnonymousListMediator();
        mediatorDefault.addAll(Arrays.asList(new Mediator[] {defaultMediator}));
        caseDefault.setCaseMediator(mediatorDefault); 
        // set ibm mediator to be called for IBM, msft for MSFT and default for others..
        switchMediator.addCase(caseOne);
        switchMediator.addCase(caseTwo);
        switchMediator.setDefaultCase(caseDefault);       
    }

    public void testSwitchConditionCaseOne() throws Exception {
        setExecutedCase(null);

        // test switch mediator, with static enveope
        switchMediator.mediate(TestUtils.getTestContext(IBM_REQ));

        assertTrue("IBM".equals(getExecutedCase()));
    }

    public void testSwitchConditionCaseTwo() throws Exception {
        setExecutedCase(null);

        // test switch mediator, with static enveope
        switchMediator.mediate(TestUtils.getTestContext(MSFT_REQ));

        assertTrue("MSFT".equals(getExecutedCase()));
    }

    public void testSwitchConditionCaseDefault() throws Exception {
        setExecutedCase(null);

        // test switch mediator, with static enveope
        switchMediator.mediate(TestUtils.getTestContext(DEFAULT_REQ));

        assertTrue("DEFAULT".equals(getExecutedCase()));
    }

    public String getExecutedCase() {
        return executedCase;
    }

    public void setExecutedCase(String executedCase) {
        if (this.executedCase != null) {
            throw new RuntimeException("Case already executed");
        }
        this.executedCase = executedCase;
    }
}
