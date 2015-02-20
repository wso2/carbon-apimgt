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
import org.apache.synapse.mediators.builtin.RespondMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

public class RespondMediatorFactory extends AbstractMediatorFactory {

    private static final QName Respond_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "respond");

    @Override
    protected Mediator createSpecificMediator(OMElement elem, Properties properties) {
        Mediator responseMediator = new RespondMediator();
        processAuditStatus(responseMediator,elem);
        return responseMediator;
    }

    public QName getTagQName() {
        return Respond_Q;
    }
}
