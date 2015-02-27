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
import org.apache.synapse.mediators.transform.XSLTMediator;
import org.apache.synapse.mediators.Value;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Properties;

/**
 * Factory for {@link XSLTMediator} instances.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;xslt key="property-key" [source="xpath"] [target="string"]&gt;
 *   &lt;property name="string" (value="literal" | expression="xpath")/&gt;*
 *   &lt;feature name="string" value="true| false" /&gt;*
 *   &lt;attribute name="string" value="string" /&gt;*
 *   &lt;resource location="..." key="..."/&gt;*
 * &lt;/transform&gt;
 * </pre>
 */
public class XSLTMediatorFactory extends AbstractMediatorFactory {

    private static final QName TAG_NAME
                = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "xslt");
    private static final QName ATTRIBUTE_Q
                = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");

    public QName getTagQName() {
        return TAG_NAME;
    }

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        XSLTMediator transformMediator = new XSLTMediator();

        OMAttribute attXslt   = elem.getAttribute(ATT_KEY);
        OMAttribute attSource = elem.getAttribute(ATT_SOURCE);
        OMAttribute attTarget = elem.getAttribute(ATT_TARGET);

        if (attXslt != null) {
            // ValueFactory for creating dynamic or static Value
            ValueFactory keyFac = new ValueFactory();
            // create dynamic or static key based on OMElement
            Value generatedKey = keyFac.createValue(XMLConfigConstants.KEY, elem);

            // set generated key as the Value
            transformMediator.setXsltKey(generatedKey);
        } else {
            handleException("The '" + XMLConfigConstants.KEY + "' " +
                    "attribute is required for the XSLT mediator");
        }

        if (attSource != null) {
            try {
                transformMediator.setSourceXPathString(attSource.getAttributeValue());
                transformMediator.setSource(SynapseXPathFactory.getSynapseXPath(elem, ATT_SOURCE));

            } catch (JaxenException e) {
                handleException("Invalid XPath specified for the source attribute : " +
                    attSource.getAttributeValue());
            }
        }

        if (attTarget != null) {
            transformMediator.setTargetPropertyName(attTarget.getAttributeValue());    
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        processAuditStatus(transformMediator, elem);
        // set the features 
        for (Map.Entry<String,String> entry : collectNameValuePairs(elem, FEATURE_Q).entrySet()) {
            String value = entry.getValue();
            boolean isFeatureEnabled;
            if ("true".equals(value)) {
                isFeatureEnabled = true;
            } else if ("false".equals(value)) {
                isFeatureEnabled = false;
            } else {
                handleException("The feature must have value true or false");
                break;
            }
            transformMediator.addFeature(entry.getKey(), isFeatureEnabled);
        }
        for (Map.Entry<String,String> entry : collectNameValuePairs(elem, ATTRIBUTE_Q).entrySet()) {
            transformMediator.addAttribute(entry.getKey(), entry.getValue());
        }
        transformMediator.addAllProperties(
            MediatorPropertyFactory.getMediatorProperties(elem));

        transformMediator.setResourceMap(ResourceMapFactory.createResourceMap(elem));

        return transformMediator;
    }
}
