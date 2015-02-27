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
import org.apache.synapse.mediators.db.DBLookupMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Factory for {@link DBLookupMediator} instances.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;dblookup&gt;
 *   &lt;connection&gt;
 *     &lt;pool&gt;
 *      (
 *       &lt;driver/&gt;
 *       &lt;url/&gt;
 *       &lt;user/&gt;
 *       &lt;password/&gt;
 *     |
 *       &lt;dsName/&gt;
 *       &lt;icClass/&gt;
 *       &lt;url/&gt;
 *       &lt;user/&gt;
 *       &lt;password/&gt;
 *     )
 *       &lt;property name="name" value="value"/&gt;*
 *     &lt;/pool&gt;
 *   &lt;/connection&gt;
 *   &lt;statement&gt;
 *     &lt;sql&gt;select something from table where something_else = ?&lt;/sql&gt;
 *     &lt;parameter [value="" | expression=""] type="int|string"/&gt;*
 *     &lt;result name="string" column="int|string"/&gt;*
 *   &lt;/statement&gt;+
 * &lt;/dblookup&gt;
 * </pre>
 */
public class DBLookupMediatorFactory extends AbstractDBMediatorFactory {

    private static final QName DBLOOKUP_Q =
        new QName(SynapseConstants.SYNAPSE_NAMESPACE, "dblookup");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        DBLookupMediator mediator = new DBLookupMediator();
        buildDataSource(elem, mediator);
        processStatements(elem, mediator);
        return mediator;
    }

    public QName getTagQName() {
        return DBLOOKUP_Q;
    }
}
