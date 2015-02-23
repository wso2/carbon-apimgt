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

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * A mediator factory capable of creating an instance of a {@link org.apache.synapse.Mediator}
 * through a given XML should implement this interface</p>
 *
 * <p>It is recommended to extend the abstract class
 * {@link org.apache.synapse.config.xml.AbstractMediatorFactory} or the
 * {@link org.apache.synapse.config.xml.AbstractListMediatorFactory} instead of
 * implementing this interface
 *
 * @see org.apache.synapse.Mediator
 * @see org.apache.synapse.config.xml.AbstractMediatorFactory
 */
public interface MediatorFactory {
    /**
     * Creates an instance of the mediator using the OMElement
     * @param elem configuration element describing the mediator properties
     * @param properties bag of properties to pass in any information to the factory
     * @return the created mediator
     */
    public Mediator createMediator(OMElement elem, Properties properties);

    /**
     * The QName of this mediator element in the XML config
     * @return QName of the mediator element
     */
    public QName getTagQName();
}
