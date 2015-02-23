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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Properties;

/**
 *
 *
 */

public abstract class AbstractTestCase extends XMLTestCase {

    private static final Log log = LogFactory.getLog(AbstractTestCase.class);

    public AbstractTestCase(String name) {
        super(name);
    }

    public AbstractTestCase() {
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected OMElement createOMElement(String xml) {
        return SynapseConfigUtils.stringToOM(xml);
    }

    protected boolean serialization(String inputXml, MediatorFactory mediatorFactory, MediatorSerializer mediatorSerializer) {

        OMElement inputOM = createOMElement(inputXml);
        Mediator mediator = mediatorFactory.createMediator(inputOM, new Properties());
        OMElement resultOM = mediatorSerializer.serializeMediator(null, mediator);
        try {
            assertXMLEqual(resultOM.toString(), inputXml);
            return true;
        } catch (SAXException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
        return false;
    }

    protected boolean serialization(String inputXml, MediatorSerializer mediatorSerializer) {
        OMElement inputOM = createOMElement(inputXml);
        Mediator mediator = MediatorFactoryFinder.getInstance().getMediator(inputOM, new Properties());
        OMElement resultOM = mediatorSerializer.serializeMediator(null, mediator);
        try {
            assertXMLEqual(resultOM.toString(), inputXml);
            return true;
        } catch (SAXException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
        return false;
    }

    protected OMElement getParent() {
        String parentXML = "<synapse xmlns=\"http://ws.apache.org/ns/synapse\"><definitions></definitions></synapse>";
        return createOMElement(parentXML);
    }

    protected boolean compare(OMElement inputElement, OMElement serializedElement)  {
        try {
            
            assertXMLEqual(inputElement.toString(), serializedElement.toString());
            return true;
        } catch (SAXException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
        return false;
    }
}
