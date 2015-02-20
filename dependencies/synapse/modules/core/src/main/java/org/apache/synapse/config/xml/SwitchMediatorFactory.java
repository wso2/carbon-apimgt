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
import org.apache.synapse.SynapseException;
import org.apache.synapse.mediators.filters.SwitchMediator;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Factory for {@link SwitchMediator} instances.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;switch source="xpath"&gt;
 *   &lt;case regex="string"&gt;
 *     mediator+
 *   &lt;/case&gt;+
 *   &lt;default&gt;
 *     mediator+
 *   &lt;/default&gt;?
 * &lt;/switch&gt;
 * </pre>
 */
public class SwitchMediatorFactory extends AbstractMediatorFactory {

    private static final QName SWITCH_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "switch");
    private static final QName CASE_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "case");
    private static final QName DEFAULT_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "default");

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        SwitchMediator switchMediator = new SwitchMediator();
        OMAttribute source = elem.getAttribute(ATT_SOURCE);
        if (source == null) {
            String msg = "A 'source' XPath attribute is required for a switch mediator";
            log.error(msg);
            throw new SynapseException(msg);
        } else {
            try {

                switchMediator.setSource(SynapsePathFactory.getSynapsePath(elem, ATT_SOURCE));

            } catch (JaxenException e) {
                String msg = "Invalid XPath for attribute 'source' : " + source.getAttributeValue();
                log.error(msg);
                throw new SynapseException(msg);
            }
        }
        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(switchMediator, elem);
        Iterator iter = elem.getChildrenWithName(CASE_Q);
        while (iter.hasNext()) {
            OMElement caseElem = (OMElement) iter.next();
            SwitchCase aCase = new SwitchCase();
            OMAttribute regex = caseElem.getAttribute(ATT_REGEX);
            if (regex == null) {
                String msg = "The 'regex' attribute is required for a switch case definition";
                log.error(msg);
                throw new SynapseException(msg);
            }
            try {
                aCase.setRegex(Pattern.compile(regex.getAttributeValue()));
            } catch (PatternSyntaxException pse) {
                String msg = "Invalid Regular Expression for attribute 'regex' : "
                        + regex.getAttributeValue();
                log.error(msg);
                throw new SynapseException(msg);
            }
            aCase.setCaseMediator(AnonymousListMediatorFactory.createAnonymousListMediator(
                    caseElem, properties));
            switchMediator.addCase(aCase);
        }
        iter = elem.getChildrenWithName(DEFAULT_Q);
        while (iter.hasNext()) {
            SwitchCase aCase = new SwitchCase();
            aCase.setCaseMediator(AnonymousListMediatorFactory.createAnonymousListMediator(
                    (OMElement) iter.next(), properties));
            switchMediator.setDefaultCase(aCase);
            break; // add only the *first* default if multiple are specified, ignore rest if any
        }
        return switchMediator;
    }

    public QName getTagQName() {
        return SWITCH_Q;
    }
}
