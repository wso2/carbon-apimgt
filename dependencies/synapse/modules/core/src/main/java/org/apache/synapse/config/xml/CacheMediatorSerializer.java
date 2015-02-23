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
import org.apache.synapse.mediators.builtin.CacheMediator;

/**
 * Serializes the Cache mediator to the XML configuration specified
 * <p/>
 * <pre>
 * &lt;cache [id="string"] [hashGenerator="class"] [timeout="seconds"]
 *      [scope=(per-host | per-mediator)] collector=(true | false) [maxMessageSize="in-bytes"]&gt;
 *   &lt;onCacheHit [sequence="key"]&gt;
 *     (mediator)+
 *   &lt;/onCacheHit&gt;?
 *   &lt;implementation type=(memory | disk) maxSize="int"/&gt;
 * &lt;/cache&gt;
 * </pre>
 */
public class CacheMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof CacheMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }
        CacheMediator mediator = (CacheMediator) m;
        OMElement cache = fac.createOMElement("cache", synNS);
        saveTracingState(cache, mediator);

        if (mediator.getId() != null) {
            cache.addAttribute(fac.createOMAttribute("id", nullNS, mediator.getId()));
        }

        if (mediator.getScope() != null) {
            cache.addAttribute(fac.createOMAttribute("scope", nullNS, mediator.getScope()));
        }

        if (mediator.isCollector()) {
            cache.addAttribute(fac.createOMAttribute("collector", nullNS, "true"));
        } else {

            cache.addAttribute(fac.createOMAttribute("collector", nullNS, "false"));

            if (mediator.getDigestGenerator() != null) {
                cache.addAttribute(fac.createOMAttribute("hashGenerator", nullNS,
                    mediator.getDigestGenerator().getClass().getName()));
            }

            if (mediator.getTimeout() != 0) {
                cache.addAttribute(
                    fac.createOMAttribute("timeout", nullNS, Long.toString(mediator.getTimeout())));
            }

            if (mediator.getMaxMessageSize() != 0) {
                cache.addAttribute(
                    fac.createOMAttribute("maxMessageSize", nullNS,
                        Integer.toString(mediator.getMaxMessageSize())));
            }

            if (mediator.getOnCacheHitRef() != null) {
                OMElement onCacheHit = fac.createOMElement("onCacheHit", synNS);
                onCacheHit.addAttribute(
                    fac.createOMAttribute("sequence", nullNS, mediator.getOnCacheHitRef()));
                cache.addChild(onCacheHit);
            } else if (mediator.getOnCacheHitSequence() != null) {
                OMElement onCacheHit = fac.createOMElement("onCacheHit", synNS);
                new SequenceMediatorSerializer()
                    .serializeChildren(onCacheHit, mediator.getOnCacheHitSequence().getList());
                cache.addChild(onCacheHit);
            }

            if (mediator.getInMemoryCacheSize() != 0) {
                OMElement implElem = fac.createOMElement("implementation", synNS);
                implElem.addAttribute(fac.createOMAttribute("type", nullNS, "memory"));
                implElem.addAttribute(fac.createOMAttribute("maxSize", nullNS,
                    Integer.toString(mediator.getInMemoryCacheSize())));
                cache.addChild(implElem);
            }

            if (mediator.getDiskCacheSize() != 0) {
                OMElement implElem = fac.createOMElement("implementation", synNS);
                implElem.addAttribute(fac.createOMAttribute("type", nullNS, "disk"));
                implElem.addAttribute(fac.createOMAttribute("maxSize", nullNS,
                    Integer.toString(mediator.getDiskCacheSize())));
                cache.addChild(implElem);
            }
        }

        return cache;
    }

    public String getMediatorClassName() {
        return CacheMediator.class.getName();
    }
}
