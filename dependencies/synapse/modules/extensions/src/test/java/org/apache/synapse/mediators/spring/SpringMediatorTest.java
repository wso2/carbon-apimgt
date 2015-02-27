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

package org.apache.synapse.mediators.spring;

import junit.framework.TestCase;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfigurationBuilder;
import org.apache.synapse.mediators.TestUtils;

import java.util.Properties;

/**
 * This unit test is a different 'type' of a unit test, such that it tests end-to-end
 * like scenario of using Spring extensions! First it tests that the configuration
 * builder properly looks up specified named and anonymous spring configurations
 * and mediates properly to Spring mediator beans. The public static invokeCounter field
 * though ugly, serves the purpose to test that the Spring beans were properly created
 * and invoked
 */
public class SpringMediatorTest extends TestCase {

    public void testSpringBean() throws Exception {

        MessageContext msgCtx = TestUtils.getTestContext("<dummy/>");
        msgCtx.setConfiguration(
            SynapseConfigurationBuilder.getConfiguration("./../../repository/conf/sample/resources/spring/synapse_spring_unittest.xml", new Properties()));
        msgCtx.getMainSequence().mediate(msgCtx);

        assertEquals(TestMediateHandlerImpl.invokeCount, 202);
    }

}
