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

import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.synapse.config.SynapseConfiguration;
import org.apache.axiom.om.OMElement;

/**
 * This interface defines the configuration serializers of Synapse.
 */
public interface ConfigurationSerializer {

    /**
     * Serializes the given configuration to an OMElement.
     *
     * @param synCfg Configuration to be serialized
     * @return OMElement describing the configuraiton
     */
    OMElement serializeConfiguration(SynapseConfiguration synCfg);

    /**
     * Get the tag QName of the element
     *
     * @return QName describing the element name
     */
    QName getTagQName();

}
