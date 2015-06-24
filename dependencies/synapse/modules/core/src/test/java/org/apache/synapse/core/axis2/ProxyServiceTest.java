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

package org.apache.synapse.core.axis2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import junit.framework.TestCase;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.util.resolver.ResourceMap;
import org.xml.sax.InputSource;

public class ProxyServiceTest extends TestCase {
    /**
     * Test that a proxy service without publishWSDL will produce a meaningful WSDL.
     * This is a regression test for SYNAPSE-366.
     */
    public void testWSDLWithoutPublishWSDL() throws Exception {
        // Build the proxy service
        SynapseConfiguration synCfg = new SynapseConfiguration();
        AxisConfiguration axisCfg = new AxisConfiguration();
        ProxyService proxyService = new ProxyService("Test");
        AxisService axisService = proxyService.buildAxisService(synCfg, axisCfg);
        // Serialize the WSDL
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        axisService.printWSDL(baos);
        // Check that the produced WSDL can be read by WSDL4J
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(baos.toByteArray())));
    }
    
    /**
     * Test a proxy with a WSDL importing another WSDL importing an XSD. The imported WSDL and
     * XSD documents are resolved using a {@link ResourceMap} (i.e. &lt;resource> elements). The
     * test checks that the proxy service can be built and produce a WSDL.
     * This is a test for the feature introduced by SYNAPSE-200 and a regression test
     * for SYNAPSE-362.
     */
    public void testWSDLWithPublishWSDLAndRecursiveImports() throws Exception {
        SynapseConfiguration synCfg = new SynapseConfiguration();
        AxisConfiguration axisCfg = new AxisConfiguration();
        // Add local entries
        Entry entry = new Entry();
        entry.setType(Entry.URL_SRC);
        entry.setSrc(getClass().getResource("root.wsdl"));
        synCfg.addEntry("root_wsdl", entry);
        entry = new Entry();
        entry.setType(Entry.URL_SRC);
        entry.setSrc(getClass().getResource("imported.xsd"));
        synCfg.addEntry("imported_xsd", entry);
        entry = new Entry();
        entry.setType(Entry.URL_SRC);
        entry.setSrc(getClass().getResource("imported.wsdl"));
        synCfg.addEntry("imported_wsdl", entry);
        // Build the proxy service
        ProxyService proxyService = new ProxyService("Test");
        proxyService.setWSDLKey("root_wsdl");
        ResourceMap resourceMap = new ResourceMap();
        resourceMap.addResource("imported.wsdl", "imported_wsdl");
        resourceMap.addResource("imported.xsd", "imported_xsd");
        proxyService.setResourceMap(resourceMap);
        AxisService axisService = proxyService.buildAxisService(synCfg, axisCfg);
        // Serialize the WSDL. Note that we can't parse the WSDL because it will have imports
        // referring to locations such as "my-matches?xsd=xsd0.xsd".
        axisService.printWSDL(new ByteArrayOutputStream());
    }
    
    /**
     * Test a proxy service with recursive imports and without a {@link ResourceMap}.
     * Regression test for SYNAPSE-442.
     */
    public void testRecursiveImports2() throws Exception {
        ProxyService testService = new ProxyService("mytest");
        SynapseConfiguration synCfg = new SynapseConfiguration();
        AxisConfiguration axisCfg = new AxisConfiguration();
        testService.setWsdlURI(getClass().getResource("SimpleStockService.wsdl").toURI());
        testService.buildAxisService(synCfg, axisCfg);
    }
}
