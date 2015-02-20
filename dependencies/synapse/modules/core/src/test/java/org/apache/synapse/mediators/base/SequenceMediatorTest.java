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

package org.apache.synapse.mediators.base;

import junit.framework.TestCase;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.SynapseMessageReceiver;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.mediators.TestMediateHandler;
import org.apache.synapse.mediators.TestMediator;
import org.apache.synapse.mediators.TestUtils;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.context.ConfigurationContext;

public class SequenceMediatorTest extends TestCase {

    private StringBuffer result = new StringBuffer();

    public void testSequenceMediator() throws Exception {

        TestMediator t1 = new TestMediator();
        t1.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    result.append("T1.");
                }
            });
        TestMediator t2 = new TestMediator();
        t2.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    result.append("T2.");
                }
            });
        TestMediator t3 = new TestMediator();
        t3.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    result.append("T3");
                }
            });

        SequenceMediator seq = new SequenceMediator();
        seq.addChild(t1);
        seq.addChild(t2);
        seq.addChild(t3);

        // invoke transformation, with static enveope
        MessageContext synCtx = TestUtils.getTestContext("<empty/>");
        seq.mediate(synCtx);

        assertTrue("T1.T2.T3".equals(result.toString()));
    }

    public void testErrorHandling() throws Exception {

        TestMediator t1 = new TestMediator();
        t1.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    result.append("T1.");
                }
            });
        TestMediator t2 = new TestMediator();
        t2.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    result.append("T2.");
                    throw new SynapseException("test");
                }
            });
        TestMediator t3 = new TestMediator();
        t3.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    result.append("T3.");
                }
            });
        TestMediator t4 = new TestMediator();
        t4.setHandler(
            new TestMediateHandler() {
                public void handle(MessageContext synCtx) {
                    result.append("T4");
                    assertEquals("test", synCtx.getProperty(SynapseConstants.ERROR_MESSAGE));
                }
            });

        SequenceMediator seq = new SequenceMediator();
        seq.addChild(t1);
        seq.addChild(t2);
        seq.addChild(t3);
        seq.setErrorHandler("myErrorHandler");

        SequenceMediator seqErr = new SequenceMediator();
        seqErr.setName("myErrorHandler");
        seqErr.addChild(t4);

        // invoke transformation, with static enveope
        SynapseConfiguration synConfig = new SynapseConfiguration();
        synConfig.addSequence("myErrorHandler", seqErr);
        synConfig.addSequence(SynapseConstants.MAIN_SEQUENCE_KEY, seq);

        MessageContextCreatorForAxis2.setSynConfig(synConfig);
        MessageContextCreatorForAxis2.setSynEnv(new Axis2SynapseEnvironment(synConfig));
        org.apache.axis2.context.MessageContext mc =
            new org.apache.axis2.context.MessageContext();
        AxisConfiguration axisConfig = synConfig.getAxisConfiguration();
        if (axisConfig == null) {
            axisConfig = new AxisConfiguration();
            synConfig.setAxisConfiguration(axisConfig);
        }
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        mc.setConfigurationContext(cfgCtx);
        mc.setEnvelope(TestUtils.getTestContext("<empty/>").getEnvelope());

        new SynapseMessageReceiver().receive(mc);

        assertTrue("T1.T2.T4".equals(result.toString()));
    }
}
