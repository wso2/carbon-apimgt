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
import org.apache.synapse.Startup;

/**
 * Defines the serialization of Startups
 */
public interface StartupSerializer {

    /**
     * Serializes the Startup to an OMElement and
     * attaches as a child to the provided parent OMElement
     *
     * @param parent  - OMElement to which, serialized startup will be attached
     * @param startup - Startup to be serialized
     * @return Serialized OMElement
     */
    public OMElement serializeStartup(OMElement parent, Startup startup);
}