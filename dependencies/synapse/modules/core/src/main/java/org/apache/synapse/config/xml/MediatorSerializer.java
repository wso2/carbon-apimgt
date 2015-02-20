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

/**
 * Interface which should be implemented by mediator serializers. Does the
 * reverse of the MediatorFactory
 */
public interface MediatorSerializer {

    /**
     * Return the XML representation of this mediator
     * @param m mediator to be serialized
     * @param parent the OMElement to which the serialization should be attached
     * @return the serialized mediator XML
     */
    public OMElement serializeMediator(OMElement parent, Mediator m);

    /**
     * Return the class name of the mediator which can be serialized
     * @return the class name 
     */
    public String getMediatorClassName();
}
