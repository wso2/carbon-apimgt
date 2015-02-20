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

package org.apache.synapse.eventing.filters;

import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.eventing.Event;

public class EventFilterTest extends TestCase {

    public void testTopicBasedEventFilter() {
        String status = "snow";
        try {
            MessageContext msgCtx = TestUtils.getAxis2MessageContext("<weatherCondition>" +
                    status + "</weatherCondition>", null).
                    getAxis2MessageContext();
            Event<MessageContext> event = new Event<MessageContext>();
            event.setMessage(msgCtx);

            TopicBasedEventFilter filter = new TopicBasedEventFilter();
            filter.setResultValue(status);
            filter.setSourceXpath(new SynapseXPath("//weatherCondition"));
            assertTrue(filter.match(event));

            filter.setResultValue("rain");
            assertFalse(filter.match(event));            
        } catch (Exception e) {
            fail("Error while constructing the test message context: " + e.getMessage());
        }
    }

    public void testXPathBasedEventFilter() {
        String status = "snow";
        try {
            org.apache.synapse.MessageContext msgCtx =
                    TestUtils.createLightweightSynapseMessageContext("<weatherCondition>" +
                            status + "</weatherCondition>");

            XPathBasedEventFilter filter = new XPathBasedEventFilter();
            filter.setResultValue(status);
            filter.setSourceXpath(new SynapseXPath("//weatherCondition"));
            assertTrue(filter.isSatisfied(msgCtx));

            filter.setResultValue("rain");
            assertFalse(filter.isSatisfied(msgCtx));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error while constructing the test message context: " + e.getMessage());
        }
    }
    
}
