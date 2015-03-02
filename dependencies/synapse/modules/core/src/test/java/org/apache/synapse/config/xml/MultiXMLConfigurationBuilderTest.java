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

package org.apache.synapse.config.xml;

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.axis2.util.JavaUtils;

public class MultiXMLConfigurationBuilderTest extends TestCase {

    public void testConfigurationBuilder() throws Exception {
        URL u = this.getClass().getClassLoader().getResource("synapse-config");
        String root = new File(u.toURI()).getAbsolutePath();

        System.out.println("Using SYNAPSE_CONFIG_HOME=" + root);
        SynapseConfiguration synapseConfig =
                MultiXMLConfigurationBuilder.getConfiguration(root, new Properties());

        assertNotNull(synapseConfig.getDefinedSequences().get("main"));
        assertNotNull(synapseConfig.getDefinedSequences().get("fault"));
        SequenceMediator foo = synapseConfig.getDefinedSequences().get("foo");
        SequenceMediator seq1 = synapseConfig.getDefinedSequences().get("synapse_xml_seq1");
        assertNotNull(foo);
        assertNotNull(seq1);
        assertEquals("foo.xml", foo.getFileName());
        assertNull(seq1.getFileName());
        assertNull(synapseConfig.getDefinedSequences().get("bar"));

        assertNotNull(synapseConfig.getDefinedEndpoints().get("epr1"));
        assertNotNull(synapseConfig.getDefinedEndpoints().get("synapse_xml_epr1"));

        assertNotNull(synapseConfig.getProxyService("proxy1"));

        assertNotNull(synapseConfig.getStartup("task1"));

        assertNotNull(synapseConfig.getRegistry());
        assertTrue(JavaUtils.isTrueExplicitly(synapseConfig.getProperty(
                MultiXMLConfigurationBuilder.SEPARATE_REGISTRY_DEFINITION)));
    }
}
