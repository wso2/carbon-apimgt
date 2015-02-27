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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfiguration;

import java.util.Properties;

/**
 * This interface defines the configuration factories of Synapse
 */
public interface ConfigurationFactory {

    /**
     * Get the tag QName of the element piece that will be
     * build using the factory
     *
     * @return QName describing the element
     */
    QName getTagQName();

    /**
     * Get (basically builds) the configuration of Synapse built up from
     * an OMElement using the defined factory
     *
     * @param element OMElement describing the configuration to be build
     * @return SynapseConfiguration build using the relevant factory
     */
    SynapseConfiguration getConfiguration(OMElement element, Properties properties);

    /**
     * Get the class which serializes the specified element
     *
     * @return Class defining the Serializer
     */
    Class getSerializerClass();
}
