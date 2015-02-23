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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;

import org.apache.synapse.mediators.elementary.EnrichMediator;
import org.apache.synapse.mediators.elementary.Source;
import org.apache.synapse.mediators.elementary.Target;

import java.util.Properties;


public class EnrichMediatorFactory extends AbstractMediatorFactory {
    private static final QName XML_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "enrich");
    private static final QName ATT_PROPERTY = new QName("property");
    private static final QName ATT_XPATH = new QName("xpath");
    private static final QName ATT_TYPE = new QName("type");
    private static final QName ATT_CLONE = new QName("clone");
    private static final QName ATT_ACTION = new QName("action");

    public static final QName SOURCE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "source");
    public static final QName TARGET_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "target");

    public static final String CUSTOM = "custom";
    public static final String PROPERTY = "property";
    public static final String ENVELOPE = "envelope";
    public static final String BODY = "body";
    public static final String INLINE = "inline";

    @Override
    protected Mediator createSpecificMediator(OMElement elem, Properties properties) {
        if (!XML_Q.equals(elem.getQName())) {
            handleException("Unable to create the enrich mediator. " +
                    "Unexpected element as the enrich mediator configuration");
        }

        EnrichMediator enrich = new EnrichMediator();

        OMElement sourceEle = elem.getFirstChildWithName(SOURCE_Q);
        if (sourceEle == null) {
            handleException("source element is mandatory");
        }
        Source source = new Source();
        enrich.setSource(source);

        OMElement targetEle = elem.getFirstChildWithName(TARGET_Q);
        if (targetEle == null) {
            handleException("target element is mandatory");
        }
        Target target = new Target();
        enrich.setTarget(target);

        populateSource(source, sourceEle);
        populateTarget(target, targetEle);

        return enrich;
    }

    private void populateSource(Source source, OMElement sourceEle) {

        // type attribue
        OMAttribute typeAttr = sourceEle.getAttribute(ATT_TYPE);
        if (typeAttr != null && typeAttr.getAttributeValue() != null) {
            source.setSourceType(convertTypeToInit(typeAttr.getAttributeValue()));
        }

        OMAttribute cloneAttr = sourceEle.getAttribute(ATT_CLONE);
        if (cloneAttr != null && cloneAttr.getAttributeValue() != null) {
            source.setClone(Boolean.parseBoolean(cloneAttr.getAttributeValue()));
        }

        if (source.getSourceType() == EnrichMediator.CUSTOM) {
            OMAttribute xpathAttr = sourceEle.getAttribute(ATT_XPATH);
            if (xpathAttr != null && xpathAttr.getAttributeValue() != null) {
                try {
                    source.setXpath(SynapseXPathFactory.getSynapseXPath(sourceEle, ATT_XPATH));
                } catch (JaxenException e) {
                    handleException("Invalid XPath expression: " + xpathAttr);
                }
            } else {
                handleException("xpath attribute is required for CUSTOM type");
            }
        } else if (source.getSourceType() == EnrichMediator.PROPERTY) {
            OMAttribute propertyAttr = sourceEle.getAttribute(ATT_PROPERTY);
            if (propertyAttr != null && propertyAttr.getAttributeValue() != null) {
                source.setProperty(propertyAttr.getAttributeValue());
            } else {
                handleException("xpath attribute is required for CUSTOM type");
            }
        } else if (source.getSourceType() == EnrichMediator.INLINE) {
            OMElement inlineElem = null;
            if (sourceEle.getFirstElement() != null) {
                inlineElem = sourceEle.getFirstElement().cloneOMElement();
            }

            if (inlineElem != null) {
                source.setInlineOMNode(inlineElem);
            } else if (sourceEle.getText() != null
                    && (!sourceEle.getText().equals(""))) {
                source.setInlineOMNode(OMAbstractFactory.getOMFactory().createOMText(sourceEle.getText()));
            } else if (sourceEle.getAttributeValue(ATT_KEY) != null) {
                source.setInlineKey(sourceEle.getAttributeValue(ATT_KEY));
            } else {
                handleException("XML element is required for INLINE type");
            }
        }
    }

    private void populateTarget(Target target, OMElement sourceEle) {
        // type attribute
        OMAttribute typeAttr = sourceEle.getAttribute(ATT_TYPE);
        if (typeAttr != null && typeAttr.getAttributeValue() != null) {
            int type = convertTypeToInit(typeAttr.getAttributeValue());
            if (type >= 0) {
                target.setTargetType(type);
            } else {
                handleException("Un-expected type : " + typeAttr.getAttributeValue());
            }
        }

        OMAttribute actionAttr = sourceEle.getAttribute(ATT_ACTION);
        if (actionAttr != null && actionAttr.getAttributeValue() != null) {
            target.setAction(actionAttr.getAttributeValue());
        }

        if (target.getTargetType() == EnrichMediator.CUSTOM) {
            OMAttribute xpathAttr = sourceEle.getAttribute(ATT_XPATH);
            if (xpathAttr != null && xpathAttr.getAttributeValue() != null) {
                try {
                    target.setXpath(SynapseXPathFactory.getSynapseXPath(sourceEle, ATT_XPATH));
                } catch (JaxenException e) {
                    handleException("Invalid XPath expression: " + xpathAttr);
                }
            } else {
                handleException("xpath attribute is required for CUSTOM type");
            }
        } else if (target.getTargetType() == EnrichMediator.PROPERTY) {
            OMAttribute propertyAttr = sourceEle.getAttribute(ATT_PROPERTY);
            if (propertyAttr != null && propertyAttr.getAttributeValue() != null) {
                target.setProperty(propertyAttr.getAttributeValue());
            } else {
                handleException("xpath attribute is required for CUSTOM type");
            }
        }
    }

    private int convertTypeToInit(String type) {
        if (type.equals(ENVELOPE)) {
            return EnrichMediator.ENVELOPE;
        } else if (type.equals(BODY)) {
            return EnrichMediator.BODY;
        } else if (type.equals(PROPERTY)) {
            return EnrichMediator.PROPERTY;
        } else if (type.equals(CUSTOM)) {
            return EnrichMediator.CUSTOM;
        } else if (type.equals(INLINE)) {
            return EnrichMediator.INLINE;
        }
        return -1;
    }

    public QName getTagQName() {
        return XML_Q;
    }

}
