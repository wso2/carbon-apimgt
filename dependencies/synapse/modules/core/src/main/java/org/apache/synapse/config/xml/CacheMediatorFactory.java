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

import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.builtin.CacheMediator;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.wso2.caching.CachingConstants;
import org.wso2.caching.digest.DigestGenerator;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

/**
 * Creates an instance of a Cache mediator using XML configuration specified
 *
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
public class CacheMediatorFactory extends AbstractMediatorFactory {

    private static final QName CACHE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "cache");
    private static final QName ATT_ID = new QName("id");
    private static final QName ATT_COLLECTOR = new QName("collector");
    private static final QName ATT_HASH_GENERATOR = new QName("hashGenerator");
    private static final QName ATT_MAX_MSG_SIZE = new QName("maxMessageSize");
    private static final QName ATT_TIMEOUT = new QName("timeout");
    private static final QName ATT_SCOPE = new QName("scope");
    private static final QName ATT_SEQUENCE = new QName("sequence");
    private static final QName ATT_TYPE = new QName("type");
    private static final QName ATT_SIZE = new QName("maxSize");
    private static final QName ON_CACHE_HIT_Q =
        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "onCacheHit");
    private static final QName IMPLEMENTATION_Q =
        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "implementation");
    private static final long DEFAULT_TIMEOUT = 5000L;
    private static final int DEFAULT_DISK_CACHE_SIZE = 200;

    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        if (!CACHE_Q.equals(elem.getQName())) {
            handleException("Unable to create the cache mediator. " +
                "Unexpected element as the cache mediator configuration");
        }

        CacheMediator cache = new CacheMediator();

        OMAttribute idAttr = elem.getAttribute(ATT_ID);
        if (idAttr != null && idAttr.getAttributeValue() != null) {
            cache.setId(idAttr.getAttributeValue());
        }

        OMAttribute scopeAttr = elem.getAttribute(ATT_SCOPE);
        if (scopeAttr != null && scopeAttr.getAttributeValue() != null &&
            isValidScope(scopeAttr.getAttributeValue(), cache.getId())) {
            cache.setScope(scopeAttr.getAttributeValue());
        } else {
            cache.setScope(CachingConstants.SCOPE_PER_HOST);
        }

        OMAttribute collectorAttr = elem.getAttribute(ATT_COLLECTOR);
        if (collectorAttr != null && collectorAttr.getAttributeValue() != null &&
            "true".equals(collectorAttr.getAttributeValue())) {

            cache.setCollector(true);
        } else {
            
            cache.setCollector(false);

            OMAttribute hashGeneratorAttr = elem.getAttribute(ATT_HASH_GENERATOR);
            if (hashGeneratorAttr != null && hashGeneratorAttr.getAttributeValue() != null) {
                try {
                    Class generator = Class.forName(hashGeneratorAttr.getAttributeValue());
                    Object o = generator.newInstance();
                    if (o instanceof DigestGenerator) {
                        cache.setDigestGenerator((DigestGenerator) o);
                    } else {
                        handleException("Specified class for the hashGenerator is not a " +
                            "DigestGenerator. It *must* implement " +
                            "org.wso2.caching.digest.DigestGenerator interface");
                    }
                } catch (ClassNotFoundException e) {
                    handleException("Unable to load the hash generator class", e);
                } catch (IllegalAccessException e) {
                    handleException("Unable to access the hash generator class", e);
                } catch (InstantiationException e) {
                    handleException("Unable to instantiate the hash generator class", e);
                }
            }

            OMAttribute timeoutAttr = elem.getAttribute(ATT_TIMEOUT);
            if (timeoutAttr != null && timeoutAttr.getAttributeValue() != null) {
                cache.setTimeout(Long.parseLong(timeoutAttr.getAttributeValue()));
            } else {
                cache.setTimeout(DEFAULT_TIMEOUT);
            }

            OMAttribute maxMessageSizeAttr = elem.getAttribute(ATT_MAX_MSG_SIZE);
            if (maxMessageSizeAttr != null && maxMessageSizeAttr.getAttributeValue() != null) {
                cache.setMaxMessageSize(Integer.parseInt(maxMessageSizeAttr.getAttributeValue()));
            }

            OMElement onCacheHitElem = elem.getFirstChildWithName(ON_CACHE_HIT_Q);
            if (onCacheHitElem != null) {
                OMAttribute sequenceAttr = onCacheHitElem.getAttribute(ATT_SEQUENCE);
                if (sequenceAttr != null && sequenceAttr.getAttributeValue() != null) {
                    cache.setOnCacheHitRef(sequenceAttr.getAttributeValue());
                } else if (onCacheHitElem.getFirstElement() != null) {
                    cache.setOnCacheHitSequence(new SequenceMediatorFactory()
                            .createAnonymousSequence(onCacheHitElem, properties));
                }
            }

            for (Iterator itr = elem.getChildrenWithName(IMPLEMENTATION_Q); itr.hasNext();) {
                OMElement implElem = (OMElement) itr.next();
                OMAttribute typeAttr = implElem.getAttribute(ATT_TYPE);
                OMAttribute sizeAttr = implElem.getAttribute(ATT_SIZE);
                if (typeAttr != null && typeAttr.getAttributeValue() != null) {
                    String type = typeAttr.getAttributeValue();
                    if (CachingConstants.TYPE_MEMORY.equals(type) && sizeAttr != null &&
                        sizeAttr.getAttributeValue() != null) {
                        cache.setInMemoryCacheSize(Integer.parseInt(sizeAttr.getAttributeValue()));
                    } else if (CachingConstants.TYPE_DISK.equals(type)) {
                        log.warn("Disk based and hirearchycal caching is not implemented yet");
                        if (sizeAttr != null && sizeAttr.getAttributeValue() != null) {
                            cache.setDiskCacheSize(Integer.parseInt(sizeAttr.getAttributeValue()));
                        } else {
                            cache.setDiskCacheSize(DEFAULT_DISK_CACHE_SIZE);
                        }
                    } else {
                        handleException("unknown implementation type for the Cache mediator");
                    }
                }
            }
        }

        return cache;
    }

    private boolean isValidScope(String scope, String id) {
        if (CachingConstants.SCOPE_PER_HOST.equals(scope)) {
            return true;
        } else if (CachingConstants.SCOPE_PER_MEDIATOR.equals(scope)) {
            if (id != null) {
                return true;
            } else {
                handleException("Id is required for a cache wirth scope : " + scope);
                return false;
            }
        } else if (CachingConstants.SCOPE_DISTRIBUTED.equals(scope)) {
            handleException("Scope distributed is not supported yet by the Cache mediator");
            return false;
        } else {
            handleException("Unknown scope " + scope + " for the Cache mediator");
            return false;
        }
    }

    public QName getTagQName() {
        return CACHE_Q;
    }
}
