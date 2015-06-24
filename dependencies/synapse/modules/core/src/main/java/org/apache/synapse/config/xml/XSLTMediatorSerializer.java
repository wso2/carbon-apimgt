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
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.mediators.transform.XSLTMediator;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Serializer for {@link XSLTMediator} instances.
 * 
 * @see XSLTMediatorFactory
 */
public class XSLTMediatorSerializer extends AbstractMediatorSerializer {

    private static final QName ATTRIBUTE_Q
                = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof XSLTMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }

        XSLTMediator mediator = (XSLTMediator) m;
        OMElement xslt = fac.createOMElement("xslt", synNS);

        if (mediator.getXsltKey() != null) {
            // Serialize Value using ValueSerializer
            ValueSerializer keySerializer =  new ValueSerializer();
            keySerializer.serializeValue(mediator.getXsltKey(), XMLConfigConstants.KEY, xslt);
        } else {
            handleException("Invalid XSLT mediator. XSLT registry key is required");
        }
        saveTracingState(xslt,mediator);

        if (mediator.getSource() != null) {

            SynapseXPathSerializer.serializeXPath(mediator.getSource(), xslt, "source");
        }
        if (mediator.getTargetPropertyName() != null) {
            xslt.addAttribute(fac.createOMAttribute(
                "target", nullNS, mediator.getTargetPropertyName()));
        }
        serializeProperties(xslt, mediator.getProperties());
        List<MediatorProperty> features = mediator.getFeatures();
        if (!features.isEmpty()) {
            for (MediatorProperty mp : features) {
                OMElement prop = fac.createOMElement("feature", synNS, xslt);
                if (mp.getName() != null) {
                    prop.addAttribute(fac.createOMAttribute("name", nullNS, mp.getName()));
                } else {
                    handleException("The Feature name is missing");
                }
                if (mp.getValue() != null) {
                    prop.addAttribute(fac.createOMAttribute("value", nullNS, mp.getValue()));
                }  else {
                    handleException("The Feature value is missing");
                }
            }
        }
        serializeMediatorProperties(xslt, mediator.getAttributes(), ATTRIBUTE_Q);
        
        ResourceMapSerializer.serializeResourceMap(xslt, mediator.getResourceMap());
        
        return xslt;
    }

    public String getMediatorClassName() {
        return XSLTMediator.class.getName();
    }
}
