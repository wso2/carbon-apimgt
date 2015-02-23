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
import org.apache.axiom.om.OMText;
import org.apache.synapse.Mediator;


import org.apache.synapse.mediators.elementary.EnrichMediator;
import org.apache.synapse.mediators.elementary.Source;
import org.apache.synapse.mediators.elementary.Target;


public class EnrichMediatorSerializer extends AbstractMediatorSerializer {

    @Override
    protected OMElement serializeSpecificMediator(Mediator m) {
        assert m != null : "mediator cannot be null";
        assert m instanceof EnrichMediator : "mediator should be of type EnrichMediator";

        EnrichMediator mediator = (EnrichMediator) m;

        OMElement enrichEle = fac.createOMElement("enrich", synNS);

        OMElement sourceEle = serializeSource(mediator.getSource());
        OMElement targetEle = serializeTarget(mediator.getTarget());

        enrichEle.addChild(sourceEle);
        enrichEle.addChild(targetEle);

        return enrichEle;
    }

    private OMElement serializeSource(Source source) {
        OMElement sourceEle = fac.createOMElement("source", synNS);

        if (source.getSourceType() != EnrichMediator.CUSTOM) {
            sourceEle.addAttribute(fac.createOMAttribute("type", nullNS,
                    intTypeToString(source.getSourceType())));
        }

        if (source.isClone()) {
            sourceEle.addAttribute(fac.createOMAttribute("clone", nullNS,
                    Boolean.toString(source.isClone())));
        }

        if (source.getSourceType() == EnrichMediator.PROPERTY) {
            sourceEle.addAttribute(fac.createOMAttribute("property", nullNS, source.getProperty()));
        } else if (source.getSourceType() == EnrichMediator.CUSTOM) {
            SynapseXPathSerializer.serializeXPath(source.getXpath(), sourceEle, "xpath");
        } else if (source.getSourceType() == EnrichMediator.INLINE) {
            if (source.getInlineOMNode() instanceof OMElement) {
                sourceEle.addChild(((OMElement) source.getInlineOMNode()).cloneOMElement());
            } else if (source.getInlineOMNode() instanceof OMText) {
                /*Text as inline content*/
                sourceEle.setText(((OMText) source.getInlineOMNode()).getText());
            } else if (source.getInlineKey() != null) {
                sourceEle.addAttribute("key", source.getInlineKey(), null);
            }
        }
        return sourceEle;
    }

    private OMElement serializeTarget(Target target) {
        OMElement targetEle = fac.createOMElement("target", synNS);

        if (target.getTargetType() != EnrichMediator.CUSTOM) {
            targetEle.addAttribute(fac.createOMAttribute("type", nullNS,
                    intTypeToString(target.getTargetType())));
        }

        if (!target.getAction().equals(Target.ACTION_REPLACE)) {
            targetEle.addAttribute(fac.createOMAttribute("action", nullNS,
                    target.getAction()));
        }

        if (target.getTargetType() == EnrichMediator.PROPERTY) {
            targetEle.addAttribute(fac.createOMAttribute("property", nullNS, target.getProperty()));
        } else if (target.getTargetType() == EnrichMediator.CUSTOM) {
            SynapseXPathSerializer.serializeXPath(target.getXpath(), targetEle, "xpath");
        }

        return targetEle;
    }


    private String intTypeToString(int type) {
        if (type == EnrichMediator.CUSTOM) {
            return EnrichMediatorFactory.CUSTOM;
        } else if (type == EnrichMediator.BODY) {
            return EnrichMediatorFactory.BODY;
        } else if (type == EnrichMediator.ENVELOPE) {
            return EnrichMediatorFactory.ENVELOPE;
        } else if (type == EnrichMediator.PROPERTY) {
            return EnrichMediatorFactory.PROPERTY;
        } else if (type == EnrichMediator.INLINE) {
            return EnrichMediatorFactory.INLINE;
        }
        return null;
    }

    public String getMediatorClassName() {
        return EnrichMediator.class.getName();
    }
}
