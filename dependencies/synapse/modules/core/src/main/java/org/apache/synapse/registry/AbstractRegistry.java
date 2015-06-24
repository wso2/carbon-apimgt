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

package org.apache.synapse.registry;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.template.TemplateMediator;

import java.util.Properties;

/**
 * Implements the core Registry lookup algorithm
 */
public abstract class AbstractRegistry implements Registry {

    private static final Log log = LogFactory.getLog(AbstractRegistry.class);

    /** The list of configuration properties */
    protected final Properties properties = new Properties();

    /**
     * Get the resource for the given key from this registry
     * @param entry The Enrty instance that contains meta-data
     * @param properties bag of properties with additional information
     * @return the matching resultant object
     */
    public Object getResource(Entry entry, Properties properties) {

        OMNode omNode = null;
        RegistryEntry re = null;

        // we are dealing with a dynamic resource. Have we seen this before and processed
        // it at least once and have it cached already?

        // if we have an unexpired cached copy, return the cached object
        if (entry.isCached() && !entry.isExpired()) {
            return entry.getValue();

        // if we have not cached the referenced object, fetch it and its RegistryEntry
        } else if (!entry.isCached()) {
            try {
                omNode = lookup(entry.getKey());
                entry.setEntryProperties(getResourceProperties(entry.getKey()));
            } catch (OMException e) {
                log.error("Error reading registry resource file : " + entry.getKey(), e);
            }

            if (omNode == null && entry.getEntryProperties() != null &&
                    !entry.getEntryProperties().isEmpty()) {
                // Collection
                re = getRegistryEntry(entry.getKey());
                if (re != null) {
                    if (re.getCachableDuration() > 0) {
                        entry.setExpiryTime(System.currentTimeMillis() + re.getCachableDuration());
                    } else {
                        entry.setExpiryTime(-1);
                    }
                    entry.setVersion(re.getVersion());
                }
            }

            if (omNode == null) {
                return null;
            } else {
                re = getRegistryEntry(entry.getKey());
            }

        // if we have cached it before, and now the cache has expired
        // get its *new* registry entry and compare versions and pick new cache duration
        } else if (entry.isExpired()) {
            if (log.isDebugEnabled()) {
                log.debug("Cached object has expired for key : " + entry.getKey());
            }
            re = getRegistryEntry(entry.getKey());

            if (re.getVersion() != Long.MIN_VALUE &&
                re.getVersion() == entry.getVersion()) {
                if (log.isDebugEnabled()) {
                    log.debug("Expired version number is same as current version in registry");
                }

                // renew cache lease for another cachable duration (as returned by the
                // new getRegistryEntry() call
                if (re.getCachableDuration() > 0) {
                    entry.setExpiryTime(
                            System.currentTimeMillis() + re.getCachableDuration());
                } else {
                    entry.setExpiryTime(-1);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Renew cache lease for another " + re.getCachableDuration() / 1000 +
                            "s");
                }

                // return cached object
                return entry.getValue();

            } else {
                omNode = lookup(entry.getKey());
                entry.setEntryProperties(getResourceProperties(entry.getKey()));

                if (omNode == null && entry.getEntryProperties() != null &&
                        !entry.getEntryProperties().isEmpty()) {
                    // Collection
                    re = getRegistryEntry(entry.getKey());
                    if (re != null) {
                        if (re.getCachableDuration() > 0) {
                            entry.setExpiryTime(System.currentTimeMillis() +
                                    re.getCachableDuration());
                        } else {
                            entry.setExpiryTime(-1);
                        }
                        entry.setVersion(re.getVersion());
                    }
                }

                if (omNode == null) {
                    return null;
                }

            }
        }

        // if we get here, we have received the raw omNode from the
        // registry and our previous copy (if we had one) has expired or is not valid
        Object expiredValue = entry.getValue();

        // if we have a XMLToObjectMapper for this entry, use it to convert this
        // resource into the appropriate object - e.g. sequence or endpoint
        if (entry.getMapper() != null) {
            entry.setValue(entry.getMapper().getObjectFromOMNode(omNode, properties));

            if (entry.getValue() instanceof SequenceMediator) {
                SequenceMediator seq = (SequenceMediator) entry.getValue();
                seq.setDynamic(true);
                seq.setRegistryKey(entry.getKey());
            } else if (entry.getValue() instanceof Endpoint) {
                Endpoint ep = (Endpoint) entry.getValue();
            } else if (entry.getValue() instanceof TemplateMediator) {
                ((TemplateMediator) entry.getValue()).setDynamic(true);
            }
        } else {
            // if the type of the object is known to have a mapper, create the
            // resultant Object using the known mapper, and cache this Object
            // else cache the raw OMNode
            if (re != null && re.getType() != null) {

                XMLToObjectMapper mapper = getMapper(re.getType());
                if (mapper != null) {
                    entry.setMapper(mapper);
                    entry.setValue(mapper.getObjectFromOMNode(omNode, properties));

                } else {
                    entry.setValue(omNode);
                }
            }
        }

        if (expiredValue != null) {
            // Destroy the old resource so that everything is properly cleaned up
            if (expiredValue instanceof SequenceMediator) {
                ((SequenceMediator) expiredValue).destroy();
            } else if (expiredValue instanceof Endpoint) {
                ((Endpoint) expiredValue).destroy();
            }
        }

        // increment cache expiry time as specified by the last getRegistryEntry() call
        if (re != null) {
            if (re.getCachableDuration() > 0) {
                entry.setExpiryTime(System.currentTimeMillis() + re.getCachableDuration());
            } else {
                entry.setExpiryTime(-1);
            }
            entry.setVersion(re.getVersion());
        }

        return entry.getValue();
    }

    private XMLToObjectMapper getMapper(String type) {
        return null;
    }

    public String getProviderClass() {
        return this.getClass().getName();
    }

    public Properties getConfigurationProperties() {
        return properties;
    }

    public void init(Properties properties) {
        this.properties.putAll(properties);
    }

    public Properties getResourceProperties(String entryKey) {
        return null;
    }

    /**
     *
     * @param entry
     * @return the OMElement of the format from the configuration
     */
   public OMElement getFormat(Entry entry){
        OMNode omNode;
        omNode = lookupFormat(entry.getKey());
       return (OMElement) omNode;
   }
}
