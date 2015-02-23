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

package org.apache.synapse.experimental;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import java.util.Properties;

public class ReplaceMediatorFactory extends AbstractMediatorFactory {

    private static final QName TAG_NAME
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "replace");
    private static final QName ATT_PROPERTY = new QName("property");

    public QName getTagQName() {
        return TAG_NAME;
    }

    public ReplaceMediator createSpecificMediator(OMElement elem, Properties properties) {
        ReplaceMediator mediator = new ReplaceMediator();

        OMAttribute attTarget = elem.getAttribute(ATT_TARGET);
        OMAttribute attProperty = elem.getAttribute(ATT_PROPERTY);
        
        if (attTarget != null) {
            try {
                mediator.setTarget(new SynapseXPath(attTarget));
            } catch (JaxenException e) {
                handleException("Invalid XPath specified for the target attribute : " +
                    attTarget.getAttributeValue());
            }
        }
        
        if (attProperty != null) {
            mediator.setProperty(attProperty.getAttributeValue());
        } else {
            handleException("The 'property' attribute is required for the replace mediator");
        }
        
        return mediator;
    }
}
