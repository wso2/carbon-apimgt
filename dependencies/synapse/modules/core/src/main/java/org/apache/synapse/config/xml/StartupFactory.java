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
import org.apache.synapse.Startup;

/**
 * Defines the factories which builds startups
 */
public interface StartupFactory {

    /**
     * Create (build from OM) from the specified OMElement
     *
     * @param elem
     *          OMELement describing the Startup
     * @return Startup build from the given element
     */
    public Startup createStartup(OMElement elem);

    /**
     * Get the tag QName of the element
     *
     * @return QName of the element
     */
    public QName getTagQName();

    /**
     * Get the Serializer class for this factory
     *
     * @return Class defining the serialization of the startup
     */
    public Class<? extends StartupSerializer> getSerializerClass();
}
