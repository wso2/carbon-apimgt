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
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.base.SynapseMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Builds the main mediator (@see SynapseConfiguration) of the Synapse instance
 *
 * <pre>
 * &lt;rules&gt;
 *   mediator+
 * &lt;rules&gt;
 * </pre>
 */
public class SynapseMediatorFactory extends AbstractListMediatorFactory {

    private final static QName RULES_Q = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "rules");

    public QName getTagQName() {
        return RULES_Q;
    }

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {
        SynapseMediator sm = new SynapseMediator();

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(sm,elem);

        addChildren(elem, sm, properties);
        return sm;
    }

}
