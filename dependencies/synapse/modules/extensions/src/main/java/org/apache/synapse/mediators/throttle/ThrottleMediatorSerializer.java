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
package org.apache.synapse.mediators.throttle;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.config.xml.SequenceMediatorSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.base.SequenceMediator;

/**
 * The Serializer for Throttle Mediator  saving throttle instance
 *
 * <pre>
 * &lt;throttle [onReject="string"] [onAccept="string"] id="string"&gt;
 *   (&lt;policy key="string"/&gt; | &lt;policy&gt;..&lt;/policy&gt;)
 *    &lt;onReject&gt;..&lt;/onReject&gt;?
 *    &lt;onAccept&gt;..&lt;/onAccept&gt;?
 * &lt;/throttle&gt;
 * </pre>
 */

public class ThrottleMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {
        if (!(m instanceof ThrottleMediator)) {
            handleException("Invalid Mediator has passed to serializer");
        }
        ThrottleMediator throttleMediator = (ThrottleMediator) m;
        OMElement throttle = fac.createOMElement("throttle", synNS);
        OMElement policy = fac.createOMElement("policy", synNS);
        String key = throttleMediator.getPolicyKey();
        if (key != null) {
            policy.addAttribute(fac.createOMAttribute(
                    "key", nullNS, key));
            throttle.addChild(policy);
        } else {
            OMNode inlinePolicy = throttleMediator.getInLinePolicy();
            if (inlinePolicy != null) {
                policy.addChild(inlinePolicy);
                throttle.addChild(policy);
            }
        }
        saveTracingState(throttle, throttleMediator);

        String id = throttleMediator.getId();
        if (id != null) {
            throttle.addAttribute(fac.createOMAttribute(
                    "id", nullNS, id));
        }

        String onReject = throttleMediator.getOnRejectSeqKey();
        if (onReject != null) {
            throttle.addAttribute(fac.createOMAttribute(XMLConfigConstants.ONREJECT, nullNS,
                    onReject));
        } else {
            Mediator mediator = throttleMediator.getOnRejectMediator();
            SequenceMediatorSerializer serializer = new SequenceMediatorSerializer();
            if (mediator != null && mediator instanceof SequenceMediator) {
                OMElement element = serializer.serializeAnonymousSequence(null,
                        (SequenceMediator) mediator);
                element.setLocalName(XMLConfigConstants.ONREJECT);
                throttle.addChild(element);
            }
        }
        String onAccept = throttleMediator.getOnAcceptSeqKey();
        if (onAccept != null) {
            throttle.addAttribute(fac.createOMAttribute(XMLConfigConstants.ONACCEPT, nullNS,
                    onAccept));
        } else {
            Mediator mediator = throttleMediator.getOnAcceptMediator();
            SequenceMediatorSerializer serializer = new SequenceMediatorSerializer();
            if (mediator != null && mediator instanceof SequenceMediator) {
                OMElement element = serializer.serializeAnonymousSequence(null,
                        (SequenceMediator) mediator);
                element.setLocalName(XMLConfigConstants.ONACCEPT);
                throttle.addChild(element);
            }
        }

        return throttle;

    }

    public String getMediatorClassName() {
        return ThrottleMediator.class.getName();
    }
}
