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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.eip.splitter.IterateMediator;
import org.apache.synapse.mediators.eip.Target;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * The &lt;iterate&gt; element is used to split messages in Synapse to smaller messages with only
 * one part of the elements described in the XPATH expression.
 * <p/>
 * <pre>
 * &lt;iterate [continueParent=(true | false)] [preservePayload=(true | false)]
 *          (attachPath="xpath")? expression="xpath"&gt;
 *   &lt;target [to="uri"] [soapAction="qname"] [sequence="sequence_ref"]
 *          [endpoint="endpoint_ref"]&gt;
 *     &lt;sequence&gt;
 *       (mediator)+
 *     &lt;/sequence&gt;?
 *     &lt;endpoint&gt;
 *       endpoint
 *     &lt;/endpoint&gt;?
 *   &lt;/target&gt;+
 * &lt;/iterate&gt;
 * </pre>
 */
public class IterateMediatorFactory extends AbstractMediatorFactory {

    private static final Log log = LogFactory.getLog(IterateMediatorFactory.class);

    /**
     * Holds the QName for the IterateMediator xml configuration
     */
    private static final QName ITERATE_Q = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "iterate");
    private static final QName ATT_CONTPAR = new QName("continueParent");
    private static final QName ATT_PREPLD = new QName("preservePayload");
    private static final QName ATT_ATTACHPATH = new QName("attachPath");
    private static final QName ATT_SEQUENCIAL = new QName("sequential");

    private static final QName ID_Q
            = new QName(XMLConfigConstants.NULL_NAMESPACE, "id");

    /**
     * This method will create the IterateMediator by parsing the given xml configuration
     *
     * @param elem OMElement describing the configuration of the IterateMediaotr
     * @param properties properties passed
     * @return IterateMediator created from the given configuration
     */
    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        IterateMediator mediator = new IterateMediator();
        processAuditStatus(mediator, elem);

        OMAttribute id = elem.getAttribute(ID_Q);
        if (id != null) {
            mediator.setId(id.getAttributeValue());
        }

        OMAttribute continueParent = elem.getAttribute(ATT_CONTPAR);
        if (continueParent != null) {
            mediator.setContinueParent(
                    Boolean.valueOf(continueParent.getAttributeValue()));
        }

        OMAttribute preservePayload = elem.getAttribute(ATT_PREPLD);
        if (preservePayload != null) {
            mediator.setPreservePayload(
                    Boolean.valueOf(preservePayload.getAttributeValue()));
        }

        OMAttribute expression = elem.getAttribute(ATT_EXPRN);
        if (expression != null) {
            try {
                mediator.setExpression(SynapseXPathFactory.getSynapseXPath(elem, ATT_EXPRN));
            } catch (JaxenException e) {
                handleException("Unable to build the IterateMediator. " + "Invalid XPATH " +
                    expression.getAttributeValue(), e);
            }
        } else {
            handleException("XPATH expression is required " +
                "for an IterateMediator under the \"expression\" attribute");
        }

        OMAttribute attachPath = elem.getAttribute(ATT_ATTACHPATH);
        String attachPathValue = ".";
        if (attachPath != null && !mediator.isPreservePayload()) {
            handleException("Wrong configuration for the iterate mediator :: if the iterator " +
                "should not preserve payload, then attachPath can not be present");
        } else if (attachPath != null) {
            attachPathValue = attachPath.getAttributeValue();
        }
        
        try {
            SynapseXPath xp = new SynapseXPath(attachPathValue);
            OMElementUtils.addNameSpaces(xp, elem, log);
            mediator.setAttachPath(xp);
        } catch (JaxenException e) {
            handleException("Unable to build the IterateMediator. Invalid XPATH " +
                attachPathValue, e);
        }

        boolean asynchronous = true;
        OMAttribute asynchronousAttr = elem.getAttribute(ATT_SEQUENCIAL);
        if (asynchronousAttr != null && asynchronousAttr.getAttributeValue().equals("true")) {
            asynchronous = false;
        }

        OMElement targetElement = elem.getFirstChildWithName(TARGET_Q);
        if (targetElement != null) {
            Target target = TargetFactory.createTarget(targetElement, properties);
            if (target != null) {
                target.setAsynchronous(asynchronous);
                mediator.setTarget(target);
            }
        } else {
            handleException("Target for an iterate mediator is required :: missing target");
        }

        return mediator;
    }

    /**
     * Get the IterateMediator configuration tag name
     *
     * @return QName specifying the IterateMediator tag name of the xml configuration
     */
    public QName getTagQName() {
        return ITERATE_Q;
    }
}
