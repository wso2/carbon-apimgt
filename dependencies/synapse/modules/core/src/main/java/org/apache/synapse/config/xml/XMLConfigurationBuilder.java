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

import java.io.InputStream;
import java.util.Properties;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;

import javax.xml.stream.XMLStreamException;

/**
 * Builds a Synapse Configuration from an XML input stream
 */
public class XMLConfigurationBuilder {

    private static Log log = LogFactory.getLog(XMLConfigurationBuilder.class);

    public static SynapseConfiguration getConfiguration(InputStream is, Properties properties)
            throws XMLStreamException {

        log.info("Generating the Synapse configuration model by parsing the XML configuration");
        
        OMElement definitions = new StAXOMBuilder(is).getDocumentElement();
        definitions.build();

        return ConfigurationFactoryAndSerializerFinder.getInstance()
                .getConfiguration(definitions, properties);
        
    }
}