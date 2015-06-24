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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.transaction.TransactionMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * The Factory for create transaction mediator- InLine XML need to provide
 * <p/>
 * <pre>
 * &lt;transaction action="new|use-existing-or-new|fault-if-no-tx|commit|rollback|suspend|resume" /&gt;
 * <p/>
 * <p/>
 */
public class TransactionMediatorFactory extends AbstractMediatorFactory {

    private static final QName TAG_NAME
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "transaction");

    /**
     * Create a Transaction mediator instance referring to the bean and configuration given
     * by the OMElement declaration
     *
     * @param elem the OMElement that specifies the Transaction mediator configuration
     * @param properties
     * @return the Transaction mediator instance created
     */
    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        TransactionMediator tm = new TransactionMediator();
        OMAttribute action
                = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "action"));

        if (action == null) {
            handleException("The 'action' attribute " +
                    "is required for Transaction mediator definition");
        } else {

            // after successfully creating the mediator
            // set its common attributes such as tracing etc
            processAuditStatus(tm, elem);
            tm.setAction(action.getAttributeValue());

            return tm;
        }

        return null;
    }

    public QName getTagQName() {
        return TAG_NAME;
    }

}
