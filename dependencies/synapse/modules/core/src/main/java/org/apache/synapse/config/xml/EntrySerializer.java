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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.Entry;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseConstants;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.synapse.config.SynapsePropertiesLoader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import java.net.URL;

/**
 * Serializer for {@link Entry} instances.
 */
public class EntrySerializer {

    private static Log log = LogFactory.getLog(EntrySerializer.class);

    protected static final OMFactory fac = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;
    protected static final OMNamespace nullNS
            = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    /**
     * Serialize the Entry object to an OMElement representing the entry
     *
     * @param entry
     * @param parent
     * @return OMElement representing the entry
     */
    public static OMElement serializeEntry(Entry entry, OMElement parent) {
        String customFactory = SynapsePropertiesLoader.getPropertyValue("synapse.entry.serializer", "");
        if (customFactory != null && !"".equals(customFactory)) {
            try {
                Class c = Class.forName(customFactory);
                Object o = c.newInstance();
                if (o instanceof IEntrySerializer) {
                    return ((IEntrySerializer) o).serializeEntry(entry, parent);
                }
            } catch (ClassNotFoundException e) {
                handleException("Class specified by the synapse.entry.factory " +
                        "synapse property not found: " + customFactory, e);
            } catch (InstantiationException e) {
                handleException("Class specified by the synapse.entry.factory " +
                        "synapse property cannot be instantiated: " + customFactory, e);
            } catch (IllegalAccessException e) {
                handleException("Class specified by the synapse.entry.factory " +
                        "synapse property cannot be accessed: " + customFactory, e);
            }
        }

        OMElement entryElement = fac.createOMElement("localEntry", synNS);

        entryElement.addAttribute(fac.createOMAttribute(
                "key", nullNS, entry.getKey().trim()));
        int type = entry.getType();
        if (type == Entry.URL_SRC) {
            URL srcUrl = entry.getSrc();
            if (srcUrl != null) {
                entryElement.addAttribute(fac.createOMAttribute(
                        "src", nullNS, srcUrl.toString().trim()));
            }
        } else if (type == Entry.INLINE_XML) {
            Object value = entry.getValue();
            if (value != null && value instanceof OMElement) {
                entryElement.addChild((OMElement) value);
            }
        } else if (type == Entry.INLINE_TEXT) {
            Object value = entry.getValue();
            if (value != null && value instanceof String) {
                OMTextImpl textData = (OMTextImpl) fac.createOMText(((String) value).trim());
                textData.setType(XMLStreamConstants.CDATA);
                entryElement.addChild(textData);
            }
        } else if (type == Entry.REMOTE_ENTRY) {
            // nothing to serialize
            return null;
        } else {
            handleException("Entry type undefined");
        }

        if (entry.getDescription() != null) {
            OMElement descriptionElem = fac.createOMElement(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "description"), entryElement);
            descriptionElem.setText(entry.getDescription());
            entryElement.addChild(descriptionElem);
        }

        if (parent != null) {
            parent.addChild(entryElement);
        }
        return entryElement;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
