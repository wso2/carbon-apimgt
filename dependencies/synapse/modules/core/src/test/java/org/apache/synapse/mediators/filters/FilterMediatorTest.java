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
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.AnonymousListMediator;
import org.apache.synapse.mediators.TestMediateHandler;
import org.apache.synapse.mediators.TestMediator;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.util.xpath.SynapseXPath;

import java.util.regex.Pattern;

public class FilterMediatorTest extends TestCase {

    private static final String REQ =
        "<m:GetQuote xmlns:m=\"http://www.webserviceX.NET/\">\n" +
        "\t<m:symbol>IBM</m:symbol>\n" +
        "</m:GetQuote>";

    private boolean filterConditionPassed = false;
    TestMediator testMediator = new TestMediator();

    public void setUp() {
        testMediator = new TestMediator();
        testMediator.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    setFilterConditionPassed(true);
                }
            });
    }

    public void testFilterConditionTrueXPath() throws Exception {
        setFilterConditionPassed(false);

        // create a new filter mediator
        FilterMediator filter = new FilterMediator();

        // set xpath condition to IBM
        SynapseXPath xpath = new SynapseXPath("//*[wsx:symbol='IBM']");
        xpath.addNamespace("wsx", "http://www.webserviceX.NET/");
        filter.setXpath(xpath);

        // set dummy mediator to be called on success
        filter.addChild(testMediator);

        // test validate mediator, with static enveope
        filter.mediate(TestUtils.getTestContext(REQ));

        assertTrue(filterConditionPassed);
    }

    public void testFilterConditionFalseXPath() throws Exception {
        setFilterConditionPassed(false);

        // create a new filter mediator
        FilterMediator filter = new FilterMediator();

        // set xpath condition to MSFT
        SynapseXPath xpath = new SynapseXPath("//*[wsx:symbol='MSFT']");
        xpath.addNamespace("wsx", "http://www.webserviceX.NET/");
        filter.setXpath(xpath);

        // set dummy mediator to be called on success
        filter.addChild(testMediator);

        // test validate mediator, with static enveope
        filter.mediate(TestUtils.getTestContext(REQ));

        assertTrue(!filterConditionPassed);
    }

    public void testFilterConditionTrueRegex() throws Exception {
        setFilterConditionPassed(false);

        // create a new filter mediator
        FilterMediator filter = new FilterMediator();

        // set source xpath condition to //symbol
        SynapseXPath source = new SynapseXPath("//wsx:symbol");
        source.addNamespace("wsx", "http://www.webserviceX.NET/");
        filter.setSource(source);

        // set regex to IBM
        Pattern regex = Pattern.compile("IBM");
        filter.setRegex(regex);

        // set dummy mediator to be called on success
        filter.addChild(testMediator);

        // test validate mediator, with static enveope
        filter.mediate(TestUtils.getTestContext(REQ));

        assertTrue(filterConditionPassed);
    }

    public void testFilterConditionFalseRegex() throws Exception {
        setFilterConditionPassed(false);

        // create a new filter mediator
        FilterMediator filter = new FilterMediator();

        // set source xpath condition to //symbol
        SynapseXPath source = new SynapseXPath("//wsx:symbol");
        source.addNamespace("wsx", "http://www.webserviceX.NET/");
        filter.setSource(source);

        // set regex to MSFT
        Pattern regex = Pattern.compile("MSFT");
        filter.setRegex(regex);

        // set dummy mediator to be called on success
        filter.addChild(testMediator);

        // test validate mediator, with static enveope
        filter.mediate(TestUtils.getTestContext(REQ));

        assertTrue(!filterConditionPassed);
    }

    public void testFilterConditionWithThenElseKey() throws Exception {
        setFilterConditionPassed(false);

        // create a new filter mediator
        FilterMediator filter = new FilterMediator();

        // set source xpath condition to //symbol
        SynapseXPath source = new SynapseXPath("//wsx:symbol");
        source.addNamespace("wsx", "http://www.webserviceX.NET/");
        filter.setSource(source);

        // set regex to MSFT
        Pattern regex = Pattern.compile("MSFT");
        filter.setRegex(regex);

        MessageContext msgCtx = TestUtils.getTestContext(REQ);

        SequenceMediator seq = new SequenceMediator();
        seq.setName("refSeq");
        seq.addChild(testMediator);

        msgCtx.getConfiguration().addSequence("refSeq", testMediator);

        filter.setElseKey("refSeq");
        // test validate mediator, with static enveope
        filter.mediate(msgCtx);

        assertTrue(filterConditionPassed);
    }

    public void testFilterConditionWithThenElse() throws Exception {
        setFilterConditionPassed(false);

        // create a new filter mediator
        FilterMediator filter = new FilterMediator();

        // set source xpath condition to //symbol
        SynapseXPath source = new SynapseXPath("//wsx:symbol");
        source.addNamespace("wsx", "http://www.webserviceX.NET/");
        filter.setSource(source);

        // set regex to MSFT
        Pattern regex = Pattern.compile("MSFT");
        filter.setRegex(regex);

        AnonymousListMediator seq = new AnonymousListMediator();
        seq.addChild(testMediator);

        filter.setElseMediator(seq);
        // test validate mediator, with static enveope
        filter.mediate(TestUtils.getTestContext(REQ));

        assertTrue(filterConditionPassed);
    }

    public boolean isFilterConditionPassed() {
        return filterConditionPassed;
    }

    public void setFilterConditionPassed(boolean filterConditionPassed) {
        this.filterConditionPassed = filterConditionPassed;
    }
}
