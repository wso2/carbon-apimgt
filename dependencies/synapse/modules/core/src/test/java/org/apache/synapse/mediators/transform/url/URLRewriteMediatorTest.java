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

package org.apache.synapse.mediators.transform.url;

import junit.framework.TestCase;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.URLRewriteMediatorFactory;
import org.apache.synapse.commons.evaluators.EqualEvaluator;
import org.apache.synapse.commons.evaluators.EvaluatorConstants;
import org.apache.synapse.commons.evaluators.MatchEvaluator;
import org.apache.synapse.commons.evaluators.source.URLTextRetriever;
import org.apache.synapse.commons.evaluators.source.SOAPEnvelopeTextRetriever;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.apache.synapse.mediators.TestUtils;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;

import java.util.Properties;
import java.util.regex.Pattern;

public class URLRewriteMediatorTest extends TestCase {

    private String targetURL =  "http://localhost:9000/services/SimpleStockQuoteService";

    public void testUnconditionalRewriteScenario1() throws Exception {
        URLRewriteMediator mediator = new URLRewriteMediator();

        RewriteAction action = new RewriteAction();
        action.setValue(targetURL);

        RewriteRule rule = new RewriteRule();
        rule.addRewriteAction(action);
        mediator.addRule(rule);

        MessageContext msgCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>");
        mediator.mediate(msgCtx);

        assertEquals(targetURL, msgCtx.getTo().getAddress());
    }

    public void testUnconditionalRewriteScenario2() throws Exception {
        URLRewriteMediator mediator = new URLRewriteMediator();
        mediator.setOutputProperty("outURL");

        RewriteAction action = new RewriteAction();
        action.setValue(targetURL);

        RewriteRule rule = new RewriteRule();
        rule.addRewriteAction(action);
        mediator.addRule(rule);

        MessageContext msgCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>");
        mediator.mediate(msgCtx);

        assertEquals(targetURL, msgCtx.getProperty("outURL"));
    }

    public void testUnconditionalRewriteScenario3() throws Exception {
        URLRewriteMediator mediator = new URLRewriteMediator();
        mediator.setOutputProperty("outURL");

        RewriteAction action1 = new RewriteAction();
        action1.setValue(targetURL);
        RewriteRule rule1 = new RewriteRule();
        rule1.addRewriteAction(action1);
        mediator.addRule(rule1);

        RewriteAction action2 = new RewriteAction();
        action2.setValue("/services/SimpleStockQuoteService");
        action2.setFragmentIndex(URIFragments.PATH);

        RewriteAction action3 = new RewriteAction();
        action3.setXpath(new SynapseXPath("get-property('port')"));
        action3.setFragmentIndex(URIFragments.PORT);

        RewriteRule rule2 = new RewriteRule();
        rule2.addRewriteAction(action2);
        rule2.addRewriteAction(action3);
        mediator.addRule(rule2);

        MessageContext msgCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>");
        msgCtx.setTo(new EndpointReference("http://localhost:8280"));
        msgCtx.setProperty("port", 9000);
        mediator.mediate(msgCtx);

        assertEquals(targetURL, msgCtx.getProperty("outURL"));
    }

    public void testConditionalRewriteScenario1() throws Exception {
        URLRewriteMediator mediator = new URLRewriteMediator();

        RewriteAction action = new RewriteAction();
        action.setValue(targetURL);

        RewriteRule rule = new RewriteRule();
        EqualEvaluator eval = new EqualEvaluator();
        URLTextRetriever txtRtvr = new URLTextRetriever();
        txtRtvr.setSource(EvaluatorConstants.URI_FRAGMENTS.port.name());
        eval.setTextRetriever(txtRtvr);
        eval.setValue("8280");
        rule.setCondition(eval);
        rule.addRewriteAction(action);
        mediator.addRule(rule);

        MessageContext msgCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>");
        msgCtx.setTo(new EndpointReference("http://localhost:8280"));
        mediator.mediate(msgCtx);

        assertEquals(targetURL, msgCtx.getTo().getAddress());
    }

    public void testConditionalRewriteScenario2() throws Exception {
        URLRewriteMediator mediator = new URLRewriteMediator();
        mediator.setOutputProperty("outURL");

        RewriteAction action = new RewriteAction();
        action.setValue(targetURL);

        RewriteRule rule = new RewriteRule();
        EqualEvaluator eval = new EqualEvaluator();
        URLTextRetriever txtRtvr = new URLTextRetriever();
        txtRtvr.setSource(EvaluatorConstants.URI_FRAGMENTS.port.name());
        eval.setTextRetriever(txtRtvr);
        eval.setValue("8280");
        rule.setCondition(eval);
        rule.addRewriteAction(action);
        mediator.addRule(rule);

        MessageContext msgCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>");
        msgCtx.setTo(new EndpointReference("http://localhost:8280"));
        mediator.mediate(msgCtx);

        assertEquals(targetURL, msgCtx.getProperty("outURL"));
    }

    public void testConditionalRewriteScenario3() throws Exception {
        URLRewriteMediator mediator = new URLRewriteMediator();
        mediator.setOutputProperty("outURL");

        RewriteAction action1 = new RewriteAction();
        action1.setValue("localhost");
        action1.setFragmentIndex(URIFragments.HOST);
        RewriteRule rule1 = new RewriteRule();
        rule1.addRewriteAction(action1);
        EqualEvaluator eval1 = new EqualEvaluator();
        URLTextRetriever txtRtvr1 = new URLTextRetriever();
        txtRtvr1.setSource(EvaluatorConstants.URI_FRAGMENTS.host.name());
        eval1.setTextRetriever(txtRtvr1);
        eval1.setValue("myhost");
        rule1.setCondition(eval1);
        mediator.addRule(rule1);

        RewriteAction action2 = new RewriteAction();
        action2.setValue("/services/SimpleStockQuoteService");
        action2.setFragmentIndex(URIFragments.PATH);

        RewriteAction action3 = new RewriteAction();
        action3.setXpath(new SynapseXPath("get-property('port')"));
        action3.setFragmentIndex(URIFragments.PORT);

        RewriteRule rule2 = new RewriteRule();
        rule2.addRewriteAction(action2);
        rule2.addRewriteAction(action3);
        MatchEvaluator eval2 = new MatchEvaluator();
        URLTextRetriever txtRtvr2 = new URLTextRetriever();
        txtRtvr2.setSource(EvaluatorConstants.URI_FRAGMENTS.path.name());
        eval2.setTextRetriever(txtRtvr2);
        eval2.setRegex(Pattern.compile(".*/MyService"));
        rule2.setCondition(eval2);
        mediator.addRule(rule2);

        MessageContext msgCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>");
        msgCtx.setTo(new EndpointReference("http://myhost:8280/MyService"));
        msgCtx.setProperty("port", 9000);
        mediator.mediate(msgCtx);

        assertEquals(targetURL, msgCtx.getProperty("outURL"));
    }

    public void testConditionalRewriteScenario4() throws Exception {
        URLRewriteMediator mediator = new URLRewriteMediator();
        mediator.setOutputProperty("outURL");

        RewriteAction action1 = new RewriteAction();
        action1.setRegex("MyService");
        action1.setValue("SimpleStockQuoteService");
        action1.setFragmentIndex(URIFragments.PATH);
        action1.setActionType(RewriteAction.ACTION_REPLACE);
        RewriteRule rule1 = new RewriteRule();
        rule1.addRewriteAction(action1);
        EqualEvaluator eval1 = new EqualEvaluator();
        SOAPEnvelopeTextRetriever txtRtvr1 = new SOAPEnvelopeTextRetriever("//symbol");
        eval1.setTextRetriever(txtRtvr1);
        eval1.setValue("IBM");
        rule1.setCondition(eval1);
        mediator.addRule(rule1);

        MessageContext msgCtx = TestUtils.createLightweightSynapseMessageContext(
                "<getQuote><symbol>IBM</symbol></getQuote>");
        msgCtx.setTo(new EndpointReference("http://localhost:9000/services/MyService"));
        mediator.mediate(msgCtx);

        assertEquals(targetURL, msgCtx.getProperty("outURL"));
    }

    public void testFullRewriteScenario1() throws Exception {
        String xml =
                "<rewrite xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "    <rewriterule>\n" +
                "        <condition>\n" +
                "            <and>\n" +
                "                <equal type=\"url\" source=\"protocol\" value=\"http\"/>\n" +
                "                <equal type=\"url\" source=\"host\" value=\"test.org\"/>\n" +
                "            </and>\n" +
                "        </condition>\n" +
                "        <action value=\"https\" fragment=\"protocol\"/>\n" +
                "        <action value=\"test.com\" fragment=\"host\"/>\n" +
                "        <action value=\"9443\" fragment=\"port\"/>\n" +
                "    </rewriterule>\n" +
                "    <rewriterule>\n" +
                "        <condition>\n" +
                "            <not>\n" +
                "                <match type=\"url\" source=\"path\" regex=\"/services/.*\"/>\n" +
                "            </not>\n" +
                "        </condition>\n" +
                "        <action value=\"/services\" type=\"prepend\" fragment=\"path\"/>\n" +
                "    </rewriterule>\n" +
                "    <rewriterule>\n" +
                "        <condition>\n" +
                "            <and>\n" +
                "               <match type=\"url\" source=\"path\" regex=\".*/MyService\"/>\n" +
                "               <equal type=\"property\" source=\"prop1\" value=\"value1\"/>\n" +
                "            </and>\n" +
                "        </condition>        \n" +
                "        <action fragment=\"path\" value=\"StockQuoteService\" regex=\"MyService\" type=\"replace\"/>\n" +
                "        <action fragment=\"ref\" value=\"id\"/>\n" +
                "    </rewriterule>\n" +
                "</rewrite>";

        OMElement element = AXIOMUtil.stringToOM(xml);
        URLRewriteMediator mediator = (URLRewriteMediator) new URLRewriteMediatorFactory().
                createMediator(element, new Properties());

        MessageContext msgCtx = TestUtils.createLightweightSynapseMessageContext("<empty/>");
        msgCtx.setTo(new EndpointReference("http://test.org:9763/MyService"));
        msgCtx.setProperty("prop1", "value1");
        mediator.mediate(msgCtx);

        assertEquals("https://test.com:9443/services/StockQuoteService#id",
                msgCtx.getTo().getAddress());
    }
}
